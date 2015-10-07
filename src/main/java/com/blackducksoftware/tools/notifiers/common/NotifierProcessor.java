/*******************************************************************************
 * Copyright (C) 2015 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 only
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *******************************************************************************/
package com.blackducksoftware.tools.notifiers.common;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.blackducksoftware.tools.commonframework.connector.protex.ProtexServerWrapper;
import com.blackducksoftware.tools.commonframework.core.exception.CommonFrameworkException;
import com.blackducksoftware.tools.commonframework.standard.common.ProjectPojo;
import com.blackducksoftware.tools.commonframework.standard.email.EmailContentMap;
import com.blackducksoftware.tools.commonframework.standard.email.EmailTriggerRule;
import com.blackducksoftware.tools.commonframework.standard.protex.ProtexProjectPojo;
import com.blackducksoftware.tools.notifiers.common.NotifierRuleEnums.EN_RULE;

/**
 * Main processing class for the notifier Invokes the Protex client from the
 * supplied configuration Creates appropriates handler.
 *
 * @author akamen
 *
 */
public class NotifierProcessor {

    private final Logger log = Logger
	    .getLogger(this.getClass().getSimpleName());

    private final NotifierConfig config;
    private final String projectName; // Protex Project name, and default for
				      // project name used in notification
    private final String projectAlias; // Project name used in notification, if
				       // different (or null if not)
    private final ProtexServerWrapper<ProtexProjectPojo> psw;

    private final EmailContentMap keysOnlyContentMap;

    private final IHandler notificationHandler;

    /**
     * Location of the config file. Project name for Black Duck Suite (Protex,
     * CC, etc?)
     *
     * @param configFile
     * @param projectName
     * @param templateFileLocation
     * @throws Exception
     */
    public NotifierProcessor(NotifierConfig config,
	    IHandler notificationHandler, EmailContentMap keysOnlyContentMap,
	    String projectName, String projectAlias) throws Exception {

	this.keysOnlyContentMap = keysOnlyContentMap;
	this.config = config;
	this.notificationHandler = notificationHandler;

	this.projectName = projectName;
	this.projectAlias = projectAlias;
	psw = new ProtexServerWrapper<>(config.getServerBean(), config, true);
    }

    /**
     * Connects to the Protex instance specified in the configuration Checks
     * total pending identifications Collections pending ID information.
     *
     * Warning: Testing has shown that process() can only be called once. If you
     * need to call it again, create a new NotifierProcessor object.
     *
     * @throws Exception
     */
    public EmailContentMap process() throws Exception {
	ProtexInformationCollector pic = null;
	log.info("Checking pending counts for project: " + projectName);
	ProjectPojo protexProject = psw.getProjectByName(projectName);
	ProjectInfo projectInfo = new ProjectInfo(protexProject, projectAlias);
	log.info("Project exists, last analyzed: "
		+ protexProject.getAnalyzedDate());

	pic = new ProtexInformationCollector(psw, config);

	// Populate the content map based on the keys within the map
	EmailContentMap content = notificationHandler.populateContentMap(
		projectInfo.getProjectPojo(), pic, keysOnlyContentMap);

	log.info("DONE collecting content from Protex.");
	String mapStringData = getStringFromContentMap(content);
	log.debug("Content: " + mapStringData);

	// Based on notification trigger rules, populate the content.
	List<EmailTriggerRule> rules = processRules(pic, content);

	// Create the notification based on Handler type (will depend on config)
	notificationHandler.processNotification(content, projectInfo, rules);

	log.info("Finished general processing.");
	return content;
    }

    /**
     * Processes rules for notification.
     */
    public List<EmailTriggerRule> processRules(ProtexInformationCollector pic,
	    EmailContentMap content) throws CommonFrameworkException {
	List<EmailTriggerRule> rules = config
		.getNotificationRulesConfiguration().getRules();

	// No rules, no notification. Simple.
	if (rules == null || rules.size() == 0) {
	    log.warn("No trigger rules setup!");
	    return rules;
	}

	for (EmailTriggerRule rule : rules) {
	    if (EN_RULE.CONFLICTS == EN_RULE.valueOf(rule.getRuleName())) {
		if (pic.isContainsConflicts()) {
		    String body = HandlerUtils.mergeComponentsIntoBody(pic
			    .getComponentMap());
		    body = HandlerUtils.buildHtmlDescription(body);
		    content.put(NotifierConstants.PROTEX_COMP_LIST, body);

		    rule.setRuleTriggered(true);
		}

	    }
	    if (EN_RULE.PENDING == EN_RULE.valueOf(rule.getRuleName())) {
		if (pic.isContainsPending()) {
		    rule.setRuleTriggered(true);
		}
	    }
	    if (EN_RULE.NON_APPROVED == EN_RULE.valueOf(rule.getRuleName())) {
		if (pic.isContainsNonApprovedComponents()) {
		    String body = HandlerUtils.mergeComponentsIntoBody(pic
			    .getComponentMap());
		    body = HandlerUtils.buildHtmlDescription(body);
		    content.put(NotifierConstants.PROTEX_NON_APPROVED_LIST,
			    body);
		    rule.setRuleTriggered(true);
		}
	    }
	}

	return rules;
    }

    private String getStringFromContentMap(Map<String, String> content) {
	StringBuilder sb = new StringBuilder();
	for (String key : content.keySet()) {
	    String value = content.get(key);
	    sb.append(key + ":" + value + "\n");
	}

	return sb.toString();
    }
}

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
package com.blackducksoftware.tools.notifiers.email;

import java.util.List;

import org.apache.log4j.Logger;

import com.blackducksoftware.tools.commonframework.standard.common.ProjectPojo;
import com.blackducksoftware.tools.commonframework.standard.email.CFEmailNotifier;
import com.blackducksoftware.tools.commonframework.standard.email.EmailContentMap;
import com.blackducksoftware.tools.commonframework.standard.email.EmailTriggerRule;
import com.blackducksoftware.tools.notifiers.common.IHandler;
import com.blackducksoftware.tools.notifiers.common.NotifierConstants;
import com.blackducksoftware.tools.notifiers.common.ProjectInfo;
import com.blackducksoftware.tools.notifiers.common.ProtexInformationCollector;

/**
 * Handles all email formatting/sending
 *
 * @author akamen
 *
 */
public class EmailHandler implements IHandler {

    private Logger log = Logger.getLogger(this.getClass().getSimpleName());

    private final CFEmailNotifier emailer;
    private boolean dryRun = false;

    public EmailHandler(EmailNotifierUtilityConfig config,
	    CFEmailNotifier emailer) {
	this.emailer = emailer;
	dryRun = config.isDryRun();
    }

    @Override
    public void processNotification(EmailContentMap content,
	    ProjectInfo protexProject, List<EmailTriggerRule> rules)
	    throws Exception {
	// Add additional content here that is not subject to user options
	content.put(NotifierConstants.PROTEX_ANALYSIS_DATE, protexProject
		.getProjectPojo().getAnalyzedDate());
	String ruleString = convertRulesToString(rules);
	content.put(NotifierConstants.EMAIL_RULES, ruleString);
	if (dryRun) {
	    log.info("Dry run mode, no emails sent");
	    log.info("Email content: " + content.toString());
	} else {
	    emailer.sendEmail(protexProject.getProjectPojo(), content, rules);
	}

	log.info("All Email processing completed");
    }

    private String convertRulesToString(List<EmailTriggerRule> rules) {
	StringBuilder ruleList = new StringBuilder();
	for (EmailTriggerRule rule : rules) {
	    ruleList.append(rule.toString() + ",");
	}

	String ruleString = ruleList.toString();
	// Remove the last comma
	if (ruleString.length() > 0) {
	    ruleString = ruleString.toString().substring(0,
		    ruleString.length() - 1);
	}

	return ruleString;
    }

    @Override
    public EmailContentMap populateContentMap(ProjectPojo protexProject,
	    ProtexInformationCollector pic, EmailContentMap keysOnlyMap)
	    throws Exception {
	EmailContentMap map = null;
	map = pic.collectContent(protexProject, keysOnlyMap);

	return map;
    }

}

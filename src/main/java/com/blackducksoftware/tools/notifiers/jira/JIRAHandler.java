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
package com.blackducksoftware.tools.notifiers.jira;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.blackducksoftware.tools.commonframework.standard.common.ProjectPojo;
import com.blackducksoftware.tools.commonframework.standard.email.EmailContentMap;
import com.blackducksoftware.tools.commonframework.standard.email.EmailTriggerRule;
import com.blackducksoftware.tools.notifiers.common.HandlerUtils;
import com.blackducksoftware.tools.notifiers.common.IHandler;
import com.blackducksoftware.tools.notifiers.common.NotifierConstants;
import com.blackducksoftware.tools.notifiers.common.NotifierRuleEnums.EN_RULE;
import com.blackducksoftware.tools.notifiers.common.ProjectInfo;
import com.blackducksoftware.tools.notifiers.common.ProtexInformationCollector;

/**
 * Class for handling JIRA issue lookup/creation
 *
 * @author akamen
 *
 */
public class JIRAHandler implements IHandler {

    private final Logger log = Logger
	    .getLogger(this.getClass().getSimpleName());

    private final JiraIntegrationUtilityConfig config;
    private final JiraConnector jiraConnector;
    private ProtexInformationCollector pic;
    private boolean dryRun = false;

    public JIRAHandler(JiraIntegrationUtilityConfig config,
	    JiraConnector jiraConnector) throws Exception {
	this.config = config;
	this.jiraConnector = jiraConnector;

	dryRun = config.isDryRun();
    }

    /**
     * Populates the map for the JIRA handler Similar to the Email handler, but
     * hangs on to the Protex Info Collector.
     *
     * Wipes out NON APPROVED components as they do not apply.
     */
    @Override
    public EmailContentMap populateContentMap(ProjectPojo protexProject,
	    ProtexInformationCollector pic, EmailContentMap keysOnlyMap)
	    throws Exception {
	this.pic = pic;

	EmailContentMap map = pic.collectContent(protexProject, keysOnlyMap);
	map.remove(NotifierConstants.PROTEX_NON_APPROVED_LIST);

	return map;
    }

    /**
     * JIRA Notification implementation
     *
     * The JIRA handler must look through the rules again in order to determine
     * trigger mechanism.
     *
     * Gets the JIRA project name from the protexProject ALIAS, which may be
     * different from the Protex project name.
     *
     * Looks up JIRA project via the JIRA connector If exists, then updates
     * Otherwise, creates a new one.
     */
    @Override
    public void processNotification(EmailContentMap content,
	    ProjectInfo protexProject, List<EmailTriggerRule> rules)
	    throws Exception {
	try {
	    String protexProjectName = protexProject.getAlias();
	    boolean fullyCombinedTicket = config.isJiraSummaryTicket();
	    String issueTypeName = config.getJiraInfo().getDefaultIssueType();

	    // One ticket for all components
	    if (fullyCombinedTicket) {
		String issueSummary = config.getJiraInfo()
			.getDefaultSummaryCombined();
		// Full description checks which rules are active.
		String description = getFullDescriptionForJira(content, rules);
		if (description != null) {
		    createOrUpdateTicket(protexProjectName, issueSummary,
			    issueTypeName, description);
		} else {
		    log.info("No rules triggered, nothing to process.");
		}

	    } else {
		/**
		 * If there are conflicting components, tickets will be
		 * generated regardless of rules.
		 */
		Map<String, String> compMap = pic.getComponentMap();

		for (String key : compMap.keySet()) {
		    String componentSummary = config.getJiraInfo()
			    .getDefaultSummaryComponent() + " " + key;
		    String description = compMap.get(key);
		    description = HandlerUtils
			    .buildHtmlDescription(description);
		    try {
			createOrUpdateTicket(protexProjectName,
				componentSummary, issueTypeName, description);
		    } catch (Exception e) {
			log.error("Error creating individual component ticket",
				e);
		    }
		}
	    }

	} catch (Exception e) {
	    log.error("Error during JIRA processing", e);
	    throw e;
	} finally {
	    jiraConnector.close();
	}
	log.info("All JIRA processing completed");
    }

    private void createOrUpdateTicket(String projectName, String issueSummary,
	    String issueTypeName, String description) throws Exception {

	BasicProject jiraProject = jiraConnector.getProjectByName(projectName);
	if (jiraProject == null) {
	    throw new Exception("Cannot find JIRA project with name: "
		    + projectName);
	}

	Issue jiraIssue = jiraConnector.getIssueBySummaryName(issueSummary,
		jiraProject);

	if (jiraIssue == null) {
	    IssueType issueType = jiraConnector
		    .getIssueTypeByName(issueTypeName);
	    if (issueType == null) {
		throw new Exception(
			"Cannot create new, because issue type does not exist: "
				+ issueTypeName);
	    }

	    log.info("No JIRA issue exists for summary (will create a new one): "
		    + issueSummary);
	    if (!dryRun) {
		jiraConnector.createNewIssue(jiraProject, issueType,
			issueSummary, description);
	    } else {
		log.info("Dry run, no ticket creation.");
		log.info("Would have created issue with summary: "
			+ issueSummary);
		log.info("Would have created issue with description: "
			+ description);
	    }
	} else {
	    log.info("JIRA issue found, updating description.");
	    log.debug("jiraIssue: " + jiraIssue);
	    if (!dryRun) {
		jiraConnector.updateDescriptionForIssue(jiraIssue, description);
	    } else {
		log.info("Dry run, no ticket creation.");
		log.info(String.format(
			"Would have updated ticket [%s] with description: %s ",
			jiraIssue.getKey(), description));
	    }
	}

    }

    /**
     * Creates a combined String depending on trigger rules to be placed into
     * the JIRA description field
     *
     * @param content
     * @param rules
     * @return
     */
    private String getFullDescriptionForJira(EmailContentMap content,
	    List<EmailTriggerRule> rules) {

	StringBuilder desc = new StringBuilder();
	boolean isRuleTriggered = false;
	for (EmailTriggerRule rule : rules) {
	    if (rule.isRuleTriggered()) {
		isRuleTriggered = true;
		if (EN_RULE.PENDING == EN_RULE.valueOf(rule.getRuleName())) {
		    String totalPendingNumber = content
			    .get(NotifierConstants.PROTEX_TOTAL_PENDING);
		    desc.append("Pending: " + totalPendingNumber);
		    desc.append("\n");
		}
		if (EN_RULE.CONFLICTS == EN_RULE.valueOf(rule.getRuleName())) {
		    String conflictList = content
			    .get(NotifierConstants.PROTEX_COMP_LIST);
		    desc.append("Conflicting Components:");
		    desc.append("\n");
		    desc.append(conflictList);
		    desc.append("\n");
		}
	    }
	}

	if (isRuleTriggered) {
	    // Add URL to bottom
	    String protexUrl = content.get(NotifierConstants.PROTEX_URL);
	    desc.append("Project URL: " + protexUrl);
	    return desc.toString();
	} else {
	    return null;
	}

    }

}

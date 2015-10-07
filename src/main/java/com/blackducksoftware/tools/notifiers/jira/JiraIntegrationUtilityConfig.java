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

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.blackducksoftware.tools.commonframework.core.config.ConfigurationPassword;
import com.blackducksoftware.tools.notifiers.common.NotifierConfig;
import com.blackducksoftware.tools.notifiers.common.NotifierConstants;

/**
 * Common Framework config extension. Contains optional JIRA setup
 *
 * @author akamen
 *
 */
public class JiraIntegrationUtilityConfig extends NotifierConfig {

    // Simple class name to avoid full package display.
    private final Logger log = Logger
	    .getLogger(this.getClass().getSimpleName());

    private JiraInfo jiraInfo = new JiraInfo();
    private boolean jiraSummaryTicket = false;

    public JiraIntegrationUtilityConfig(File configFile) {
	super(configFile);
	init();
    }

    public JiraIntegrationUtilityConfig(Properties props) {
	super(props);
	init();
    }

    private void init() {

	// Create JIRA bean
	String jiraUrl = getProperty(NotifierConstants.JIRA_URL);
	String jiraAdmin = getProperty(NotifierConstants.JIRA_ADMIN_NAME);

	ConfigurationPassword configPasswordForJira = ConfigurationPassword
		.createFromProperty(super.getProps(),
			NotifierConstants.JIRA_ADMIN_PASSWORD_PREFIX);

	String jiraPassword = configPasswordForJira.getPlainText();
	String jiraDefaultSummaryCombined = getOptionalProperty(
		NotifierConstants.JIRA_DEFAULT_SUMMARY_COMBINED,
		NotifierConstants.JIRA_BASE_COMBINED_SUMMARY, String.class);
	String jiraDefaultSummaryComponent = getOptionalProperty(
		NotifierConstants.JIRA_DEFAULT_SUMMARY_COMPONENT,
		NotifierConstants.JIRA_BASE_COMPONENT_SUMMARY, String.class);

	String jiraDefaultIssue = getOptionalProperty(
		NotifierConstants.JIRA_DEFAULT_ISSUE_TYPE,
		NotifierConstants.JIRA_BASE_ISSUE_TYPE, String.class);
	jiraSummaryTicket = getOptionalProperty(
		NotifierConstants.JIRA_COMBINED_TICKET, false, Boolean.class);

	log.info("Jira Configuration provided");
	jiraInfo.setAdminName(jiraAdmin);
	jiraInfo.setUrl(jiraUrl);
	jiraInfo.setAdminPassword(jiraPassword);
	jiraInfo.setDefaultIssue(jiraDefaultIssue);
	jiraInfo.setDefaultSummaryCombined(jiraDefaultSummaryCombined);
	jiraInfo.setDefaultSummaryComponent(jiraDefaultSummaryComponent);
	jiraInfo.setIsJiraConfigured(true);

    }

    public JiraInfo getJiraInfo() {
	return jiraInfo;
    }

    public void setJiraInfo(JiraInfo jiraInfo) {
	this.jiraInfo = jiraInfo;
    }

    public boolean isJiraSummaryTicket() {
	return jiraSummaryTicket;
    }
}

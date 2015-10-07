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

public class NotifierConstants {

    // Command line options
    public static final String CL_PROTEX_PROJECT_NAME = "protexProjectName";
    public static final String CL_JIRA_PROJECT_NAME = "jiraProjectName";

    public static final String CL_CONFIG = "config";
    public static final String CL_TEMPLATE_FILE = "template";

    // Protex email success template
    public static final String PROTEX_EMAIL_SUCCESS_TEMPLATE = "success-template.xml";

    // Protex Email content
    public static final String PROTEX_URL = "protexURL";
    public static final String PROTEX_TOTAL_PENDING = "protexTotalPending";
    public static final String PROTEX_COMP_LIST = "protexConflictingComponents";
    public static final String PROTEX_NON_APPROVED_LIST = "protexNonApprovedComponents";
    public static final String PROTEX_ANALYSIS_DATE = "protexAnalysisFinishDate";
    // Not quite Protex, but the rules which govern the email trigger.
    public static final String EMAIL_RULES = "emailRules";

    // Notifier Properties
    public static final String NOTIFIER_PROPERTY_SHOW_FILE_PATHS = "notifier.show.filepaths";
    public static final String NOTIFIER_PROPERTY_SHOW_FILE_PATHS_COUNT = NOTIFIER_PROPERTY_SHOW_FILE_PATHS
	    + ".maxcount";
    public static final String NOTIFIER_TRIGGER_RULES = "email.trigger.rules";
    public static final String NOTIFIER_PROPERTY_DRY_RUN = "notifier.dry.run";

    // JIRA Properties
    public static final String JIRA_URL = "jira.url";
    public static final String JIRA_ADMIN_NAME = "jira.admin.name";
    public static final String JIRA_ADMIN_PASSWORD_PREFIX = "jira.admin";
    public static final String JIRA_DEFAULT_SUMMARY_COMBINED = "jira.default.summary.combined";
    public static final String JIRA_DEFAULT_SUMMARY_COMPONENT = "jira.default.summary.component";
    public static final String JIRA_DEFAULT_ISSUE_TYPE = "jira.default.issue.type";
    public static final String JIRA_COMBINED_TICKET = "jira.combined.ticket";

    // JIRA base values (in case user does not set them)
    public static final String JIRA_BASE_COMBINED_SUMMARY = "Black Duck Summary";
    public static final String JIRA_BASE_COMPONENT_SUMMARY = JIRA_BASE_COMBINED_SUMMARY
	    + " For:";
    public static final String JIRA_BASE_ISSUE_TYPE = "Task";

}

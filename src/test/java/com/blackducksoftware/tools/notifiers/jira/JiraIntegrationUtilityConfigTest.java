package com.blackducksoftware.tools.notifiers.jira;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.tools.notifiers.jira.JiraIntegrationUtilityConfig;

public class JiraIntegrationUtilityConfigTest {

    private static final String DRY_RUN_FLAG = "false";
    private static final String ISSUE_TYPE = "task";
    private static final String SUMMARY_CONFLICT = "Protex Component Conflict";
    private static final String SUMMARY_COMBINED = "Protex Summary";
    private static final String JIRA_USERNAME = "hitachiUser";
    private static final String JIRA_URL = "http://bds00882.bds-ad.lc:8080";
    private static final String FILEPATHS_COUNT = "10";
    private static final String FILEPATHS_FLAG = "true";
    private static final String RULES = "PENDING,CONFLICTS";
    private static final String PROTEX_PASSWORD = "blackduck";
    private static final String JIRA_PASSWORD = "blackduck";
    private static final String PROTEX_USERNAME = "unitTester@blackducksoftware.com";
    private static final String PROTEX_URL = "https://se-px01.dc1.lan/";
    private static final String COMBINED_TICKET_FLAG = "true";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void test() {
	Properties props = new Properties();
	props.setProperty("protex.server.name", PROTEX_URL);
	props.setProperty("protex.user.name", PROTEX_USERNAME);
	props.setProperty("protex.password", PROTEX_PASSWORD);

	props.setProperty("notifier.trigger.rules", RULES);

	props.setProperty("notifier.show.filepaths", FILEPATHS_FLAG);
	props.setProperty("notifier.show.filepaths.maxcount", FILEPATHS_COUNT);

	props.setProperty("jira.url", JIRA_URL);
	props.setProperty("jira.admin.name", JIRA_USERNAME);
	props.setProperty("jira.admin.password", JIRA_PASSWORD);
	props.setProperty("jira.default.summary.combined", SUMMARY_COMBINED);
	props.setProperty("jira.default.summary.component", SUMMARY_CONFLICT);
	props.setProperty("jira.default.issue.type", ISSUE_TYPE);
	props.setProperty("jira.combined.ticket", COMBINED_TICKET_FLAG);
	props.setProperty("notifier.dry.run", DRY_RUN_FLAG);

	JiraIntegrationUtilityConfig config = new JiraIntegrationUtilityConfig(
		props);

	assertEquals(PROTEX_URL, config.getServerBean().getServerName());
	assertEquals(PROTEX_USERNAME, config.getServerBean().getUserName());
	assertEquals(PROTEX_PASSWORD, config.getServerBean().getPassword());

	assertEquals(Boolean.valueOf(FILEPATHS_FLAG), config.isShowFilePaths());
	assertEquals(Integer.valueOf(FILEPATHS_COUNT),
		config.getShowFilePathCount());
	assertEquals(JIRA_URL, config.getJiraInfo().getUrl());
	assertEquals(JIRA_USERNAME, config.getJiraInfo().getAdminName());
	assertEquals(JIRA_PASSWORD, config.getJiraInfo().getAdminPassword());
	assertEquals(SUMMARY_COMBINED, config.getJiraInfo()
		.getDefaultSummaryCombined());
	assertEquals(SUMMARY_CONFLICT, config.getJiraInfo()
		.getDefaultSummaryComponent());
	assertEquals(ISSUE_TYPE, config.getJiraInfo().getDefaultIssueType());
	assertEquals(Boolean.valueOf(COMBINED_TICKET_FLAG),
		config.isJiraSummaryTicket());
	assertEquals(Boolean.valueOf(DRY_RUN_FLAG), config.isDryRun());
    }
}

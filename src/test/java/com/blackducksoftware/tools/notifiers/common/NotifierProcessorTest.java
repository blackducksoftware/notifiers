package com.blackducksoftware.tools.notifiers.common;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;

import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.blackducksoftware.sdk.protex.common.ApprovalState;
import com.blackducksoftware.sdk.protex.license.GlobalLicense;
import com.blackducksoftware.sdk.protex.license.LicenseApi;
import com.blackducksoftware.sdk.protex.license.LicenseInfo;
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.project.bom.BomApi;
import com.blackducksoftware.sdk.protex.project.bom.BomComponent;
import com.blackducksoftware.tools.commonframework.connector.protex.CodeTreeHelper;
import com.blackducksoftware.tools.commonframework.connector.protex.ProtexAPIWrapper;
import com.blackducksoftware.tools.commonframework.connector.protex.ProtexServerWrapper;
import com.blackducksoftware.tools.commonframework.standard.email.CFEmailNotifier;
import com.blackducksoftware.tools.commonframework.standard.email.EmailContentMap;
import com.blackducksoftware.tools.commonframework.standard.protex.ProtexProjectPojo;
import com.blackducksoftware.tools.notifiers.email.EmailHandler;
import com.blackducksoftware.tools.notifiers.email.EmailNotifierUtilityConfig;
import com.blackducksoftware.tools.notifiers.jira.JIRAHandler;
import com.blackducksoftware.tools.notifiers.jira.JiraConnector;
import com.blackducksoftware.tools.notifiers.jira.JiraIntegrationUtilityConfig;

public class NotifierProcessorTest {
    private static final String PROTEX_SUMMARY_COMBINED = "Protex Summary";
    private static final String NON_APPROVED_STRING = "<table><tr><th>Component Name</th><th>Version</th><th>License</th></tr><tr><td>Test Component Name</td><td>Test Component Version</td><td>Test License Name</td></tr></table>";
    private static final String JIRA_ISSUE_DESCRIPTION = "Pending: 99\n"
	    + "Conflicting Components:\n"
	    + "<table><tr><th>Component Name</th><th>Version</th><th>License</th></tr><tr><td>Test Component Name</td><td>Test Component Version</td><td>Test License Name</td></tr></table>\n"
	    + "Project URL: https://protex.test.com//protex/ProtexIPIdentifyFolderBillOfMaterialsContainer?isAtTop=true&ProtexIPProjectId=c_junitemailnotifier2_8710&ProtexIPIdentifyFileViewLevel=folder&ProtexIPIdentifyFileId=-1";
    private static final String LICENSE_NAME = "Test License Name";
    private static final String LICENSE_ID_STRING = "testLicenseId";
    private static final String PROJECT_ID_STRING = "projectId";
    private static final String LAST_ANALYZED_DATE_STRING = "10/01/2015";
    private static final long NUM_PENDING_ID_COMPONENTS = 99L;
    private static final String TOTAL_PENDING_STRING = String
	    .valueOf(NUM_PENDING_ID_COMPONENTS);
    private static final String CONFLICT_STRING = "<table><tr><th>Component Name</th><th>Version</th>"
	    + "<th>License</th></tr><tr><td>Test Component Name</td><td>Test Component Version</td><td>Test License Name</td></tr></table>";
    private static final String PROJECT_URL = "https://protex.test.com//protex/ProtexIPIdentifyFolderBillOfMaterialsContainer?"
	    + "isAtTop=true&ProtexIPProjectId=c_junitemailnotifier2_8710&ProtexIPIdentifyFileViewLevel=folder&ProtexIPIdentifyFileId=-1";
    private static final String PROJECT_NAME = "JUnit EmailNotifier 2";

    private static ProtexServerWrapper<ProtexProjectPojo> mockProtexServerWrapper;
    private static ProtexAPIWrapper mockApiWrapper;
    private static ProjectApi mockProjectApi;
    private static LicenseApi mockLicenseApi;
    private static BomApi mockBomApi;

    private static ProtexProjectPojo mockProjectPojo;

    private static CFEmailNotifier mockEmailer;
    private static EmailContentMap emailContentMap;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

	// Mock wrappers and APIs
	mockProtexServerWrapper = mock(ProtexServerWrapper.class);
	mockApiWrapper = mock(ProtexAPIWrapper.class);
	mockProjectApi = mock(ProjectApi.class);
	mockLicenseApi = mock(LicenseApi.class);
	mockBomApi = mock(BomApi.class);

	// Make the mock wrapper return the mock APIs
	when(mockProtexServerWrapper.getInternalApiWrapper()).thenReturn(
		mockApiWrapper);
	when(mockApiWrapper.getProjectApi()).thenReturn(mockProjectApi);
	when(mockApiWrapper.getLicenseApi()).thenReturn(mockLicenseApi);
	when(mockApiWrapper.getBomApi()).thenReturn(mockBomApi);

	// Mock project
	mockProjectPojo = mock(ProtexProjectPojo.class);
	when(mockProtexServerWrapper.getProjectByName(PROJECT_NAME))
		.thenReturn(mockProjectPojo);
	when(mockProtexServerWrapper.getProjectURL(mockProjectPojo))
		.thenReturn(PROJECT_URL);
	when(mockProjectPojo.getAnalyzedDate()).thenReturn(
		LAST_ANALYZED_DATE_STRING);
	when(mockProjectPojo.getProjectKey()).thenReturn(PROJECT_ID_STRING);

	// List of mock BOM components (size=1)
	List<BomComponent> bomComponents = new ArrayList<>();
	BomComponent mockBomComponent = mock(BomComponent.class);
	when(mockBomComponent.getComponentName()).thenReturn(
		"Test Component Name");
	when(mockBomComponent.getVersionName()).thenReturn(
		"Test Component Version");
	when(mockBomComponent.getApprovalState()).thenReturn(
		ApprovalState.DIS_APPROVED);
	when(mockBomComponent.isHasDeclaredLicenseConflict()).thenReturn(true);
	LicenseInfo mockLicenseInfo = mock(LicenseInfo.class);
	when(mockLicenseInfo.getLicenseId()).thenReturn(LICENSE_ID_STRING);

	// Mock license
	GlobalLicense mockGlobalLicense = mock(GlobalLicense.class);
	when(mockGlobalLicense.getName()).thenReturn(LICENSE_NAME);

	when(mockLicenseApi.getLicenseById(LICENSE_ID_STRING)).thenReturn(
		mockGlobalLicense);

	when(mockBomComponent.getLicenseInfo()).thenReturn(mockLicenseInfo);
	bomComponents.add(mockBomComponent);
	when(mockBomApi.getBomComponents(PROJECT_ID_STRING)).thenReturn(
		bomComponents);

	// Pending ID count
	CodeTreeHelper mockTreeHelper = mock(CodeTreeHelper.class);
	when(mockProtexServerWrapper.getCodeTreeHelper()).thenReturn(
		mockTreeHelper);
	when(mockTreeHelper.getTotalPendingIDCount(mockProjectPojo))
		.thenReturn(NUM_PENDING_ID_COMPONENTS);

	// Email Notifier
	mockEmailer = mock(CFEmailNotifier.class);
	when(mockEmailer.isConfigured()).thenReturn(true);
	emailContentMap = createEmailContentMap();
	when(mockEmailer.getEmailContentMap()).thenReturn(emailContentMap);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * This tests the NotifierProcessor and the EmailHandler.
     *
     * @throws Exception
     */
    @Test
    public void testEmail() throws Exception {

	// Configuration manager
	Properties props = createEmailProperties();
	EmailNotifierUtilityConfig emailConfig = new EmailNotifierUtilityConfig(
		props);

	// Create the Email notifier
	IHandler notificationHandler = new EmailHandler(emailConfig,
		mockEmailer);
	NotifierProcessor notifier = new NotifierProcessor(emailConfig,
		mockProtexServerWrapper, notificationHandler,
		mockEmailer.getEmailContentMap(), PROJECT_NAME, null);

	// Run notifier to send mail
	EmailContentMap publishedContent = notifier.process();

	// Verify the email contents map
	assertEquals(PROJECT_URL, publishedContent.get("protexURL"));
	assertEquals(CONFLICT_STRING,
		publishedContent.get("protexConflictingComponents"));
	assertEquals(LAST_ANALYZED_DATE_STRING,
		publishedContent.get("protexAnalysisFinishDate"));
	assertEquals(null, publishedContent.get("projectName"));
	assertEquals(TOTAL_PENDING_STRING,
		publishedContent.get("protexTotalPending"));
	assertEquals(NON_APPROVED_STRING,
		publishedContent.get("protexNonApprovedComponents"));

	// Verify that email was (mock) sent
	verify(mockEmailer).sendEmail(
		Matchers.eq(mockProjectPojo),
		Matchers.eq(emailContentMap),
		Matchers.eq(emailConfig.getNotificationRulesConfiguration()
			.getRules()));
    }

    /**
     * Test JIRA logic: Issue exists; update descriptions. Other tests that
     * would be nice: new issue, add comment
     *
     * @throws Exception
     */
    @Test
    public void testJiraExistingIssueDescription() throws Exception {

	// Configuration manager
	Properties props = createJiraProperties();
	JiraIntegrationUtilityConfig jiraConfig = new JiraIntegrationUtilityConfig(
		props);

	// Create the mock jira connector
	Issue mockIssue = mock(Issue.class);
	JiraConnector mockJiraConnector = createMockJiraConnector(mockIssue);

	// Create the notifier
	IHandler notificationHandler = new JIRAHandler(jiraConfig,
		mockJiraConnector);
	NotifierProcessor notifier = new NotifierProcessor(jiraConfig,
		mockProtexServerWrapper, notificationHandler,
		mockEmailer.getEmailContentMap(), PROJECT_NAME, PROJECT_NAME);

	// Run notifier to update JIRA
	notifier.process();

	// Verify that jira connector gets called to update description of our
	// mock issue
	verify(mockJiraConnector).updateDescriptionForIssue(mockIssue,
		JIRA_ISSUE_DESCRIPTION);
    }

    private JiraConnector createMockJiraConnector(Issue mockIssue)
	    throws Exception {
	BasicProject mockJiraProject = mock(BasicProject.class);
	JiraConnector mockJiraConnector = mock(JiraConnector.class);
	when(mockJiraConnector.getProjectByName(PROJECT_NAME)).thenReturn(
		mockJiraProject);

	when(
		mockJiraConnector.getIssueBySummaryName(
			PROTEX_SUMMARY_COMBINED, mockJiraProject)).thenReturn(
		mockIssue);
	return mockJiraConnector;
    }

    private static Properties createEmailProperties() {
	Properties props = createBasicProperties();

	props.setProperty("email.smtp.address",
		"mailrelay.blackducksoftware.com");
	props.setProperty("email.smtp.from", "aaa@blackducksoftware.com");
	props.setProperty("email.smtp.to",
		"aaa@blackducksoftware.com,blackhole@blackducksoftware.com");

	return props;
    }

    private static Properties createJiraProperties() {
	Properties props = createBasicProperties();

	props.setProperty("jira.url", "http://jira.test.com:8080");
	props.setProperty("jira.admin.name", "jiraUsername");
	props.setProperty("jira.admin.password", "jiraPassword");
	props.setProperty("jira.default.summary.combined",
		PROTEX_SUMMARY_COMBINED);
	props.setProperty("jira.default.summary.component",
		"Protex Component Conflict");
	props.setProperty("jira.default.issue.type", "task");
	props.setProperty("jira.combined.ticket", "true");

	return props;
    }

    private static Properties createBasicProperties() {
	Properties props = new Properties();
	props.setProperty("protex.server.name", "https://se-px01.dc1.lan/");
	props.setProperty("protex.user.name",
		"unitTester@blackducksoftware.com");
	props.setProperty("protex.password", "blackduck");

	props.setProperty("notifier.show.filepaths", "false");
	props.setProperty("notifier.show.filepaths.maxcount", "10");

	props.setProperty("email.trigger.rules",
		"PENDING,CONFLICTS,NON_APPROVED");

	return props;
    }

    private static EmailContentMap createEmailContentMap() {
	EmailContentMap emailContentMap = new EmailContentMap();
	emailContentMap.put("protexURL", null);
	emailContentMap.put("protexConflictingComponents", null);
	emailContentMap.put("protexAnalysisFinishDate", null);
	emailContentMap.put("smtpTo", null);
	emailContentMap.put("your variable", null);
	emailContentMap.put("projectName", null);
	emailContentMap.put("smtpFrom", null);
	emailContentMap.put("protexTotalPending", null);
	emailContentMap.put("protexNonApprovedComponents", null);
	return emailContentMap;
    }
}

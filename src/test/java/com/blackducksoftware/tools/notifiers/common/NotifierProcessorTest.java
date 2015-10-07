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
import com.blackducksoftware.tools.commonframework.standard.common.ProjectPojo;
import com.blackducksoftware.tools.commonframework.standard.email.CFEmailNotifier;
import com.blackducksoftware.tools.commonframework.standard.email.EmailContentMap;
import com.blackducksoftware.tools.commonframework.standard.protex.ProtexProjectPojo;
import com.blackducksoftware.tools.notifiers.email.EmailHandler;
import com.blackducksoftware.tools.notifiers.email.EmailNotifierUtilityConfig;

public class NotifierProcessorTest {
    private static final String NON_APPROVED_STRING = "<table><tr><th>Component Name</th><th>Version</th><th>License</th></tr><tr><td>Test Component Name</td><td>Test Component Version</td><td>Test License Name</td></tr></table>";
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

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

	// Mock wrappers and APIs
	mockProtexServerWrapper = mock(ProtexServerWrapper.class);
	mockApiWrapper = mock(ProtexAPIWrapper.class);
	mockProjectApi = mock(ProjectApi.class);
	mockLicenseApi = mock(LicenseApi.class);
	mockBomApi = mock(BomApi.class);

	when(mockProtexServerWrapper.getInternalApiWrapper()).thenReturn(
		mockApiWrapper);
	when(mockApiWrapper.getProjectApi()).thenReturn(mockProjectApi);
	when(mockApiWrapper.getLicenseApi()).thenReturn(mockLicenseApi);
	when(mockApiWrapper.getBomApi()).thenReturn(mockBomApi);

	mockProjectPojo = mock(ProtexProjectPojo.class);
	when(mockProtexServerWrapper.getProjectByName(PROJECT_NAME))
		.thenReturn(mockProjectPojo);
	when(mockProtexServerWrapper.getProjectURL(mockProjectPojo))
		.thenReturn(PROJECT_URL);
	when(mockProjectPojo.getAnalyzedDate()).thenReturn(
		LAST_ANALYZED_DATE_STRING);
	when(mockProjectPojo.getProjectKey()).thenReturn(PROJECT_ID_STRING);

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

	GlobalLicense mockGlobalLicense = mock(GlobalLicense.class);
	when(mockGlobalLicense.getName()).thenReturn(LICENSE_NAME);

	when(mockLicenseApi.getLicenseById(LICENSE_ID_STRING)).thenReturn(
		mockGlobalLicense);

	when(mockBomComponent.getLicenseInfo()).thenReturn(mockLicenseInfo);
	bomComponents.add(mockBomComponent);
	when(mockBomApi.getBomComponents(PROJECT_ID_STRING)).thenReturn(
		bomComponents);

	CodeTreeHelper mockTreeHelper = mock(CodeTreeHelper.class);
	when(mockProtexServerWrapper.getCodeTreeHelper()).thenReturn(
		mockTreeHelper);
	when(mockTreeHelper.getTotalPendingIDCount(mockProjectPojo))
		.thenReturn(NUM_PENDING_ID_COMPONENTS);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testBasicEmail() throws Exception {
	Properties props = createBasicProperties();

	// Configuration manager
	EmailNotifierUtilityConfig emailConfig = new EmailNotifierUtilityConfig(
		props);

	// Email Notifier
	CFEmailNotifier emailer = mock(CFEmailNotifier.class);
	when(emailer.isConfigured()).thenReturn(true);
	EmailContentMap emailContentMap = createEmailContentMap();
	when(emailer.getEmailContentMap()).thenReturn(emailContentMap);

	IHandler notificationHandler = new EmailHandler(emailConfig, emailer);
	NotifierProcessor enp = new NotifierProcessor(emailConfig,
		mockProtexServerWrapper, notificationHandler,
		emailer.getEmailContentMap(), PROJECT_NAME, null);

	EmailContentMap publishedContent = enp.process();

	assertEquals(PROJECT_URL, publishedContent.get("protexURL"));
	assertEquals(CONFLICT_STRING,
		publishedContent.get("protexConflictingComponents"));
	assertEquals(LAST_ANALYZED_DATE_STRING,
		publishedContent.get("protexAnalysisFinishDate"));
	assertEquals(null, publishedContent.get("projectName"));
	assertEquals(TOTAL_PENDING_STRING,
		publishedContent.get("protexTotalPending"));
	assertEquals(
		NON_APPROVED_STRING,
		publishedContent.get("protexNonApprovedComponents"));
	verify(emailer).sendEmail(
		Matchers.isA(ProjectPojo.class),
		Matchers.eq(emailContentMap),
		Matchers.eq(emailConfig.getNotificationRulesConfiguration()
			.getRules()));
    }

    private Properties createBasicProperties() {
	Properties props = new Properties();
	props.setProperty("protex.server.name", "https://se-px01.dc1.lan/");
	props.setProperty("protex.user.name",
		"unitTester@blackducksoftware.com");
	props.setProperty("protex.password", "blackduck");
	props.setProperty("email.smtp.address",
		"mailrelay.blackducksoftware.com");
	props.setProperty("email.smtp.from", "aaa@blackducksoftware.com");
	props.setProperty("email.smtp.to",
		"aaa@blackducksoftware.com,blackhole@blackducksoftware.com");
	props.setProperty("email.trigger.rules",
		"PENDING,CONFLICTS,NON_APPROVED");
	props.setProperty("notifier.show.filepaths", "false");
	props.setProperty("notifier.show.filepaths.maxcount", "10");
	return props;
    }

    private EmailContentMap createEmailContentMap() {
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

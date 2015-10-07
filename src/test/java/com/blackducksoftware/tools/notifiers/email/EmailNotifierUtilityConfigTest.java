package com.blackducksoftware.tools.notifiers.email;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.tools.notifiers.email.EmailNotifierUtilityConfig;

public class EmailNotifierUtilityConfigTest {

    private static final String DRY_RUN_FLAG = "false";
    private static final String FILEPATHS_COUNT = "10";
    private static final String FILEPATHS_FLAG = "true";
    private static final String RULES = "PENDING,CONFLICTS";
    private static final String PROTEX_PASSWORD = "password";
    private static final String PROTEX_USERNAME = "test@test.com";
    private static final String PROTEX_URL = "https://protex.test.com/";

    private static final String SMTP_ADDRESS = "mailrelay.test.com";
    private static final String SMTP_FROM = "from@test.com";
    private static final String SMTP_TO = "to@test.com";

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

	props.setProperty("email.smtp.address", SMTP_ADDRESS);
	props.setProperty("email.smtp.from", SMTP_FROM);
	props.setProperty("email.smtp.to", SMTP_TO);

	EmailNotifierUtilityConfig config = new EmailNotifierUtilityConfig(
		props);

	assertEquals(PROTEX_URL, config.getServerBean().getServerName());
	assertEquals(PROTEX_USERNAME, config.getServerBean().getUserName());
	assertEquals(PROTEX_PASSWORD, config.getServerBean().getPassword());

	assertEquals(SMTP_ADDRESS, config.getEmailConfiguration()
		.getSmtpAddress());

	assertEquals(Boolean.valueOf(FILEPATHS_FLAG), config.isShowFilePaths());
	assertEquals(Integer.valueOf(FILEPATHS_COUNT),
		config.getShowFilePathCount());

	assertEquals(Boolean.valueOf(DRY_RUN_FLAG), config.isDryRun());
    }

}

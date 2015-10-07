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

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import com.blackducksoftware.tools.commonframework.standard.email.CFEmailNotifier;
import com.blackducksoftware.tools.notifiers.common.IHandler;
import com.blackducksoftware.tools.notifiers.common.NotifierConstants;
import com.blackducksoftware.tools.notifiers.common.NotifierException;
import com.blackducksoftware.tools.notifiers.common.NotifierProcessor;

/**
 * Hello world!
 *
 */
public class EmailNotifierUtility {
    private static final Logger log = Logger
	    .getLogger(EmailNotifierUtility.class.getClass().getName());
    private static final Options options = new Options();

    public static void main(String[] args) {
	int status = process(args);
	System.exit(status);
    }

    static int process(String[] args) {
	System.out.println("Email Notifier for Black Duck Suite");
	CommandLineParser parser = new DefaultParser();

	options.addOption("h", "help", false, "show help.");

	Option protexProjectNameOption = new Option(
		NotifierConstants.CL_PROTEX_PROJECT_NAME, true,
		"Name of Project (required)");
	protexProjectNameOption.setRequired(true);
	options.addOption(protexProjectNameOption);

	Option configFileOption = new Option("config", true,
		"Location of configuration file (required)");
	configFileOption.setRequired(true);
	options.addOption(configFileOption);

	Option templateFileOption = new Option("template", true,
		"Location of email template file (optional)");
	templateFileOption.setRequired(false);
	options.addOption(templateFileOption);

	try {
	    CommandLine cmd = parser.parse(options, args);

	    if (cmd.hasOption("h")) {
		help();
		return 0;
	    }

	    String projectName = null;
	    File configFile = null;
	    String templateFileLocation = null;
	    if (cmd.hasOption(NotifierConstants.CL_PROTEX_PROJECT_NAME)) {
		projectName = cmd
			.getOptionValue(NotifierConstants.CL_PROTEX_PROJECT_NAME);
		log.info("Project name: " + projectName);
	    } else {
		log.error("Must specify project name!");
		help();
		return -1;
	    }

	    // Config File
	    if (cmd.hasOption(NotifierConstants.CL_CONFIG)) {
		String configFilePath = cmd
			.getOptionValue(NotifierConstants.CL_CONFIG);
		log.info("Config file location: " + configFilePath);
		configFile = new File(configFilePath);
		if (!configFile.exists()) {
		    log.error("Configuration file does not exist at location: "
			    + configFile);
		    return -1;
		}
	    } else {
		log.error("Must specify configuration file!");
		help();
		return -1;
	    }

	    if (cmd.hasOption(NotifierConstants.CL_TEMPLATE_FILE)) {
		templateFileLocation = cmd
			.getOptionValue(NotifierConstants.CL_TEMPLATE_FILE);
		log.info("Template file location: " + templateFileLocation);
	    }

	    // Configuration manager
	    EmailNotifierUtilityConfig emailConfig = new EmailNotifierUtilityConfig(
		    configFile);

	    // Call the processor
	    try {
		CFEmailNotifier emailer = new CFEmailNotifier(emailConfig);
		if (!emailer.isConfigured()) {
		    throw new NotifierException(
			    "Email Configuration not properly configured.");
		}
		// Make sure the template is parsed now
		// If user did not specify template file, use default resource
		// package.
		if (templateFileLocation == null) {
		    templateFileLocation = ClassLoader.getSystemResource(
			    NotifierConstants.PROTEX_EMAIL_SUCCESS_TEMPLATE)
			    .getFile();
		}
		emailer.configureContentMap(templateFileLocation);

		IHandler notificationHandler = new EmailHandler(emailConfig,
			emailer);
		NotifierProcessor enp = new NotifierProcessor(emailConfig,
			notificationHandler, emailer.getEmailContentMap(),
			projectName, projectName);
		enp.process();
	    } catch (Exception e) {
		log.error("Fatal error: " + e.getMessage());
	    }

	    log.info("Exiting.");
	    return 0;

	} catch (ParseException e) {
	    log.error("Unable to parse command line arguments: "
		    + e.getMessage());
	    help();
	    return -1;
	}
    }

    private static void help() {
	HelpFormatter formater = new HelpFormatter();
	formater.printHelp("Email Notifier", options);
    }

}

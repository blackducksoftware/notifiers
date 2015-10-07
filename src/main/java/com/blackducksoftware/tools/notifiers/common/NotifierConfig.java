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

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.blackducksoftware.tools.commonframework.core.config.ConfigurationManager;

/**
 * Abstract configuration parent class for notifiers. The actual notifier
 * utility config classes extend this class.
 *
 * @author sbillings
 *
 */
public abstract class NotifierConfig extends ConfigurationManager {

    // Simple class name to avoid full package display.
    private final Logger log = Logger
	    .getLogger(this.getClass().getSimpleName());

    private Boolean showFilePaths;
    private Integer filePathCount;
    private boolean dryRun = false;

    public Boolean isShowFilePaths() {
	return showFilePaths;
    }

    public NotifierConfig(File configFile) {
	super(configFile.getAbsolutePath(), APPLICATION.PROTEX);
	init();
    }

    public NotifierConfig(Properties props) {
	super(props, APPLICATION.PROTEX);
	init();
    }

    private void init() {
	showFilePaths = getOptionalProperty(
		NotifierConstants.NOTIFIER_PROPERTY_SHOW_FILE_PATHS, false,
		Boolean.class);
	log.info("Showing file paths: " + showFilePaths);

	filePathCount = getOptionalProperty(
		NotifierConstants.NOTIFIER_PROPERTY_SHOW_FILE_PATHS_COUNT, 25,
		Integer.class);
	log.info("File path max count: " + filePathCount);

	dryRun = getOptionalProperty(
		NotifierConstants.NOTIFIER_PROPERTY_DRY_RUN, false,
		Boolean.class);
    }

    public Integer getShowFilePathCount() {
	return filePathCount;
    }

    public void setShowFilePathCount(Integer showFilePathCount) {
	filePathCount = showFilePathCount;
    }

    public boolean isDryRun() {
	return dryRun;
    }
}

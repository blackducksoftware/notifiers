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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.common.ApprovalState;
import com.blackducksoftware.sdk.protex.license.GlobalLicense;
import com.blackducksoftware.sdk.protex.license.LicenseApi;
import com.blackducksoftware.sdk.protex.license.LicenseInfo;
import com.blackducksoftware.sdk.protex.project.bom.BomApi;
import com.blackducksoftware.sdk.protex.project.bom.BomComponent;
import com.blackducksoftware.sdk.protex.report.ReportFormat;
import com.blackducksoftware.sdk.protex.report.ReportSectionType;
import com.blackducksoftware.tools.commonframework.connector.protex.CodeTreeHelper;
import com.blackducksoftware.tools.commonframework.connector.protex.ProtexServerWrapper;
import com.blackducksoftware.tools.commonframework.connector.protex.report.ReportUtils;
import com.blackducksoftware.tools.commonframework.standard.common.ProjectPojo;
import com.blackducksoftware.tools.commonframework.standard.email.EmailContentMap;
import com.blackducksoftware.tools.commonframework.standard.protex.ProtexProjectPojo;
import com.blackducksoftware.tools.commonframework.standard.protex.report.AdHocElement;

/**
 * Class responsible for collecting all Protex data. Currently the type of data
 * collected is baked into the class, but in theory could and should be
 * externalized to make this a bit more robust.
 *
 * @author akamen
 *
 */
public class ProtexInformationCollector {

    // Simple class name to avoid full package display.
    private final Logger log = Logger
	    .getLogger(this.getClass().getSimpleName());

    private final NotifierConfig config;
    private final ProtexServerWrapper<ProtexProjectPojo> psw;
    private EmailContentMap content;

    // Map that holds individual component information
    private Map<String, String> componentMap = new HashMap<String, String>();

    // Internal of list of components (in case multiple rules require them)
    private List<BomComponent> bomComponents;
    // Internal list of paths associated with each component
    private HashMap<String, Set<String>> componentToPathMapping;

    // Tracker for whether we should send notification or not.
    private boolean containsPending = false;
    private boolean containsConflictComponents = false;
    private boolean containsNonApprovedComponents = false;

    private final static String UNSPECIFIED_VERSION = "Unspecified";

    public ProtexInformationCollector(
	    ProtexServerWrapper<ProtexProjectPojo> psw,
	    NotifierConfig emailConfig) {
	config = emailConfig;
	this.psw = psw;

    }

    public EmailContentMap collectContent(ProjectPojo protexProject,
	    EmailContentMap content) {
	this.content = content;
	collectDataBasedOnKey(protexProject, NotifierConstants.PROTEX_URL,
		"collectUrl");
	collectDataBasedOnKey(protexProject,
		NotifierConstants.PROTEX_TOTAL_PENDING,
		"collectPendingIDInformation");
	collectDataBasedOnKey(protexProject,
		NotifierConstants.PROTEX_COMP_LIST, "collectConflicts");
	collectDataBasedOnKey(protexProject,
		NotifierConstants.PROTEX_NON_APPROVED_LIST,
		"collectNonApprovedComponents");

	return content;
    }

    /**
     * Checks to see if the template contains they key, if key exists then
     * invoke the method via reflection
     *
     * @param protexProject
     * @param key
     * @param method
     */
    private void collectDataBasedOnKey(ProjectPojo protexProject, String key,
	    String methodName) {
	if (content.doesMapContainKey(key)) {
	    // Call the appropriate method via reflection
	    Method method = null;
	    try {
		method = this.getClass().getDeclaredMethod(methodName,
			ProjectPojo.class, String.class);
		method.setAccessible(true);
		method.invoke(this, protexProject, key);
	    } catch (Exception e) {
		log.error("Unable to reflectively call method: " + methodName,
			e);
	    }
	} else {
	    log.debug("User template does not contain key: " + key);
	}
    }

    /**
     * ALL Methods below are invoked through the reflective
     * collectDataBasedOnKey() method.
     * ******************************************
     * ****************************************
     */

    @SuppressWarnings("unused")
    private void collectUrl(ProjectPojo protexProject, String key) {
	String url = psw.getProjectURL(protexProject);
	log.info("Build URL for protex project: " + url);
	content.put(key, url);
    }

    /**
     * Collects all components that are not approved in the BOM
     *
     * @param protexProject
     * @param nonApprovedComponentKey
     *            - The key from the mail template
     */
    @SuppressWarnings("unused")
    private void collectNonApprovedComponents(ProjectPojo protexProject,
	    String nonApprovedComponentKey) {
	log.info("START Gathering BOM components for: "
		+ protexProject.getProjectName());
	try {

	    LicenseApi licenseApi = psw.getInternalApiWrapper().getLicenseApi();
	    bomComponents = getBomComponents(protexProject);

	    int counter = 0;
	    for (BomComponent comp : bomComponents) {
		ApprovalState approvalState = comp.getApprovalState();
		if (approvalState == ApprovalState.DIS_APPROVED) {
		    log.debug("Found a component that is not approved: "
			    + comp.getComponentName());
		    counter = buildComponentRow(comp, counter, licenseApi,
			    protexProject);
		}
	    }
	    // This will get overwritten during final email processing
	    content.put(nonApprovedComponentKey, "No disapproved components");

	    if (counter > 0) {
		containsNonApprovedComponents = true;
	    }

	    log.info("DONE Gathering BOM components for: "
		    + protexProject.getProjectName());
	} catch (Exception e) {
	    log.error("Unable to get BOM components for project: "
		    + protexProject.getProjectName());
	}
    }

    /**
     * Collects the component information for all components, including optional
     * file paths and populates the content map.
     *
     * @param protexProject
     * @param compConflictKey
     *            - The template key denoting conflicts
     */
    @SuppressWarnings("unused")
    private void collectConflicts(ProjectPojo protexProject,
	    String compConflictKey) {
	log.info("START Gathering BOM components for: "
		+ protexProject.getProjectName());
	try {
	    LicenseApi licenseApi = psw.getInternalApiWrapper().getLicenseApi();
	    bomComponents = getBomComponents(protexProject);

	    int counter = 0;
	    for (BomComponent comp : bomComponents) {
		if (comp.isHasDeclaredLicenseConflict()) {
		    counter = buildComponentRow(comp, counter, licenseApi,
			    protexProject);
		}
	    }
	    // This will get overwritten during final email processing if
	    // trigger rules are set.
	    content.put(compConflictKey, "No Conflicting Components");

	    if (counter > 0) {
		containsConflictComponents = true;
	    }

	    log.info("DONE Gathering BOM components for: "
		    + protexProject.getProjectName());
	} catch (Exception e) {
	    log.error("Unable to get BOM components for project: "
		    + protexProject.getProjectName());
	}

    }

    @SuppressWarnings("unused")
    private void collectPendingIDInformation(ProjectPojo protexProject,
	    String key) {
	log.info("START Gathering pending ID information for: "
		+ protexProject.getProjectName());
	CodeTreeHelper treeHelper = psw.getCodeTreeHelper();

	Long totalCount = treeHelper.getTotalPendingIDCount(protexProject);
	if (totalCount > 0) {
	    containsPending = true;
	    log.debug("Found pending information, count: " + totalCount);
	}
	content.put(key, totalCount.toString());
	log.info("DONE gathering pending ID information, total:  " + totalCount);
    }

    // //// HELPER METHODS ///////

    /**
     * Builds an HTML table row for the component
     *
     * @param sb
     * @param comp
     * @param counter
     * @param licenseApi
     * @param protexProject
     * @return
     */
    private int buildComponentRow(BomComponent comp, int counter,
	    LicenseApi licenseApi, ProjectPojo protexProject) {

	StringBuilder sb = new StringBuilder();
	String versionName = comp.getVersionName();
	if (versionName == null) {
	    versionName = UNSPECIFIED_VERSION;
	}

	LicenseInfo licenseInfo = comp.getLicenseInfo();
	GlobalLicense globalLicense;
	try {
	    globalLicense = licenseApi.getLicenseById(licenseInfo
		    .getLicenseId());

	    log.info("Found license for component: " + globalLicense.getName());
	    sb.append("<tr>");
	    sb.append("<td>" + comp.getComponentName() + "</td>");
	    sb.append("<td>" + versionName + "</td>");
	    sb.append("<td>" + globalLicense.getName() + "</td>");
	    sb.append("</tr>");
	    counter++;

	    if (config.isShowFilePaths()) {
		Set<String> filePaths = collectPathsPerComponent(comp,
			protexProject);
		if (filePaths != null) {
		    sb.append("</tr>");
		    sb.append("<td colspan=3>");
		    int currentPathCount = 0;
		    int maxFilePathDisplayCount = config.getShowFilePathCount();
		    for (String path : filePaths) {
			if (currentPathCount < maxFilePathDisplayCount) {
			    sb.append("<li><i>" + path + "</i></li>");
			    sb.append("<br>");
			    currentPathCount++;
			} else {
			    String maxMessage = String
				    .format("The maximum allowed file path count of {%s} exceeded for component: {%s} total available {%s}",
					    maxFilePathDisplayCount,
					    comp.getComponentName(),
					    filePaths.size());
			    log.info(maxMessage);

			    sb.append("<li>" + maxMessage);
			    sb.append("<br>");
			    break;
			}
		    }
		    sb.append("</td>");
		    sb.append("</tr>");
		}
	    }

	    // Place into map
	    String key = getCustomComponentKey(comp);
	    if (componentMap.containsKey(key)) {
		log.warn("Component information already exists, for key (overwriting):"
			+ key);
	    }
	    componentMap.put(key, sb.toString());

	} catch (SdkFault e) {
	    log.error("Error getting license information: " + e.getMessage());
	}

	return counter;

    }

    /**
     * Grabs all the identified file paths for that component.
     *
     * @param comp
     * @param pojo
     * @return
     */
    private Set<String> collectPathsPerComponent(BomComponent comp,
	    ProjectPojo pojo) {
	Set<String> paths = null;
	try {
	    String key = getCustomComponentKey(comp);
	    if (componentToPathMapping == null) {
		componentToPathMapping = collectPaths(pojo);
	    }

	    paths = componentToPathMapping.get(key);
	    if (paths == null) {
		log.warn("Unable to get any paths for component key: " + key);
	    }
	} catch (Exception e) {
	    log.error("Error while collecting paths, for component: "
		    + comp.getComponentName());
	}

	return paths;
    }

    /**
     * Helper method to build out the same component key combination as the one
     * derived from the identified files report. Because the report does not
     * contain true comp IDs, we make our own using a name:version combo.
     * Problem arises when we attempt to retrieve it, since we are retrieving
     * using a Component object instead of strings.
     *
     * @param comp
     * @return
     */
    private String getCustomComponentKey(BomComponent comp) {
	String name = comp.getComponentName();
	String version = comp.getVersionName();

	String key = null;
	if (version != null && version.length() > 0) {
	    key = name + ":" + version;
	} else {
	    key = name + ":" + UNSPECIFIED_VERSION;
	}

	return key;
    }

    /**
     *
     * @param pojo
     * @param maxCount
     * @return
     * @throws Exception
     */
    private HashMap<String, Set<String>> collectPaths(ProjectPojo pojo)
	    throws Exception {

	// TODO: Remove this with 7.1 CSV header implementation.
	// We know the columns in advance (see RGT report_template.xml for
	// reference)
	String compColumnName = "Component";
	String versionColumnName = "Version";
	String fileFolderColumn = "File/Folder";
	String licenseColumn = "License";
	AdHocElement header = new AdHocElement();
	// These are the ones we want
	header.setCoordinate(7, compColumnName);
	header.setCoordinate(8, versionColumnName);
	header.setCoordinate(9, licenseColumn);
	header.setCoordinate(3, fileFolderColumn);
	// These are just here to prove this is a non-vertical sheet
	header.setCoordinate(1, "doesnotmatter");
	header.setCoordinate(2, "doesnotmatter");
	header.setCoordinate(4, "doesnotmatter");
	header.setCoordinate(5, "doesnotmatter");
	header.setCoordinate(6, "doesnotmatter");

	componentToPathMapping = new HashMap<String, Set<String>>();

	log.info("START identified files section report for getAllComponentsAndPaths()");
	ReportUtils reportUtils = new ReportUtils();

	List<AdHocElement> idElements = reportUtils.getReportSection(psw, pojo,
		ReportSectionType.IDENTIFIED_FILES.toString(),
		ReportFormat.CSV, AdHocElement.class, header);

	log.info("Parsing identified files...");
	for (AdHocElement idElement : idElements) {
	    String compName = idElement.getValue(compColumnName);
	    String compVersion = idElement.getValue(versionColumnName);
	    String path = idElement.getValue(fileFolderColumn);

	    String key = compName + ":" + compVersion;

	    Set<String> paths = componentToPathMapping.get(key);
	    if (paths == null) {
		paths = new HashSet<String>();
		componentToPathMapping.put(key, paths);

	    }
	    paths.add(path);
	}
	log.info("DONE identified files section report for getAllComponentsAndPaths()");
	componentToPathMapping.keySet();
	return componentToPathMapping;
    }

    private List<BomComponent> getBomComponents(ProjectPojo protexProject)
	    throws SdkFault {
	if (bomComponents == null) {
	    BomApi bomApi = psw.getInternalApiWrapper().getBomApi();
	    bomComponents = bomApi.getBomComponents(protexProject
		    .getProjectKey());
	}

	return bomComponents;
    }

    public boolean isContainsPending() {
	return containsPending;
    }

    public boolean isContainsConflicts() {
	return containsConflictComponents;
    }

    public boolean isContainsNonApprovedComponents() {
	return containsNonApprovedComponents;
    }

    /**
     * Contains the individual breakdown for each component.
     *
     * @return
     */
    public Map<String, String> getComponentMap() {
	return componentMap;
    }

    public void resetComponentMap() {
	componentMap = new HashMap<String, String>();
    }

}

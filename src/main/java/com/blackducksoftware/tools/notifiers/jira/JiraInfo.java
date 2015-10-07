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

/**
 * Simple bean to hold JIRA connectivity (could be moved to CF)
 * 
 * @author akamen
 *
 */
public class JiraInfo {

    private String url;
    private String adminName;
    private String adminPassword;
    private String defaultSummaryCombined;
    private String defaultSummaryComponent;
    private String defaultIssue;
    private Boolean isJiraConfigured = false;

    public JiraInfo() {
    }

    public String getUrl() {
	return url;
    }

    public void setUrl(String url) {
	this.url = url;
    }

    public String getAdminName() {
	return adminName;
    }

    public void setAdminName(String adminName) {
	this.adminName = adminName;
    }

    public String getAdminPassword() {
	return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
	this.adminPassword = adminPassword;
    }

    public String getDefaultSummaryCombined() {
	return defaultSummaryCombined;
    }

    public void setDefaultSummaryCombined(String setDefaultSummaryCombined) {
	this.defaultSummaryCombined = setDefaultSummaryCombined;
    }

    /**
     * Type of issue in JIRA (task, bug, etc)
     * 
     * @return
     */
    public String getDefaultIssueType() {
	return defaultIssue;
    }

    public void setDefaultIssue(String defaultIssue) {
	this.defaultIssue = defaultIssue;
    }

    public Boolean isJiraConfigured() {
	return isJiraConfigured;
    }

    public void setIsJiraConfigured(Boolean isJiraConfigured) {
	this.isJiraConfigured = isJiraConfigured;
    }

    public String getDefaultSummaryComponent() {
	return defaultSummaryComponent;
    }

    public void setDefaultSummaryComponent(String defaultSummaryComponent) {
	this.defaultSummaryComponent = defaultSummaryComponent;
    }

}

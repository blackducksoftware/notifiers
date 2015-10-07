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

import java.net.URI;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.ProjectRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.LoginInfo;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import com.blackducksoftware.tools.notifiers.common.NotifierException;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * Takes in credentials and establishes a JIRA connection Provides methods to
 * perform basic JIRA tasks
 *
 * @author akamen
 *
 */
public class JiraConnector {

    // Simple class name to avoid full package display.
    private final Logger log = Logger
	    .getLogger(this.getClass().getSimpleName());

    private final String url;
    private final String adminName;
    private final String adminPassword;

    private JiraRestClient jiraClient;
    private Client jerseyClient = Client.create();

    private BasicProject ourProject;

    /**
     * Establishes connection for the admin user.
     *
     * @param url
     * @param adminName
     * @param password
     * @throws Exception
     */
    public JiraConnector(String url, String adminName, String password)
	    throws Exception {
	this.url = url;
	this.adminName = adminName;
	adminPassword = password;

	try {
	    JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
	    URI uri = new URI(url);
	    log.info("Attempting JIRA connection for url: " + url);

	    jiraClient = factory.createWithBasicHttpAuthentication(uri,
		    adminName, password);

	    LoginInfo loginInfo = jiraClient.getSessionClient()
		    .getCurrentSession().claim().getLoginInfo();
	    log.info("Logged in via jira client, last login date: "
		    + loginInfo.getPreviousLoginDate());
	    // Test the user
	    // Invoke the JRJC Client
	    jiraClient.getProjectClient().getAllProjects().claim();
	    Promise<User> promise = jiraClient.getUserClient().getUser(
		    adminName);
	    User user = promise.claim();
	    log.info("Connection established, user found: "
		    + user.getDisplayName());

	    // Create the Jersey client, for those situtions where the JRJC is
	    // incomplete
	    jerseyClient = Client.create();

	} catch (Exception e) {
	    jiraClient.close();
	    throw new Exception("Unable to establish JIRA connection: "
		    + e.getMessage(), e);
	}
    }

    /**
     * Retrieves a project based on the JIRA Key
     *
     * @param projectKey
     * @return
     */
    public Project getProjectByKey(String projectKey) {
	// Iterable<BasicProject> allProjects = this.getAllProjects();
	Promise<Project> promise = jiraClient.getProjectClient().getProject(
		projectKey);
	Project project = promise.claim();
	return project;
    }

    /**
     * Gets a JIRA project by name (slower than by key)
     *
     * @param projectName
     * @return
     */
    public BasicProject getProjectByName(String projectName) {
	if (ourProject == null) {
	    Iterable<BasicProject> projectIterator = getAllProjects();
	    for (BasicProject basicProject : projectIterator) {
		if (basicProject.getName().equalsIgnoreCase(projectName)) {
		    log.info("Found a project matching your name: "
			    + projectName);
		    ourProject = basicProject;
		    break;
		}
	    }

	    if (ourProject == null) {
		log.warn("Unable to find project with name: " + projectName);
	    }
	}
	return ourProject;
    }

    public Iterable<BasicProject> getAllProjects() {
	Iterable<BasicProject> projectIterator = null;
	try {
	    log.info("Fetching all projects...");
	    ProjectRestClient projectClient = jiraClient.getProjectClient();
	    Promise<Iterable<BasicProject>> allProjects = projectClient
		    .getAllProjects();

	    projectIterator = allProjects.claim();
	    log.info("Retrieved all projects");
	} catch (Exception e) {
	    log.error("Error retrieving all projects", e);
	}

	return projectIterator;
    }

    public IssueType getIssueTypeByName(String issueName) {
	Promise<Iterable<IssueType>> allIssueTypes = jiraClient
		.getMetadataClient().getIssueTypes();
	Iterable<IssueType> issueTypeIterator = allIssueTypes.claim();

	IssueType issueType = null;
	for (IssueType type : issueTypeIterator) {
	    String name = type.getName();
	    log.debug("Found issue with name: " + name);
	    if (name.equalsIgnoreCase("task")) {
		log.info("Found JIRA issue type, for name: " + issueName);
		issueType = type;
		break;
	    }
	}

	if (issueType == null) {
	    log.warn("Unable to find JIRA issue type for name: " + issueName);
	}

	return issueType;
    }

    /**
     * Performs a search and returns the issue if the name matches.
     *
     * @param issueSummary
     * @param ourProject
     * @return
     * @throws Exception
     */
    public Issue getIssueBySummaryName(String summaryName,
	    BasicProject ourProject) throws Exception {
	StringBuilder jqlString = new StringBuilder();
	jqlString.append("project=" + ourProject.getKey());
	jqlString.append(" AND "); // Do not forget the space!
	jqlString.append("summary ~" + "'" + summaryName + "'");

	SearchRestClient searchClient = jiraClient.getSearchClient();
	// We only expect one matching result.
	Promise<SearchResult> searchResultsPromise = searchClient.searchJql(
		jqlString.toString(), 1, 0, null);

	Issue ourIssue = null;

	try {
	    SearchResult searchResults = searchResultsPromise.claim();

	    for (Issue issue : searchResults.getIssues()) {
		if (issue.getSummary().equalsIgnoreCase(summaryName)) {
		    log.info("Found match for issue name: " + summaryName);
		    ourIssue = issue;
		}
	    }
	} catch (Exception e) {
	    throw new Exception("Error during searching of issue", e);
	}

	if (ourIssue == null) {
	    log.warn("No issue found with summary: " + summaryName);
	}
	return ourIssue;

    }

    /**
     * Direct REST call to update description Not using JRJC as update is not
     * supported
     *
     * @param ourIssue
     * @param descriptionContent
     * @throws NotifierException
     */
    public void updateDescriptionForIssue(Issue ourIssue,
	    String descriptionContent) throws NotifierException {
	JSONIssueRepresentation issueRepresentation = new JSONIssueRepresentation();
	issueRepresentation.setDescription(descriptionContent);

	Gson gson = new Gson();
	String restIssueUrl = "/rest/api/2/issue/" + ourIssue.getKey();

	jerseyClient
		.addFilter(new HTTPBasicAuthFilter(adminName, adminPassword));
	WebResource webResource = jerseyClient.resource(url + restIssueUrl);

	String input = gson.toJson(issueRepresentation);
	log.info("Sending JSON content: " + input);

	ClientResponse response = webResource.type(MediaType.APPLICATION_JSON)
		.put(ClientResponse.class, input);

	if (response.getClientResponseStatus().getStatusCode() != HttpStatus.SC_NO_CONTENT) {
	    StringBuilder errorMsgSb = new StringBuilder(
		    "Update description failed, client response status: "
			    + response.getClientResponseStatus()
				    .getStatusCode()
			    + "; reason: "
			    + response.getClientResponseStatus()
				    .getReasonPhrase());
	    String output = response.getEntity(String.class);
	    errorMsgSb.append("\nFull message: ");
	    errorMsgSb.append(output);
	    if (output.contains("It is not on the appropriate screen")) {
		errorMsgSb
			.append("\nPlease check to be sure user "
				+ adminName
				+ " has the permissions required to create and edit JIRA issues");
	    }
	    throw new NotifierException(errorMsgSb.toString());
	}

	log.info("Description updated");
    }

    /**
     * Updates the comment for the specified issue
     *
     * @param ourIssue
     * @param updateComment
     */
    public void updateCommentForIssue(Issue ourIssue, String updateComment) {
	log.debug("Start updating comment for issue: "
		+ ourIssue.getCommentsUri());
	jiraClient.getIssueClient().addComment(ourIssue.getCommentsUri(),
		Comment.valueOf(updateComment));
	log.debug("Done updating comment for issue: "
		+ ourIssue.getCommentsUri());
    }

    /**
     * Creates a new issue for a particular project, with a particular issue
     * type and description
     *
     * @param ourProject
     * @param issueType
     * @param issueSummaryTitle
     * @param description
     * @return
     */
    public BasicIssue createNewIssue(BasicProject ourProject,
	    IssueType issueType, String issueSummaryTitle, String description) {
	BasicIssue basicIssue = null;
	try {
	    IssueInputBuilder builder = new IssueInputBuilder(
		    ourProject.getKey(), issueType.getId(), issueSummaryTitle);

	    builder.setDescription(description);
	    IssueInput newIssue = builder.build();

	    Promise<BasicIssue> promiseIssue = jiraClient.getIssueClient()
		    .createIssue(newIssue);
	    basicIssue = promiseIssue.claim();
	} catch (Exception e) {
	    log.error("Unable to create issue", e);
	}
	if (basicIssue != null) {
	    log.info("Created new issue: " + basicIssue.getKey());
	} else {
	    log.warn("Issue not created!");
	}

	return basicIssue;
    }

    /**
     * Cleans up all the REST clients
     */
    public void close() {
	try {
	    if (jerseyClient != null) {
		jerseyClient.destroy();
	    }
	    if (jiraClient != null) {
		jiraClient.close();
	    }
	} catch (Exception ignore) {
	}

    }

}

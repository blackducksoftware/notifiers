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

import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Static helper methods that assist all handler types
 *
 * @author akamen
 *
 */
public class HandlerUtils {

    private static final Logger log = Logger.getLogger(HandlerUtils.class
	    .getClass().getName());

    /**
     * Builds HTML based description content for each component Includes
     * information like license, version, file paths (optional)
     *
     * @param content
     * @param componentMap
     * @param templateKey
     * @return Returns the constructed body;
     */
    public static String buildHtmlDescription(String body) {

	StringBuilder sb = new StringBuilder();
	// Build a table out of it to display nicely.
	sb.append("<table>");
	sb.append("<tr><th>Component Name</th><th>Version</th><th>License</th></tr>");

	sb.append(body);

	sb.append("</table>");

	return sb.toString();

    }

    /**
     * Iterates through all components and builds one merged String.
     *
     * @param componentMap
     * @return
     */
    public static String mergeComponentsIntoBody(
	    Map<String, String> componentMap) {
	StringBuilder sb = new StringBuilder();
	for (String key : componentMap.keySet()) {
	    String componentInformation = componentMap.get(key);
	    sb.append(componentInformation);
	    log.debug("Adding component row information: "
		    + componentInformation);
	}
	return sb.toString();
    }

}

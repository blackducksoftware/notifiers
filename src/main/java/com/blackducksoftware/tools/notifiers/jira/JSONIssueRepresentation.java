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

import java.util.HashMap;
import java.util.Map;

/**
 * Object representing the JIRA issue
 *
 * This is an incomplete representation
 *
 * @author akamen
 *
 */
public class JSONIssueRepresentation {

    private final Map<String, String> fields = new HashMap<String, String>();
    // These should just be constants elsewhere.
    private transient final String fieldDescription = "description";

    public JSONIssueRepresentation() {
    }

    public void setDescription(String description) {
	fields.put(fieldDescription, description);
    }

}

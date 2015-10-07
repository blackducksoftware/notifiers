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

import java.util.List;

import com.blackducksoftware.tools.commonframework.standard.common.ProjectPojo;
import com.blackducksoftware.tools.commonframework.standard.email.EmailContentMap;
import com.blackducksoftware.tools.commonframework.standard.email.EmailTriggerRule;

/**
 * Handler interface
 *
 * @author akamen
 *
 */
public interface IHandler {

    /**
     * Dispatches the actual notification based on implementation
     *
     * @param emailMap
     * @param protexProject
     * @param compMap
     * @param rules
     * @throws Exception
     */
    void processNotification(EmailContentMap emailMap,
	    ProjectInfo protexProject, List<EmailTriggerRule> rules)
	    throws Exception;

    EmailContentMap populateContentMap(ProjectPojo protexProject,
	    ProtexInformationCollector pic, EmailContentMap keysOnlyMap)
	    throws Exception;

}

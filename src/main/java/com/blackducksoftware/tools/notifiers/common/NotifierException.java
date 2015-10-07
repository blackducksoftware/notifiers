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

public class NotifierException extends Exception {

    public NotifierException() {
	super();
    }

    public NotifierException(String arg0, Throwable arg1, boolean arg2,
	    boolean arg3) {
	super(arg0, arg1, arg2, arg3);
    }

    public NotifierException(String arg0, Throwable arg1) {
	super(arg0, arg1);
    }

    public NotifierException(String arg0) {
	super(arg0);
    }

    public NotifierException(Throwable arg0) {
	super(arg0);
    }

}

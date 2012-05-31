/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.kernel.process;

/**
 * @author Sergio González
 */
public class ProcessStatusConstants {

	public static final int COMPLETED = 1;

	public static final int COPYING = 3;

	public static final int DOWNLOADING = 2;

	public static final int ERROR = -1;

	public static final int PREPARED = 0;

	public static final int UPLOADING = 4;

}
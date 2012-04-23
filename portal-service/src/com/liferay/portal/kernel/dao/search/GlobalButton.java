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

package com.liferay.portal.kernel.dao.search;

import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import javax.portlet.PortletResponse;

/**
 * @author Brian Wing Shun Chan
 */
public class GlobalButton {

	public GlobalButton(String name, String id, String image, boolean show, String body) {
		_body = body;
		_id = id;
		_image = image;
		_name = name;
		_show = show;
	}

	public String getId() {
		return _id;
	}

	public String getImage() {
		return _image;
	}


	public String getName() {
		return _name;
	}

	public boolean isShow() {
		return _show;
	}

	public void setId(String id) {
		_id = id;
	}

	public void setImage(String image) {
		_image = image;
	}

	public void setName(String name) {
		_name = name;
	}

	public void setShow(boolean show) {
		_show = show;
	}

	public String getBody() {
		return _body;
	}

	public void setBody(String body) {
		_body = body;
	}

	private String _id;
	private String _image;
	private String _name;
	private boolean _show = true;
	private String _body;

}
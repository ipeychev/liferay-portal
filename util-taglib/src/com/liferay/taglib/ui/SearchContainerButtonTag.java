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

package com.liferay.taglib.ui;

import com.liferay.portal.kernel.bean.BeanPropertiesUtil;
import com.liferay.portal.kernel.dao.search.GlobalButton;
import com.liferay.portal.kernel.dao.search.ResultRow;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.repository.model.RepositoryModel;
import com.liferay.portal.kernel.servlet.taglib.BaseBodyTagSupport;
import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.FriendlyURLNormalizerUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ServerDetector;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.BaseModel;
import com.liferay.portal.util.PortalUtil;
import com.liferay.taglib.util.ParamAndPropertyAncestorTagImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Raymond Augé
 */
public class SearchContainerButtonTag<R>
	extends BaseBodyTagSupport implements BodyTag {

	@Override
	public int doAfterBody() {
		BodyContent bodyContent = getBodyContent();

		if (bodyContent != null) {
			_body = bodyContent.getString();
		}

		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {
		SearchContainerTag<R> searchContainerTag =
			(SearchContainerTag<R>)findAncestorWithClass(
				this, SearchContainerTag.class);

		SearchContainerRowTag<R> searchContainerRowTag =
			(SearchContainerRowTag<R>)findAncestorWithClass(
				this, SearchContainerRowTag.class);

		if (searchContainerTag != null && searchContainerRowTag != null) {
			SearchContainer<R> searchContainer =
				searchContainerTag.getSearchContainer();

			ResultRow row = searchContainerRowTag.getRow();

			int rowIndex = (Integer)pageContext.getAttribute(
				searchContainerRowTag.getIndexVar());

			if (rowIndex == 0) {
				if (Validator.isNull(_id)) {
					_id = _name;
				}

				GlobalButton button = new GlobalButton(
					_name, _id, _image, _show, _body);

				searchContainer.addGlobalButton(button);
			}

			row.addPermission(_id, _show);
		}

		return SKIP_BODY;
	}

	public void cleanUp() {
		_id = null;
		_image = null;
		_name = null;
		_show = true;
	}

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		_id = id;
	}

	public String getImage() {
		return _image;
	}

	public void setImage(String image) {
		_image = image;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public boolean isShow() {
		return _show;
	}

	public void setShow(boolean show) {
		_show = show;
	}

	private String _body;
	private String _id;
	private String _image;
	private String _name;
	private boolean _show = true;

}
/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
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

package com.liferay.taglib.util;

import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.servlet.taglib.util.OutputData;
import com.liferay.portal.kernel.util.ServerDetector;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * @author Shuyang Zhou
 */
public class OutputTag extends PositionTagSupport {

	public static StringBundler getData(
		ServletRequest servletRequest, String webKey) {

		OutputData outputData = _getOutputData(servletRequest);

		return outputData.getMergedData(webKey);
	}

	public OutputTag(String stringBundlerKey) {
		_webKey = stringBundlerKey;
	}

	@Override
	public int doEndTag() throws JspException {
		try {
			if (_output) {
				OutputData outputData = _getOutputData(
					pageContext.getRequest());

				outputData.addData(
					_outputKey, _webKey, getBodyContentAsStringBundler());
			}
			else {
				HttpServletRequest request =
					(HttpServletRequest)pageContext.getRequest();

				String pjax = request.getHeader(HttpHeaders.X_PJAX);

				if (Validator.isNotNull(pjax) && pjax.equals("true")) {
					JspWriter jspWriter = pageContext.getOut();

					jspWriter.write("</noscript>");
				}
			}

			return EVAL_PAGE;
		}
		catch (Exception e) {
			throw new JspException(e);
		}
		finally {
			if (!ServerDetector.isResin()) {
				cleanUp();
			}
		}
	}

	@Override
	public int doStartTag() throws JspException {
		try {
			HttpServletRequest request =
				(HttpServletRequest)pageContext.getRequest();

			if (Validator.isNotNull(_outputKey)) {
				OutputData outputData = _getOutputData(request);

				String pjaxResources = request.getHeader(
					HttpHeaders.X_PJAX_RESOURCES);

				if (((pjaxResources != null) &&
					 pjaxResources.contains(_outputKey)) ||
					!outputData.addOutputKey(_outputKey)) {

					_output = false;

					return SKIP_BODY;
				}
			}

			if (isPositionInLine()) {
				_output = false;

				if (Validator.isNotNull(_outputKey)) {
					String pjax = request.getHeader(HttpHeaders.X_PJAX);

					if (Validator.isNotNull(pjax) && pjax.equals("true")) {
						JspWriter jspWriter = pageContext.getOut();

						StringBundler sb = new StringBundler(3);

						sb.append("<noscript data-outputkey=\"");
						sb.append(_outputKey);
						sb.append("\">");

						jspWriter.write(sb.toString());
					}
				}

				return EVAL_BODY_INCLUDE;
			}

			_output = true;

			return EVAL_BODY_BUFFERED;
		}
		catch (Exception e) {
			throw new JspException(e);
		}
	}

	public void setOutputKey(String outputKey) {
		_outputKey = outputKey;
	}

	private static OutputData _getOutputData(ServletRequest servletRequest) {
		OutputData outputData = (OutputData)servletRequest.getAttribute(
			WebKeys.OUTPUT_DATA);

		if (outputData == null) {
			outputData = new OutputData();

			servletRequest.setAttribute(WebKeys.OUTPUT_DATA, outputData);
		}

		return outputData;
	}

	private boolean _output;
	private String _outputKey;
	private final String _webKey;

}
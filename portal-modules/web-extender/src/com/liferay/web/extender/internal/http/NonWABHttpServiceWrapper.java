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

package com.liferay.web.extender.internal.http;


import com.liferay.portal.kernel.servlet.ServletContextPool;
import com.liferay.web.extender.internal.servlet.BundleServletContext;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

/**
 * @author Raymond Augé
 */
public class NonWABHttpServiceWrapper implements ExtendedHttpService, HttpService {

	public NonWABHttpServiceWrapper(BundleServletContext bundleServletContext) {
		_bundleServletContext = bundleServletContext;
		_registrations = new ArrayList<Object>();
	}

	public HttpContext createDefaultHttpContext() {
		return new DefaultHttpContext(_bundleServletContext);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void registerFilter(
			String filterMapping, Filter filter, Dictionary initParams,
			HttpContext httpContext)
		throws NamespaceException, ServletException {

		if (httpContext == null) {
			httpContext = createDefaultHttpContext();
		}

		_bundleServletContext.registerFilter(
			filterMapping, filter, initParams, httpContext);

		_registrations.add(filterMapping);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void registerListener(
			Object listener, Dictionary initParams, HttpContext httpContext)
		throws ServletException {

		if (httpContext == null) {
			httpContext = createDefaultHttpContext();
		}

		_bundleServletContext.registerListener(
			listener, initParams, httpContext);

		_registrations.add(listener);
	}

	public void registerResources(
			String alias, String name, HttpContext httpContext)
		throws NamespaceException {

		if (httpContext == null) {
			httpContext = createDefaultHttpContext();
		}

		_bundleServletContext.registerResources(alias, name, httpContext);

		_registrations.add(alias);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void registerServlet(
			String alias, Servlet servlet, Dictionary initparams,
			HttpContext httpContext)
		throws NamespaceException, ServletException {

		if (httpContext == null) {
			httpContext = createDefaultHttpContext();
		}

		_bundleServletContext.registerServlet(
			alias, servlet, initparams, httpContext);

		_registrations.add(alias);
	}

	public void unregister(String alias) {
		_bundleServletContext.unregister(alias);

		_registrations.remove(alias);

		checkRegistrations();
	}

	public void unregisterFilter(String filterMapping) {
		_bundleServletContext.unregisterFilter(filterMapping);

		_registrations.remove(filterMapping);

		checkRegistrations();
	}

	public void unregisterListener(Object listener) {
		_bundleServletContext.unregisterListener(listener);

		_registrations.remove(listener);

		checkRegistrations();
	}

	protected void checkRegistrations() {
		if (!_registrations.isEmpty()) {
			return;
		}

		ServletContextPool.remove(
			_bundleServletContext.getServletContextName());

		_bundleServletContext = null;
	}

	private BundleServletContext _bundleServletContext;
	private List<Object> _registrations;

}
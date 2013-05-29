/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
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

package com.liferay.portalweb.socialofficehome.sites.site.viewpaginationsitesdirectory;

import com.liferay.portalweb.portal.BaseTestSuite;
import com.liferay.portalweb.socialofficehome.sites.site.addsitessite.TearDownSOSitesTest;
import com.liferay.portalweb.socialofficehome.sites.site.addsitessites.AddSitesSite10Test;
import com.liferay.portalweb.socialofficehome.sites.site.addsitessites.AddSitesSite1Test;
import com.liferay.portalweb.socialofficehome.sites.site.addsitessites.AddSitesSite2Test;
import com.liferay.portalweb.socialofficehome.sites.site.addsitessites.AddSitesSite3Test;
import com.liferay.portalweb.socialofficehome.sites.site.addsitessites.AddSitesSite4Test;
import com.liferay.portalweb.socialofficehome.sites.site.addsitessites.AddSitesSite5Test;
import com.liferay.portalweb.socialofficehome.sites.site.addsitessites.AddSitesSite6Test;
import com.liferay.portalweb.socialofficehome.sites.site.addsitessites.AddSitesSite7Test;
import com.liferay.portalweb.socialofficehome.sites.site.addsitessites.AddSitesSite8Test;
import com.liferay.portalweb.socialofficehome.sites.site.addsitessites.AddSitesSite9Test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Brian Wing Shun Chan
 */
public class ViewPaginationSitesDirectoryTests extends BaseTestSuite {
	public static Test suite() {
		TestSuite testSuite = new TestSuite();
		testSuite.addTestSuite(AddSitesSite1Test.class);
		testSuite.addTestSuite(AddSitesSite2Test.class);
		testSuite.addTestSuite(AddSitesSite3Test.class);
		testSuite.addTestSuite(AddSitesSite4Test.class);
		testSuite.addTestSuite(AddSitesSite5Test.class);
		testSuite.addTestSuite(AddSitesSite6Test.class);
		testSuite.addTestSuite(AddSitesSite7Test.class);
		testSuite.addTestSuite(AddSitesSite8Test.class);
		testSuite.addTestSuite(AddSitesSite9Test.class);
		testSuite.addTestSuite(AddSitesSite10Test.class);
		testSuite.addTestSuite(ViewPaginationSitesDirectoryTest.class);
		testSuite.addTestSuite(TearDownSOSitesTest.class);
		testSuite.addTestSuite(TearDownSOSitesTest.class);

		return testSuite;
	}
}
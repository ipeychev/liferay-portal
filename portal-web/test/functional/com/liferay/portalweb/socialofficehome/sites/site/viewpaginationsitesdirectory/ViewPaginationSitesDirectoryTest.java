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

import com.liferay.portalweb.portal.BaseTestCase;
import com.liferay.portalweb.portal.util.RuntimeVariables;

/**
 * @author Brian Wing Shun Chan
 */
public class ViewPaginationSitesDirectoryTest extends BaseTestCase {
	public void testViewPaginationSitesDirectory() throws Exception {
		selenium.selectWindow("null");
		selenium.selectFrame("relative=top");
		selenium.open("/user/joebloggs/so/dashboard/");
		selenium.waitForVisible("//li[contains(@class, 'selected')]/a/span");
		assertEquals(RuntimeVariables.replace("Dashboard"),
			selenium.getText("//li[contains(@class, 'selected')]/a/span"));
		selenium.waitForVisible("//li[@class='more']/a");
		assertEquals(RuntimeVariables.replace("View All (12)"),
			selenium.getText("//li[@class='more']/a"));
		selenium.clickAt("//li[@class='more']/a",
			RuntimeVariables.replace("View All (12)"));
		Thread.sleep(1000);
		selenium.waitForVisible("//h1[@class='header-title']/span");
		assertEquals(RuntimeVariables.replace("Directory"),
			selenium.getText("//h1[@class='header-title']/span"));
		assertEquals(RuntimeVariables.replace("Liferay, Inc."),
			selenium.getText(
				"xPath=(//span[@class='name']/a)[contains(.,'Liferay, Inc.')]"));
		assertEquals(RuntimeVariables.replace("Liferay"),
			selenium.getText("xPath=(//span[@class='name']/a)[.='Liferay']"));
		assertEquals(RuntimeVariables.replace("Open Site1 Name"),
			selenium.getText(
				"xPath=(//span[@class='name']/a)[contains(.,'Open Site1 Name')]"));
		assertEquals(RuntimeVariables.replace("Open Site10 Name"),
			selenium.getText(
				"xPath=(//span[@class='name']/a)[contains(.,'Open Site10 Name')]"));
		assertEquals(RuntimeVariables.replace("Open Site2 Name"),
			selenium.getText(
				"xPath=(//span[@class='name']/a)[contains(.,'Open Site2 Name')]"));
		assertEquals(RuntimeVariables.replace("Open Site3 Name"),
			selenium.getText(
				"xPath=(//span[@class='name']/a)[contains(.,'Open Site3 Name')]"));
		assertEquals(RuntimeVariables.replace("Open Site4 Name"),
			selenium.getText(
				"xPath=(//span[@class='name']/a)[contains(.,'Open Site4 Name')]"));
		assertEquals(RuntimeVariables.replace("Open Site5 Name"),
			selenium.getText(
				"xPath=(//span[@class='name']/a)[contains(.,'Open Site5 Name')]"));
		assertEquals(RuntimeVariables.replace("Open Site6 Name"),
			selenium.getText(
				"xPath=(//span[@class='name']/a)[contains(.,'Open Site6 Name')]"));
		assertEquals(RuntimeVariables.replace("Open Site7 Name"),
			selenium.getText(
				"xPath=(//span[@class='name']/a)[contains(.,'Open Site7 Name')]"));
		assertEquals(RuntimeVariables.replace("Page 1 of 2"),
			selenium.getText("//span[@class='page-indicator']"));
		assertEquals("Next", selenium.getValue("//input[@value='Next']"));
		selenium.clickAt("//input[@value='Next']",
			RuntimeVariables.replace("Next"));
		selenium.waitForVisible(
			"xPath=(//span[@class='name']/a)[contains(.,'Open Site8 Name')]");
		assertEquals(RuntimeVariables.replace("Open Site8 Name"),
			selenium.getText(
				"xPath=(//span[@class='name']/a)[contains(.,'Open Site8 Name')]"));
		assertEquals(RuntimeVariables.replace("Open Site9 Name"),
			selenium.getText(
				"xPath=(//span[@class='name']/a)[contains(.,'Open Site9 Name')]"));
		assertEquals(RuntimeVariables.replace("Page 2 of 2"),
			selenium.getText("//span[@class='page-indicator']"));
		assertEquals("Previous", selenium.getValue("//input[@value='Previous']"));
		selenium.clickAt("//input[@value='Previous']",
			RuntimeVariables.replace("Previous"));
		selenium.waitForVisible(
			"xPath=(//span[@class='name']/a)[contains(.,'Liferay, Inc.')]");
		assertEquals(RuntimeVariables.replace("Liferay, Inc."),
			selenium.getText(
				"xPath=(//span[@class='name']/a)[contains(.,'Liferay, Inc.')]"));
		assertEquals(RuntimeVariables.replace("Liferay"),
			selenium.getText("xPath=(//span[@class='name']/a)[.='Liferay']"));
		assertEquals(RuntimeVariables.replace("Open Site1 Name"),
			selenium.getText(
				"xPath=(//span[@class='name']/a)[contains(.,'Open Site1 Name')]"));
		assertEquals(RuntimeVariables.replace("Open Site10 Name"),
			selenium.getText(
				"xPath=(//span[@class='name']/a)[contains(.,'Open Site10 Name')]"));
		assertEquals(RuntimeVariables.replace("Open Site2 Name"),
			selenium.getText(
				"xPath=(//span[@class='name']/a)[contains(.,'Open Site2 Name')]"));
		assertEquals(RuntimeVariables.replace("Open Site3 Name"),
			selenium.getText(
				"xPath=(//span[@class='name']/a)[contains(.,'Open Site3 Name')]"));
		assertEquals(RuntimeVariables.replace("Open Site4 Name"),
			selenium.getText(
				"xPath=(//span[@class='name']/a)[contains(.,'Open Site4 Name')]"));
		assertEquals(RuntimeVariables.replace("Open Site5 Name"),
			selenium.getText(
				"xPath=(//span[@class='name']/a)[contains(.,'Open Site5 Name')]"));
		assertEquals(RuntimeVariables.replace("Open Site6 Name"),
			selenium.getText(
				"xPath=(//span[@class='name']/a)[contains(.,'Open Site6 Name')]"));
		assertEquals(RuntimeVariables.replace("Open Site7 Name"),
			selenium.getText(
				"xPath=(//span[@class='name']/a)[contains(.,'Open Site7 Name')]"));
		assertEquals(RuntimeVariables.replace("Page 1 of 2"),
			selenium.getText("//span[@class='page-indicator']"));
		assertEquals("Next", selenium.getValue("//input[@value='Next']"));
	}
}
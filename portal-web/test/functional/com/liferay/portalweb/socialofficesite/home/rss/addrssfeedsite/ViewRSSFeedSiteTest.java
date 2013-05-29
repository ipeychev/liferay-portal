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

package com.liferay.portalweb.socialofficesite.home.rss.addrssfeedsite;

import com.liferay.portalweb.portal.BaseTestCase;
import com.liferay.portalweb.portal.util.RuntimeVariables;

/**
 * @author Brian Wing Shun Chan
 */
public class ViewRSSFeedSiteTest extends BaseTestCase {
	public void testViewRSSFeedSite() throws Exception {
		selenium.selectWindow("null");
		selenium.selectFrame("relative=top");
		selenium.open("/user/joebloggs/so/dashboard/");
		assertEquals(RuntimeVariables.replace("Sites"),
			selenium.getText("//div[@id='so-sidebar']/h3"));
		assertTrue(selenium.isVisible("//input[@class='search-input']"));
		selenium.type("//input[@class='search-input']",
			RuntimeVariables.replace("Open"));
		Thread.sleep(1000);
		assertEquals(RuntimeVariables.replace("Open Site Name"),
			selenium.getText(
				"//li[contains(@class, 'social-office-enabled')]/span[2]/a"));
		selenium.clickAt("//li[contains(@class, 'social-office-enabled')]/span[2]/a",
			RuntimeVariables.replace("Open Site Name"));
		selenium.waitForPageToLoad("30000");
		selenium.waitForVisible(
			"//div[contains(.,'Cnet')]/div[@class='feed-title']");
		assertTrue(selenium.isPartialText(
				"//div[contains(.,'Cnet')]/div[@class='feed-title']", "Cnet"));
		assertTrue(selenium.isVisible(
				"//div[contains(.,'Cnet')]/div[contains(@class,'feed-published-date')]"));
		assertTrue(selenium.isVisible(
				"//div[contains(.,'Cnet')]/div[@class='feed-description']"));
		assertTrue(selenium.isVisible("//a[contains(@title,'CNET News')]/img"));
		assertTrue(selenium.isVisible(
				"xPath=(//div[contains(.,'Cnet')]/div[@class='feed-entries']/div[@class='feed-entry']/span[@class='feed-entry-title'])[1]"));
		assertTrue(selenium.isVisible(
				"xPath=(//div[contains(.,'Cnet')]/div[@class='feed-entries']/div[@class='feed-entry']/span[@class='feed-entry-title'])[2]"));
		assertTrue(selenium.isVisible(
				"xPath=(//div[contains(.,'Cnet')]/div[@class='feed-entries']/div[@class='feed-entry']/span[@class='feed-entry-title'])[3]"));
		assertTrue(selenium.isVisible(
				"xPath=(//div[contains(.,'Cnet')]/div[@class='feed-entries']/div[@class='feed-entry']/img)[1]"));
		selenium.clickAt("xPath=(//div[contains(.,'Cnet')]/div[@class='feed-entries']/div[@class='feed-entry']/img)[1]",
			RuntimeVariables.replace("CNET Feed Entry 1 Expand Button"));
		selenium.waitForVisible(
			"xPath=(//div[contains(.,'Cnet')]/div[@class='feed-entries']/div[@class='feed-entry']/div[@class='feed-entry-content']/div[@class='feed-date'])[1]");
		assertTrue(selenium.isVisible(
				"xPath=(//div[contains(.,'Cnet')]/div[@class='feed-entries']/div[@class='feed-entry']/div[@class='feed-entry-content']/div[@class='feed-date'])[1]"));
		assertTrue(selenium.isVisible(
				"xPath=(//div[contains(.,'Cnet')]/div[@class='feed-entries']/div[@class='feed-entry']/div[@class='feed-entry-content']/span[@class='feed-entry-author'])[1]"));
		assertTrue(selenium.isVisible(
				"xPath=(//div[contains(.,'Cnet')]/div[@class='feed-entries']/div[@class='feed-entry']/div[@class='feed-entry-content'])[1]"));
		assertTrue(selenium.isPartialText(
				"xPath=(//div[contains(.,'Cnet')]/div[@class='feed-entries']/div[@class='feed-entry']/div[@class='feed-entry-content']/a)[1]",
				"Read more"));
	}
}
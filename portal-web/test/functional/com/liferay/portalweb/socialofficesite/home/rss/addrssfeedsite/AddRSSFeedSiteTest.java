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
public class AddRSSFeedSiteTest extends BaseTestCase {
	public void testAddRSSFeedSite() throws Exception {
		int label = 1;

		while (label >= 1) {
			switch (label) {
			case 1:
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

				boolean configurationVisible = selenium.isVisible(
						"//header[contains(.,'RSS')]/menu/span/ul/li/strong/a");

				if (configurationVisible) {
					label = 2;

					continue;
				}

				selenium.clickAt("//div[@id='dockbar']",
					RuntimeVariables.replace("Dockbar"));
				selenium.waitForElementPresent(
					"//script[contains(@src,'/aui/aui-editable/aui-editable-min.js')]");
				selenium.clickAt("//li[@class='toggle-controls']/a",
					RuntimeVariables.replace("Edit Controls"));
				selenium.waitForVisible(
					"//header[contains(.,'RSS')]/menu/span/ul/li/strong/a");

			case 2:
				Thread.sleep(1000);
				assertEquals(RuntimeVariables.replace("Options"),
					selenium.getText(
						"//header[contains(.,'RSS')]/menu/span/ul/li/strong/a"));
				selenium.clickAt("//header[contains(.,'RSS')]/menu/span/ul/li/strong/a",
					RuntimeVariables.replace("Options"));
				selenium.waitForVisible(
					"//div[@class='lfr-component lfr-menu-list']/ul/li[contains(.,'Configuration')]/a");
				assertEquals(RuntimeVariables.replace("Configuration"),
					selenium.getText(
						"//div[@class='lfr-component lfr-menu-list']/ul/li[contains(.,'Configuration')]/a"));
				selenium.clickAt("//div[@class='lfr-component lfr-menu-list']/ul/li[contains(.,'Configuration')]/a",
					RuntimeVariables.replace("Configuration"));
				selenium.waitForVisible(
					"//iframe[contains(@id,'configurationIframeDialog')]");
				selenium.selectFrame(
					"//iframe[contains(@id,'configurationIframeDialog')]");
				selenium.waitForVisible("//button[contains(@class,'add-row')]");
				selenium.clickAt("//button[contains(@class,'add-row')]",
					RuntimeVariables.replace("Add Row Button"));
				selenium.waitForVisible("//input[@id='_86_title2']");
				selenium.type("//input[@id='_86_title2']",
					RuntimeVariables.replace("Cnet"));
				selenium.type("//input[@id='_86_url2']",
					RuntimeVariables.replace(
						"http://feeds.feedburner.com/cnet/tcoc"));

				boolean showFeedTitleChecked = selenium.isChecked(
						"//input[@id='_86_showFeedTitleCheckbox']");

				if (showFeedTitleChecked) {
					label = 3;

					continue;
				}

				selenium.clickAt("//input[@id='_86_showFeedTitleCheckbox']",
					RuntimeVariables.replace("Show Feed Title Checkbox"));

			case 3:
				assertTrue(selenium.isChecked(
						"//input[@id='_86_showFeedTitleCheckbox']"));

				boolean showFeedPublishedDateChecked = selenium.isChecked(
						"//input[@id='_86_showFeedPublishedDateCheckbox']");

				if (showFeedPublishedDateChecked) {
					label = 4;

					continue;
				}

				selenium.clickAt("//input[@id='_86_showFeedPublishedDateCheckbox']",
					RuntimeVariables.replace(
						"Show Feed Published Date Checkbox"));

			case 4:
				assertTrue(selenium.isChecked(
						"//input[@id='_86_showFeedPublishedDateCheckbox']"));

				boolean showFeedDescriptionChecked = selenium.isChecked(
						"//input[@id='_86_showFeedDescriptionCheckbox']");

				if (showFeedDescriptionChecked) {
					label = 5;

					continue;
				}

				selenium.clickAt("//input[@id='_86_showFeedDescriptionCheckbox']",
					RuntimeVariables.replace("Show Feed Description Checkbox"));

			case 5:
				assertTrue(selenium.isChecked(
						"//input[@id='_86_showFeedDescriptionCheckbox']"));

				boolean showFeedImageChecked = selenium.isChecked(
						"//input[@id='_86_showFeedImageCheckbox']");

				if (showFeedImageChecked) {
					label = 6;

					continue;
				}

				selenium.clickAt("//input[@id='_86_showFeedImageCheckbox']",
					RuntimeVariables.replace("Show Feed Image Checkbox"));

			case 6:
				assertTrue(selenium.isChecked(
						"//input[@id='_86_showFeedImageCheckbox']"));

				boolean showFeedItemAuthorChecked = selenium.isChecked(
						"//input[@id='_86_showFeedItemAuthorCheckbox']");

				if (showFeedItemAuthorChecked) {
					label = 7;

					continue;
				}

				selenium.clickAt("//input[@id='_86_showFeedItemAuthorCheckbox']",
					RuntimeVariables.replace("Show Feed Item Author Checkbox"));

			case 7:
				assertTrue(selenium.isChecked(
						"//input[@id='_86_showFeedItemAuthorCheckbox']"));
				selenium.select("//select[@id='_86_entriesPerFeed']",
					RuntimeVariables.replace("3"));
				selenium.select("//select[@id='_86_expandedEntriesPerFeed']",
					RuntimeVariables.replace("0"));
				selenium.clickAt("//input[@value='Save']",
					RuntimeVariables.replace("Save"));
				selenium.waitForVisible("//div[@class='portlet-msg-success']");
				assertEquals(RuntimeVariables.replace(
						"You have successfully updated the setup."),
					selenium.getText("//div[@class='portlet-msg-success']"));
				selenium.selectFrame("relative=top");

			case 100:
				label = -1;
			}
		}
	}
}
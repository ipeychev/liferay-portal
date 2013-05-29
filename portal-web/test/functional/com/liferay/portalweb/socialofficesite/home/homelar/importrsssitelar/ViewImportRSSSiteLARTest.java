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

package com.liferay.portalweb.socialofficesite.home.homelar.importrsssitelar;

import com.liferay.portalweb.portal.BaseTestCase;
import com.liferay.portalweb.portal.util.RuntimeVariables;

/**
 * @author Brian Wing Shun Chan
 */
public class ViewImportRSSSiteLARTest extends BaseTestCase {
	public void testViewImportRSSSiteLAR() throws Exception {
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
				selenium.waitForVisible(
					"//input[@id='_86_showFeedTitleCheckbox']");
				assertTrue(selenium.isChecked(
						"//input[@id='_86_showFeedTitleCheckbox']"));
				assertTrue(selenium.isChecked(
						"//input[@id='_86_showFeedPublishedDateCheckbox']"));
				assertTrue(selenium.isChecked(
						"//input[@id='_86_showFeedDescriptionCheckbox']"));
				assertTrue(selenium.isChecked(
						"//input[@id='_86_showFeedImageCheckbox']"));
				assertTrue(selenium.isChecked(
						"//input[@id='_86_showFeedItemAuthorCheckbox']"));
				assertEquals("3",
					selenium.getSelectedLabel(
						"//select[@id='_86_entriesPerFeed']"));
				assertEquals("0",
					selenium.getSelectedLabel(
						"//select[@id='_86_expandedEntriesPerFeed']"));
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
					"//div[contains(.,'NYT')]/div[@class='feed-title']");
				assertTrue(selenium.isPartialText(
						"//div[contains(.,'NYT')]/div[@class='feed-title']",
						"NYT"));
				assertTrue(selenium.isPartialText(
						"//div[contains(.,'Cnet')]/div[@class='feed-title']",
						"Cnet"));
				assertTrue(selenium.isVisible(
						"//div[contains(.,'Cnet')]/div[contains(@class,'feed-published-date')]"));
				assertTrue(selenium.isVisible(
						"//div[contains(.,'Cnet')]/div[@class='feed-description']"));
				assertTrue(selenium.isVisible(
						"//a[contains(@title,'CNET News')]/img"));
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

			case 100:
				label = -1;
			}
		}
	}
}
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.workflow.node.delegate.util;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns an enriched site plan (and optional blog entries) into the ordered
 * batch-engine envelopes the Content Site Generator commits: 01-site,
 * 02-asset-library, 03-connected-site, 04-fragment-set, 05-fragments,
 * 06-pages, and (only when blog entries are present) 07-blogs.
 *
 * @author Mario Gomes
 * @author Iliyan Peychev
 */
public class SitePlanBatchFileBuilder {

	public static String detectLanguages(
		JSONArray itemsJSONArray, String fileName) {

		Set<String> locales = new TreeSet<>();

		if ((itemsJSONArray != null) && (itemsJSONArray.length() > 0)) {
			Matcher i18nMatcher = _i18nBlockPattern.matcher(
				itemsJSONArray.toString());

			while (i18nMatcher.find()) {
				Matcher localeMatcher = _localeKeyPattern.matcher(
					i18nMatcher.group(1));

				while (localeMatcher.find()) {
					String locale = localeMatcher.group(1);

					locales.add(StringUtil.toLowerCase(locale));
				}
			}
		}

		if (locales.isEmpty()) {
			Matcher matcher = _fileNameLanguagePattern.matcher(fileName);

			if (matcher.find()) {
				String locale = matcher.group(1);

				locales.add(StringUtil.toLowerCase(locale));
			}
		}

		return String.join(",", locales);
	}

	public static JSONObject stripMetadata(JSONObject itemJSONObject) {
		JSONObject strippedJSONObject = JSONFactoryUtil.createJSONObject();

		for (String key : itemJSONObject.keySet()) {
			if (_metadataKeys.contains(key)) {
				continue;
			}

			strippedJSONObject.put(key, itemJSONObject.get(key));
		}

		return strippedJSONObject;
	}

	public SitePlanBatchFileBuilder(
		String enrichedSitePlan, String blogEntries) {

		_enrichedSitePlan = enrichedSitePlan;
		_blogEntries = blogEntries;
	}

	public List<BatchFile> build() throws Exception {
		if (Validator.isNull(_enrichedSitePlan)) {
			throw new IllegalArgumentException(
				"Enriched site plan is required");
		}

		JSONObject planJSONObject = JSONFactoryUtil.createJSONObject(
			_enrichedSitePlan);

		JSONObject siteJSONObject = planJSONObject.getJSONObject("site");

		if (siteJSONObject == null) {
			throw new IllegalArgumentException(
				"Enriched site plan does not contain a \"site\" object");
		}

		String siteERC = siteJSONObject.getString("externalReferenceCode");
		String siteTitle = siteJSONObject.getString("name");

		List<BatchFile> batchFiles = new ArrayList<>();

		batchFiles.add(_buildSiteBatchFile(siteERC, siteJSONObject, siteTitle));
		batchFiles.add(_buildAssetLibraryBatchFile(siteERC, siteTitle));
		batchFiles.add(_buildConnectedSiteBatchFile(siteERC, siteTitle));
		batchFiles.add(_buildFragmentSetBatchFile(siteERC, siteTitle));
		batchFiles.add(_buildFragmentsBatchFile(planJSONObject, siteERC));
		batchFiles.add(_buildPagesBatchFile(planJSONObject, siteERC));

		BatchFile blogsBatchFile = _buildBlogsBatchFile(siteERC);

		if (blogsBatchFile != null) {
			batchFiles.add(blogsBatchFile);
		}

		return batchFiles;
	}

	public static class BatchFile {

		public BatchFile(String fileName, JSONObject envelopeJSONObject) {
			_fileName = fileName;
			_envelopeJSONObject = envelopeJSONObject;
		}

		public JSONObject getEnvelopeJSONObject() {
			return _envelopeJSONObject;
		}

		public String getFileName() {
			return _fileName;
		}

		private final JSONObject _envelopeJSONObject;
		private final String _fileName;

	}

	private void _appendDraftSuffix(JSONArray elementsJSONArray) {
		for (int i = 0; i < elementsJSONArray.length(); i++) {
			JSONObject elementJSONObject = elementsJSONArray.getJSONObject(i);

			String erc = elementJSONObject.getString("externalReferenceCode");

			if (Validator.isNotNull(erc)) {
				elementJSONObject.put("externalReferenceCode", erc + "-draft");
			}

			String parentERC = elementJSONObject.getString(
				"parentExternalReferenceCode");

			if (Validator.isNotNull(parentERC)) {
				elementJSONObject.put(
					"parentExternalReferenceCode", parentERC + "-draft");
			}

			JSONObject definitionJSONObject = elementJSONObject.getJSONObject(
				"pageElementDefinition");

			if (definitionJSONObject != null) {
				JSONObject fragmentInstanceJSONObject =
					definitionJSONObject.getJSONObject("fragmentInstance");

				if (fragmentInstanceJSONObject != null) {
					String instanceERC = fragmentInstanceJSONObject.getString(
						"fragmentInstanceExternalReferenceCode");

					if (Validator.isNotNull(instanceERC)) {
						fragmentInstanceJSONObject.put(
							"fragmentInstanceExternalReferenceCode",
							instanceERC + "-draft");
					}
				}
			}

			JSONArray childrenJSONArray = elementJSONObject.getJSONArray(
				"pageElements");

			if ((childrenJSONArray != null) &&
				(childrenJSONArray.length() > 0)) {

				_appendDraftSuffix(childrenJSONArray);
			}
		}
	}

	private BatchFile _buildAssetLibraryBatchFile(
			String siteERC, String siteTitle)
		throws Exception {

		String assetLibraryERC = siteERC + "-space";

		JSONObject assetLibraryBatchJSONObject = _createBatchWrapper(
			"com.liferay.headless.asset.library.dto.v1_0.AssetLibrary", false,
			siteERC, false);

		// AssetLibraryResourceImpl._putUnicodeProperties returns null when
		// settings is null; the upsert path then NPEs in
		// UnicodePropertiesBuilder.putAll. Send an empty settings object to
		// take the non-null branch with default values.

		assetLibraryBatchJSONObject.put(
			"items",
			JSONFactoryUtil.createJSONArray(
			).put(
				JSONUtil.put(
					"assetLibraryKey", assetLibraryERC
				).put(
					"externalReferenceCode", assetLibraryERC
				).put(
					"name", siteTitle + " Space"
				).put(
					"name_i18n", _createI18nJSON("en-US", siteTitle + " Space")
				).put(
					"settings", JSONFactoryUtil.createJSONObject()
				).put(
					"type", "Space"
				)
			));

		return new BatchFile(
			"02-asset-library.batch-engine-data.json",
			assetLibraryBatchJSONObject);
	}

	private BatchFile _buildBlogsBatchFile(String siteERC) {
		if (Validator.isNull(_blogEntries)) {
			return null;
		}

		String trimmedBlogEntries = _blogEntries.trim();

		JSONArray blogJSONArray = null;

		try {
			if (trimmedBlogEntries.startsWith("[")) {
				blogJSONArray = JSONFactoryUtil.createJSONArray(
					trimmedBlogEntries);
			}
			else if (trimmedBlogEntries.startsWith("{")) {
				JSONObject wrapperJSONObject = JSONFactoryUtil.createJSONObject(
					trimmedBlogEntries);

				for (String key : wrapperJSONObject.keySet()) {
					Object value = wrapperJSONObject.get(key);

					if (value instanceof JSONArray) {
						blogJSONArray = (JSONArray)value;

						break;
					}
				}
			}
		}
		catch (Exception exception) {
			_log.error(
				"Unable to parse blogEntries as JSON; skipping 07-blogs",
				exception);

			return null;
		}

		if (blogJSONArray == null) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to parse blogEntries as a JSON array");
			}

			return null;
		}

		_sanitizeBlogKeywords(blogJSONArray);

		return new BatchFile(
			"07-blogs.batch-engine-data.json",
			JSONUtil.put(
				"configuration",
				JSONUtil.put(
					"className", "com.liferay.object.rest.dto.v1_0.ObjectEntry"
				).put(
					"multiCompany", true
				).put(
					"parameters",
					JSONUtil.put(
						"containsHeaders", "true"
					).put(
						"createStrategy", "UPSERT"
					).put(
						"featureFlag", "LPD-17564"
					).put(
						"importStrategy", "ON_ERROR_FAIL"
					).put(
						"scopeKey", siteERC + "-space"
					).put(
						"updateStrategy", "UPDATE"
					)
				).put(
					"taskItemDelegateName", "CMSBlog"
				)
			).put(
				"items", blogJSONArray
			));
	}

	private BatchFile _buildConnectedSiteBatchFile(
		String siteERC, String siteTitle) {

		String assetLibraryERC = siteERC + "-space";

		JSONObject connectedSiteConfigurationJSONObject = JSONUtil.put(
			"className",
			"com.liferay.headless.asset.library.dto.v1_0.ConnectedSite"
		).put(
			"multiCompany", true
		).put(
			"parameters",
			JSONUtil.put(
				"assetLibraryExternalReferenceCode", assetLibraryERC
			).put(
				"containsHeaders", "true"
			).put(
				"createStrategy", "UPSERT"
			).put(
				"importStrategy", "ON_ERROR_FAIL"
			)
		).put(
			"taskItemDelegateName", "DEFAULT"
		);

		return new BatchFile(
			"03-connected-site.batch-engine-data.json",
			JSONUtil.put(
				"configuration", connectedSiteConfigurationJSONObject
			).put(
				"items",
				JSONFactoryUtil.createJSONArray(
				).put(
					JSONUtil.put(
						"descriptiveName", siteTitle
					).put(
						"externalReferenceCode", siteERC
					).put(
						"name", siteTitle
					).put(
						"searchable", true
					)
				)
			));
	}

	private BatchFile _buildFragmentsBatchFile(
			JSONObject planJSONObject, String siteERC)
		throws Exception {

		String fragmentSetERC = siteERC + "-fragments";

		JSONArray customFragmentsJSONArray = planJSONObject.getJSONArray(
			"customFragments");

		JSONObject fragmentsBatchJSONObject = _createBatchWrapper(
			"com.liferay.headless.admin.fragment.dto.v1_0.Fragment", true,
			siteERC, false);

		JSONArray fragmentItemsJSONArray = JSONFactoryUtil.createJSONArray();

		if ((customFragmentsJSONArray != null) &&
			(customFragmentsJSONArray.length() > 0)) {

			for (int i = 0; i < customFragmentsJSONArray.length(); i++) {
				JSONObject fragmentJSONObject =
					customFragmentsJSONArray.getJSONObject(i);

				String fragmentKey = fragmentJSONObject.getString("key");

				JSONObject approvedVersionJSONObject = JSONUtil.put(
					"css", fragmentJSONObject.getString("css")
				).put(
					"html",
					_fixImageEditables(fragmentJSONObject.getString("html"))
				).put(
					"js", fragmentJSONObject.getString("js")
				).put(
					"status", "Approved"
				);

				if (fragmentJSONObject.getBoolean("isNavigationMenu")) {
					approvedVersionJSONObject.put(
						"configuration", _NAV_MENU_CONFIGURATION);
				}

				fragmentItemsJSONArray.put(
					JSONUtil.put(
						"externalReferenceCode", fragmentKey
					).put(
						"fragmentSet",
						JSONUtil.put("externalReferenceCode", fragmentSetERC)
					).put(
						"fragmentVersions",
						JSONFactoryUtil.createJSONArray(
						).put(
							approvedVersionJSONObject
						)
					).put(
						"key", fragmentKey
					).put(
						"name", fragmentJSONObject.getString("name")
					).put(
						"type", "Component"
					));
			}
		}

		fragmentsBatchJSONObject.put("items", fragmentItemsJSONArray);

		return new BatchFile(
			"05-fragments.batch-engine-data.json", fragmentsBatchJSONObject);
	}

	private String _buildFragmentsCatalog(JSONArray customFragmentsJSONArray) {
		if ((customFragmentsJSONArray == null) ||
			(customFragmentsJSONArray.length() == 0)) {

			return "[]";
		}

		JSONArray catalogJSONArray = JSONFactoryUtil.createJSONArray();

		for (int i = 0; i < customFragmentsJSONArray.length(); i++) {
			JSONObject fragmentJSONObject =
				customFragmentsJSONArray.getJSONObject(i);

			JSONObject catalogEntryJSONObject = JSONUtil.put(
				"css", fragmentJSONObject.getString("css")
			).put(
				"editables", fragmentJSONObject.getJSONArray("editables")
			).put(
				"externalReferenceCode", fragmentJSONObject.getString("key")
			).put(
				"html", fragmentJSONObject.getString("html")
			).put(
				"js", fragmentJSONObject.getString("js")
			);

			if (fragmentJSONObject.getBoolean("isNavigationMenu")) {
				catalogEntryJSONObject.put(
					"configuration", _NAV_MENU_CONFIGURATION);
			}

			catalogJSONArray.put(catalogEntryJSONObject);
		}

		return catalogJSONArray.toString();
	}

	private BatchFile _buildFragmentSetBatchFile(
			String siteERC, String siteTitle)
		throws Exception {

		String fragmentSetERC = siteERC + "-fragments";

		JSONObject fragmentSetBatchJSONObject = _createBatchWrapper(
			"com.liferay.headless.admin.fragment.dto.v1_0.FragmentSet", true,
			siteERC, false);

		fragmentSetBatchJSONObject.put(
			"items",
			JSONFactoryUtil.createJSONArray(
			).put(
				JSONUtil.put(
					"externalReferenceCode", fragmentSetERC
				).put(
					"key", fragmentSetERC
				).put(
					"name", siteTitle + " Fragments"
				)
			));

		return new BatchFile(
			"04-fragment-set.batch-engine-data.json",
			fragmentSetBatchJSONObject);
	}

	private JSONObject _buildPageBody(
			String pageERC, String pageTitle, JSONArray pageElementsJSONArray)
		throws Exception {

		// Approved spec

		JSONObject approvedExperienceJSONObject = JSONUtil.put(
			"externalReferenceCode", pageERC + "-default"
		).put(
			"key", "DEFAULT"
		).put(
			"name_i18n", _createI18nJSON("en-US", "Default")
		).put(
			"pageElements", pageElementsJSONArray
		).put(
			"priority", 0
		);

		JSONObject approvedSpecJSONObject = JSONUtil.put(
			"draftContentPageSpecificationExternalReferenceCode",
			pageERC + "-draft"
		).put(
			"externalReferenceCode", pageERC
		).put(
			"pageExperiences",
			JSONFactoryUtil.createJSONArray(
			).put(
				approvedExperienceJSONObject
			)
		).put(
			"settings", _createThemeSettings()
		).put(
			"status", "Approved"
		).put(
			"type", "ContentPageSpecification"
		);

		// Draft spec

		JSONArray draftPageElementsJSONArray =
			JSONFactoryUtil.createJSONArray();

		if (pageElementsJSONArray.length() > 0) {
			draftPageElementsJSONArray = JSONFactoryUtil.createJSONArray(
				pageElementsJSONArray.toString());

			_appendDraftSuffix(draftPageElementsJSONArray);
		}

		JSONObject draftSpecJSONObject = JSONUtil.put(
			"externalReferenceCode", pageERC + "-draft"
		).put(
			"pageExperiences",
			JSONFactoryUtil.createJSONArray(
			).put(
				JSONUtil.put(
					"externalReferenceCode", pageERC + "-draft-default"
				).put(
					"key", "DEFAULT"
				).put(
					"name_i18n", _createI18nJSON("en-US", "Default")
				).put(
					"pageElements", draftPageElementsJSONArray
				).put(
					"priority", 0
				)
			)
		).put(
			"settings", _createThemeSettings()
		).put(
			"status", "Draft"
		).put(
			"type", "ContentPageSpecification"
		);

		// Page body

		return JSONUtil.put(
			"externalReferenceCode", pageERC
		).put(
			"name_i18n", _createI18nJSON("en-US", pageTitle)
		).put(
			"pageSettings", JSONUtil.put("type", "ContentPageSettings")
		).put(
			"pageSpecifications",
			JSONFactoryUtil.createJSONArray(
			).put(
				approvedSpecJSONObject
			).put(
				draftSpecJSONObject
			)
		).put(
			"type", "ContentPage"
		);
	}

	private BatchFile _buildPagesBatchFile(
			JSONObject planJSONObject, String siteERC)
		throws Exception {

		JSONArray pagesJSONArray = planJSONObject.getJSONArray("pages");

		JSONObject pagesBatchJSONObject = _createBatchWrapper(
			"com.liferay.headless.admin.site.dto.v1_0.SitePage", true, siteERC,
			true);

		JSONArray pageItemsJSONArray = JSONFactoryUtil.createJSONArray();

		if ((pagesJSONArray != null) && (pagesJSONArray.length() > 0)) {
			String fragmentsCatalog = _buildFragmentsCatalog(
				planJSONObject.getJSONArray("customFragments"));

			IRToPageSpecConverter irToPageSpecConverter =
				new IRToPageSpecConverter(fragmentsCatalog);

			for (int i = 0; i < pagesJSONArray.length(); i++) {
				JSONObject pageJSONObject = pagesJSONArray.getJSONObject(i);

				String pageERC = pageJSONObject.getString(
					"externalReferenceCode");
				String pageTitle = pageJSONObject.getString("title");

				JSONObject irJSONObject = pageJSONObject.getJSONObject("ir");

				JSONArray pageElementsJSONArray =
					JSONFactoryUtil.createJSONArray();

				if (irJSONObject != null) {
					String approvedSpec = irToPageSpecConverter.convert(
						irJSONObject.toString(), pageERC, pageERC + "-default",
						"en-US");

					if (!approvedSpec.startsWith("Error")) {
						JSONObject specJSONObject =
							JSONFactoryUtil.createJSONObject(approvedSpec);

						JSONArray experiencesJSONArray =
							specJSONObject.getJSONArray("pageExperiences");

						if ((experiencesJSONArray != null) &&
							(experiencesJSONArray.length() > 0)) {

							JSONObject experienceJSONObject =
								experiencesJSONArray.getJSONObject(0);

							pageElementsJSONArray =
								experienceJSONObject.getJSONArray(
									"pageElements");
						}
					}
				}

				pageItemsJSONArray.put(
					_buildPageBody(pageERC, pageTitle, pageElementsJSONArray));
			}
		}

		pagesBatchJSONObject.put("items", pageItemsJSONArray);

		return new BatchFile(
			"06-pages.batch-engine-data.json", pagesBatchJSONObject);
	}

	private BatchFile _buildSiteBatchFile(
			String siteERC, JSONObject siteJSONObject, String siteTitle)
		throws Exception {

		JSONObject siteBatchJSONObject = _createBatchWrapper(
			"com.liferay.headless.admin.site.dto.v1_0.Site", false, siteERC,
			false);

		siteBatchJSONObject.put(
			"items",
			JSONFactoryUtil.createJSONArray(
			).put(
				JSONUtil.put(
					"active", true
				).put(
					"description", siteJSONObject.getString("description")
				).put(
					"externalReferenceCode", siteERC
				).put(
					"membershipType", "open"
				).put(
					"name", siteTitle
				)
			));

		return new BatchFile(
			"01-site.batch-engine-data.json", siteBatchJSONObject);
	}

	private JSONObject _createBatchWrapper(
		String className, boolean includeSiteERC, String siteERC,
		boolean includePrivateLayout) {

		JSONObject parametersJSONObject = JSONUtil.put(
			"containsHeaders", "true"
		).put(
			"createStrategy", "UPSERT"
		).put(
			"featureFlag", "LPD-39244"
		).put(
			"importStrategy", "ON_ERROR_FAIL"
		);

		if (includeSiteERC) {
			parametersJSONObject.put("siteExternalReferenceCode", siteERC);
		}

		if (includePrivateLayout) {
			parametersJSONObject.put("privateLayout", "false");
		}

		return JSONUtil.put(
			"configuration",
			JSONUtil.put(
				"className", className
			).put(
				"multiCompany", true
			).put(
				"parameters", parametersJSONObject
			).put(
				"taskItemDelegateName", "DEFAULT"
			));
	}

	private JSONObject _createI18nJSON(String locale, String value) {
		return JSONUtil.put(locale, value);
	}

	private JSONObject _createThemeSettings() {
		return JSONUtil.put(
			"colorSchemeName", "01"
		).put(
			"themeName", "classic_WAR_classictheme"
		).put(
			"themeSettings",
			JSONUtil.put(
				"lfr-theme:regular:show-footer", "false"
			).put(
				"lfr-theme:regular:show-header", "false"
			).put(
				"lfr-theme:regular:show-header-search", "false"
			).put(
				"lfr-theme:regular:wrap-widget-page-content", "false"
			)
		);
	}

	private String _fixImageEditables(String html) {
		if (html == null) {
			return html;
		}

		// Find image editable elements that are not <img> tags and don't
		// contain <img> tags. Replace the opening tag with an <img> tag and
		// drop the now-orphaned closing tag so the result is valid HTML.
		// Previously only the opening tag was rewritten, producing markup such
		// as "<img ...></div>".

		Matcher matcher = _imageEditableTagPattern.matcher(html);

		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {
			String tagName = matcher.group(1);

			String editableId = matcher.group(2);

			String afterAttributes = matcher.group(3);

			String replacement = StringBundler.concat(
				"<img data-lfr-editable-id=\"", editableId,
				"\" data-lfr-editable-type=\"image\"", afterAttributes,
				" alt=\"\" src=\"\">");

			matcher.appendReplacement(
				sb, Matcher.quoteReplacement(replacement));

			// Remove the matching closing tag for the original element so the
			// rewritten <img> is not left with a dangling closing tag.

			Pattern closingTagPattern = Pattern.compile(
				StringBundler.concat("</", Pattern.quote(tagName), "\\s*>"));

			Matcher closingTagMatcher = closingTagPattern.matcher(sb);

			int lastStart = -1;
			int lastEnd = -1;

			while (closingTagMatcher.find()) {
				lastStart = closingTagMatcher.start();
				lastEnd = closingTagMatcher.end();
			}

			if (lastStart != -1) {
				sb.delete(lastStart, lastEnd);
			}
		}

		matcher.appendTail(sb);

		return sb.toString();
	}

	private void _sanitizeBlogKeywords(JSONArray blogJSONArray) {

		// Asset tag validation rejects ~26 special characters (see
		// AssetTagLocalServiceImpl). LLM-emitted keywords with characters like
		// '&', '/', or "'" would otherwise fail the entire batch under
		// ON_ERROR_FAIL. Strip invalid characters; drop keywords that go blank.

		for (int i = 0; i < blogJSONArray.length(); i++) {
			JSONObject blogJSONObject = blogJSONArray.getJSONObject(i);

			if (blogJSONObject == null) {
				continue;
			}

			JSONArray keywordsJSONArray = blogJSONObject.getJSONArray(
				"keywords");

			if (keywordsJSONArray == null) {
				continue;
			}

			JSONArray sanitizedJSONArray = JSONFactoryUtil.createJSONArray();

			for (int j = 0; j < keywordsJSONArray.length(); j++) {
				String keyword = keywordsJSONArray.getString(j);

				if (Validator.isNull(keyword)) {
					continue;
				}

				StringBuilder sb = new StringBuilder(keyword.length());

				for (int k = 0; k < keyword.length(); k++) {
					char c = keyword.charAt(k);

					if (_INVALID_ASSET_TAG_CHARS.indexOf(c) < 0) {
						sb.append(c);
					}
				}

				String cleaned = sb.toString(
				).trim();

				if (!cleaned.isEmpty()) {
					sanitizedJSONArray.put(cleaned);
				}
			}

			blogJSONObject.put("keywords", sanitizedJSONArray);
		}
	}

	private static final String _INVALID_ASSET_TAG_CHARS =
		"&'@\\]}:,=>/<\n[{%|+#`?\"\r;*~";

	private static final String _NAV_MENU_CONFIGURATION =
		"{\"fieldSets\":[{\"fields\":[{\"name\":\"source\"," +
			"\"label\":\"source\",\"type\":\"navigationMenuSelector\"}]}]}";

	private static final Log _log = LogFactoryUtil.getLog(
		SitePlanBatchFileBuilder.class);

	private static final Pattern _fileNameLanguagePattern = Pattern.compile(
		"-([a-z]{2})(?:[-_][A-Z]{2})?\\.json$");
	private static final Pattern _i18nBlockPattern = Pattern.compile(
		"\"[a-zA-Z]+_i18n\"\\s*:\\s*\\{([^{}]*)\\}");
	private static final Pattern _imageEditableTagPattern = Pattern.compile(
		"<(?!img)(\\w+)\\s+[^>]*?data-lfr-editable-id=\"([^\"]+)\"\\s+" +
			"data-lfr-editable-type=\"image\"([^>]*?)>");
	private static final Pattern _localeKeyPattern = Pattern.compile(
		"\"([a-z]{2})(?:_[A-Z]{2})?\"\\s*:");
	private static final Set<String> _metadataKeys = Set.of(
		"actions", "classNameId", "classPK", "createDate", "creator",
		"dateCreated", "dateModified", "externalReferenceCode", "groupId", "id",
		"modifiedDate", "parentExternalReferenceCode", "priority", "siteId",
		"sortOrder", "status", "userId");

	private final String _blogEntries;
	private final String _enrichedSitePlan;

}
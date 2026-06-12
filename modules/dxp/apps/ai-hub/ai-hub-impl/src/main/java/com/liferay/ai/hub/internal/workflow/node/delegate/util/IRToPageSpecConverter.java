/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.workflow.node.delegate.util;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Mario Gomes
 * @author Iliyan Peychev
 */
public class IRToPageSpecConverter {

	public IRToPageSpecConverter(String fullFragmentsCatalog) {
		JSONArray catalogJSONArray = null;

		if (Validator.isNotNull(fullFragmentsCatalog)) {
			try {
				catalogJSONArray = JSONFactoryUtil.createJSONArray(
					fullFragmentsCatalog);
			}
			catch (Exception exception) {
				if (_log.isDebugEnabled()) {
					_log.debug(exception);
				}

				catalogJSONArray = null;
			}
		}

		_customEditableTypesMap = _parseCustomEditableTypes(catalogJSONArray);
		_customFragmentSourcesMap = _parseCustomFragmentSources(
			catalogJSONArray);
	}

	public String convert(
		String irJSON, String draftERC, String experienceERC, String locale) {

		if ((locale == null) || locale.isEmpty()) {
			locale = "en-US";
		}

		try {
			JSONObject irJSONObject = JSONFactoryUtil.createJSONObject(
				_stripMarkdownFences(irJSON));

			JSONArray irElementsJSONArray = irJSONObject.getJSONArray(
				"elements");

			if (irElementsJSONArray == null) {
				irElementsJSONArray = JSONFactoryUtil.createJSONArray();
			}

			JSONArray pageElementsJSONArray = JSONFactoryUtil.createJSONArray();

			for (int i = 0; i < irElementsJSONArray.length(); i++) {
				JSONObject elementJSONObject = _convertElement(
					irElementsJSONArray.getJSONObject(i), null, i, locale);

				if (elementJSONObject != null) {
					pageElementsJSONArray.put(elementJSONObject);
				}
			}

			return JSONUtil.put(
				"customFields", JSONFactoryUtil.createJSONArray()
			).put(
				"externalReferenceCode", draftERC
			).put(
				"pageExperiences",
				JSONUtil.put(
					JSONUtil.put(
						"externalReferenceCode", experienceERC
					).put(
						"key", "DEFAULT"
					).put(
						"name_i18n", _i18n(locale, "Default")
					).put(
						"pageElements", pageElementsJSONArray
					).put(
						"priority", 0
					))
			).put(
				"status", "Draft"
			).put(
				"type", "ContentPageSpecification"
			).toString();
		}
		catch (Exception exception) {
			return "Error converting IR to page spec: " +
				exception.getMessage();
		}
	}

	private void _applySpacing(
			JSONObject sourceJSONObject, JSONObject targetJSONObject,
			String prefix, String defaultValue)
		throws Exception {

		JSONObject spacingJSONObject = sourceJSONObject.getJSONObject(prefix);

		for (String side : _SIDES) {
			String value = null;

			if (spacingJSONObject != null) {
				value = spacingJSONObject.getString(
					StringUtil.toLowerCase(side));
			}

			if ((value == null) || value.isEmpty()) {
				value = defaultValue;
			}

			if (value != null) {
				targetJSONObject.put(prefix + side, value);
			}
		}
	}

	private void _applyStyleProperties(
		JSONObject sourceJSONObject, JSONObject styleJSONObject) {

		for (String colorKey : _colorStyleKeys) {
			String value = sourceJSONObject.getString(colorKey);

			if (Validator.isNotNull(value)) {
				styleJSONObject.put(colorKey, _normalizeColor(value));
			}
		}

		for (String key : _stringStyleKeys) {
			String value = sourceJSONObject.getString(key);

			if (Validator.isNotNull(value)) {
				styleJSONObject.put(key, value);
			}
		}

		if (sourceJSONObject.has("hidden")) {
			styleJSONObject.put(
				"hidden", sourceJSONObject.getBoolean("hidden"));
		}
	}

	private JSONObject _buildContainerLayout(JSONObject irJSONObject)
		throws Exception {

		JSONObject layoutJSONObject = JSONUtil.put(
			"align", "Center"
		).put(
			"contentDisplay", "Block"
		).put(
			"justify", "Center"
		).put(
			"widthType", "Fluid"
		);

		String contentDisplay = irJSONObject.getString("contentDisplay");

		if (Objects.equals(contentDisplay, "flex-row")) {
			layoutJSONObject.put("contentDisplay", "FlexRow");
		}
		else if (Objects.equals(contentDisplay, "flex-column")) {
			layoutJSONObject.put("contentDisplay", "FlexColumn");
		}

		String widthType = irJSONObject.getString("widthType");

		if (Objects.equals(widthType, "fixed")) {
			layoutJSONObject.put("widthType", "Fixed");
		}

		return layoutJSONObject;
	}

	private JSONObject _buildEditable(
			String id, String type, Object raw, String locale)
		throws Exception {

		if (Objects.equals(type, "text")) {
			return _buildTextEditable(id, _toString(raw), locale);
		}
		else if (Objects.equals(type, "richText")) {
			return _buildRichTextEditable(id, _toString(raw), locale);
		}
		else if (Objects.equals(type, "image")) {
			return _buildImageEditable(id, raw, locale);
		}
		else if (Objects.equals(type, "link")) {
			return _buildLinkEditable(id, raw, locale);
		}

		return null;
	}

	private JSONArray _buildEditableElements(
			JSONObject contentJSONObject, String fragmentKey, String locale)
		throws Exception {

		JSONArray elementsJSONArray = JSONFactoryUtil.createJSONArray();

		Map<String, String> editableTypesMap = _ootbEditableTypes.get(
			fragmentKey);

		if (editableTypesMap == null) {
			editableTypesMap = _customEditableTypesMap.get(fragmentKey);
		}

		Iterator<String> keysIterator = contentJSONObject.keys();

		while (keysIterator.hasNext()) {
			String id = keysIterator.next();

			Object raw = contentJSONObject.get(id);

			String editableType = "text";

			if (editableTypesMap != null) {
				String mapped = editableTypesMap.get(id);

				if (mapped != null) {
					editableType = mapped;
				}
			}

			JSONObject editableJSONObject = _buildEditable(
				id, editableType, raw, locale);

			if (editableJSONObject != null) {
				elementsJSONArray.put(editableJSONObject);
			}
		}

		return elementsJSONArray;
	}

	private JSONObject _buildImageEditable(String id, Object raw, String locale)
		throws Exception {

		String url;

		if (raw instanceof JSONObject) {
			JSONObject rawJSONObject = (JSONObject)raw;

			url = rawJSONObject.getString("url");
		}
		else {
			url = (raw == null) ? StringPool.BLANK : String.valueOf(raw);
		}

		return JSONUtil.put(
			"fragmentEditableElementValue",
			JSONUtil.put(
				"fragmentImage",
				JSONUtil.put(
					"fragmentImageValue",
					JSONUtil.put(
						"type", "Direct"
					).put(
						"value_i18n",
						_i18nObject(
							locale,
							JSONUtil.put(
								"type", "URL"
							).put(
								"url", url
							))
					))
			).put(
				"type", "Image"
			)
		).put(
			"id", id
		);
	}

	private JSONObject _buildLinkEditable(String id, Object raw, String locale)
		throws Exception {

		String text = "Learn more";
		String url = "#";
		String target = "Self";

		if (raw instanceof JSONObject) {
			JSONObject linkJSONObject = (JSONObject)raw;

			text = linkJSONObject.getString("text", "Learn more");
			url = linkJSONObject.getString("url", "#");

			String rawTarget = linkJSONObject.getString("target");

			if ((rawTarget != null) && !rawTarget.isEmpty()) {
				target = _normalizeLinkTarget(rawTarget);
			}
		}
		else if (raw instanceof String) {
			url = (String)raw;
		}

		return JSONUtil.put(
			"fragmentEditableElementValue",
			JSONUtil.put(
				"fragmentLinkTextValue",
				JSONUtil.put(
					"fragmentEditableElementValueFragmentLink",
					JSONUtil.put(
						"fragmentLink",
						JSONUtil.put(
							"target", target
						).put(
							"value",
							JSONUtil.put(
								"type", "FragmentInlineValue"
							).put(
								"value_i18n", _i18n(locale, url)
							)
						))
				).put(
					"textFragmentValue",
					JSONUtil.put(
						"fragmentInlineValue",
						JSONUtil.put("value_i18n", _i18n(locale, text))
					).put(
						"type", "Inline"
					)
				)
			).put(
				"type", "Text"
			)
		).put(
			"id", id
		);
	}

	private JSONObject _buildRichTextEditable(
			String id, String value, String locale)
		throws Exception {

		return JSONUtil.put(
			"fragmentEditableElementValue",
			JSONUtil.put(
				"htmlFragmentValue",
				JSONUtil.put(
					"fragmentInlineValue",
					JSONUtil.put("value_i18n", _i18n(locale, value))
				).put(
					"type", "Inline"
				)
			).put(
				"type", "RichText"
			)
		).put(
			"id", id
		);
	}

	private JSONObject _buildTextEditable(
			String id, String value, String locale)
		throws Exception {

		return JSONUtil.put(
			"fragmentEditableElementValue",
			JSONUtil.put(
				"fragmentLinkTextValue",
				JSONUtil.put(
					"textFragmentValue",
					JSONUtil.put(
						"fragmentInlineValue",
						JSONUtil.put("value_i18n", _i18n(locale, value))
					).put(
						"type", "Inline"
					))
			).put(
				"type", "Text"
			)
		).put(
			"id", id
		);
	}

	private JSONObject _buildViewportStyle(JSONObject irJSONObject)
		throws Exception {

		JSONObject styleJSONObject = JSONFactoryUtil.createJSONObject();

		_applySpacing(irJSONObject, styleJSONObject, "padding", "5");
		_applySpacing(irJSONObject, styleJSONObject, "margin", null);
		_applyStyleProperties(irJSONObject, styleJSONObject);

		JSONObject irStyleJSONObject = irJSONObject.getJSONObject("style");

		if (irStyleJSONObject != null) {
			_applySpacing(irStyleJSONObject, styleJSONObject, "padding", null);
			_applySpacing(irStyleJSONObject, styleJSONObject, "margin", null);
			_applyStyleProperties(irStyleJSONObject, styleJSONObject);
		}

		return styleJSONObject;
	}

	private JSONArray _convertChildren(
			JSONArray childrenJSONArray, String parentERC, String locale)
		throws Exception {

		JSONArray resultJSONArray = JSONFactoryUtil.createJSONArray();

		if (childrenJSONArray == null) {
			return resultJSONArray;
		}

		for (int i = 0; i < childrenJSONArray.length(); i++) {
			JSONObject convertedJSONObject = _convertElement(
				childrenJSONArray.getJSONObject(i), parentERC, i, locale);

			if (convertedJSONObject != null) {
				resultJSONArray.put(convertedJSONObject);
			}
		}

		return resultJSONArray;
	}

	private JSONObject _convertContainer(
			JSONObject irJSONObject, String parentERC, int position,
			String locale)
		throws Exception {

		String erc = irJSONObject.getString("erc");

		JSONObject definitionJSONObject = JSONUtil.put(
			"fragmentViewports",
			JSONUtil.put(
				JSONUtil.put(
					"fragmentViewportStyle", _buildViewportStyle(irJSONObject)
				).put(
					"id", "Desktop"
				))
		).put(
			"layout", _buildContainerLayout(irJSONObject)
		).put(
			"type", "Container"
		);

		JSONArray childrenJSONArray = _convertChildren(
			irJSONObject.getJSONArray("children"), erc, locale);

		JSONObject nodeJSONObject = JSONUtil.put(
			"externalReferenceCode", erc
		).put(
			"pageElementDefinition", definitionJSONObject
		).put(
			"pageElements", childrenJSONArray
		).put(
			"position", position
		);

		if (parentERC != null) {
			nodeJSONObject.put("parentExternalReferenceCode", parentERC);
		}

		return nodeJSONObject;
	}

	private JSONObject _convertElement(
			JSONObject irJSONObject, String parentERC, int position,
			String locale)
		throws Exception {

		String type = irJSONObject.getString("type");

		if (Objects.equals(type, "container")) {
			return _convertContainer(irJSONObject, parentERC, position, locale);
		}
		else if (Objects.equals(type, "grid")) {
			return _convertGrid(irJSONObject, parentERC, position, locale);
		}
		else if (Objects.equals(type, "fragment")) {
			return _convertFragment(irJSONObject, parentERC, position, locale);
		}

		return null;
	}

	private JSONObject _convertFragment(
			JSONObject irJSONObject, String parentERC, int position,
			String locale)
		throws Exception {

		String erc = irJSONObject.getString("erc");

		JSONObject fragmentReferenceJSONObject =
			JSONFactoryUtil.createJSONObject();

		String source = irJSONObject.getString("source");

		if (Objects.equals(source, "ootb")) {
			String key = irJSONObject.getString("key");

			String fragmentKey = _ootbNameToKey.get(key);

			if (fragmentKey == null) {
				fragmentKey = key;
			}

			fragmentReferenceJSONObject.put(
				"defaultFragmentKey", fragmentKey
			).put(
				"fragmentReferenceType", "DefaultFragmentReference"
			);
		}
		else if (Objects.equals(source, "custom")) {
			fragmentReferenceJSONObject.put(
				"externalReferenceCode", irJSONObject.getString("key")
			).put(
				"fragmentReferenceType", "FragmentItemExternalReference"
			);
		}

		JSONObject instanceJSONObject = JSONUtil.put(
			"fragmentInstanceExternalReferenceCode", erc + "-inst"
		).put(
			"fragmentReference", fragmentReferenceJSONObject
		).put(
			"indexed", true
		);

		JSONObject contentJSONObject = irJSONObject.getJSONObject("content");

		if (contentJSONObject != null) {
			String fragmentKey = null;

			if (Objects.equals(source, "ootb")) {
				String key = irJSONObject.getString("key");

				fragmentKey = _ootbNameToKey.get(key);

				if (fragmentKey == null) {
					fragmentKey = key;
				}
			}
			else if (Objects.equals(source, "custom")) {
				fragmentKey = irJSONObject.getString("key");
			}

			JSONArray editableElementsJSONArray = _buildEditableElements(
				contentJSONObject, fragmentKey, locale);

			if (editableElementsJSONArray.length() > 0) {
				instanceJSONObject.put(
					"fragmentEditableElements", editableElementsJSONArray);
			}
		}

		JSONObject irStyleJSONObject = irJSONObject.getJSONObject("style");

		if (irStyleJSONObject != null) {
			instanceJSONObject.put(
				"fragmentViewports",
				JSONUtil.put(
					JSONUtil.put(
						"fragmentViewportStyle",
						_buildViewportStyle(irJSONObject)
					).put(
						"id", "Desktop"
					)));
		}

		if (Objects.equals(source, "custom")) {
			String fragmentKey = irJSONObject.getString("key");

			JSONObject fragmentSourcesJSONObject =
				_customFragmentSourcesMap.get(fragmentKey);

			if (fragmentSourcesJSONObject != null) {
				String configuration = fragmentSourcesJSONObject.getString(
					"configuration");

				if (Validator.isNotNull(configuration)) {
					instanceJSONObject.put("configuration", configuration);
				}

				String css = fragmentSourcesJSONObject.getString("css");

				if (Validator.isNotNull(css)) {
					instanceJSONObject.put("css", css);
				}

				String html = fragmentSourcesJSONObject.getString("html");

				if (Validator.isNotNull(html)) {
					instanceJSONObject.put("html", html);
				}

				String js = fragmentSourcesJSONObject.getString("js");

				if (Validator.isNotNull(js)) {
					instanceJSONObject.put("js", js);
				}
			}
		}

		JSONObject nodeJSONObject = JSONUtil.put(
			"externalReferenceCode", erc
		).put(
			"pageElementDefinition",
			JSONUtil.put(
				"fragmentInstance", instanceJSONObject
			).put(
				"type", "BasicFragment"
			)
		).put(
			"pageElements", JSONFactoryUtil.createJSONArray()
		).put(
			"position", position
		);

		if (parentERC != null) {
			nodeJSONObject.put("parentExternalReferenceCode", parentERC);
		}

		return nodeJSONObject;
	}

	private JSONObject _convertGrid(
			JSONObject irJSONObject, String parentERC, int position,
			String locale)
		throws Exception {

		String erc = irJSONObject.getString("erc");

		JSONArray columnsJSONArray = irJSONObject.getJSONArray("columns");

		if (columnsJSONArray == null) {
			columnsJSONArray = JSONFactoryUtil.createJSONArray();
		}

		int columnCount = columnsJSONArray.length();

		boolean gutters = irJSONObject.getBoolean("gutters", true);

		JSONObject modulesPerRowJSONObject = irJSONObject.getJSONObject(
			"modulesPerRow");

		int desktopPerRow = columnCount;
		int tabletPerRow = 0;
		int mobilePerRow = 0;

		if (modulesPerRowJSONObject != null) {
			desktopPerRow = modulesPerRowJSONObject.getInt(
				"desktop", columnCount);
			tabletPerRow = modulesPerRowJSONObject.getInt("tablet", 0);
			mobilePerRow = modulesPerRowJSONObject.getInt("mobile", 0);
		}

		JSONArray gridViewportsJSONArray = JSONUtil.put(
			JSONUtil.put(
				"gridViewportDefinition",
				JSONUtil.put(
					"modulesPerRow", desktopPerRow
				).put(
					"verticalAlignment", "Top"
				)
			).put(
				"id", "Desktop"
			));

		if (tabletPerRow > 0) {
			gridViewportsJSONArray.put(
				JSONUtil.put(
					"gridViewportDefinition",
					JSONUtil.put(
						"modulesPerRow", tabletPerRow
					).put(
						"verticalAlignment", "Top"
					)
				).put(
					"id", "Tablet"
				));
		}

		if (mobilePerRow > 0) {
			gridViewportsJSONArray.put(
				JSONUtil.put(
					"gridViewportDefinition",
					JSONUtil.put(
						"modulesPerRow", mobilePerRow
					).put(
						"verticalAlignment", "Top"
					)
				).put(
					"id", "PortraitMobile"
				));
		}

		JSONObject definitionJSONObject = JSONUtil.put(
			"gridViewports", gridViewportsJSONArray
		).put(
			"gutters", gutters
		).put(
			"numberOfModules", columnCount
		).put(
			"type", "Grid"
		);

		JSONArray moduleElementsJSONArray = JSONFactoryUtil.createJSONArray();

		for (int i = 0; i < columnCount; i++) {
			JSONObject columnJSONObject = columnsJSONArray.getJSONObject(i);

			moduleElementsJSONArray.put(
				_convertModule(columnJSONObject, erc, i, columnCount, locale));
		}

		JSONObject nodeJSONObject = JSONUtil.put(
			"externalReferenceCode", erc
		).put(
			"pageElementDefinition", definitionJSONObject
		).put(
			"pageElements", moduleElementsJSONArray
		).put(
			"position", position
		);

		if (parentERC != null) {
			nodeJSONObject.put("parentExternalReferenceCode", parentERC);
		}

		return nodeJSONObject;
	}

	private JSONObject _convertModule(
			JSONObject columnJSONObject, String gridERC, int position,
			int columnCount, String locale)
		throws Exception {

		int defaultSize = (columnCount > 0) ? (12 / columnCount) : 6;

		int size = columnJSONObject.getInt("size", defaultSize);

		String colERC = StringBundler.concat(
			"mod-", gridERC, "-", position + 1);

		JSONObject definitionJSONObject = JSONUtil.put(
			"moduleViewports",
			JSONUtil.put(
				JSONUtil.put(
					"id", "Desktop"
				).put(
					"moduleViewportDefinition", JSONUtil.put("size", size)
				))
		).put(
			"type", "Module"
		);

		JSONArray childrenJSONArray = _convertChildren(
			columnJSONObject.getJSONArray("children"), colERC, locale);

		return JSONUtil.put(
			"externalReferenceCode", colERC
		).put(
			"pageElementDefinition", definitionJSONObject
		).put(
			"pageElements", childrenJSONArray
		).put(
			"parentExternalReferenceCode", gridERC
		).put(
			"position", position
		);
	}

	private JSONObject _i18n(String locale, String value) throws Exception {
		return JSONUtil.put(locale, value);
	}

	private JSONObject _i18nObject(String locale, JSONObject valueJSONObject)
		throws Exception {

		return JSONUtil.put(locale, valueJSONObject);
	}

	private String _normalizeColor(String color) {
		if ((color != null) && !color.endsWith("Color")) {
			return color + "Color";
		}

		return color;
	}

	private String _normalizeLinkTarget(String target) {
		if (target == null) {
			return "Self";
		}

		String cleaned = StringUtil.toLowerCase(target.replaceFirst("^_", ""));

		if (Objects.equals(cleaned, "blank")) {
			return "Blank";
		}
		else if (Objects.equals(cleaned, "parent")) {
			return "Parent";
		}
		else if (Objects.equals(cleaned, "top")) {
			return "Top";
		}

		return "Self";
	}

	private Map<String, Map<String, String>> _parseCustomEditableTypes(
		JSONArray catalogJSONArray) {

		Map<String, Map<String, String>> resultMap = new HashMap<>();

		if (catalogJSONArray == null) {
			return resultMap;
		}

		try {
			for (int i = 0; i < catalogJSONArray.length(); i++) {
				JSONObject fragmentJSONObject = catalogJSONArray.getJSONObject(
					i);

				JSONArray editablesJSONArray = fragmentJSONObject.getJSONArray(
					"editables");

				if ((editablesJSONArray == null) ||
					(editablesJSONArray.length() == 0)) {

					continue;
				}

				String erc = fragmentJSONObject.getString(
					"externalReferenceCode");

				Map<String, String> editableMap = new HashMap<>();

				for (int j = 0; j < editablesJSONArray.length(); j++) {
					JSONObject editableJSONObject =
						editablesJSONArray.getJSONObject(j);

					editableMap.put(
						editableJSONObject.getString("id"),
						editableJSONObject.getString("type"));
				}

				resultMap.put(erc, editableMap);
			}
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}
		}

		return resultMap;
	}

	private Map<String, JSONObject> _parseCustomFragmentSources(
		JSONArray catalogJSONArray) {

		Map<String, JSONObject> resultMap = new HashMap<>();

		if (catalogJSONArray == null) {
			return resultMap;
		}

		try {
			for (int i = 0; i < catalogJSONArray.length(); i++) {
				JSONObject fragmentJSONObject = catalogJSONArray.getJSONObject(
					i);

				resultMap.put(
					fragmentJSONObject.getString("externalReferenceCode"),
					JSONUtil.put(
						"configuration",
						fragmentJSONObject.getString("configuration")
					).put(
						"css", fragmentJSONObject.getString("css")
					).put(
						"html", fragmentJSONObject.getString("html")
					).put(
						"js", fragmentJSONObject.getString("js")
					));
			}
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}
		}

		return resultMap;
	}

	private String _stripMarkdownFences(String input) {
		if (input == null) {
			return input;
		}

		input = input.trim();

		if (input.startsWith("```")) {
			int firstNewline = input.indexOf('\n');

			if (firstNewline > 0) {
				input = input.substring(firstNewline + 1);
			}
			else {
				input = input.substring(3);
			}
		}

		if (input.endsWith("```")) {
			input = input.substring(0, input.length() - 3);
		}

		return input.trim();
	}

	private String _toString(Object value) {
		if (value instanceof String) {
			return (String)value;
		}

		if (value instanceof JSONObject) {
			JSONObject valueJSONObject = (JSONObject)value;

			if (valueJSONObject.has("text")) {
				return valueJSONObject.getString("text");
			}

			if (valueJSONObject.has("value")) {
				return valueJSONObject.getString("value");
			}

			if (valueJSONObject.has("content")) {
				return valueJSONObject.getString("content");
			}
		}

		if (value == null) {
			return StringPool.BLANK;
		}

		return String.valueOf(value);
	}

	private static final String[] _SIDES = {"Top", "Bottom", "Left", "Right"};

	private static final Log _log = LogFactoryUtil.getLog(
		IRToPageSpecConverter.class);

	private static final Set<String> _colorStyleKeys = Set.of(
		"backgroundColor", "borderColor", "textColor");
	private static final Map<String, Map<String, String>> _ootbEditableTypes =
		Map.of(
			"BASIC_COMPONENT-heading", Map.of("element-text", "text"),
			"BASIC_COMPONENT-paragraph", Map.of("element-text", "richText"),
			"BASIC_COMPONENT-card",
			Map.of(
				"01-img", "image", "02-title", "richText", "03-content",
				"richText", "04-link", "link"),
			"BASIC_COMPONENT-image", Map.of("image-square", "image"),
			"BASIC_COMPONENT-button",
			Map.of("element-text", "text", "element-link", "link"),
			"BASIC_COMPONENT-video", Map.of("element-video", "link"));
	private static final Map<String, String> _ootbNameToKey = Map.of(
		"button", "BASIC_COMPONENT-button", "card", "BASIC_COMPONENT-card",
		"heading", "BASIC_COMPONENT-heading", "image", "BASIC_COMPONENT-image",
		"paragraph", "BASIC_COMPONENT-paragraph", "separator",
		"BASIC_COMPONENT-separator", "spacer", "BASIC_COMPONENT-spacer",
		"video", "BASIC_COMPONENT-video");
	private static final Set<String> _stringStyleKeys = Set.of(
		"borderRadius", "borderWidth", "fontFamily", "fontSize", "fontWeight",
		"height", "maxHeight", "maxWidth", "minHeight", "minWidth", "opacity",
		"overflow", "shadow", "textAlign", "width");

	private final Map<String, Map<String, String>> _customEditableTypesMap;
	private final Map<String, JSONObject> _customFragmentSourcesMap;

}
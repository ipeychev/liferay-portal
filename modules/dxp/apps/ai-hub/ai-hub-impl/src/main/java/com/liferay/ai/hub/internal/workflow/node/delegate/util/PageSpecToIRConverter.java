/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.workflow.node.delegate.util;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * @author Mario Gomes
 * @author Iliyan Peychev
 */
public class PageSpecToIRConverter {

	public String convert(String pageSpecJSON, String title, String locale) {
		if ((locale == null) || locale.isEmpty()) {
			locale = "en-US";
		}

		try {
			JSONObject specJSONObject = _unwrapSpec(
				JSONFactoryUtil.createJSONObject(
					_stripMarkdownFences(pageSpecJSON)));

			String erc = specJSONObject.getString("externalReferenceCode");

			JSONArray pageExperiencesJSONArray = specJSONObject.getJSONArray(
				"pageExperiences");

			JSONArray pageElementsJSONArray = _resolvePageElements(
				pageExperiencesJSONArray);

			JSONArray elementsJSONArray = JSONFactoryUtil.createJSONArray();

			for (int i = 0; i < pageElementsJSONArray.length(); i++) {
				JSONObject convertedJSONObject = _convertElement(
					pageElementsJSONArray.getJSONObject(i), locale);

				if (convertedJSONObject != null) {
					elementsJSONArray.put(convertedJSONObject);
				}
			}

			return JSONUtil.put(
				"elements", elementsJSONArray
			).put(
				"erc", erc
			).put(
				"locale", locale
			).put(
				"title", title
			).toString();
		}
		catch (Exception exception) {
			return "Error converting page spec to IR: " +
				exception.getMessage();
		}
	}

	private void _addSpacingValue(
		JSONObject vpStyleJSONObject, JSONObject spacingJSONObject,
		String prefix) {

		for (String side : _SIDES) {
			String value = vpStyleJSONObject.getString(prefix + side);

			if ((value != null) && !value.isEmpty()) {
				spacingJSONObject.put(
					StringUtil.toLowerCase(side), _parseIntSafe(value));
			}
		}
	}

	private void _applySpacingFromStyle(
			JSONObject vpStyleJSONObject, JSONObject targetJSONObject,
			String prefix)
		throws Exception {

		String top = vpStyleJSONObject.getString(prefix + "Top");
		String bottom = vpStyleJSONObject.getString(prefix + "Bottom");
		String left = vpStyleJSONObject.getString(prefix + "Left");
		String right = vpStyleJSONObject.getString(prefix + "Right");

		boolean hasValue = false;

		if (((top != null) && !top.isEmpty()) ||
			((bottom != null) && !bottom.isEmpty()) ||
			((left != null) && !left.isEmpty()) ||
			((right != null) && !right.isEmpty())) {

			hasValue = true;
		}

		if (!hasValue) {
			return;
		}

		JSONObject spacingJSONObject = JSONFactoryUtil.createJSONObject();

		if ((top != null) && !top.isEmpty()) {
			spacingJSONObject.put("top", _parseIntSafe(top));
		}

		if ((bottom != null) && !bottom.isEmpty()) {
			spacingJSONObject.put("bottom", _parseIntSafe(bottom));
		}

		if ((left != null) && !left.isEmpty()) {
			spacingJSONObject.put("left", _parseIntSafe(left));
		}

		if ((right != null) && !right.isEmpty()) {
			spacingJSONObject.put("right", _parseIntSafe(right));
		}

		targetJSONObject.put(prefix, spacingJSONObject);
	}

	private JSONArray _convertChildren(
			JSONObject elementJSONObject, String locale)
		throws Exception {

		JSONArray childrenJSONArray = JSONFactoryUtil.createJSONArray();

		JSONArray pageElementsJSONArray = elementJSONObject.getJSONArray(
			"pageElements");

		if (pageElementsJSONArray == null) {
			return childrenJSONArray;
		}

		for (int i = 0; i < pageElementsJSONArray.length(); i++) {
			JSONObject convertedJSONObject = _convertElement(
				pageElementsJSONArray.getJSONObject(i), locale);

			if (convertedJSONObject != null) {
				childrenJSONArray.put(convertedJSONObject);
			}
		}

		return childrenJSONArray;
	}

	private JSONObject _convertColumn(
			JSONObject elementJSONObject, String locale)
		throws Exception {

		JSONObject definitionJSONObject = elementJSONObject.getJSONObject(
			"pageElementDefinition");

		JSONArray moduleViewportsJSONArray = definitionJSONObject.getJSONArray(
			"moduleViewports");

		int size = 6;

		if (moduleViewportsJSONArray != null) {
			for (int i = 0; i < moduleViewportsJSONArray.length(); i++) {
				JSONObject viewportJSONObject =
					moduleViewportsJSONArray.getJSONObject(i);

				if (Objects.equals(
						viewportJSONObject.getString("id"), "Desktop")) {

					JSONObject viewportDefJSONObject =
						viewportJSONObject.getJSONObject(
							"moduleViewportDefinition");

					if (viewportDefJSONObject != null) {
						size = viewportDefJSONObject.getInt("size", 6);
					}
				}
			}
		}

		JSONObject columnJSONObject = JSONUtil.put("size", size);

		JSONArray childrenJSONArray = _convertChildren(
			elementJSONObject, locale);

		if (childrenJSONArray.length() > 0) {
			columnJSONObject.put("children", childrenJSONArray);
		}

		return columnJSONObject;
	}

	private JSONObject _convertContainer(
			JSONObject elementJSONObject, JSONObject definitionJSONObject,
			String locale)
		throws Exception {

		JSONObject containerJSONObject = JSONUtil.put(
			"erc", elementJSONObject.getString("externalReferenceCode")
		).put(
			"type", "container"
		);

		JSONObject layoutJSONObject = definitionJSONObject.getJSONObject(
			"layout");

		if (layoutJSONObject != null) {
			String contentDisplay = layoutJSONObject.getString(
				"contentDisplay");

			if (Objects.equals(contentDisplay, "FlexRow")) {
				containerJSONObject.put("contentDisplay", "flex-row");
			}
			else if (Objects.equals(contentDisplay, "FlexColumn")) {
				containerJSONObject.put("contentDisplay", "flex-column");
			}

			String widthType = layoutJSONObject.getString("widthType");

			if (Objects.equals(widthType, "Fixed")) {
				containerJSONObject.put("widthType", "fixed");
			}
		}

		_extractViewportStyle(definitionJSONObject, containerJSONObject);

		JSONArray childrenJSONArray = _convertChildren(
			elementJSONObject, locale);

		if (childrenJSONArray.length() > 0) {
			containerJSONObject.put("children", childrenJSONArray);
		}

		return containerJSONObject;
	}

	private JSONObject _convertElement(
			JSONObject elementJSONObject, String locale)
		throws Exception {

		JSONObject definitionJSONObject = elementJSONObject.getJSONObject(
			"pageElementDefinition");

		if (definitionJSONObject == null) {
			return null;
		}

		String type = definitionJSONObject.getString("type");

		if (Objects.equals(type, "Container")) {
			return _convertContainer(
				elementJSONObject, definitionJSONObject, locale);
		}
		else if (Objects.equals(type, "Grid")) {
			return _convertGrid(
				elementJSONObject, definitionJSONObject, locale);
		}
		else if (Objects.equals(type, "BasicFragment")) {
			return _convertFragment(
				elementJSONObject, definitionJSONObject, locale);
		}

		return null;
	}

	private JSONObject _convertFragment(
			JSONObject elementJSONObject, JSONObject definitionJSONObject,
			String locale)
		throws Exception {

		JSONObject fragmentJSONObject = JSONUtil.put(
			"erc", elementJSONObject.getString("externalReferenceCode")
		).put(
			"type", "fragment"
		);

		JSONObject instanceJSONObject = definitionJSONObject.getJSONObject(
			"fragmentInstance");

		if (instanceJSONObject == null) {
			return fragmentJSONObject;
		}

		JSONObject refJSONObject = instanceJSONObject.getJSONObject(
			"fragmentReference");

		if (refJSONObject != null) {
			String refType = refJSONObject.getString("fragmentReferenceType");

			if (Objects.equals(refType, "DefaultFragmentReference")) {
				fragmentJSONObject.put("source", "ootb");

				String fragmentKey = refJSONObject.getString(
					"defaultFragmentKey");

				String friendlyName = _ootbKeyToName.get(fragmentKey);

				fragmentJSONObject.put(
					"key", (friendlyName != null) ? friendlyName : fragmentKey);
			}
			else if (Objects.equals(refType, "FragmentItemExternalReference")) {
				fragmentJSONObject.put(
					"key", refJSONObject.getString("externalReferenceCode")
				).put(
					"source", "custom"
				);
			}
		}

		JSONArray editableElementsJSONArray = instanceJSONObject.getJSONArray(
			"fragmentEditableElements");

		if ((editableElementsJSONArray != null) &&
			(editableElementsJSONArray.length() > 0)) {

			JSONObject contentJSONObject = JSONFactoryUtil.createJSONObject();

			for (int i = 0; i < editableElementsJSONArray.length(); i++) {
				JSONObject editableJSONObject =
					editableElementsJSONArray.getJSONObject(i);

				String id = editableJSONObject.getString("id");

				Object value = _extractEditableValue(
					editableJSONObject, locale);

				if (value != null) {
					contentJSONObject.put(id, value);
				}
			}

			if (contentJSONObject.length() > 0) {
				fragmentJSONObject.put("content", contentJSONObject);
			}
		}

		_extractFragmentStyle(instanceJSONObject, fragmentJSONObject);

		return fragmentJSONObject;
	}

	private JSONObject _convertGrid(
			JSONObject elementJSONObject, JSONObject definitionJSONObject,
			String locale)
		throws Exception {

		JSONObject gridJSONObject = JSONUtil.put(
			"erc", elementJSONObject.getString("externalReferenceCode")
		).put(
			"type", "grid"
		);

		boolean gutters = definitionJSONObject.getBoolean("gutters", true);

		if (!gutters) {
			gridJSONObject.put("gutters", false);
		}

		JSONArray gridViewportsJSONArray = definitionJSONObject.getJSONArray(
			"gridViewports");

		if (gridViewportsJSONArray != null) {
			JSONObject modulesPerRowJSONObject =
				JSONFactoryUtil.createJSONObject();

			for (int i = 0; i < gridViewportsJSONArray.length(); i++) {
				JSONObject viewportJSONObject =
					gridViewportsJSONArray.getJSONObject(i);

				JSONObject vpDefJSONObject = viewportJSONObject.getJSONObject(
					"gridViewportDefinition");

				if (vpDefJSONObject == null) {
					continue;
				}

				int perRow = vpDefJSONObject.getInt("modulesPerRow", 0);

				if (perRow <= 0) {
					continue;
				}

				String vpId = viewportJSONObject.getString("id");

				if (Objects.equals(vpId, "Desktop")) {
					modulesPerRowJSONObject.put("desktop", perRow);
				}
				else if (Objects.equals(vpId, "Tablet")) {
					modulesPerRowJSONObject.put("tablet", perRow);
				}
				else if (Objects.equals(vpId, "PortraitMobile")) {
					modulesPerRowJSONObject.put("mobile", perRow);
				}
			}

			if (modulesPerRowJSONObject.length() > 0) {
				gridJSONObject.put("modulesPerRow", modulesPerRowJSONObject);
			}
		}

		JSONArray pageElementsJSONArray = elementJSONObject.getJSONArray(
			"pageElements");

		JSONArray columnsJSONArray = JSONFactoryUtil.createJSONArray();

		if (pageElementsJSONArray != null) {
			for (int i = 0; i < pageElementsJSONArray.length(); i++) {
				JSONObject moduleElementJSONObject =
					pageElementsJSONArray.getJSONObject(i);

				JSONObject moduleDefJSONObject =
					moduleElementJSONObject.getJSONObject(
						"pageElementDefinition");

				if ((moduleDefJSONObject != null) &&
					Objects.equals(
						moduleDefJSONObject.getString("type"), "Module")) {

					columnsJSONArray.put(
						_convertColumn(moduleElementJSONObject, locale));
				}
			}
		}

		gridJSONObject.put("columns", columnsJSONArray);

		return gridJSONObject;
	}

	private String _denormalizeColor(String color) {
		if ((color != null) && color.endsWith("Color")) {
			return color.substring(0, color.length() - 5);
		}

		return color;
	}

	private Object _extractEditableValue(
			JSONObject editableJSONObject, String locale)
		throws Exception {

		JSONObject elementValueJSONObject = editableJSONObject.getJSONObject(
			"fragmentEditableElementValue");

		if (elementValueJSONObject == null) {
			return null;
		}

		String type = elementValueJSONObject.getString("type");

		if (Objects.equals(type, "Text")) {
			return _extractTextValue(elementValueJSONObject, locale);
		}
		else if (Objects.equals(type, "RichText")) {
			return _extractRichTextValue(elementValueJSONObject, locale);
		}
		else if (Objects.equals(type, "Image")) {
			return _extractImageValue(elementValueJSONObject, locale);
		}

		return null;
	}

	private void _extractFragmentStyle(
			JSONObject instanceJSONObject, JSONObject fragmentJSONObject)
		throws Exception {

		JSONArray viewportsJSONArray = instanceJSONObject.getJSONArray(
			"fragmentViewports");

		if (viewportsJSONArray == null) {
			return;
		}

		for (int i = 0; i < viewportsJSONArray.length(); i++) {
			JSONObject viewportJSONObject = viewportsJSONArray.getJSONObject(i);

			if (!Objects.equals(
					viewportJSONObject.getString("id"), "Desktop")) {

				continue;
			}

			JSONObject vpStyleJSONObject = viewportJSONObject.getJSONObject(
				"fragmentViewportStyle");

			if (vpStyleJSONObject == null) {
				continue;
			}

			JSONObject styleJSONObject = _parseViewportStyle(vpStyleJSONObject);

			if (styleJSONObject.length() > 0) {
				fragmentJSONObject.put("style", styleJSONObject);
			}
		}
	}

	private Object _extractImageValue(
			JSONObject elementValueJSONObject, String locale)
		throws Exception {

		JSONObject fragmentImageJSONObject =
			elementValueJSONObject.getJSONObject("fragmentImage");

		if (fragmentImageJSONObject == null) {
			return null;
		}

		JSONObject imageValueJSONObject = fragmentImageJSONObject.getJSONObject(
			"fragmentImageValue");

		if (imageValueJSONObject == null) {
			return null;
		}

		JSONObject i18nJSONObject = imageValueJSONObject.getJSONObject(
			"value_i18n");

		if (i18nJSONObject == null) {
			return null;
		}

		JSONObject localeValueJSONObject = i18nJSONObject.getJSONObject(locale);

		if (localeValueJSONObject == null) {
			Iterator<String> keysIterator = i18nJSONObject.keys();

			if (keysIterator.hasNext()) {
				localeValueJSONObject = i18nJSONObject.getJSONObject(
					keysIterator.next());
			}
		}

		if (localeValueJSONObject == null) {
			return null;
		}

		String url = localeValueJSONObject.getString("url");

		if ((url == null) || url.isEmpty()) {
			return null;
		}

		return JSONUtil.put("url", url);
	}

	private Object _extractRichTextValue(
			JSONObject elementValueJSONObject, String locale)
		throws Exception {

		JSONObject htmlValueJSONObject = elementValueJSONObject.getJSONObject(
			"htmlFragmentValue");

		if (htmlValueJSONObject == null) {
			return null;
		}

		JSONObject inlineValueJSONObject = htmlValueJSONObject.getJSONObject(
			"fragmentInlineValue");

		if (inlineValueJSONObject == null) {
			return null;
		}

		return _getI18nValue(inlineValueJSONObject, locale);
	}

	private Object _extractTextValue(
			JSONObject elementValueJSONObject, String locale)
		throws Exception {

		JSONObject linkTextValueJSONObject =
			elementValueJSONObject.getJSONObject("fragmentLinkTextValue");

		if (linkTextValueJSONObject == null) {
			return null;
		}

		JSONObject fragmentLinkJSONObject = null;

		JSONObject linkRefJSONObject = linkTextValueJSONObject.getJSONObject(
			"fragmentEditableElementValueFragmentLink");

		if (linkRefJSONObject != null) {
			fragmentLinkJSONObject = linkRefJSONObject.getJSONObject(
				"fragmentLink");
		}

		JSONObject textValueJSONObject = linkTextValueJSONObject.getJSONObject(
			"textFragmentValue");

		String text = null;

		if (textValueJSONObject != null) {
			JSONObject inlineValueJSONObject =
				textValueJSONObject.getJSONObject("fragmentInlineValue");

			if (inlineValueJSONObject != null) {
				text = _getI18nValue(inlineValueJSONObject, locale);
			}
		}

		if (fragmentLinkJSONObject == null) {
			return text;
		}

		JSONObject linkJSONObject = JSONFactoryUtil.createJSONObject();

		if (text != null) {
			linkJSONObject.put("text", text);
		}

		JSONObject linkValueJSONObject = fragmentLinkJSONObject.getJSONObject(
			"value");

		if (linkValueJSONObject != null) {
			JSONObject linkI18nJSONObject = linkValueJSONObject.getJSONObject(
				"value_i18n");

			if (linkI18nJSONObject != null) {
				String url = _getI18nValue(linkI18nJSONObject, locale);

				if (url != null) {
					linkJSONObject.put("url", url);
				}
			}
		}

		String target = fragmentLinkJSONObject.getString("target");

		if ((target != null) && !target.isEmpty() &&
			!Objects.equals(target, "Self")) {

			linkJSONObject.put("target", StringUtil.toLowerCase(target));
		}

		return linkJSONObject;
	}

	private void _extractViewportStyle(
			JSONObject definitionJSONObject, JSONObject containerJSONObject)
		throws Exception {

		JSONArray viewportsJSONArray = definitionJSONObject.getJSONArray(
			"fragmentViewports");

		if (viewportsJSONArray == null) {
			return;
		}

		for (int i = 0; i < viewportsJSONArray.length(); i++) {
			JSONObject viewportJSONObject = viewportsJSONArray.getJSONObject(i);

			if (!Objects.equals(
					viewportJSONObject.getString("id"), "Desktop")) {

				continue;
			}

			JSONObject vpStyleJSONObject = viewportJSONObject.getJSONObject(
				"fragmentViewportStyle");

			if (vpStyleJSONObject == null) {
				continue;
			}

			_applySpacingFromStyle(
				vpStyleJSONObject, containerJSONObject, "padding");
			_applySpacingFromStyle(
				vpStyleJSONObject, containerJSONObject, "margin");

			String backgroundColor = vpStyleJSONObject.getString(
				"backgroundColor");

			if ((backgroundColor != null) && !backgroundColor.isEmpty()) {
				containerJSONObject.put(
					"backgroundColor", _denormalizeColor(backgroundColor));
			}

			String textColor = vpStyleJSONObject.getString("textColor");

			if ((textColor != null) && !textColor.isEmpty()) {
				containerJSONObject.put(
					"textColor", _denormalizeColor(textColor));
			}

			String textAlign = vpStyleJSONObject.getString("textAlign");

			if ((textAlign != null) && !textAlign.isEmpty()) {
				containerJSONObject.put("textAlign", textAlign);
			}
		}
	}

	private String _getI18nValue(JSONObject i18nJSONObject, String locale) {
		String value = i18nJSONObject.getString(locale);

		if ((value != null) && !value.isEmpty()) {
			return value;
		}

		JSONObject valueI18nJSONObject = i18nJSONObject.getJSONObject(
			"value_i18n");

		if (valueI18nJSONObject != null) {
			value = valueI18nJSONObject.getString(locale);

			if ((value != null) && !value.isEmpty()) {
				return value;
			}

			Iterator<String> keysIterator = valueI18nJSONObject.keys();

			if (keysIterator.hasNext()) {
				return valueI18nJSONObject.getString(keysIterator.next());
			}
		}

		Iterator<String> keysIterator = i18nJSONObject.keys();

		if (keysIterator.hasNext()) {
			String key = keysIterator.next();

			if (!Objects.equals(key, "value_i18n")) {
				return i18nJSONObject.getString(key);
			}
		}

		return null;
	}

	private int _parseIntSafe(String value) {
		try {
			return Integer.parseInt(value);
		}
		catch (NumberFormatException numberFormatException) {
			if (_log.isDebugEnabled()) {
				_log.debug(numberFormatException);
			}

			return 0;
		}
	}

	private JSONObject _parseViewportStyle(JSONObject vpStyleJSONObject)
		throws Exception {

		JSONObject styleJSONObject = JSONFactoryUtil.createJSONObject();

		String backgroundColor = vpStyleJSONObject.getString("backgroundColor");

		if ((backgroundColor != null) && !backgroundColor.isEmpty()) {
			styleJSONObject.put(
				"backgroundColor", _denormalizeColor(backgroundColor));
		}

		String textColor = vpStyleJSONObject.getString("textColor");

		if ((textColor != null) && !textColor.isEmpty()) {
			styleJSONObject.put("textColor", _denormalizeColor(textColor));
		}

		String textAlign = vpStyleJSONObject.getString("textAlign");

		if ((textAlign != null) && !textAlign.isEmpty()) {
			styleJSONObject.put("textAlign", textAlign);
		}

		JSONObject paddingJSONObject = JSONFactoryUtil.createJSONObject();

		_addSpacingValue(vpStyleJSONObject, paddingJSONObject, "padding");

		if (paddingJSONObject.length() > 0) {
			styleJSONObject.put("padding", paddingJSONObject);
		}

		JSONObject marginJSONObject = JSONFactoryUtil.createJSONObject();

		_addSpacingValue(vpStyleJSONObject, marginJSONObject, "margin");

		if (marginJSONObject.length() > 0) {
			styleJSONObject.put("margin", marginJSONObject);
		}

		return styleJSONObject;
	}

	private JSONArray _resolvePageElements(JSONArray pageExperiencesJSONArray) {
		if (pageExperiencesJSONArray == null) {
			return JSONFactoryUtil.createJSONArray();
		}

		for (int i = 0; i < pageExperiencesJSONArray.length(); i++) {
			JSONObject experienceJSONObject =
				pageExperiencesJSONArray.getJSONObject(i);

			if (Objects.equals(
					experienceJSONObject.getString("key"), "DEFAULT")) {

				JSONArray elementsJSONArray = experienceJSONObject.getJSONArray(
					"pageElements");

				if (elementsJSONArray != null) {
					return elementsJSONArray;
				}
			}
		}

		if (pageExperiencesJSONArray.length() > 0) {
			JSONObject firstExperienceJSONObject =
				pageExperiencesJSONArray.getJSONObject(0);

			JSONArray elementsJSONArray =
				firstExperienceJSONObject.getJSONArray("pageElements");

			if (elementsJSONArray != null) {
				return elementsJSONArray;
			}
		}

		return JSONFactoryUtil.createJSONArray();
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

	private JSONObject _unwrapSpec(JSONObject jsonObject) {
		if (jsonObject.has("pageExperiences")) {
			return jsonObject;
		}

		if (jsonObject.has("result")) {
			Object result = jsonObject.get("result");

			if (result instanceof JSONObject) {
				return _unwrapSpec((JSONObject)result);
			}

			if (result instanceof String) {
				try {
					return _unwrapSpec(
						JSONFactoryUtil.createJSONObject((String)result));
				}
				catch (Exception exception) {
					if (_log.isDebugEnabled()) {
						_log.debug(exception);
					}
				}
			}
		}

		if (jsonObject.has("pageSpecifications")) {
			JSONArray specsJSONArray = jsonObject.getJSONArray(
				"pageSpecifications");

			if (specsJSONArray != null) {
				for (int i = 0; i < specsJSONArray.length(); i++) {
					JSONObject candidateJSONObject =
						specsJSONArray.getJSONObject(i);

					if (Objects.equals(
							candidateJSONObject.getString("status"), "Draft")) {

						return candidateJSONObject;
					}
				}

				if (specsJSONArray.length() > 0) {
					return specsJSONArray.getJSONObject(0);
				}
			}
		}

		for (String key : jsonObject.keySet()) {
			Object value = jsonObject.get(key);

			if (value instanceof JSONObject) {
				JSONObject nestedJSONObject = (JSONObject)value;

				if (nestedJSONObject.has("pageExperiences")) {
					return nestedJSONObject;
				}
			}
		}

		return jsonObject;
	}

	private static final String[] _SIDES = {"Top", "Bottom", "Left", "Right"};

	private static final Log _log = LogFactoryUtil.getLog(
		PageSpecToIRConverter.class);

	private static final Map<String, String> _ootbKeyToName = Map.of(
		"BASIC_COMPONENT-button", "button", "BASIC_COMPONENT-card", "card",
		"BASIC_COMPONENT-heading", "heading", "BASIC_COMPONENT-image", "image",
		"BASIC_COMPONENT-paragraph", "paragraph", "BASIC_COMPONENT-separator",
		"separator", "BASIC_COMPONENT-spacer", "spacer",
		"BASIC_COMPONENT-video", "video");

}
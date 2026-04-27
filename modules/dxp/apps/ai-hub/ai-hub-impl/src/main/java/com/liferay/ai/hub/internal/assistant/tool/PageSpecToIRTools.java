/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.assistant.tool;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Mahmoud Tayem
 */
public class PageSpecToIRTools {

	@Tool("Convert a Liferay ContentPageSpecification JSON into an IR object")
	public String convertToIR(
		@P("Full ContentPageSpecification JSON payload") String pageSpecJSON,
		@P("Page title") String title,
		@P("Locale code, e.g. en-US") String locale) {

		if ((locale == null) || locale.isEmpty()) {
			locale = "en-US";
		}

		try {
			JSONObject spec = JSONFactoryUtil.createJSONObject(
				_stripMarkdownFences(pageSpecJSON));

			spec = _unwrapSpec(spec);

			String erc = spec.getString("externalReferenceCode");

			JSONArray pageExperiences = spec.getJSONArray("pageExperiences");

			JSONArray pageElements = _resolvePageElements(pageExperiences);

			JSONObject ir = JSONFactoryUtil.createJSONObject();

			ir.put("title", title);
			ir.put("erc", erc);
			ir.put("locale", locale);

			JSONArray elements = JSONFactoryUtil.createJSONArray();

			for (int i = 0; i < pageElements.length(); i++) {
				JSONObject converted = _convertElement(
					pageElements.getJSONObject(i), locale);

				if (converted != null) {
					elements.put(converted);
				}
			}

			ir.put("elements", elements);

			return ir.toString();
		}
		catch (Exception exception) {
			return "Error converting page spec to IR: " +
				exception.getMessage();
		}
	}

	private JSONObject _convertContainer(
			JSONObject element, JSONObject definition, String locale)
		throws Exception {

		JSONObject container = JSONFactoryUtil.createJSONObject();

		container.put(
			"erc", element.getString("externalReferenceCode"));
		container.put("type", "container");

		JSONObject layout = definition.getJSONObject("layout");

		if (layout != null) {
			String contentDisplay = layout.getString("contentDisplay");

			if ("FlexRow".equals(contentDisplay)) {
				container.put("contentDisplay", "flex-row");
			}
			else if ("FlexColumn".equals(contentDisplay)) {
				container.put("contentDisplay", "flex-column");
			}

			String widthType = layout.getString("widthType");

			if ("Fixed".equals(widthType)) {
				container.put("widthType", "fixed");
			}
		}

		_extractViewportStyle(definition, container);

		JSONArray children = _convertChildren(element, locale);

		if (children.length() > 0) {
			container.put("children", children);
		}

		return container;
	}

	private JSONArray _convertChildren(JSONObject element, String locale)
		throws Exception {

		JSONArray children = JSONFactoryUtil.createJSONArray();

		JSONArray pageElements = element.getJSONArray("pageElements");

		if (pageElements == null) {
			return children;
		}

		for (int i = 0; i < pageElements.length(); i++) {
			JSONObject converted = _convertElement(
				pageElements.getJSONObject(i), locale);

			if (converted != null) {
				children.put(converted);
			}
		}

		return children;
	}

	private JSONObject _convertColumn(JSONObject element, String locale)
		throws Exception {

		JSONObject column = JSONFactoryUtil.createJSONObject();

		JSONObject definition = element.getJSONObject(
			"pageElementDefinition");

		JSONArray moduleViewports = definition.getJSONArray("moduleViewports");

		int size = 6;

		if (moduleViewports != null) {
			for (int i = 0; i < moduleViewports.length(); i++) {
				JSONObject viewport = moduleViewports.getJSONObject(i);

				if ("Desktop".equals(viewport.getString("id"))) {
					JSONObject viewportDef = viewport.getJSONObject(
						"moduleViewportDefinition");

					if (viewportDef != null) {
						size = viewportDef.getInt("size", 6);
					}
				}
			}
		}

		column.put("size", size);

		JSONArray children = _convertChildren(element, locale);

		if (children.length() > 0) {
			column.put("children", children);
		}

		return column;
	}

	private JSONObject _convertElement(JSONObject element, String locale)
		throws Exception {

		JSONObject definition = element.getJSONObject(
			"pageElementDefinition");

		if (definition == null) {
			return null;
		}

		String type = definition.getString("type");

		switch (type) {
			case "Container":
				return _convertContainer(element, definition, locale);
			case "Grid":
				return _convertGrid(element, definition, locale);
			case "BasicFragment":
				return _convertFragment(element, definition, locale);
			default:
				return null;
		}
	}

	private JSONObject _convertFragment(
			JSONObject element, JSONObject definition, String locale)
		throws Exception {

		JSONObject fragment = JSONFactoryUtil.createJSONObject();

		fragment.put(
			"erc", element.getString("externalReferenceCode"));
		fragment.put("type", "fragment");

		JSONObject instance = definition.getJSONObject("fragmentInstance");

		if (instance == null) {
			return fragment;
		}

		JSONObject ref = instance.getJSONObject("fragmentReference");

		if (ref != null) {
			String refType = ref.getString("fragmentReferenceType");

			if ("DefaultFragmentReference".equals(refType)) {
				fragment.put("source", "ootb");

				String fragmentKey = ref.getString("defaultFragmentKey");

				String friendlyName = _OOTB_KEY_TO_NAME.get(fragmentKey);

				fragment.put("key", (friendlyName != null) ?
					friendlyName : fragmentKey);
			}
			else if ("FragmentItemExternalReference".equals(refType)) {
				fragment.put("source", "custom");
				fragment.put(
					"key", ref.getString("externalReferenceCode"));
			}
		}

		JSONArray editableElements = instance.getJSONArray(
			"fragmentEditableElements");

		if ((editableElements != null) && (editableElements.length() > 0)) {
			JSONObject content = JSONFactoryUtil.createJSONObject();

			for (int i = 0; i < editableElements.length(); i++) {
				JSONObject editable = editableElements.getJSONObject(i);

				String id = editable.getString("id");

				Object value = _extractEditableValue(editable, locale);

				if (value != null) {
					content.put(id, value);
				}
			}

			if (content.length() > 0) {
				fragment.put("content", content);
			}
		}

		_extractFragmentStyle(instance, fragment);

		return fragment;
	}

	private JSONObject _convertGrid(
			JSONObject element, JSONObject definition, String locale)
		throws Exception {

		JSONObject grid = JSONFactoryUtil.createJSONObject();

		grid.put("erc", element.getString("externalReferenceCode"));
		grid.put("type", "grid");

		boolean gutters = definition.getBoolean("gutters", true);

		if (!gutters) {
			grid.put("gutters", false);
		}

		JSONArray gridViewports = definition.getJSONArray("gridViewports");

		if (gridViewports != null) {
			JSONObject modulesPerRow = JSONFactoryUtil.createJSONObject();

			for (int i = 0; i < gridViewports.length(); i++) {
				JSONObject viewport = gridViewports.getJSONObject(i);

				String vpId = viewport.getString("id");
				JSONObject vpDef = viewport.getJSONObject(
					"gridViewportDefinition");

				if (vpDef == null) {
					continue;
				}

				int perRow = vpDef.getInt("modulesPerRow", 0);

				if (perRow <= 0) {
					continue;
				}

				if ("Desktop".equals(vpId)) {
					modulesPerRow.put("desktop", perRow);
				}
				else if ("Tablet".equals(vpId)) {
					modulesPerRow.put("tablet", perRow);
				}
				else if ("PortraitMobile".equals(vpId)) {
					modulesPerRow.put("mobile", perRow);
				}
			}

			if (modulesPerRow.length() > 0) {
				grid.put("modulesPerRow", modulesPerRow);
			}
		}

		JSONArray pageElements = element.getJSONArray("pageElements");
		JSONArray columns = JSONFactoryUtil.createJSONArray();

		if (pageElements != null) {
			for (int i = 0; i < pageElements.length(); i++) {
				JSONObject moduleElement = pageElements.getJSONObject(i);

				JSONObject moduleDef = moduleElement.getJSONObject(
					"pageElementDefinition");

				if ((moduleDef != null) &&
					"Module".equals(moduleDef.getString("type"))) {

					columns.put(_convertColumn(moduleElement, locale));
				}
			}
		}

		grid.put("columns", columns);

		return grid;
	}

	private Object _extractEditableValue(JSONObject editable, String locale)
		throws Exception {

		JSONObject elementValue = editable.getJSONObject(
			"fragmentEditableElementValue");

		if (elementValue == null) {
			return null;
		}

		String type = elementValue.getString("type");

		switch (type) {
			case "Text":
				return _extractTextValue(elementValue, locale);
			case "RichText":
				return _extractRichTextValue(elementValue, locale);
			case "Image":
				return _extractImageValue(elementValue, locale);
			default:
				return null;
		}
	}

	private void _extractFragmentStyle(
			JSONObject instance, JSONObject fragment)
		throws Exception {

		JSONArray viewports = instance.getJSONArray("fragmentViewports");

		if (viewports == null) {
			return;
		}

		for (int i = 0; i < viewports.length(); i++) {
			JSONObject viewport = viewports.getJSONObject(i);

			if (!"Desktop".equals(viewport.getString("id"))) {
				continue;
			}

			JSONObject vpStyle = viewport.getJSONObject(
				"fragmentViewportStyle");

			if (vpStyle == null) {
				continue;
			}

			JSONObject style = _parseViewportStyle(vpStyle);

			if (style.length() > 0) {
				fragment.put("style", style);
			}
		}
	}

	private Object _extractImageValue(JSONObject elementValue, String locale)
		throws Exception {

		JSONObject fragmentImage = elementValue.getJSONObject("fragmentImage");

		if (fragmentImage == null) {
			return null;
		}

		JSONObject imageValue = fragmentImage.getJSONObject(
			"fragmentImageValue");

		if (imageValue == null) {
			return null;
		}

		JSONObject i18n = imageValue.getJSONObject("value_i18n");

		if (i18n == null) {
			return null;
		}

		JSONObject localeValue = i18n.getJSONObject(locale);

		if (localeValue == null) {
			Iterator<String> keys = i18n.keys();

			if (keys.hasNext()) {
				localeValue = i18n.getJSONObject(keys.next());
			}
		}

		if (localeValue == null) {
			return null;
		}

		String url = localeValue.getString("url");

		if ((url == null) || url.isEmpty()) {
			return null;
		}

		JSONObject image = JSONFactoryUtil.createJSONObject();

		image.put("url", url);

		return image;
	}

	private Object _extractRichTextValue(
			JSONObject elementValue, String locale)
		throws Exception {

		JSONObject htmlValue = elementValue.getJSONObject(
			"htmlFragmentValue");

		if (htmlValue == null) {
			return null;
		}

		JSONObject inlineValue = htmlValue.getJSONObject(
			"fragmentInlineValue");

		if (inlineValue == null) {
			return null;
		}

		return _getI18nValue(inlineValue, locale);
	}

	private Object _extractTextValue(JSONObject elementValue, String locale)
		throws Exception {

		JSONObject linkTextValue = elementValue.getJSONObject(
			"fragmentLinkTextValue");

		if (linkTextValue == null) {
			return null;
		}

		JSONObject fragmentLink = null;

		JSONObject linkRef = linkTextValue.getJSONObject(
			"fragmentEditableElementValueFragmentLink");

		if (linkRef != null) {
			fragmentLink = linkRef.getJSONObject("fragmentLink");
		}

		JSONObject textValue = linkTextValue.getJSONObject(
			"textFragmentValue");

		String text = null;

		if (textValue != null) {
			JSONObject inlineValue = textValue.getJSONObject(
				"fragmentInlineValue");

			if (inlineValue != null) {
				text = _getI18nValue(inlineValue, locale);
			}
		}

		if (fragmentLink == null) {
			return text;
		}

		JSONObject link = JSONFactoryUtil.createJSONObject();

		if (text != null) {
			link.put("text", text);
		}

		JSONObject linkValue = fragmentLink.getJSONObject("value");

		if (linkValue != null) {
			JSONObject linkI18n = linkValue.getJSONObject("value_i18n");

			if (linkI18n != null) {
				String url = _getI18nValue(linkI18n, locale);

				if (url != null) {
					link.put("url", url);
				}
			}
		}

		String target = fragmentLink.getString("target");

		if ((target != null) && !target.isEmpty() &&
			!"Self".equals(target)) {

			link.put("target", target.toLowerCase());
		}

		return link;
	}

	private void _extractViewportStyle(
			JSONObject definition, JSONObject container)
		throws Exception {

		JSONArray viewports = definition.getJSONArray("fragmentViewports");

		if (viewports == null) {
			return;
		}

		for (int i = 0; i < viewports.length(); i++) {
			JSONObject viewport = viewports.getJSONObject(i);

			if (!"Desktop".equals(viewport.getString("id"))) {
				continue;
			}

			JSONObject vpStyle = viewport.getJSONObject(
				"fragmentViewportStyle");

			if (vpStyle == null) {
				continue;
			}

			_applySpacingFromStyle(vpStyle, container, "padding");
			_applySpacingFromStyle(vpStyle, container, "margin");

			String backgroundColor = vpStyle.getString("backgroundColor");

			if ((backgroundColor != null) && !backgroundColor.isEmpty()) {
				container.put(
					"backgroundColor", _denormalizeColor(backgroundColor));
			}

			String textColor = vpStyle.getString("textColor");

			if ((textColor != null) && !textColor.isEmpty()) {
				container.put("textColor", _denormalizeColor(textColor));
			}

			String textAlign = vpStyle.getString("textAlign");

			if ((textAlign != null) && !textAlign.isEmpty()) {
				container.put("textAlign", textAlign);
			}
		}
	}

	private String _getI18nValue(JSONObject i18nObject, String locale) {
		String value = i18nObject.getString(locale);

		if ((value != null) && !value.isEmpty()) {
			return value;
		}

		JSONObject i18n = i18nObject.getJSONObject("value_i18n");

		if (i18n != null) {
			value = i18n.getString(locale);

			if ((value != null) && !value.isEmpty()) {
				return value;
			}

			Iterator<String> keys = i18n.keys();

			if (keys.hasNext()) {
				return i18n.getString(keys.next());
			}
		}

		Iterator<String> keys = i18nObject.keys();

		if (keys.hasNext()) {
			String key = keys.next();

			if (!"value_i18n".equals(key)) {
				return i18nObject.getString(key);
			}
		}

		return null;
	}

	private void _applySpacingFromStyle(
			JSONObject vpStyle, JSONObject target, String prefix)
		throws Exception {

		String top = vpStyle.getString(prefix + "Top");
		String bottom = vpStyle.getString(prefix + "Bottom");
		String left = vpStyle.getString(prefix + "Left");
		String right = vpStyle.getString(prefix + "Right");

		boolean hasValue =
			((top != null) && !top.isEmpty()) ||
			((bottom != null) && !bottom.isEmpty()) ||
			((left != null) && !left.isEmpty()) ||
			((right != null) && !right.isEmpty());

		if (!hasValue) {
			return;
		}

		JSONObject spacing = JSONFactoryUtil.createJSONObject();

		if ((top != null) && !top.isEmpty()) {
			spacing.put("top", _parseIntSafe(top));
		}

		if ((bottom != null) && !bottom.isEmpty()) {
			spacing.put("bottom", _parseIntSafe(bottom));
		}

		if ((left != null) && !left.isEmpty()) {
			spacing.put("left", _parseIntSafe(left));
		}

		if ((right != null) && !right.isEmpty()) {
			spacing.put("right", _parseIntSafe(right));
		}

		target.put(prefix, spacing);
	}

	private String _denormalizeColor(String color) {
		if ((color != null) && color.endsWith("Color")) {
			return color.substring(0, color.length() - 5);
		}

		return color;
	}

	private int _parseIntSafe(String value) {
		try {
			return Integer.parseInt(value);
		}
		catch (NumberFormatException numberFormatException) {
			return 0;
		}
	}

	private JSONObject _parseViewportStyle(JSONObject vpStyle)
		throws Exception {

		JSONObject style = JSONFactoryUtil.createJSONObject();

		String backgroundColor = vpStyle.getString("backgroundColor");

		if ((backgroundColor != null) && !backgroundColor.isEmpty()) {
			style.put("backgroundColor", _denormalizeColor(backgroundColor));
		}

		String textColor = vpStyle.getString("textColor");

		if ((textColor != null) && !textColor.isEmpty()) {
			style.put("textColor", _denormalizeColor(textColor));
		}

		String textAlign = vpStyle.getString("textAlign");

		if ((textAlign != null) && !textAlign.isEmpty()) {
			style.put("textAlign", textAlign);
		}

		JSONObject padding = JSONFactoryUtil.createJSONObject();

		_addSpacingValue(vpStyle, padding, "padding");

		if (padding.length() > 0) {
			style.put("padding", padding);
		}

		JSONObject margin = JSONFactoryUtil.createJSONObject();

		_addSpacingValue(vpStyle, margin, "margin");

		if (margin.length() > 0) {
			style.put("margin", margin);
		}

		return style;
	}

	private void _addSpacingValue(
		JSONObject vpStyle, JSONObject spacing, String prefix) {

		String[] sides = {"Top", "Bottom", "Left", "Right"};

		for (String side : sides) {
			String value = vpStyle.getString(prefix + side);

			if ((value != null) && !value.isEmpty()) {
				spacing.put(side.toLowerCase(), _parseIntSafe(value));
			}
		}
	}

	private JSONArray _resolvePageElements(JSONArray pageExperiences) {
		if (pageExperiences == null) {
			return JSONFactoryUtil.createJSONArray();
		}

		for (int i = 0; i < pageExperiences.length(); i++) {
			JSONObject experience = pageExperiences.getJSONObject(i);

			if ("DEFAULT".equals(experience.getString("key"))) {
				JSONArray elements = experience.getJSONArray("pageElements");

				if (elements != null) {
					return elements;
				}
			}
		}

		if (pageExperiences.length() > 0) {
			JSONArray elements = pageExperiences.getJSONObject(
				0
			).getJSONArray("pageElements");

			if (elements != null) {
				return elements;
			}
		}

		return JSONFactoryUtil.createJSONArray();
	}

	private JSONObject _unwrapSpec(JSONObject obj) {
		if (obj.has("pageExperiences")) {
			return obj;
		}

		if (obj.has("result")) {
			Object result = obj.get("result");

			if (result instanceof JSONObject) {
				return _unwrapSpec((JSONObject)result);
			}

			if (result instanceof String) {
				try {
					return _unwrapSpec(
						JSONFactoryUtil.createJSONObject((String)result));
				}
				catch (Exception exception) {
				}
			}
		}

		if (obj.has("pageSpecifications")) {
			JSONArray specs = obj.getJSONArray("pageSpecifications");

			if (specs != null) {
				for (int i = 0; i < specs.length(); i++) {
					JSONObject candidate = specs.getJSONObject(i);

					if ("Draft".equals(candidate.getString("status"))) {
						return candidate;
					}
				}

				if (specs.length() > 0) {
					return specs.getJSONObject(0);
				}
			}
		}

		for (String key : obj.keySet()) {
			Object value = obj.get(key);

			if (value instanceof JSONObject) {
				JSONObject nested = (JSONObject)value;

				if (nested.has("pageExperiences")) {
					return nested;
				}
			}
		}

		return obj;
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

	private static final Map<String, String> _OOTB_KEY_TO_NAME =
		new HashMap<String, String>() {
			{
				put("BASIC_COMPONENT-heading", "heading");
				put("BASIC_COMPONENT-paragraph", "paragraph");
				put("BASIC_COMPONENT-card", "card");
				put("BASIC_COMPONENT-image", "image");
				put("BASIC_COMPONENT-button", "button");
				put("BASIC_COMPONENT-video", "video");
				put("BASIC_COMPONENT-spacer", "spacer");
				put("BASIC_COMPONENT-separator", "separator");
			}
		};

}

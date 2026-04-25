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
public class IRToPageSpecTools {

	@Tool("Convert an IR JSON object into a Liferay ContentPageSpecification JSON")
	public String convertToPageSpec(
		@P("IR JSON object") String irJSON,
		@P("Draft page specification ERC") String draftERC,
		@P("Draft page experience ERC") String experienceERC,
		@P("Locale code, e.g. en-US") String locale) {

		try {
			JSONObject ir = JSONFactoryUtil.createJSONObject(
				_stripMarkdownFences(irJSON));

			JSONArray irElements = ir.getJSONArray("elements");

			if (irElements == null) {
				irElements = JSONFactoryUtil.createJSONArray();
			}

			JSONArray pageElements = JSONFactoryUtil.createJSONArray();

			for (int i = 0; i < irElements.length(); i++) {
				JSONObject element = _convertElement(
					irElements.getJSONObject(i), null, i, locale);

				if (element != null) {
					pageElements.put(element);
				}
			}

			JSONObject experience = JSONFactoryUtil.createJSONObject();

			experience.put("externalReferenceCode", experienceERC);
			experience.put("key", "DEFAULT");
			experience.put(
				"name_i18n",
				_i18n(locale, "Default"));
			experience.put("pageElements", pageElements);
			experience.put("priority", 0);

			JSONArray pageExperiences = JSONFactoryUtil.createJSONArray();

			pageExperiences.put(experience);

			JSONObject spec = JSONFactoryUtil.createJSONObject();

			spec.put("externalReferenceCode", draftERC);
			spec.put("pageExperiences", pageExperiences);
			spec.put("status", "Draft");
			spec.put("type", "ContentPageSpecification");

			return spec.toString();
		}
		catch (Exception exception) {
			return "Error converting IR to page spec: " +
				exception.getMessage();
		}
	}

	private JSONObject _buildContainerLayout(JSONObject ir) throws Exception {
		JSONObject layout = JSONFactoryUtil.createJSONObject();

		layout.put("align", "Center");
		layout.put("justify", "Center");
		layout.put("widthType", "Fluid");
		layout.put("contentDisplay", "Block");

		String contentDisplay = ir.getString("contentDisplay");

		if ("flex-row".equals(contentDisplay)) {
			layout.put("contentDisplay", "FlexRow");
		}
		else if ("flex-column".equals(contentDisplay)) {
			layout.put("contentDisplay", "FlexColumn");
		}

		String widthType = ir.getString("widthType");

		if ("fixed".equals(widthType)) {
			layout.put("widthType", "Fixed");
		}

		return layout;
	}

	private JSONObject _buildViewportStyle(JSONObject ir) throws Exception {
		JSONObject style = JSONFactoryUtil.createJSONObject();

		_applySpacing(ir, style, "padding", "5");
		_applySpacing(ir, style, "margin", null);

		String backgroundColor = ir.getString("backgroundColor");

		if ((backgroundColor != null) && !backgroundColor.isEmpty()) {
			style.put("backgroundColor", _normalizeColor(backgroundColor));
		}

		String textColor = ir.getString("textColor");

		if ((textColor != null) && !textColor.isEmpty()) {
			style.put("textColor", _normalizeColor(textColor));
		}

		String textAlign = ir.getString("textAlign");

		if ((textAlign != null) && !textAlign.isEmpty()) {
			style.put("textAlign", textAlign);
		}

		JSONObject irStyle = ir.getJSONObject("style");

		if (irStyle != null) {
			_applySpacing(irStyle, style, "padding", null);
			_applySpacing(irStyle, style, "margin", null);

			backgroundColor = irStyle.getString("backgroundColor");

			if ((backgroundColor != null) && !backgroundColor.isEmpty()) {
				style.put(
					"backgroundColor", _normalizeColor(backgroundColor));
			}

			textColor = irStyle.getString("textColor");

			if ((textColor != null) && !textColor.isEmpty()) {
				style.put("textColor", _normalizeColor(textColor));
			}

			textAlign = irStyle.getString("textAlign");

			if ((textAlign != null) && !textAlign.isEmpty()) {
				style.put("textAlign", textAlign);
			}
		}

		return style;
	}

	private JSONObject _convertContainer(
			JSONObject ir, String parentERC, int position, String locale)
		throws Exception {

		String erc = ir.getString("erc");

		JSONObject layout = _buildContainerLayout(ir);

		JSONObject viewportStyle = _buildViewportStyle(ir);

		JSONObject definition = JSONFactoryUtil.createJSONObject();

		definition.put("indexed", true);
		definition.put("layout", layout);
		definition.put("type", "Container");

		JSONArray fragmentViewports = JSONFactoryUtil.createJSONArray();
		JSONObject desktop = JSONFactoryUtil.createJSONObject();

		desktop.put("fragmentViewportStyle", viewportStyle);
		desktop.put("id", "Desktop");

		fragmentViewports.put(desktop);

		definition.put("fragmentViewports", fragmentViewports);

		JSONArray children = _convertChildren(
			ir.getJSONArray("children"), erc, locale);

		JSONObject node = JSONFactoryUtil.createJSONObject();

		node.put("externalReferenceCode", erc);
		node.put("pageElementDefinition", definition);
		node.put("pageElements", children);
		node.put("position", position);

		if (parentERC != null) {
			node.put("parentExternalReferenceCode", parentERC);
		}

		return node;
	}

	private JSONObject _convertElement(
			JSONObject ir, String parentERC, int position, String locale)
		throws Exception {

		String type = ir.getString("type");

		switch (type) {
			case "container":
				return _convertContainer(ir, parentERC, position, locale);
			case "grid":
				return _convertGrid(ir, parentERC, position, locale);
			case "fragment":
				return _convertFragment(ir, parentERC, position, locale);
			default:
				return null;
		}
	}

	private JSONObject _convertFragment(
			JSONObject ir, String parentERC, int position, String locale)
		throws Exception {

		String erc = ir.getString("erc");

		JSONObject fragmentReference = JSONFactoryUtil.createJSONObject();

		String source = ir.getString("source");

		if ("ootb".equals(source)) {
			String key = ir.getString("key");
			String fragmentKey = _OOTB_NAME_TO_KEY.get(key);

			if (fragmentKey == null) {
				fragmentKey = key;
			}

			fragmentReference.put("defaultFragmentKey", fragmentKey);
			fragmentReference.put(
				"fragmentReferenceType", "DefaultFragmentReference");
		}
		else if ("custom".equals(source)) {
			fragmentReference.put(
				"externalReferenceCode", ir.getString("key"));
			fragmentReference.put(
				"fragmentReferenceType", "FragmentItemExternalReference");
		}

		JSONObject instance = JSONFactoryUtil.createJSONObject();

		instance.put(
			"fragmentInstanceExternalReferenceCode", erc + "-inst");
		instance.put("fragmentReference", fragmentReference);

		JSONObject content = ir.getJSONObject("content");

		if (content != null) {
			String fragmentKey = null;

			if ("ootb".equals(source)) {
				String key = ir.getString("key");

				fragmentKey = _OOTB_NAME_TO_KEY.get(key);

				if (fragmentKey == null) {
					fragmentKey = key;
				}
			}

			JSONArray editableElements = _buildEditableElements(
				content, fragmentKey, locale);

			if (editableElements.length() > 0) {
				instance.put("fragmentEditableElements", editableElements);
			}
		}

		JSONObject irStyle = ir.getJSONObject("style");

		if (irStyle != null) {
			JSONObject viewportStyle = _buildViewportStyle(ir);

			JSONArray fragmentViewports = JSONFactoryUtil.createJSONArray();
			JSONObject desktop = JSONFactoryUtil.createJSONObject();

			desktop.put("fragmentViewportStyle", viewportStyle);
			desktop.put("id", "Desktop");

			fragmentViewports.put(desktop);

			instance.put("fragmentViewports", fragmentViewports);
		}

		JSONObject definition = JSONFactoryUtil.createJSONObject();

		definition.put("fragmentInstance", instance);
		definition.put("type", "BasicFragment");

		JSONObject node = JSONFactoryUtil.createJSONObject();

		node.put("externalReferenceCode", erc);
		node.put("pageElementDefinition", definition);
		node.put("pageElements", JSONFactoryUtil.createJSONArray());
		node.put("position", position);

		if (parentERC != null) {
			node.put("parentExternalReferenceCode", parentERC);
		}

		return node;
	}

	private JSONObject _convertGrid(
			JSONObject ir, String parentERC, int position, String locale)
		throws Exception {

		String erc = ir.getString("erc");

		JSONArray columns = ir.getJSONArray("columns");

		if (columns == null) {
			columns = JSONFactoryUtil.createJSONArray();
		}

		int columnCount = columns.length();

		boolean gutters = ir.getBoolean("gutters", true);

		JSONObject modulesPerRow = ir.getJSONObject("modulesPerRow");

		int desktopPerRow = columnCount;
		int tabletPerRow = 0;
		int mobilePerRow = 0;

		if (modulesPerRow != null) {
			desktopPerRow = modulesPerRow.getInt("desktop", columnCount);
			tabletPerRow = modulesPerRow.getInt("tablet", 0);
			mobilePerRow = modulesPerRow.getInt("mobile", 0);
		}

		JSONArray gridViewports = JSONFactoryUtil.createJSONArray();

		JSONObject desktopVP = JSONFactoryUtil.createJSONObject();
		JSONObject desktopDef = JSONFactoryUtil.createJSONObject();

		desktopDef.put("modulesPerRow", desktopPerRow);
		desktopDef.put("verticalAlignment", "Top");

		desktopVP.put("gridViewportDefinition", desktopDef);
		desktopVP.put("id", "Desktop");

		gridViewports.put(desktopVP);

		if (tabletPerRow > 0) {
			JSONObject tabletVP = JSONFactoryUtil.createJSONObject();
			JSONObject tabletDef = JSONFactoryUtil.createJSONObject();

			tabletDef.put("modulesPerRow", tabletPerRow);
			tabletDef.put("verticalAlignment", "Top");

			tabletVP.put("gridViewportDefinition", tabletDef);
			tabletVP.put("id", "Tablet");

			gridViewports.put(tabletVP);
		}

		if (mobilePerRow > 0) {
			JSONObject mobileVP = JSONFactoryUtil.createJSONObject();
			JSONObject mobileDef = JSONFactoryUtil.createJSONObject();

			mobileDef.put("modulesPerRow", mobilePerRow);
			mobileDef.put("verticalAlignment", "Top");

			mobileVP.put("gridViewportDefinition", mobileDef);
			mobileVP.put("id", "PortraitMobile");

			gridViewports.put(mobileVP);
		}

		JSONObject definition = JSONFactoryUtil.createJSONObject();

		definition.put("gridViewports", gridViewports);
		definition.put("gutters", gutters);
		definition.put("indexed", true);
		definition.put("numberOfModules", columnCount);
		definition.put("type", "Grid");

		JSONArray moduleElements = JSONFactoryUtil.createJSONArray();

		for (int i = 0; i < columnCount; i++) {
			JSONObject col = columns.getJSONObject(i);

			moduleElements.put(
				_convertModule(col, erc, i, locale));
		}

		JSONObject node = JSONFactoryUtil.createJSONObject();

		node.put("externalReferenceCode", erc);
		node.put("pageElementDefinition", definition);
		node.put("pageElements", moduleElements);
		node.put("position", position);

		if (parentERC != null) {
			node.put("parentExternalReferenceCode", parentERC);
		}

		return node;
	}

	private JSONObject _convertModule(
			JSONObject col, String gridERC, int position, String locale)
		throws Exception {

		int size = col.getInt("size", 6);

		String colERC = gridERC + "-col-" + (position + 1);

		JSONObject viewportDef = JSONFactoryUtil.createJSONObject();

		viewportDef.put("size", size);

		JSONObject desktopVP = JSONFactoryUtil.createJSONObject();

		desktopVP.put("id", "Desktop");
		desktopVP.put("moduleViewportDefinition", viewportDef);

		JSONArray moduleViewports = JSONFactoryUtil.createJSONArray();

		moduleViewports.put(desktopVP);

		JSONObject definition = JSONFactoryUtil.createJSONObject();

		definition.put("indexed", true);
		definition.put("moduleViewports", moduleViewports);
		definition.put("type", "Module");

		JSONArray children = _convertChildren(
			col.getJSONArray("children"), colERC, locale);

		JSONObject node = JSONFactoryUtil.createJSONObject();

		node.put("externalReferenceCode", colERC);
		node.put("pageElementDefinition", definition);
		node.put("pageElements", children);
		node.put("parentExternalReferenceCode", gridERC);
		node.put("position", position);

		return node;
	}

	private JSONArray _convertChildren(
			JSONArray children, String parentERC, String locale)
		throws Exception {

		JSONArray result = JSONFactoryUtil.createJSONArray();

		if (children == null) {
			return result;
		}

		for (int i = 0; i < children.length(); i++) {
			JSONObject converted = _convertElement(
				children.getJSONObject(i), parentERC, i, locale);

			if (converted != null) {
				result.put(converted);
			}
		}

		return result;
	}

	private JSONArray _buildEditableElements(
			JSONObject content, String fragmentKey, String locale)
		throws Exception {

		JSONArray elements = JSONFactoryUtil.createJSONArray();

		Map<String, String> editableTypes = _OOTB_EDITABLE_TYPES.get(
			fragmentKey);

		Iterator<String> keys = content.keys();

		while (keys.hasNext()) {
			String id = keys.next();

			Object raw = content.get(id);

			String editableType = "text";

			if (editableTypes != null) {
				String mapped = editableTypes.get(id);

				if (mapped != null) {
					editableType = mapped;
				}
			}

			JSONObject editable = _buildEditable(
				id, editableType, raw, locale);

			if (editable != null) {
				elements.put(editable);
			}
		}

		return elements;
	}

	private JSONObject _buildEditable(
			String id, String type, Object raw, String locale)
		throws Exception {

		switch (type) {
			case "text":
				return _buildTextEditable(id, _toString(raw), locale);
			case "richText":
				return _buildRichTextEditable(id, _toString(raw), locale);
			case "image":
				return _buildImageEditable(id, raw, locale);
			case "link":
				return _buildLinkEditable(id, raw, locale);
			default:
				return null;
		}
	}

	private JSONObject _buildTextEditable(
			String id, String value, String locale)
		throws Exception {

		JSONObject inlineValue = JSONFactoryUtil.createJSONObject();

		inlineValue.put("value_i18n", _i18n(locale, value));

		JSONObject textFragment = JSONFactoryUtil.createJSONObject();

		textFragment.put("fragmentInlineValue", inlineValue);
		textFragment.put("type", "Inline");

		JSONObject linkTextValue = JSONFactoryUtil.createJSONObject();

		linkTextValue.put("textFragmentValue", textFragment);

		JSONObject elementValue = JSONFactoryUtil.createJSONObject();

		elementValue.put("fragmentLinkTextValue", linkTextValue);
		elementValue.put("type", "Text");

		JSONObject editable = JSONFactoryUtil.createJSONObject();

		editable.put("fragmentEditableElementValue", elementValue);
		editable.put("id", id);

		return editable;
	}

	private JSONObject _buildRichTextEditable(
			String id, String value, String locale)
		throws Exception {

		JSONObject inlineValue = JSONFactoryUtil.createJSONObject();

		inlineValue.put("value_i18n", _i18n(locale, value));

		JSONObject htmlFragment = JSONFactoryUtil.createJSONObject();

		htmlFragment.put("fragmentInlineValue", inlineValue);
		htmlFragment.put("type", "Inline");

		JSONObject elementValue = JSONFactoryUtil.createJSONObject();

		elementValue.put("htmlFragmentValue", htmlFragment);
		elementValue.put("type", "RichText");

		JSONObject editable = JSONFactoryUtil.createJSONObject();

		editable.put("fragmentEditableElementValue", elementValue);
		editable.put("id", id);

		return editable;
	}

	private JSONObject _buildImageEditable(
			String id, Object raw, String locale)
		throws Exception {

		String url;

		if (raw instanceof JSONObject) {
			url = ((JSONObject)raw).getString("url");
		}
		else {
			url = String.valueOf(raw);
		}

		JSONObject urlObj = JSONFactoryUtil.createJSONObject();

		urlObj.put("type", "URL");
		urlObj.put("url", url);

		JSONObject imageValue = JSONFactoryUtil.createJSONObject();

		imageValue.put("type", "Direct");
		imageValue.put("value_i18n", _i18nObject(locale, urlObj));

		JSONObject fragmentImage = JSONFactoryUtil.createJSONObject();

		fragmentImage.put("fragmentImageValue", imageValue);

		JSONObject elementValue = JSONFactoryUtil.createJSONObject();

		elementValue.put("fragmentImage", fragmentImage);
		elementValue.put("type", "Image");

		JSONObject editable = JSONFactoryUtil.createJSONObject();

		editable.put("fragmentEditableElementValue", elementValue);
		editable.put("id", id);

		return editable;
	}

	private JSONObject _buildLinkEditable(
			String id, Object raw, String locale)
		throws Exception {

		String text = "Learn more";
		String url = "#";
		String target = "Self";

		if (raw instanceof JSONObject) {
			JSONObject linkObj = (JSONObject)raw;

			text = linkObj.getString("text", "Learn more");
			url = linkObj.getString("url", "#");

			String rawTarget = linkObj.getString("target");

			if ((rawTarget != null) && !rawTarget.isEmpty()) {
				target = _normalizeLinkTarget(rawTarget);
			}
		}
		else if (raw instanceof String) {
			url = (String)raw;
		}

		JSONObject textInline = JSONFactoryUtil.createJSONObject();

		textInline.put("value_i18n", _i18n(locale, text));

		JSONObject textFragment = JSONFactoryUtil.createJSONObject();

		textFragment.put("fragmentInlineValue", textInline);
		textFragment.put("type", "Inline");

		JSONObject linkUrlInline = JSONFactoryUtil.createJSONObject();

		linkUrlInline.put("type", "FragmentInlineValue");
		linkUrlInline.put("value_i18n", _i18n(locale, url));

		JSONObject fragmentLink = JSONFactoryUtil.createJSONObject();

		fragmentLink.put("target", target);
		fragmentLink.put("value", linkUrlInline);

		JSONObject linkRef = JSONFactoryUtil.createJSONObject();

		linkRef.put("fragmentLink", fragmentLink);

		JSONObject linkTextValue = JSONFactoryUtil.createJSONObject();

		linkTextValue.put(
			"fragmentEditableElementValueFragmentLink", linkRef);
		linkTextValue.put("textFragmentValue", textFragment);

		JSONObject elementValue = JSONFactoryUtil.createJSONObject();

		elementValue.put("fragmentLinkTextValue", linkTextValue);
		elementValue.put("type", "Text");

		JSONObject editable = JSONFactoryUtil.createJSONObject();

		editable.put("fragmentEditableElementValue", elementValue);
		editable.put("id", id);

		return editable;
	}

	private void _applySpacing(
			JSONObject source, JSONObject target, String prefix,
			String defaultValue)
		throws Exception {

		JSONObject spacing = source.getJSONObject(prefix);

		String[] sides = {"Top", "Bottom", "Left", "Right"};

		for (String side : sides) {
			String value = null;

			if (spacing != null) {
				value = spacing.getString(side.toLowerCase());
			}

			if ((value == null) || value.isEmpty()) {
				value = defaultValue;
			}

			if (value != null) {
				target.put(prefix + side, value);
			}
		}
	}

	private JSONObject _i18n(String locale, String value) throws Exception {
		JSONObject i18n = JSONFactoryUtil.createJSONObject();

		i18n.put(locale, value);

		return i18n;
	}

	private JSONObject _i18nObject(String locale, JSONObject value)
		throws Exception {

		JSONObject i18n = JSONFactoryUtil.createJSONObject();

		i18n.put(locale, value);

		return i18n;
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

		String cleaned = target.replaceFirst("^_", "").toLowerCase();

		switch (cleaned) {
			case "blank":
				return "Blank";
			case "parent":
				return "Parent";
			case "top":
				return "Top";
			default:
				return "Self";
		}
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

		return String.valueOf(value);
	}

	private static final Map<String, String> _OOTB_NAME_TO_KEY =
		new HashMap<String, String>() {
			{
				put("heading", "BASIC_COMPONENT-heading");
				put("paragraph", "BASIC_COMPONENT-paragraph");
				put("card", "BASIC_COMPONENT-card");
				put("image", "BASIC_COMPONENT-image");
				put("button", "BASIC_COMPONENT-button");
				put("video", "BASIC_COMPONENT-video");
				put("spacer", "BASIC_COMPONENT-spacer");
				put("separator", "BASIC_COMPONENT-separator");
			}
		};

	private static final Map<String, Map<String, String>>
		_OOTB_EDITABLE_TYPES =
			new HashMap<String, Map<String, String>>() {
				{
					put(
						"BASIC_COMPONENT-heading",
						Map.of("element-text", "text"));
					put(
						"BASIC_COMPONENT-paragraph",
						Map.of("element-text", "richText"));
					put(
						"BASIC_COMPONENT-card",
						Map.of(
							"01-img", "image",
							"02-title", "richText",
							"03-content", "richText",
							"04-link", "link"));
					put(
						"BASIC_COMPONENT-image",
						Map.of("image-square", "image"));
					put(
						"BASIC_COMPONENT-button",
						Map.of(
							"element-text", "text",
							"element-link", "link"));
					put(
						"BASIC_COMPONENT-video",
						Map.of("element-video", "link"));
				}
			};

}

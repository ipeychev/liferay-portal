/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
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

package com.liferay.portal.kernel.servlet.taglib.aui;

import com.liferay.portal.kernel.util.Mergeable;
import com.liferay.portal.kernel.util.ObjectValuePair;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Brian Wing Shun Chan
 * @author Shuyang Zhou
 */
public class ScriptData implements Mergeable<ScriptData>, Serializable {

	public void append(
		String portletId, String content, String modules, ModuleTypes type) {

		PortletData portletData = _getPortletData(portletId);

		portletData.append(content, modules, type);
	}

	public void append(
		String portletId, StringBundler contentSB, String modules,
		ModuleTypes type) {

		PortletData portletData = _getPortletData(portletId);

		portletData.append(contentSB, modules, type);
	}

	public void mark() {
		for (PortletData portletData : _portletDataMap.values()) {
			_addToSBIndexList(portletData._auiCallbackSB);
			_addToSBIndexList(portletData._es6CallbackSB);
			_addToSBIndexList(portletData._rawSB);
		}
	}

	@Override
	public ScriptData merge(ScriptData scriptData) {
		if ((scriptData != null) && (scriptData != this)) {
			_portletDataMap.putAll(scriptData._portletDataMap);
		}

		return this;
	}

	public void reset() {
		for (ObjectValuePair<StringBundler, Integer> ovp : _sbIndexList) {
			StringBundler sb = ovp.getKey();

			sb.setIndex(ovp.getValue());
		}
	}

	public void writeTo(HttpServletRequest request, Writer writer)
		throws IOException {

		writer.write("<script type=\"text/javascript\">\n// <![CDATA[\n");

		StringBundler auiModulesSB = new StringBundler(_portletDataMap.size());
		StringBundler es6ModulesSB = new StringBundler(_portletDataMap.size());

		Set<String> auiModulesSet = new HashSet<>();
		Set<String> es6ModulesSet = new HashSet<>();

		for (PortletData portletData : _portletDataMap.values()) {
			portletData._rawSB.writeTo(writer);

			if (!portletData._auiModulesSet.isEmpty()) {
				auiModulesSB.append(portletData._auiCallbackSB);
			}

			if (!portletData._es6ModulesSet.isEmpty()) {
				es6ModulesSB.append(portletData._es6CallbackSB);
			}

			auiModulesSet.addAll(portletData._auiModulesSet);
			es6ModulesSet.addAll(portletData._es6ModulesSet);
		}

		if ((auiModulesSB.index() == 0) && (es6ModulesSB.index() == 0)) {
			writer.write("\n// ]]>\n</script>");

			return;
		}

		if (!es6ModulesSet.isEmpty()) {
			writer.write("require(");

			Map<String, String> generatedVariables = _generateVariables(
				es6ModulesSet);

			for (Iterator<String> iterator = es6ModulesSet.iterator();
				 iterator.hasNext();) {
					writer.write(StringPool.APOSTROPHE);
					writer.write(iterator.next());
					writer.write(StringPool.APOSTROPHE);

					if (iterator.hasNext()) {
						writer.write(StringPool.COMMA_AND_SPACE);
					}
			}

			writer.write(StringPool.COMMA_AND_SPACE);
			writer.write("function(");

			for (Iterator<String> iterator = es6ModulesSet.iterator();
				 iterator.hasNext();) {
					writer.write(generatedVariables.get(iterator.next()));

					if (iterator.hasNext()) {
						writer.write(StringPool.COMMA_AND_SPACE);
					}
			}

			writer.write(") {\n");

			es6ModulesSB.writeTo(writer);

			writer.write("},\nfunction(error) {\nconsole.error(error);\n});");

			writer.write("\n// ]]>\n</script>");
		}
		else if (!auiModulesSet.isEmpty()) {
			writer.write("AUI().use(");

			for (String use : auiModulesSet) {
				writer.write(StringPool.APOSTROPHE);
				writer.write(use);
				writer.write(StringPool.APOSTROPHE);
				writer.write(StringPool.COMMA_AND_SPACE);
			}

			writer.write("function(A) {");

			auiModulesSB.writeTo(writer);

			writer.write("});");

			writer.write("\n// ]]>\n</script>");
		}
	}

	public static enum ModuleTypes {

		MODULE_AUI, MODULE_ES6

	}

	private void _addToSBIndexList(StringBundler sb) {
		ObjectValuePair<StringBundler, Integer> ovp = new ObjectValuePair<>(
			sb, sb.index());

		int index = _sbIndexList.indexOf(ovp);

		if (index == -1) {
			_sbIndexList.add(ovp);
		}
		else {
			ovp = _sbIndexList.get(index);

			ovp.setValue(sb.index());
		}
	}

	private Map<String, String> _generateVariables(Set<String> requiredFiles) {
		Map<String, String> generatedVariablesMap = new HashMap<>();
		Map<String, Integer> variablesIndex = new HashMap<>();
		Set<String> generatedVariables = new HashSet<>();

		for (String requiredFile : requiredFiles) {
			StringBundler sb = new StringBundler();
			CharSequence firstCharacter = requiredFile.subSequence(0, 1);
			Matcher matcher = patternValidFirstCharacter.matcher(
				firstCharacter);

			if (!matcher.matches()) {
				sb.append(StringPool.UNDERLINE);
			}
			else {
				sb.append(firstCharacter);
			}

			for (int i = 1; i < requiredFile.length(); i++) {
				CharSequence curCharacter = requiredFile.subSequence(i, i + 1);

				matcher = patternValidCharacters.matcher(curCharacter);

				if (!matcher.matches()) {
					while (++i < requiredFile.length()) {
						CharSequence nextCharacter = requiredFile.subSequence(
							i, i + 1);

						matcher = patternValidCharacters.matcher(nextCharacter);

						if (matcher.matches()) {
							StringUtil.toUpperCase(
								sb.append(nextCharacter).toString());

							break;
						}
					}
				}
				else {
					sb.append(curCharacter);
				}
			}

			String generatedVariable = sb.toString();

			if (generatedVariables.contains(generatedVariable)) {
				int index = 1;

				if (variablesIndex.containsKey(generatedVariable)) {
					index = variablesIndex.get(generatedVariable) + 1;
				}

				variablesIndex.put(generatedVariable, index);

				generatedVariable += index;
			}

			generatedVariables.add(generatedVariable);
			generatedVariablesMap.put(requiredFile, generatedVariable);
		}

		return generatedVariablesMap;
	}

	private PortletData _getPortletData(String portletId) {
		if (Validator.isNull(portletId)) {
			portletId = StringPool.BLANK;
		}

		PortletData portletData = _portletDataMap.get(portletId);

		if (portletData == null) {
			portletData = new PortletData();

			PortletData oldPortletData = _portletDataMap.putIfAbsent(
				portletId, portletData);

			if (oldPortletData != null) {
				portletData = oldPortletData;
			}
		}

		return portletData;
	}

	private static final long serialVersionUID = 1L;

	private final ConcurrentMap<String, PortletData> _portletDataMap =
		new ConcurrentHashMap<>();
	private final List<ObjectValuePair<StringBundler, Integer>> _sbIndexList =
		new ArrayList<>();
	private final Pattern patternValidCharacters = Pattern.compile(
		"[0-9a-z_$]", Pattern.CASE_INSENSITIVE);
	private final Pattern patternValidFirstCharacter = Pattern.compile(
		"[a-z_$]", Pattern.CASE_INSENSITIVE);

	private class PortletData implements Serializable {

		public void append(String content, String modules, ModuleTypes type) {
			if (Validator.isNull(modules)) {
				_rawSB.append(content);
			}
			else {
				String[] modulesArray = StringUtil.split(modules);

				if (type == ModuleTypes.MODULE_AUI) {
					_auiCallbackSB.append("(function() {");
					_auiCallbackSB.append(content);
					_auiCallbackSB.append("})();");

					for (String module : modulesArray) {
						_auiModulesSet.add(StringUtil.trim(module));
					}
				}
				else {
					_es6CallbackSB.append("(function() {");
					_es6CallbackSB.append(content);
					_es6CallbackSB.append("})();");

					for (String module : modulesArray) {
						_es6ModulesSet.add(StringUtil.trim(module));
					}
				}
			}
		}

		public void append(
			StringBundler contentSB, String modules, ModuleTypes type) {

			if (Validator.isNull(modules)) {
				_rawSB.append(contentSB);
			}
			else {
				String[] modulesArray = StringUtil.split(modules);

				if (type == ModuleTypes.MODULE_AUI) {
					_auiCallbackSB.append("(function() {");
					_auiCallbackSB.append(contentSB);
					_auiCallbackSB.append("})();");

					for (String module : modulesArray) {
						_auiModulesSet.add(StringUtil.trim(module));
					}
				}
				else {
					_es6CallbackSB.append("(function() {");
					_es6CallbackSB.append(contentSB);
					_es6CallbackSB.append("})();");

					for (String module : modulesArray) {
						_es6ModulesSet.add(StringUtil.trim(module));
					}
				}
			}
		}

		private static final long serialVersionUID = 1L;

		private final StringBundler _auiCallbackSB = new StringBundler();
		private final Set<String> _auiModulesSet = new HashSet<>();
		private final StringBundler _es6CallbackSB = new StringBundler();
		private final Set<String> _es6ModulesSet = new HashSet<>();
		private final StringBundler _rawSB = new StringBundler();

	}

}
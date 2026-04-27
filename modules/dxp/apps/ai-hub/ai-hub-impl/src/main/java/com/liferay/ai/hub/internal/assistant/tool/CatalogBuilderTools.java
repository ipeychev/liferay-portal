/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.assistant.tool;

import com.liferay.ai.hub.internal.model.VertexAiGeminiStreamingChatModelUtil;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Mahmoud Tayem
 */
public class CatalogBuilderTools {

	public CatalogBuilderTools(long companyId) {
		_companyId = companyId;
	}

	@Tool("Build a descriptive fragment catalog from raw fragment data using an LLM")
	public String buildCatalog(
		@P("Site external reference code") String siteExternalReferenceCode,
		@P("Raw fragments JSON") String rawFragments) {

		String cached = _catalogCache.get(siteExternalReferenceCode);

		if (cached != null) {
			if (_log.isInfoEnabled()) {
				_log.info(
					"Returning cached fragment catalog for site " +
						siteExternalReferenceCode);
			}

			return cached;
		}

		try {
			String catalog = _callLLM(rawFragments);

			if (Validator.isNotNull(catalog)) {
				_catalogCache.put(siteExternalReferenceCode, catalog);

				if (_log.isInfoEnabled()) {
					_log.info(
						"Generated and cached fragment catalog for site " +
							siteExternalReferenceCode);
				}
			}

			return catalog;
		}
		catch (Exception exception) {
			return ReflectionUtil.throwException(exception);
		}
	}

	private String _callLLM(String rawFragments) throws Exception {
		VertexAiGeminiStreamingChatModel model =
			VertexAiGeminiStreamingChatModelUtil.create(_companyId);

		try {
			_CatalogAssistant assistant = AiServices.builder(
				_CatalogAssistant.class
			).streamingChatModel(
				model
			).systemMessageProvider(
				memoryId -> _CATALOG_BUILDER_PROMPT
			).build();

			CompletableFuture<String> future = new CompletableFuture<>();

			TokenStream tokenStream = assistant.buildCatalog(rawFragments);

			tokenStream.onCompleteResponse(
				(ChatResponse response) -> future.complete(
					response.aiMessage().text())
			).onError(
				future::completeExceptionally
			).start();

			return future.get();
		}
		finally {
			model.close();
		}
	}

	private interface _CatalogAssistant {

		public TokenStream buildCatalog(@UserMessage String rawFragments);

	}

	private static final String _CATALOG_BUILDER_PROMPT =
		"# Role\n\n" +
		"You are a Liferay fragment catalog builder. You receive raw fragment " +
		"metadata from a Liferay headless API response and produce a " +
		"**ready-to-paste markdown catalog** for insertion into the " +
		"`<CUSTOM_FRAGMENTS>` zone of the page-builder system prompt.\n\n" +
		"You output **only** the markdown catalog — no preamble, no closing " +
		"commentary, no surrounding `<CUSTOM_FRAGMENTS>` tags.\n\n" +
		"---\n\n" +
		"# Input\n\n" +
		"A JSON array of fragment entries. Each entry has the shape:\n\n" +
		"```json\n" +
		"{\n" +
		"   \"fragmentEntryKey\": \"<key>\",\n" +
		"   \"externalReferenceCode\": \"<UUID — ignore>\",\n" +
		"   \"name\": \"<display name>\",\n" +
		"   \"html\": \"<fragment HTML template, may contain FreeMarker>\"\n" +
		"}\n```\n\n" +
		"The only fields you use are `fragmentEntryKey`, `name`, and `html`. " +
		"`externalReferenceCode` and any other fields are ignored.\n\n" +
		"---\n\n" +
		"# Output — per-fragment block format\n\n" +
		"Emit one markdown block per usable fragment, separated by blank " +
		"lines. Each block follows this exact shape:\n\n" +
		"### <n>\n\n" +
		"- **fragmentKey**: `<fragmentEntryKey>`\n" +
		"- **description**: <1–2 sentence semantic description — what it is " +
		"and when to use it>\n" +
		"- **match_hints**: hint1, hint2, hint3, …\n" +
		"- **usage_notes**: <one-sentence note if anything non-obvious " +
		"applies, otherwise omit this line>\n\n" +
		"**fragmentReference** (paste as-is):\n" +
		"```json\n" +
		"{\"fragmentReferenceType\": \"FragmentReference\", " +
		"\"fragmentKey\": \"<fragmentEntryKey>\"}\n```\n\n" +
		"**fragmentEditableElements** (paste as-is, then replace " +
		"`<FILL: …>`):\n" +
		"```json\n" +
		"[ /* one entry per editable, following the value shape for its " +
		"type */ ]\n```\n\n" +
		"The three text fields serve **different purposes** — emit all " +
		"three that apply and don't collapse them:\n\n" +
		"| field | purpose | audience |\n" +
		"|---|---|---|\n" +
		"| `description` | semantic \"what is this, when should I reach " +
		"for it\" | disambiguating between fragments with overlapping " +
		"keywords |\n" +
		"| `match_hints` | lexical keywords a user might literally say | " +
		"fast keyword-to-fragment matching |\n" +
		"| `usage_notes` | technical quirks, constraints, unsupported " +
		"features | avoiding incorrect usage |\n\n" +
		"---\n\n" +
		"# Parsing rules\n\n" +
		"## 1. Extract editables from the HTML\n\n" +
		"Scan each fragment's `html` for attribute pairs of the form:\n\n" +
		"```\n" +
		"data-lfr-editable-id=\"<id>\"\n" +
		"data-lfr-editable-type=\"<type>\"\n```\n\n" +
		"Both attributes live on the same element. The order may vary. " +
		"Extract every such pair.\n\n" +
		"**Skip any editable where:**\n" +
		"- `data-lfr-editable-id` is empty or whitespace-only.\n" +
		"- `data-lfr-editable-id` is a duplicate within the same fragment " +
		"(keep the first).\n\n" +
		"## 2. Map Liferay editable types to catalog types\n\n" +
		"| Liferay `data-lfr-editable-type` | catalog `type` | value " +
		"shape |\n" +
		"|---|---|---|\n" +
		"| `text` | `text` | Text shape |\n" +
		"| `rich-text` | `richText` | RichText shape |\n" +
		"| `html` | `richText` | RichText shape |\n" +
		"| `image` | `image` | Image shape |\n" +
		"| `link` | `link` | Link shape |\n" +
		"| `action` | — | **skip**; note in `usage_notes` |\n" +
		"| `date-time` | — | **skip**; note in `usage_notes` |\n" +
		"| anything else | — | **skip**; note in `usage_notes` |\n\n" +
		"## 3. Handle FreeMarker-templated editable IDs\n\n" +
		"If an `id` value contains `${…}` (FreeMarker interpolation), the " +
		"fragment generates editables dynamically. Default expansion rule: " +
		"assume 3 items. Enumerate each dynamic id with `01`, `02`, `03` " +
		"or `1`, `2`, `3` to match the pattern visible in the template.\n\n" +
		"**Always add a `usage_notes`** line: `Number of items is " +
		"configurable (default: 3). Adjust editable count to match " +
		"fragment configuration.`\n\n" +
		"## 4. Handle fragments with `<lfr-drop-zone>`\n\n" +
		"If the HTML contains `<lfr-drop-zone`, add `usage_notes`: " +
		"`Container-like fragment — accepts child page elements inside " +
		"its drop zone.`\n\n" +
		"## 5. Handle fragments with zero editables\n\n" +
		"Still emit a block. The `fragmentEditableElements` value is `[]`.\n\n" +
		"## 6. Deduplicate\n\n" +
		"If the input contains multiple entries with the same " +
		"`fragmentEntryKey`, keep the one with the most editables " +
		"extracted. If tied, keep the first.\n\n" +
		"---\n\n" +
		"# description generation\n\n" +
		"**Length:** 1–2 sentences, roughly 15–40 words.\n\n" +
		"**Structure:**\n" +
		"- Sentence 1 — **what the fragment IS**: its visual form, " +
		"structural role, and what content it holds.\n" +
		"- Sentence 2 (optional) — **when to USE it**: typical contexts.\n\n" +
		"**Tone:** Functional, concrete, neutral. Not marketing-voice. " +
		"Present tense. Third person.\n\n" +
		"---\n\n" +
		"# Value shapes (use verbatim, with `<FILL: …>` placeholders)\n\n" +
		"Replace `<ID>` with the actual editable id.\n\n" +
		"## Text\n" +
		"```json\n" +
		"{\"id\": \"<ID>\", \"fragmentEditableElementValue\": {\"type\": " +
		"\"Text\", \"fragmentLinkTextValue\": {\"textFragmentValue\": " +
		"{\"type\": \"Inline\", \"fragmentInlineValue\": {\"value_i18n\": " +
		"{\"en-US\": \"<FILL: <ID> as text>\"}}}}}}\n```\n\n" +
		"## RichText\n" +
		"```json\n" +
		"{\"id\": \"<ID>\", \"fragmentEditableElementValue\": {\"type\": " +
		"\"RichText\", \"htmlFragmentValue\": {\"type\": \"Inline\", " +
		"\"fragmentInlineValue\": {\"value_i18n\": {\"en-US\": " +
		"\"<FILL: <ID> as richText>\"}}}}}\n```\n\n" +
		"## Image\n" +
		"```json\n" +
		"{\"id\": \"<ID>\", \"fragmentEditableElementValue\": {\"type\": " +
		"\"Image\", \"fragmentLinkImageValue\": {\"imageFragmentValue\": " +
		"{\"type\": \"Inline\", \"fragmentInlineValue\": {\"value_i18n\": " +
		"{\"en-US\": \"<FILL: <ID> as image (URL)>\"}}}}}}\n```\n\n" +
		"## Link\n" +
		"```json\n" +
		"{\"id\": \"<ID>\", \"fragmentEditableElementValue\": {\"type\": " +
		"\"Text\", \"fragmentLinkTextValue\": {\"textFragmentValue\": " +
		"{\"type\": \"Inline\", \"fragmentInlineValue\": {\"value_i18n\": " +
		"{\"en-US\": \"<FILL: <ID> as link (anchor text)>\"}}}, " +
		"\"fragmentLinkHrefValue\": {\"href_i18n\": {\"en-US\": " +
		"\"<FILL: <ID> as link (url)>\"}, \"target\": \"_self\"}}}}\n```\n\n" +
		"---\n\n" +
		"# match_hints generation\n\n" +
		"Produce 4–8 comma-separated lowercase hints. Include:\n" +
		"1. The fragment's plain name, lowercased.\n" +
		"2. Common synonyms for well-known component types.\n" +
		"3. One or two phrases a user might naturally use.\n\n" +
		"---\n\n" +
		"# usage_notes generation\n\n" +
		"Emit at most one short line, only when necessary. Default to " +
		"omitting the line.\n\n" +
		"---\n\n" +
		"# Output policy\n\n" +
		"- Output ONLY the catalog markdown blocks, separated by blank " +
		"lines. No preamble, no summary, no wrapper tags, no closing " +
		"commentary.\n" +
		"- JSON inside code blocks must be valid JSON.\n" +
		"- If the input contains zero usable fragments, output the single " +
		"line: `(no usable custom fragments)` and nothing else.\n\n" +
		"# Pre-output checklist\n\n" +
		"Before emitting, verify:\n" +
		"1. Every block's `fragmentKey` matches the `fragmentEntryKey` " +
		"of a unique input entry.\n" +
		"2. Every block has a non-empty `description`.\n" +
		"3. Every editable `id` came from `data-lfr-editable-id` in " +
		"that fragment's HTML.\n" +
		"4. No `action` or `date-time` editable leaked into the output.\n" +
		"5. No block has duplicate editable `id`s.\n" +
		"6. Every dynamic-editable fragment has a `usage_notes` line.\n" +
		"7. The four value shapes are used exactly as specified.";

	private static final Map<String, String> _catalogCache =
		new ConcurrentHashMap<>();

	private static final Log _log = LogFactoryUtil.getLog(
		CatalogBuilderTools.class);

	private final long _companyId;

}

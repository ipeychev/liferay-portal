/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import type {
	ContentSample,
	ContentSampleField,
	DetectedConfig,
	SummaryStat,
	Template,
} from '../types/ContentModel';
import type {GeneratedPage} from '../types/GeneratedPage';
import type {Generation} from '../types/Generation';
import type {GenerationItem} from '../types/GenerationItem';

const LANGUAGE_LABELS: Record<string, string> = {
	de: 'German',
	en: 'English',
	es: 'Spanish',
	fr: 'French',
	it: 'Italian',
	ja: 'Japanese',
	pt: 'Portuguese',
};

const METADATA_KEYS = new Set([
	'actions',
	'classNameId',
	'classPK',
	'createDate',
	'creator',
	'dateCreated',
	'dateModified',
	'externalReferenceCode',
	'groupId',
	'id',
	'modifiedDate',
	'parentExternalReferenceCode',
	'priority',
	'siteId',
	'sortOrder',
	'status',
	'userId',
]);

const PAGE_FILE_TOKENS = new Set(['blogs', 'pages']);

const TEMPLATE_ICONS: Record<string, string> = {
	'asset-library': 'documents-and-media',
	'blogs': 'document-text',
	'connected-site': 'link',
	'fragment-set': 'code',
	'fragments': 'code',
	'pages': 'page',
	'site': 'home',
};

export function getFileToken(fileName: string): string {
	const base = fileName.replace(/\.batch-engine-data\.json$/i, '');

	return base.replace(/^\d+-/, '');
}

export function getLanguageLabel(code: string): string {
	return LANGUAGE_LABELS[code.toLowerCase()] ?? code.toUpperCase();
}

export function getTemplateLabel(fileName: string): string {
	switch (getFileToken(fileName)) {
		case 'asset-library':
			return Liferay.Language.get('asset-library');
		case 'blogs':
			return Liferay.Language.get('blog-article');
		case 'connected-site':
			return Liferay.Language.get('connected-site');
		case 'fragment-set':
			return Liferay.Language.get('fragment-set');
		case 'fragments':
			return Liferay.Language.get('fragment');
		case 'pages':
			return Liferay.Language.get('page');
		case 'site':
			return Liferay.Language.get('site');
		default:
			return fileName;
	}
}

export function getTemplateIcon(fileName: string): string {
	return TEMPLATE_ICONS[getFileToken(fileName)] ?? 'document';
}

export function getItemLanguages(item: GenerationItem): string[] {
	if (!item.languages) {
		return [];
	}

	return item.languages
		.split(',')
		.map((language) => language.trim().toLowerCase())
		.filter(Boolean);
}

export function getItemURL(item: GenerationItem): string | null {
	if (!item.previewItem) {
		return null;
	}

	try {
		return getEntryURL(JSON.parse(item.previewItem));
	}
	catch (exception) {
		return null;
	}
}

export function buildTemplates(items: GenerationItem[]): Template[] {
	return items.map((item) => ({
		icon: getTemplateIcon(item.fileName),
		itemCount: item.itemCount ?? 0,
		label: getTemplateLabel(item.fileName),
		languageCount: getItemLanguages(item).length,
		pageCount: PAGE_FILE_TOKENS.has(getFileToken(item.fileName))
			? item.itemCount ?? 0
			: 0,
	}));
}

export function buildSummary(
	generation: Generation,
	items: GenerationItem[]
): SummaryStat[] {
	const languages = new Set<string>();

	let pages = 0;
	let totalEntries = 0;

	for (const item of items) {
		totalEntries += item.itemCount ?? 0;

		if (PAGE_FILE_TOKENS.has(getFileToken(item.fileName))) {
			pages += item.itemCount ?? 0;
		}

		for (const language of getItemLanguages(item)) {
			languages.add(language);
		}
	}

	if (!languages.size && generation.targetLanguages) {
		for (const language of generation.targetLanguages.split(',')) {
			languages.add(language.trim().toLowerCase());
		}
	}

	return [
		{
			icon: 'document',
			label: Liferay.Language.get('total-pages'),
			value: pages,
		},
		{
			icon: 'automatic-translate',
			label: Liferay.Language.get('languages'),
			value: languages.size,
		},
		{
			icon: 'stars',
			label: Liferay.Language.get('templates'),
			value: items.length,
		},
		{
			icon: 'document',
			label: Liferay.Language.get('total-entries'),
			value: totalEntries,
		},
	];
}

export function buildDetectedConfig(
	generation: Generation,
	items: GenerationItem[]
): DetectedConfig {
	const languages = new Set<string>();

	for (const item of items) {
		for (const language of getItemLanguages(item)) {
			languages.add(language);
		}
	}

	if (!languages.size && generation.targetLanguages) {
		for (const language of generation.targetLanguages.split(',')) {
			const trimmed = language.trim().toLowerCase();

			if (trimmed) {
				languages.add(trimmed);
			}
		}
	}

	return {
		languageLabels: [...languages].map(getLanguageLabel),
	};
}

function getKnownFieldLabel(key: string): string | null {
	switch (key) {
		case 'excerpt':
			return Liferay.Language.get('excerpt');
		case 'friendlyURLPath':
		case 'urlTitle':
			return Liferay.Language.get('url');
		case 'h1Heading':
			return Liferay.Language.get('h1-heading');
		case 'metaDescription':
			return Liferay.Language.get('meta-description');
		case 'seoTitle':
			return Liferay.Language.get('seo-title');
		default:
			return null;
	}
}

function humanizeKey(key: string): string {
	return key
		.replace(/_i18n$/, '')
		.replace(/([A-Z])/g, ' $1')
		.replace(/[_-]+/g, ' ')
		.replace(/^./, (character) => character.toUpperCase())
		.trim();
}

function stringifyValue(value: unknown): string {
	if (value === null || value === undefined) {
		return '';
	}

	if (typeof value === 'string') {
		return value;
	}

	if (typeof value === 'number' || typeof value === 'boolean') {
		return String(value);
	}

	if (typeof value === 'object') {
		const map = value as Record<string, string>;

		const localized = map.en_US ?? map[Object.keys(map)[0] ?? ''];

		if (typeof localized === 'string') {
			return localized;
		}
	}

	return '';
}

export function parseContentSample(item: GenerationItem): ContentSample | null {
	if (!item.previewItem) {
		return null;
	}

	let parsed: Record<string, unknown>;

	try {
		parsed = JSON.parse(item.previewItem);
	}
	catch (exception) {
		return null;
	}

	const chips: string[] = [];
	const fields: ContentSampleField[] = [];

	for (const [key, value] of Object.entries(parsed)) {
		if (METADATA_KEYS.has(key)) {
			continue;
		}

		const label = humanizeKey(key);

		chips.push(label);

		const stringValue = stringifyValue(value);

		if (stringValue) {
			fields.push({
				label: getKnownFieldLabel(key) ?? label,
				value: stringValue,
			});
		}
	}

	return {
		chips,
		fields,
		title: getTemplateLabel(item.fileName),
	};
}

export function getEntryTitle(entry: Record<string, unknown>): string {
	return (
		stringifyValue(entry.title) ||
		stringifyValue(entry.title_i18n) ||
		stringifyValue(entry.name) ||
		stringifyValue(entry.name_i18n) ||
		stringifyValue(entry.headline)
	);
}

export function getEntryURL(entry: Record<string, unknown>): string | null {
	const url =
		stringifyValue(entry.friendlyUrlPath) ||
		stringifyValue(entry.friendlyUrlPath_i18n) ||
		stringifyValue(entry.friendlyURLPath) ||
		stringifyValue(entry.urlTitle);

	return url || null;
}

export function buildPages(
	item: GenerationItem,
	entries: Record<string, unknown>[]
): GeneratedPage[] {
	const icon = getTemplateIcon(item.fileName);
	const languages = getItemLanguages(item);
	const templateLabel = getTemplateLabel(item.fileName);

	return entries.map((entry, index) => ({
		icon,
		id: `${item.id}-${index}`,
		languages,
		templateLabel,
		title: getEntryTitle(entry) || `${templateLabel} ${index + 1}`,
		url: getEntryURL(entry),
	}));
}

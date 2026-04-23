/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.cell.web.internal.frontend.data.set.view;

import com.liferay.ai.hub.cell.web.internal.constants.AIHubCellFDSNames;
import com.liferay.frontend.data.set.view.FDSView;
import com.liferay.frontend.data.set.view.table.BaseTableFDSView;
import com.liferay.frontend.data.set.view.table.FDSTableSchema;
import com.liferay.frontend.data.set.view.table.FDSTableSchemaBuilder;
import com.liferay.frontend.data.set.view.table.FDSTableSchemaBuilderFactory;

import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Mylena Monte
 */
@Component(
	property = "frontend.data.set.name=" + AIHubCellFDSNames.CONTENT_SITE_GENERATOR,
	service = FDSView.class
)
public class ContentSiteGeneratorTableFDSView extends BaseTableFDSView {

	@Override
	public FDSTableSchema getFDSTableSchema(Locale locale) {
		FDSTableSchemaBuilder fdsTableSchemaBuilder =
			_fdsTableSchemaBuilderFactory.create();

		return fdsTableSchemaBuilder.add(
			"name", "name"
		).add(
			"items", "items"
		).add(
			"language", "language"
		).add(
			"createdBy", "created-by"
		).add(
			"status", "status"
		).add(
			"modified", "modified"
		).build();
	}

	@Override
	public boolean isDefault(String fdsName) {
		return true;
	}

	@Reference
	private FDSTableSchemaBuilderFactory _fdsTableSchemaBuilderFactory;

}
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

package com.liferay.shopping.model;

import aQute.bnd.annotation.ProviderType;

import com.liferay.portal.model.PersistedModel;

/**
 * The extended model interface for the ShoppingCart service. Represents a row in the &quot;ShoppingCart&quot; database table, with each column mapped to a property of this class.
 *
 * @author Brian Wing Shun Chan
 * @see ShoppingCartModel
 * @see com.liferay.shopping.model.impl.ShoppingCartImpl
 * @see com.liferay.shopping.model.impl.ShoppingCartModelImpl
 * @generated
 */
@ProviderType
public interface ShoppingCart extends ShoppingCartModel, PersistedModel {
	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify this interface directly. Add methods to {@link com.liferay.shopping.model.impl.ShoppingCartImpl} and rerun ServiceBuilder to automatically copy the method declarations to this interface.
	 */
	public void addItemId(long itemId, java.lang.String fields);

	public com.liferay.shopping.model.ShoppingCoupon getCoupon()
		throws com.liferay.portal.kernel.exception.PortalException;

	public java.util.Map<com.liferay.shopping.model.ShoppingCartItem, java.lang.Integer> getItems();

	public int getItemsSize();
}
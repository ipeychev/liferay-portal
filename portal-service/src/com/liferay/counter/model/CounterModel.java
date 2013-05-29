/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
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

package com.liferay.counter.model;

import com.liferay.portal.kernel.bean.AutoEscape;
import com.liferay.portal.model.BaseModel;
import com.liferay.portal.model.CacheModel;
import com.liferay.portal.service.ServiceContext;

import com.liferay.portlet.expando.model.ExpandoBridge;

import java.io.Serializable;

/**
 * The base model interface for the Counter service. Represents a row in the &quot;Counter&quot; database table, with each column mapped to a property of this class.
 *
 * <p>
 * This interface and its corresponding implementation {@link com.liferay.counter.model.impl.CounterModelImpl} exist only as a container for the default property accessors generated by ServiceBuilder. Helper methods and all application logic should be put in {@link com.liferay.counter.model.impl.CounterImpl}.
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see Counter
 * @see com.liferay.counter.model.impl.CounterImpl
 * @see com.liferay.counter.model.impl.CounterModelImpl
 * @generated
 */
public interface CounterModel extends BaseModel<Counter> {
	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this interface directly. All methods that expect a counter model instance should use the {@link Counter} interface instead.
	 */

	/**
	 * Returns the primary key of this counter.
	 *
	 * @return the primary key of this counter
	 */
	public String getPrimaryKey();

	/**
	 * Sets the primary key of this counter.
	 *
	 * @param primaryKey the primary key of this counter
	 */
	public void setPrimaryKey(String primaryKey);

	/**
	 * Returns the name of this counter.
	 *
	 * @return the name of this counter
	 */
	@AutoEscape
	public String getName();

	/**
	 * Sets the name of this counter.
	 *
	 * @param name the name of this counter
	 */
	public void setName(String name);

	/**
	 * Returns the current ID of this counter.
	 *
	 * @return the current ID of this counter
	 */
	public long getCurrentId();

	/**
	 * Sets the current ID of this counter.
	 *
	 * @param currentId the current ID of this counter
	 */
	public void setCurrentId(long currentId);

	@Override
	public boolean isNew();

	@Override
	public void setNew(boolean n);

	@Override
	public boolean isCachedModel();

	@Override
	public void setCachedModel(boolean cachedModel);

	@Override
	public boolean isEscapedModel();

	@Override
	public Serializable getPrimaryKeyObj();

	@Override
	public void setPrimaryKeyObj(Serializable primaryKeyObj);

	@Override
	public ExpandoBridge getExpandoBridge();

	@Override
	public void setExpandoBridgeAttributes(BaseModel<?> baseModel);

	@Override
	public void setExpandoBridgeAttributes(ExpandoBridge expandoBridge);

	@Override
	public void setExpandoBridgeAttributes(ServiceContext serviceContext);

	@Override
	public Object clone();

	@Override
	public int compareTo(Counter counter);

	@Override
	public int hashCode();

	@Override
	public CacheModel<Counter> toCacheModel();

	@Override
	public Counter toEscapedModel();

	@Override
	public Counter toUnescapedModel();

	@Override
	public String toString();

	@Override
	public String toXmlString();
}
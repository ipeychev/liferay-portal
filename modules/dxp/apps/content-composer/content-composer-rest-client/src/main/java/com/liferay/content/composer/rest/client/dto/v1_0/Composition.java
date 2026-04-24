/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.content.composer.rest.client.dto.v1_0;

import com.liferay.content.composer.rest.client.function.UnsafeSupplier;
import com.liferay.content.composer.rest.client.serdes.v1_0.CompositionSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Objects;

/**
 * @author Feliphe Marinho
 * @generated
 */
@Generated("")
public class Composition implements Cloneable, Serializable {

	public static Composition toDTO(String json) {
		return CompositionSerDes.toDTO(json);
	}

	public String getBrief() {
		return brief;
	}

	public void setBrief(String brief) {
		this.brief = brief;
	}

	public void setBrief(
		UnsafeSupplier<String, Exception> briefUnsafeSupplier) {

		try {
			brief = briefUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String brief;

	public String getChatExternalReferenceCode() {
		return chatExternalReferenceCode;
	}

	public void setChatExternalReferenceCode(String chatExternalReferenceCode) {
		this.chatExternalReferenceCode = chatExternalReferenceCode;
	}

	public void setChatExternalReferenceCode(
		UnsafeSupplier<String, Exception>
			chatExternalReferenceCodeUnsafeSupplier) {

		try {
			chatExternalReferenceCode =
				chatExternalReferenceCodeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String chatExternalReferenceCode;

	public String getExternalReferenceCode() {
		return externalReferenceCode;
	}

	public void setExternalReferenceCode(String externalReferenceCode) {
		this.externalReferenceCode = externalReferenceCode;
	}

	public void setExternalReferenceCode(
		UnsafeSupplier<String, Exception> externalReferenceCodeUnsafeSupplier) {

		try {
			externalReferenceCode = externalReferenceCodeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String externalReferenceCode;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setId(UnsafeSupplier<Long, Exception> idUnsafeSupplier) {
		try {
			id = idUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Long id;

	@Override
	public Composition clone() throws CloneNotSupportedException {
		return (Composition)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof Composition)) {
			return false;
		}

		Composition composition = (Composition)object;

		return Objects.equals(toString(), composition.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return CompositionSerDes.toJSON(this);
	}

}
// LIFERAY-REST-BUILDER-HASH:616737774
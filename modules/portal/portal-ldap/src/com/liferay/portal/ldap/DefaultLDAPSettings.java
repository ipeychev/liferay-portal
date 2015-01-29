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

package com.liferay.portal.ldap;

import aQute.bnd.annotation.metatype.Configurable;

import com.liferay.portal.kernel.ldap.LDAPUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.log.LogUtil;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.PropertiesUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.ldap.configuration.LDAPConfiguration;
import com.liferay.portal.model.User;
import com.liferay.portal.security.ldap.LDAPSettings;
import com.liferay.portal.service.UserLocalServiceUtil;

import java.util.Map;
import java.util.Properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

/**
 * @author Edward Han
 * @author Michael C. Han
 * @author Brian Wing Shun Chan
 */
@Component(immediate = true, service = LDAPSettings.class)
public class DefaultLDAPSettings implements LDAPSettings {

	@Override
	public String getAuthSearchFilter(
			long ldapServerId, long companyId, String emailAddress,
			String screenName, String userId)
		throws Exception {

		String postfix = getPropertyPostfix(ldapServerId);

		String filter = PrefsPropsUtil.getString(
			companyId, PropsKeys.LDAP_AUTH_SEARCH_FILTER + postfix);

		if (_log.isDebugEnabled()) {
			_log.debug("Search filter before transformation " + filter);
		}

		filter = StringUtil.replace(
			filter,
			new String[] {
				"@company_id@", "@email_address@", "@screen_name@", "@user_id@"
			},
			new String[] {
				String.valueOf(companyId), emailAddress, screenName, userId
			});

		LDAPUtil.validateFilter(filter);

		if (_log.isDebugEnabled()) {
			_log.debug("Search filter after transformation " + filter);
		}

		return filter;
	}

	@Override
	public Properties getContactExpandoMappings(
			long ldapServerId, long companyId)
		throws Exception {

		String postfix = getPropertyPostfix(ldapServerId);

		Properties contactExpandoMappings = PropertiesUtil.load(
			PrefsPropsUtil.getString(
				companyId, PropsKeys.LDAP_CONTACT_CUSTOM_MAPPINGS + postfix,
				StringPool.BLANK));

		LogUtil.debug(_log, contactExpandoMappings);

		return contactExpandoMappings;
	}

	@Override
	public Properties getContactMappings(long ldapServerId, long companyId)
		throws Exception {

		String postfix = getPropertyPostfix(ldapServerId);

		Properties contactMappings = PropertiesUtil.load(
			PrefsPropsUtil.getString(
				companyId, PropsKeys.LDAP_CONTACT_MAPPINGS + postfix,
				StringPool.BLANK));

		LogUtil.debug(_log, contactMappings);

		return contactMappings;
	}

	@Override
	public Properties getGroupMappings(long ldapServerId, long companyId)
		throws Exception {

		String postfix = getPropertyPostfix(ldapServerId);

		Properties groupMappings = PropertiesUtil.load(
			PrefsPropsUtil.getString(
				companyId, PropsKeys.LDAP_GROUP_MAPPINGS + postfix,
				StringPool.BLANK));

		LogUtil.debug(_log, groupMappings);

		return groupMappings;
	}

	@Override
	public long getPreferredLDAPServerId(long companyId, String screenName) {
		User user = UserLocalServiceUtil.fetchUserByScreenName(
			companyId, screenName);

		if (user == null) {
			return -1;
		}

		return user.getLdapServerId();
	}

	@Override
	public String getPropertyPostfix(long ldapServerId) {
		return StringPool.PERIOD + ldapServerId;
	}

	@Override
	public Properties getUserExpandoMappings(long ldapServerId, long companyId)
		throws Exception {

		String postfix = getPropertyPostfix(ldapServerId);

		Properties userExpandoMappings = PropertiesUtil.load(
			PrefsPropsUtil.getString(
				companyId, PropsKeys.LDAP_USER_CUSTOM_MAPPINGS + postfix,
				StringPool.BLANK));

		LogUtil.debug(_log, userExpandoMappings);

		return userExpandoMappings;
	}

	@Override
	public Properties getUserMappings(long ldapServerId, long companyId)
		throws Exception {

		String postfix = getPropertyPostfix(ldapServerId);

		Properties userMappings = PropertiesUtil.load(
			PrefsPropsUtil.getString(
				companyId, PropsKeys.LDAP_USER_MAPPINGS + postfix,
				StringPool.BLANK));

		LogUtil.debug(_log, userMappings);

		return userMappings;
	}

	@Override
	public boolean isExportEnabled(long companyId) {
		boolean defaultImportUserPasswordAutogenerated =
			_ldapConfiguration.importUserPasswordAutogenerated();

		if (isImportEnabled(companyId) &&
			defaultImportUserPasswordAutogenerated) {

			return false;
		}

		boolean defaultLDAPExportEnabled = _ldapConfiguration.exportEnabled();

		if (PrefsPropsUtil.getBoolean(
				companyId, PropsKeys.LDAP_EXPORT_ENABLED,
			defaultLDAPExportEnabled)) {

			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public boolean isExportGroupEnabled(long companyId) {
		boolean defaultLDAPExportGroupEnabled =
			_ldapConfiguration.exportGroupEnabled();

		if (PrefsPropsUtil.getBoolean(
				companyId, PropsKeys.LDAP_EXPORT_GROUP_ENABLED,
			defaultLDAPExportGroupEnabled)) {

			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public boolean isImportEnabled(long companyId) {
		boolean defaultLDAPImportEnabled = _ldapConfiguration.importEnabled();

		if (PrefsPropsUtil.getBoolean(
				companyId, PropsKeys.LDAP_IMPORT_ENABLED,
			defaultLDAPImportEnabled)) {

			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public boolean isImportOnStartup(long companyId) {
		if (PrefsPropsUtil.getBoolean(
				companyId, PropsKeys.LDAP_IMPORT_ON_STARTUP,
			_ldapConfiguration.importOnStartup())) {

			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public boolean isPasswordPolicyEnabled(long companyId) {
		boolean defaultLDAPPasswordPolicyEnabled =
			_ldapConfiguration.passwordPolicyEnabled();

		if (PrefsPropsUtil.getBoolean(
				companyId, PropsKeys.LDAP_PASSWORD_POLICY_ENABLED,
			defaultLDAPPasswordPolicyEnabled)) {

			return true;
		}
		else {
			return false;
		}
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_ldapConfiguration = Configurable.createConfigurable(
			LDAPConfiguration.class, properties);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DefaultLDAPSettings.class);

	private volatile LDAPConfiguration _ldapConfiguration;

}
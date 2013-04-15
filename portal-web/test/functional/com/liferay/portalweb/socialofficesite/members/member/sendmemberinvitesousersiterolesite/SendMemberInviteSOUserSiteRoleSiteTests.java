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

package com.liferay.portalweb.socialofficesite.members.member.sendmemberinvitesousersiterolesite;

import com.liferay.portalweb.portal.BaseTestSuite;
import com.liferay.portalweb.socialoffice.users.user.addsouser.AddSOUser1Test;
import com.liferay.portalweb.socialoffice.users.user.addsouser.AddSOUser2Test;
import com.liferay.portalweb.socialoffice.users.user.addsouser.TearDownSOUserTest;
import com.liferay.portalweb.socialoffice.users.user.editsouserpassword.EditSOUser1PasswordTest;
import com.liferay.portalweb.socialoffice.users.user.editsouserpassword.EditSOUser2PasswordTest;
import com.liferay.portalweb.socialoffice.users.user.selectregularrolessouser.SelectRegularRolesSOUser1Test;
import com.liferay.portalweb.socialoffice.users.user.selectregularrolessouser.SelectRegularRolesSOUser2Test;
import com.liferay.portalweb.socialoffice.users.user.signinso.SOUs1_SignInSOTest;
import com.liferay.portalweb.socialoffice.users.user.signinso.SOUs1_SignOutSOTest;
import com.liferay.portalweb.socialoffice.users.user.signinso.SOUs2_SignInSOTest;
import com.liferay.portalweb.socialoffice.users.user.signinso.SOUs2_SignOutSOTest;
import com.liferay.portalweb.socialoffice.users.user.signinso.SignInSOTest;
import com.liferay.portalweb.socialoffice.users.user.signinso.SignOutSOTest;
import com.liferay.portalweb.socialofficehome.sites.site.addsitessite.AddSitesSiteTest;
import com.liferay.portalweb.socialofficehome.sites.site.addsitessite.TearDownSOSitesTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Brian Wing Shun Chan
 */
public class SendMemberInviteSOUserSiteRoleSiteTests extends BaseTestSuite {
	public static Test suite() {
		TestSuite testSuite = new TestSuite();
		testSuite.addTestSuite(AddSOUser1Test.class);
		testSuite.addTestSuite(SelectRegularRolesSOUser1Test.class);
		testSuite.addTestSuite(EditSOUser1PasswordTest.class);
		testSuite.addTestSuite(AddSOUser2Test.class);
		testSuite.addTestSuite(SelectRegularRolesSOUser2Test.class);
		testSuite.addTestSuite(EditSOUser2PasswordTest.class);
		testSuite.addTestSuite(AddSitesSiteTest.class);
		testSuite.addTestSuite(SendMemberInviteSOUser1SiteOwnerRoleSiteTest.class);
		testSuite.addTestSuite(SendMemberInviteSOUser2SiteAdminRoleSiteTest.class);
		testSuite.addTestSuite(SignOutSOTest.class);
		testSuite.addTestSuite(SOUs1_SignInSOTest.class);
		testSuite.addTestSuite(SOUs1_ConfirmMemberInviteSiteOwnerRoleSiteTest.class);
		testSuite.addTestSuite(SOUs1_ViewConfirmMemberInviteSiteOwnerRoleSiteTest.class);
		testSuite.addTestSuite(SOUs1_SignOutSOTest.class);
		testSuite.addTestSuite(SOUs2_SignInSOTest.class);
		testSuite.addTestSuite(SOUs2_ConfirmMemberInviteSiteAdminRoleSiteTest.class);
		testSuite.addTestSuite(SOUs2_ViewConfirmMemberInviteSiteAdminRoleSiteTest.class);
		testSuite.addTestSuite(SOUs2_SignOutSOTest.class);
		testSuite.addTestSuite(SignInSOTest.class);
		testSuite.addTestSuite(TearDownSOUserTest.class);
		testSuite.addTestSuite(TearDownSOSitesTest.class);

		return testSuite;
	}
}
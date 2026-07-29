/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.internal.manager;

import com.liferay.digital.signature.internal.http.DSHttp;
import com.liferay.digital.signature.manager.DSRecipientViewDefinitionManager;
import com.liferay.digital.signature.model.DSRecipientViewDefinition;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;

import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author José Abelenda
 */
@Component(service = DSRecipientViewDefinitionManager.class)
public class DSRecipientViewDefinitionManagerImpl
	implements DSRecipientViewDefinitionManager {

	@Override
	public String addDSRecipientViewDefinition(
			long companyId, long groupId, String dsEnvelopeId,
			DSRecipientViewDefinition dsRecipientViewDefinition)
		throws Exception {

		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		// A company administrator may request any recipient view, and a
		// signed-in user may request the embedded signing ceremony URL for
		// their own recipient view (their email), but no one may request a
		// signing URL on another recipient's behalf.

		if ((permissionChecker == null) ||
			(!permissionChecker.isCompanyAdmin(companyId) &&
			 !_isOwnRecipientView(
				 permissionChecker, dsRecipientViewDefinition))) {

			throw new PrincipalException(
				"User is neither a company administrator nor the recipient");
		}

		JSONObject jsonObject = _dsHttp.post(
			companyId, groupId,
			StringBundler.concat(
				"envelopes/", dsEnvelopeId, "/views/recipient"),
			dsRecipientViewDefinition.toJSONObject());

		return jsonObject.getString("url");
	}

	private boolean _isOwnRecipientView(
		PermissionChecker permissionChecker,
		DSRecipientViewDefinition dsRecipientViewDefinition) {

		User user = permissionChecker.getUser();

		if (user == null) {
			return false;
		}

		return Objects.equals(
			user.getEmailAddress(),
			dsRecipientViewDefinition.getEmailAddress());
	}

	@Reference
	private DSHttp _dsHttp;

}
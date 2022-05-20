/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.search.experiences.internal.database;

import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.search.experiences.internal.model.listener.CompanyModelListener;
import com.liferay.search.experiences.model.SXPElement;
import com.liferay.search.experiences.service.SXPElementLocalService;

import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Shuyang Zhou
 */
@Component(immediate = true, service = {})
public class SXPElementDefaultDataInitializer {

	@Activate
	protected void activate(BundleContext bundleContext) throws Exception {
		_companyLocalService.forEachCompany(
			company -> {
				List<SXPElement> elements =
					_sxpElementLocalService.getSXPElements(
						company.getCompanyId(), true);

				if (elements.isEmpty()) {
					_companyModelListener.addSXPElements(
						company, _sxpElementLocalService);
				}
			});
	}

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private CompanyModelListener _companyModelListener;

	@Reference
	private SXPElementLocalService _sxpElementLocalService;

}
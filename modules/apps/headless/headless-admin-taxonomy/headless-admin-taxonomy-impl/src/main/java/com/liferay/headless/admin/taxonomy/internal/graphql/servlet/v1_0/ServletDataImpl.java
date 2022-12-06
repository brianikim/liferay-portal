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

package com.liferay.headless.admin.taxonomy.internal.graphql.servlet.v1_0;

import com.liferay.headless.admin.taxonomy.internal.graphql.mutation.v1_0.Mutation;
import com.liferay.headless.admin.taxonomy.internal.graphql.query.v1_0.Query;
import com.liferay.headless.admin.taxonomy.internal.resource.v1_0.KeywordResourceImpl;
import com.liferay.headless.admin.taxonomy.internal.resource.v1_0.TaxonomyCategoryResourceImpl;
import com.liferay.headless.admin.taxonomy.internal.resource.v1_0.TaxonomyVocabularyResourceImpl;
import com.liferay.headless.admin.taxonomy.resource.v1_0.KeywordResource;
import com.liferay.headless.admin.taxonomy.resource.v1_0.TaxonomyCategoryResource;
import com.liferay.headless.admin.taxonomy.resource.v1_0.TaxonomyVocabularyResource;
import com.liferay.portal.kernel.util.ObjectValuePair;
import com.liferay.portal.vulcan.graphql.servlet.ServletData;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;

/**
 * @author Javier Gamarra
 * @generated
 */
@Component(service = ServletData.class)
@Generated("")
public class ServletDataImpl implements ServletData {

	@Activate
	public void activate(BundleContext bundleContext) {
		Mutation.setKeywordResourceComponentServiceObjects(
			_keywordResourceComponentServiceObjects);
		Mutation.setTaxonomyCategoryResourceComponentServiceObjects(
			_taxonomyCategoryResourceComponentServiceObjects);
		Mutation.setTaxonomyVocabularyResourceComponentServiceObjects(
			_taxonomyVocabularyResourceComponentServiceObjects);

		Query.setKeywordResourceComponentServiceObjects(
			_keywordResourceComponentServiceObjects);
		Query.setTaxonomyCategoryResourceComponentServiceObjects(
			_taxonomyCategoryResourceComponentServiceObjects);
		Query.setTaxonomyVocabularyResourceComponentServiceObjects(
			_taxonomyVocabularyResourceComponentServiceObjects);
	}

	public String getApplicationName() {
		return "Liferay.Headless.Admin.Taxonomy";
	}

	@Override
	public Mutation getMutation() {
		return new Mutation();
	}

	@Override
	public String getPath() {
		return "/headless-admin-taxonomy-graphql/v1_0";
	}

	@Override
	public Query getQuery() {
		return new Query();
	}

	public ObjectValuePair<Class<?>, String> getResourceMethodObjectValuePair(
		String methodName, boolean mutation) {

		if (mutation) {
			return _resourceMethodObjectValuePairs.get(
				"mutation#" + methodName);
		}

		return _resourceMethodObjectValuePairs.get("query#" + methodName);
	}

	private static final Map<String, ObjectValuePair<Class<?>, String>>
		_resourceMethodObjectValuePairs =
			new HashMap<String, ObjectValuePair<Class<?>, String>>() {
				{
					put(
						"mutation#createAssetLibraryKeyword",
						new ObjectValuePair<>(
							KeywordResourceImpl.class,
							"postAssetLibraryKeyword"));
					put(
						"mutation#createAssetLibraryKeywordBatch",
						new ObjectValuePair<>(
							KeywordResourceImpl.class,
							"postAssetLibraryKeywordBatch"));
					put(
						"mutation#deleteKeyword",
						new ObjectValuePair<>(
							KeywordResourceImpl.class, "deleteKeyword"));
					put(
						"mutation#deleteKeywordBatch",
						new ObjectValuePair<>(
							KeywordResourceImpl.class, "deleteKeywordBatch"));
					put(
						"mutation#updateKeyword",
						new ObjectValuePair<>(
							KeywordResourceImpl.class, "putKeyword"));
					put(
						"mutation#updateKeywordBatch",
						new ObjectValuePair<>(
							KeywordResourceImpl.class, "putKeywordBatch"));
					put(
						"mutation#createSiteKeyword",
						new ObjectValuePair<>(
							KeywordResourceImpl.class, "postSiteKeyword"));
					put(
						"mutation#createSiteKeywordBatch",
						new ObjectValuePair<>(
							KeywordResourceImpl.class, "postSiteKeywordBatch"));
					put(
						"mutation#createTaxonomyCategoryTaxonomyCategory",
						new ObjectValuePair<>(
							TaxonomyCategoryResourceImpl.class,
							"postTaxonomyCategoryTaxonomyCategory"));
					put(
						"mutation#deleteTaxonomyCategory",
						new ObjectValuePair<>(
							TaxonomyCategoryResourceImpl.class,
							"deleteTaxonomyCategory"));
					put(
						"mutation#deleteTaxonomyCategoryBatch",
						new ObjectValuePair<>(
							TaxonomyCategoryResourceImpl.class,
							"deleteTaxonomyCategoryBatch"));
					put(
						"mutation#patchTaxonomyCategory",
						new ObjectValuePair<>(
							TaxonomyCategoryResourceImpl.class,
							"patchTaxonomyCategory"));
					put(
						"mutation#updateTaxonomyCategory",
						new ObjectValuePair<>(
							TaxonomyCategoryResourceImpl.class,
							"putTaxonomyCategory"));
					put(
						"mutation#updateTaxonomyCategoryBatch",
						new ObjectValuePair<>(
							TaxonomyCategoryResourceImpl.class,
							"putTaxonomyCategoryBatch"));
					put(
						"mutation#createTaxonomyVocabularyTaxonomyCategory",
						new ObjectValuePair<>(
							TaxonomyCategoryResourceImpl.class,
							"postTaxonomyVocabularyTaxonomyCategory"));
					put(
						"mutation#createTaxonomyVocabularyTaxonomyCategoryBatch",
						new ObjectValuePair<>(
							TaxonomyCategoryResourceImpl.class,
							"postTaxonomyVocabularyTaxonomyCategoryBatch"));
					put(
						"mutation#createAssetLibraryTaxonomyVocabulary",
						new ObjectValuePair<>(
							TaxonomyVocabularyResourceImpl.class,
							"postAssetLibraryTaxonomyVocabulary"));
					put(
						"mutation#createAssetLibraryTaxonomyVocabularyBatch",
						new ObjectValuePair<>(
							TaxonomyVocabularyResourceImpl.class,
							"postAssetLibraryTaxonomyVocabularyBatch"));
					put(
						"mutation#createSiteTaxonomyVocabulary",
						new ObjectValuePair<>(
							TaxonomyVocabularyResourceImpl.class,
							"postSiteTaxonomyVocabulary"));
					put(
						"mutation#createSiteTaxonomyVocabularyBatch",
						new ObjectValuePair<>(
							TaxonomyVocabularyResourceImpl.class,
							"postSiteTaxonomyVocabularyBatch"));
					put(
						"mutation#deleteTaxonomyVocabulary",
						new ObjectValuePair<>(
							TaxonomyVocabularyResourceImpl.class,
							"deleteTaxonomyVocabulary"));
					put(
						"mutation#deleteTaxonomyVocabularyBatch",
						new ObjectValuePair<>(
							TaxonomyVocabularyResourceImpl.class,
							"deleteTaxonomyVocabularyBatch"));
					put(
						"mutation#patchTaxonomyVocabulary",
						new ObjectValuePair<>(
							TaxonomyVocabularyResourceImpl.class,
							"patchTaxonomyVocabulary"));
					put(
						"mutation#updateTaxonomyVocabulary",
						new ObjectValuePair<>(
							TaxonomyVocabularyResourceImpl.class,
							"putTaxonomyVocabulary"));
					put(
						"mutation#updateTaxonomyVocabularyBatch",
						new ObjectValuePair<>(
							TaxonomyVocabularyResourceImpl.class,
							"putTaxonomyVocabularyBatch"));

					put(
						"query#assetLibraryKeywords",
						new ObjectValuePair<>(
							KeywordResourceImpl.class,
							"getAssetLibraryKeywordsPage"));
					put(
						"query#keywordsRanked",
						new ObjectValuePair<>(
							KeywordResourceImpl.class,
							"getKeywordsRankedPage"));
					put(
						"query#keyword",
						new ObjectValuePair<>(
							KeywordResourceImpl.class, "getKeyword"));
					put(
						"query#keywords",
						new ObjectValuePair<>(
							KeywordResourceImpl.class, "getSiteKeywordsPage"));
					put(
						"query#taxonomyCategoryRanked",
						new ObjectValuePair<>(
							TaxonomyCategoryResourceImpl.class,
							"getTaxonomyCategoryRankedPage"));
					put(
						"query#taxonomyCategoryTaxonomyCategories",
						new ObjectValuePair<>(
							TaxonomyCategoryResourceImpl.class,
							"getTaxonomyCategoryTaxonomyCategoriesPage"));
					put(
						"query#taxonomyCategory",
						new ObjectValuePair<>(
							TaxonomyCategoryResourceImpl.class,
							"getTaxonomyCategory"));
					put(
						"query#taxonomyVocabularyTaxonomyCategories",
						new ObjectValuePair<>(
							TaxonomyCategoryResourceImpl.class,
							"getTaxonomyVocabularyTaxonomyCategoriesPage"));
					put(
						"query#assetLibraryTaxonomyVocabularies",
						new ObjectValuePair<>(
							TaxonomyVocabularyResourceImpl.class,
							"getAssetLibraryTaxonomyVocabulariesPage"));
					put(
						"query#taxonomyVocabularies",
						new ObjectValuePair<>(
							TaxonomyVocabularyResourceImpl.class,
							"getSiteTaxonomyVocabulariesPage"));
					put(
						"query#taxonomyVocabulary",
						new ObjectValuePair<>(
							TaxonomyVocabularyResourceImpl.class,
							"getTaxonomyVocabulary"));
				}
			};

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<KeywordResource>
		_keywordResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<TaxonomyCategoryResource>
		_taxonomyCategoryResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<TaxonomyVocabularyResource>
		_taxonomyVocabularyResourceComponentServiceObjects;

}
# Remaining Poshi Tests

After the 32 groups, 220 Poshi tests remain in `modules/apps/commerce/commerce-product-test/src/testFunctional/tests/`. This file records why each one is still there, so nobody repeats work that was already evaluated or duplicates work that another open pull request already owns.

## Summary

- 56 tests are owned by open pull requests on liferay-commerce/liferay-portal. Do not migrate them on this branch.
- 31 tests were attempted and deliberately left in place. The reasons are listed below.
- The remaining 133 tests have not been attempted. The earlier classification placed most of them in the Medium tier, with a few that need a Minium site or are hard to migrate.

Several entries are empty `@ignore` placeholders with only a TODO comment. They qualify as obsolete under the deletion rule in `HANDOVER.md`, so they can be removed in a `Drop obsolete <subject> test` commit once the team agrees on that title pattern.

The open pull request list changes over time. Run `check_overlaps.py` for the current state before starting any new migration.

## Attempted and Left in Place

| Poshi Test | Reason |
| --- | --- |
| CPCommerceChannels::CannotSelectShippingMethodEligibilitiesTwice | The 409 'shipping fixed option qualifier already exists' message comes from the headless admin-channel mapper, but ShippingFixedOptionOrderTypeResourceTest and ShippingFixedOptionTermResourceTest are @Ignore at class level, so a faithful test has no class that runs; putting it in TermResourceTest would break the rule that a test lives in its subject's class. |
| CPCommerceProductOptions::CanUseTheAccordionForNormalProducts | The only test that fills every option type sits on the Minium bundled setup, and a faithful Minium-free copy would duplicate that entire option and product details flow, which was out of scope for this pass. |
| CPCommerceProducts::VerifyIfCategorizationIsWorking | This is an @ignore stub with an empty body (TODO COMMERCE-7075) and no assertions or description to migrate. It was left for a separate decision because no removal-only commit pattern had been agreed. |
| CPCommerceSettings::CreateNewMeasurementUnits | Saving the new dimension and weight units as Primary removes the Primary flag from the company's existing units (CPMeasurementUnitLocalServiceImpl._validate), and there is no API helper to restore them, so the test would leave shared company state changed for parallel specs. |
| CommerceAccelerators::FragmentsCanBeDragAndDroppedToMiniumSitePage | It checks that fragments render on a Minium site page under the Minium theme, so it genuinely needs the Minium site and theme. |
| CommerceAccelerators::InitializeNewMiniumSite | Running MiniumSiteInitializer in the shared commerce-test integration bundle seeds company-scoped data (accounts, users, organizations, options, specifications, price lists, warehouses) that cannot be cleaned up and would pollute other integration tests; there is no precedent for running it in an isolated company. |
| CommerceAccelerators::InitializeNewSpeedwellSite | Same issue as the Minium case: SpeedwellSiteInitializer seeds company-scoped data that the shared integration bundle cannot clean up, and there is no isolated-company precedent. |
| CommerceAccelerators::MiniumSiteFullFlow | It verifies the Minium accelerator site end to end, so it genuinely needs the Minium site. |
| CommerceAccelerators::MiniumSiteFullFlowCheckingOutViaOrderDetails | It verifies the Minium accelerator site end to end, so it genuinely needs the Minium site. |
| CommerceAccountManagement::CanAssertImpersonatingOnMiniumMaintainsContext | It asserts the user name in the Minium theme sidebar across impersonation, so it genuinely needs the Minium site and theme, and its only planned home calls miniumSetUp. |
| CommerceAccountSelector::CanChangeActiveInFlowAccountFromAccountsList | The storefront taglib always renders AccountSelector with refreshPageOnAccountSelected=true, so the updated selector head after switching accounts depends on the server persisting setCurrentAccounts and re-rendering after a reload, which Jest cannot assert; the in-component path without a reload also crashes (see risks), so a Jest version would not be faithful. |
| CommerceCategoryDisplayPages::CanGuestUserViewTheCategoriesNavigationWidgetWithoutErrors | Its main assertion is AssertConsoleTextNotPresent on the server log (AssetVocabulary VIEW permission error), which Playwright cannot check, so migrating only the on-page error check would drop that assertion. |
| CommerceCategoryDisplayPages::CanSelectChangeAndRemoveOverrideDefaultCategoryDisplayPage | The LPD-3381 target builds the Minium site through the minium-initializer, and a Minium-free rewrite needs the global category item selector flow, which no Minium-free spec exercises to copy from. |
| CommerceCompare::CanRemoveComparisonItemFromComparePage | The compare-checkbox taglib serializes itemId as a number, and CompareCheckbox's refreshOnRemove branch only removes string ids from the compare cookie. A Jest probe with a numeric id left the cookie at '1:2:3', so a faithful test fails. This looks like a product bug that needs triage or a live run. |
| CommerceCompare::EditMaxNumberOfComparisonItemsInComparePage | On an API site I found no verified way to select specific products for comparison. The product publisher lists every product visible to the channel (the manual catalogEntryXml selection preference is unverified), the classic product details page has no compare checkbox, and the mini compare Compare URL depends on getPlidFromPortletId finding a content page. |
| CommerceCompare::RemoveComparisonItemFromMiniCompare | The MiniCompare delete button has the same numeric-id problem. A Jest probe with production-typed (numeric) ids kept the removed id in the cookie ('1:2'), so the Poshi assertion that the product is gone from the compare page cannot be migrated faithfully. |
| CommerceCompare::ViewProductOptionsAndSpecificationsWhenComparingItemsInComparePage | Same blocker as the products limit test: there is no verified API-site path for choosing which products go on the compare page. |
| CommercePriceOnApplication::CanPreventPriceOnApplicationProductBeDirectlyCheckoutTogetherWithANormalProduct | Its quote submission journey (Request a Quote, pending order quote form, redirect to Placed Orders, Quote Requested status) was not reproduced, and salesAgent.spec.ts only possibly covers the quote flow, which does not meet the verified-coverage bar for a drop. |
| CommercePriceOnApplication::CanViewPriceOnApplicationLabelOnStoreFront | Request a Quote redirects to the order display page, which an API site only has after building a default Order display page template in the page editor. The only Minium-free precedent (the pendingOrder.spec request-quote test) does not render order item unit prices there, so the final Price on Application and $15.00 unit price assertions have no verified target. |
| CommerceProductDetails::UseADTInProductDetailsWidget | This @ignore placeholder has only a TODO comment and no steps or assertions, so there is nothing to migrate and it was left for a separate decision because no removal-only commit pattern had been agreed. |
| CommerceProductDetails::UseCustomRenderersInProductDetails | This @ignore placeholder has only a TODO comment and no steps or assertions, so there is nothing to migrate and it was left for a separate decision because no removal-only commit pattern had been agreed. |
| CommerceQuickAddToCart::CanUseQuickAddToCartWhenOneProductHasExpiredSKU | The test also asserts the products admin SKU status labels after the delete (Expired shown, Approved absent), which Jest cannot reproduce; the client-side part (a product with no SKUs next to a searchable SKU) is already a row of the new Jest results loop. |
| CommerceSearch::CanGlobalSearchViewSearchSuggestionsWithLanguageOverride | It exercises the Minium theme global search bar suggestions, so it genuinely needs the Minium site and theme. |
| CommerceSearch::GlobalSearchSearchEntryInAccounts | Empty @ignore placeholder (TODO COMMERCE-6323) with no steps or assertions, so nothing can be migrated and it was left for a separate decision because no removal-only commit pattern had been agreed. |
| CommerceSearch::GlobalSearchSearchEntryInOrders | Empty @ignore placeholder (TODO COMMERCE-6325) with no steps or assertions, so nothing can be migrated and it was left for a separate decision because no removal-only commit pattern had been agreed. |
| CommerceSearch::GlobalSearchSuggestedAccountsEntry | Empty @ignore placeholder (TODO Review) with no steps or assertions, so nothing can be migrated and it was left for a separate decision because no removal-only commit pattern had been agreed. |
| CommerceSearch::SearchBarWidgetSearchProductByName | Every candidate spec uses miniumSetUp, and its exact result counts depend on the Minium catalog, which cannot be made deterministic on a shared index with a Minium-free catalog. |
| CommerceSearch::SearchBarWidgetSearchProductByNameDoubleQuotes | Its only planned coverage is the Minium guest search test, which calls miniumSetUp, so it does not qualify as verified coverage for a drop. |
| CommerceSearch::SearchBarWidgetSearchProductByNameSingleQuotes | The target guest search test calls miniumSetUp and the relevance and count assertions rely on the Minium catalog. |
| GuestCheckout::GuestCheckoutDisabled | Its target test uses classicCommerceSetUp, which is forbidden, and guest catalog visibility of API-created products has no Minium-free and initializer-free precedent to follow. |
| GuestCheckout::GuestCheckoutEnabledWithoutAuthenticationPage | The only candidate coverage is an existing classicCommerceSetUp test, which does not meet the verified-coverage bar for a drop. |

## Owned by Open Pull Requests

| Poshi Test | Pull Request |
| --- | --- |
| CPCommerceSettings::CanViewLanguageTagsForCategoryOnSitemapPage | #8185 |
| CPCommerceSettings::CanViewSubcategoryForCategoryOnSitemapPage | #8185 |
| CommerceMiniCartEditUnitsOfMeasure::CanAssertPricingTypesAreAppliedWhileEditingUOMInMiniCart | #8192 |
| CommerceMiniCartEditUnitsOfMeasure::CanEditUOMInMiniCart | #8192 |
| CommerceMiniCartEditUnitsOfMeasure::CanVerifyPriceTableWhileEditingUOMInMiniCart | #8192 |
| CommerceMiniCartEditUnitsOfMeasure::CannotChooseUOMWithoutStockWhileEditingUOMInMiniCart | #8192 |
| CommercePaymentMethodsPermissions::UsersWithoutManagePermissionSkipsPaymentMethodStepDuringCheckout | #8195 |
| CommerceProductCard::AddProductToWishListFromProductCard | #7802 |
| CommerceProductCard::AssertOnlyAllowedQuantitiesAreSelectable | #7802 |
| CommerceProductCard::AssertOnlyMultipleValuesAreSelectableQuantities | #7802 |
| CommerceProductCard::AssertProductCardShowsCatalogDefaultImage | #7802 |
| CommerceProductCard::AssertProductCardShowsCustomImageIfSet | #7802 |
| CommerceProductCard::AssertProductIsAvailable | #7802 |
| CommerceProductCard::AssertProductIsUnavailable | #7802 |
| CommerceProductCard::AssertProductNameIsTranslated | #7802 |
| CommerceProductCard::AssertQuantityCanBeSelectedUpToAMaximumQuantitySet | #7802 |
| CommerceProductCard::AssertSelectableQuantitiesStartFromMinimumQuantitySet | #7802 |
| CommerceProductCard::AssertViewAllVariantsButtonRedirectsToProductDetailsPage | #7802 |
| CommerceProductCard::CanAddToCartSingleSkuDynamicPriceBundledProductFromProductCard | #7802 |
| CommerceProductCard::CanAddToCartSingleSkuStaticPriceBundledProductFromProductCard | #7802 |
| CommerceProductCard::CanViewOnlyCorrectAllowedQuantitiesAreSelectableForProductCard | #7802 |
| CommerceProductCard::CannotAddToCartMultipleSkuDynamicPriceBundledProductFromProductCard | #7802 |
| CommerceProductCard::CannotAddToCartMultipleSkuStaticPriceBundledProductFromProductCard | #7802 |
| CommerceProductCard::CompareProductFromProductCard | #7802 |
| CommerceProductCard::RemoveProductFromComparisonItems | #7802 |
| CommerceProductCard::RemoveProductFromWishList | #7802 |
| CommerceProductCard::ViewFirstSelectableQuantityWhenMinimumOrderQuantityIsLowerThanMultipleOrderQuantity | #7802 |
| CommerceProductDetails::AssertOnlyAllowedQuantitiesAreSelectableInProductDetails | #8174 |
| CommerceProductDetails::AssertOnlyMultipleValuesAreSelectableQuantitiesInProductDetails | #8174 |
| CommerceProductDetails::AssertProductCanBeAddedToCartFromProductDetailsIfBackOrderIsEnabled | #8174 |
| CommerceProductDetails::AssertProductCannotBeAddedToCartFromProductDetailsIfProductIsNotPurchasable | #8174 |
| CommerceProductDetails::AssertQuantityInProductDetailsCanBeSelectedUpToAMaximumQuantitySet | #8174 |
| CommerceProductDetails::AssertSelectableQuantitiesInProductDetailsStartFromMinimumQuantitySet | #8174 |
| CommerceProductDetails::CanAssertFullDescriptionTabNotPresent | #8174 |
| CommerceProductDetails::CanAssertMultipleSkusProductInvalidAllowedOrderQuantity | #8174 |
| CommerceProductDetails::CanAssertMultipleSkusProductInvalidMaximumOrderQuantity | #8174 |
| CommerceProductDetails::CanAssertMultipleSkusProductInvalidMinimumOrderQuantity | #8174 |
| CommerceProductDetails::CanAssertMultipleSkusProductInvalidMultipleOrderQuantity | #8174 |
| CommerceProductDetails::CanAssertMultipleSkusProductMaximumMultipleCombinationInvalidQuantities | #8174 |
| CommerceProductDetails::CanAssertMultipleSkusProductMinimumMultipleCombinationInvalidQuantities | #8174 |
| CommerceProductDetails::CannotAddToCartOptionWhenStockIsZeroAndBackOrderIsDisabled | #8174 |
| CommerceQuickAddToCart::CanRemoveQuickAddToCartChips | #8195 |
| CommerceUnitsOfMeasure::CanAddSKUUOMsAndVerifyInfoOnDiscountPage | #8192 |
| CommerceUnitsOfMeasure::CanAddToCartSKUWithMultipleUOMs | #8192 |
| CommerceUnitsOfMeasure::CanAssertQuantitySelectorPopUpUOMErrors | #8192 |
| CommerceUnitsOfMeasure::CanBasePriceBeUpdatedBasedOnTheOneFromPrimaryUOM | #8192 |
| CommerceUnitsOfMeasure::CanBuyerViewOrderItemUOMAndQuantity | #8192 |
| CommerceUnitsOfMeasure::CanOrderManagerViewOrderItemUOMAndQuantity | #8192 |
| CommerceUnitsOfMeasure::CanVerifyHowProductPricingIsAffectedByUOM | #8192 |
| CommerceUnitsOfMeasure::CanVerifyUOMInventoryInfoOnInventoryPage | #8192 |
| CommerceUnitsOfMeasure::CanVerifyUOMInventoryInfoOnSKUPage | #8192 |
| CommerceUnitsOfMeasure::CanViewDefaultPriceInfoOnTablesEntriesWhenNoUOMsAreCreated | #8192 |
| CommerceUnitsOfMeasure::CannotAddToCartUOMWhenStockIsZeroAndBackOrderIsDisabled | #8192 |
| CommerceUnitsOfMeasure::CannotViewPricingInformationFromSKUDetails | #8192 |
| CommerceUnitsOfMeasureBundles::CanAssertUOMBundledProductIsOverlappedUsingNormalAndQuickAdd | #8192 |
| SFCommerceOrders::UseADTInPendingOrdersWidget | #8195 |

## All Remaining Tests by File

Markers: **PR** means an open pull request owns the test, and **Left** means it was attempted and left in place.

### CPCommerceChannels

- CannotSelectShippingMethodEligibilitiesTwice (Left)
- CheckAvalaraTaxPresence
- CompleteCheckoutWithDefaultPaymentMethod
- EditMoneyOrderPaymentMethod
- SearchProductDisplayPage
- UploadCustomJasperPrintTemplate
- UsePunchOut
- ViewAndDownloadCustomJasperPrintTemplate
- ViewOrderPreviewWithJasperTemplate

### CPCommerceProductMedia

- AddAnAttachment
- AddAnImage
- CanAddAndRemoveTagsForAttachments
- CanAddAndRemoveTagsForImages
- CanViewImagesCorrectlyByUsingTheShoppingExperienceImageGalleryToggle

### CPCommerceProductOptionDefineExternally

- CanApplyDiscountToExternallyDefinedProducts
- CanAssertExternallyDefinedOptionValuesListShowsOnlySkusWithProductVisibilitySet
- CanCheckoutBundledProductWithExternallyDefinedProductOptions
- CanDefineExternallyWithMultipleCategories
- CanManuallySelectedProductOptionConfigurationBeRemembered
- CanProductNotViewItselfInExternallyDefinedOptionValuesList
- CanViewExternallyDefinedProductsListedAccordingToSelectedCategory
- CanViewOnlyValidSKUsAreListedAsExternallyDefinedProducts

### CPCommerceProductOptions

- CanAddToCartGeneratedProductSkus
- CanLinkDifferentCatalogSKUsToOptionValue
- CanLocalizeOptionsInformation
- CanUseTheAccordionForBundledProductsWithUOM
- CanUseTheAccordionForDefineExternallyProducts
- CanUseTheAccordionForNormalProducts (Left)

### CPCommerceProductRelationsWorkflow

- CanCreateNewRelationTypeWithXSS
- CanCreateNewRelationsWithCustomTypeApproveWorkflow
- CanCreateNewRelationsWithCustomTypeRejectWorkflow

### CPCommerceProductReplacements

- CanViewDiscontinuedProductInformationOnProductCard
- CreateNewSKUWithReplacement
- SetProductAsDiscontinued

### CPCommerceProductSKUs

- VerifyIfSaveAndSaveAsDraftButtonsAreNotPresent

### CPCommerceProductSkusVirtualSettingsOverride

- CanDownloadConfiguredVirtualProductWithSKUOverride
- CanDownloadConfiguredVirtualProductWithSKUOverrideAndWithoutPermissions

### CPCommerceProductVersioning

- CanCheckVersionableProductVisibilityOnMiniumCatalogPage

### CPCommerceProducts

- CanCheckApprovedProductWorkflowStatusWithSingleApproverWorkflow
- CanCheckDraftProductWorkflowStatusWithSingleApproverWorkflow
- CanCheckPendingProductWorkflowStatusWithSingleApproverWorkflow
- CanDownloadConfiguredVirtualProduct
- CanDownloadConfiguredVirtualProductWithoutPermissions
- CanTierPriceSettingsDoesNotChangeAfterCreatingATierPriceEntry
- CanUploadProductImageViaAPIUsingCDN
- ConfigureGroupedProduct
- CreateProductBundleWithPriceTypeDynamic
- CreateProductBundleWithPriceTypeStatic
- VerifyIfCategorizationIsWorking (Left)

### CPCommerceSettings

- CanViewLanguageTagsForCategoryOnSitemapPage (PR #8185)
- CanViewSubcategoryForCategoryOnSitemapPage (PR #8185)
- CreateNewMeasurementUnits (Left)

### CommerceAccelerators

- AddToCartButtonAndQuantitySelectorAreDisabledOnB2BScenarioForGuestUser
- AddToCartButtonAndQuantitySelectorAreDisabledOnB2BScenarioForLoggedAdminUser
- AddToCartButtonAndQuantitySelectorAreDisabledOnB2BScenarioForLoggedNonAdminUser
- AddToCartButtonAndQuantitySelectorAreEnabledOnB2CScenarioForGuestUser
- AddToCartButtonAndQuantitySelectorAreEnabledOnB2CScenarioForLoggedUser
- AddToCartButtonAndQuantitySelectorAreEnabledOnB2XScenarioForGuestUser
- AddToCartButtonAndQuantitySelectorAreEnabledOnB2XScenarioForLoggedUser
- CanApplyMiniumThemeToSite
- CanGuestUserViewOrderDetailsInPendingOrdersPage
- CanSaveCommerceCategoriesNavigationRootCategory
- CannotAddToCartAsAccountUserWithoutRoles
- CannotAddToCartWithExistingOrdersAsAccountUserWithoutRoles
- DeactivateCommerceAccount
- DeactivateCommerceAccountAdminLogOut
- FragmentsCanBeDragAndDroppedToMiniumSitePage (Left)
- InitializeNewMiniumSite (Left)
- InitializeNewSpeedwellSite (Left)
- MiniumSiteFullFlow (Left)
- MiniumSiteFullFlowCheckingOutViaOrderDetails (Left)
- SpeedwellSiteFullFlow

### CommerceAccountManagement

- CanAssertImpersonatingOnMiniumMaintainsContext (Left)
- RemoveAddressFromAccount
- RemoveUserFromAccount

### CommerceAccountSelector

- AssertActiveAccountIsVisibleOnAccountSelectorBar
- AssertActiveAccountIsVisibleOnAccountSelectorDropdown
- CanChangeActiveInFlowAccountFromAccountsList (Left)
- CanCreateNewAccountInFlow

### CommerceAddressesEligibility

- CanCreateAChannelDefaultWithEligibleAddressesAndCompleteTheCheckout
- CanNotUseIllegibleAddressesOnCheckout
- CanNotUseIllegibleAddressesOnCheckoutWithAccountDefault
- CanNotUseIllegibleAddressesOnCheckoutWithChannelDefault
- CanUseAnIneligibleAddressForReorder
- CanUseEligibleAddressesOnCheckout
- CanUseEligibleAddressesOnCheckoutWithAccountDefault

### CommerceCategoryDisplayPages

- CanCategoryManagerAccessCategoriesAdminMenu
- CanGuestUserViewTheCategoriesNavigationWidgetWithoutErrors (Left)
- CanSee404UtilityPagesWorkOnMiniumSite
- CanSelectChangeAndRemoveDefaultCategoryDisplayPage
- CanSelectChangeAndRemoveOverrideDefaultCategoryDisplayPage (Left)
- CanViewSamePageAfterPublishingAnImageForACategory
- OverrideDefaultCategoryDisplayPageForDifferentCategories
- OverridePrivateDefaultCategoryDisplayPageWithAPrivatePage
- OverridePrivateDefaultCategoryDisplayPageWithAPublicPage
- OverridePublicDefaultCategoryDisplayPageWithAPrivatePage
- OverridePublicDefaultCategoryDisplayPageWithAPublicPage
- ViewDefaultCategoryDisplayPageWithPrivateWidgetAndContentPages
- ViewDefaultCategoryDisplayPageWithPublicWidgetAndContentPages

### CommerceCompare

- AddComparisonItemFromProductDetailsPage
- AssertComparisonItemsAreVisibleOnMiniCompare
- CanRemoveComparisonItemFromComparePage (Left)
- EditMaxNumberOfComparisonItemsInComparePage (Left)
- RemoveComparisonItemFromMiniCompare (Left)
- RemoveComparisonItemFromProductDetailsPage
- ViewProductOptionsAndSpecificationsWhenComparingItemsInComparePage (Left)

### CommerceDisplayPageTemplates

- CanAssertProductAvailabilityUsingAvailabilityFragment
- CanDefaultCommerceProductDPTBeOverriddenByOverrideDefaultProductDisplayPageSet
- CanDefaultCommerceProductDPTOverrideDefaultProductDisplayPageSet
- CanDisplayRelatedProductsWithProductCardListItemStyle
- CanMapRelatedProductsToCollectionDisplayFragment
- CanSpecificProductDPTOverrideDefaultDPT
- CanSpecificProductDPTSetOverrideDefaultProductDisplayPage
- CanSpecificProductViewDPTSet
- CanViewProductImagesUsingImageGalleryFragment

### CommerceFacets

- CannotAddSpecificationKeyWithDots

### CommerceMiniCartEditUnitsOfMeasure

- CanAssertPricingTypesAreAppliedWhileEditingUOMInMiniCart (PR #8192)
- CanEditUOMInMiniCart (PR #8192)
- CanVerifyPriceTableWhileEditingUOMInMiniCart (PR #8192)
- CannotChooseUOMWithoutStockWhileEditingUOMInMiniCart (PR #8192)

### CommercePaymentMethodsPermissions

- CanAssignChannelPaymentMethodPermissionsPermission
- CanSFUsersSelectPaymentMethodsDuringCheckoutBasedOnViewPermissionForEachPaymentMethod
- CanUsersSelectPaymentMethodsBasedOnViewPermissionForEachPaymentMethod
- UsersWithManagePermissionCanSelectPaymentMethodDuringCheckout
- UsersWithoutManagePermissionSkipsPaymentMethodStepDuringCheckout (PR #8195)

### CommercePriceOnApplication

- CanPreventPriceOnApplicationProductBeDirectlyCheckoutTogetherWithANormalProduct (Left)
- CanViewPriceOnApplicationLabelOnStoreFront (Left)

### CommerceProductBundleRules

- CanVerifyIncludedAndExcludedOptionValuesAreDisabledWithReasonMessageWithoutSKUContributor
- CanVerifyIncludedAndExcludedOptionValuesOnlySelectableOptionsShown
- CanVerifyIncludedAndExcludedOptionValuesOnlySelectableOptionsShownWithoutSKUContributor
- CanVerifyIncludedAndExcludedOptionValuesUsingFragments
- CanVerifyProductsLimitRule

### CommerceProductCard

- AddProductToWishListFromProductCard (PR #7802)
- AssertOnlyAllowedQuantitiesAreSelectable (PR #7802)
- AssertOnlyMultipleValuesAreSelectableQuantities (PR #7802)
- AssertProductCardShowsCatalogDefaultImage (PR #7802)
- AssertProductCardShowsCustomImageIfSet (PR #7802)
- AssertProductIsAvailable (PR #7802)
- AssertProductIsUnavailable (PR #7802)
- AssertProductNameIsTranslated (PR #7802)
- AssertQuantityCanBeSelectedUpToAMaximumQuantitySet (PR #7802)
- AssertSelectableQuantitiesStartFromMinimumQuantitySet (PR #7802)
- AssertViewAllVariantsButtonRedirectsToProductDetailsPage (PR #7802)
- CanAddToCartSingleSkuDynamicPriceBundledProductFromProductCard (PR #7802)
- CanAddToCartSingleSkuStaticPriceBundledProductFromProductCard (PR #7802)
- CanViewOnlyCorrectAllowedQuantitiesAreSelectableForProductCard (PR #7802)
- CannotAddToCartMultipleSkuDynamicPriceBundledProductFromProductCard (PR #7802)
- CannotAddToCartMultipleSkuStaticPriceBundledProductFromProductCard (PR #7802)
- CompareProductFromProductCard (PR #7802)
- RemoveProductFromComparisonItems (PR #7802)
- RemoveProductFromWishList (PR #7802)
- ViewFirstSelectableQuantityWhenMinimumOrderQuantityIsLowerThanMultipleOrderQuantity (PR #7802)

### CommerceProductDetails

- AddProductToCartFromProductDetails
- AssertOnlyAllowedQuantitiesAreSelectableInProductDetails (PR #8174)
- AssertOnlyMultipleValuesAreSelectableQuantitiesInProductDetails (PR #8174)
- AssertProductCanBeAddedToCartFromProductDetailsIfBackOrderIsEnabled (PR #8174)
- AssertProductCannotBeAddedToCartFromProductDetailsIfProductIsNotPurchasable (PR #8174)
- AssertQuantityInProductDetailsCanBeSelectedUpToAMaximumQuantitySet (PR #8174)
- AssertSelectableQuantitiesInProductDetailsStartFromMinimumQuantitySet (PR #8174)
- CanAssertFullDescriptionTabNotPresent (PR #8174)
- CanAssertMultipleSkusProductInvalidAllowedOrderQuantity (PR #8174)
- CanAssertMultipleSkusProductInvalidMaximumOrderQuantity (PR #8174)
- CanAssertMultipleSkusProductInvalidMinimumOrderQuantity (PR #8174)
- CanAssertMultipleSkusProductInvalidMultipleOrderQuantity (PR #8174)
- CanAssertMultipleSkusProductMaximumMultipleCombinationInvalidQuantities (PR #8174)
- CanAssertMultipleSkusProductMinimumMultipleCombinationInvalidQuantities (PR #8174)
- CanViewOnlyCorrectAllowedQuantitiesAreSelectableInProductDetails
- CanViewSingleSkuGroupedProductDetailPage
- CannotAddToCartOptionWhenStockIsZeroAndBackOrderIsDisabled (PR #8174)
- CannotExecuteXSSWithAvailabilityEstimatesInProductDetails
- SelectOptionValueAndAddProductToCart
- SelectOptionValueAndAssertBundledProductDetailsAreUpdated
- SelectOptionValueAndAssertProductPriceIsUpdated
- UseADTInProductDetailsWidget (Left)
- UseCustomRenderersInProductDetails (Left)
- ViewProductDetailsPriceWithPromotion
- ViewProductDetailsWidget

### CommerceProductDisplayPages

- CanSelectChangeAndRemoveDefaultProductDisplayPage
- CanSelectChangeAndRemoveOverrideDefaultProductDisplayPage
- OverrideDefaultProductDisplayPageForDifferentProducts
- OverridePrivateDefaultProductDisplayPageWithAPrivatePage
- OverridePrivateDefaultProductDisplayPageWithAPublicPage
- OverridePublicDefaultProductDisplayPageWithAPrivatePage
- OverridePublicDefaultProductDisplayPageWithAPublicPage
- ViewDefaultProductDisplayPageWithPrivateContentAndWidgetPages
- ViewDefaultProductDisplayPageWithPublicContentAndWidgetPages

### CommerceQuickAddToCart

- CanRemoveQuickAddToCartChips (PR #8195)
- CanUseQuickAddToCartWhenOneProductHasExpiredSKU (Left)

### CommerceSearch

- CanGlobalSearchViewSearchSuggestionsWithLanguageOverride (Left)
- CanUserViewGlobalSearchSuggestionsInSetLanguage
- GlobalSearchSearchEntryInAccounts (Left)
- GlobalSearchSearchEntryInOrders (Left)
- GlobalSearchSuggestedAccountsEntry (Left)
- SearchBarWidgetSearchProductByName (Left)
- SearchBarWidgetSearchProductByNameDoubleQuotes (Left)
- SearchBarWidgetSearchProductByNameSingleQuotes (Left)
- UseADTInSearchResultsWidget

### CommerceUnitsOfMeasure

- CanAddSKUUOMsAndVerifyInfoOnDiscountPage (PR #8192)
- CanAddToCartSKUWithMultipleUOMs (PR #8192)
- CanAssertQuantitySelectorPopUpUOMErrors (PR #8192)
- CanBasePriceBeUpdatedBasedOnTheOneFromPrimaryUOM (PR #8192)
- CanBuyerViewOrderItemUOMAndQuantity (PR #8192)
- CanOrderManagerViewOrderItemUOMAndQuantity (PR #8192)
- CanVerifyHowProductPricingIsAffectedByUOM (PR #8192)
- CanVerifyUOMInventoryInfoOnInventoryPage (PR #8192)
- CanVerifyUOMInventoryInfoOnSKUPage (PR #8192)
- CanViewDefaultPriceInfoOnTablesEntriesWhenNoUOMsAreCreated (PR #8192)
- CannotAddToCartUOMWhenStockIsZeroAndBackOrderIsDisabled (PR #8192)
- CannotViewPricingInformationFromSKUDetails (PR #8192)

### CommerceUnitsOfMeasureBundles

- CanAssertUOMBundledProductIsOverlappedUsingNormalAndQuickAdd (PR #8192)

### CommerceUpgrade

- ViewCommerceAppsAfterUpgrade7310

### CommerceVirtualProducts

- CanDownloadVirtualProductWhenActivationStatusPending

### GuestCheckout

- AssertGuestCartIsTransferredToAccount
- CanGuestUserCreateQuoteRequestOnMinium
- CanGuestUserCreateQuoteRequestOnSpeedwell
- CanLoggedUserNotInheritGuestAddress
- GuestCheckoutDisabled (Left)
- GuestCheckoutEnabledWithoutAuthenticationPage (Left)

### SFCommerceOrders

- EditOrderFromPendingOrderDetailsPage
- UseADTInPendingOrdersWidget (PR #8195)
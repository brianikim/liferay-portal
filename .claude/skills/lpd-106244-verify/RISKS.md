# Known Risks

None of the tests on this branch has been run against a bundle. The migrations were written by reading Poshi sources, JSPs, React components and Java services, and each author recorded the assumptions that only a real run can confirm. This file collects those notes so the verifier knows where to look first when a test fails.

Every item is a hypothesis to check, not a known defect. When a run confirms or disproves one, record the result in the group report.

## Assumptions Common to Every Group

- Most Playwright locators were chosen by reading source, not by inspecting a rendered page. Treat any locator timeout as a locator problem first and a product problem second.

- Playwright tests build their storefront through `apiStorefrontSetUp` (API site, channel, catalog and a back-orderable product priced at 10). Tests that assert prices or availability depend on that setup.

- During review, several assertions that were always true (image `src` checks, cart quantity cells and similar) were tightened to assert specific values. Confirm that the tightened values match what the page renders.

- Java tests were compiled and the TypeScript was type-checked (`tsc --noEmit`), but no integration, JUnit or Playwright test was executed. Jest was executed only for `CartQuickAdd.js`.

## Batch 1: Java Integration and JUnit (Groups 2, 5, 6, 8, 9, 11 to 15, 17, 18, 22)

### Product Definition Services (Group 12)

- `testCloneCPDefinition` assumes the default locale is en_US. It builds the expected name as "Copy of " plus the name, and the expected URL as `FriendlyURLNormalizerUtil.normalize(name)`. The service uses `normalizeWithEncoding`, which gives the same result for alphanumeric names.

- `_assertCloneCPDefinition` re-reads the clone with `getCPDefinition` and reads names through `getCPDefinitionNameMap`, because the model returned by `cloneCPDefinition` still holds the source's name map.

- The draft-under-workflow check relies on `addCPDefinition` forcing `ACTION_SAVE_DRAFT` when a `WorkflowDefinitionLink` exists for the catalog group, and assumes `getWorkflowTaskCountByUserRoles` is unchanged by adding a draft product.

### Product Search (Groups 13 and 28)

- The two-SKU search depends on the `skus` field being text with a whitespace analyzer (`additional-type-mappings.json`), so the match query combines the tokens with OR.

- The suffix search (`sku.substring(3)`) depends on the `skus.1_10_ngram` term query or the `skus.reverse` phrase prefix.

- The two-category-name search depends on `assetCategoryNames.text` matching either token.

- `testSearchCPDefinitionsByStatus` creates an approved product and then sets it to DRAFT through `updateStatus`, because no `CPTestUtil` method creates a draft inside a given catalog group.

- The grouped-entry search relies on the `@Indexable` reindex on `addCPDefinitionGroupedEntry` being visible to an immediate search.

### Options, SKUs, Channels and Media (Groups 6, 8, 12, 14 and 17)

- `CPAttachmentFileEntryLocalServiceTest` sets expando attributes through the service context. `ExpandoBridgeImpl.setAttributes` checks write permission by default, and the class runs in a `CompanyTestUtil` company with the omniadmin permission checker. If that check fails, the expando assertion fails. `addDefaultTable` assumes no custom fields table exists yet for `CPAttachmentFileEntry` in the new company.

- `CommerceChannelLocalServiceTest.testSearch` searches the exact mixed-case random name. If keyword matching is case sensitive or the channel indexer is not synchronous, the first assertion could fail.

- `testValidateStaticLinkedCPDefinitionOptionValueRelWithUOM` assumes the random option value is the only value linked to its child SKU, which is true in `CPTestUtil` today.

### REST Resource Tests (Groups 5, 8, 14, 15 and 17)

- The JPG assertion in `_testPostProductIdImageWithBase64` depends on `MimeTypesUtil.getExtensions("image/jpeg")` returning ".jpg" first. The order is deterministic today but could change with the Tika glob list.

- The CDN variant uses a cdnURL of `http://www.liferay.com/<random>.jpg`, which must pass `Validator.isUrl`.

- The `SkuResourceTest` option deletion check expects `buildCPInstances` to return exactly two instances on the virtual product.

- The Single Approver image assertion expects the image attachment to be approved while the product itself is pending workflow, which is what the Poshi test asserted for COMMERCE-9503.

### Orders, Notifications, Virtual Items, Address and Currency (Groups 8, 11, 18 and 22)

- `CommerceCurrencyLocalServiceTest` asserts the currency name "US Dollar" under `LocaleUtil.US`, which assumes the default company locale is en_US.

- `testGetPendingCommerceOrderByOrderId` and `testGetPlacedCommerceOrderByOrderId` depend on the order search index being synchronous in tests, as the existing partial ID test already does.

- `CommerceVirtualOrderItemLocalServiceTest` relies on auto-unboxing when it passes `Integer` arguments to `VirtualCPTypeTestUtil.addCPDefinitionVirtualSetting`.

## Batches 2 and 3: Jest and Playwright (Groups 1 to 22)

### Jest (Groups 10, 19 and 20)

- There appears to be a latent product defect in `AccountSelector`. When `refreshPageOnAccountSelected` is false, `changeAccount` calls `setCurrentOrder(null)`, and the next render fails at `currentOrder.id` with the toast "Cannot read properties of null (reading 'id')". The storefront avoids it because the taglib passes true. This may deserve its own ticket.

- The quick add Poshi tests asserted server-side cart totals. Jest asserts the per-SKU quantity that `getCorrectedQuantity` sends, and asserts 0 for the cases that add nothing. Server-side merging of repeated adds is not covered at this layer.

- `CartQuickAdd.js` depends on `ClayMultiSelect` rendering the chip as `.label` and the dropdown items as `.dropdown-item`, and it mocks the channel product search and the cart item POST with fetch-mock 5.

- The product details test asserts that the minimum rule is red for quantity 4 (effective minimum 5). This is current behavior, but it is stricter than the Poshi test.

- CI runs Jest through `yarn test`, not the wrapper used during development. Confirm the results match.

### Admin Playwright Tests

- Shipping: deleting a shipping option or a variable rate setting shows a success alert inside the side panel frame, the Priority column is the third cell of the shipping options table, and rows are sorted by priority.

- Product admin: the Name input matches `input[id$="_nameMapAsXML"]`, the Catalog select renders because more than one catalog exists, Save as Draft is a header link, the product list after Back shows the new product with its type and, with versioning on, both Approved and Draft rows. The es-ES locale item is found by `getByRole('menuitem', {name: 'es-ES'})`.

- Subscriptions: clicking the subscription labels leaves the hidden checkboxes checked, and the tab link is named Subscription.

- SKUs: the ERC input matches `input[id$="_externalReferenceCode"]`, the SKU row action Delete removes the SKU and shows an alert, and an admin in a default B2C channel sees the product card price on the Search Results page.

- Add Price modal: fields are labeled Price List and Unit Price, Add Entry creates a second row, the footer Add button is in the main page `.modal-footer`, and the entry side panel shows the currency in `.input-group-text`.

- Virtual products: the sample section trigger is `fieldset#sample > a`, and the sample file controls and the Sample File URL label resolve. A virtual SKU panel has no Shipping Override text.

- Channel visibility: removing the channel VIEW role association takes effect on reload, and the table then shows the Channels empty state.

- Tax engines: Avalara can be toggled without credentials, Active renders as `.label-success`, and the Remote engine has a Configuration tab with two endpoint inputs.

- Payment methods: the eligibility option labels are Specific Payment Terms and No Payment Terms, PayPal can be activated without credentials, and Authorize.Net is absent from the order edit list.

- Pending orders: Add Order with an order type lands on the order view, and the Pending Orders row shows both the order type and Approved.

- The sort XSS test relies on the `/catalog` friendly URL generated from the page title Catalog.

### Storefront Playwright Tests

- Replacements: a new `apiStorefrontSetUp` SKU with no inventory counts as unavailable, so the Mini Cart quick add swaps in the replacement. The discount, promotion and price on application values are $ 5.00, $ 10.00 list with $ 2.00 promotion, Submit disabled and Request A Quote enabled.

- Account selector: in-flow account creation makes the new account current, and the admin account list search filters by the shared random prefix.

- Orders: the order date cell renders only the date after Show Order Create Time is unchecked, the placed order status label is "Order Status" with "Pending", and the imported `placed_orders_template.ftl` renders each order ID inside the Placed Orders portlet.

- Pending order details: the URL contains `/-/pending-order/`, the admin-created cart is listed for the buyer under both accounts, and Delete is in the Actions menu with a native confirm.

- Quick add: partial and leading searches on generated SKU names return the expected SKUs, and checkout goes shipping address, shipping method, then order summary with no payment step.

- Channel details: the Currency select defaults to US Dollar, and the Type tab info box renders "Site".

- Relation workflow: after a rejection, the submitter gets five update tasks with a resubmit transition and then five new review tasks.

- The Media tab row action buttons are named "<title> Actions" in both the images and the attachments tables.

## Medium Tier (Groups 23 to 32)

### Product Workflow, SKUs, Subscriptions and Media (Groups 23 and 24)

- The product header menu is located as `#dropdown-header-container` button "Actions". Convert to Draft and the version replacement prompt are assumed to be native confirm dialogs.

- The Duplicate modal is assumed to have the title "Duplicate", a "Type Here" autocomplete and a "Submit" button, and the copy opens as "Copy of <name>". The statuses "In Recycle Bin" and "Any" render as `.workflow-status` text.

- Admin list test: the next arrow is `[data-testid="nextArrow"]` with a "20 Items" button, sort headers are column headers named "Name" and "Modified Date", and the SKU phrase prefix search returns all 21 SKUs.

- The image API test assumes the server can fetch the document content URL from `liferayConfig.environment.baseUrl`.

- Subscription tests wait for the success alert after every publish, because `publish()` skips clicking while an older alert is still visible.

- SKU price modal: the base price list is preselected, and adding two units of measure by API yields four catalog base rows.

- Tier prices: the tier price modal opens at the top level, deletes show no confirm, and the storefront card shows 10.00, 20.00, 25.00 and then $ 0.00.

- Channel currency: with the channel set to EUR, the card shows € 0.00 without clearing the currency cookie.

- Adaptive media: the gallery `srcset` contains `/o/adaptive-media/image/` once variants are processed, retried with a reload.

- Versioning: toggling versioning off mid-test relies on one worker. Open PR #8168 (LPD-106110) changes single-draft and Convert to Draft behavior and may change the Incomplete row expectations.

- Integration: `addDefaultTable` for `CPAttachmentFileEntry` may throw `DuplicateTableNameException` if another test left the table behind.

### Price on Application, Quick Add, Mini Cart and Compare (Groups 25 and 26)

- Each new catalog is assumed to get its base price list and base promotion from `CommerceCatalogModelListener`, and `ConfigurationTemporarySwapper` on `CommercePricingConfiguration` must take effect synchronously.

- Expected values were derived from `CommerceProductPriceCalculationV2Impl`: a price on application unit price is POA money with price 0, and an empty or ignored promotion reads as 0.

- Updating price list priority through the model and toggling price on application through `updateCommercePriceEntry` must not hit stale MVCC versions.

- Playwright: an unqualified price list entry must win over the base price list on the storefront ($15.00). The multi-account test assumes the account selector selects "<name> 1" by default and that the mini cart follows the account switch without a reload.

- The Mini Cart approval flow assumes Submit, Approve, Reject, Resubmit and Checkout are buttons inside the portlet, and that the workflow modal exposes Cancel and Done.

- The SKU Price tab table must have header cells aligned with the body cells, including a "Price on Application" column with Yes and No values.

### Channels, Search, Facets, Checkout and Accounts (Groups 27, 28 and 29)

- `CPDefinitionIndexerTest` assumes that adding a second SKU to an ignore-SKU-combinations product expires the approved sibling.

- `testMergeGuestCommerceOrder` uses a second B2B order on the same account as the guest order, not a real guest order.

- `testReorderCommerceOrderWithUnavailableProduct` expects exactly one validator result with the en_US message.

- The widget configuration keys `paginate`, `paginationDelta` and `rangesJSONArrayString` are assumed to become portlet preferences that the widgets read.

- Facet and price range counts rely on keyword scoping with a random token to isolate test products, and on the "$ 0.00 - $ 49.99" label format.

- Payment method side panel labels and channel setting labels were taken from the Poshi macros and are unverified.

- Account reverse sort assumes a "Reverse Order Direction" title and that the direction persists across reload. The test reverts the direction at the end.

### Display Pages, Options, Product Details and Virtual Products (Groups 30, 31 and 32)

- `headless-commerce-delivery-catalog-test` gained a `commerce-inventory-api` dependency.

- The virtual activation helper creates real file entries and asserts the order item's file entry, duration and maximum usages against the winning virtual setting.

- `testGetStockQuantityUsingMultipleChannels` assumes `CommerceTestUtil.addCommerceChannel(currencyCode)` works in this company and that the 11-unit back order is accepted.

- The admin `_testPatchSkuWithSkuVirtualSettings` assumes `patchSku` honors `nestedFields=skuVirtualSettings` and returns null settings after the override is disabled.

- Display page template tests assume Related Items Collection Providers lists "Product Specifications", "Product Attachments" and "Related Diagrams", and that the style options include "Bordered List" and "Diagram Card".

- Several tests view storefront pages as the admin rather than as a buyer.

- The products limit tests depend on no other active, unscoped order types existing at run time. Otherwise add to cart opens the order type modal.

- The decimal unit of measure and mini cart edit tests toggle the company-wide Show Unselectable Options setting.

- The default option test leaves an empty buyer cart behind.

- The option facet test's final `toBeHidden` could pass before the facet portlet has rendered. Strengthen it if it proves vacuous.
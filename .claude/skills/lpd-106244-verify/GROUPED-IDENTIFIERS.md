# LPD-106244 Grouped Identifiers

This document explains the temporary `LPD-106244-Grouped-N` identifiers on the LPD-106244 branch, where they appear, and how to replace them with real Jira tickets.

## Purpose

The LPD-106244 branch migrates commerce Poshi tests to Jest, JUnit, integration and Playwright tests. Its commits are organized into 32 groups of related work. Each group is intended to become its own reviewable unit with its own Jira ticket, but those tickets do not exist yet. Until they do, every group uses a placeholder identifier, `LPD-106244-Grouped-N`, where N is the group number.

The placeholder appears in two places:

1. **Commit titles.** Every commit title starts with `LPD-106244-Grouped-N` instead of a ticket, for example `LPD-106244-Grouped-23 Migrate workflow status tests to Playwright`.

1. **Playwright test tags.** The Liferay convention tags each migrated Playwright test with its Poshi source ticket and with the ticket for the migration itself, for example `{tag: ['@COMMERCE-12396', '@LPD-106905']}`. Because the migration tickets do not exist yet, every Playwright test that a group adds or extends carries the placeholder as its migration tag, for example `{tag: ['@COMMERCE-9181', '@LPD-106244-Grouped-23']}`. A test whose Poshi source had no ticket carries only the placeholder, for example `{tag: '@LPD-106244-Grouped-7'}`.

Jest, JUnit and integration tests have no tag mechanism, so commits that only touch those layers need no change beyond their titles.

## Rules for the Placeholder

- Commit titles follow four patterns: `Migrate <subject> test(s) to <Playwright|Jest|integration|JUnit>`, `Drop <subject> test(s) already covered`, `Add <thing> to <page object or helper>`, and `Extract <thing> helper in <test class>`.
- The 72-character title limit is measured as if the prefix were a normal ticket such as `LPD-106244 ` (11 characters). The `-Grouped-N` portion is temporary, so some titles exceed 72 characters while the placeholder is in place, which is expected.
- Each placeholder tag is added in the commit that introduced or extended the test, so each group remains self-contained.

## Groups

| Group | Area | Commits | Poshi tests removed | Tagged Playwright tests | Layers | Deleted .testcase files |
|---|---|---|---|---|---|---|
| 1 | CommerceSmoke file removal | 2 | 2 | 1 | Playwright | CommerceSmoke.testcase |
| 2 | CommerceDiscountCheckout file removal | 2 | 2 | 0 | integration | CommerceDiscountCheckout.testcase |
| 3 | CPCommerceOptions file removal | 5 | 3 | 3 | Playwright | CPCommerceOptions.testcase |
| 4 | CommerceTermsAndConditions file removal | 9 | 9 | 4 | Playwright | CommerceTermsAndConditions.testcase |
| 5 | CPCommerceTermsAndConditions file removal | 9 | 8 | 3 | Playwright, integration | CPCommerceTermsAndConditions.testcase |
| 6 | Units of measure | 4 | 4 | 1 | Playwright, integration | - |
| 7 | Channel administration | 16 | 25 | 10 | Playwright | - |
| 8 | Channel services | 4 | 6 | 0 | integration | - |
| 9 | Account management | 7 | 12 | 1 | Playwright, integration | - |
| 10 | Account selector | 7 | 8 | 3 | Jest, Playwright | - |
| 11 | Storefront orders | 9 | 13 | 6 | Playwright, integration | - |
| 12 | Product definition services | 9 | 13 | 0 | integration | - |
| 13 | Product search | 5 | 8 | 0 | integration | - |
| 14 | Product options and SKUs | 4 | 5 | 0 | integration | - |
| 15 | Product replacements | 4 | 9 | 1 | Playwright, integration | - |
| 16 | Admin catalog | 10 | 20 | 8 | Playwright | - |
| 17 | Product media | 4 | 8 | 1 | Playwright, integration | - |
| 18 | Virtual products | 2 | 7 | 1 | Playwright, integration | - |
| 19 | Quick add and mini cart | 12 | 19 | 3 | Jest, Playwright | - |
| 20 | Product comparison and details | 4 | 5 | 1 | Jest, Playwright | - |
| 21 | Storefront search and sort | 2 | 2 | 2 | Playwright | - |
| 22 | Commerce service and unit tests | 5 | 5 | 0 | JUnit, integration | - |
| 23 | Product workflow and admin | 11 | 23 | 11 | Playwright, integration | - |
| 24 | SKUs, subscriptions and media | 10 | 17 | 8 | Playwright, integration | CPCommerceProductSubscriptions.testcase |
| 25 | Price on application | 5 | 20 | 3 | Playwright, integration | - |
| 26 | Quick add, mini cart and compare | 7 | 15 | 4 | Jest, Playwright | CommerceMiniCart.testcase |
| 27 | Channels | 9 | 12 | 4 | Playwright, integration | - |
| 28 | Search and facets | 6 | 15 | 8 | Playwright, integration | - |
| 29 | Checkout and accounts | 8 | 8 | 4 | Playwright, integration | - |
| 30 | Display pages and templates | 8 | 13 | 6 | JUnit, Playwright | - |
| 31 | Bundle rules, options and product details | 21 | 24 | 15 | Playwright, integration | - |
| 32 | Virtual products and SKU settings | 7 | 7 | 3 | Playwright, integration | - |
| | **Total** | **227** | **347** | **115** | | **7 files** |

Groups 1 to 5 each remove an entire `.testcase` file. Groups 6 to 32 cover partially migrated files, grouped by feature area. Shared helper commits sit in the first group that uses them, so later groups build on earlier ones.

## Known Exceptions

Two tests carry the placeholders of two groups, because both groups migrated Poshi tests into them:

- "Editing the base price and tier prices of a SKU updates its storefront price" in `commerceAdminProducts.spec.ts` carries groups 16 and 24.
- "LPD-21637 Virtual item details section visible for product and sku" in `commerceAdminVirtualProducts.spec.ts` carries groups 18 and 32.

In both cases, the later group renamed or reshaped the test, so the earlier group's tag was added in the later group's commit: the group 16 tag in the group 24 commit `Migrate SKU tier price tests to Playwright`, and the group 18 tag in the group 32 commit `Migrate SKU override disable test to integration`. The replacement below handles both tests normally, because each tag maps to its own ticket wherever it sits.

## Replacing the Placeholders

When the real tickets exist, replace every placeholder in one pass with `replace_grouped.py`, which sits next to this document in `.claude/skills/lpd-106244-verify`.

1. Create a mapping file from group number to ticket:

	```json
	{"1": "LPD-12345", "2": "LPD-12346"}
	```

1. From the liferay-portal checkout, on the LPD-106244 branch, with a clean working tree, run:

	```bash
	git branch -f LPD-106244-pre-tickets HEAD
	python3 .claude/skills/lpd-106244-verify/replace_grouped.py mapping.json "$(git merge-base HEAD upstream/master)"
	```

	The first argument is the mapping file, and the second is the branch base (`git merge-base HEAD upstream/master` by default, where `upstream` is the liferay/liferay-portal remote). Pass the base explicitly if the remote has a different name. If the handover commit is still on the branch, drop it first or it will be rewritten too.

1. The script rebases the branch once. After each commit it replaces the placeholders in the commit title and in the files that commit changed, and it resolves any conflict by taking that commit's version of the file and replacing its placeholders. When it finishes, it reports how many placeholders remain in files and in titles. Both numbers should be zero.

1. Verify the result:

	```bash
	git grep -c "LPD-106244-Grouped-" -- ':!.claude/skills/lpd-106244-verify'
	git log --format=%s "$(git merge-base HEAD upstream/master)"..HEAD | grep -c "Grouped-"
	git diff --stat LPD-106244-pre-tickets HEAD
	```

	The first two commands should print nothing or zero. The diff should show only placeholder-to-ticket replacements.

A group that has no ticket in the mapping keeps its placeholder, so the script can also be run one group at a time.

The script was verified on a throwaway copy of the branch with a mapping of each group N to `LPD-9000N`. It left no placeholders in files or titles, and every one of the 115 changed lines was a pure placeholder replacement.

## Appendix: Tagged Playwright Tests by Group

### LPD-106244-Grouped-1: CommerceSmoke file removal

- `commerceSpecifications.spec.ts`: Create new specification label and specification group

### LPD-106244-Grouped-3: CPCommerceOptions file removal

- `commerceAdminOptions.spec.ts`: Create an option from the standalone options portlet
- `commerceAdminOptions.spec.ts`: Custom fields can be returned from the option value API
- `commerceAdminOptions.spec.ts`: Remove an option from the standalone options portlet

### LPD-106244-Grouped-4: CommerceTermsAndConditions file removal

- `checkout.spec.ts`: Buyer can view every eligible ${variant.name} term and change it before completing checkout
- `checkout.spec.ts`: Delivery and payment terms checkout steps are hidden without the manage terms permissions
- `commerceAdminOrderDetails.spec.ts`: Admin can change the delivery and payment terms attached to an order
- `placedOrder.spec.ts`: Terms are not editable in the placed order details page

### LPD-106244-Grouped-5: CPCommerceTermsAndConditions file removal

- `checkout.spec.ts`: Buyer can complete checkout with rich text term descriptions
- `paymentTerms.spec.ts`: Account default ${variant.name} terms are listed by priority and can be removed
- `paymentTerms.spec.ts`: An inactive account default ${variant.name} term stays listed as inactive

### LPD-106244-Grouped-6: Units of measure

- `commerceMiniCart.spec.ts`: variant.description

### LPD-106244-Grouped-7: Channel administration

- `commerceAdminChannelDetails.spec.ts`: Payment method eligibility can be set to a payment term and back to no payment terms
- `commerceAdminProductVisibility.spec.ts`: Channels visibility follows the channel view permission of the user
- `commerceTaxEngine.spec.ts`: LPD-31663 Activate By Address Tax Engine and Add Tax Rate
- `commerceTaxEngine.spec.ts`: LPD-31663 Activate Fixed Tax Engine and Add Tax Rate
- `commerceTaxEngine.spec.ts`: Tax engines can be reactivated and the Remote engine can be configured
- `paymentMethodPermissions.spec.ts`: Users with the manage payment methods permission can edit the payment method of an order
- `shippingMethod.spec.ts`: A shipping option can be added to and removed from the ${shippingMethod} shipping method
- `shippingMethod.spec.ts`: Flat Rate shipping options show their priority and are ordered by it
- `shippingMethod.spec.ts`: The Flat Rate shipping method tracking URL can be edited and cleared
- `shippingMethod.spec.ts`: Verify Variable Rate Shipping Option Settings is viewed from Shipping Option

### LPD-106244-Grouped-9: Account management

- `accountUsers.spec.ts`: Can filter valid domain users and all users when assigning

### LPD-106244-Grouped-10: Account selector

- `commerceAccountSelector.spec.ts`: Account Selector in Minium theme working as expected
- `commerceAccountSelector.spec.ts`: Accounts created in flow become active and can be searched and selected in the account selector
- `commerceAccountSelector.spec.ts`: The in-flow account creation requires a name and cancelling it does not create the account

### LPD-106244-Grouped-11: Storefront orders

- `pendingOrder.spec.ts`: A buyer can switch the active account from and delete a pending order on its details page
- `pendingOrder.spec.ts`: An order created from the Pending Orders page is listed with its order type
- `placedOrder.spec.ts`: A display template selected in the Placed Orders widget configuration renders the orders and survives reopening it
- `placedOrder.spec.ts`: ERC and status are displayed in the placed order details page
- `placedOrder.spec.ts`: Placed order details show the billing and shipping addresses, and the widget configuration adds their full address and phone number
- `placedOrder.spec.ts`: The order date shows the create time until the Placed Orders widget configuration hides it

### LPD-106244-Grouped-15: Product replacements

- `commerceReplacementProducts.spec.ts`: variant.description

### LPD-106244-Grouped-16: Admin catalog

- `commerceAdminOptions.spec.ts`: LPD-45740 Product options can be added from product admins
- `commerceAdminProductRelations.spec.ts`: Product relations created under the Single Approver workflow stay pending through a rejection and become approved once resubmitted and approved
- `commerceAdminProducts.spec.ts`: Add a SKU with subscriptions
- `commerceAdminProducts.spec.ts`: Add, edit, and delete a SKU
- `commerceAdminProducts.spec.ts`: Currency changes based on price lists
- `commerceAdminProducts.spec.ts`: Edit a product name and its translation
- `commerceAdminProducts.spec.ts`: Editing the base price and tier prices of a SKU updates its storefront price
- `commerceAdminProducts.spec.ts`: Publish a ${productType} product

### LPD-106244-Grouped-17: Product media

- `commerceProducts.spec.ts`: An ${variant.mediaType} can be deleted from the Media tab of a product

### LPD-106244-Grouped-18: Virtual products

- `commerceAdminVirtualProducts.spec.ts`: LPD-21637 Virtual item details section visible for product and sku

### LPD-106244-Grouped-19: Quick add and mini cart

- `commerceMiniCart.spec.ts`: A SKU added from the product details and then through quick add stays a single mini cart item
- `commerceMiniCart.spec.ts`: A buyer checks out from the Mini Cart widget
- `commerceMiniCart.spec.ts`: Mini cart shows the Price on Application labels for a SKU with a UOM marked as price on application

### LPD-106244-Grouped-20: Product comparison and details

- `commerceWishLists.spec.ts`: A product added to and removed from the wish list on its product details page leaves the wish list

### LPD-106244-Grouped-21: Storefront search and sort

- `facet.spec.ts`: Specification Facet keeps working when searching products with many specifications
- `sort.spec.ts`: Sort widget does not execute a script injected through orderByCol

### LPD-106244-Grouped-23: Product workflow and admin

- `commerceAdminProductVersioning.spec.ts`: Converting an approved versionable product to draft keeps its product ID
- `commerceAdminProductVersioning.spec.ts`: Saving an approved versionable product as draft creates a single draft version
- `commerceAdminProducts.spec.ts`: A product with ${status} status can be duplicated and converted to draft
- `commerceAdminProducts.spec.ts`: A product with Pending status can be duplicated and converted to draft
- `commerceAdminProducts.spec.ts`: Converting an approved product to draft keeps its edited name
- `commerceAdminProducts.spec.ts`: Publish a product with an expiration date
- `commerceAdminProducts.spec.ts`: Saving a draft product as draft keeps its edited name
- `commerceAdminProducts.spec.ts`: Search, sort and paginate the products and SKUs admin lists
- `commerceAdminVirtualProducts.spec.ts`: Configure the file entry, maximum downloads and sample of a virtual product
- `commerceProducts.spec.ts`: A product image posted by URL through the API is shown in the products admin
- `productDetails.spec.ts`: A product is reachable through its friendly URL once the Product Detail health check is fixed

### LPD-106244-Grouped-24: SKUs, subscriptions and media

- `commerceAdminProducts.spec.ts`: A SKU external reference code cannot be used by another SKU
- `commerceAdminProducts.spec.ts`: Add price entries for different units of measure from the SKU price modal
- `commerceAdminProducts.spec.ts`: Changing the channel currency does not change the SKU price list currency
- `commerceAdminProducts.spec.ts`: Editing the base price and tier prices of a SKU updates its storefront price
- `commerceAdminProducts.spec.ts`: Product and SKU subscription configurations are saved
- `commerceAdminProducts.spec.ts`: The ${subscriptionName} length shows the subscription type in singular or plural
- `productDetails.spec.ts`: Buyer views the payment subscription of a product with multiple SKUs on its product details page
- `productDetails.spec.ts`: Buyer views the product image through adaptive media on the product details page

### LPD-106244-Grouped-25: Price on application

- `commerceAdminProducts.spec.ts`: The Price tab of a SKU shows which price list and promotion entries are priced on application
- `commerceAdminProducts.spec.ts`: Toggling Price on Application on a SKU base price list entry updates its storefront label
- `commercePricing.spec.ts`: Marking a base promotion or base price list entry as Price on Application disables its price settings and applies only the price list to the storefront

### LPD-106244-Grouped-26: Quick add, mini cart and compare

- `commerceMiniCart.spec.ts`: A buyer linked to several accounts quick adds a SKU at the price of the selected account
- `commerceMiniCart.spec.ts`: A buyer without orders searches by SKU and product name, quick adds several SKUs at quantity 1, gets a pending order and checks out
- `commerceMiniCart.spec.ts`: An account member approves and checks out an order from the Mini Cart widget under the buyer order approval workflow
- `commerceMiniCart.spec.ts`: SKUs quick added to the Mini Cart fragment show their price list and promotion prices

### LPD-106244-Grouped-27: Channels

- `checkout.spec.ts`: Shipping options are offered according to their order type eligibility
- `commerceAdminChannelDetails.spec.ts`: ${paymentMethod} payment method details and configuration can be edited
- `commerceAdminChannelDetails.spec.ts`: Channel general settings can be edited and are persisted
- `commerceAdminChannelDetails.spec.ts`: Shipping option eligibility can be set to a delivery term and back to no delivery terms

### LPD-106244-Grouped-28: Search and facets

- `facet.spec.ts`: ${widget} - Selecting a term narrows the search results and the URL
- `facet.spec.ts`: Category Facet narrows the search results to the selected category
- `facet.spec.ts`: Commerce facet selections can be cleared
- `facet.spec.ts`: Commerce facets filter cumulatively without the Category Facet
- `priceRangeFacet.spec.ts`: Price Range Facet applies the custom ranges of its configuration
- `priceRangeFacet.spec.ts`: Price Range Facet filters the search results by the selected range and by the minimum and maximum inputs
- `priceRangeFacet.spec.ts`: Price Range Facet keeps the term counts when ranges are selected
- `searchResults.spec.ts`: name

### LPD-106244-Grouped-29: Checkout and accounts

- `accountEntriesManagementPortlet.spec.ts`: User can reverse the sort direction of the accounts
- `commerceAdminOrderDetails.spec.ts`: Scripts injected in account addresses are not executed in the addresses widget and the admin order details
- `commerceAdminProductConfigurations.spec.ts`: Availability estimates can be added from the Availability Estimates admin
- `placedOrder.spec.ts`: A placed order keeps its item details after the product is deleted

### LPD-106244-Grouped-30: Display pages and templates

- `commerceAssetCategories.spec.ts`: Clicking a category in the categories navigation widget opens the category content page when no default category display page is set
- `productDetails.spec.ts`: Clicking a product in the catalog opens the product details widget page when no default product display page is set
- `productDisplayPageTemplates.spec.ts`: Collection Display fragment lists the related diagrams of the displayed product with each list item style
- `productDisplayPageTemplates.spec.ts`: Collection Display fragment lists the specifications and the attachments of the displayed product
- `productDisplayPageTemplates.spec.ts`: Heading fragments mapped to product fields show the product values on the product page
- `productDisplayPageTemplates.spec.ts`: Marking and unmarking a product display page template as default switches the product page

### LPD-106244-Grouped-31: Bundle rules, options and product details

- `commerceAdminOptions.spec.ts`: Product option values can be edited and deleted from product admins
- `commerceAdminProductConfigurations.spec.ts`: Product configuration tab rejects empty and less than minimum order quantities
- `commerceProducts.spec.ts`: A bundle cannot be added to the cart when a product required in the bundle is not purchasable or out of stock
- `commerceProducts.spec.ts`: A products limit rule applies to a bundled product added in separate order items
- `commerceProducts.spec.ts`: A products limit rule applies to a bundled product linked to a decimal unit of measure quantity
- `commerceProducts.spec.ts`: A products limit rule applies to a bundled product only for the eligible account
- `commerceProducts.spec.ts`: A products limit rule applies to a bundled product only in orders of the eligible order type
- `commerceProducts.spec.ts`: A products limit rule applies to a bundled product only in the eligible channel
- `commerceProducts.spec.ts`: The mini cart edit panel shows why bundle option values are included or excluded
- `commerceProducts.spec.ts`: The products limit rule with the highest priority applies to a bundled product
- `facet.spec.ts`: Option Facet shows a facetable product option with its values until the option is no longer facetable
- `productCard.spec.ts`: Only the out of stock product card shows an availability label when a product with variants follows it
- `productDetails.spec.ts`: Product subscription information is shown on the product details page
- `productOptions.spec.ts`: A default option value is checked on the product details page and added to the cart until it is no longer the default
- `productOptions.spec.ts`: The images bound to the selected option value are shown on the product details widget and the image gallery fragment

### LPD-106244-Grouped-32: Virtual products and SKU settings

- `commerceAdminOrderDetails.spec.ts`: Admin can view the virtual settings of a virtual order item
- `commerceAdminVirtualProducts.spec.ts`: LPD-21637 Virtual item details section visible for product and sku
- `productDetails.spec.ts`: Virtual product details and the SKU override sample file are shown on the product details page
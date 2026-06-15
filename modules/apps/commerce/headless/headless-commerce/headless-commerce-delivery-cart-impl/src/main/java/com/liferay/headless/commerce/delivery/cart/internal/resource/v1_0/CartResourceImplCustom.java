package at.liferay.osram.cart.internal.resource.v1_0;

import at.dccs.liferay.amsosram.product.util.CartUtil;
import at.dccs.liferay.amsosram.product.util.ProductValidatorUtil;
import at.dccs.liferay.amsosram.util.AmsOsramLiferayObjectUtil;
import at.dccs.liferay.amsosram.util.CommerceContextUtil;
import at.dccs.liferay.amsosram.util.CommercePermissionUtil;
import at.dccs.liferay.amsosram.util.constants.commerce.ObjectExternalReferenceCodeConstantsCommerce;
import at.dccs.liferay.amsosram.util.datamodel.EcomCommerceContext;
import at.liferay.osram.cart.dto.v1_0.ProductRequest;
import at.liferay.osram.cart.dto.v1_0.RemoveProductItemRequest;
import at.liferay.osram.cart.resource.v1_0.CartResource;
import at.liferay.osram.customer.service.AmsOsramCustomerService;
import at.liferay.osram.customer.service.model.PurChaseOrderDTO;
import at.liferay.osram.customer.service.model.PurChaseOrderItem;
import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryUserRelLocalService;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.model.CommerceOrderItem;
import com.liferay.commerce.service.*;
import com.liferay.list.type.model.ListTypeDefinition;
import com.liferay.list.type.model.ListTypeEntry;
import com.liferay.list.type.service.ListTypeDefinitionLocalService;
import com.liferay.list.type.service.ListTypeEntryLocalService;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.TransactionConfig;
import com.liferay.portal.kernel.transaction.TransactionInvokerUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.multipart.BinaryFile;
import com.liferay.portal.vulcan.multipart.MultipartBody;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import javax.ws.rs.core.Response;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import com.liferay.object.model.ObjectDefinition;
import java.io.Serializable;
import java.util.*;


/**
 * @author amsosram
 */
@Component(properties = "OSGI-INF/liferay/rest/v1_0/cart.properties", scope = ServiceScope.PROTOTYPE, service = CartResource.class)
public class CartResourceImpl extends BaseCartResourceImpl {

	private static final String PICKLIST_CART_STATUS_ERC = "pl_osram_cart_status";
	private static final String FIELD_CART_STATUS = "cartStatus";

	private static final Log LOGGER = LogFactoryUtil.getLog(CartResourceImpl.class);
	@Reference
	CommerceContextUtil commerceContextUtil;
	@Reference
	private ProductValidatorUtil productValidatorUtil;
	@Reference
	private CartUtil cartUtil;
	@Reference
	private AmsOsramCustomerService amsOsramCustomerService;
	@Reference
	private CommerceOrderLocalService commerceOrderLocalService;
	@Reference
	private ObjectEntryLocalService objectEntryLocalService;
	@Reference
	private CommerceOrderItemLocalService commerceOrderItemLocalService;
	@Reference
	private AmsOsramLiferayObjectUtil amsOsramLiferayObjectUtil;
	@Reference
	private ListTypeDefinitionLocalService listTypeDefinitionLocalService;
	@Reference
	private ListTypeEntryLocalService listTypeEntryLocalService;
	@Reference
	private AccountEntryUserRelLocalService accountEntryUserRelLocalService;
	@Reference
	private CommercePermissionUtil commercePermissionUtil;
	@Reference
	private at.dccs.liferay.amsosram.util.SAPFormatUtil sapFormatUtil;
	@Reference
	private com.liferay.object.service.ObjectDefinitionLocalService objectDefinitionLocalService;

	@Override
	public Response removeProductsFromCart(RemoveProductItemRequest[] removeProductItemRequests) {
		if (removeProductItemRequests == null || removeProductItemRequests.length == 0) {
			LOGGER.warn("No cart items provided for removal.");
			return Response.status(Response.Status.BAD_REQUEST)
				.entity("No cart items provided for removal.")
				.build();
		}

		for (RemoveProductItemRequest request : removeProductItemRequests) {
			if (request == null) {
				LOGGER.warn("Encountered a null request object while removing cart items.");
				continue;
			}
			String cartItemERC = request.getCartItemERC();

			CommerceOrderItem cartItem = commerceOrderItemLocalService.fetchCommerceOrderItemByExternalReferenceCode(cartItemERC, contextCompany.getCompanyId());
			if (cartItem == null) {
				return Response.ok("Item does not exist").build();
			}
			if (!isCartItemAllowed(cartItem)) {
				LOGGER.error("CartItem " + cartItemERC + " does not belong to current user " + contextUser.getEmailAddress());
				return Response.status(Response.Status.FORBIDDEN).build();
			}
			deleteCartItem(cartItem);
		}

		return Response.ok("All provided cart items processed for removal.").build();
	}

	private void deleteCartItem(CommerceOrderItem item) {

		LOGGER.debug(String.format(
			"Attempting to remove item - CartId: %d, CartItemId: %d, ERC: %s",
			item.getCommerceOrderId(), item.getCommerceOrderItemId(), item.getExternalReferenceCode()
		));

		// Fetch related additional data entries
		String filter = String.format("r_orderItemId_commerceOrderItemERC eq '%s'", item.getExternalReferenceCode());
		List<Map<String, String>> additionalItems =
			amsOsramLiferayObjectUtil.getObjectValues(
				ObjectExternalReferenceCodeConstantsCommerce.COMMERCE_ORDER_ITEM_ADDITIONAL_DATA,
				0,
				PrincipalThreadLocal.getUserId(),
				filter,
				null,
				Locale.US,
				contextCompany.getCompanyId()
			);

		additionalItems.forEach(data -> deleteObject(data.get("c_commerceOrderItemAdditionalDataId")));
		// Remove main CommerceOrderItem
		commerceOrderItemLocalService.deleteCommerceOrderItem(item);
		LOGGER.debug(String.format(
			"Successfully deleted CartItemId: %d from CartId: %d",
			item.getCommerceOrderItemId(), item.getCommerceOrderId()
		));
	}

	@Override
	public Response purchaseOrderRequest(String cartId, String businessTransaction, MultipartBody multipartBody) throws Exception {
		LOGGER.debug("cartId is: " + cartId + " BusinessTransaction is: " + businessTransaction);
		LOGGER.debug("multipartBody is: " + multipartBody);
		BinaryFile file = multipartBody.getBinaryFile("file");
		PurChaseOrderDTO purChaseOrderDTO = amsOsramCustomerService.purchaseRequest(file, businessTransaction);
		LOGGER.debug("purChaseOrderDTO is: " + purChaseOrderDTO);
		ProductRequest[] productRequests = transformToProductRequests(cartId, purChaseOrderDTO);
		return addValidatedProductsToCart(productRequests);
	}

	private ProductRequest[] transformToProductRequests(String cartId, PurChaseOrderDTO purChaseOrderDTO) {
		List<ProductRequest> productRequests = new ArrayList<>();
		for (PurChaseOrderItem purChaseOrderItem : purChaseOrderDTO.getItem()) {
			ProductRequest productRequest = new ProductRequest();
			productRequest.setCartId(cartId);
			productRequest.setProductReference(purChaseOrderItem.getProductNo());
			productRequest.setQuantity(purChaseOrderItem.getQuantity());
			productRequests.add(productRequest);
		}

		return productRequests.toArray(new ProductRequest[0]);
	}

	@Override
	public Response addProductsToCart(ProductRequest[] productRequests) throws Exception {
		return addValidatedProductsToCart(productRequests);
	}

	private Response addValidatedProductsToCart(ProductRequest[] productRequests) {
		List<Map<String, Object>> result = new ArrayList<>();
		final Class<?>[] rollbackForClasses = {Exception.class};
		List<String> minQuantityProducts = new ArrayList<>();
		List<String> inValidProducts = new ArrayList<>();
		List<String> validProducts = new ArrayList<>();
		for (ProductRequest productRequest : productRequests) {
			try {
				EcomCommerceContext commerceContext = commerceContextUtil.getCommerceContext(contextHttpServletRequest);
				long cartId  = commerceContext.getCommerceOrder().getCommerceOrderId();
				CommerceOrder order = commerceOrderLocalService.fetchCommerceOrder(cartId);
				if (order == null) {
					LOGGER.warn("Invalid order/cartId " + cartId + " in productRequest " + productRequest.getProductReference());
					continue;
				}
				if (!isCartAllowed(order)) {
					LOGGER.error("Cart " + cartId + " does not belong to an account of the current user " + contextUser.getEmailAddress());
					return Response.status(Response.Status.FORBIDDEN).build();
				}
				String productERC = productValidatorUtil.productNumberIdentifer(cartId, productRequest.getProductReference(), contextUser);
				boolean isProductValid = productValidatorUtil.validateProduct(cartId, productERC, productRequest.getQuantity(), contextUser);

				if (isProductValid) {
					String validatedQuantity = productValidatorUtil.validateQuantity(cartId, productERC, productRequest.getQuantity(), contextUser);
					String productOriginalQuantity = productRequest.getQuantity();
					if (productRequest.getQuantity() == null || !productRequest.getQuantity().equals(validatedQuantity)) {
						productRequest.setQuantity(validatedQuantity);
						minQuantityProducts.add(productRequest.getProductReference());
					}
					LOGGER.debug("productERC is: " + productERC + "isProductValid is: " + isProductValid + " validatedQuantity  is: " + productRequest.getQuantity());
					TransactionInvokerUtil.invoke(TransactionConfig.Factory.create(Propagation.REQUIRES_NEW, rollbackForClasses), () -> {
						PrincipalThreadLocal.setName(contextUser.getUserId());
						CommerceOrderItem commerceOrderItem = cartUtil.addProductToCart(cartId, productERC, productRequest.getQuantity(), contextUser, result);
						LOGGER.debug("Added commerceOrderItem: " + commerceOrderItem);

						Map<String, Serializable> extraValues = new HashMap<>();
						extraValues.put("originalEnteredProduct", String.valueOf(productRequest.getProductReference()));

						if (!productERC.equalsIgnoreCase(productRequest.getProductReference())) {
							extraValues.put("customerPartNumber", String.valueOf(productRequest.getProductReference()));
						} else {
							String customerMaterialNumber = productValidatorUtil.fetchCustomerMaterialNumber(productERC, commerceContext.getAccountEntryId());
							extraValues.put("customerPartNumber", customerMaterialNumber);
						}

						extraValues.put("productID", String.valueOf(productERC));

						if (productRequest.getDeliveryDate() != null && !productRequest.getDeliveryDate().isBlank()) {
							try {
								Date parsedDelivery = sapFormatUtil.parseDateWithLocale(productRequest.getDeliveryDate(), contextUser.getLocale());
								if (parsedDelivery != null) {
									extraValues.put("deliveryDate", parsedDelivery);
								}
							} catch (Exception e) {
								LOGGER.error("Error parsing deliveryDate from productRequest: " + e, e);
								result.add(Map.of("success", false, "message", "Error parsing deliveryDate"));
							}
						}

						ObjectEntry objectEntry = cartUtil.maintainCommerceOrderItemAdditionalData(commerceOrderItem, extraValues, productERC, contextUser, result);
						LOGGER.debug("objectEntry is: " + objectEntry);
						if (productOriginalQuantity.equals(validatedQuantity)) {
							validProducts.add(productRequest.getProductReference());
						}
						return result;
					});

				} else {
					inValidProducts.add(productERC);
				}
			} catch (Throwable e) {
				result.add(Map.of("success", false, "message", "Product " + productRequest.getProductReference() + " cannot be added to cart because " + e.getMessage()));
				LOGGER.error("Error is: ", e);
			}
		}
		String minQuantityProductsError = String.join(",", minQuantityProducts);
		if (!minQuantityProducts.isEmpty()) {
			String minQuantityError = LanguageUtil.getLanguage().format(contextUser.getLocale(), "quickAdder.minimumQuantity", minQuantityProductsError);
			if (minQuantityProductsError.contains(","))
				minQuantityError = LanguageUtil.getLanguage().format(contextUser.getLocale(), "quickAdder.minimumQuantity.plural", minQuantityProductsError);
			result.add(Map.of("success", false, "message", minQuantityError));
		}
		String validProductsSuccess = LanguageUtil.getLanguage().format(contextUser.getLocale(), "quickAdder.validProduct", validProducts.size());
		if (!validProducts.isEmpty()) {
			result.add(Map.of("success", true, "message", validProductsSuccess));
		}


		String inValidProductsSeperated = String.join(",", inValidProducts);
		String inValidProductsError = LanguageUtil.getLanguage().format(contextUser.getLocale(), "quickAdder.inValidProduct", inValidProductsSeperated);
		if (!inValidProducts.isEmpty()) {
			result.add(Map.of("success", false, "message", inValidProductsError));
		}

		return Response.status(Response.Status.ACCEPTED).entity(result).build();
	}

	@POST
	@Path("/orderItems/deliveryDate")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateOrderItemDeliveryDate(Map<String, String> payload) {
		String erc = payload.get("externalReferenceCode");
		String deliveryDateStr = payload.get("deliveryDate");

		if (Validator.isNull(erc) || Validator.isNull(deliveryDateStr)) {
			return Response.status(Response.Status.BAD_REQUEST).entity("externalReferenceCode and deliveryDate are required").build();
		}

		CommerceOrderItem cartItem = commerceOrderItemLocalService.fetchCommerceOrderItemByExternalReferenceCode(erc, contextCompany.getCompanyId());

		if (cartItem == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Cart item not found").build();
		}

		if (!isCartItemAllowed(cartItem)) {
			return Response.status(Response.Status.FORBIDDEN).build();
		}

		try {
			Date parsedDelivery = sapFormatUtil.parseDateWithLocale(deliveryDateStr, contextUser.getLocale());
			ObjectDefinition objectDefinition = objectDefinitionLocalService.fetchObjectDefinitionByExternalReferenceCode(ObjectExternalReferenceCodeConstantsCommerce.COMMERCE_ORDER_ITEM_ADDITIONAL_DATA, contextCompany.getCompanyId());
			ObjectEntry objectEntry = objectEntryLocalService.getObjectEntry(cartItem.getExternalReferenceCode(), objectDefinition.getObjectDefinitionId());
			Map<String, Serializable> values = objectEntry.getValues();
			LOGGER.debug("values are: " + values);
			values.put("deliveryDate", parsedDelivery);
			ObjectEntry updated = objectEntryLocalService.addOrUpdateObjectEntry(cartItem.getExternalReferenceCode(), objectDefinition.getUserId(), 0, objectDefinition.getObjectDefinitionId(), values, new ServiceContext());
			LOGGER.debug("Updated deliveryDate for item: " + cartItem.getCommerceOrderItemId() + " -> " + updated.getValues());
			return Response.ok().build();
		} catch (Exception e) {
			LOGGER.error("Error updating item delivery date", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error updating delivery date").build();
		}
	}

	@Override
	public Response deleteCart(Long cartId) throws Exception {
		CommerceOrder order = commerceOrderLocalService.fetchCommerceOrder(cartId);
		if (order == null) {
			return Response.ok("Order does not exists").build();
		}

		if (!isCartAllowed(order)) {
			LOGGER.error("Cart " + cartId + " does not belong to an account of the current user " + contextUser.getEmailAddress());
			return Response.status(Response.Status.FORBIDDEN).build();
		}
		deleteOrderAdditionalData(order);
		commerceOrderLocalService.deleteCommerceOrder(order);
		return Response.ok().build();
	}

	private void deleteObject(String idString) {
		if (Validator.isNull(idString)) return;

		try {
			objectEntryLocalService.deleteObjectEntry(Long.parseLong(idString));
			LOGGER.debug("Deleted Object: " + idString);
		} catch (PortalException e) {
			LOGGER.warn("Could not delete Object with ID " + idString, e);
		}
	}

	@Override
	public Response changeCartStatus(Long cartId, String cartNewStatus) throws Exception {
		CommerceOrder commerceOrder = commerceOrderLocalService.getCommerceOrder(cartId);

		if (!isUserMemberOfCommerceOrderAccount(cartId, contextUser.getUserId(), commerceOrder.getAccountEntry().getAccountEntryId())) {
			LOGGER.debug("Current User is not a member of this cart.");
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		final String filter = String.format("r_orderId_commerceOrderId eq '%s'", commerceOrder.getCommerceOrderId());

		List<Map<String, String>> commerceOrderAdditionalDatas = amsOsramLiferayObjectUtil.getObjectValues(
			ObjectExternalReferenceCodeConstantsCommerce.COMMERCE_ORDER_ADDITIONAL_DATA, 0,
			PrincipalThreadLocal.getUserId(), filter, null, Locale.US, commerceOrder.getCompanyId());

		if (commerceOrderAdditionalDatas != null) {
			for (Map<String, String> commerceOrderAdditionalData : commerceOrderAdditionalDatas) {
				if (commerceOrderAdditionalData.containsKey(FIELD_CART_STATUS)) {
					String cartStatusStr = commerceOrderAdditionalData.get(FIELD_CART_STATUS);
					String commerceAdditionalDataId = commerceOrderAdditionalData.get("c_commerceOrderAdditionalDataId");

					if (cartStatusStr != null && !cartStatusStr.isEmpty()) {
						try {
							ListTypeDefinition listTypeDefinition = listTypeDefinitionLocalService
								.fetchListTypeDefinitionByExternalReferenceCode(PICKLIST_CART_STATUS_ERC, commerceOrder.getCompanyId());

							ListTypeEntry listTypeEntry = listTypeEntryLocalService
								.fetchListTypeEntryByExternalReferenceCode(cartStatusStr,
									commerceOrder.getCompanyId(),
									listTypeDefinition.getListTypeDefinitionId());

							if (listTypeEntry == null) {
								LOGGER.error("ListTypeEntry not found for externalReferenceCode: " + cartStatusStr);
								return null;
							}

							String listTypeEntryName = listTypeEntry.getName(Locale.US);
							LOGGER.debug("Cart Status: " + listTypeEntryName);

							ObjectEntry objectEntry = updateCartStatus(Long.parseLong(commerceAdditionalDataId), cartNewStatus);
							LOGGER.debug("Updated cart status object entry: " + objectEntry);

						} catch (NumberFormatException e) {
							LOGGER.error("Invalid cartStatus value: " + cartStatusStr, e);
						}
					}
				}
			}
		}

		LOGGER.debug("Commerce order additional data: " + commerceOrderAdditionalDatas);
		return Response.ok().build();
	}

	public ObjectEntry updateCartStatus(long objectEntryId, String newCartStatusValue) {
		try {
			ObjectEntry existingEntry = objectEntryLocalService.getObjectEntry(objectEntryId);

			if (existingEntry == null) {
				LOGGER.error("Object entry not found with ID: " + objectEntryId);
				return null;
			}

			Map<String, Serializable> values = new HashMap<>(existingEntry.getValues());
			LOGGER.debug("Current object entry values: " + values);

			values.put("cartStatus", newCartStatusValue);

			ServiceContext serviceContext = new ServiceContext();
			serviceContext.setUserId(contextUser.getUserId());
			serviceContext.setCompanyId(contextCompany.getCompanyId());

			ObjectEntry updatedEntry = objectEntryLocalService.updateObjectEntry(
				contextUser.getUserId(),
				objectEntryId,
				values,
				serviceContext
			);

			LOGGER.debug("Successfully updated cart status to  " + newCartStatusValue + " for object entry ID: " + objectEntryId);

			return updatedEntry;

		} catch (PortalException e) {
			LOGGER.error("Error updating cartStatus for object entry ID: " + objectEntryId, e);
			return null;
		}
	}

	public boolean isUserMemberOfCommerceOrderAccount(long commerceOrderId, long userId, long accountId) {
		try {
			CommerceOrder commerceOrder = commerceOrderLocalService.getCommerceOrder(commerceOrderId);

			if (commercePermissionUtil.hasStandardCartPermission(userId, accountId) || commercePermissionUtil.hasItalianCartPermission(userId, accountId)) {
				if (commerceOrder == null) {
					LOGGER.error("CommerceOrder not found with ID: " + commerceOrderId);
					return false;
				}
				long commerceAccountId = commerceOrder.getCommerceAccountId();

				// Checking if user is associated with this account
				return accountEntryUserRelLocalService.hasAccountEntryUserRel(commerceAccountId, userId);
			}
			LOGGER.debug("You do not have valid role to fetch this cart information");
			return false;
		} catch (PortalException e) {
			LOGGER.error("Error checking user membership for commerce order: " + commerceOrderId + " and user: " + userId, e);
			return false;
		}
	}


	private void deleteOrderAdditionalData(CommerceOrder order) {
		final String filter = String.format("r_orderId_commerceOrderId eq '%s'", order.getCommerceOrderId());
		List<Map<String, String>> commerceOrderAdditionalDatas = amsOsramLiferayObjectUtil.getObjectValues(ObjectExternalReferenceCodeConstantsCommerce.COMMERCE_ORDER_ADDITIONAL_DATA, 0,
			PrincipalThreadLocal.getUserId(), filter, null, Locale.US, order.getCompanyId());

		commerceOrderAdditionalDatas.forEach(data -> deleteObject(data.get("c_commerceOrderAdditionalDataId")));
		List<CommerceOrderItem> cartItems = order.getCommerceOrderItems();
		cartItems.forEach(this::deleteCartItem);
	}

	private boolean isCartAllowed(CommerceOrder order) {
		try {
			return isAccountAvailable(order.getAccountEntry());
		} catch (PortalException e) {
			LOGGER.error(e);
		}
		return false;
	}

	private boolean isCartItemAllowed(CommerceOrderItem item) {
		try {
			AccountEntry entry = item.getCommerceOrder().getAccountEntry();
			return isAccountAvailable(entry);
		} catch (PortalException e) {
			LOGGER.error(e);
		}
		return false;
	}

	private boolean isAccountAvailable(AccountEntry entry) {
		if (entry == null) return false;
		EcomCommerceContext commerceContext = commerceContextUtil.getCommerceContext(contextHttpServletRequest);
		if (!commerceContext.isValid()) return false;
		return commerceContext.getOnBoardedAccountEntries().contains(entry);
	}
}

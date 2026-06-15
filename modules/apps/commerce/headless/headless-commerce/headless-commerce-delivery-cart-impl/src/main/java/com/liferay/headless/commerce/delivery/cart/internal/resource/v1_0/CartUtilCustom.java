package at.dccs.liferay.amsosram.product.util;

import at.dccs.liferay.amsosram.util.constants.commerce.ObjectExternalReferenceCodeConstantsCommerce;
import at.dccs.liferay.amsosram.util.constants.commerce.OrderConstantsCommerce;
import com.liferay.commerce.context.CommerceContext;
import com.liferay.commerce.context.CommerceContextFactory;
import com.liferay.commerce.exception.CommerceOrderValidatorException;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.model.CommerceOrderItem;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.commerce.product.model.CProduct;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.product.service.CPInstanceLocalService;
import com.liferay.commerce.product.service.CProductLocalService;
import com.liferay.commerce.service.CommerceOrderItemLocalService;
import com.liferay.commerce.service.CommerceOrderLocalService;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.BigDecimalUtil;
import com.liferay.portal.kernel.util.Validator;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component(immediate = true, service = CartUtil.class)
public class CartUtil {

	private static final Log LOGGER = LogFactoryUtil.getLog(CartUtil.class);

	@Reference
	private CPDefinitionLocalService cpDefinitionLocalService;
	@Reference
	private CommerceContextFactory commerceContextFactory;
	@Reference
	private CPInstanceLocalService cpInstanceLocalService;
	@Reference
	private ObjectDefinitionLocalService objectDefinitionLocalService;
	@Reference
	private CProductLocalService cProductLocalService;
	@Reference
	private ObjectEntryLocalService objectEntryLocalService;
	@Reference
	private CommerceOrderItemLocalService commerceOrderItemLocalService;
	@Reference
	private CommerceOrderLocalService commerceOrderLocalService;
	@Reference
	AMSPProductUtil amspProductUtil;

	public CommerceOrderItem addProductToCart(long cartId, String productERC, String productQuantity, User user, List<Map<String, Object>> result) {

		CProduct cProduct = cProductLocalService.fetchCProductByExternalReferenceCode(productERC, user.getCompanyId());
		CPDefinition cpDefinition = cpDefinitionLocalService.fetchCPDefinitionByCProductId(cProduct.getCProductId());
		CPInstance cpInstance = cpInstanceLocalService.fetchDefaultCPInstance(cpDefinition.getCPDefinitionId());
		LOGGER.debug("product is: " + cProduct + "cpDefinition is: " + cpDefinition + "cpInstance is: " + cpInstance);
		CommerceOrder commerceOrder = commerceOrderLocalService.fetchCommerceOrder(cartId);
		commerceOrder.setOrderStatus(OrderConstantsCommerce.OPEN);
		commerceOrder = commerceOrderLocalService.updateCommerceOrder(commerceOrder);
		// create serviceContext
		ServiceContext serviceContext = new ServiceContext();
		serviceContext.setUserId(user.getUserId());
		serviceContext.setScopeGroupId(user.getGroupId());
		serviceContext.setCompanyId(user.getCompanyId());
		// create commerceContext
		CommerceContext commerceContext = commerceContextFactory.create(user.getCompanyId(), commerceOrder.getGroupId(), user.getUserId(), commerceOrder.getCommerceOrderId(), commerceOrder.getCommerceAccountId());
		long commerceOrderId = commerceOrder.getCommerceOrderId();
		long cpInstanceId = cpInstance.getCPInstanceId();
		BigDecimal quantity = new BigDecimal(productQuantity);
		long replacedCPInstanceId = 0L;
		CommerceOrderItem commerceOrderItem = null;
		// add product to cart
		try {
			LOGGER.debug("accountId is: " + commerceContext.getAccountEntry().getAccountEntryId());
			LOGGER.debug("orderId is: " + commerceOrderId);
			commerceOrderItem = commerceOrderItemLocalService.addCommerceOrderItem(user.getUserId(), commerceOrderId, cpInstanceId, "", BigDecimalUtil.get(quantity, BigDecimal.ONE), replacedCPInstanceId, BigDecimal.ZERO, "", commerceContext, serviceContext);
			LOGGER.debug("Product Added to Cart is: " + commerceOrderItem);
		} catch (CommerceOrderValidatorException c) {
			if (!c.getCommerceOrderValidatorResults().isEmpty()) {
				LOGGER.error("Error: " + c.getCommerceOrderValidatorResults().getFirst().getLocalizedMessage());
				result.add(Map.of("success", false, "message", "Error " + c.getCommerceOrderValidatorResults().getFirst().getLocalizedMessage()));
			}
		} catch (PortalException e) {
			LOGGER.error("Error: " + e);
			result.add(Map.of("success", false, "message", "Error " + e));
		}
		return commerceOrderItem;
	}

	public ObjectEntry maintainCommerceOrderItemAdditionalData(CommerceOrderItem commerceOrderItem, Map<String, Serializable> extraValues, String productExternalReferenceCode, User user, List<Map<String, Object>> result) {
		ObjectDefinition objectDefinition = objectDefinitionLocalService.fetchObjectDefinitionByExternalReferenceCode(ObjectExternalReferenceCodeConstantsCommerce.COMMERCE_ORDER_ITEM_ADDITIONAL_DATA, user.getCompanyId());
		LOGGER.debug("objectDefinition is: " + objectDefinition.getExternalReferenceCode());
		long commerceOrderId = commerceOrderItem.getCommerceOrderId();
		long sequence = orderItemSequence(commerceOrderId);
		Map<String, Serializable> values = new HashMap<>();
		values.put("itemNumber", sequence);
		values.put("r_orderItemId_commerceOrderItemId", commerceOrderItem.getCommerceOrderItemId());

		// Defaults
		if (extraValues == null || !extraValues.containsKey("originalEnteredProduct")) {
			values.put("originalEnteredProduct", "");
		}
		if (extraValues == null || !extraValues.containsKey("productID")) {
			values.put("productID", String.valueOf(productExternalReferenceCode));
		}

		String productPrimaryURL = getProductPrimaryURL(productExternalReferenceCode, user.getUserId(), user.getLocale(), user.getCompanyId());
		values.put("productDetailPageURL", productPrimaryURL);

		// Merge extras (overrides defaults)
		if (extraValues != null) {
			values.putAll(extraValues);
		}

		ObjectEntry commerceOrderItemAdditionalData = null;
		try {
			commerceOrderItemAdditionalData = objectEntryLocalService.addOrUpdateObjectEntry(commerceOrderItem.getExternalReferenceCode(), objectDefinition.getUserId(), 0, objectDefinition.getObjectDefinitionId(), values, new ServiceContext());
		} catch (PortalException e) {
			LOGGER.error("Error: " + e);
			result.add(Map.of("success", false, "message", "Error " + e));
		}
		LOGGER.debug("CommerceOrderItemAdditionalData created: " + commerceOrderItemAdditionalData);
		return commerceOrderItemAdditionalData;
	}

	private long orderItemSequence(long commerceOrderId) {
		List<CommerceOrderItem> commerceOrderItems = commerceOrderItemLocalService.getCommerceOrderItems(commerceOrderId, -1, -1);
		LOGGER.debug("No. of commerceOrderItems in cart are: " + commerceOrderItems.size());
		return commerceOrderItems.size();
	}

	public String getProductPrimaryURL(String productERC, long userId, Locale locale, long comapnyId) {
		String aggregatedProductPimId = amspProductUtil.getProductAdditionalData(productERC, userId, locale, comapnyId);
		String primaryProductURL = "";
		if (!Validator.isBlank(aggregatedProductPimId)) {
			primaryProductURL = amspProductUtil.getPrimaryProductURL(aggregatedProductPimId);
			return primaryProductURL;
		}
		return primaryProductURL;
	}
}

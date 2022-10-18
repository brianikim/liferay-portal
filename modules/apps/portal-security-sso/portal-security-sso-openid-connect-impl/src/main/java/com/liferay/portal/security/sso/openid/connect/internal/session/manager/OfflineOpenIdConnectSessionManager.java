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

package com.liferay.portal.security.sso.openid.connect.internal.session.manager;

import com.liferay.counter.kernel.service.CounterLocalService;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.messaging.BaseMessageListener;
import com.liferay.portal.kernel.messaging.Destination;
import com.liferay.portal.kernel.messaging.DestinationConfiguration;
import com.liferay.portal.kernel.messaging.DestinationFactory;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBus;
import com.liferay.portal.kernel.messaging.MessageListener;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.sso.openid.connect.OpenIdConnectProvider;
import com.liferay.portal.security.sso.openid.connect.OpenIdConnectProviderRegistry;
import com.liferay.portal.security.sso.openid.connect.configuration.OpenIdConnectConfiguration;
import com.liferay.portal.security.sso.openid.connect.constants.OpenIdConnectWebKeys;
import com.liferay.portal.security.sso.openid.connect.internal.constants.OpenIdConnectDestinationNames;
import com.liferay.portal.security.sso.openid.connect.internal.util.OpenIdConnectTokenRequestUtil;
import com.liferay.portal.security.sso.openid.connect.persistence.model.OpenIdConnectSession;
import com.liferay.portal.security.sso.openid.connect.persistence.service.OpenIdConnectSessionLocalService;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientMetadata;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;

import java.util.Date;
import java.util.Dictionary;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Arthur Chan
 */
@Component(
	configurationPid = "com.liferay.portal.security.sso.openid.connect.configuration.OpenIdConnectConfiguration",
	configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true,
	service = OfflineOpenIdConnectSessionManager.class
)
public class OfflineOpenIdConnectSessionManager {

	public boolean isOpenIdConnectSession(HttpSession httpSession) {
		if (httpSession == null) {
			return false;
		}

		Long openIdConnectSessionId = (Long)httpSession.getAttribute(
			OpenIdConnectWebKeys.OPEN_ID_CONNECT_SESSION_ID);

		if (openIdConnectSessionId != null) {
			return true;
		}

		return false;
	}

	public boolean isOpenIdConnectSessionExpired(HttpSession httpSession) {
		Long openIdConnectSessionId = (Long)httpSession.getAttribute(
			OpenIdConnectWebKeys.OPEN_ID_CONNECT_SESSION_ID);

		if (openIdConnectSessionId == null) {
			return true;
		}

		OpenIdConnectSession openIdConnectSession =
			_openIdConnectSessionLocalService.fetchOpenIdConnectSession(
				openIdConnectSessionId);

		if (openIdConnectSession == null) {
			return true;
		}

		AccessToken accessToken = _getAccessToken(openIdConnectSession);

		long lifetime = accessToken.getLifetime() * Time.SECOND;

		Date modifiedDate = openIdConnectSession.getModifiedDate();

		long elapsedTime = System.currentTimeMillis() - modifiedDate.getTime();

		if (elapsedTime > (lifetime - _tokenRefreshOffsetMillis)) {
			Message message = new Message();

			message.put("openIdConnectSessionId", openIdConnectSessionId);

			_messageBus.sendMessage(
				OpenIdConnectDestinationNames.OPENID_CONNECT_TOKEN_REFRESH,
				message);
		}

		return false;
	}

	public long startOpenIdConnectSession(
		String configurationPid, OIDCTokens oidcTokens, String providerName,
		long userId) {

		OpenIdConnectSession openIdConnectSession =
			_openIdConnectSessionLocalService.fetchOpenIdConnectSession(
				userId, configurationPid);

		if (openIdConnectSession == null) {
			openIdConnectSession =
				_openIdConnectSessionLocalService.createOpenIdConnectSession(
					_counterLocalService.increment(
						OpenIdConnectSession.class.getName()));
		}

		_updateOpenIdConnectSession(
			oidcTokens.getAccessToken(), configurationPid,
			oidcTokens.getIDTokenString(), oidcTokens.getRefreshToken(),
			openIdConnectSession, providerName, userId);

		return openIdConnectSession.getOpenIdConnectSessionId();
	}

	@Modified
	protected void activate(
			BundleContext bundleContext, Map<String, Object> properties)
		throws Exception {

		_bundleContext = bundleContext;

		OpenIdConnectConfiguration openIdConnectConfiguration =
			ConfigurableUtil.createConfigurable(
				OpenIdConnectConfiguration.class, properties);

		if (openIdConnectConfiguration.tokenRefreshOffset() < 30) {
			throw new IllegalArgumentException(
				"Token refresh offset needs to be at least 30 seconds");
		}

		_tokenRefreshOffsetMillis =
			openIdConnectConfiguration.tokenRefreshOffset() * Time.SECOND;

		if (!openIdConnectConfiguration.enabled()) {
			deactivate();

			return;
		}

		_registerServices(
			bundleContext,
			OpenIdConnectDestinationNames.OPENID_CONNECT_TOKEN_REFRESH);
	}

	@Deactivate
	protected void deactivate() {
		_unregisterServices();
	}

	private AccessToken _extendOpenIdConnectSession(
		OpenIdConnectSession openIdConnectSession) {

		if (Validator.isNull(openIdConnectSession.getRefreshToken())) {
			_openIdConnectSessionLocalService.deleteOpenIdConnectSession(
				openIdConnectSession);

			return null;
		}

		RefreshToken refreshToken = new RefreshToken(
			openIdConnectSession.getRefreshToken());

		try {
			OpenIdConnectProvider<OIDCClientMetadata, OIDCProviderMetadata>
				openIdConnectProvider =
					_openIdConnectProviderRegistry.findOpenIdConnectProvider(
						CompanyThreadLocal.getCompanyId(),
						openIdConnectSession.getProviderName());

			OIDCTokens oidcTokens = OpenIdConnectTokenRequestUtil.request(
				openIdConnectProvider, refreshToken);

			_updateOpenIdConnectSession(
				oidcTokens.getAccessToken(), openIdConnectSession,
				oidcTokens.getRefreshToken());

			return oidcTokens.getAccessToken();
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(exception);
			}

			_openIdConnectSessionLocalService.deleteOpenIdConnectSession(
				openIdConnectSession);
		}

		return null;
	}

	private AccessToken _getAccessToken(
		OpenIdConnectSession openIdConnectSession) {

		try {
			return AccessToken.parse(
				JSONObjectUtils.parse(openIdConnectSession.getAccessToken()));
		}
		catch (ParseException parseException) {
			if (_log.isWarnEnabled()) {
				_log.warn(parseException, parseException);
			}

			return null;
		}
	}

	private void _registerServices(
		BundleContext bundleContext, String destinationName) {

		if (_messageListenerServiceRegistration != null) {
			return;
		}

		DestinationConfiguration destinationConfiguration =
			DestinationConfiguration.createSerialDestinationConfiguration(
				destinationName);

		Destination destination = _destinationFactory.createDestination(
			destinationConfiguration);

		Dictionary<String, Object> dictionary =
			HashMapDictionaryBuilder.<String, Object>put(
				"destination.name", destination.getName()
			).build();

		_destinationServiceRegistration = bundleContext.registerService(
			Destination.class, destination, dictionary);

		_messageListenerServiceRegistration = bundleContext.registerService(
			MessageListener.class, new TokenRefreshMessageListener(),
			dictionary);
	}

	private void _unregisterServices() {
		if (_messageListenerServiceRegistration != null) {
			_messageListenerServiceRegistration.unregister();

			_messageListenerServiceRegistration = null;
		}

		if (_destinationServiceRegistration != null) {
			Destination destination = _bundleContext.getService(
				_destinationServiceRegistration.getReference());

			destination.destroy();

			_destinationServiceRegistration.unregister();

			_destinationServiceRegistration = null;
		}
	}

	private void _updateOpenIdConnectSession(
		AccessToken accessToken, OpenIdConnectSession openIdConnectSession,
		RefreshToken refreshToken) {

		openIdConnectSession.setAccessToken(accessToken.toJSONString());

		if (refreshToken != null) {
			openIdConnectSession.setRefreshToken(refreshToken.toString());
		}

		openIdConnectSession.setModifiedDate(new Date());

		_openIdConnectSessionLocalService.updateOpenIdConnectSession(
			openIdConnectSession);
	}

	private void _updateOpenIdConnectSession(
		AccessToken accessToken, String configurationPid, String idTokenString,
		RefreshToken refreshToken, OpenIdConnectSession openIdConnectSession,
		String providerName, long userId) {

		openIdConnectSession.setUserId(userId);
		openIdConnectSession.setConfigurationPid(configurationPid);
		openIdConnectSession.setIdToken(idTokenString);
		openIdConnectSession.setProviderName(providerName);

		_updateOpenIdConnectSession(
			accessToken, openIdConnectSession, refreshToken);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		OfflineOpenIdConnectSessionManager.class);

	private volatile BundleContext _bundleContext;

	@Reference
	private CounterLocalService _counterLocalService;

	@Reference
	private DestinationFactory _destinationFactory;

	private ServiceRegistration<Destination> _destinationServiceRegistration;

	@Reference
	private MessageBus _messageBus;

	private ServiceRegistration<MessageListener>
		_messageListenerServiceRegistration;

	@Reference
	private OpenIdConnectProviderRegistry
		<OIDCClientMetadata, OIDCProviderMetadata>
			_openIdConnectProviderRegistry;

	@Reference
	private OpenIdConnectSessionLocalService _openIdConnectSessionLocalService;

	private volatile long _tokenRefreshOffsetMillis = 60 * Time.SECOND;

	private class TokenRefreshMessageListener extends BaseMessageListener {

		@Override
		protected void doReceive(Message message) throws Exception {
			Long openIdConnectSessionId = message.getLong(
				"openIdConnectSessionId");

			if ((openIdConnectSessionId == null) ||
				(openIdConnectSessionId < 1)) {

				return;
			}

			OpenIdConnectSession openIdConnectSession =
				_openIdConnectSessionLocalService.fetchOpenIdConnectSession(
					openIdConnectSessionId);

			if (openIdConnectSession == null) {
				return;
			}

			Date modifiedDate = openIdConnectSession.getModifiedDate();

			if (System.currentTimeMillis() <=
					(modifiedDate.getTime() + _tokenRefreshOffsetMillis)) {

				return;
			}

			_extendOpenIdConnectSession(openIdConnectSession);
		}

	}

}
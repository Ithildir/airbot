/**
 * Copyright (c) 2017 Andrea Di Giorgi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.ithildir.airbot.server.api.ai;

import ai.api.model.AIResponse;
import ai.api.model.Fulfillment;
import ai.api.model.Result;

import com.github.ithildir.airbot.model.Location;
import com.github.ithildir.airbot.service.GeoService;
import com.github.ithildir.airbot.service.UserService;
import com.github.ithildir.airbot.util.AirQualityMessageBuilder;
import com.github.ithildir.airbot.util.ApiAiUtil;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.Locale;
import java.util.Map;

/**
 * @author Andrea Di Giorgi
 */
public class AirQualityApiAiFulfillmentBuilder
	implements ApiAiFulfillmentBuilder {

	public AirQualityApiAiFulfillmentBuilder(
		AirQualityMessageBuilder airQualityMessageBuilder,
		GeoService geoService, UserService userService) {

		_airQualityMessageBuilder = airQualityMessageBuilder;
		_geoService = geoService;
		_userService = userService;
	}

	@Override
	public Future<Fulfillment> build(
		AIResponse aiResponse, JsonObject responseJsonObject) {

		Locale locale = new Locale(aiResponse.getLang());
		String locationString = _getLocationString(aiResponse);

		if (locationString != null) {
			return _buildLocationFulfillment(locationString, locale);
		}
		else {
			return _buildUserFulfillment(
				locale, aiResponse, responseJsonObject);
		}
	}

	@Override
	public String getAction() {
		return "AIR_QUALITY";
	}

	private Future<Fulfillment> _buildFulfillment(
		Location location, String locationString, Locale locale) {

		Future<String> messageFuture = _airQualityMessageBuilder.getMessage(
			location, locationString, locale);

		return messageFuture.compose(
			message -> {
				Fulfillment fulfillment = new Fulfillment();

				fulfillment.setSpeech(message);

				return Future.succeededFuture(fulfillment);
			});
	}

	private Future<Fulfillment> _buildLocationFulfillment(
		String locationString, Locale locale) {

		Future<Location> locationFuture = Future.future();

		_geoService.getLocationByQuery(locationString, locationFuture);

		return locationFuture.compose(
			location -> _buildFulfillment(location, locationString, locale));
	}

	private Future<Fulfillment> _buildUserFulfillment(
		Locale locale, AIResponse aiResponse, JsonObject responseJsonObject) {

		String userId = aiResponse.getSessionId();

		Future<Location> locationFuture = _getReponseLocation(
			responseJsonObject);

		locationFuture = locationFuture.compose(
			location -> {
				if (location != null) {
					return Future.succeededFuture(location);
				}

				Future<Location> future = Future.future();

				_userService.getUserLocation(userId, future);

				return future;
			});

		return locationFuture.compose(
			location -> {
				if (location == null) {
					Fulfillment fulfillment =
						ApiAiUtil.buildGooglePermissionFulfillment(
							"in-order-to-get-information-about-the-air-" +
								"quality-around-you",
							"DEVICE_PRECISE_LOCATION", locale);

					return Future.succeededFuture(fulfillment);
				}

				_updateUserLocation(userId, location);

				return _buildFulfillment(location, null, locale);
			});
	}

	private String _getLocationString(AIResponse aiResponse) {
		Result result = aiResponse.getResult();

		Map<String, JsonElement> parameters = result.getParameters();

		JsonElement jsonElement = parameters.get("location");

		if ((jsonElement == null) || !jsonElement.isJsonObject()) {
			return null;
		}

		JsonObject jsonObject = jsonElement.getAsJsonObject();

		jsonElement = jsonObject.get("business-name");

		if (jsonElement == null) {
			jsonElement = jsonObject.get("city");
		}

		return jsonElement.getAsString();
	}

	private Future<Location> _getReponseLocation(
		JsonObject responseJsonObject) {

		double[] coordinates = ApiAiUtil.getResponseCoordinates(
			responseJsonObject);

		if (coordinates == null) {
			return Future.succeededFuture(null);
		}

		Future<Location> future = Future.future();

		_geoService.getLocationByCoordinates(
			coordinates[0], coordinates[1], future);

		return future;
	}

	private void _updateUserLocation(String userId, Location location) {
		_userService.updateUserLocation(
			userId, location.getLatitude(), location.getLongitude(),
			location.getCountry(),
			asyncResult -> {
				if (asyncResult.failed()) {
					_logger.error(
						"Unable to update user location for {0} to {1}",
						asyncResult.cause(), userId, location);
				}
			});
	}

	private static final Logger _logger = LoggerFactory.getLogger(
		AirQualityApiAiFulfillmentBuilder.class);

	private final AirQualityMessageBuilder _airQualityMessageBuilder;
	private final GeoService _geoService;
	private final UserService _userService;

}
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

package com.github.ithildir.airbot.util;

import ai.api.model.Fulfillment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.Locale;

/**
 * @author Andrea Di Giorgi
 */
public class ApiAiUtil {

	public static Fulfillment buildGooglePermissionFulfillment(
		String key, String permission, Locale locale) {

		Fulfillment fulfillment = new Fulfillment();

		JsonObject dataJsonObject = new JsonObject();

		dataJsonObject.addProperty(
			"@type",
			"type.googleapis.com/google.actions.v2.PermissionValueSpec");
		dataJsonObject.addProperty("optContext", LanguageUtil.get(locale, key));

		JsonArray permissionsJsonArray = new JsonArray(1);

		permissionsJsonArray.add(permission);

		dataJsonObject.add("permissions", permissionsJsonArray);

		JsonObject systemIntentJsonObject = new JsonObject();

		systemIntentJsonObject.addProperty(
			"intent", "actions.intent.PERMISSION");
		systemIntentJsonObject.add("data", dataJsonObject);

		JsonObject googleJsonObject = new JsonObject();

		googleJsonObject.addProperty("expectUserResponse", true);
		googleJsonObject.add("systemIntent", systemIntentJsonObject);

		fulfillment.setData(
			Collections.singletonMap("google", googleJsonObject));

		fulfillment.setSpeech("Speechless");

		return fulfillment;
	}

	public static double[] getResponseCoordinates(
		JsonObject responseJsonObject) {

		JsonObject originalRequestJsonObject =
			responseJsonObject.getAsJsonObject("originalRequest");

		JsonObject dataJsonObject = originalRequestJsonObject.getAsJsonObject(
			"data");

		JsonObject deviceJsonObject = dataJsonObject.getAsJsonObject("device");

		if (deviceJsonObject == null) {
			return null;
		}

		JsonObject locationJsonObject = deviceJsonObject.getAsJsonObject(
			"location");

		if (locationJsonObject == null) {
			return null;
		}

		JsonObject coordinatesJsonObject = locationJsonObject.getAsJsonObject(
			"coordinates");

		if (coordinatesJsonObject == null) {
			return null;
		}

		JsonElement latitudeJsonElement = coordinatesJsonObject.get("latitude");
		JsonElement longitudeJsonElement = coordinatesJsonObject.get(
			"longitude");

		return new double[] {
			latitudeJsonElement.getAsDouble(),
			longitudeJsonElement.getAsDouble()
		};
	}

}
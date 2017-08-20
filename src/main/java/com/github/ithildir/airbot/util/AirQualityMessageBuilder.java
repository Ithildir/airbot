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

import com.github.ithildir.airbot.model.Location;
import com.github.ithildir.airbot.model.Measurement;
import com.github.ithildir.airbot.service.MeasurementService;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.ocpsoft.prettytime.PrettyTime;

/**
 * @author Andrea Di Giorgi
 */
public class AirQualityMessageBuilder {

	public AirQualityMessageBuilder(
		Map<String, MeasurementService> measurementServices) {

		_measurementServices = measurementServices;
	}

	public Future<String> getMessage(
		Location location, String locationString, Locale locale) {

		Future<String> future = Future.future();

		MeasurementService measurementService = _getMeasurementService(
			location);

		Future<Measurement> measurementFuture = Future.future();
		Future<String> nameFuture = Future.future();

		measurementService.getMeasurement(
			location.getLatitude(), location.getLongitude(), measurementFuture);

		measurementService.getName(nameFuture);

		CompositeFuture compositeFuture = CompositeFuture.all(
			measurementFuture, nameFuture);

		compositeFuture.setHandler(
			asyncResult -> {
				if (asyncResult.failed()) {
					future.fail(asyncResult.cause());

					return;
				}

				CompositeFuture resultCompositeFuture = asyncResult.result();

				Measurement measurement =
					(Measurement)resultCompositeFuture.resultAt(0);
				String name = (String)resultCompositeFuture.resultAt(1);

				String message = _getMessage(
					measurement, name, locationString, locale);

				future.complete(message);
			});

		return future;
	}

	private String _getAQILevel(int aqi, Locale locale) {
		String key = "good";

		if ((aqi >= 51) && (aqi <= 100)) {
			key = "moderate";
		}
		else if ((aqi >= 101) && (aqi <= 150)) {
			key = "unhealthy-for-sensitive-groups";
		}
		else if ((aqi >= 151) && (aqi <= 200)) {
			key = "unhealthy";
		}
		else if ((aqi >= 201) && (aqi <= 300)) {
			key = "very-unhealthy";
		}
		else if (aqi > 300) {
			key = "hazardous";
		}

		return LanguageUtil.get(locale, key);
	}

	private MeasurementService _getMeasurementService(Location location) {
		String country = location.getCountry();

		MeasurementService measurementService = _measurementServices.get(
			country);

		if (measurementService == null) {
			measurementService = _measurementServices.get(null);
		}

		return measurementService;
	}

	private String _getMessage(
		Measurement measurement, String name, String locationString,
		Locale locale) {

		if (locationString == null) {
			if (measurement != null) {
				locationString = measurement.getCity();
			}
			else {
				locationString = LanguageUtil.get(locale, "this-location");
			}
		}

		if (measurement == null) {
			return LanguageUtil.format(
				locale, "the-air-quality-measurement-for-x-is-not-available",
				locationString);
		}

		name = LanguageUtil.get(locale, name);

		String aqiLevel = _getAQILevel(measurement.getAqi(), locale);
		String mainPollutant = LanguageUtil.get(
			locale, "pollutant-" + measurement.getMainPollutant());

		PrettyTime prettyTime = new PrettyTime(locale);

		String time = prettyTime.format(new Date(measurement.getTime()));

		return LanguageUtil.format(
			locale,
			"according-to-x-the-air-quality-in-x-was-x-x,-with-x-as-main-" +
				"pollutant",
			name, locationString, aqiLevel, time, mainPollutant);
	}

	private final Map<String, MeasurementService> _measurementServices;

}
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

package com.github.ithildir.airbot.service;

import com.github.ithildir.airbot.model.Measurement;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ProxyHelper;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Andrea Di Giorgi
 */
@ProxyGen
public interface MeasurementService {

	public static String getAddress(String country) {
		String address = MeasurementService.class.getName();

		if (StringUtils.isNotBlank(country)) {
			address += "." + country;
		}

		return address;
	}

	public static MeasurementService getInstance(Vertx vertx, String country) {
		return ProxyHelper.createProxy(
			MeasurementService.class, vertx, getAddress(country));
	}

	public void getMeasurement(
		double latitude, double longitude,
		Handler<AsyncResult<Measurement>> handler);

	public void getName(Handler<AsyncResult<String>> handler);

	public void init(Handler<AsyncResult<Void>> handler);

}
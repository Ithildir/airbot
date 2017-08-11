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

package com.github.ithildir.airbot;

import com.github.ithildir.airbot.service.MeasurementService;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * @author Andrea Di Giorgi
 */
public abstract class BaseMeasurementServiceVerticle
	extends BaseServiceVerticle<MeasurementService> {

	@Override
	protected String getAddress() {
		return MeasurementService.getAddress(getCountry());
	}

	protected abstract String getCountry();

	protected long getInitInterval() {
		return 0;
	}

	@Override
	protected Class<MeasurementService> getServiceInterface() {
		return MeasurementService.class;
	}

	@Override
	protected Future<Void> start(JsonObject configJsonObject) {
		Future<Void> future = super.start(configJsonObject);

		future = future.compose(
			v -> {
				return _init();
			});

		long initInterval = getInitInterval();

		if (initInterval > 0) {
			vertx.setPeriodic(
				initInterval,
				timerId -> {
					_init();
				});
		}

		return future;
	}

	private Future<Void> _init() {
		Future<Void> future = Future.future();

		MeasurementService measurementService = ProxyHelper.createProxy(
			MeasurementService.class, vertx, getAddress());

		measurementService.init(future);

		return future;
	}

}
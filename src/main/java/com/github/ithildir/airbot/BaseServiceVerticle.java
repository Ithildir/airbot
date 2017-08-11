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

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * @author Andrea Di Giorgi
 */
public abstract class BaseServiceVerticle<T> extends AbstractVerticle {

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		ConfigRetriever configRetriever = ConfigRetriever.create(vertx);

		configRetriever.getConfig(
			asyncResult -> {
				if (asyncResult.failed()) {
					startFuture.fail(asyncResult.cause());

					return;
				}

				JsonObject configJsonObject = asyncResult.result();

				Future<Void> future = start(configJsonObject);

				future.setHandler(startFuture);
			});
	}

	@Override
	public void stop() throws Exception {
		ProxyHelper.unregisterService(_messageConsumer);
	}

	protected abstract String getAddress();

	protected abstract T getServiceImpl(JsonObject configJsonObject);

	protected abstract Class<T> getServiceInterface();

	protected Future<Void> start(JsonObject configJsonObject) {
		_messageConsumer = ProxyHelper.registerService(
			getServiceInterface(), vertx, getServiceImpl(configJsonObject),
			getAddress());

		return Future.succeededFuture();
	}

	private MessageConsumer<JsonObject> _messageConsumer;

}
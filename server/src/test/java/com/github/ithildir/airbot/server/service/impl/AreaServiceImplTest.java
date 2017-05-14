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

package com.github.ithildir.airbot.server.service.impl;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Andrea Di Giorgi
 */
@RunWith(VertxUnitRunner.class)
public class AreaServiceImplTest
	extends BaseRecordServiceImplTestCase<AreaServiceImpl> {

	@Test
	public void testGetArea(TestContext testContext) {
		Async async = testContext.async(2);

		AreaServiceImpl areaServiceImpl = getRecordServiceImpl();

		areaServiceImpl.getArea(
			"12845",
			asyncResult -> {
				testContext.assertEquals(
					"Adirondacks Region", asyncResult.result());

				async.countDown();
			});

		areaServiceImpl.getArea(
			"00000",
			asyncResult -> {
				testContext.assertTrue(asyncResult.failed());

				async.countDown();
			});

		async.awaitSuccess();
	}

	@Test
	public void testHeaderSkipped(TestContext testContext) {
		Async async = testContext.async();

		AreaServiceImpl areaServiceImpl = getRecordServiceImpl();

		areaServiceImpl.getArea(
			"Zipcode",
			asyncResult -> {
				testContext.assertTrue(asyncResult.failed());

				async.countDown();
			});

		async.awaitSuccess();
	}

	@Override
	protected JsonObject createConfigJsonObject(String url) {
		JsonObject configJsonObject = super.createConfigJsonObject(url);

		configJsonObject.put(
			AreaServiceImpl.CONFIG_KEY_VALUE_DELIMITER_PATTERN, "\\|");
		configJsonObject.put(AreaServiceImpl.CONFIG_KEY_VALUE_INDEX_AREA, 0);
		configJsonObject.put(
			AreaServiceImpl.CONFIG_KEY_VALUE_INDEX_ZIP_CODE, 2);

		return configJsonObject;
	}

	@Override
	protected AreaServiceImpl createRecordServiceImpl(
		Vertx vertx, JsonObject configJsonObject) {

		return new AreaServiceImpl(vertx, configJsonObject);
	}

	@Override
	protected String getFileName() {
		return "com/github/ithildir/airbot/server/service/impl/dependencies" +
			"/cityzipcodes.dat";
	}

}
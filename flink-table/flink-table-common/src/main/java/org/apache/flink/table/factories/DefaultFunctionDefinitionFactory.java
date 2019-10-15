/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.table.factories;

import org.apache.flink.table.catalog.CatalogFunction;
import org.apache.flink.table.functions.AggregateFunction;
import org.apache.flink.table.functions.AggregateFunctionDefinition;
import org.apache.flink.table.functions.FunctionDefinition;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.table.functions.ScalarFunctionDefinition;
import org.apache.flink.table.functions.TableAggregateFunction;
import org.apache.flink.table.functions.TableAggregateFunctionDefinition;
import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.table.functions.TableFunctionDefinition;
import org.apache.flink.table.functions.UserDefinedFunction;

/**
 * A default factory to instantiate {@link FunctionDefinition}.
 */
public class DefaultFunctionDefinitionFactory implements FunctionDefinitionFactory {

	public static final FunctionDefinitionFactory INSTANCE = new DefaultFunctionDefinitionFactory();

	private DefaultFunctionDefinitionFactory() {}

	@Override
	public FunctionDefinition createFunctionDefinition(String name, CatalogFunction catalogFunction) {
		// Currently only handles Java class-based functions
		Object func;
		try {
			func = Thread.currentThread().getContextClassLoader().loadClass(catalogFunction.getClassName()).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new IllegalStateException(
				String.format("Failed instantiating '%s'", catalogFunction.getClassName())
			);
		}

		UserDefinedFunction udf = (UserDefinedFunction) func;

		if (udf instanceof ScalarFunction) {
			return new ScalarFunctionDefinition(
				name,
				(ScalarFunction) udf
			);
		} else if (udf instanceof TableFunction) {
			TableFunction t = (TableFunction) udf;
			return new TableFunctionDefinition(
				name,
				t,
				t.getResultType()
			);
		} else if (udf instanceof AggregateFunction) {
			AggregateFunction a = (AggregateFunction) udf;

			return new AggregateFunctionDefinition(
				name,
				a,
				a.getAccumulatorType(),
				a.getResultType()
			);
		} else if (udf instanceof TableAggregateFunction) {
			TableAggregateFunction a = (TableAggregateFunction) udf;

			return new TableAggregateFunctionDefinition(
				name,
				a,
				a.getAccumulatorType(),
				a.getResultType()
			);
		} else {
			throw new UnsupportedOperationException(
				String.format("Function %s should be of ScalarFunction, TableFunction, AggregateFunction, or TableAggregateFunction", catalogFunction.getClassName())
			);
		}
	}
}

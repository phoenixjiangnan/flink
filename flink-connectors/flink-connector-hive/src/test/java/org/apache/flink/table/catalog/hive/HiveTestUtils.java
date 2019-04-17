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

package org.apache.flink.table.catalog.hive;

import org.apache.hadoop.hive.conf.HiveConf;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

/**
 * Test utils for Hive connector.
 */
public class HiveTestUtils {
	private static final String HIVE_SITE_XML = "hive-site.xml";
	private static final String HIVE_WAREHOUSE_URI_FORMAT = "jdbc:derby:;databaseName=%s;create=true";
	private static final TemporaryFolder TEMPORARY_FOLDER = new TemporaryFolder();

	public static String warehouseDir;
	private static String warehouseUri;

	/**
	 * Create a GenericHiveMetastoreCatalog with an embedded Hive Metastore.
	 * @throws IOException
	 */
		public static GenericHiveMetastoreCatalog createGenericHiveMetastoreCatalog() throws IOException {
		return new GenericHiveMetastoreCatalog("test", getHiveConf());
	}

	private static HiveConf getHiveConf() throws IOException {
		ClassLoader classLoader = new HiveTestUtils().getClass().getClassLoader();
		HiveConf.setHiveSiteLocation(classLoader.getResource(HIVE_SITE_XML));

		TEMPORARY_FOLDER.create();
		warehouseDir = TEMPORARY_FOLDER.newFolder().getAbsolutePath() + "/metastore_db";
		warehouseUri = String.format(HIVE_WAREHOUSE_URI_FORMAT, warehouseDir);
		HiveConf hiveConf = new HiveConf();
		hiveConf.setBoolVar(HiveConf.ConfVars.METASTORE_SCHEMA_VERIFICATION, false);
		hiveConf.setBoolean("datanucleus.schema.autoCreateTables", true);
		hiveConf.setVar(HiveConf.ConfVars.METASTOREWAREHOUSE, TEMPORARY_FOLDER.newFolder("hive_warehouse").getAbsolutePath());
		hiveConf.setVar(HiveConf.ConfVars.METASTORECONNECTURLKEY, warehouseUri);

		return hiveConf;
	}
}

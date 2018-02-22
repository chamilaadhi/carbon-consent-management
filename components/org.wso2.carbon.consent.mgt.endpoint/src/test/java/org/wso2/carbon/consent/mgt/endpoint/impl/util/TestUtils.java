/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.consent.mgt.endpoint.impl.util;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;

import java.io.FileInputStream;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PublicKey;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

public class TestUtils {

    public static Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    public static final String DB_NAME = "ConsentDB";
    public static final String H2_SCRIPT_NAME = "h2.sql";

    public static Connection spyConnection(Connection connection) throws SQLException {
        Connection spy = spy(connection);
        doNothing().when(spy).close();
        return spy;
    }

    public static Connection spyConnectionWithError(Connection connection) throws SQLException {
        Connection spy = spy(connection);
        doThrow(new SQLException("Test Exception")).when(spy).prepareStatement(anyString());
        return spy;
    }

    public static String getFilePath(String fileName) {
        if (StringUtils.isNotBlank(fileName)) {
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "dbscripts", fileName)
                        .toString();
        }
        throw new IllegalArgumentException("DB Script file name cannot be empty.");
    }

    public static void initiateH2Base() throws Exception {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:h2:mem:test" + DB_NAME);
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().executeUpdate("RUNSCRIPT FROM '" + getFilePath(H2_SCRIPT_NAME) + "'");
        }
        dataSourceMap.put(DB_NAME, dataSource);
    }

    public static void closeH2Base() throws Exception {
        BasicDataSource dataSource =  dataSourceMap.get(DB_NAME);
        if(dataSource != null) {
            dataSource.close();
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSourceMap.get(DB_NAME) != null) {
            return dataSourceMap.get(DB_NAME).getConnection();
        }
        throw new RuntimeException("No data source initiated for database: " + DB_NAME);
    }


    public static KeyStore loadKeyStoreFromFileSystem(String keyStorePath, String password, String type) {
        try (FileInputStream inputStream = new FileInputStream(keyStorePath)) {
            KeyStore keyStore = KeyStore.getInstance(type);
            keyStore.load(inputStream, password.toCharArray());
            return keyStore;
        } catch (Exception e) {
            String errorMsg = "Error loading the key store from the given location.";
            throw new SecurityException(errorMsg, e);
        }
    }

    public static PublicKey getPublicKey(KeyStore keyStore, String alias) throws KeyStoreException {
        return keyStore.getCertificate(alias).getPublicKey();
    }

    public static String getFilePathInConfDirectory(String fileName) {
        if (StringUtils.isNotBlank(fileName)) {
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "conf", fileName).toString();
        }
        throw new IllegalArgumentException("File name cannot be empty.");
    }

}

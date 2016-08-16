// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.jdbc;

import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.components.jdbc.runtime.JDBCSourceOrSink;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionDefinition;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionProperties;
import org.talend.daikon.properties.ValidationResult;

public class JDBCConnectionTestIT {

    private static String driverClass;

    private static String jdbcUrl;

    private static String userId;

    private static String password;

    private static JDBCConnectionModule connectionInfo;

    @BeforeClass
    public static void init() throws Exception {
        java.util.Properties props = new java.util.Properties();
        try (InputStream is = JDBCConnectionTestIT.class.getClassLoader().getResourceAsStream("connection.properties")) {
            props = new java.util.Properties();
            props.load(is);
        }

        driverClass = props.getProperty("driverClass");

        jdbcUrl = props.getProperty("jdbcUrl");

        userId = props.getProperty("userId");

        password = props.getProperty("password");

        connectionInfo = new JDBCConnectionModule("connection");

        connectionInfo.driverClass.setValue(driverClass);
        connectionInfo.jdbcUrl.setValue(jdbcUrl);
        connectionInfo.userPassword.userId.setValue(userId);
        connectionInfo.userPassword.password.setValue(password);
    }

    @Test
    public void validate() {
        JDBCSourceOrSink source = createSource();

        ValidationResult result = source.validate(null);
        assertTrue(result.getStatus() == ValidationResult.Result.OK);
    }

    private JDBCSourceOrSink createSource() {
        TJDBCConnectionDefinition definition = new TJDBCConnectionDefinition();
        TJDBCConnectionProperties properties = (TJDBCConnectionProperties) definition.createRuntimeProperties();

        // TODO now framework doesn't support to load the JDBC jar by the setting
        // properties.connection.driverJar.setValue("port", props.getProperty("port"));
        properties.connection.driverClass.setValue(driverClass);
        properties.connection.jdbcUrl.setValue(jdbcUrl);
        properties.connection.userPassword.userId.setValue(userId);
        properties.connection.userPassword.password.setValue(password);

        JDBCSourceOrSink source = (JDBCSourceOrSink) definition.getRuntime();
        source.initialize(null, properties);

        return source;
    }

}

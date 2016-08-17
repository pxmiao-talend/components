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

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.components.jdbc.runtime.JDBCRollbackSourceOrSink;
import org.talend.components.jdbc.runtime.JDBCSink;
import org.talend.components.jdbc.runtime.JDBCSourceOrSink;
import org.talend.components.jdbc.runtime.JDBCTemplate;
import org.talend.components.jdbc.runtime.writer.JDBCOutputInsertWriter;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionDefinition;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionProperties;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputDefinition;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputProperties;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputProperties.DataAction;
import org.talend.components.jdbc.tjdbcrollback.TJDBCRollbackDefinition;
import org.talend.components.jdbc.tjdbcrollback.TJDBCRollbackProperties;
import org.talend.daikon.properties.ValidationResult;

public class JDBCRollbackTestIT {

    private static String driverClass;

    private static String jdbcUrl;

    private static String userId;

    private static String password;

    private static String tablename;

    private static JDBCConnectionModule connectionInfo;

    private final String refComponentId = "tJDBCConnection1";

    RuntimeContainer container = new RuntimeContainer() {

        private Map<String, Object> map = new HashMap<>();

        public Object getComponentData(String componentId, String key) {
            return map.get(componentId + "_" + key);
        }

        public void setComponentData(String componentId, String key, Object data) {
            map.put(componentId + "_" + key, data);
        }

        public String getCurrentComponentId() {
            return refComponentId;
        }

        @Override
        public Object getGlobalData(String key) {
            return null;
        }

    };

    @BeforeClass
    public static void init() throws Exception {
        java.util.Properties props = new java.util.Properties();
        try (InputStream is = JDBCOutputTestIT.class.getClassLoader().getResourceAsStream("connection.properties")) {
            props = new java.util.Properties();
            props.load(is);
        }

        driverClass = props.getProperty("driverClass");

        jdbcUrl = props.getProperty("jdbcUrl");

        userId = props.getProperty("userId");

        password = props.getProperty("password");

        tablename = props.getProperty("tablename");

        connectionInfo = new JDBCConnectionModule("connection");

        connectionInfo.driverClass.setValue(driverClass);
        connectionInfo.jdbcUrl.setValue(jdbcUrl);
        connectionInfo.userPassword.userId.setValue(userId);
        connectionInfo.userPassword.password.setValue(password);
    }

    @AfterClass
    public static void clean() throws ClassNotFoundException, SQLException {
        cleanTable();
    }

    @Before
    public void before() throws ClassNotFoundException, SQLException, Exception {
        prepareTableAndData();
    }

    private static void cleanTable() throws ClassNotFoundException, SQLException {
        Connection conn = JDBCTemplate.createConnection(connectionInfo);

        try {
            dropTestTable(conn);
        } catch (Exception e) {
            // do nothing
        }

        conn.close();
    }

    private void prepareTableAndData() throws ClassNotFoundException, SQLException, Exception {
        Connection conn = JDBCTemplate.createConnection(connectionInfo);

        try {
            dropTestTable(conn);
        } catch (Exception e) {
            // do nothing
        }
        createTestTable(conn);
        loadTestData(conn);

        conn.close();
    }

    @Test
    public void testRollback() throws IOException, ClassNotFoundException, SQLException {
        // connection part
        TJDBCConnectionDefinition connectionDefinition = new TJDBCConnectionDefinition();
        TJDBCConnectionProperties connectionProperties = (TJDBCConnectionProperties) connectionDefinition
                .createRuntimeProperties();

        // TODO now framework doesn't support to load the JDBC jar by the setting
        // properties.connection.driverJar.setValue("port", props.getProperty("port"));
        connectionProperties.connection.driverClass.setValue(driverClass);
        connectionProperties.connection.jdbcUrl.setValue(jdbcUrl);
        connectionProperties.connection.userPassword.userId.setValue(userId);
        connectionProperties.connection.userPassword.password.setValue(password);

        JDBCSourceOrSink sourceOrSink = (JDBCSourceOrSink) connectionDefinition.getRuntime();
        sourceOrSink.initialize(null, connectionProperties);

        ValidationResult result = sourceOrSink.validate(container);
        assertTrue(result.getStatus() == ValidationResult.Result.OK);

        // output part
        TJDBCOutputDefinition outputDefinition = new TJDBCOutputDefinition();
        TJDBCOutputProperties outputProperties = (TJDBCOutputProperties) outputDefinition.createRuntimeProperties();

        outputProperties.main.schema.setValue(createTestSchema());
        outputProperties.updateOutputSchemas();

        outputProperties.tablename.setValue(tablename);

        outputProperties.dataAction.setValue(DataAction.INSERT);

        outputProperties.referencedComponent.componentInstanceId.setValue(refComponentId);

        JDBCSink sink = (JDBCSink) outputDefinition.getRuntime();
        sink.initialize(null, outputProperties);

        WriteOperation writerOperation = sink.createWriteOperation();
        writerOperation.initialize(null);
        JDBCOutputInsertWriter writer = (JDBCOutputInsertWriter) writerOperation.createWriter(null);

        writer.open("wid");

        IndexedRecord r1 = new GenericData.Record(createTestSchema());
        r1.put(0, 4);
        r1.put(1, "xiaoming");
        writer.write(r1);

        IndexedRecord r2 = new GenericData.Record(createTestSchema());
        r2.put(0, 5);
        r2.put(1, "xiaobai");
        writer.write(r2);

        assertThat(writer.getRejectedWrites(), empty());
        assertThat(writer.getSuccessfulWrites(), hasSize(2));
        assertThat(writer.getSuccessfulWrites().get(0), is(r1));
        assertThat(writer.getSuccessfulWrites().get(1), is(r2));

        writer.close();

        // rollback part
        TJDBCRollbackDefinition rollbackDefinition = new TJDBCRollbackDefinition();
        TJDBCRollbackProperties rollbackProperties = (TJDBCRollbackProperties) rollbackDefinition.createRuntimeProperties();

        rollbackProperties.referencedComponent.componentInstanceId.setValue(refComponentId);
        rollbackProperties.closeConnection.setValue(true);

        JDBCRollbackSourceOrSink rollbackSourceOrSink = (JDBCRollbackSourceOrSink) rollbackDefinition.getRuntime();
        rollbackSourceOrSink.validate(container);

        // create another session and check if the data is inserted
        Connection conn = JDBCTemplate.createConnection(connectionInfo);
        Statement statement = conn.createStatement();
        ResultSet resultset = statement.executeQuery("select count(*) from TEST");
        int count = -1;
        if (resultset.next()) {
            count = resultset.getInt(1);
        }
        statement.close();
        conn.close();

        Assert.assertEquals(3, count);

        java.sql.Connection refConnection = (java.sql.Connection) container.getComponentData(refComponentId,
                ComponentConstants.CONNECTION_KEY);
        if (refConnection != null) {
            try {
                Assert.assertTrue(refConnection.isClosed());
            } catch (SQLException e) {
                Assert.fail(e.getMessage());
            }
        }

    }

    private static void createTestTable(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.execute("create table TEST (ID int, NAME varchar(64))");
        statement.close();
    }

    private static void dropTestTable(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        statement.execute("drop table TEST");
        statement.close();
    }

    private static void loadTestData(Connection conn) throws Exception {
        PreparedStatement statement = conn.prepareStatement("insert into TEST values(?,?)");

        statement.setInt(1, 1);
        statement.setString(2, "wangwei");

        statement.executeUpdate();

        statement.setInt(1, 2);
        statement.setString(2, "gaoyan");

        statement.executeUpdate();

        statement.setInt(1, 3);
        statement.setString(2, "dabao");

        statement.executeUpdate();

        statement.close();

        conn.commit();
    }

    private static Schema createTestSchema() {
        return SchemaBuilder.builder().record("TEST").fields().name("ID").type().nullable().intType().noDefault().name("NAME")
                .type().nullable().stringType().noDefault().endRecord();
    }

}

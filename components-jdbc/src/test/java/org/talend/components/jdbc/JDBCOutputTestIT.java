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

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.SchemaBuilder.FieldAssembler;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.components.jdbc.runtime.JDBCSink;
import org.talend.components.jdbc.runtime.JDBCTemplate;
import org.talend.components.jdbc.runtime.writer.JDBCOutputDeleteWriter;
import org.talend.components.jdbc.runtime.writer.JDBCOutputInsertOrUpdateWriter;
import org.talend.components.jdbc.runtime.writer.JDBCOutputInsertWriter;
import org.talend.components.jdbc.runtime.writer.JDBCOutputUpdateOrInsertWriter;
import org.talend.components.jdbc.runtime.writer.JDBCOutputUpdateWriter;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputDefinition;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputProperties;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputProperties.DataAction;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

public class JDBCOutputTestIT {

    private static String driverClass;

    private static String jdbcUrl;

    private static String userId;

    private static String password;

    private static String tablename;

    private static JDBCConnectionModule connectionInfo;

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

    @SuppressWarnings("rawtypes")
    @Test
    public void testInsert() throws Exception {
        TJDBCOutputDefinition definition = new TJDBCOutputDefinition();
        TJDBCOutputProperties properties = (TJDBCOutputProperties) definition.createRuntimeProperties();

        // TODO now framework doesn't support to load the JDBC jar by the setting
        // properties.connection.driverJar.setValue(null);
        properties.connection.driverClass.setValue(driverClass);
        properties.connection.jdbcUrl.setValue(jdbcUrl);
        properties.connection.userPassword.userId.setValue(userId);
        properties.connection.userPassword.password.setValue(password);

        properties.main.schema.setValue(createTestSchema());
        properties.updateOutputSchemas();

        properties.tablename.setValue(tablename);

        properties.dataAction.setValue(DataAction.INSERT);

        JDBCSink sink = (JDBCSink) definition.getRuntime();
        sink.initialize(null, properties);

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
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testUpdate() throws Exception {
        TJDBCOutputDefinition definition = new TJDBCOutputDefinition();
        TJDBCOutputProperties properties = (TJDBCOutputProperties) definition.createRuntimeProperties();

        // TODO now framework doesn't support to load the JDBC jar by the setting
        // properties.connection.driverJar.setValue(null);
        properties.connection.driverClass.setValue(driverClass);
        properties.connection.jdbcUrl.setValue(jdbcUrl);
        properties.connection.userPassword.userId.setValue(userId);
        properties.connection.userPassword.password.setValue(password);

        properties.main.schema.setValue(createTestSchema2());
        properties.updateOutputSchemas();

        properties.tablename.setValue(tablename);

        properties.dataAction.setValue(DataAction.UPDATE);

        JDBCSink sink = (JDBCSink) definition.getRuntime();
        sink.initialize(null, properties);

        WriteOperation writerOperation = sink.createWriteOperation();
        writerOperation.initialize(null);
        JDBCOutputUpdateWriter writer = (JDBCOutputUpdateWriter) writerOperation.createWriter(null);

        writer.open("wid");

        IndexedRecord r1 = new GenericData.Record(createTestSchema2());
        r1.put(0, 1);
        r1.put(1, "xiaoming");
        writer.write(r1);

        IndexedRecord r2 = new GenericData.Record(createTestSchema2());
        r2.put(0, 2);
        r2.put(1, "xiaobai");
        writer.write(r2);

        assertThat(writer.getRejectedWrites(), empty());
        assertThat(writer.getSuccessfulWrites(), hasSize(2));
        assertThat(writer.getSuccessfulWrites().get(0), is(r1));
        assertThat(writer.getSuccessfulWrites().get(1), is(r2));

        writer.close();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testDelete() throws Exception {
        TJDBCOutputDefinition definition = new TJDBCOutputDefinition();
        TJDBCOutputProperties properties = (TJDBCOutputProperties) definition.createRuntimeProperties();

        // TODO now framework doesn't support to load the JDBC jar by the setting
        // properties.connection.driverJar.setValue(null);
        properties.connection.driverClass.setValue(driverClass);
        properties.connection.jdbcUrl.setValue(jdbcUrl);
        properties.connection.userPassword.userId.setValue(userId);
        properties.connection.userPassword.password.setValue(password);

        properties.main.schema.setValue(createTestSchema2());
        properties.updateOutputSchemas();

        properties.tablename.setValue(tablename);

        properties.dataAction.setValue(DataAction.DELETE);

        JDBCSink sink = (JDBCSink) definition.getRuntime();
        sink.initialize(null, properties);

        WriteOperation writerOperation = sink.createWriteOperation();
        writerOperation.initialize(null);
        JDBCOutputDeleteWriter writer = (JDBCOutputDeleteWriter) writerOperation.createWriter(null);

        writer.open("wid");

        IndexedRecord r1 = new GenericData.Record(createTestSchema2());
        r1.put(0, 1);
        writer.write(r1);

        IndexedRecord r2 = new GenericData.Record(createTestSchema2());
        r2.put(0, 2);
        writer.write(r2);

        assertThat(writer.getRejectedWrites(), empty());
        assertThat(writer.getSuccessfulWrites(), hasSize(2));
        assertThat(writer.getSuccessfulWrites().get(0), is(r1));
        assertThat(writer.getSuccessfulWrites().get(1), is(r2));

        writer.close();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testInsertOrUpodate() throws Exception {
        TJDBCOutputDefinition definition = new TJDBCOutputDefinition();
        TJDBCOutputProperties properties = (TJDBCOutputProperties) definition.createRuntimeProperties();

        // TODO now framework doesn't support to load the JDBC jar by the setting
        // properties.connection.driverJar.setValue(null);
        properties.connection.driverClass.setValue(driverClass);
        properties.connection.jdbcUrl.setValue(jdbcUrl);
        properties.connection.userPassword.userId.setValue(userId);
        properties.connection.userPassword.password.setValue(password);

        properties.main.schema.setValue(createTestSchema2());
        properties.updateOutputSchemas();

        properties.tablename.setValue(tablename);

        properties.dataAction.setValue(DataAction.INSERTORUPDATE);

        JDBCSink sink = (JDBCSink) definition.getRuntime();
        sink.initialize(null, properties);

        WriteOperation writerOperation = sink.createWriteOperation();
        writerOperation.initialize(null);
        JDBCOutputInsertOrUpdateWriter writer = (JDBCOutputInsertOrUpdateWriter) writerOperation.createWriter(null);

        writer.open("wid");

        IndexedRecord r1 = new GenericData.Record(createTestSchema2());
        r1.put(0, 1);
        r1.put(1, "xiaoming");
        writer.write(r1);

        IndexedRecord r2 = new GenericData.Record(createTestSchema2());
        r2.put(0, 2);
        r2.put(1, "xiaobai");
        writer.write(r2);

        assertThat(writer.getRejectedWrites(), empty());
        assertThat(writer.getSuccessfulWrites(), hasSize(2));
        assertThat(writer.getSuccessfulWrites().get(0), is(r1));
        assertThat(writer.getSuccessfulWrites().get(1), is(r2));

        writer.close();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testUpodateOrInsert() throws Exception {
        TJDBCOutputDefinition definition = new TJDBCOutputDefinition();
        TJDBCOutputProperties properties = (TJDBCOutputProperties) definition.createRuntimeProperties();

        // TODO now framework doesn't support to load the JDBC jar by the setting
        // properties.connection.driverJar.setValue(null);
        properties.connection.driverClass.setValue(driverClass);
        properties.connection.jdbcUrl.setValue(jdbcUrl);
        properties.connection.userPassword.userId.setValue(userId);
        properties.connection.userPassword.password.setValue(password);

        properties.main.schema.setValue(createTestSchema2());
        properties.updateOutputSchemas();

        properties.tablename.setValue(tablename);

        properties.dataAction.setValue(DataAction.UPDATEORINSERT);

        JDBCSink sink = (JDBCSink) definition.getRuntime();
        sink.initialize(null, properties);

        WriteOperation writerOperation = sink.createWriteOperation();
        writerOperation.initialize(null);
        JDBCOutputUpdateOrInsertWriter writer = (JDBCOutputUpdateOrInsertWriter) writerOperation.createWriter(null);

        writer.open("wid");

        IndexedRecord r1 = new GenericData.Record(createTestSchema2());
        r1.put(0, 1);
        r1.put(1, "xiaoming");
        writer.write(r1);

        IndexedRecord r2 = new GenericData.Record(createTestSchema2());
        r2.put(0, 2);
        r2.put(1, "xiaobai");
        writer.write(r2);

        assertThat(writer.getRejectedWrites(), empty());
        assertThat(writer.getSuccessfulWrites(), hasSize(2));
        assertThat(writer.getSuccessfulWrites().get(0), is(r1));
        assertThat(writer.getSuccessfulWrites().get(1), is(r2));

        writer.close();
    }

    private static Schema createTestSchema() {
        return SchemaBuilder.builder().record("TEST").fields().name("ID").type().nullable().intType().noDefault().name("NAME")
                .type().nullable().stringType().noDefault().endRecord();
    }

    private static Schema createTestSchema2() {
        FieldAssembler<Schema> builder = SchemaBuilder.builder().record("TEST").fields();

        Schema field = AvroUtils._int();
        field = SchemaBuilder.builder().nullable().type(field);
        field.addProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "ID");
        field.addProp(SchemaConstants.TALEND_COLUMN_IS_KEY, "true");
        builder = builder.name("ID").type(field).noDefault();

        field = AvroUtils._string();
        field = SchemaBuilder.builder().nullable().type(field);
        field.addProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "NAME");
        builder = builder.name("NAME").type(field).noDefault();

        return builder.endRecord();
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

}

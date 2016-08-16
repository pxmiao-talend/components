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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.components.jdbc.runtime.JDBCSource;
import org.talend.components.jdbc.runtime.JDBCTemplate;
import org.talend.components.jdbc.tjdbcinput.TJDBCInputDefinition;
import org.talend.components.jdbc.tjdbcinput.TJDBCInputProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.IndexedRecordConverter;
import org.talend.daikon.di.DiOutgoingSchemaEnforcer;

public class JDBCInputTestIT {

    private static String driverClass;

    private static String jdbcUrl;

    private static String userId;

    private static String password;

    private static String tablename;

    private static String sql;

    private static JDBCConnectionModule connectionInfo;

    @BeforeClass
    public static void init() throws Exception {
        java.util.Properties props = new java.util.Properties();
        try (InputStream is = JDBCInputTestIT.class.getClassLoader().getResourceAsStream("connection.properties")) {
            props = new java.util.Properties();
            props.load(is);
        }

        driverClass = props.getProperty("driverClass");

        jdbcUrl = props.getProperty("jdbcUrl");

        userId = props.getProperty("userId");

        password = props.getProperty("password");

        tablename = props.getProperty("tablename");

        sql = props.getProperty("sql");

        connectionInfo = new JDBCConnectionModule("connection");

        connectionInfo.driverClass.setValue(driverClass);
        connectionInfo.jdbcUrl.setValue(jdbcUrl);
        connectionInfo.userPassword.userId.setValue(userId);
        connectionInfo.userPassword.password.setValue(password);

        prepareTableAndData();
    }

    @AfterClass
    public static void clean() throws ClassNotFoundException, SQLException {
        cleanTable();
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

    private static void prepareTableAndData() throws ClassNotFoundException, SQLException, Exception {
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
    public void testGetSchemaNames() throws Exception {
        JDBCSource source = createSource();

        List<NamedThing> schemaNames = source.getSchemaNames(null);
        assertTrue(schemaNames != null);
        assertTrue(!schemaNames.isEmpty());

        boolean exists = false;
        for (NamedThing name : schemaNames) {
            if ("TEST".equals(name.getName())) {
                exists = true;
                break;
            }
        }

        assertTrue(exists);
    }

    @Test
    public void testGetSchema() throws Exception {
        JDBCSource source = createSource();

        Schema schema = source.getEndpointSchema(null, "TEST");
        assertEquals("TEST", schema.getName());
        List<Field> columns = schema.getFields();
        testMetadata(columns);
    }

    private void testMetadata(List<Field> columns) {
        Schema columnSchema = columns.get(0).schema().getTypes().get(0);

        assertEquals("ID", columnSchema.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(Schema.Type.INT, columnSchema.getType());
        assertEquals(java.sql.Types.DECIMAL, columnSchema.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals(null, columnSchema.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        assertEquals(38, columnSchema.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(0, columnSchema.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals(null, columnSchema.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, columnSchema.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        columnSchema = columns.get(1).schema().getTypes().get(0);

        assertEquals("NAME", columnSchema.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(Schema.Type.STRING, columnSchema.getType());
        assertEquals(java.sql.Types.VARCHAR, columnSchema.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals(64, columnSchema.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        assertEquals(null, columnSchema.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, columnSchema.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals(null, columnSchema.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, columnSchema.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));
    }

    @Test
    public void testReader() throws Exception {
        JDBCSource source = createSource();

        Reader reader = source.createReader(null);

        reader.start();

        IndexedRecord row = (IndexedRecord) reader.getCurrent();
        BigDecimal id = (BigDecimal) row.get(0);
        String name = (String) row.get(1);

        assertEquals(new BigDecimal("1"), id);
        assertEquals("wangwei", name);

        reader.advance();

        row = (IndexedRecord) reader.getCurrent();
        id = (BigDecimal) row.get(0);
        name = (String) row.get(1);

        assertEquals(new BigDecimal("2"), id);
        assertEquals("gaoyan", name);

        reader.advance();

        row = (IndexedRecord) reader.getCurrent();
        id = (BigDecimal) row.get(0);
        name = (String) row.get(1);

        assertEquals(new BigDecimal("3"), id);
        assertEquals("dabao", name);
    }

    @Test
    public void testType() throws Exception {
        JDBCSource source = createSource();

        Reader reader = source.createReader(null);

        IndexedRecordConverter<Object, ? extends IndexedRecord> factory = null;
        // TODO get schema from the properties
        DiOutgoingSchemaEnforcer current = new DiOutgoingSchemaEnforcer(null, false);

        for (boolean available = reader.start(); available; available = reader.advance()) {
            if (factory == null)
                factory = (IndexedRecordConverter<Object, ? extends IndexedRecord>) new AvroRegistry()
                        .createIndexedRecordConverter(reader.getCurrent().getClass());

            IndexedRecord unenforced = factory.convertToAvro(reader.getCurrent());
            current.setWrapped(unenforced);

            assertEquals(BigDecimal.class, current.get(0).getClass());
            assertEquals(String.class, current.get(1).getClass());
        }
    }

    private JDBCSource createSource() {
        TJDBCInputDefinition definition = new TJDBCInputDefinition();
        TJDBCInputProperties properties = (TJDBCInputProperties) definition.createRuntimeProperties();

        // TODO now framework doesn't support to load the JDBC jar by the setting
        // properties.connection.driverJar.setValue("port", props.getProperty("port"));
        properties.connection.driverClass.setValue(driverClass);
        properties.connection.jdbcUrl.setValue(jdbcUrl);
        properties.connection.userPassword.userId.setValue(userId);
        properties.connection.userPassword.password.setValue(password);

        properties.schema.schema.setValue(createTestSchema());

        properties.tablename.setValue(tablename);
        properties.sql.setValue(sql);

        JDBCSource source = (JDBCSource) definition.getRuntime();
        source.initialize(null, properties);

        return source;
    }

    private static Schema createTestSchema() {
        return SchemaBuilder.builder().record("TEST").fields().name("ID").type().nullable().intType().noDefault().name("NAME")
                .type().nullable().stringType().noDefault().endRecord();
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

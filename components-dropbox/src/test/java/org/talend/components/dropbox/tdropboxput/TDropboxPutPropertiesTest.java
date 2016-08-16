// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.dropbox.tdropboxput;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.talend.daikon.avro.SchemaConstants.TALEND_IS_LOCKED;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field.Order;
import org.junit.Test;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.SchemaProperties;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

/**
 * Unit-tests for {@link TDropboxPutProperties} class
 */
public class TDropboxPutPropertiesTest {

    /**
     * Expected schemas for several of test cases
     */
    private static final Schema STRING_SCHEMA;

    private static final Schema BYTES_SCHEMA;

    static {
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();
        Schema bytesSchema = registry.getConverter(ByteBuffer.class).getSchema();

        Schema.Field stringContentField = new Schema.Field("content", stringSchema, null, null, Order.ASCENDING);
        STRING_SCHEMA = Schema.createRecord("dropbox", null, null, false, Collections.singletonList(stringContentField));
        STRING_SCHEMA.addProp(TALEND_IS_LOCKED, "true");

        Schema.Field bytesContentField = new Schema.Field("content", bytesSchema, null, null, Order.ASCENDING);
        BYTES_SCHEMA = Schema.createRecord("dropbox", null, null, false, Collections.singletonList(bytesContentField));
        BYTES_SCHEMA.addProp(TALEND_IS_LOCKED, "true");
    }

    /**
     * Checks {@link TDropboxPutProperties#afterUploadFrom()} shows Schema widget, hides Local File widget and sets string schema,
     * when STRING Upload From is chosen
     */
    @Test
    public void testAfterUploadFromString() {
        TDropboxPutProperties properties = new TDropboxPutProperties("root");
        properties.init();
        properties.uploadFrom.setValue(ContentType.STRING);

        properties.afterUploadFrom();

        Schema actualSchema = properties.schema.schema.getValue();
        assertEquals(STRING_SCHEMA, actualSchema);
        boolean schemaIsHidden = properties.getForm(Form.MAIN).getWidget("schema").isHidden();
        assertFalse(schemaIsHidden);
        boolean localFileIsHidden = properties.getForm(Form.MAIN).getWidget("localFile").isHidden();
        assertTrue(localFileIsHidden);
    }

    /**
     * Checks {@link TDropboxPutProperties#afterUploadFrom()} hides Schema widget, shows Local File widget and sets empty schema,
     * when LOCAL_FILE Upload From is chosen
     */
    @Test
    public void testAfterUploadFromFile() {
        TDropboxPutProperties properties = new TDropboxPutProperties("root");
        properties.init();
        properties.uploadFrom.setValue(ContentType.LOCAL_FILE);

        properties.afterUploadFrom();

        Schema actualSchema = properties.schema.schema.getValue();
        assertEquals(SchemaProperties.EMPTY_SCHEMA, actualSchema);
        boolean schemaIsHidden = properties.getForm(Form.MAIN).getWidget("schema").isHidden();
        assertTrue(schemaIsHidden);
        boolean localFileIsHidden = properties.getForm(Form.MAIN).getWidget("localFile").isHidden();
        assertFalse(localFileIsHidden);
    }

    /**
     * Checks {@link TDropboxPutProperties#afterUploadFrom()} shows Schema widget, hides Local File widget and sets bytes schema,
     * when BYTE_ARRAY Upload From is chosen
     */
    @Test
    public void testAfterUploadFromBytes() {
        TDropboxPutProperties properties = new TDropboxPutProperties("root");
        properties.init();
        properties.uploadFrom.setValue(ContentType.BYTE_ARRAY);

        properties.afterUploadFrom();

        Schema actualSchema = properties.schema.schema.getValue();
        assertEquals(BYTES_SCHEMA, actualSchema);
        boolean schemaIsHidden = properties.getForm(Form.MAIN).getWidget("schema").isHidden();
        assertFalse(schemaIsHidden);
        boolean localFileIsHidden = properties.getForm(Form.MAIN).getWidget("localFile").isHidden();
        assertTrue(localFileIsHidden);
    }

    /**
     * Checks {@link TDropboxPutProperties#afterUploadMode()} doesn't show Revision widget, when RENAME Update Mode is chosen
     */
    @Test
    public void testAfterUploadModeRename() {
        TDropboxPutProperties properties = new TDropboxPutProperties("root");
        properties.init();
        properties.uploadMode.setValue(UploadMode.RENAME);

        properties.afterUploadMode();

        boolean revisionIsHidden = properties.getForm(Form.MAIN).getWidget("revision").isHidden();
        assertTrue(revisionIsHidden);
    }

    /**
     * Checks {@link TDropboxPutProperties#afterUploadMode()} doesn't show Revision widget, when REPLACE Update Mode is chosen
     */
    @Test
    public void testAfterUploadModeReplace() {
        TDropboxPutProperties properties = new TDropboxPutProperties("root");
        properties.init();
        properties.uploadMode.setValue(UploadMode.REPLACE);

        properties.afterUploadMode();

        boolean revisionIsHidden = properties.getForm(Form.MAIN).getWidget("revision").isHidden();
        assertTrue(revisionIsHidden);
    }

    /**
     * Checks {@link TDropboxPutProperties#afterUploadMode()} shows Revision widget, when UPDATE_REVISION Update Mode is chosen
     */
    @Test
    public void testAfterUploadModeUpdateRevision() {
        TDropboxPutProperties properties = new TDropboxPutProperties("root");
        properties.init();
        properties.uploadMode.setValue(UploadMode.UPDATE_REVISION);

        properties.afterUploadMode();

        boolean revisionIsHidden = properties.getForm(Form.MAIN).getWidget("revision").isHidden();
        assertFalse(revisionIsHidden);
    }

    /**
     * Checks {@link TDropboxPutProperties#setupProperties()} sets correct initial property values
     */
    @Test
    public void testSetupProperties() {
        TDropboxPutProperties properties = new TDropboxPutProperties("root");
        properties.setupProperties();

        String pathValue = properties.path.getValue();
        UploadMode uploadModeValue = properties.uploadMode.getValue();
        String revisionValue = properties.revision.getValue();
        ContentType uploadFromValue = properties.uploadFrom.getValue();
        String localFileValue = properties.localFile.getValue();
        Schema schemaValue = properties.schema.schema.getValue();

        assertThat(pathValue, equalTo(""));
        assertThat(uploadModeValue, equalTo(UploadMode.RENAME));
        assertThat(revisionValue, equalTo(""));
        assertThat(uploadFromValue, equalTo(ContentType.STRING));
        assertThat(localFileValue, equalTo(""));
        assertThat(schemaValue, equalTo(STRING_SCHEMA));
    }

    /**
     * Checks {@link TDropboxPutProperties#refreshLayout(Form)} hides Local File widget, shows all rest widgets,
     * sets String schema in initial state
     */
    @Test
    public void testRefreshLayoutMainInitial() {
        TDropboxPutProperties properties = new TDropboxPutProperties("root");
        properties.init();

        properties.refreshLayout(properties.getForm(Form.MAIN));

        boolean connectionIsHidden = properties.getForm(Form.MAIN).getWidget("connection").isHidden();
        boolean pathIsHidden = properties.getForm(Form.MAIN).getWidget("path").isHidden();
        boolean uploadModeIsHidden = properties.getForm(Form.MAIN).getWidget("uploadMode").isHidden();
        boolean revisionIsHidden = properties.getForm(Form.MAIN).getWidget("revision").isHidden();
        boolean uploadFromIsHidden = properties.getForm(Form.MAIN).getWidget("uploadFrom").isHidden();
        boolean localFileIsHidden = properties.getForm(Form.MAIN).getWidget("localFile").isHidden();
        boolean schemaIsHidden = properties.getForm(Form.MAIN).getWidget("schema").isHidden();
        assertFalse(connectionIsHidden);
        assertFalse(pathIsHidden);
        assertFalse(uploadModeIsHidden);
        assertTrue(revisionIsHidden);
        assertFalse(uploadFromIsHidden);
        assertTrue(localFileIsHidden);
        assertFalse(schemaIsHidden);

        assertEquals(STRING_SCHEMA, properties.schema.schema.getValue());
    }

    /**
     * Checks {@link TDropboxPutProperties#refreshLayout(Form)} doesn't refresh anything if non-existent form passed as
     * parameter
     */
    @Test
    public void testRefreshLayoutWrongForm() {
        TDropboxPutProperties properties = new TDropboxPutProperties("root");
        properties.init();

        boolean connectionExpected = properties.getForm(Form.MAIN).getWidget("connection").isHidden();
        boolean pathExpected = properties.getForm(Form.MAIN).getWidget("path").isHidden();
        boolean uploadModeExpected = properties.getForm(Form.MAIN).getWidget("uploadMode").isHidden();
        boolean revisionExpected = properties.getForm(Form.MAIN).getWidget("revision").isHidden();
        boolean uploadFromExpected = properties.getForm(Form.MAIN).getWidget("uploadFrom").isHidden();
        boolean localFileExpected = properties.getForm(Form.MAIN).getWidget("localFile").isHidden();
        boolean schemaExpected = properties.getForm(Form.MAIN).getWidget("schema").isHidden();

        properties.refreshLayout(new Form(properties, "NotMain"));

        boolean connectionActual = properties.getForm(Form.MAIN).getWidget("connection").isHidden();
        boolean pathActual = properties.getForm(Form.MAIN).getWidget("path").isHidden();
        boolean uploadModeActual = properties.getForm(Form.MAIN).getWidget("uploadMode").isHidden();
        boolean revisionActual = properties.getForm(Form.MAIN).getWidget("revision").isHidden();
        boolean uploadFromActual = properties.getForm(Form.MAIN).getWidget("uploadFrom").isHidden();
        boolean localFileActual = properties.getForm(Form.MAIN).getWidget("localFile").isHidden();
        boolean schemaActual = properties.getForm(Form.MAIN).getWidget("schema").isHidden();

        assertEquals(connectionExpected, connectionActual);
        assertEquals(pathExpected, pathActual);
        assertEquals(uploadModeExpected, uploadModeActual);
        assertEquals(revisionExpected, revisionActual);
        assertEquals(uploadFromExpected, uploadFromActual);
        assertEquals(localFileExpected, localFileActual);
        assertEquals(schemaExpected, schemaActual);
    }

    /**
     * Checks {@link TDropboxPutProperties#setupLayout()} creates Main form,
     * which contains 7 widgets: Connection, Path, Upload Mode, Revision, Upload From, Local File and Schema
     */
    @Test
    public void testSetupLayout() {
        TDropboxPutProperties properties = new TDropboxPutProperties("root");
        properties.connection.setupLayout();
        properties.schema.setupLayout();
        properties.setupLayout();

        Form main = properties.getForm(Form.MAIN);
        assertThat(main, notNullValue());
        Form advanced = properties.getForm(Form.ADVANCED);
        assertThat(advanced, nullValue());

        Collection<Widget> mainWidgets = main.getWidgets();
        assertThat(mainWidgets, hasSize(7));

        Widget connectionWidget = main.getWidget("connection");
        assertThat(connectionWidget, notNullValue());
        Widget pathWidget = main.getWidget("path");
        assertThat(pathWidget, notNullValue());
        Widget uploadModeWidget = main.getWidget("uploadMode");
        assertThat(uploadModeWidget, notNullValue());
        Widget revisionWidget = main.getWidget("revision");
        assertThat(revisionWidget, notNullValue());
        Widget uploadFromWidget = main.getWidget("uploadFrom");
        assertThat(uploadFromWidget, notNullValue());
        Widget localFileWidget = main.getWidget("localFile");
        assertThat(localFileWidget, notNullValue());
        Widget schemaWidget = main.getWidget("schema");
        assertThat(schemaWidget, notNullValue());
    }

    /**
     * Checks {@link TDropboxPutProperties#getAllSchemaPropertiesConnectors(boolean)} returns {@link Set} with 1
     * connector, when false is passed
     */
    @Test
    public void testGetAllSchemaPropertiesConnectorsInput() {
        TDropboxPutProperties properties = new TDropboxPutProperties("root");

        Set<PropertyPathConnector> connectors = properties.getAllSchemaPropertiesConnectors(false);
        assertThat(connectors, hasSize(1));
    }

    /**
     * Checks {@link TDropboxPutProperties#getAllSchemaPropertiesConnectors(boolean)} returns empty {@link Set}, when
     * true is passed
     */
    @Test
    public void testGetAllSchemaPropertiesConnectorsOutput() {
        TDropboxPutProperties properties = new TDropboxPutProperties("root");

        Set<PropertyPathConnector> connectors = properties.getAllSchemaPropertiesConnectors(true);
        assertThat(connectors, is(empty()));
    }
}

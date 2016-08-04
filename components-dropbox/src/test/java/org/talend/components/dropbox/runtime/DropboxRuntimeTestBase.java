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
package org.talend.components.dropbox.runtime;

import static org.talend.components.dropbox.DropboxTestConstants.ACCESS_TOKEN;
import static org.talend.daikon.avro.SchemaConstants.TALEND_IS_LOCKED;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field.Order;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.dropbox.DropboxProperties;
import org.talend.components.dropbox.tdropboxconnection.TDropboxConnectionProperties;
import org.talend.components.dropbox.tdropboxget.TDropboxGetProperties;
import org.talend.daikon.avro.AvroRegistry;

/**
 * Base class for Runtime classes tests
 */
public class DropboxRuntimeTestBase {

    protected RuntimeContainer container = new DefaultComponentRuntimeContainerImpl();

    protected Schema schema;

    protected TDropboxConnectionProperties connectionProperties;

    protected DropboxProperties commonProperties;

    protected TDropboxGetProperties getProperties;

    protected DropboxGetSource getSource;

    protected void setupSchema() {
        // get Schema for String class
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();
        Schema bytesSchema = registry.getConverter(ByteBuffer.class).getSchema();

        Schema.Field fileNameField = new Schema.Field("fileName", stringSchema, null, null, Order.ASCENDING);
        Schema.Field contentField = new Schema.Field("content", bytesSchema, null, null, Order.ASCENDING);
        List<Schema.Field> fields = Arrays.asList(fileNameField, contentField);
        schema = Schema.createRecord("dropbox", null, null, false, fields);
        schema.addProp(TALEND_IS_LOCKED, "true");
    }

    /**
     * Creates test instance of {@link TDropboxConnectionProperties} and sets it with test values
     */
    protected void setupConnectionProperties() {
        connectionProperties = new TDropboxConnectionProperties("connection");
        connectionProperties.setupProperties();
        connectionProperties.accessToken.setValue(ACCESS_TOKEN);
        connectionProperties.useHttpProxy.setValue(false);
    }

    /**
     * Creates test instance of {@link DropboxProperties} and sets it with test values
     */
    protected void setupCommonProperties() {
        commonProperties = new DropboxProperties("root") {

            @Override
            protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
                return null;
            }
        };

        commonProperties.path.setValue("/path/to/test/file.txt");
        commonProperties.connection = connectionProperties;
    }

    /**
     * Creates test instance of {@link TDropboxGetProperties} and sets it with test values
     */
    protected void setupGetProperties() {
        getProperties = new TDropboxGetProperties("root");
        getProperties.path.setValue("/Readme.md");
        getProperties.connection = connectionProperties;
        getProperties.saveAsFile.setValue(true);
        getProperties.saveTo.setValue("d:/test/Readme.md");
        getProperties.schema.schema.setValue(schema);
    }

    /**
     * Creates test instance of {@link DropboxGetSource} and sets it with test values
     */
    protected void setupGetSource() {
        getSource = new DropboxGetSource();
        getSource.initialize(container, getProperties);
    }
}
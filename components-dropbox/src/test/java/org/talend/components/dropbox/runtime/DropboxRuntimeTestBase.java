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
import static org.talend.components.dropbox.DropboxTestConstants.DOWNLOAD_FILE;
import static org.talend.components.dropbox.DropboxTestConstants.PATH_TO_SAVE;
import static org.talend.components.dropbox.DropboxTestConstants.PATH_TO_UPLOAD;
import static org.talend.components.dropbox.DropboxTestConstants.UPLOAD_FILE;
import static org.talend.daikon.avro.SchemaConstants.TALEND_IS_LOCKED;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
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
import org.talend.components.dropbox.tdropboxput.ContentType;
import org.talend.components.dropbox.tdropboxput.TDropboxPutProperties;
import org.talend.components.dropbox.tdropboxput.UploadMode;
import org.talend.daikon.avro.AvroRegistry;

/**
 * Base class for Runtime classes tests
 */
public class DropboxRuntimeTestBase {

    protected RuntimeContainer container = new DefaultComponentRuntimeContainerImpl();

    protected Schema getFileSchema;

    protected Schema putFileStringSchema;

    protected TDropboxConnectionProperties connectionProperties;

    protected DropboxProperties commonProperties;

    protected TDropboxGetProperties getProperties;

    protected TDropboxPutProperties putProperties;

    protected DropboxGetSource getSource;

    protected void setupGetFileSchema() {
        // get Schema for String class
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();
        Schema bytesSchema = registry.getConverter(ByteBuffer.class).getSchema();

        Schema.Field fileNameField = new Schema.Field("fileName", stringSchema, null, null, Order.ASCENDING);
        Schema.Field contentField = new Schema.Field("content", bytesSchema, null, null, Order.ASCENDING);
        List<Schema.Field> fields = Arrays.asList(fileNameField, contentField);
        getFileSchema = Schema.createRecord("dropbox", null, null, false, fields);
        getFileSchema.addProp(TALEND_IS_LOCKED, "true");
    }

    protected void setupPutFileStringSchema() {
        // get Schema for String class
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();

        Schema.Field contentField = new Schema.Field("content", stringSchema, null, null, Order.ASCENDING);
        putFileStringSchema = Schema.createRecord("dropbox", null, null, false, Collections.singletonList(contentField));
        putFileStringSchema.addProp(TALEND_IS_LOCKED, "true");
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
        getProperties.path.setValue(DOWNLOAD_FILE);
        getProperties.connection = connectionProperties;
        getProperties.saveAsFile.setValue(false);
        getProperties.saveTo.setValue(PATH_TO_SAVE);
        getProperties.schema.schema.setValue(getFileSchema);
        getProperties.chunkMode.setValue(true);
        getProperties.chunkSize.setValue(8192);
    }

    /**
     * Creates test instance of {@link TDropboxPutProperties} and sets it with test values
     */
    protected void setupPutProperties() {
        putProperties = new TDropboxPutProperties("root");
        putProperties.path.setValue(UPLOAD_FILE);
        putProperties.connection = connectionProperties;
        putProperties.uploadMode.setValue(UploadMode.RENAME);
        putProperties.uploadFrom.setValue(ContentType.STRING);
        putProperties.localFile.setValue(PATH_TO_UPLOAD);
        putProperties.schema.schema.setValue(putFileStringSchema);
    }

    /**
     * Creates test instance of {@link DropboxGetSource} and sets it with test values
     */
    protected void setupGetSource() {
        getSource = new DropboxGetSource();
        getSource.initialize(container, getProperties);
    }
}

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

import static org.talend.daikon.avro.SchemaConstants.TALEND_IS_LOCKED;
import static org.talend.daikon.properties.presentation.Widget.widget;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field.Order;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.dropbox.DropboxProperties;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

/**
 * {@link ComponentProperties} of Dropbox Put component
 */
public class TDropboxPutProperties extends DropboxProperties {

    /**
     * Incoming data schema for {@link ContentType#STRING}
     */
    private static final Schema STRING_SCHEMA;

    /**
     * Incoming data schema for {@link ContentType#STRING}
     */
    private static final Schema BYTES_SCHEMA;

    /**
     * Initializes schema constants
     */
    static {
        // get Schema for String class
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
     * Defines Dropbox Put component upload mode. Possible values are: REPLACE, RENAME
     */
    public Property<UploadMode> uploadMode = PropertyFactory.newEnum("uploadMode", UploadMode.class);

    /**
     * This property is used, when Update revision upload mode is chosen.
     * It defines file revision(version) value, which should be updated
     */
    public Property<String> revision = PropertyFactory.newString("revision");

    /**
     * Defines incoming content type. Possible values are: STRING, LOCAL_FILE, BYTE_ARRAY
     */
    public Property<ContentType> uploadFrom = PropertyFactory.newEnum("uploadFrom", ContentType.class);

    /**
     * Defines path to local file, which should be downloaded
     */
    public Property<String> localFile = PropertyFactory.newString("localFile");

    /**
     * Schema property, which defines to columns: filename and content
     */
    public SchemaProperties schema = new SchemaProperties("schema");

    /**
     * Main connector (accepts Main flow connections) and provides schema property
     */
    private transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schema");

    /**
     * Constructor sets {@link Properties} name
     * 
     * @param name {@link Properties} name
     */
    public TDropboxPutProperties(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupProperties() {
        super.setupProperties();
        uploadMode.setValue(UploadMode.RENAME);
        revision.setValue("");
        uploadFrom.setValue(ContentType.STRING);
        localFile.setValue("");
        schema.schema.setValue(STRING_SCHEMA);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(uploadMode);
        mainForm.addColumn(revision);
        mainForm.addRow(uploadFrom);
        mainForm.addRow(widget(localFile).setWidgetType(Widget.FILE_WIDGET_TYPE));
        mainForm.addRow(schema.getForm(Form.REFERENCE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.MAIN)) {
            ContentType uploadFromValue = uploadFrom.getValue();
            switch (uploadFromValue) {
            case LOCAL_FILE: {
                form.getWidget(localFile.getName()).setHidden(false);
                schema.schema.setValue(SchemaProperties.EMPTY_SCHEMA);
                form.getWidget(schema.getName()).setHidden(true);
                break;
            }
            case BYTE_ARRAY: {
                form.getWidget(localFile.getName()).setHidden(true);
                schema.schema.setValue(BYTES_SCHEMA);
                form.getWidget(schema.getName()).setHidden(false);
                break;
            }
            case STRING:
            default: {
                form.getWidget(localFile.getName()).setHidden(true);
                schema.schema.setValue(STRING_SCHEMA);
                form.getWidget(schema.getName()).setHidden(false);
                break;
            }
            }

            UploadMode uploadModeValue = uploadMode.getValue();
            if (uploadModeValue == UploadMode.UPDATE_REVISION) {
                form.getWidget(revision.getName()).setHidden(false);
            } else {
                form.getWidget(revision.getName()).setHidden(true);
            }
        }
    }

    /**
     * Returns set of {@link PropertyPathConnector} associated with this {@link ComponentProperties}
     * 
     * @param getOutputConnectors specify whether output or input connectors required
     * @return output connectors if getOutputConnectors equals true, input connectors otherwise
     */
    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean getOutputConnectors) {
        if (getOutputConnectors) {
            return Collections.EMPTY_SET;
        } else {
            return Collections.singleton(MAIN_CONNECTOR);
        }
    }

    /**
     * Refreshes Main form after Upload From widget was changed
     */
    public void afterUploadFrom() {
        refreshLayout(getForm(Form.MAIN));
    }

    /**
     * Refreshes Main form after Upload Mode widget was changed
     */
    public void afterUploadMode() {
        refreshLayout(getForm(Form.MAIN));
    }
}

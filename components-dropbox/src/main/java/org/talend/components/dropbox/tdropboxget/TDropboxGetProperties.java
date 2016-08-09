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
package org.talend.components.dropbox.tdropboxget;

import static org.talend.daikon.avro.SchemaConstants.TALEND_IS_LOCKED;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

/**
 * {@link ComponentProperties} of Dropbox Get component
 */
public class TDropboxGetProperties extends DropboxProperties {

    /**
     * Unchangeable Dropbox file schema
     */
    private static final Schema FILE_SCHEMA;

    /**
     * Default value of chunk size
     */
    public static final int DEFAULT_CHUNK_SIZE = 8192;

    /**
     * Initializes schema constant
     */
    static {
        // get Schema for String class
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();
        Schema bytesSchema = registry.getConverter(ByteBuffer.class).getSchema();

        Schema.Field fileNameField = new Schema.Field("fileName", stringSchema, null, null, Order.ASCENDING);
        Schema.Field contentField = new Schema.Field("content", bytesSchema, null, null, Order.ASCENDING);
        List<Schema.Field> fields = Arrays.asList(fileNameField, contentField);
        FILE_SCHEMA = Schema.createRecord("dropbox", null, null, false, fields);
        FILE_SCHEMA.addProp(TALEND_IS_LOCKED, "true");
    }

    /**
     * Flag, which indicates whether to save file on filesystem
     */
    public Property<Boolean> saveAsFile = PropertyFactory.newBoolean("saveAsFile");

    /**
     * Path on filesystem, where to save file
     */
    public Property<String> saveTo = PropertyFactory.newString("saveTo");

    /**
     * Schema property, which defines to columns: filename and content
     */
    public SchemaProperties schema = new SchemaProperties("schema");

    /**
     * Flag, which indicates whether to use chunkMode
     */
    public Property<Boolean> chunkMode = PropertyFactory.newBoolean("chunkMode");

    /**
     * Specifies size of data chunk
     */
    public Property<Integer> chunkSize = PropertyFactory.newInteger("chunkSize");

    /**
     * Main connector (accepts Main flow connections) and provides schema property
     */
    private transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schema");

    /**
     * Constructor sets {@link Properties} name
     * 
     * @param name {@link Properties} name
     */
    public TDropboxGetProperties(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupProperties() {
        super.setupProperties();
        saveTo.setValue("");
        schema.schema.setValue(FILE_SCHEMA);
        chunkSize.setValue(DEFAULT_CHUNK_SIZE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(saveAsFile);
        mainForm.addColumn(saveTo);
        mainForm.addRow(schema.getForm(Form.REFERENCE));

        Form advancedForm = new Form(this, Form.ADVANCED);
        advancedForm.addRow(chunkMode);
        advancedForm.addColumn(chunkSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.MAIN)) {
            boolean saveAsFileValue = saveAsFile.getValue();
            if (saveAsFileValue) {
                form.getWidget(saveTo.getName()).setHidden(false);
            } else {
                form.getWidget(saveTo.getName()).setHidden(true);
            }
        }

        if (form.getName().equals(Form.ADVANCED)) {
            boolean chunkModeValue = chunkMode.getValue();
            if (chunkModeValue) {
                form.getWidget(chunkSize.getName()).setHidden(false);
            } else {
                form.getWidget(chunkSize.getName()).setHidden(true);
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
            return Collections.singleton(MAIN_CONNECTOR);
        } else {
            return Collections.EMPTY_SET;
        }
    }

    /**
     * Refreshes layout after Save As file checkbox is changed
     */
    public void afterSaveAsFile() {
        refreshLayout(getForm(Form.MAIN));
    }

    /**
     * Refreshes layout after Chunk Mode checkbox is changed
     */
    public void afterChunkMode() {
        refreshLayout(getForm(Form.ADVANCED));
    }

}

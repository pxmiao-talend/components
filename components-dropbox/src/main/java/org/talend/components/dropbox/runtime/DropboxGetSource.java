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

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.dropbox.tdropboxget.TDropboxGetProperties;

/**
 * {@link SourceOrSink} for DropboxGet component
 */
public class DropboxGetSource extends DropboxComponentSourceOrSink implements Source {

    private static final long serialVersionUID = -3229629880478387820L;

    private static final Logger LOG = LoggerFactory.getLogger(DropboxComponentSourceOrSink.class);

    /**
     * Defines whether to save content as file
     */
    private boolean saveAsFile;

    /**
     * Specifies path to save a file on filesystem
     */
    private String saveTo;

    /**
     * Data schema
     */
    private Schema schema;

    /**
     * Initializes this {@link SourceOrSink} with user specified properties
     * Accepts {@link TDropboxGetProperties}
     * 
     * @param container {@link RuntimeContainer} instance
     * @param properties user specified properties
     */
    @Override
    public void initialize(RuntimeContainer container, ComponentProperties properties) {
        super.initialize(container, properties);
        if (properties instanceof TDropboxGetProperties) {
            TDropboxGetProperties getProperties = (TDropboxGetProperties) properties;
            saveAsFile = getProperties.saveAsFile.getValue();
            saveTo = getProperties.saveTo.getValue();
            schema = getProperties.schema.schema.getValue();
        } else {
            LOG.debug("Wrong properties type");
        }
    }

    @Override
    public Reader createReader(RuntimeContainer container) {
        return null;
    }

    /**
     * Returns value, which specifies whether to save content as file
     * 
     * @return save as file values
     */
    public boolean isSaveAsFile() {
        return saveAsFile;
    }

    /**
     * Returns path where to save file on file system
     * 
     * @return path where to save file on file system
     */
    public String getSaveTo() {
        return saveTo;
    }

    /**
     * Returns data schema
     * 
     * @return data schema
     */
    public Schema getSchema() {
        return schema;
    }

}

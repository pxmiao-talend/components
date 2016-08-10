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
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.dropbox.tdropboxput.ContentType;
import org.talend.components.dropbox.tdropboxput.TDropboxPutProperties;
import org.talend.components.dropbox.tdropboxput.UploadMode;

/**
 * Dropbox Put component {@link Sink}
 */
public class DropboxPutSink extends DropboxComponentSourceOrSink implements Sink {

    private static final long serialVersionUID = 5956598129531199826L;

    private static final Logger LOG = LoggerFactory.getLogger(DropboxPutSink.class);

    /**
     * Defines Dropbox Put component upload mode. Possible values are: REPLACE, RENAME
     */
    private UploadMode uploadMode;

    /**
     * Defines incoming content type. Possible values are: STRING, LOCAL_FILE, BYTE_ARRAY
     */
    private ContentType contentType;

    /**
     * Defines path to file on local filesystem
     */
    private String filePath;

    //    /**
    //     * Data schema
    //     */
    //    private Schema schema;

    /**
     * Initializes this {@link SourceOrSink} with user specified properties
     * Accepts {@link TDropboxPutProperties}
     * 
     * @param container {@link RuntimeContainer} instance
     * @param properties user specified properties
     */
    @Override
    public void initialize(RuntimeContainer container, ComponentProperties properties) {
        super.initialize(container, properties);
        if (properties instanceof TDropboxPutProperties) {
            TDropboxPutProperties putProperties = (TDropboxPutProperties) properties;
            uploadMode = putProperties.uploadMode.getValue();
            contentType = putProperties.uploadFrom.getValue();
            filePath = putProperties.localFile.getValue();
            //            schema = putProperties.schema.schema.getValue();
        } else {
            LOG.debug("Wrong properties type");
        }
    }

    /**
     * Returns an instance of a {@link WriteOperation} that can write to this Sink.
     */
    @Override
    public WriteOperation<?> createWriteOperation() {
        return new DropboxWriteOperation(this);
    }

    /**
     * Returns upload mode
     * 
     * @return upload mode
     */
    public UploadMode getUploadMode() {
        return uploadMode;
    }

    /**
     * Returns content type of incoming data
     * 
     * @return content type
     */
    public ContentType getContentType() {
        return contentType;
    }

    /**
     * Returns path to file on local filesystem, which should be uploaded on Dropbox
     * 
     * @return path to local file
     */
    public String getFilePath() {
        return filePath;
    }

    //    public Schema getSchema() {
    //        return schema;
    //    }

}

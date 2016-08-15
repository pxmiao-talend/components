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
package org.talend.components.dropbox.runtime.writer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.codehaus.plexus.util.StringInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.dropbox.runtime.DropboxWriteOperation;
import org.talend.components.dropbox.tdropboxput.ContentType;
import org.talend.components.dropbox.tdropboxput.UploadMode;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.UploadBuilder;
import com.dropbox.core.v2.files.WriteMode;

public class DropboxPutWriter implements Writer<Result> {

    private static final Logger LOG = LoggerFactory.getLogger(DropboxPutWriter.class);

    /**
     * {@link WriteOperation}, part of which this {@link Writer} is 
     */
    private DropboxWriteOperation writeOperation;

    /**
     * Denotes whether this {@link Writer} is opened or closed
     */
    private boolean opened = false;
    
    /**
     * IndexedRecord converter
     */
    private IndexedRecordConverter<Object, ? extends IndexedRecord> converter;
    
    /**
     * Actual {@link Schema} of incoming records
     */
    private Schema recordSchema;

    /**
     * Return results
     */
    private Result result;

    /**
     * Defines from which source to write file content.
     * Possible values are: String, byte[] columns or local file
     */
    private final ContentType contentType;

    /**
     * Dropbox writing/upload mode
     */
    private final WriteMode dbxWriteMode;

    /**
     * Path to upload file on Dropbox
     */
    private final String path;

    /**
     * Dropbox files client, which is responsible for operation with files like download, upload, copy
     */
    private DbxUserFilesRequests filesClient;

    /**
     * Constructor sets {@link WriteOperation}, Content type, upload mode and 
     * path to which upload file on Dropbox
     * 
     * @param writeOperation {@link WriteOperation}, part of which this {@link Writer} is 
     */
    public DropboxPutWriter(DropboxWriteOperation writeOperation) {
        this.writeOperation = writeOperation;
        contentType = writeOperation.getSink().getContentType();
        path = writeOperation.getSink().getPath();
        dbxWriteMode = writeOperation.getSink().getUploadMode().toWriteMode();
        filesClient = writeOperation.getSink().getClient().files();
    }

    /**
     * First method to be called when working with this class
     * 
     * It changes state of this instance to opened,
     * creates new instance of {@link Result}, which collects return values
     * 
     * If {@link ContentType#LOCAL_FILE} is chosen, than uploads local file to dropbox
     * and changes state to closed
     */
    @Override
    public void open(String uId) throws IOException {
        if (opened) {
            LOG.debug("Writer is already opened");
            return;
        }
        result = new Result(uId);
        recordSchema = null;
        if (contentType == ContentType.LOCAL_FILE) {
            uploadLocalFile();
            opened = false;
        }
        opened = true;
    }

    /**
     * Uploads local file to Dropbox.
     * It uses local file path and path on Dropbox properties, which are specified by user
     * There are several write modes:
     * {@link UploadMode#RENAME} - renames file if path is already exist
     * {@link UploadMode#REPLACE} - replaces file if path is already exist
     * 
     *  {@link DbxException} could be thrown. It is wrapped in {@link IOException}
     */
    private void uploadLocalFile() throws IOException {
        String localFilePath = writeOperation.getSink().getFilePath();
        File localFile = new File(localFilePath);
        try (FileInputStream fis = new FileInputStream(localFile)) {
            UploadBuilder uploadBuilder = filesClient.uploadBuilder(path).withMode(dbxWriteMode).withAutorename(true);
            uploadBuilder.uploadAndFinish(fis);
        } catch (DbxException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void write(Object datum) throws IOException {
        if (!opened) {
            throw new IOException("Writer wasn't opened");
        }
        if (datum == null) {
            return;
        }
        if (null == converter) {
            converter = (IndexedRecordConverter<Object, ? extends IndexedRecord>) new AvroRegistry()
                    .createIndexedRecordConverter(datum.getClass());
        }
        IndexedRecord record = converter.convertToAvro(datum);
        if (contentType == ContentType.STRING) {
            writeString(record);
        }
        if (contentType == ContentType.BYTE_ARRAY) {
            writeByteArray(record);
        }
    }
    
    /**
     * Checks incoming record's schema matches schema specified by component.
     * This component allows only locked schema.
     * Retrieves file content from incoming record and uploads it on Dropbox
     * 
     * This method shouldn't be used to upload a file larger than 150 MB.
     * 
     * @param record Incoming {@link IndexedRecord}
     * @throws IOException which wraps {@link DbxException}
     */
    private void writeString(IndexedRecord record) throws IOException {
        if (recordSchema == null) {
            recordSchema = record.getSchema();
            if (!getWriteOperation().getSink().getComponentSchema().equals(recordSchema)) {
                recordSchema = null;
                throw new IOException("Incoming and component schemas don't match");
            }
        }
        String stringContent = (String) record.get(0);
        UploadBuilder uploadBuilder = filesClient.uploadBuilder(path).withMode(dbxWriteMode).withAutorename(true);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(stringContent.getBytes())) {
            uploadBuilder.uploadAndFinish(bais);
        } catch (DbxException e) {
            throw new IOException(e);
        }   
    }
    
    private void writeByteArray(IndexedRecord object) {
        // TODO
    }

    @Override
    public Result close() throws IOException {
        // TODO Auto-generated method stub
        // uploads file
        opened = false;
        return result;
    }

    /**
     * Returns {@link WriteOperation}, part of which this {@link Writer} is
     */
    @Override
    public DropboxWriteOperation getWriteOperation() {
        return writeOperation;
    }

}

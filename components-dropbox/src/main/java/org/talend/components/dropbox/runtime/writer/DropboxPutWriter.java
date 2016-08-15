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

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.dropbox.runtime.DropboxWriteOperation;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.WriteMode;

/**
 * Common {@link Writer} for different Content types of Dropbox Put component
 */
public abstract class DropboxPutWriter implements Writer<Result> {

    private static final Logger LOG = LoggerFactory.getLogger(DropboxPutWriter.class);

    /**
     * {@link WriteOperation}, part of which this {@link Writer} is 
     */
    private DropboxWriteOperation writeOperation;

    /**
     * Denotes whether this {@link Writer} is opened or closed
     */
    protected boolean opened = false;

    /**
     * IndexedRecord converter
     */
    protected IndexedRecordConverter<Object, ? extends IndexedRecord> converter;

    /**
     * Actual {@link Schema} of incoming records
     */
    protected Schema recordSchema;

    /**
     * Return results
     */
    private Result result;

    /**
     * Dropbox writing/upload mode
     */
    protected final WriteMode dbxWriteMode;

    /**
     * Path to upload file on Dropbox
     */
    protected final String path;

    /**
     * Dropbox files client, which is responsible for operation with files like download, upload, copy
     */
    protected final DbxUserFilesRequests filesClient;

    /**
     * Constructor sets {@link WriteOperation}, Content type, upload mode and 
     * path to which upload file on Dropbox
     * 
     * @param writeOperation {@link WriteOperation}, part of which this {@link Writer} is 
     */
    public DropboxPutWriter(DropboxWriteOperation writeOperation) {
        this.writeOperation = writeOperation;
        path = writeOperation.getSink().getPath();
        dbxWriteMode = writeOperation.getSink().getUploadMode().toWriteMode();
        filesClient = writeOperation.getSink().getClient().files();
    }

    /**
     * First method to be called when working with this class
     * It changes state of this instance to opened,
     * creates new instance of {@link Result}, which collects return values
     * 
     * @param uId unique Id of current {@link Writer}
     * @throws IOException its overridden methods may throw exception, but this method don't throw exception
     */
    @Override
    public void open(String uId) throws IOException {
        if (opened) {
            LOG.debug("Writer is already opened");
            return;
        }
        result = new Result(uId);
        opened = true;
    }

    /**
     * Changes state of this {@link Writer} to closed and 
     * returns return variables
     * 
     * @return return varibles wrapped in {@link Result} instance
     */
    @Override
    public Result close() throws IOException {
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

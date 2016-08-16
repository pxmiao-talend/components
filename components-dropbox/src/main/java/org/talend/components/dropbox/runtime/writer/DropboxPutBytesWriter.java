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
import java.io.OutputStream;

import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.dropbox.runtime.DropboxWriteOperation;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.UploadBuilder;
import com.dropbox.core.v2.files.UploadUploader;

/**
 * Dropbox Put component {@link Writer} implementation, which provides writing from byte[] content
 * Full write operation consists of following method calls:
 * {@link Writer#open()} should be called once to initialize this Writer
 * {@link Writer#write(Object)} could be called several times to write all Object/IndexedRecord(s)
 * {@link Writer#close()} should be called once in the end to finish writing and release resources
 */
public class DropboxPutBytesWriter extends DropboxPutWriter {

    /**
     * Dropbox API instance, which used for uploading file on Dropbox
     */
    private UploadUploader uploader;

    /**
     * {@link OutputStream} used to upload chunks of file content
     */
    private OutputStream os;

    /**
     * Constructor sets {@link WriteOperation}
     * 
     * @param writeOperation {@link WriteOperation}, part of which this {@link Writer} is
     */
    public DropboxPutBytesWriter(DropboxWriteOperation writeOperation) {
        super(writeOperation);
    }

    /**
     * {@inheritDoc}
     * 
     * It starts Dropbox uploader and retrieves {@link OutputStream} for writing chunks of file content
     * 
     * @param uId unique Id of current {@link Writer}
     * @throws IOException its overridden methods may throw exception, but this method don't throw exception
     */
    @Override
    public void open(String uId) throws IOException {
        super.open(uId);
        UploadBuilder uploadBuilder = filesClient.uploadBuilder(path).withMode(dbxWriteMode).withAutorename(true);
        try {
            uploader = uploadBuilder.start();
            os = uploader.getOutputStream();
        } catch (DbxException e) {
            throw new IOException(e);
        }
    }

    /**
     * Checks incoming record's schema matches schema specified by component.
     * This component allows only locked schema.
     * Retrieves a byte[] chunk of file content from incoming record and writes it
     * to {@link OutputStream}
     * 
     * This method shouldn't be used to upload a file larger than 150 MB.
     * 
     * @param record Incoming {@link IndexedRecord}
     * @throws IOException which wraps {@link DbxException}
     */
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
        if (recordSchema == null) {
            recordSchema = record.getSchema();
            if (!getWriteOperation().getSink().getComponentSchema().equals(recordSchema)) {
                recordSchema = null;
                throw new IOException("Incoming and component schemas don't match");
            }
        }
        byte[] contentChunk = (byte[]) record.get(0);
        os.write(contentChunk);
    }

    /**
     * Closes Dropbox uploader.
     * 
     * {@inheritDoc}
     * @throws IOException in case of errors during finalizing writing
     */
    @Override
    public Result close() throws IOException {
        try {
            uploader.finish();
        } catch (DbxException e) {
            throw new IOException(e);
        } finally {
            uploader.close();
        }
        return super.close();
    }
}

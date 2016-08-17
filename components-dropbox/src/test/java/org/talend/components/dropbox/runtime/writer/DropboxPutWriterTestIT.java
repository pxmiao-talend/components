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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.dropbox.avro.DropboxIndexedRecord;
import org.talend.components.dropbox.runtime.DropboxRuntimeTestBase;
import org.talend.components.dropbox.tdropboxput.ContentType;
import org.talend.components.dropbox.tdropboxput.UploadMode;

/**
 * Integration test for {@link DropboxPutWriter}
 */
public class DropboxPutWriterTestIT extends DropboxRuntimeTestBase {

    /**
     * Prepares required instances for tests
     */
    @Before
    public void setUp() {
        setupPutFileStringSchema();
        setupPutFileByteSchema();
        setupConnectionProperties();
        setupPutProperties();
        putProperties.uploadFrom.setValue(ContentType.LOCAL_FILE);
        setupPutSink();
        setupWriteOperation();
    }

    @Override
    protected void setupPutSink() {
        super.setupPutSink();
        putSink.validate(container);
    }

    /**
     * Checks {@link DropboxPutWriter#open()} uploads local file on Dropbox 
     */
    @Ignore
    @Test
    public void testUploadFromFile() throws IOException {
        DropboxPutWriter writer = writeOperation.createWriter(container);
        writer.open("putUid");
    }

    /**
     * Checks {@link DropboxPutWriter#write()} upload file from String content if {@link ContentType#STRING} is specified
     */
    @Ignore
    @Test
    public void testUploadFromString() throws IOException {
        // prepare test IndexedRecord
        DropboxIndexedRecord record = new DropboxIndexedRecord(putFileStringSchema);
        String content = "This is string content of file. And another string";
        record.put(0, content);
        changeContentTypeTo(ContentType.STRING, putFileStringSchema);
        changeUploadModeTo(UploadMode.REPLACE, "");

        DropboxPutWriter writer = writeOperation.createWriter(container);
        writer.open("putUid");
        writer.write(record);
        Result result = writer.close();
    }

    @Ignore
    @Test
    public void testUploadFromByteArray() throws IOException {
        changeContentTypeTo(ContentType.BYTE_ARRAY, putFileByteSchema);
        List<IndexedRecord> records = prepareByteArrayRecords();

        DropboxPutWriter writer = writeOperation.createWriter(container);
        writer.open("putUid");
        for (IndexedRecord record : records) {
            writer.write(record);
        }
        Result result = writer.close();
    }

    /**
     * Checks Dropbox Put update revision feature
     */
    @Ignore
    @Test
    public void testUploadWithUpdateRevision() throws IOException {
        // prepare test IndexedRecord
        DropboxIndexedRecord record = new DropboxIndexedRecord(putFileStringSchema);
        String content = "Now it should create another file";
        record.put(0, content);
        changeContentTypeTo(ContentType.STRING, putFileStringSchema);
        changeUploadModeTo(UploadMode.UPDATE_REVISION, "4e4b95d247");

        DropboxPutWriter writer = writeOperation.createWriter(container);
        writer.open("putUid");
        writer.write(record);
        Result result = writer.close();
    }

    /**
     * Changes contentType for different test-case
     */
    private void changeContentTypeTo(ContentType contentType, Schema schema) {
        putProperties.uploadFrom.setValue(contentType);
        putProperties.schema.schema.setValue(schema);
        setupPutSink();
        setupWriteOperation();
    }

    /**
     * Sets specified {@link UploadMode}
     */
    private void changeUploadModeTo(UploadMode mode, String revision) {
        putProperties.uploadMode.setValue(mode);
        putProperties.revision.setValue(revision);
        setupPutSink();
        setupWriteOperation();
    }

    /**
     * Prepares a list of {@link IndexedRecord} for testUploadFromByteArray case
     */
    private List<IndexedRecord> prepareByteArrayRecords() throws IOException {
        File bigFile = new File("D:/test/angular.js");
        FileInputStream fis = new FileInputStream(bigFile);
        LinkedList<IndexedRecord> records = new LinkedList<>();
        // just to test with 10 records, no need to read whole file
        for (int i = 0; i < 10; i++) {
            byte[] buffer = new byte[1024];
            fis.read(buffer);
            DropboxIndexedRecord record = new DropboxIndexedRecord(putFileByteSchema);
            record.put(0, buffer);
            records.add(record);
        }
        return records;
    }
}

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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.dropbox.avro.DropboxIndexedRecord;
import org.talend.components.dropbox.runtime.DropboxRuntimeTestBase;
import org.talend.components.dropbox.tdropboxput.ContentType;

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
        String content = "This is string content of file";
        record.put(0, content);
        changeContentTypeTo(ContentType.STRING);
        
        DropboxPutWriter writer = writeOperation.createWriter(container);
        writer.open("putUid");
        writer.write(record);
        Result result = writer.close();
    }

    /**
     * Changes contentType for different test-case
     */
    private void changeContentTypeTo(ContentType contentType) {
        putProperties.uploadFrom.setValue(contentType);
        setupPutSink();
        setupWriteOperation();
    }
}

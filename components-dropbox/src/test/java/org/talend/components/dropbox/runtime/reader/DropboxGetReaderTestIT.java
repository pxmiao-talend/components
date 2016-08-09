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
package org.talend.components.dropbox.runtime.reader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.dropbox.runtime.DropboxRuntimeTestBase;
import org.talend.daikon.properties.ValidationResult;

/**
 * Integration-tests for {@link DropboxGetReader}
 * This test requires Internet connection and Dropbox account configured for this test
 */
public class DropboxGetReaderTestIT extends DropboxRuntimeTestBase {

    /**
     * Prepares required instances for tests
     */
    @Before
    public void setUp() {
        setupSchema();
        setupConnectionProperties();
        setupGetProperties();
        setupGetSource();
    }

    /**
     * Checks Dropbox component read test-case:
     * 1. Only 1 record should be read, because file is less than default size of chunk
     * 2. It should provide IndexedRecord with 2 fields: fileName and content
     * 3. It should read only specified file. Here, TestFile.txt
     * 4. File content should be available
     */
    @Ignore
    @Test
    public void testRead() throws IOException {
        ValidationResult vr = getSource.validate(container);
        assertEquals(ValidationResult.OK, vr);
        DropboxGetReader reader = new DropboxGetReader(getSource);
        List<IndexedRecord> records = new ArrayList<IndexedRecord>(1);
        ByteArrayOutputStream fileBuffer = new ByteArrayOutputStream();
        for (boolean hasNext = reader.start(); hasNext; hasNext = reader.advance()) {
            IndexedRecord record = reader.getCurrent();
            fileBuffer.write((byte[]) record.get(1));
            records.add(record);
        }
        reader.close();

        assertThat(records, hasSize(1));
        IndexedRecord record = records.get(0);
        Schema actualSchema = record.getSchema();
        assertEquals(schema, actualSchema);
        String fileName = (String) record.get(0);
        assertEquals("TestFile.txt", fileName);
        String content = fileBuffer.toString();
        assertEquals("Talend the Best", content);
        fileBuffer.close();
    }

}

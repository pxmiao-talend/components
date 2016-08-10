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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.dropbox.tdropboxput.ContentType;
import org.talend.components.dropbox.tdropboxput.TDropboxPutProperties;
import org.talend.components.dropbox.tdropboxput.UploadMode;

/**
 * Unit-tests for {@link DropboxPutSink}
 */
public class DropboxPutSinkTest extends DropboxRuntimeTestBase {

    /**
     * Prepares required instances for tests
     */
    @Before
    public void setUp() {
        setupPutFileStringSchema();
        setupConnectionProperties();
        setupPutProperties();
    }

    /**
     * Checks {@link DropboxPutSink#initialize()} sets property values from {@link TDropboxPutProperties} without changing them
     */
    @Test
    public void testInitialize() {
        DropboxPutSink sink = new DropboxPutSink();
        sink.initialize(container, putProperties);

        UploadMode uploadMode = sink.getUploadMode();
        assertEquals(UploadMode.RENAME, uploadMode);
        ContentType contentType = sink.getContentType();
        assertEquals(ContentType.STRING, contentType);
        String localFile = sink.getFilePath();
        assertEquals("d:/test/UploadFile.txt", localFile);
    }

    /**
     * Checks {@link DropboxPutSink#createWriteOperation()} creates WriteOperation of type {@link DropboxWriteOperation}
     */
    @Test
    public void testCreateWriteOperation() {
        DropboxPutSink sink = new DropboxPutSink();

        WriteOperation<?> writeOperation = sink.createWriteOperation();
        assertThat(writeOperation, is(instanceOf(DropboxWriteOperation.class)));
    }
}

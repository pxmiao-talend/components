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
import static org.junit.Assert.assertTrue;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.dropbox.runtime.reader.DropboxGetReader;
import org.talend.components.dropbox.tdropboxget.TDropboxGetProperties;

/**
 * Unit-tests for {@link DropboxGetSource}
 */
public class DropboxGetSourceTest extends DropboxRuntimeTestBase {

    /**
     * Prepares required instances for tests
     */
    @Before
    public void setUp() {
        setupSchema();
        setupConnectionProperties();
        setupGetProperties();
    }

    /**
     * Checks {@link DropboxGetSource#initialize()} sets property values from {@link TDropboxGetProperties} without changing them
     */
    @Test
    public void testInitialize() {
        DropboxGetSource source = new DropboxGetSource();
        source.initialize(container, getProperties);

        boolean saveAsFile = source.isSaveAsFile();
        assertTrue(saveAsFile);
        String saveTo = source.getSaveTo();
        assertEquals("d:/test/Readme.md", saveTo);
        Schema actualSchema = source.getSchema();
        assertEquals(schema, actualSchema);
    }

    /**
     * Checks {@link DropboxGetSource#createReader()} creates reader of type {@link DropboxGetReader}
     */
    @Test
    public void testCreateReader() {
        DropboxGetSource source = new DropboxGetSource();
        Reader<IndexedRecord> reader = source.createReader(container);
        assertThat(reader, is(instanceOf(DropboxGetReader.class)));
    }

}

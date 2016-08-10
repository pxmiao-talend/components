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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit-tests for {@link DropboxWriteOperation} class
 */
public class DropboxWriteOperationTest extends DropboxRuntimeTestBase {

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
     * Checks {@link DropboxWriteOperation#getSink()} returns {@link DropboxPutSink} without any changes
     */
    @Test
    public void testGetSink() {
        DropboxWriteOperation writeOperation = new DropboxWriteOperation(putSink);

        DropboxPutSink actualSink = writeOperation.getSink();

        assertEquals(putSink, actualSink);
    }

}

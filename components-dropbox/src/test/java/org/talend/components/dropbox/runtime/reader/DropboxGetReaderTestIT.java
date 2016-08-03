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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

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

    @Before
    public void setUp() {
        setupSchema();
        setupConnectionProperties();
        setupGetProperties();
        setupGetSource();
    }

    @Ignore
    @Test
    public void testStart() {
        ValidationResult vr = getSource.validate(container);
        assertEquals(ValidationResult.OK, vr);
        DropboxGetReader reader = new DropboxGetReader(getSource);
        try {
            reader.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

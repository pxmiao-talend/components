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

import org.junit.Ignore;
import org.junit.Test;
import org.talend.daikon.properties.ValidationResult;

/**
 * Integration tests for {@link DropboxSourceOrSink} class
 * These tests require Internet connection
 */
public class DropboxSourceOrSinkTestIT {

    /**
     * Checks {@link DropboxSourceOrSink#validateHost()} checks connection to Dropbox server and returns ValidationResult.OK
     */
    @Ignore
    @Test
    public void testValidateHost() {
        DropboxSourceOrSink sourceOrSink = new DropboxSourceOrSink();
        ValidationResult result = sourceOrSink.validateHost();
        assertEquals(ValidationResult.Result.OK, result.getStatus());
    }
}

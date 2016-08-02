package org.talend.components.dropbox.runtime;

import org.junit.Test;
import org.talend.daikon.properties.ValidationResult;

import static org.junit.Assert.assertEquals;

/**
 * Integration tests for {@link DropboxSourceOrSink} class
 * These tests require Internet connection
 */
public class DropboxSourceOrSinkTestIT {

    /**
     * Checks {@link DropboxSourceOrSink#validateHost()} checks connection to Dropbox server and returns ValidationResult.OK
     */
    @Test
    public void testValidateHost() {
        DropboxSourceOrSink sourceOrSink = new DropboxSourceOrSink();
        ValidationResult result = sourceOrSink.validateHost();
        assertEquals(result.getStatus(), ValidationResult.Result.OK);
    }
}

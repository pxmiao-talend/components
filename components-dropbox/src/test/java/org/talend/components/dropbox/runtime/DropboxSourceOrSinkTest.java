package org.talend.components.dropbox.runtime;

import org.junit.Test;
import org.talend.daikon.properties.ValidationResult;

import static org.junit.Assert.assertEquals;

public class DropboxSourceOrSinkTest {

    
    @Test
    public void testValidateHost () {
        DropboxSourceOrSink sourceOrSink = new DropboxSourceOrSink();
        ValidationResult result = sourceOrSink.validateHost();
        assertEquals(result.getStatus(), ValidationResult.Result.OK);
    }
}

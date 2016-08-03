package org.talend.components.dropbox.runtime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.daikon.properties.ValidationResult;

import com.dropbox.core.v2.DbxClientV2;

/**
 * Integration tests for {@link DropboxComponentSourceOrSink}
 * This test requires Internet connection
 */
public class DropboxComponentSourceOrSinkTestIT extends DropboxRuntimeTestBase {

    /**
     * Prepares required instances for tests
     */
    @Before
    public void setUp() {
        setupConnectionProperties();
        setupCommonProperties();
    }

    /**
     * Checks {@link DropboxComponentSourceOrSink#validate(RuntimeContainer)} returns {@link ValidationResult#OK}
     * and creates Dropbox client (connection)
     */
    @Test
    public void testValidate() {
        DropboxComponentSourceOrSink sourceOrSink = new DropboxComponentSourceOrSink();
        sourceOrSink.initialize(null, commonProperties);

        ValidationResult vr = sourceOrSink.validate(container);
        assertEquals(ValidationResult.OK, vr);

        DbxClientV2 client = sourceOrSink.getClient();
        assertThat(client, notNullValue());
    }

}

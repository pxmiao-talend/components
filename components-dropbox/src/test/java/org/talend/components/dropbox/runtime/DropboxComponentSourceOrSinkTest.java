package org.talend.components.dropbox.runtime;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;

/**
 * Unit-tests for {@link DropboxComponentSourceOrSink} class
 */
public class DropboxComponentSourceOrSinkTest extends DropboxRuntimeTestBase {

    /**
     * Prepares required instances for tests
     */
    @Before
    public void setUp() {
        setupConnectionProperties();
        setupCommonProperties();
    }

    /**
     * Checks {@link DropboxComponentSourceOrSink#initialize(RuntimeContainer, ComponentProperties)} sets required fields from
     * {@link ComponentProperties}
     */
    @Test
    public void testInitialize() {
        DropboxComponentSourceOrSink sourceOrSink = new DropboxComponentSourceOrSink();

        sourceOrSink.initialize(null, commonProperties);

        String path = sourceOrSink.getPath();
        assertEquals("/path/to/test/file.txt", path);
    }

}

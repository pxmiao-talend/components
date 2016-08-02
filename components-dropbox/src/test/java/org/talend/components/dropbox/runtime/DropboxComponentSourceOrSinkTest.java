package org.talend.components.dropbox.runtime;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.dropbox.DropboxProperties;
import org.talend.components.dropbox.tdropboxconnection.TDropboxConnectionProperties;

/**
 * Unit-tests for {@link DropboxComponentSourceOrSink} class
 */
public class DropboxComponentSourceOrSinkTest {

    /**
     * {@link ComponentProperties} for {@link DropboxComponentSourceOrSink}
     */
    private DropboxProperties properties;

    /**
     * Prepares required instances for tests
     */
    @Before
    public void setUp() {
        TDropboxConnectionProperties connectionProperties = new TDropboxConnectionProperties("connection");
        connectionProperties.setupProperties();
        connectionProperties.accessToken.setValue("testAccessToken");
        connectionProperties.useHttpProxy.setValue(false);
        properties = new DropboxProperties("root") {

            @Override
            protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
                return null;
            }
        };

        properties.path.setValue("/path/to/test/file.txt");
        properties.connection = connectionProperties;
    }

    /**
     * Checks {@link DropboxComponentSourceOrSink#initialize(RuntimeContainer, ComponentProperties)} sets required fields from
     * {@link ComponentProperties}
     */
    @Test
    public void testInitialize() {
        DropboxComponentSourceOrSink sourceOrSink = new DropboxComponentSourceOrSink();

        sourceOrSink.initialize(null, properties);

        String path = sourceOrSink.getPath();
        assertEquals("/path/to/test/file.txt", path);
    }

}

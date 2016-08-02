package org.talend.components.dropbox.runtime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.dropbox.DropboxProperties;
import org.talend.components.dropbox.tdropboxconnection.TDropboxConnectionProperties;
import org.talend.daikon.properties.ValidationResult;

import com.dropbox.core.v2.DbxClientV2;

/**
 * Integration tests for {@link DropboxComponentSourceOrSink}
 * This test requires Internet connection
 */
public class DropboxComponentSourceOrSinkTestIT {

    /**
     * {@link ComponentProperties} for {@link DropboxComponentSourceOrSink}
     */
    private DropboxProperties properties;

    /**
     * {@link RuntimeContainer} instance used for tests
     */
    private RuntimeContainer container = new DefaultComponentRuntimeContainerImpl();

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
     * Checks {@link DropboxComponentSourceOrSink#validate(RuntimeContainer)} returns {@link ValidationResult#OK}
     * and creates Dropbox client (connection)
     */
    @Test
    public void testValidate() {
        DropboxComponentSourceOrSink sourceOrSink = new DropboxComponentSourceOrSink();
        sourceOrSink.initialize(null, properties);

        ValidationResult vr = sourceOrSink.validate(container);
        assertEquals(ValidationResult.OK, vr);

        DbxClientV2 client = sourceOrSink.getClient();
        assertThat(client, notNullValue());
    }

}

package org.talend.components.dropbox.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.dropbox.DropboxProperties;
import org.talend.daikon.properties.ValidationResult;

import com.dropbox.core.v2.DbxClientV2;

/**
 * Common {@link SourceOrSink} for Dropbox components: DropboxGet, DropboxDelete, DropboxPut,
 * DropboxList
 */
public class DropboxComponentSourceOrSink extends DropboxSourceOrSink {

    private static final long serialVersionUID = 303714967255935422L;

    private static final Logger LOG = LoggerFactory.getLogger(DropboxComponentSourceOrSink.class);

    /**
     * Dropbox file path
     */
    private String path;

    /**
     * Id of Connection component to which this component reference to
     */
    private String referencedComponentId;

    /**
     * Dropbox client (connection)
     */
    private DbxClientV2 client;

    /**
     * Initializes this {@link SourceOrSink} with user specified properties
     * Accepts {@link DropboxProperties}
     * 
     * @param container {@link RuntimeContainer} instance
     * @param properties user specified properties
     */
    @Override
    public void initialize(RuntimeContainer container, ComponentProperties properties) {
        if (properties instanceof DropboxProperties) {
            DropboxProperties dropboxProperties = (DropboxProperties) properties;
            super.initialize(container, dropboxProperties.connection);
            path = dropboxProperties.path.getValue();
            referencedComponentId = dropboxProperties.connection.referencedComponent.componentInstanceId.getValue();
        } else {
            LOG.debug("Wrong properties type");
        }
    }

    /**
     * Validates Dropbox server availability, obtains connection from container if it already exist or
     * creates new connection
     * 
     * @param container {@link RuntimeContainer} instance
     * @return {@link ValidationResult#OK} if server available and Error status otherwise
     */
    @Override
    public ValidationResult validate(RuntimeContainer container) {
        ValidationResult result = validateHost();
        if (result.status == ValidationResult.Result.OK) {
            // uses existent connection component
            if (referencedComponentId != null) {
                if (container != null) {
                    client = (DbxClientV2) container.getComponentData(referencedComponentId, CONNECTION_KEY);
                }
            }
            // creates new connection
            if (client == null) {
                client = connect();
            }
        }
        return result;
    }

    /**
     * Returns path to Dropbox file
     * 
     * @return path to Dropbox file
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns Dropbox client (connection)
     * 
     * @return Dropbox client (connection)
     */
    public DbxClientV2 getClient() {
        return client;
    }

}

package org.talend.components.dropbox.runtime;

import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.avro.Schema;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.dropbox.tdropboxconnection.TDropboxConnectionProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.HttpRequestor;
import com.dropbox.core.http.StandardHttpRequestor;
import com.dropbox.core.v2.DbxClientV2;

/**
 * Dropbox Server {@link SourceOrSink}
 */
public class DropboxSourceOrSink implements SourceOrSink {

    private static final long serialVersionUID = -2001358138539319311L;

    private static final Logger LOG = LoggerFactory.getLogger(DropboxSourceOrSink.class);

    /**
     * Client identifier required by dropbox sdk API
     */
    private static final String CLIENT_IDENTIFIER = "components-dropbox";

    /**
     * User's locale
     */
    private static final String LOCALE = Locale.getDefault().toString();

    /**
     * Key for storing in {@link RuntimeContainer}
     */
    private static final String CONNECTION_KEY = "Connection";

    /**
     * Host of dropbox server
     */
    private static final String DROPBOX_HOST = "https://www.dropbox.com/";

    /**
     * Connection properties
     */
    private String accessToken;

    private boolean useProxy;

    private String proxyHost;

    private int proxyPort;

    /**
     * Provides i18ned messages
     */
    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider().getI18nMessages(DropboxSourceOrSink.class);

    /**
     * Initializes this {@link SourceOrSink} with user specified properties
     * Accepts {@link TDropboxConnectionProperties}
     * 
     * @param container {@link RuntimeContainer} instance
     * @param properties user specified properties
     */
    @Override
    public void initialize(RuntimeContainer container, ComponentProperties properties) {
        if (properties instanceof TDropboxConnectionProperties) {
            TDropboxConnectionProperties connectionProperties = (TDropboxConnectionProperties) properties;
            accessToken = connectionProperties.accessToken.getValue();
            useProxy = connectionProperties.useHttpProxy.getValue();
            proxyHost = connectionProperties.proxyHost.getValue();
            proxyPort = connectionProperties.proxyPort.getValue();
        } else {
            LOG.debug("Wrong properties type");
        }
    }

    /**
     * Validates Dropbox server availability, creates connection and put it into {@link RuntimeContainer}
     * 
     * @param container {@link RuntimeContainer} instance
     * @return {@link ValidationResult#OK} if server available and Error status otherwise
     */
    @Override
    public ValidationResult validate(RuntimeContainer container) {
        ValidationResult result = validateHost();
        if (result.status == ValidationResult.Result.OK) {
            DbxClientV2 dbxClient = connect();
            if (container != null) {
                String componentId = container.getCurrentComponentId();
                container.setComponentData(componentId, CONNECTION_KEY, dbxClient);
            }
        }
        return result;
    }

    /**
     * Checks connection to Dropbox server and returns {@link ValidationResult}
     * 
     * @return {@link ValidationResult#OK} if server available and Error status otherwise
     */
    protected ValidationResult validateHost() {
        HttpClient httpClient = new DefaultHttpClient();
        HttpHead httpHead = new HttpHead(DROPBOX_HOST);
        String errorMessage;
        try {
            HttpResponse httpResponse = httpClient.execute(httpHead);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == SC_OK) {
                return ValidationResult.OK;
            } else {
                errorMessage = messages.getMessage("error.wrongStatusCode", statusCode);
                LOG.debug(errorMessage);
            }
        } catch (IOException e) {
            errorMessage = messages.getMessage("error.connectionException", e);
            LOG.debug(errorMessage);
        }
        String validationFailed = messages.getMessage("error.hostNotValidated", DROPBOX_HOST);
        StringBuilder sb = new StringBuilder(validationFailed);
        sb.append(System.lineSeparator());
        sb.append(errorMessage);
        ValidationResult validationResult = new ValidationResult();
        validationResult.setStatus(Result.ERROR);
        validationResult.setMessage(sb.toString());
        return validationResult;
    }

    /**
     * Creates and returns Dropbox client using connection properties
     * 
     * @return Dropbox client
     */
    protected DbxClientV2 connect() {
        DbxRequestConfig.Builder configBuilder = DbxRequestConfig.newBuilder(CLIENT_IDENTIFIER);
        configBuilder.withUserLocale(LOCALE);
        if (useProxy) {
            InetSocketAddress socketAddress = new InetSocketAddress(proxyHost, proxyPort);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, socketAddress);
            StandardHttpRequestor.Config config = StandardHttpRequestor.Config.builder().withNoConnectTimeout().withProxy(proxy)
                    .build();
            HttpRequestor httpRequestor = new StandardHttpRequestor(config);
            configBuilder.withHttpRequestor(httpRequestor);
        }
        DbxRequestConfig dbxConfig = configBuilder.build();
        DbxClientV2 client = new DbxClientV2(dbxConfig, accessToken);
        return client;
    }

    /**
     * Returns empty list, because Dropbox server doesn't any schema
     * 
     * @param container {@link RuntimeContainer} instance
     * @return empty list
     */
    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) {
        return Collections.EMPTY_LIST;
    }

    /**
     * Returns null, because there is no schema associated with this {@link SourceOrSink}
     * 
     * @param container {@link RuntimeContainer} instance
     * @param schemaName name of schema
     * @return null
     */
    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) {
        return null;
    }

}

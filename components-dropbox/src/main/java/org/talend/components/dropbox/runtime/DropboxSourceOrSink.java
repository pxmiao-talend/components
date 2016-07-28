package org.talend.components.dropbox.runtime;

import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
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

    private static final long serialVersionUID = 2527954241309305306L;

    private static final Logger LOG = LoggerFactory.getLogger(DropboxSourceOrSink.class);
    
    private static final String CLIENT_IDENTIFIER = "components-dropbox";
    
    private static final String LOCALE = Locale.getDefault().toString();

    protected static final String DROPBOX_HOST = "https://www.dropbox.com/";
    
    private String accessToken;
    
    private boolean useProxy;
    
    private String proxyHost;
    
    private int proxyPort;

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider().getI18nMessages(DropboxSourceOrSink.class);

    @Override
    public void initialize(RuntimeContainer container, ComponentProperties properties) {

    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        return null;
    }

    public ValidationResult validateHost() {
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

    public DbxClientV2 connect() {
        DbxRequestConfig.Builder configBuilder = DbxRequestConfig.newBuilder(CLIENT_IDENTIFIER);
        configBuilder.withUserLocale(LOCALE);
        if (useProxy) {
            InetSocketAddress socketAddress = new InetSocketAddress(proxyHost, proxyPort);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, socketAddress);
            StandardHttpRequestor.Config config = StandardHttpRequestor.Config.builder()
                    .withNoConnectTimeout()
                    .withProxy(proxy)
                    .build();
            HttpRequestor httpRequestor = new StandardHttpRequestor(config);
            configBuilder.withHttpRequestor(httpRequestor);
        }
        DbxRequestConfig dbxConfig = configBuilder.build();
        DbxClientV2 client = new DbxClientV2(dbxConfig, accessToken);
        return client;
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        return null;
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        return null;
    }

}

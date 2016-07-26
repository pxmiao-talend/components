package org.talend.components.dropbox.tdropboxconnection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

/**
 * Unit-tests for {@link TDropboxConnectionProperties} class
 */
public class TDropboxConnectionPropertiesTest {

    /**
     * Checks {@link TDropboxConnectionProperties#afterUseHttpProxy()} shows Proxy Host and Proxy Port widgets if Use HTTP Proxy checkbox is checked
     */
    @Test
    public void afterUseHttpProxyTrue() {
        TDropboxConnectionProperties properties = new TDropboxConnectionProperties("root");
        properties.init();
        properties.useHttpProxy.setValue(true);

        properties.afterUseHttpProxy();

        boolean proxyHostHidden = properties.getForm(Form.MAIN).getWidget("proxyHost").isHidden();
        boolean proxyPortHidden = properties.getForm(Form.MAIN).getWidget("proxyPort").isHidden();
        assertFalse(proxyHostHidden);
        assertFalse(proxyPortHidden);
    }

    /**
     * Checks {@link TDropboxConnectionProperties#afterUseHttpProxy()} hides Proxy Host and Proxy Port widgets if Use HTTP Proxy checkbox is unchecked
     */
    @Test
    public void afterUseHttpProxyFalse() {
        TDropboxConnectionProperties properties = new TDropboxConnectionProperties("root");
        properties.init();
        properties.useHttpProxy.setValue(false);

        properties.afterUseHttpProxy();

        boolean proxyHostHidden = properties.getForm(Form.MAIN).getWidget("proxyHost").isHidden();
        boolean proxyPortHidden = properties.getForm(Form.MAIN).getWidget("proxyPort").isHidden();
        assertTrue(proxyHostHidden);
        assertTrue(proxyPortHidden);
    }

    /**
     * Checks {@link TDropboxConnectionProperties#setupProperties()} sets correct initial property values
     */
    @Test
    public void testSetupProperties() {
        TDropboxConnectionProperties properties = new TDropboxConnectionProperties("root");
        properties.setupProperties();

        String accessTokenValue = properties.accessToken.getValue();
        boolean useHttpProxyValue = properties.useHttpProxy.getValue();
        String proxyHostValue = properties.proxyHost.getValue();
        int proxyPortValue = properties.proxyPort.getValue();

        assertThat(accessTokenValue, equalTo(""));
        assertFalse(useHttpProxyValue);
        assertThat(proxyHostValue, equalTo("127.0.0.1"));
        assertThat(proxyPortValue, equalTo(8087));
    }

    /**
     * Checks {@link TDropboxConnectionProperties#refreshLayout(Form)} hides Proxy Host and Proxy Port widgets in initial state
     */
    @Test
    public void testRefreshLayoutMainInitial() {
        TDropboxConnectionProperties properties = new TDropboxConnectionProperties("root");
        properties.init();

        properties.refreshLayout(properties.getForm(Form.MAIN));

        boolean proxyHostHidden = properties.getForm(Form.MAIN).getWidget("proxyHost").isHidden();
        boolean proxyPortHidden = properties.getForm(Form.MAIN).getWidget("proxyPort").isHidden();
        assertTrue(proxyHostHidden);
        assertTrue(proxyPortHidden);
    }

    /**
     * Checks {@link TDropboxConnectionProperties#refreshLayout(Form)} doesn't refresh anything if non-existent form passed as
     * parameter
     */
    @Test
    public void testRefreshLayoutWrongForm() {
        TDropboxConnectionProperties properties = new TDropboxConnectionProperties("root");
        properties.init();

        boolean proxyHostExpected = properties.getForm(Form.MAIN).getWidget("proxyHost").isHidden();
        boolean proxyPortExpected = properties.getForm(Form.MAIN).getWidget("proxyPort").isHidden();

        properties.refreshLayout(new Form(properties, "NotMain"));

        boolean proxyHostActual = properties.getForm(Form.MAIN).getWidget("proxyHost").isHidden();
        boolean proxyPortActual = properties.getForm(Form.MAIN).getWidget("proxyPort").isHidden();

        assertEquals(proxyHostExpected, proxyHostActual);
        assertEquals(proxyPortExpected, proxyPortActual);
    }

    /**
     * Checks {@link TDropboxConnectionProperties#setupLayout()} creates 1 Main forms,
     * which contains 4 widgets
     */
    @Test
    public void testSetupLayout() {
        TDropboxConnectionProperties properties = new TDropboxConnectionProperties("root");
        properties.setupLayout();

        Form main = properties.getForm(Form.MAIN);
        assertThat(main, notNullValue());
        Form advanced = properties.getForm(Form.ADVANCED);
        assertThat(advanced, nullValue());

        Collection<Widget> mainWidgets = main.getWidgets();
        assertThat(mainWidgets, hasSize(4));

        Widget accessTokenWidget = main.getWidget("accessToken");
        assertThat(accessTokenWidget, notNullValue());
        Widget useHttpProxyWidget = main.getWidget("useHttpProxy");
        assertThat(useHttpProxyWidget, notNullValue());
        Widget proxyHostWidget = main.getWidget("proxyHost");
        assertThat(proxyHostWidget, notNullValue());
        Widget proxyPortWidget = main.getWidget("proxyPort");
        assertThat(proxyPortWidget, notNullValue());
    }

}

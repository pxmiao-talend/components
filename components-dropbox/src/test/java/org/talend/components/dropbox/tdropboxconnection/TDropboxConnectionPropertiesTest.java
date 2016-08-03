// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
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
     * Checks {@link TDropboxConnectionProperties#afterReferencedComponent()} hides Main form widget from Reference from, when componentId is specified
     */
    @Test
    public void testAfterReferencedComponent() {
        TDropboxConnectionProperties properties = new TDropboxConnectionProperties("root");
        properties.init();
        properties.referencedComponent.componentInstanceId.setValue("tDropboxConnection_1");

        properties.afterReferencedComponent();

        boolean mainFormHidden = properties.getForm(Form.REFERENCE).getWidget(properties.getName()).isHidden();
        assertTrue(mainFormHidden);
    }

    /**
     * Checks {@link TDropboxConnectionProperties#afterUseHttpProxy()} shows Proxy Host and Proxy Port widgets if Use HTTP Proxy checkbox is checked
     */
    @Test
    public void testAfterUseHttpProxyTrue() {
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
    public void testAfterUseHttpProxyFalse() {
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

        String referencedComponentType = properties.referencedComponent.componentType.getValue();
        String accessTokenValue = properties.accessToken.getValue();
        boolean useHttpProxyValue = properties.useHttpProxy.getValue();
        String proxyHostValue = properties.proxyHost.getValue();
        int proxyPortValue = properties.proxyPort.getValue();

        assertThat(referencedComponentType, equalTo("tDropboxConnection"));
        assertThat(accessTokenValue, equalTo(""));
        assertFalse(useHttpProxyValue);
        assertThat(proxyHostValue, equalTo("127.0.0.1"));
        assertThat(proxyPortValue, equalTo(8087));
    }

    /**
     * Checks {@link TDropboxConnectionProperties#refreshLayout(Form)} hides Proxy Host and Proxy Port widgets in initial state
     * and shows main form on reference form
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

        properties.refreshLayout(properties.getForm(Form.REFERENCE));

        boolean mainFormHidden = properties.getForm(Form.REFERENCE).getWidget(properties.getName()).isHidden();
        assertFalse(mainFormHidden);
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
        boolean mainFormWidgetExpected = properties.getForm(Form.REFERENCE).getWidget(properties.getName()).isHidden();

        properties.refreshLayout(new Form(properties, "NotMain"));

        boolean proxyHostActual = properties.getForm(Form.MAIN).getWidget("proxyHost").isHidden();
        boolean proxyPortActual = properties.getForm(Form.MAIN).getWidget("proxyPort").isHidden();
        boolean mainFormWidgetActual = properties.getForm(Form.REFERENCE).getWidget(properties.getName()).isHidden();

        assertEquals(proxyHostExpected, proxyHostActual);
        assertEquals(proxyPortExpected, proxyPortActual);
        assertEquals(mainFormWidgetExpected, mainFormWidgetActual);
    }

    /**
     * Checks {@link TDropboxConnectionProperties#setupLayout()} creates 2 forms: Main and Reference,
     * Main form should contain 4 widgets: accessToken, useHttpProxy, proxyHost, proxyPort;
     * Reference form should contain 2 widgets: componentList, mainForm
     */
    @Test
    public void testSetupLayout() {
        TDropboxConnectionProperties properties = new TDropboxConnectionProperties("root");
        properties.setupLayout();

        Form main = properties.getForm(Form.MAIN);
        assertThat(main, notNullValue());
        Form advanced = properties.getForm(Form.ADVANCED);
        assertThat(advanced, nullValue());
        Form reference = properties.getForm(Form.REFERENCE);
        assertThat(reference, notNullValue());

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

        Collection<Widget> referenceWidgets = reference.getWidgets();
        assertThat(referenceWidgets, hasSize(2));

        Widget componentListWidget = reference.getWidget("referencedComponent");
        assertThat(componentListWidget, notNullValue());
        Widget mainFormWidget = reference.getWidget(properties.getName());
        assertThat(mainFormWidget, notNullValue());
    }

}

// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.fullexample;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.AbstractComponentTest;
import org.talend.components.api.test.SimpleComponentRegistry;
import org.talend.components.api.test.SimpleComponentService;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;

public class FullExamplePropertiesTest extends AbstractComponentTest {

    FullExampleProperties cp;

    private SimpleComponentService simpleComponentService;

    @Before
    public void init() {
        cp = (FullExampleProperties) getComponentService().getComponentProperties(FullExampleDefinition.COMPONENT_NAME);

    }

    @Test
    public void testAllCallBacksAreCalled() throws Throwable {
        interactWithAllWidgets();
        Properties validateProperty = getComponentService().validateProperty(cp.validateAllCallbackCalled.getName(), cp);
        assertEquals(validateProperty.getValidationResult().getMessage(), Result.OK,
                validateProperty.getValidationResult().getStatus());
    }

    @Test
    public void testHiddingAField() throws Throwable {
        assertFalse(cp.getForm(Form.MAIN).getWidget(cp.stringProp.getName()).isHidden());
        cp.hideStringPropProp.setValue(true);
        Properties resultCP = getComponentService().afterProperty(cp.hideStringPropProp.getName(), cp);
        assertTrue(resultCP.getForm(Form.MAIN).getWidget(cp.stringProp.getName()).isHidden());
    }

    @Override
    public ComponentService getComponentService() {
        if (simpleComponentService == null) {
            SimpleComponentRegistry componentRegistry = new SimpleComponentRegistry();
            componentRegistry.addComponent(FullExampleDefinition.COMPONENT_NAME, new FullExampleDefinition());
            simpleComponentService = new SimpleComponentService(componentRegistry);
        }
        return simpleComponentService;
    }

    /**
     * this method should be implemented by all clients to create interaction with all widget
     * This default implmentaiton of this unit test is calling all service callbacks, but this must be overriden by clients.
     * 
     * @throws Throwable
     */
    protected void interactWithAllWidgets() throws Throwable {
        // StringProp
        callAllPropertiesCallback(cp.stringProp);
        callAllPropertiesCallback(cp.schema);
        callAllPropertiesCallback(cp.multipleSelectionProp);
        callAllPropertiesCallback(cp.filepathProp);
        callAllPropertiesCallback(cp.hiddenTextProp);
        callAllPropertiesCallback(cp.showNewForm);
        getComponentService().beforeFormPresent(FullExampleProperties.POPUP_FORM_NAME, cp);
    }

    private void callAllPropertiesCallback(NamedThing prop) throws Throwable {
        getComponentService().beforePropertyActivate(prop.getName(), cp);
        getComponentService().beforePropertyPresent(prop.getName(), cp);
        getComponentService().afterProperty(prop.getName(), cp);
        getComponentService().validateProperty(prop.getName(), cp);
    }

    @Ignore
    @Test
    @Override
    public void testAllRuntime() {
        // do nothing cause there is not runtime for this sample example.
    }
}

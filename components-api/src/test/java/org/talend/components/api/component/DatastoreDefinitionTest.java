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
package org.talend.components.api.component;

import static org.junit.Assert.*;

import org.junit.Test;
import org.talend.components.api.service.testdatastore.TestDatastoreDefinition;
import org.talend.components.api.service.testdatastore.TestDatastoreProperties;

public class DatastoreDefinitionTest {

    @Test
    public void test() {
        TestDatastoreDefinition cd = new TestDatastoreDefinition();
        TestDatastoreProperties prop = (TestDatastoreProperties) cd.createRuntimeProperties();
        assertNotNull(prop.initLater);
        assertNull(prop.mainForm);
    }

    @Test
    public void testi18NForDatastoreDefintion() {
        TestDatastoreDefinition tcd = new TestDatastoreDefinition();
        assertEquals("Test Datastore", tcd.getDisplayName());
        assertEquals("Ze Test Datastore Title", tcd.getTitle());
    }

}

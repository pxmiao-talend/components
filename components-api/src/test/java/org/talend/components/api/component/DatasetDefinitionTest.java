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
import org.talend.components.api.service.testdataset.TestDatasetDefinition;
import org.talend.components.api.service.testdataset.TestDatasetProperties;

public class DatasetDefinitionTest {

    @Test
    public void test() {
        TestDatasetDefinition cd = new TestDatasetDefinition();
        TestDatasetProperties prop = (TestDatasetProperties) cd.createRuntimeProperties();
        assertNotNull(prop.initLater);
        assertNull(prop.mainForm);
    }

    @Test
    public void testi18NForDatasetDefintion() {
        TestDatasetDefinition tcd = new TestDatasetDefinition();
        assertEquals("Test Dataset", tcd.getDisplayName());
        assertEquals("Ze Test Dataset Title", tcd.getTitle());
    }

}

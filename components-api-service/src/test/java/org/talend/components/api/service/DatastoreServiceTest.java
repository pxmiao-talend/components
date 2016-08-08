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
package org.talend.components.api.service;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.component.DatastoreDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.api.service.ComponentServiceTest.NotExistingComponentProperties;
import org.talend.components.api.service.internal.DatastoreServiceImpl;
import org.talend.components.api.service.testcomponent.ComponentPropertiesWithDefinedI18N;
import org.talend.components.api.service.testcomponent.nestedprop.NestedComponentProperties;
import org.talend.components.api.service.testdatastore.TestDatastoreDefinition;
import org.talend.components.api.test.AbstractDatastoreTest;
import org.talend.components.api.test.DatastoreTestUtils;
import org.talend.components.api.test.SimpleDatastoreRegistry;

public class DatastoreServiceTest extends AbstractDatastoreTest {

    static class NotExistingDatastoreProperties extends ComponentPropertiesImpl {

        public NotExistingDatastoreProperties() {
            super("foo");
        }
    }

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private DatastoreServiceImpl datastoreService;

    @Before
    public void initializeDatastoreRegistryAnsService() {
        // reset the datastore service
        datastoreService = null;
    }

    // default implementation for pure java test. Shall be overriden of Spring or OSGI tests
    @Override
    public DatastoreService getDatastoreService() {
        if (datastoreService == null) {
            SimpleDatastoreRegistry testDatastoreRegistry = new SimpleDatastoreRegistry();
            testDatastoreRegistry.addDatastore(TestDatastoreDefinition.DATASTORE_NAME, new TestDatastoreDefinition());
            datastoreService = new DatastoreServiceImpl(testDatastoreRegistry);
        }
        return datastoreService;
    }

    @Test
    public void testGetDatastores() {
        String[] datasets = getDatastoreService().getDatasets(TestDatastoreDefinition.DATASTORE_NAME);
        assertEquals(2, datasets.length);
        assertThat(Arrays.asList(datasets), containsInAnyOrder("testDataset1", //
                "testDataset2"));
    }

    @Test
    public void testSupportsProps() throws Throwable {
        ComponentProperties props = getDatastoreService().getComponentProperties(TestDatastoreDefinition.DATASTORE_NAME);
        ComponentPropertiesWithDefinedI18N anotherProp = (ComponentPropertiesWithDefinedI18N) new ComponentPropertiesWithDefinedI18N(
                "foo").init();
        List<DatastoreDefinition> datastores = getDatastoreService().getPossibleDatastores(props, anotherProp);
        assertEquals("TestDatastore", datastores.get(0).getName());

        datastores = getDatastoreService().getPossibleDatastores(new NestedComponentProperties("props"),
                new NotExistingComponentProperties());
        assertEquals(0, datastores.size());
    }

    @Test
    public void testFamilies() {
        TestDatastoreDefinition testDatastoreDefinition = new TestDatastoreDefinition();
        assertEquals(2, testDatastoreDefinition.getFamilies().length);
    }

    @Override
    @Test
    public void testAlli18n() {
        DatastoreTestUtils.testAlli18n(getDatastoreService(), errorCollector);
    }

    @Override
    @Test
    public void testAllImages() {
        DatastoreTestUtils.testAllImages(getDatastoreService());
    }

    @Test
    public void testGetAllDepenendencies() {
        DatastoreTestUtils.testAllDesignDependenciesPresent(getDatastoreService(), errorCollector);
    }

}

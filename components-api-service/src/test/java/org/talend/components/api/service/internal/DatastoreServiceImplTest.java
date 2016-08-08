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
package org.talend.components.api.service.internal;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.talend.components.api.component.DatastoreDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.testcomponent.TestComponentProperties;
import org.talend.components.api.test.MockDatastoreRegistry;

public class DatastoreServiceImplTest {

    private DatastoreServiceImpl datastoreServiceImpl = new DatastoreServiceImpl(new MockDatastoreRegistry());

    @Test
    public void testParseMvnUri() {
        String parsedMvnUri = datastoreServiceImpl
                .parseMvnUri("     org.talend.components:components-api:test-jar:tests:0.4.0.BUILD-SNAPSHOT:test");
        assertEquals("mvn:org.talend.components/components-api/0.4.0.BUILD-SNAPSHOT/test-jar/tests", parsedMvnUri);
        parsedMvnUri = datastoreServiceImpl
                .parseMvnUri("    org.talend.components:components-api:jar:0.4.0.BUILD-SNAPSHOT:compile   ");
        assertEquals("mvn:org.talend.components/components-api/0.4.0.BUILD-SNAPSHOT/jar", parsedMvnUri);
    }

    @Test
    public void testGetJSONSchema() {
        String parsedMvnUri = datastoreServiceImpl.getJSONSchema(MockDatastoreRegistry.DATASTORE_NAME);
        assertEquals("{\"testJSON\":\"true\"}", parsedMvnUri);
    }

    @Test
    public void testGetAllDatastoreNames() {
        Set<String> datastoreNames = datastoreServiceImpl.getAllDatastoreNames();
        assertEquals(1, datastoreNames.size());
        assertTrue(datastoreNames.contains("testDatastore"));
    }

    @Test
    public void testGetAllDatastores() {
        Set<DatastoreDefinition> datastores = datastoreServiceImpl.getAllDatastores();
        assertEquals(1, datastores.size());
        for (DatastoreDefinition datastore : datastores) {
            assertEquals("TestDatastore", datastore.getName());
        }
    }

    @Test
    public void testGetComponentProperties() {
        ComponentProperties p = datastoreServiceImpl.getComponentProperties(MockDatastoreRegistry.DATASTORE_NAME);
        assertEquals("root", p.getName());
    }

    @Test
    public void testGetDatastoreDefinition() {
        DatastoreDefinition datastore = datastoreServiceImpl.getDatastoreDefinition(MockDatastoreRegistry.DATASTORE_NAME);
        assertEquals("TestDatastore", datastore.getName());
    }

    @Test
    public void testGetPossibleDatastores() {
        ComponentProperties p = datastoreServiceImpl.getComponentProperties(MockDatastoreRegistry.DATASTORE_NAME);
        List<DatastoreDefinition> datastores = datastoreServiceImpl.getPossibleDatastores(p);
        assertEquals(1, datastores.size());
        for (DatastoreDefinition datastore : datastores) {
            assertEquals("TestDatastore", datastore.getName());
        }
    }

    @Test
    public void testGetPossibleDatastores_invalidCase() {
        TestComponentProperties t = new TestComponentProperties("invalid");
        List<DatastoreDefinition> datastores = datastoreServiceImpl.getPossibleDatastores(t);
        assertEquals(0, datastores.size());
    }

    @Test
    public void testComputeDesignDependenciesPath() {
        assertEquals("META-INF/maven/mavenGroupId/mavenArtifactId/dependencies.txt",
                datastoreServiceImpl.computeDesignDependenciesPath("mavenGroupId", "mavenArtifactId"));

    }
}

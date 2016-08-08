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
import org.talend.components.api.component.DatasetDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.testcomponent.TestComponentProperties;
import org.talend.components.api.test.MockDatasetRegistry;

public class DatasetServiceImplTest {

    private DatasetServiceImpl datasetServiceImpl = new DatasetServiceImpl(new MockDatasetRegistry());

    @Test
    public void testParseMvnUri() {
        String parsedMvnUri = datasetServiceImpl
                .parseMvnUri("     org.talend.components:components-api:test-jar:tests:0.4.0.BUILD-SNAPSHOT:test");
        assertEquals("mvn:org.talend.components/components-api/0.4.0.BUILD-SNAPSHOT/test-jar/tests", parsedMvnUri);
        parsedMvnUri = datasetServiceImpl
                .parseMvnUri("    org.talend.components:components-api:jar:0.4.0.BUILD-SNAPSHOT:compile   ");
        assertEquals("mvn:org.talend.components/components-api/0.4.0.BUILD-SNAPSHOT/jar", parsedMvnUri);
    }

    @Test
    public void testGetJSONSchema() {
        String parsedMvnUri = datasetServiceImpl.getJSONSchema(MockDatasetRegistry.DATASET_NAME);
        assertEquals("{\"testJSON\":\"true\"}", parsedMvnUri);
    }

    @Test
    public void testGetAllDatasetNames() {
        Set<String> datasetNames = datasetServiceImpl.getAllDatasetNames();
        assertEquals(1, datasetNames.size());
        assertTrue(datasetNames.contains("testDataset"));
    }

    @Test
    public void testGetAllDatasets() {
        Set<DatasetDefinition> datasets = datasetServiceImpl.getAllDatasets();
        assertEquals(1, datasets.size());
        for (DatasetDefinition dataset : datasets) {
            assertEquals("TestDataset", dataset.getName());
        }
    }

    @Test
    public void testGetComponentProperties() {
        ComponentProperties p = datasetServiceImpl.getComponentProperties(MockDatasetRegistry.DATASET_NAME);
        assertEquals("root", p.getName());
    }

    @Test
    public void testGetDatasetDefinition() {
        DatasetDefinition dataset = datasetServiceImpl.getDatasetDefinition(MockDatasetRegistry.DATASET_NAME);
        assertEquals("TestDataset", dataset.getName());
    }

    @Test
    public void testGetPossibleDatasets() {
        ComponentProperties p = datasetServiceImpl.getComponentProperties(MockDatasetRegistry.DATASET_NAME);
        List<DatasetDefinition> datasets = datasetServiceImpl.getPossibleDatasets(p);
        assertEquals(1, datasets.size());
        for (DatasetDefinition dataset : datasets) {
            assertEquals("TestDataset", dataset.getName());
        }
    }

    @Test
    public void testGetPossibleDatasets_invalidCase() {
        TestComponentProperties t = new TestComponentProperties("invalid");
        List<DatasetDefinition> datasets = datasetServiceImpl.getPossibleDatasets(t);
        assertEquals(0, datasets.size());
    }

    @Test
    public void testComputeDesignDependenciesPath() {
        assertEquals("META-INF/maven/mavenGroupId/mavenArtifactId/dependencies.txt",
                datasetServiceImpl.computeDesignDependenciesPath("mavenGroupId", "mavenArtifactId"));

    }

}

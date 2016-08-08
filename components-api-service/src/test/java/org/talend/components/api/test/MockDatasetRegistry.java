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
package org.talend.components.api.test;

import java.util.HashMap;
import java.util.Map;

import org.talend.components.api.Constants;
import org.talend.components.api.component.DatasetDefinition;
import org.talend.components.api.service.internal.DatasetRegistry;
import org.talend.components.api.service.testdataset.TestDatasetDefinition;

/**
 * created by sgandon on 10 déc. 2015
 */
public class MockDatasetRegistry implements DatasetRegistry {

    private Map<String, DatasetDefinition> dataset = new HashMap<>();

    public static String DATASET_NAME = "testDataset";

    public MockDatasetRegistry() {
        dataset.put(Constants.DATASET_BEAN_PREFIX + DATASET_NAME, new TestDatasetDefinition());
    }

    public void addDataset(String name, DatasetDefinition component) {
        dataset.put(Constants.DATASET_BEAN_PREFIX + name, component);
    }

    public void removeDataset(String name) {
        dataset.remove(Constants.DATASET_BEAN_PREFIX + name);
    }

    @Override
    public Map<String, DatasetDefinition> getDatasets() {
        return dataset;
    }

}
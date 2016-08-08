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
import org.talend.components.api.component.DatastoreDefinition;
import org.talend.components.api.service.internal.DatastoreRegistry;
import org.talend.components.api.service.testdatastore.TestDatastoreDefinition;

/**
 * created by sgandon on 10 déc. 2015
 */
public class MockDatastoreRegistry implements DatastoreRegistry {

    private Map<String, DatastoreDefinition> datastore = new HashMap<>();

    public static String DATASTORE_NAME = "testDatastore";

    public MockDatastoreRegistry() {
        datastore.put(Constants.DATASTORE_BEAN_PREFIX + DATASTORE_NAME, new TestDatastoreDefinition());
    }

    public void addDatastore(String name, DatastoreDefinition component) {
        datastore.put(Constants.DATASTORE_BEAN_PREFIX + name, component);
    }

    public void removeDatastore(String name) {
        datastore.remove(Constants.DATASTORE_BEAN_PREFIX + name);
    }

    @Override
    public Map<String, DatastoreDefinition> getDatastores() {
        return datastore;
    }

}
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

import java.util.Map;

import org.talend.components.api.component.DatasetDefinition;

/**
 * Interface for the internal dataset registry that will have a specific implementation for OSGi and Spring
 */
public interface DatasetRegistry {

    /**
     * @return a map of datasets using their name as a key, never null.
     */
    Map<String, DatasetDefinition> getDatasets();

}
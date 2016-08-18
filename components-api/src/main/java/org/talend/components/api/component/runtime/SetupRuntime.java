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
package org.talend.components.api.component.runtime;

/**
 * Basic interface for defining properties and common methods required for Runtimes of components without connectors, or
 * components which need some pre-processing before performing the actual job.
 */
public interface SetupRuntime extends Runtime {

    /**
     * Perform processes required before main Component's processes or in case a Component doesn't have any connections.
     */
    void setup();

}

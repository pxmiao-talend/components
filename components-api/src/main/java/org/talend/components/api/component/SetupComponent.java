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
package org.talend.components.api.component;

import org.talend.components.api.component.runtime.SetupRuntime;

/**
 * Definition of a component which has Setup Runtime. It could be a component which doesn't have any input or output
 * connectors, or a component which requires some pre-processing.
 */
public interface SetupComponent {

    /**
     * Return the {@link SetupRuntime}.
     */
    SetupRuntime getSetupRuntime();

}

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

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.ValidationResult;

/**
 * Base interface for defining properties and methods common to Runtime interfaces, like {@link SourceOrSink} or
 * {@link SetupRuntime}.
 */
public interface Runtime {

    /**
     * Initialize based on the specified properties. This will typically store the {@link ComponentProperties} in the
     * object.
     */
    void initialize(RuntimeContainer container, ComponentProperties properties);

    /**
     * Checks that this source or sink is valid, before it can be used. This will typically make a connection and return
     * the results of the connection establishment.
     */
    ValidationResult validate(RuntimeContainer container);

}

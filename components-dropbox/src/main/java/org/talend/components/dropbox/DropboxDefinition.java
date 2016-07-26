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
package org.talend.components.dropbox;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.daikon.properties.property.Property;

/**
 * Common definition class for Dropbox components. It acts as an entry point
 * for all of services that a component provides to integrate with the Studio (at design-time)
 * and other components (at run-time).
 */
public abstract class DropboxDefinition extends AbstractComponentDefinition {

    /**
     * Constructor sets component name
     * 
     * @param componentName component name
     */
    public DropboxDefinition(String componentName) {
        super(componentName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getFamilies() {
        return new String[] { "Cloud/Dropbox" }; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Property[] getReturnProperties() {
        return new Property[] { RETURN_ERROR_MESSAGE_PROP };
    }

    /**
     * {@inheritDoc}
     */
    public String getMavenGroupId() {
        return "org.talend.components";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMavenArtifactId() {
        return "components-dropbox";
    }

}

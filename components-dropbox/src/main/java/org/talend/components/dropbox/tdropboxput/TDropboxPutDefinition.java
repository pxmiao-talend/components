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
package org.talend.components.dropbox.tdropboxput;

import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.InputComponentDefinition;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.dropbox.DropboxDefinition;

import aQute.bnd.annotation.component.Component;

/**
 * Dropbox put component definition
 */
@Component(name = Constants.COMPONENT_BEAN_PREFIX + TDropboxPutDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TDropboxPutDefinition extends DropboxDefinition implements InputComponentDefinition {

    /**
     * Dropbox put component name
     */
    public static final String COMPONENT_NAME = "tDropboxPut";

    /**
     * Constructor sets component name
     */
    public TDropboxPutDefinition() {
        super(COMPONENT_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Source getRuntime() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return null;
    }

}

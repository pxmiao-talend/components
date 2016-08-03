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
package org.talend.components.dropbox.tdropboxget;

import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.InputComponentDefinition;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.dropbox.DropboxDefinition;
import org.talend.components.dropbox.runtime.DropboxGetSource;

import aQute.bnd.annotation.component.Component;

/**
 * Dropbox get component definition
 */
@Component(name = Constants.COMPONENT_BEAN_PREFIX + TDropboxGetDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TDropboxGetDefinition extends DropboxDefinition implements InputComponentDefinition {

    /**
     * Dropbox get component name
     */
    public static final String COMPONENT_NAME = "tDropboxGet";

    /**
     * Constructor sets component name
     */
    public TDropboxGetDefinition() {
        super(COMPONENT_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Source getRuntime() {
        return new DropboxGetSource();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TDropboxGetProperties.class;
    }

}

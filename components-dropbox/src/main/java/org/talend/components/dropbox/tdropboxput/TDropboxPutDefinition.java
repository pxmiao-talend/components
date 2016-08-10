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
import org.talend.components.api.component.OutputComponentDefinition;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.dropbox.DropboxDefinition;
import org.talend.components.dropbox.runtime.DropboxPutSink;

import aQute.bnd.annotation.component.Component;

/**
 * Dropbox put component definition
 */
@Component(name = Constants.COMPONENT_BEAN_PREFIX + TDropboxPutDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TDropboxPutDefinition extends DropboxDefinition implements OutputComponentDefinition {

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
    public Sink getRuntime() {
        return new DropboxPutSink();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TDropboxPutProperties.class;
    }

}

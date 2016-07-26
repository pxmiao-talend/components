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
package org.talend.components.dropbox.tdropboxconnection;

import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.EndpointComponentDefinition;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.dropbox.DropboxDefinition;

import aQute.bnd.annotation.component.Component;

/**
 * Dropbox connection component definition
 */
@Component(name = Constants.COMPONENT_BEAN_PREFIX
        + TDropboxConnectionDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TDropboxConnectionDefinition extends DropboxDefinition implements EndpointComponentDefinition {

    /**
     * Dropbox connection component name
     */
    public static final String COMPONENT_NAME = "tDropboxConnection";

    /**
     * Constructor sets component name
     */
    public TDropboxConnectionDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public SourceOrSink getRuntime() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return true in case it is startable component, false otherwise
     */
    @Override
    public boolean isStartable() {
        return true;
    }

}

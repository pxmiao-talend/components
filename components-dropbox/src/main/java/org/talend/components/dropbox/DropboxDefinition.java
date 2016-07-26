
package org.talend.components.dropbox;

import org.talend.components.api.Constants;
import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.component.InputComponentDefinition;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.properties.ComponentProperties;

import org.talend.daikon.properties.property.Property;

import aQute.bnd.annotation.component.Component;

/**
 * The DropboxDefinition acts as an entry point for all of services that a
 * component provides to integrate with the Studio (at design-time) and other
 * components (at run-time).
 */
@Component(name = Constants.COMPONENT_BEAN_PREFIX + DropboxDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class DropboxDefinition extends AbstractComponentDefinition implements InputComponentDefinition {

    public static final String COMPONENT_NAME = "Dropbox"; //$NON-NLS-1$

    public DropboxDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "File/Input" }; //$NON-NLS-1$
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[] {};
    }

    @Override
    public String getPngImagePath(ComponentImageType imageType) {
        switch (imageType) {
        case PALLETE_ICON_32X32:
            return "fileReader_icon32.png"; //$NON-NLS-1$
        default:
            return "fileReader_icon32.png"; //$NON-NLS-1$
        }
    }

    public String getMavenGroupId() {
        return "org.talend.components";
    }

    @Override
    public String getMavenArtifactId() {
        return "components-dropbox";
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return DropboxProperties.class;
    }

    @Override
    public Source getRuntime() {
        return new DropboxSource();
    }
}

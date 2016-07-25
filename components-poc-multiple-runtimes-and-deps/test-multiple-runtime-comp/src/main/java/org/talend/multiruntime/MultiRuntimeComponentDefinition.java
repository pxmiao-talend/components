
package org.talend.multiruntime;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Arrays;
import java.util.HashSet;

import org.ops4j.pax.url.mvn.Handler;
import org.ops4j.pax.url.mvn.ServiceConstants;
import org.talend.components.api.Constants;
import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.component.InputComponentDefinition;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.Property;
import org.talend.multiruntime.MultiRuntimeComponentProperties.Version;
import org.talend.sandbox.Sandbox;

import aQute.bnd.annotation.component.Component;

/**
 * The MultiRuntimeComponentDefinition acts as an entry point for all of services that
 * a component provides to integrate with the Studio (at design-time) and other
 * components (at run-time).
 */
@Component(name = Constants.COMPONENT_BEAN_PREFIX
        + MultiRuntimeComponentDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class MultiRuntimeComponentDefinition extends AbstractComponentDefinition implements InputComponentDefinition {

    public static final String COMPONENT_NAME = "MultiRuntimeComponent"; //$NON-NLS-1$

    public MultiRuntimeComponentDefinition() {
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

    @Override
    public String getMavenGroupId() {
        return "org.talend.components";
    }

    @Override
    public String getMavenArtifactId() {
        return "test-multiple-runtime-comp";
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return MultiRuntimeComponentProperties.class;
    }

    @Override
    public Source getRuntime() {
        return null;// new MultiRuntimeComponentSource();
    }

    static {
        URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {

            @Override
            public URLStreamHandler createURLStreamHandler(String protocol) {
                if (ServiceConstants.PROTOCOL.equals(protocol)) {
                    return new Handler();
                } else {
                    return null;
                }
            }
        });

    }

    public Source getRuntime(Properties prop) {
        if (prop != null && prop instanceof MultiRuntimeComponentProperties) {
            MultiRuntimeComponentProperties mrcpProp = (MultiRuntimeComponentProperties) prop;
            try {
                if (mrcpProp.version.getValue() == Version.VERSION_0_1) {
                    return (Source) new Sandbox().createRuntimeInstance("org.talend.multiruntime.MultiRuntimeComponentSource",
                            new HashSet<>(Arrays.asList(
                                    new URL("mvn:org.talend.components/test-multiple-runtime-comp-runtime-v01/0.1.0-SNAPSHOT"),
                                    new URL("mvn:org.talend.test/zeLib/0.0.1-SNAPSHOT"))));
                } else {
                    return (Source) new Sandbox().createRuntimeInstance("org.talend.multiruntime.MultiRuntimeComponentSource",
                            new HashSet<>(Arrays.asList(
                                    new URL("mvn:org.talend.components/test-multiple-runtime-comp-runtime-v02/0.1.0-SNAPSHOT"),
                                    new URL("mvn:org.talend.test/zeLib/0.0.2-SNAPSHOT"))));
                }
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

}

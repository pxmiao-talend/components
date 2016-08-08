package org.talend.components.cassandra.output;

import aQute.bnd.annotation.component.Component;
import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.OutputComponentDefinition;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.cassandra.CassandraDefinition;

@Component(name = Constants.COMPONENT_BEAN_PREFIX
        + TCassandraOutputDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TCassandraOutputDefinition extends CassandraDefinition implements OutputComponentDefinition {
    public static final String COMPONENT_NAME = "tCassandraOutput"; //$NON-NLS-1$

    public TCassandraOutputDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TCassandraOutputProperties.class;
    }

    @Override
    public Sink getRuntime() {
        // TODO(rskraba): redirect to the runtime to actually load.
        try {
            return (Sink) Class.forName("org.talend.components.cassandra.runtime_3_0.CassandraSink").newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}

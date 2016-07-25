package org.talend.multiruntime;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintWriter;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.internal.ComponentServiceImpl;
import org.talend.components.api.test.SimpleComponentRegistry;
import org.talend.multiruntime.MultiRuntimeComponentProperties.Version;

@SuppressWarnings("nls")
public class MultiRuntimeComponentTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private ComponentServiceImpl componentService;

    @Before
    public void initializeComponentRegistryAndService() {
        // reset the component service
        componentService = null;
    }

    // default implementation for pure java test.
    public ComponentService getComponentService() {
        if (componentService == null) {
            SimpleComponentRegistry testComponentRegistry = new SimpleComponentRegistry();
            testComponentRegistry.addComponent(MultiRuntimeComponentDefinition.COMPONENT_NAME,
                    new MultiRuntimeComponentDefinition());
            componentService = new ComponentServiceImpl(testComponentRegistry);
        }
        return componentService;
    }

    @Test
    public void testMultiRuntimeComponentRuntime() throws Exception {
        MultiRuntimeComponentDefinition def = (MultiRuntimeComponentDefinition) getComponentService()
                .getComponentDefinition("MultiRuntimeComponent");
        MultiRuntimeComponentProperties props = (MultiRuntimeComponentProperties) getComponentService()
                .getComponentProperties("MultiRuntimeComponent");

        // Set up the test schema - not really used for anything now
        Schema schema = SchemaBuilder.builder().record("testRecord").fields().name("field1").type().stringType().noDefault()
                .endRecord();
        props.schema.schema.setValue(schema);

        File temp = File.createTempFile("MultiRuntimeComponenttestFile", ".txt");
        try {
            PrintWriter writer = new PrintWriter(temp.getAbsolutePath(), "UTF-8");
            writer.println("The first line");
            writer.println("The second line");
            writer.close();

            props.version.setValue(Version.VERSION_0_1);
            Source source = def.getRuntime(props);
            source.initialize(null, props);
            assertEquals(source.validate(null).getMessage(), "Me");

            props.version.setValue(Version.VERSION_0_2);
            Source source2 = def.getRuntime(props);
            source2.initialize(null, props);
            assertEquals(source2.validate(null).getMessage(), "AnotherMe");
        } finally {// remote the temp file
            temp.delete();
        }
    }

}

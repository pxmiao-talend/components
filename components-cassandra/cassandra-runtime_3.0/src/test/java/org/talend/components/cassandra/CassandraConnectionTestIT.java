package org.talend.components.cassandra;

import org.junit.Test;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.cassandra.connection.TCassandraConnectionDefinition;
import org.talend.daikon.properties.presentation.Form;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


public class CassandraConnectionTestIT extends CassandraTestBase {

    @Test
    public void testPropsAndi18n(){
        ComponentProperties props = new TCassandraConnectionDefinition().createProperties();
        //Basic test, it will check i18n also
        ComponentTestUtils.checkSerialize(props, errorCollector);
        assertThat(props.getForm(Form.MAIN).getName(), is(Form.MAIN));
        assertThat(props.getForm(Form.REFERENCE).getName(), is(Form.REFERENCE));
        assertThat(props.getForm("Wizard").getName(), is("Wizard"));
    }

}

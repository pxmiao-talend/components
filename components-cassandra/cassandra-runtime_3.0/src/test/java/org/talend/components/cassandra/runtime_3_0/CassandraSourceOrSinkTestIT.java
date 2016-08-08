package org.talend.components.cassandra.runtime_3_0;

import org.apache.avro.Schema;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.talend.components.cassandra.CassandraConnectionProperties;
import org.talend.components.cassandra.CassandraTestBase;
import org.talend.components.cassandra.EmbeddedCassandraExampleDataResource;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class CassandraSourceOrSinkTestIT extends CassandraTestBase{
    public static final String KS_NAME = CassandraSourceOrSinkTestIT.class.getSimpleName().toLowerCase();
    @Rule
    public EmbeddedCassandraExampleDataResource mCass = new EmbeddedCassandraExampleDataResource(KS_NAME);
    private CassandraConnectionProperties connProps;
    private CassandraConnectionProperties wrongConnProps;
    private CassandraSourceOrSink cassandraSourceOrSink = new CassandraSourceOrSink();

    @Before
    public void prepare() {
        connProps = new CassandraConnectionProperties("test");
        connProps.host.setValue(EmbeddedCassandraExampleDataResource.HOST);
        connProps.port.setValue(EmbeddedCassandraExampleDataResource.PORT);
        connProps.version.setValue(CassandraConnectionProperties.CassandraVersion.V_3_0);
        connProps.needAuth.setValue(false);

        wrongConnProps = new CassandraConnectionProperties("wrong");
        wrongConnProps.host.setValue("wronghost");
        wrongConnProps.port.setValue("9042");
        wrongConnProps.version.setValue(CassandraConnectionProperties.CassandraVersion.V_3_0);
        wrongConnProps.needAuth.setValue(false);

    }

    @Test
    public void validate() throws Exception {
        cassandraSourceOrSink.initialize(null, connProps);
        assertThat(cassandraSourceOrSink.validate(null), is(ValidationResult.OK));
        cassandraSourceOrSink.initialize(null, wrongConnProps);
        assertThat(cassandraSourceOrSink.validate(null).getStatus(), is(ValidationResult.Result.ERROR));
    }

    @Test
    public void getKeyspaceNames() throws Exception {
        cassandraSourceOrSink.initialize(null, connProps);
        List<NamedThing> keyspaceNames = cassandraSourceOrSink.getKeyspaceNames(null);
        List<String> ksNames = namedThingToString(keyspaceNames);
        assertThat(ksNames, hasItem(KS_NAME));
    }

    @Test
    public void getTableNames() throws Exception {
        cassandraSourceOrSink.initialize(null, connProps);
        List<NamedThing> tableNames = cassandraSourceOrSink.getTableNames(null, KS_NAME);
        List<String> tbNames = namedThingToString(tableNames);
        assertThat(tbNames.size(), is(3));
        assertThat(tbNames, hasItems("example_src", "example_counter", "example_dst"));
    }

    @Test
    public void getSchema() throws Exception {
        cassandraSourceOrSink.initialize(null, connProps);
        Schema example_src = cassandraSourceOrSink.getSchema(null, KS_NAME, "example_src");
        System.out.println(example_src);
    }

}
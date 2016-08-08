package org.talend.components.cassandra.runtime;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.daikon.NamedThing;

import java.io.IOException;
import java.util.List;

/**
 * This shouldn't be here, but is required for Forms using component properties.
 */
public interface CassandraSourceOrSink extends SourceOrSink {
    List<NamedThing> getKeyspaceNames(RuntimeContainer container) throws IOException;

    List<NamedThing> getTableNames(RuntimeContainer container, String stringValue) throws IOException;

    Schema getSchema(RuntimeContainer container, String stringValue, String stringValue1) throws IOException;
}

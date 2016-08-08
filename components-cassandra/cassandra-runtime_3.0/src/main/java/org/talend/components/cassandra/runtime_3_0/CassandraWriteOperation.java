package org.talend.components.cassandra.runtime_3_0;

import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;

import java.util.Map;

public class CassandraWriteOperation implements WriteOperation<Result> {
    private CassandraSink cassandraSink;
    public CassandraWriteOperation(CassandraSink cassandraSink) {
        this.cassandraSink = cassandraSink;
    }

    @Override
    public void initialize(RuntimeContainer container) {

    }

    @Override
    public CassandraWriter createWriter(RuntimeContainer container) {
        return new CassandraWriter(this, container);
    }

    @Override
    public Sink getSink() {
        return cassandraSink;
    }

    @Override
    public Map<String, Object> finalize(Iterable<Result> writerResults, RuntimeContainer adaptor) {
        return Result.accumulateAndReturnMap(writerResults);
    }
}

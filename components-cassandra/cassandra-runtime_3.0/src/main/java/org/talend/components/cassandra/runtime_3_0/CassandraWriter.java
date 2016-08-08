package org.talend.components.cassandra.runtime_3_0;

import com.datastax.driver.core.*;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.WriterWithFeedback;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.cassandra.output.TCassandraOutputProperties;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CassandraWriter implements WriterWithFeedback<Result, IndexedRecord, IndexedRecord> {
    private CassandraWriteOperation cassandraWriteOperation;
    private RuntimeContainer container;

    private CassandraSink cassandraSink;
    private transient IndexedRecordConverter<Object, ? extends IndexedRecord> adapterFactory;
    private BoundStatementAdapterFactory boundStatementAdapterFactory = new BoundStatementAdapterFactory();

    private TCassandraOutputProperties properties;
    private Session session;
    private PreparedStatement preparedStatement;
    private BoundStatement boundStatement;

    //batch only properties
    private boolean useBatch = false;
    private BatchStatement batchStatement;
    private CassandraBatchUtil cassandraBatchUtil;
    private ByteBuffer currentKey;
    private ByteBuffer lastKey;
    private boolean newOne;

    private final List<IndexedRecord> successfulWrites = new ArrayList<>();

    private final List<IndexedRecord> rejectedWrites = new ArrayList<>();

    private String uId;

    private int dataCount;

    public CassandraWriter(CassandraWriteOperation cassandraWriteOperation, RuntimeContainer container) {
        this.cassandraWriteOperation = cassandraWriteOperation;
        this.container = container;
        this.cassandraSink = (CassandraSink) cassandraWriteOperation.getSink();
    }

    @Override
    public void open(String uId) throws IOException {
        this.uId = uId;
        session = ((CassandraSink) getWriteOperation().getSink()).connect(container);
        properties = (TCassandraOutputProperties) cassandraSink.properties;
        CQLManager cqlManager = new CQLManager(properties);
        preparedStatement = session.prepare(cqlManager.generatePreActionCQL());
        useBatch = properties.useUnloggedBatch.getValue();
        if (useBatch) {
            cassandraBatchUtil = new CassandraBatchUtil(session.getCluster(), cqlManager.getKeyspace(), cqlManager.getTableName(), cqlManager.getValueColumns());
            newOne = false;
            batchStatement = new BatchStatement(BatchStatement.Type.UNLOGGED);
        }
        if(boundStatement == null){
            //actually for batch mode, no need init BoundStatement here, but for boundStatementAdapterFactory, need
            boundStatement = new BoundStatement(preparedStatement);
        }
        boundStatementAdapterFactory.setContainerTypeFromInstance(boundStatement);
        CassandraAvroRegistry.get().buildAdaptersUsingDataType(boundStatementAdapterFactory, null);

    }

    @Override
    public void write(Object object) throws IOException {
        dataCount++;

        if(object == null){
            return;
        }

        if(object instanceof Row){
            //TODO optimizedWrite
        }

        if (useBatch) {
            boundStatement = new BoundStatement(preparedStatement);
        }

        if (adapterFactory == null) {
            adapterFactory = (IndexedRecordConverter<Object, ? extends IndexedRecord>) CassandraAvroRegistry.get().createIndexedRecordConverter(object.getClass());
        }

        boundStatement = boundStatementAdapterFactory.convertToDatum(adapterFactory.convertToAvro(object));

        if (useBatch) {
            currentKey = cassandraBatchUtil.getKey(boundStatement);
            newOne = lastKey != null && lastKey.compareTo(currentKey) != 0;
            if (newOne && batchStatement.size() > 0) {
                executeBatch();
            }
            batchStatement.add(boundStatement);
            lastKey = currentKey;
            if (batchStatement.size() >= properties.batchSize.getValue()) {
                executeBatch();
            }
        } else {
            try {
                session.execute(boundStatement);
            } catch (Exception e) {
                if (properties.dieOnError.getValue()) {
                    throw e;
                } else {
                    //FIXME(bchen) what can we do here? still print err
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    private void executeBatch() {
        session.execute(batchStatement);
        batchStatement.clear();
    }

    @Override
    public Result close() throws IOException {
        session.close();
        session.getCluster().close();
        return new Result(uId, dataCount, dataCount, 0);
    }

    @Override
    public WriteOperation<Result> getWriteOperation() {
        return cassandraWriteOperation;
    }

    @Override
    public Iterable<IndexedRecord> getSuccessfulWrites() {
        return Collections.unmodifiableList(successfulWrites);
    }

    @Override
    public Iterable<IndexedRecord> getRejectedWrites() {
        return Collections.unmodifiableList(rejectedWrites);
    }
}

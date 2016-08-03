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
package org.talend.components.jdbc.runtime.writer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.WriterWithFeedback;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jdbc.ReferAnotherComponent;
import org.talend.components.jdbc.runtime.JDBCSink;
import org.talend.components.jdbc.runtime.type.JDBCAvroRegistry;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputProperties;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

abstract public class JDBCOutputWriter implements WriterWithFeedback<Result, IndexedRecord, IndexedRecord> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JDBCOutputWriter.class);

    private WriteOperation<Result> writeOperation;

    protected Connection conn;

    protected JDBCSink sink;

    protected TJDBCOutputProperties properties;

    protected RuntimeContainer runtime;

    protected Result result;

    protected final List<IndexedRecord> successfulWrites = new ArrayList<>();

    protected final List<IndexedRecord> rejectedWrites = new ArrayList<>();

    protected int successCount;

    protected int rejectCount;

    protected boolean useBatch;

    protected int batchSize;

    protected int batchCount;

    protected int commitEvery;

    protected int commitCount;

    public JDBCOutputWriter(WriteOperation<Result> writeOperation, RuntimeContainer runtime) {
        this.writeOperation = writeOperation;
        this.runtime = runtime;
        sink = (JDBCSink) writeOperation.getSink();
        properties = (TJDBCOutputProperties) sink.properties;
    }

    abstract public void open(String uId) throws IOException;

    abstract public void write(Object datum) throws IOException;

    @Override
    public Result close() throws IOException {
        String refComponentId = ((ReferAnotherComponent) properties).getReferencedComponentId();
        if (refComponentId == null) {
            try {
                if (commitCount > 0) {
                    conn.commit();
                }
                conn.close();
            } catch (SQLException e) {
                throw new IOException(e);
            }
        }

        return result;
    }

    @Override
    public WriteOperation<Result> getWriteOperation() {
        return writeOperation;
    }

    private IndexedRecordConverter<Object, ? extends IndexedRecord> factory;

    @SuppressWarnings("unchecked")
    protected IndexedRecordConverter<Object, ? extends IndexedRecord> getFactory(Object datum) {
        if (null == factory) {
            factory = (IndexedRecordConverter<Object, ? extends IndexedRecord>) JDBCAvroRegistry.get()
                    .createIndexedRecordConverter(datum.getClass());
        }
        return factory;
    }

    @Override
    public List<IndexedRecord> getSuccessfulWrites() {
        return Collections.unmodifiableList(successfulWrites);
    }

    @Override
    public List<IndexedRecord> getRejectedWrites() {
        return Collections.unmodifiableList(rejectedWrites);
    }

    protected void handleSuccess(IndexedRecord input) {
        successCount++;
        successfulWrites.add(input);
    }

    protected void handleReject(IndexedRecord input, SQLException e) throws IOException {
        rejectCount++;
        Schema outSchema = properties.schemaReject.schema.getValue();
        IndexedRecord reject = new GenericData.Record(outSchema);
        for (Schema.Field outField : reject.getSchema().getFields()) {
            Object outValue = null;
            Schema.Field inField = input.getSchema().getField(outField.name());

            if (inField != null) {
                outValue = input.get(inField.pos());
            } else if ("errorCode".equals(outField.name())) {
                outValue = e.getSQLState();
            } else if ("errorMessage".equals(outField.name())) {
                outValue = e.getMessage();
            }

            reject.put(outField.pos(), outValue);
        }
        rejectedWrites.add(reject);
    }

    protected void executeCommit() throws SQLException {
        if (commitCount < commitEvery) {
            commitCount++;
        } else {
            commitCount = 0;
            conn.commit();
        }
    }

    protected void execute(IndexedRecord input, PreparedStatement statement) throws SQLException {
        if (useBatch) {
            if (batchCount < batchSize) {
                batchCount++;
                statement.addBatch();
            } else {
                batchCount = 0;
                int[] batchResult = statement.executeBatch();
            }
        } else {
            int insertResult = statement.executeUpdate();
            handleSuccess(input);
        }
    }
}

package org.talend.components.cassandra.runtime_3_0;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.DataType;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

/**
 * Creates an {@link IndexedRecordConverter} that knows how to interpret Cassandra {@link BoundStatement} objects.
 */
public class BoundStatementAdapterFactory extends CassandraBaseAdapterFactory<BoundStatement, BoundStatement, BoundStatement> {

    @Override
    public Class<BoundStatement> getDatumClass() {
        return BoundStatement.class;
    }

    @Override
    protected void setContainerTypeFromInstance(BoundStatement statement) {
        setContainerType(statement);
    }

    @Override
    public DataType getFieldType(int i) {
        return getContainerType().preparedStatement().getVariables().getType(i);
    }

    /**
     * This always returns the instance passed in {@link #setContainerTypeFromInstance(BoundStatement)}.
     */
    @Override
    protected BoundStatement createOrGetInstance() {
        return getContainerType();
    }
}

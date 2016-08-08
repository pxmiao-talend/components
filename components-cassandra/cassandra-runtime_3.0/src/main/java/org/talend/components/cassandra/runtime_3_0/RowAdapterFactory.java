package org.talend.components.cassandra.runtime_3_0;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.TupleValue;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

/**
 * Creates an {@link IndexedRecordConverter} that knows how to interpret Cassandra {@link Row} objects.
 */
public class RowAdapterFactory extends CassandraBaseAdapterFactory<Row, TupleValue, Row> {

    @Override
    public Class<Row> getDatumClass() {
        return Row.class;
    }

    @Override
    protected void setContainerTypeFromInstance(Row row) {
        setContainerType(row);
    }

    @Override
    protected DataType getFieldType(int i) {
        return getContainerType().getColumnDefinitions().getType(i);
    }

    @Override
    protected TupleValue createOrGetInstance() {
        // This should never happen, there is never any reason to create a Row.
        throw new UnmodifiableAdapterException();
    }
}

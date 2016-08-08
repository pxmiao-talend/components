package org.talend.components.cassandra.runtime_3_0.avro;

import com.datastax.driver.core.LocalDate;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.AvroConverter;

public class ConvertLocalDate implements AvroConverter<LocalDate, Long> {
    @Override
    public Schema getSchema() {
        return SchemaBuilder.builder().longBuilder().prop(SchemaConstants.JAVA_CLASS_FLAG, getDatumClass().getCanonicalName()).endLong();
    }

    @Override
    public Class<LocalDate> getDatumClass() {
        return LocalDate.class;
    }

    @Override
    public LocalDate convertToDatum(Long value) {
        return value == null ? null : LocalDate.fromMillisSinceEpoch(value);
    }

    @Override
    public Long convertToAvro(LocalDate value) {
        return value == null ? null : value.getMillisSinceEpoch();
    }
}

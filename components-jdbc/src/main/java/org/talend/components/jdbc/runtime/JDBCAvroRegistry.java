package org.talend.components.jdbc.runtime;

import java.math.BigDecimal;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.SchemaBuilder.FieldAssembler;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.di.DiSchemaConstants;
import org.talend.daikon.java8.SerializableFunction;

public class JDBCAvroRegistry extends AvroRegistry {

    public static final String FAMILY_NAME = "JDBC"; //$NON-NLS-1$

    private static final JDBCAvroRegistry sInstance = new JDBCAvroRegistry();

    private JDBCAvroRegistry() {

        registerSchemaInferrer(ResultSet.class, new SerializableFunction<ResultSet, Schema>() {

            /** Default serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public Schema apply(ResultSet t) {
                try {
                    return inferSchemaResultSet(t);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return null;
            }

        });

    }

    public static JDBCAvroRegistry get() {
        return sInstance;
    }

    /**
     * @return The family that uses the specific objects that this converter knows how to translate.
     */
    public String getFamilyName() {
        return FAMILY_NAME;
    }

    private Schema inferSchemaResultSet(ResultSet metadata) throws SQLException {
        if (!metadata.next()) {
            return null;
        }
        FieldAssembler<Schema> builder = SchemaBuilder.builder().record(metadata.getString("TABLE_NAME")).fields();
        do {
            Schema base;

            int size = metadata.getInt("COLUMN_SIZE");
            int scale = metadata.getInt("DECIMAL_DIGITS");

            int dbtype = metadata.getInt("DATA_TYPE");
            switch (dbtype) {
            case java.sql.Types.VARCHAR:
                base = Schema.create(Schema.Type.STRING);
                base.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, size);
                break;
            case java.sql.Types.INTEGER:
                base = Schema.create(Schema.Type.INT);
                base.addProp(SchemaConstants.TALEND_COLUMN_PRECISION, size);
                break;
            case java.sql.Types.DECIMAL:
                base = Schema.create(Schema.Type.STRING);
                base.addProp(SchemaConstants.TALEND_COLUMN_PRECISION, size);
                base.addProp(SchemaConstants.TALEND_COLUMN_SCALE, scale);
                break;
            case java.sql.Types.BIGINT:
                base = Schema.create(Schema.Type.STRING);
                base.addProp(SchemaConstants.TALEND_COLUMN_PRECISION, size);
                break;
            case java.sql.Types.NUMERIC:
                base = Schema.create(Schema.Type.STRING);
                base.addProp(SchemaConstants.TALEND_COLUMN_PRECISION, size);
                base.addProp(SchemaConstants.TALEND_COLUMN_SCALE, scale);
                break;
            case java.sql.Types.TINYINT:
                base = Schema.create(Schema.Type.STRING);
                base.addProp(SchemaConstants.TALEND_COLUMN_PRECISION, size);
                break;
            case java.sql.Types.DOUBLE:
                base = Schema.create(Schema.Type.DOUBLE);
                break;
            case java.sql.Types.FLOAT:
                base = Schema.create(Schema.Type.FLOAT);
                break;
            case java.sql.Types.DATE:
                base = Schema.create(Schema.Type.LONG);
                base.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd"); //$NON-NLS-1$
                break;
            case java.sql.Types.TIME:
                base = Schema.create(Schema.Type.LONG);
                base.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "HH:mm:ss"); //$NON-NLS-1$
                break;
            case java.sql.Types.TIMESTAMP:
                base = Schema.create(Schema.Type.LONG);
                base.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd HH:mm:ss.SSS"); //$NON-NLS-1$
                break;
            case java.sql.Types.BOOLEAN:
                base = Schema.create(Schema.Type.BOOLEAN);
                break;
            case java.sql.Types.CHAR:
                base = Schema.create(Schema.Type.STRING);
                break;
            default:
                base = Schema.create(Schema.Type.STRING);
                break;
            }

            base.addProp(SchemaConstants.TALEND_COLUMN_DB_TYPE, dbtype);

            boolean nullable = DatabaseMetaData.columnNullable == metadata.getInt("NULLABLE");

            String defaultValue = metadata.getString("COLUMN_DEF");
            if (defaultValue != null) {
                base.addProp(SchemaConstants.TALEND_COLUMN_DEFAULT, defaultValue);
            }

            Schema fieldSchema = nullable ? SchemaBuilder.builder().nullable().type(base) : base;

            String columnName = metadata.getString("COLUMN_NAME");
            base.addProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, columnName);
            if (null == defaultValue) {
                builder = builder.name(columnName).type(fieldSchema).noDefault();
            } else {
                builder = builder.name(columnName).type(fieldSchema).withDefault(defaultValue);
            }
        } while (metadata.next());

        return builder.endRecord();
    }

    public AvroConverter<String, ?> getConverterFromString(org.apache.avro.Schema.Field f) {
        Schema fieldSchema = AvroUtils.unwrapIfNullable(f.schema());

        switch (fieldSchema.getType()) {
        case BOOLEAN:
            return new StringToBooleanConverter(fieldSchema);
        case DOUBLE:
            return new StringToDoubleConverter(fieldSchema);
        case FLOAT:
            return new StringToFloatConverter(fieldSchema);
        case INT:
            return new StringToIntegerConverter(fieldSchema);
        case LONG:
            return new StringToDateConverter(fieldSchema);
        case STRING:
            return super.getConverter(String.class);
        default:
            throw new UnsupportedOperationException("The type " + fieldSchema.getType() + " is not supported.");
        }
    }

    public static abstract class AsStringConverter<T> implements AvroConverter<String, T> {

        private final Schema schema;

        AsStringConverter(Schema schema) {
            this.schema = schema;
        }

        @Override
        public Schema getSchema() {
            return schema;
        }

        @Override
        public Class<String> getDatumClass() {
            return String.class;
        }

        @Override
        public String convertToDatum(T value) {
            return value == null ? null : String.valueOf(value);
        }
    }

    public static class StringToBooleanConverter extends AsStringConverter<Boolean> {

        StringToBooleanConverter(Schema schema) {
            super(schema);
        }

        @Override
        public Boolean convertToAvro(String value) {
            return value == null ? null : Boolean.parseBoolean(value);
        }
    }

    public static class StringToDecimalConverter extends AsStringConverter<BigDecimal> {

        StringToDecimalConverter(Schema schema) {
            super(schema);
        }

        @Override
        public BigDecimal convertToAvro(String value) {
            return value == null ? null : new BigDecimal(value);
        }
    }

    public static class StringToDoubleConverter extends AsStringConverter<Double> {

        StringToDoubleConverter(Schema schema) {
            super(schema);
        }

        @Override
        public Double convertToAvro(String value) {
            return value == null ? null : Double.parseDouble(value);
        }
    }

    public static class StringToFloatConverter extends AsStringConverter<Float> {

        StringToFloatConverter(Schema schema) {
            super(schema);
        }

        @Override
        public Float convertToAvro(String value) {
            return value == null ? null : Float.parseFloat(value);
        }
    }

    public static class StringToDateConverter extends AsStringConverter<Long> {

        private final SimpleDateFormat format;

        StringToDateConverter(Schema schema) {
            super(schema);
            String pattern = schema.getProp(DiSchemaConstants.TALEND6_COLUMN_PATTERN);
            // TODO: null handling
            format = new SimpleDateFormat(pattern);
        }

        @Override
        public Long convertToAvro(String value) {
            try {
                return value == null ? null : format.parse(value).getTime();
            } catch (ParseException e) {
                // TODO: error handling
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        @Override
        public String convertToDatum(Long value) {
            return value == null ? null : format.format(new Date(value));
        }

    }

    public static class StringToIntegerConverter extends AsStringConverter<Integer> {

        StringToIntegerConverter(Schema schema) {
            super(schema);
        }

        @Override
        public Integer convertToAvro(String value) {
            return value == null ? null : Integer.parseInt(value);
        }
    }

}

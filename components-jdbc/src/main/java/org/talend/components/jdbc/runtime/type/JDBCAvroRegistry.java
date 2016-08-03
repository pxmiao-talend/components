package org.talend.components.jdbc.runtime.type;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.SchemaBuilder.FieldAssembler;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.java8.SerializableFunction;

public class JDBCAvroRegistry extends AvroRegistry {

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

    public JDBCConverter getConverter(Field f) {
        if (AvroUtils.isSameType(f.schema(), AvroUtils._string())) {
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    try {
                        return value.getString(f.pos());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

            };
        } else if (AvroUtils.isSameType(f.schema(), AvroUtils._int())) {
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    try {
                        return value.getInt(f.pos());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

            };
        } else if (AvroUtils.isSameType(f.schema(), AvroUtils._date())) {
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    try {
                        return value.getDate(f.pos());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

            };
        } else if (AvroUtils.isSameType(f.schema(), AvroUtils._decimal())) {
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    try {
                        return value.getBigDecimal(f.pos());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

            };
        } else if (AvroUtils.isSameType(f.schema(), AvroUtils._long())) {
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    try {
                        return value.getLong(f.pos());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

            };
        } else if (AvroUtils.isSameType(f.schema(), AvroUtils._double())) {
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    try {
                        return value.getDouble(f.pos());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

            };
        } else if (AvroUtils.isSameType(f.schema(), AvroUtils._float())) {
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    try {
                        return value.getFloat(f.pos());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

            };
        } else if (AvroUtils.isSameType(f.schema(), AvroUtils._boolean())) {
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    try {
                        return value.getBoolean(f.pos());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

            };
        } else if (AvroUtils.isSameType(f.schema(), AvroUtils._short())) {
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    try {
                        return value.getShort(f.pos());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

            };
        } else if (AvroUtils.isSameType(f.schema(), AvroUtils._character())) {
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    try {
                        return (char) value.getInt(f.pos());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

            };
        } else if (AvroUtils.isSameType(f.schema(), AvroUtils._byte())) {
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    try {
                        return value.getByte(f.pos());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

            };
        } else {
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    try {
                        return value.getString(f.pos());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

            };
        }
    }

    private abstract class JDBCConverter implements AvroConverter<ResultSet, Object> {

        @Override
        public Schema getSchema() {
            // do nothing
            return null;
        }

        @Override
        public Class<ResultSet> getDatumClass() {
            // do nothing
            return null;
        }

        @Override
        public ResultSet convertToDatum(Object value) {
            // do nothing
            return null;
        }

    }

}

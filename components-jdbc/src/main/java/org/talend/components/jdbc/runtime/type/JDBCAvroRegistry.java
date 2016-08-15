package org.talend.components.jdbc.runtime.type;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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

        registerSchemaInferrer(ResultSetMetaData.class, new SerializableFunction<ResultSetMetaData, Schema>() {

            /** Default serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public Schema apply(ResultSetMetaData t) {
                try {
                    return inferSchemaResultSetMetaData(t);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return null;
            }

        });

    }

    private Schema inferSchemaResultSetMetaData(ResultSetMetaData metadata) throws SQLException {
        FieldAssembler<Schema> builder = SchemaBuilder.builder().record("DYNAMIC").fields();

        int count = metadata.getColumnCount();
        for (int i = 0; i < count; i++) {
            Schema base;

            int size = metadata.getPrecision(i);
            int scale = metadata.getScale(i);

            int dbtype = metadata.getColumnType(i);
            base = sqlType2Avro(size, scale, dbtype);

            base.addProp(SchemaConstants.TALEND_COLUMN_DB_TYPE, dbtype);

            boolean nullable = ResultSetMetaData.columnNullable == metadata.isNullable(i);

            Schema fieldSchema = nullable ? SchemaBuilder.builder().nullable().type(base) : base;

            String dbColumnName = metadata.getColumnName(i);
            fieldSchema.addProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, dbColumnName);

            builder = builder.name(metadata.getColumnLabel(i)).type(fieldSchema).noDefault();
        }

        return builder.endRecord();
    }

    private Schema sqlType2Avro(int size, int scale, int dbtype) {
        Schema base;
        switch (dbtype) {
        case java.sql.Types.VARCHAR:
            base = AvroUtils._string();
            base.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, size);
            break;
        case java.sql.Types.INTEGER:
            base = AvroUtils._int();
            base.addProp(SchemaConstants.TALEND_COLUMN_PRECISION, size);
            break;
        case java.sql.Types.DECIMAL:
            base = AvroUtils._decimal();
            base.addProp(SchemaConstants.TALEND_COLUMN_PRECISION, size);
            base.addProp(SchemaConstants.TALEND_COLUMN_SCALE, scale);
            break;
        case java.sql.Types.BIGINT:
            base = AvroUtils._decimal();
            base.addProp(SchemaConstants.TALEND_COLUMN_PRECISION, size);
            break;
        case java.sql.Types.NUMERIC:
            base = AvroUtils._decimal();
            base.addProp(SchemaConstants.TALEND_COLUMN_PRECISION, size);
            base.addProp(SchemaConstants.TALEND_COLUMN_SCALE, scale);
            break;
        case java.sql.Types.TINYINT:
            base = AvroUtils._int();
            base.addProp(SchemaConstants.TALEND_COLUMN_PRECISION, size);
            break;
        case java.sql.Types.DOUBLE:
            base = AvroUtils._double();
            break;
        case java.sql.Types.FLOAT:
            base = AvroUtils._float();
            break;
        case java.sql.Types.DATE:
            base = AvroUtils._date();
            base.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd"); //$NON-NLS-1$
            break;
        case java.sql.Types.TIME:
            base = AvroUtils._date();
            base.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "HH:mm:ss"); //$NON-NLS-1$
            break;
        case java.sql.Types.TIMESTAMP:
            base = AvroUtils._date();
            base.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd HH:mm:ss.SSS"); //$NON-NLS-1$
            break;
        case java.sql.Types.BOOLEAN:
            base = AvroUtils._boolean();
            break;
        case java.sql.Types.CHAR:
            base = AvroUtils._character();
            break;
        default:
            base = AvroUtils._string();
            break;
        }
        return base;
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
            base = sqlType2Avro(size, scale, dbtype);

            base.addProp(SchemaConstants.TALEND_COLUMN_DB_TYPE, dbtype);

            boolean nullable = DatabaseMetaData.columnNullable == metadata.getInt("NULLABLE");

            String defaultValue = metadata.getString("COLUMN_DEF");
            if (defaultValue != null) {
                base.addProp(SchemaConstants.TALEND_COLUMN_DEFAULT, defaultValue);
            }

            Schema fieldSchema = nullable ? SchemaBuilder.builder().nullable().type(base) : base;

            String columnName = metadata.getString("COLUMN_NAME");
            fieldSchema.addProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, columnName);
            if (null == defaultValue) {
                builder = builder.name(columnName).type(fieldSchema).noDefault();
            } else {
                builder = builder.name(columnName).type(fieldSchema).withDefault(defaultValue);
            }
        } while (metadata.next());

        return builder.endRecord();
    }

    public JDBCConverter getConverter(final Field f) {
        if (AvroUtils.isSameType(f.schema(), AvroUtils._string())) {
            return new JDBCConverter() {

                @Override
                public Object convertToAvro(ResultSet value) {
                    try {
                        return value.getString(f.pos() + 1);
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
                        return value.getInt(f.pos() + 1);
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
                        return value.getDate(f.pos() + 1);
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
                        return value.getBigDecimal(f.pos() + 1);
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
                        return value.getLong(f.pos() + 1);
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
                        return value.getDouble(f.pos() + 1);
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
                        return value.getFloat(f.pos() + 1);
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
                        return value.getBoolean(f.pos() + 1);
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
                        return value.getShort(f.pos() + 1);
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
                        return (char) value.getInt(f.pos() + 1);
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
                        return value.getByte(f.pos() + 1);
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
                        return value.getString(f.pos() + 1);
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

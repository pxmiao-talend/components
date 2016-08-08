package org.talend.components.cassandra.runtime_3_0;

import org.apache.beam.sdk.io.cassandra.CassandraColumnDefinition;
import org.apache.beam.sdk.io.cassandra.CassandraRow;
import com.datastax.driver.core.*;
import com.google.common.collect.ImmutableMap;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.talend.components.cassandra.runtime_3_0.avro.ConvertLocalDate;
import org.talend.components.cassandra.runtime_3_0.beam.CassandraIORowConverter;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.container.ContainerReaderByIndex;
import org.talend.daikon.avro.container.ContainerRegistry;
import org.talend.daikon.avro.container.ContainerWriterByIndex;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.avro.converter.ConvertAvroList;
import org.talend.daikon.avro.converter.ConvertAvroMap;
import org.talend.daikon.java8.SerializableFunction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * http://docs.datastax.com/en/developer/java-driver/3.0/java-driver/reference/javaClass2Cql3Datatypes.html
 */
public class CassandraAvroRegistry extends AvroRegistry {

    public static final String TUPLE_RECORD_NAME = "TupleRecord"; // TODO(bchen) is there any common way?

    public static final String TUPLE_FIELD_PREFIX = "field"; // TODO(bchen) is there any common way?

    private static final CassandraAvroRegistry sInstance = new CassandraAvroRegistry();

    /**
     * Contains the DataType.Name from Cassandra where we can infer the Object type without any additional information.
     */
    private static final Map<DataType.Name, Class<?>> mCassandraNativeTypes;

    private static final Map<DataType.Name, Class<?>> mCassandraAllTypes;

    private static final Map<CassandraColumnDefinition.Type, Class<?>> mCassandraIOTypes;

    private static final CassandraContainerRegistry sContainerReadWrite = new CassandraContainerRegistry();

    static {
        // The DataType.Name that we can convert to Avro-compatible without any additional information.
        mCassandraNativeTypes = new ImmutableMap.Builder<DataType.Name, Class<?>>() //
                .put(DataType.Name.ASCII, String.class) //
                .put(DataType.Name.BIGINT, Long.class) //
                .put(DataType.Name.BLOB, ByteBuffer.class) // TODO(bchen) not byte[]?
                .put(DataType.Name.BOOLEAN, Boolean.class) //
                .put(DataType.Name.COUNTER, Long.class) //
                .put(DataType.Name.DATE, LocalDate.class) // new from 2.2
                .put(DataType.Name.DECIMAL, BigDecimal.class) //
                .put(DataType.Name.DOUBLE, Double.class) //
                .put(DataType.Name.FLOAT, Float.class) //
                .put(DataType.Name.INET, InetAddress.class) //
                .put(DataType.Name.INT, Integer.class) //
                .put(DataType.Name.SMALLINT, Short.class) // new from 2.2
                .put(DataType.Name.TEXT, String.class) //
                .put(DataType.Name.TIME, Long.class) // new from 2.2
                .put(DataType.Name.TIMESTAMP, Date.class) //
                .put(DataType.Name.TIMEUUID, UUID.class) //
                .put(DataType.Name.TINYINT, Byte.class) // new from 2.2
                .put(DataType.Name.UUID, UUID.class) //
                .put(DataType.Name.VARCHAR, String.class) //
                .put(DataType.Name.VARINT, BigInteger.class) //
                .build();
        mCassandraAllTypes = new ImmutableMap.Builder<DataType.Name, Class<?>>() //
                .putAll(mCassandraNativeTypes) //
                .put(DataType.Name.SET, Set.class) //
                .put(DataType.Name.LIST, List.class) //
                .put(DataType.Name.MAP, Map.class) //
                .put(DataType.Name.UDT, UDTValue.class) //
                .put(DataType.Name.TUPLE, TupleValue.class) //
                .build();
        mCassandraIOTypes = new ImmutableMap.Builder<CassandraColumnDefinition.Type,
                Class<?>>() //
                .put(CassandraColumnDefinition.Type.INT, Integer.class) //
                .put(CassandraColumnDefinition.Type.TEXT, String.class) //
                .build();
    }

    private CassandraAvroRegistry() {

        registerConverter(LocalDate.class, new ConvertLocalDate());

        // Ensure that other components know how to process Cassandra objects by sharing these facades.
        registerIndexedRecordConverter(Row.class, RowAdapterFactory.class);
        registerIndexedRecordConverter(UDTValue.class, UDTValueAdapterFactory.class);
        registerIndexedRecordConverter(TupleValue.class, TupleValueAdapterFactory.class);

        // Ensure that we know how to get Schemas for these Cassandra objects.
        registerSchemaInferrer(BoundStatement.class, new SerializableFunction<BoundStatement, Schema>() {
            @Override
            public Schema apply(BoundStatement boundStatement) {
                return inferSchemaColumnDefinitions("BoundStatement", boundStatement.preparedStatement().getVariables());
            }
        });
        registerSchemaInferrer(Row.class, new SerializableFunction<Row, Schema>() {
            @Override
            public Schema apply(Row row) {
                return inferSchemaColumnDefinitions("Row", row.getColumnDefinitions());
            }
        });
        registerSchemaInferrer(ColumnDefinitions.class, new SerializableFunction<ColumnDefinitions, Schema>() {
            @Override
            public Schema apply(ColumnDefinitions definitions) {
                return inferSchemaColumnDefinitions("Record", definitions);
            }
        });
        registerSchemaInferrer(UDTValue.class, new SerializableFunction<UDTValue, Schema>() {
            @Override
            public Schema apply(UDTValue udtValue) {
                return inferSchemaDataType(udtValue.getType());
            }
        });
        registerSchemaInferrer(TupleValue.class, new SerializableFunction<TupleValue, Schema>() {
            @Override
            public Schema apply(TupleValue tupleValue) {
                return inferSchemaDataType(tupleValue.getType());
            }
        });
        registerSchemaInferrer(TableMetadata.class, new SerializableFunction<TableMetadata, Schema>() {
            @Override
            public Schema apply(TableMetadata metadata) {
                return inferSchemaTableMetadata(metadata);
            }
        });
        registerSchemaInferrer(DataType.class, new SerializableFunction<DataType, Schema>() {
            @Override
            public Schema apply(DataType dataType) {
                return inferSchemaDataType(dataType);
            }
        });

        registerIndexedRecordConverter(CassandraRow.class, CassandraIORowConverter.class);

        registerSchemaInferrer(CassandraRow.class, new SerializableFunction<CassandraRow, Schema>
                () {
            @Override
            public Schema apply(CassandraRow row) {
                return inferSchemaCassandraIORow(row);
            }
        });

        registerSchemaInferrer(CassandraColumnDefinition.Type.class, new
                SerializableFunction<CassandraColumnDefinition.Type, Schema>() {
                    @Override
                    public Schema apply(CassandraColumnDefinition.Type dataType) {
                        return inferSchemaType(dataType);
                    }
                });
    }

    private Schema inferSchemaCassandraIORow(CassandraRow row) {
        List<CassandraColumnDefinition> definitions = row.getDefinitions();
        if (definitions.size() == 0) {
            return Schema.create(Schema.Type.NULL);
        }

        SchemaBuilder.FieldAssembler<Schema> fa = SchemaBuilder.record("row").namespace("cassandra")
                .fields();
        for (CassandraColumnDefinition definition : definitions) {
            fa = fa.name(definition.getColName()).type(inferSchema(definition.getColType()))
                    .noDefault();
        }
        return fa.endRecord();
    }

    private Schema inferSchemaType(CassandraColumnDefinition.Type type) {
        Class<?> nativeClass = mCassandraIOTypes.get(type);
        if (nativeClass != null) {
            AvroConverter<?, ?> ac = getConverter(nativeClass);
            if (ac == null) {
                // This should never occur if all of the native DataTypes are handled.
                throw new RuntimeException("The type " + type + " is not handled.");
            }
            Schema s = new Schema.Parser().parse(ac.getSchema().toString());
            return AvroUtils.wrapAsNullable(s);
        }
        // This should never occur.
        throw new RuntimeException("The type " + type + " is not handled.");
    }

    public <T> AvroConverter<? super T, ?> getConverter(CassandraColumnDefinition.Type type, Schema
            schema, Class<T> datumClass) {
        if (type != null) {
            if (mCassandraIOTypes.containsKey(type)) {
                AvroConverter<? super T, ?> converter = (AvroConverter<? super T, ?>) getConverter
                        (mCassandraIOTypes.get(type));
                if (converter != null) {
                    return converter;
                }
            }
        }
        // If a converter wasn't found, try and get using the class of the datum.
        AvroConverter<? super T, ?> converter = getConverter(datumClass);
        if (converter != null)
            return converter;

        // This should never occur.
        throw new RuntimeException("Cannot convert " + type + ".");
    }

    public static CassandraAvroRegistry get() {
        return sInstance;
    }

    /**
     * Infers an Avro schema for the given table. This can be an expensive operation so the schema should be cached
     * where possible. This is always an {@link Schema.Type#RECORD}.
     *
     * @param in the TableMetadata to analyse.
     * @return the schema for the given table.
     */
    private Schema inferSchemaTableMetadata(TableMetadata in) {
        // Tables without
        List<ColumnMetadata> columns = in.getColumns();
        if (columns.size() == 0)
            return Schema.create(Schema.Type.NULL);

        SchemaBuilder.FieldAssembler<Schema> fa = SchemaBuilder.record(in.getName()) //
                //.prop(PROP_CQL_CREATE, in.exportAsString()) // TODO(bchen) why need it?
                .namespace(in.getKeyspace().getName())
                .fields();
        for (ColumnMetadata columnMetadata : in.getColumns()) {
            fa = fa.name(columnMetadata.getName()).type(inferSchema(columnMetadata.getType())).noDefault();
        }
        return fa.endRecord();
    }

    /**
     * Infers an Avro schema for the given ColumnDefinitions. This can be an expensive operation so the schema should be
     * cached where possible. This is always an {@link Schema.Type#RECORD}.
     *
     * @param in the row to analyze.
     * @return the schema for the given row.
     */
    private Schema inferSchemaColumnDefinitions(String recordNamePrefix, ColumnDefinitions in) {
        if (in.size() == 0)
            return Schema.create(Schema.Type.NULL);

        // Use the first column to get the namespace and record name from the keyspace and table name.
        ColumnDefinitions.Definition cd1 = in.iterator().next();
        SchemaBuilder.FieldAssembler<Schema> fa = SchemaBuilder.record(cd1.getTable() + recordNamePrefix) //
                .namespace(cd1.getKeyspace() + '.' + cd1.getTable()).fields();
        for (ColumnDefinitions.Definition cd : in) {
            fa = fa.name(cd.getName()).type(inferSchema(cd.getType())).noDefault();
        }
        return fa.endRecord();
    }

    /**
     * Infers an Avro schema for the given Cassandra DataType. This can be an expensive operation so the schema should
     * be cached where possible.
     *
     * @param in the DataType to analyze.
     * @return the schema for the given row.
     */
    private Schema inferSchemaDataType(DataType in) {
        // Determine if a native type that can be easily mapped to a Schema exists.
        Class<?> nativeClass = mCassandraNativeTypes.get(in.getName());
        if (nativeClass != null) {
            AvroConverter<?, ?> ac = getConverter(nativeClass);
            if (ac == null) {
                // This should never occur if all of the native DataTypes are handled.
                throw new RuntimeException("The DataType " + in + " is not handled.");
            }
            // TODO(rskraba): There's gotta be something better than this.
            Schema s = new Schema.Parser().parse(ac.getSchema().toString());
            // TODO(rskraba): What do we do about unions? -- bchen: is it ok to add the db type in each element of the union?
            if (s.getType() != Schema.Type.UNION) {
                s.addProp(SchemaConstants.TALEND_COLUMN_DB_TYPE, in.getName().name());
            } else {
                List<Schema> types = s.getTypes();
                for (Schema type : types) {
                    type.addProp(SchemaConstants.TALEND_COLUMN_DB_TYPE, in.getName().name());
                }
                Schema.createUnion(types);
            }
            return AvroUtils.wrapAsNullable(s);
        }

        // If a native type doesn't exist for the DataType, then additional
        // information is necessary to create the
        // schema.
        if (in.isCollection()) {
            List<DataType> containedTypes = in.getTypeArguments();
            // LIST and SET are mapped to Schema.Type.ARRAY
            if (in.getName().equals(DataType.Name.LIST) || in.getName().equals(DataType.Name.SET)) {
                if (containedTypes == null || containedTypes.size() < 1)
                    throw new RuntimeException("The DataType " + in + " is not handled.");
                Schema itemSchema = inferSchema(containedTypes.get(0));
                return AvroUtils.wrapAsNullable(SchemaBuilder.array() //
                        .prop(SchemaConstants.TALEND_COLUMN_DB_TYPE, in.getName().name()) //
                        .items(itemSchema));
            } else if (in.getName().equals(DataType.Name.MAP)) {
                if (containedTypes == null || containedTypes.size() < 2)
                    throw new RuntimeException("The DataType " + in + " is not handled.");
                // TODO(rskraba): validate that the key type is Schema.Type.STRING compatible.
                Schema valueSchema = inferSchema(containedTypes.get(1));
                return AvroUtils.wrapAsNullable(SchemaBuilder.map() //
                        .prop(SchemaConstants.TALEND_COLUMN_DB_TYPE, in.getName().name()) //
                        .values(valueSchema));
            }
        } else if (in instanceof TupleType) {
            // TupleTypes are nested records with unnamed fields.
            TupleType tt = (TupleType) in;
            // TODO(rskraba): record naming strategy for tuples
            SchemaBuilder.FieldAssembler<Schema> fa = SchemaBuilder.record(TUPLE_RECORD_NAME) //
                    .prop(SchemaConstants.TALEND_COLUMN_DB_TYPE, DataType.Name.TUPLE.name()) //
                    .fields();
            int i = 0;
            for (DataType fieldType : tt.getComponentTypes()) {
                fa = fa.name(TUPLE_FIELD_PREFIX + (i++)).type(inferSchema(fieldType)).noDefault();
            }
            return AvroUtils.wrapAsNullable(fa.endRecord());
        } else if (in instanceof UserType) {
            // UserTypes are nested records.
            UserType ut = (UserType) in;
            SchemaBuilder.FieldAssembler<Schema> fa = SchemaBuilder.record(ut.getTypeName()) //
                    .namespace(ut.getKeyspace()) //
                    //.prop(PROP_CQL_CREATE, ut.asCQLQuery()) // TODO(bchen) why need it?
                    .prop(SchemaConstants.TALEND_COLUMN_DB_TYPE, DataType.Name.UDT.name()) //
                    .fields();
            for (String fieldName : ut.getFieldNames()) {
                DataType fieldType = ut.getFieldType(fieldName);
                fa = fa.name(fieldName).type(inferSchema(fieldType)).noDefault();
            }
            return AvroUtils.wrapAsNullable(fa.endRecord());
        }

        // This should never occur.
        throw new RuntimeException("The DataType " + in + " is not handled.");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    /**
     * Gets an AvroConverter for transforming between Cassandra objects and Avro-compatible objects using Cassandra type
     * information.
     *
     * @param type
     * @param schema
     * @param datumClass
     * @return
     */
    public <T> AvroConverter<? super T, ?> getConverter(DataType type, Schema schema, Class<T> datumClass) {
        // If the DataType is present, then use it to get the correct converter.
        if (type != null) {
            // Primitive types are easiest :)
            if (mCassandraNativeTypes.containsKey(type.getName())) {
                AvroConverter<? super T, ?> converter = (AvroConverter<? super T, ?>) getConverter(
                        mCassandraNativeTypes.get(type.getName()));
                if (converter != null)
                    return converter;
            }

            // The types that can have nested values require more work to put together.
            switch (type.getName()) {
                case LIST:
                case SET:
                    DataType elementType = type.getTypeArguments().get(0);
                    Class<?> elementClass = mCassandraAllTypes.get(elementType.getName());
                    Schema elementSchema = AvroUtils.unwrapIfNullable(AvroUtils.unwrapIfNullable(schema).getElementType());
                    return (AvroConverter<? super T, ?>) new ConvertAvroList(datumClass, schema,
                            getConverter(elementType, elementSchema, elementClass));
                case MAP:
                    DataType valueType = type.getTypeArguments().get(1);
                    Class<?> valueClass = mCassandraAllTypes.get(valueType.getName());
                    Schema valueSchema = AvroUtils.unwrapIfNullable(AvroUtils.unwrapIfNullable(schema).getValueType());
                    return (AvroConverter<? super T, ?>) new ConvertAvroMap(datumClass, schema,
                            getConverter(valueType, valueSchema, valueClass));
                case TUPLE:
                    return (AvroConverter<? super T, ?>) createIndexedRecordConverter(TupleValue.class);
                case UDT:
                    return (AvroConverter<? super T, ?>) createIndexedRecordConverter(UDTValue.class);
                default:
                    break;
            }
        }

        // If a converter wasn't found, try and get using the class of the datum.
        AvroConverter<? super T, ?> converter = getConverter(datumClass);
        if (converter != null)
            return converter;

        // This should never occur.
        throw new RuntimeException("Cannot convert " + type + ".");
    }

    public ContainerReaderByIndex<GettableByIndexData, ?> getReader(DataType type) {
        return sContainerReadWrite.getReader(type);
    }

    public ContainerWriterByIndex<SettableByIndexData<?>, ?> getWriter(DataType type) {
        return sContainerReadWrite.getWriter(type);
    }

    public static String getDataType(Schema schema) {
        schema = AvroUtils.unwrapIfNullable(schema);
        String overrideDataType = schema.getProp(SchemaConstants.TALEND_COLUMN_DB_TYPE);
        if (overrideDataType == null || "".equals(overrideDataType)) {
            //FIXME(bchen) give the best choice based on the current avro type
        }
        return overrideDataType.toLowerCase();
    }

    public void buildAdaptersUsingDataType(AvroConverter<?, ?> ac, DataType t) {
        if (t != null) {
            switch (t.getName()) {
                // The container types need to be recursively built.
                case LIST:
                case SET:
                    for (AvroConverter<?, ?> nestedAc : ((ConvertAvroList<?, ?>) ac).getNestedAvroConverters())
                        buildAdaptersUsingDataType(nestedAc, t.getTypeArguments().get(0));
                    return;
                case MAP:
                    for (AvroConverter<?, ?> nestedAc : ((ConvertAvroMap<?, ?>) ac).getNestedAvroConverters())
                        buildAdaptersUsingDataType(nestedAc, t.getTypeArguments().get(1));
                    return;
                // User defined types need to have their type set, and then fall through to have their fields built.
                case TUPLE:
                    TupleValueAdapterFactory tvff = (TupleValueAdapterFactory) ac;
                    tvff.setContainerType((TupleType) t);
                    break;
                case UDT:
                    UDTValueAdapterFactory uvff = (UDTValueAdapterFactory) ac;
                    uvff.setContainerType((UserType) t);
                    break;
                default:
                    // All other types do not need to be built.
                    return;
            }
        }

        // Build all of the fields.
        if (ac instanceof CassandraBaseAdapterFactory) {
            CassandraBaseAdapterFactory<?, ?, ?> cbff = (CassandraBaseAdapterFactory<?, ?, ?>) ac;
            int i = 0;
            for (AvroConverter<?, ?> nestedAc : cbff.getNestedAvroConverters())
                buildAdaptersUsingDataType(nestedAc, cbff.getFieldType(i++));
        }
    }

    /**
     * A utility class for getting callbacks for reading and writing to Cassandra containers.
     */
    //TODO(bchen) can it be DataType.Name, to accordance with mCassandraAllTypes
    private static class CassandraContainerRegistry
            extends ContainerRegistry<DataType, GettableByIndexData, SettableByIndexData<?>> {

        public CassandraContainerRegistry() {
            // The following types can be read without requiring any additional information.
            registerReader(DataType.ascii(), new ContainerReaderByIndex<GettableByIndexData, String>() {
                @Override
                public String readValue(GettableByIndexData obj, int index) {
                    return obj.getString(index);
                }
            });
            registerReader(DataType.bigint(), new ContainerReaderByIndex<GettableByIndexData, Long>() {
                @Override
                public Long readValue(GettableByIndexData obj, int index) {
                    return obj.getLong(index);
                }
            });
            registerReader(DataType.blob(), new ContainerReaderByIndex<GettableByIndexData, ByteBuffer>() {
                @Override
                public ByteBuffer readValue(GettableByIndexData obj, int index) {
                    return obj.getBytes(index);
                }
            });
            registerReader(DataType.cboolean(), new ContainerReaderByIndex<GettableByIndexData, Boolean>() {
                @Override
                public Boolean readValue(GettableByIndexData obj, int index) {
                    return obj.getBool(index);
                }
            });
            registerReader(DataType.counter(), new ContainerReaderByIndex<GettableByIndexData, Long>() {
                @Override
                public Long readValue(GettableByIndexData obj, int index) {
                    return obj.getLong(index);
                }
            });
            registerReader(DataType.date(), new ContainerReaderByIndex<GettableByIndexData, LocalDate>() {
                @Override
                public LocalDate readValue(GettableByIndexData obj, int index) {
                    return obj.getDate(index);
                }
            }); // from 2.2
            registerReader(DataType.decimal(), new ContainerReaderByIndex<GettableByIndexData, BigDecimal>() {
                @Override
                public BigDecimal readValue(GettableByIndexData obj, int index) {
                    return obj.getDecimal(index);
                }
            });
            registerReader(DataType.cdouble(), new ContainerReaderByIndex<GettableByIndexData, Double>() {
                @Override
                public Double readValue(GettableByIndexData obj, int index) {
                    return obj.getDouble(index);
                }
            });
            registerReader(DataType.cfloat(), new ContainerReaderByIndex<GettableByIndexData, Float>() {
                @Override
                public Float readValue(GettableByIndexData obj, int index) {
                    return obj.getFloat(index);
                }
            });
            registerReader(DataType.inet(), new ContainerReaderByIndex<GettableByIndexData, InetAddress>() {
                @Override
                public InetAddress readValue(GettableByIndexData obj, int index) {
                    return obj.getInet(index);
                }
            });
            registerReader(DataType.cint(), new ContainerReaderByIndex<GettableByIndexData, Integer>() {
                @Override
                public Integer readValue(GettableByIndexData obj, int index) {
                    return obj.getInt(index);
                }
            });
            registerReader(DataType.smallint(), new ContainerReaderByIndex<GettableByIndexData, Short>() {
                @Override
                public Short readValue(GettableByIndexData obj, int index) {
                    return obj.getShort(index);
                }
            }); // from 2.2
            registerReader(DataType.text(), new ContainerReaderByIndex<GettableByIndexData, String>() {
                @Override
                public String readValue(GettableByIndexData obj, int index) {
                    return obj.getString(index);
                }
            });
            registerReader(DataType.time(), new ContainerReaderByIndex<GettableByIndexData, Long>() {
                @Override
                public Long readValue(GettableByIndexData obj, int index) {
                    return obj.getTime(index);
                }
            }); // from 2.2
            registerReader(DataType.timestamp(), new ContainerReaderByIndex<GettableByIndexData, Date>() {
                @Override
                public Date readValue(GettableByIndexData obj, int index) {
                    return obj.getTimestamp(index);
                }
            }); // FIXME(bchen) different method between 2.1 and 3.0 API
            registerReader(DataType.timeuuid(), new ContainerReaderByIndex<GettableByIndexData, UUID>() {
                @Override
                public UUID readValue(GettableByIndexData obj, int index) {
                    return obj.getUUID(index);
                }
            });
            registerReader(DataType.tinyint(), new ContainerReaderByIndex<GettableByIndexData, Byte>() {
                @Override
                public Byte readValue(GettableByIndexData obj, int index) {
                    return obj.getByte(index);
                }
            }); // from 2.2
            registerReader(DataType.uuid(), new ContainerReaderByIndex<GettableByIndexData, UUID>() {
                @Override
                public UUID readValue(GettableByIndexData obj, int index) {
                    return obj.getUUID(index);
                }
            });
            registerReader(DataType.varchar(), new ContainerReaderByIndex<GettableByIndexData, String>() {
                @Override
                public String readValue(GettableByIndexData obj, int index) {
                    return obj.getString(index);
                }
            });
            registerReader(DataType.varint(), new ContainerReaderByIndex<GettableByIndexData, BigInteger>() {
                @Override
                public BigInteger readValue(GettableByIndexData obj, int index) {
                    return obj.getVarint(index);
                }
            });

            // The following types can be written without any additional information.
            registerWriter(DataType.ascii(), new ContainerWriterByIndex<SettableByIndexData<?>, String>() {
                @Override
                public void writeValue(SettableByIndexData<?> c, int i, String v) {
                    c.setString(i, v);
                }
            });
            registerWriter(DataType.bigint(), new ContainerWriterByIndex<SettableByIndexData<?>, Long>() {
                @Override
                public void writeValue(SettableByIndexData<?> c, int i, Long v) {
                    c.setLong(i, v);
                }
            });
            registerWriter(DataType.blob(), new ContainerWriterByIndex<SettableByIndexData<?>, ByteBuffer>() {
                @Override
                public void writeValue(SettableByIndexData<?> c, int i, ByteBuffer v) {
                    c.setBytes(i, v);
                }
            });
            registerWriter(DataType.cboolean(), new ContainerWriterByIndex<SettableByIndexData<?>, Boolean>() {
                @Override
                public void writeValue(SettableByIndexData<?> c, int i, Boolean v) {
                    c.setBool(i, v);
                }
            });
            registerWriter(DataType.counter(), new ContainerWriterByIndex<SettableByIndexData<?>, Long>() {
                @Override
                public void writeValue(SettableByIndexData<?> c, int i, Long v) {
                    c.setLong(i, v);
                }
            });
            registerWriter(DataType.date(), new ContainerWriterByIndex<SettableByIndexData<?>, LocalDate>() {
                @Override
                public void writeValue(SettableByIndexData<?> c, int i, LocalDate v) {
                    c.setDate(i, v);
                }
            }); // from 2.2
            registerWriter(DataType.decimal(), new ContainerWriterByIndex<SettableByIndexData<?>, BigDecimal>() {
                @Override
                public void writeValue(SettableByIndexData<?> c, int i, BigDecimal v) {
                    c.setDecimal(i, v);
                }
            });
            registerWriter(DataType.cdouble(), new ContainerWriterByIndex<SettableByIndexData<?>, Double>() {
                @Override
                public void writeValue(SettableByIndexData<?> c, int i, Double v) {
                    c.setDouble(i, v);
                }
            });
            registerWriter(DataType.cfloat(), new ContainerWriterByIndex<SettableByIndexData<?>, Float>() {
                @Override
                public void writeValue(SettableByIndexData<?> c, int i, Float v) {
                    c.setFloat(i, v);
                }
            });
            registerWriter(DataType.inet(), new ContainerWriterByIndex<SettableByIndexData<?>, InetAddress>() {
                @Override
                public void writeValue(SettableByIndexData<?> c, int i, InetAddress v) {
                    c.setInet(i, v);
                }
            });
            registerWriter(DataType.cint(), new ContainerWriterByIndex<SettableByIndexData<?>, Integer>() {
                @Override
                public void writeValue(SettableByIndexData<?> c, int i, Integer v) {
                    c.setInt(i, v);
                }
            });
            registerWriter(DataType.smallint(), new ContainerWriterByIndex<SettableByIndexData<?>, Short>() {
                @Override
                public void writeValue(SettableByIndexData<?> c, int i, Short v) {
                    c.setShort(i, v);
                }
            }); // from 2.2
            registerWriter(DataType.text(), new ContainerWriterByIndex<SettableByIndexData<?>, String>() {
                @Override
                public void writeValue(SettableByIndexData<?> c, int i, String v) {
                    c.setString(i, v);
                }
            });
            registerWriter(DataType.time(), new ContainerWriterByIndex<SettableByIndexData<?>, Long>() {
                @Override
                public void writeValue(SettableByIndexData<?> c, int i, Long v) {
                    c.setTime(i, v);
                }
            }); // from 2.2
            registerWriter(DataType.timestamp(), new ContainerWriterByIndex<SettableByIndexData<?>, Date>() {
                @Override
                public void writeValue(SettableByIndexData<?> c, int i, Date v) {
                    c.setTimestamp(i, v);
                }
            }); // FIXME(bchen) different method between 2.1 and 3.0 API
            registerWriter(DataType.timeuuid(), new ContainerWriterByIndex<SettableByIndexData<?>, UUID>() {
                @Override
                public void writeValue(SettableByIndexData<?> c, int i, UUID v) {
                    c.setUUID(i, v);
                }
            });
            registerWriter(DataType.tinyint(), new ContainerWriterByIndex<SettableByIndexData<?>, Byte>() {
                @Override
                public void writeValue(SettableByIndexData<?> c, int i, Byte v) {
                    c.setByte(i, v);
                }
            }); // from 2.2
            registerWriter(DataType.uuid(), new ContainerWriterByIndex<SettableByIndexData<?>, UUID>() {
                @Override
                public void writeValue(SettableByIndexData<?> c, int i, UUID v) {
                    c.setUUID(i, v);
                }
            });
            registerWriter(DataType.varchar(), new ContainerWriterByIndex<SettableByIndexData<?>, String>() {
                @Override
                public void writeValue(SettableByIndexData<?> c, int i, String v) {
                    c.setString(i, v);
                }
            });
            registerWriter(DataType.varint(), new ContainerWriterByIndex<SettableByIndexData<?>, BigInteger>() {
                @Override
                public void writeValue(SettableByIndexData<?> c, int i, BigInteger v) {
                    c.setVarint(i, v);
                }
            });
        }

        @Override
        public ContainerReaderByIndex<GettableByIndexData, ?> getReader(DataType type) {
            ContainerReaderByIndex<GettableByIndexData, ?> reader = super.getReader(type);
            if (reader != null)
                return reader;

            if (type.isCollection()) {
                final List<DataType> containedTypes = type.getTypeArguments();
                if (type.getName().equals(DataType.Name.LIST)) {
                    return new ContainerReaderByIndex<GettableByIndexData, Object>() {
                        @Override
                        public Object readValue(GettableByIndexData c, int i) {
                            return c.getList(i, mCassandraAllTypes.get(containedTypes.get(0).getName()));
                        }
                    };
                } else if (type.getName().equals(DataType.Name.SET)) {
                    // TODO: is there a better way to enforce a consistent order after read?
                    return new ContainerReaderByIndex<GettableByIndexData, Object>() {
                        @Override
                        public Object readValue(GettableByIndexData c, int i) {
                            return new ArrayList<>(c.getSet(i, mCassandraAllTypes.get(containedTypes.get(0).getName())));
                        }
                    };
                } else if (type.getName().equals(DataType.Name.MAP)) {
                    return new ContainerReaderByIndex<GettableByIndexData, Object>() {
                        @Override
                        public Object readValue(GettableByIndexData c, int i) {
                            return c.getMap(i, mCassandraAllTypes.get(containedTypes.get(0).getName()), mCassandraAllTypes.get(containedTypes.get(1).getName()));
                        }
                    };
                }
            }

            switch (type.getName()) {
                case TUPLE:
                    return new ContainerReaderByIndex<GettableByIndexData, Object>() {
                        @Override
                        public Object readValue(GettableByIndexData c, int i) {
                            return c.getTupleValue(i);
                        }
                    };
                case UDT:
                    return new ContainerReaderByIndex<GettableByIndexData, Object>() {
                        @Override
                        public Object readValue(GettableByIndexData c, int i) {
                            return c.getUDTValue(i);
                        }
                    };
                default:
                    throw new RuntimeException("The DataType " + type + " is not handled.");
            }
        }

        @Override
        public ContainerWriterByIndex<SettableByIndexData<?>, ?> getWriter(DataType type) {
            ContainerWriterByIndex<SettableByIndexData<?>, ?> writer = super.getWriter(type);
            if (writer != null)
                return writer;

            if (type.isCollection()) {
                if (type.getName().equals(DataType.Name.LIST)) {
                    return new ContainerWriterByIndex<SettableByIndexData<?>, List<?>>() {
                        @Override
                        public void writeValue(SettableByIndexData<?> c, int i, List<?> v) {
                            c.setList(i, v);
                        }
                    };
                } else if (type.getName().equals(DataType.Name.SET)) {
                    // TODO: is there a better way to enforce a consistent order after read?
                    return new ContainerWriterByIndex<SettableByIndexData<?>, List<?>>() {
                        @Override
                        public void writeValue(SettableByIndexData<?> c, int i, List<?> v) {
                            c.setSet(i, new HashSet<>(v));
                        }
                    };
                } else if (type.getName().equals(DataType.Name.MAP)) {
                    return new ContainerWriterByIndex<SettableByIndexData<?>, Map<?, ?>>() {
                        @Override
                        public void writeValue(SettableByIndexData<?> c, int i, Map<?, ?> v) {
                            c.setMap(i, v);
                        }
                    };
                }
            }

            switch (type.getName()) {
                case TUPLE:
                    return new ContainerWriterByIndex<SettableByIndexData<?>, TupleValue>() {
                        @Override
                        public void writeValue(SettableByIndexData<?> c, int i, TupleValue v) {
                            c.setTupleValue(i, v);
                        }
                    };
                case UDT:
                    return new ContainerWriterByIndex<SettableByIndexData<?>, UDTValue>() {
                        @Override
                        public void writeValue(SettableByIndexData<?> c, int i, UDTValue v) {
                            c.setUDTValue(i, v);
                        }
                    };
                default:
                    throw new RuntimeException("The DataType " + type + " is not handled.");
            }
        }

    }

}

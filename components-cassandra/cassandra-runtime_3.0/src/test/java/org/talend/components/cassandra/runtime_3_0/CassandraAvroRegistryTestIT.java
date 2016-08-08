package org.talend.components.cassandra.runtime_3_0;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;
import org.apache.avro.Schema;
import org.junit.Test;
import org.talend.components.cassandra.CassandraTestBase;
import org.talend.components.cassandra.runtime_3_0.CassandraAvroRegistry;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for the {@link CassandraAvroRegistry}.
 */
public class CassandraAvroRegistryTestIT extends CassandraTestBase{

    private static final CassandraAvroRegistry sRegistry = CassandraAvroRegistry.get();

    /**
     * Basic test case adapting a simple {@link Row} from the embedded database.
     */
    @Test
    public void testBasic() {
        Row r = mCass.execute("SELECT st_text FROM " + mCass.getTableSrc()).one();
        assertThat(r, not(nullValue()));

        Schema s = sRegistry.inferSchema(r);
        assertThat(s.toString().replace('"', '\''), //
                is("{'type':'record','name':'example_srcRow','namespace':'cassandratestbase.example_src','fields':[" //
                        + "{'name':'st_text'," //
                        + "'type':[{'type':'string','"+SchemaConstants.TALEND_COLUMN_DB_TYPE+"':'VARCHAR'},'null']}" //
                        + "]}")); //
    }

    /**
     * Helper method to call {@link CassandraAvroRegistry#getSchema} on Row objects fetched from the Cassandra database.
     * Each row has exactly one field.
     * 
     * @param fieldName
     * @param tableName
     * @return The schema only for that one field in the Row.
     */
    private Schema getSchemaForOneColumnExampleRow(String fieldName, String tableName) {
        // Get the schema for a Row with only the given field name.
        Schema s = sRegistry.inferSchema(mCass.execute("SELECT " + fieldName + " FROM " + tableName).one());
        // Rows always return records.
        assertThat(s.getType(), is(Schema.Type.RECORD));
        assertThat(s.getName(), is(tableName + "Row"));
        assertThat(s.getNamespace(), is(mCass.getKeySpace() + '.' + tableName));
        // No properties for a row.
        assertThat(s.getObjectProps().size(), is(0));
        // Exactly one field for this example.
        assertThat(s.getFields(), hasSize(1));
        assertThat(s.getFields().get(0).name(), is(fieldName));
        // And return the Schema only for that field.
        return s.getFields().get(0).schema();
    }

    /**
     * As above, but just for the default example_src table.
     * 
     * @param fieldName
     * @return
     */
    private Schema getSchemaForOneColumnExampleRow(String fieldName) {
        return getSchemaForOneColumnExampleRow(fieldName, mCass.getTableSrc());
    }

    /**
     * Helper method to unsure that a Schema is a simple, nullable type, against the name and expected non-null schema
     * type.
     */
    private Schema assertSimpleNullable(Schema s, DataType.Name name, Schema.Type schemaType) {
        // Check the nullable type.
        assertThat(s.getType(), is(Schema.Type.UNION));
        assertThat(s.getTypes(), hasSize(2));
        assertThat(s.getTypes(), hasItem(Schema.create(Schema.Type.NULL)));

        // check the other type.
        s = AvroUtils.unwrapIfNullable(s);
        assertThat(s.getObjectProps().entrySet(), hasSize(greaterThanOrEqualTo(1)));
        assertThat(s.getObjectProps(), hasKey(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertThat(s.getProp(SchemaConstants.TALEND_COLUMN_DB_TYPE), is(name.name()));
        assertThat(s, hasProperty("type", is(schemaType)));
        return s;
    }

    /**
     * Validates the schema associated with our UDT type called udt_item.
     * 
     * @param s the schema associated with a UDT type.
     */
    public void assertUdtItem(Schema s) {
        s = assertSimpleNullable(s, DataType.Name.UDT, Schema.Type.RECORD);

        assertThat(s.getType(), is(Schema.Type.RECORD));
        assertThat(s.getName(), is("udt_item"));
        assertThat(s.getObjectProps().size(), is(1));
//        assertThat(s.getProp(CassandraAvroRegistry.PROP_CQL_CREATE),
//                is("CREATE TYPE cassandraavroregistrytest.udt_item (id int,name varchar,valid boolean,value double);"));
        assertThat(s.getProp(SchemaConstants.TALEND_COLUMN_DB_TYPE), is(DataType.Name.UDT.name()));
        assertThat(s.getFields(), hasSize(4));

        {
            Schema.Field f = s.getFields().get(0);
            assertThat(f.name(), is("id"));
            assertSimpleNullable(f.schema(), DataType.Name.INT, Schema.Type.INT);
        }
        {
            Schema.Field f = s.getFields().get(1);
            assertThat(f.name(), is("name"));
            assertSimpleNullable(f.schema(), DataType.Name.VARCHAR, Schema.Type.STRING);
        }
        {
            Schema.Field f = s.getFields().get(2);
            assertThat(f.name(), is("valid"));
            assertSimpleNullable(f.schema(), DataType.Name.BOOLEAN, Schema.Type.BOOLEAN);
        }
        {
            Schema.Field f = s.getFields().get(3);
            assertThat(f.name(), is("value"));
            assertSimpleNullable(f.schema(), DataType.Name.DOUBLE, Schema.Type.DOUBLE);
        }
    }

    @Test
    public void testGetSchemaNativeTypes() {
        assertSimpleNullable(getSchemaForOneColumnExampleRow("st_ascii"), DataType.Name.ASCII, Schema.Type.STRING);
        assertSimpleNullable(getSchemaForOneColumnExampleRow("st_bigint"), DataType.Name.BIGINT, Schema.Type.LONG);
        assertSimpleNullable(getSchemaForOneColumnExampleRow("st_blob"), DataType.Name.BLOB, Schema.Type.BYTES);
        assertSimpleNullable(getSchemaForOneColumnExampleRow("st_boolean"), DataType.Name.BOOLEAN, Schema.Type.BOOLEAN);
        assertSimpleNullable(getSchemaForOneColumnExampleRow("st_decimal"), DataType.Name.DECIMAL, Schema.Type.STRING);
        assertSimpleNullable(getSchemaForOneColumnExampleRow("st_double"), DataType.Name.DOUBLE, Schema.Type.DOUBLE);
        assertSimpleNullable(getSchemaForOneColumnExampleRow("st_float"), DataType.Name.FLOAT, Schema.Type.FLOAT);
        assertSimpleNullable(getSchemaForOneColumnExampleRow("st_int"), DataType.Name.INT, Schema.Type.INT);
        // This is read from the row itself as VARCHAR, never as TEXT.
        assertSimpleNullable(getSchemaForOneColumnExampleRow("st_text"), DataType.Name.VARCHAR, Schema.Type.STRING);
        assertSimpleNullable(getSchemaForOneColumnExampleRow("st_timestamp"), DataType.Name.TIMESTAMP, Schema.Type.LONG);
        assertSimpleNullable(getSchemaForOneColumnExampleRow("st_timeuuid"), DataType.Name.TIMEUUID, Schema.Type.STRING);
        assertSimpleNullable(getSchemaForOneColumnExampleRow("st_uuid"), DataType.Name.UUID, Schema.Type.STRING);
        assertSimpleNullable(getSchemaForOneColumnExampleRow("st_varchar"), DataType.Name.VARCHAR, Schema.Type.STRING);
        assertSimpleNullable(getSchemaForOneColumnExampleRow("st_varint"), DataType.Name.VARINT, Schema.Type.STRING);

        // The counter is in a separate table.
        assertSimpleNullable(getSchemaForOneColumnExampleRow("st_counter", mCass.getTableCounter()), DataType.Name.COUNTER,
                Schema.Type.LONG);

        // TODO:
        // Not yet implemented in our version of CQL.
        // + "st_date date, " //
        // + "st_smallint smallint, " //
        // + "st_tinyint tinyint, " //
    }

    @Test
    public void testGetSchemaListType() {
        Schema s = getSchemaForOneColumnExampleRow("st_list");
        s = assertSimpleNullable(s, DataType.Name.LIST, Schema.Type.ARRAY);
        assertUdtItem(s.getElementType());
    }

    @Test
    public void testGetSchemaMapType() {
        Schema s = getSchemaForOneColumnExampleRow("st_map");
        s = assertSimpleNullable(s, DataType.Name.MAP, Schema.Type.MAP);
        assertUdtItem(s.getValueType());
    }

    @Test
    public void testGetSchemaSetType() {
        Schema s = getSchemaForOneColumnExampleRow("st_set");
        s = assertSimpleNullable(s, DataType.Name.SET, Schema.Type.ARRAY);
        assertUdtItem(s.getElementType());
    }

    @Test
    public void testGetSchemaTupleType() {
        Schema s = getSchemaForOneColumnExampleRow("st_tuple");
        s = assertSimpleNullable(s, DataType.Name.TUPLE, Schema.Type.RECORD);
        assertThat(s.getName(), is(CassandraAvroRegistry.TUPLE_RECORD_NAME));
        assertThat(s.getFields(), hasSize(3));

        {
            Schema.Field f = s.getFields().get(0);
            assertThat(f.name(), is(CassandraAvroRegistry.TUPLE_FIELD_PREFIX + "0"));
            assertSimpleNullable(f.schema(), DataType.Name.INT, Schema.Type.INT);
        }
        {
            Schema.Field f = s.getFields().get(1);
            assertThat(f.name(), is(CassandraAvroRegistry.TUPLE_FIELD_PREFIX + "1"));
            assertSimpleNullable(f.schema(), DataType.Name.VARCHAR, Schema.Type.STRING);
        }
        {
            Schema.Field f = s.getFields().get(2);
            assertThat(f.name(), is(CassandraAvroRegistry.TUPLE_FIELD_PREFIX + "2"));
            assertSimpleNullable(f.schema(), DataType.Name.FLOAT, Schema.Type.FLOAT);
        }

    }

    @Test
    public void testGetSchemaUdtType() {
        assertUdtItem(getSchemaForOneColumnExampleRow("st_udt"));
    }
}

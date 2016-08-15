package org.talend.components.jdbc.runtime.type;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.avro.Schema;
import org.talend.daikon.avro.AvroUtils;

public class JDBCMapping {

    public static void setValue(final PreparedStatement statement, final Schema.Field f, final Object value) throws SQLException {
        int index = f.pos() + 1;
        if (AvroUtils.isSameType(f.schema(), AvroUtils._string())) {
            statement.setString(index, (String) value);
        } else if (AvroUtils.isSameType(f.schema(), AvroUtils._int())) {
            statement.setInt(index, (Integer) value);
        } else if (AvroUtils.isSameType(f.schema(), AvroUtils._date())) {
            statement.setTimestamp(index, new java.sql.Timestamp(((java.util.Date) value).getTime()));
        } else if (AvroUtils.isSameType(f.schema(), AvroUtils._decimal())) {
            statement.setBigDecimal(index, (BigDecimal) value);
        } else if (AvroUtils.isSameType(f.schema(), AvroUtils._long())) {
            statement.setLong(index, (Long) value);
        } else if (AvroUtils.isSameType(f.schema(), AvroUtils._double())) {
            statement.setDouble(index, (Double) value);
        } else if (AvroUtils.isSameType(f.schema(), AvroUtils._float())) {
            statement.setFloat(index, (Float) value);
        } else if (AvroUtils.isSameType(f.schema(), AvroUtils._boolean())) {
            statement.setBoolean(index, (Boolean) value);
        } else if (AvroUtils.isSameType(f.schema(), AvroUtils._short())) {
            statement.setShort(index, (Short) value);
        } else if (AvroUtils.isSameType(f.schema(), AvroUtils._character())) {
            statement.setInt(index, (Character) value);
        } else if (AvroUtils.isSameType(f.schema(), AvroUtils._byte())) {
            statement.setByte(index, (Byte) value);
        } else {
            statement.setObject(index, value);
        }
    }
}

package org.talend.components.jdbc.runtime.writer;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jdbc.runtime.JDBCTemplate;
import org.talend.components.jdbc.runtime.sqlbuilder.JDBCSQLBuilder;
import org.talend.components.jdbc.runtime.type.JDBCMapping;

public class JDBCOutputUpdateOrInsertWriter extends JDBCOutputWriter {

    private String sqlInsert;

    private String sqlUpdate;

    private PreparedStatement statementInsert;

    private PreparedStatement statementUpdate;

    public JDBCOutputUpdateOrInsertWriter(WriteOperation<Result> writeOperation, RuntimeContainer runtime) {
        super(writeOperation, runtime);
    }

    @Override
    public void open(String uId) throws IOException {
        try {
            conn = sink.connect(runtime);

            sqlInsert = JDBCSQLBuilder.getInstance().generateSQL4Insert(properties.tablename.getValue(),
                    properties.main.schema.getValue());
            statementInsert = conn.prepareStatement(sqlInsert);

            sqlUpdate = JDBCSQLBuilder.getInstance().generateSQL4Update(properties.tablename.getValue(),
                    properties.main.schema.getValue());
            statementUpdate = conn.prepareStatement(sqlUpdate);

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void write(Object datum) throws IOException {
        IndexedRecord input = this.getFactory(datum).convertToAvro(datum);

        List<Schema.Field> allFields = input.getSchema().getFields();
        List<Schema.Field> keys = JDBCTemplate.getKeyColumns(allFields);
        List<Schema.Field> values = JDBCTemplate.getValueColumns(allFields);

        try {
            for (Schema.Field value : values) {
                JDBCMapping.setValue(statementUpdate, value, input.get(value.pos()));
            }

            for (Schema.Field key : keys) {
                JDBCMapping.setValue(statementUpdate, key, input.get(key.pos()));
            }

            int count = statementUpdate.executeUpdate();
            boolean noDataUpdate = (count == 0);

            if (noDataUpdate) {
                for (Schema.Field field : allFields) {
                    JDBCMapping.setValue(statementInsert, field, input.get(field.pos()));
                }

                execute(input, statementInsert);
            } else {

            }
        } catch (SQLException e) {
            if (useBatch) {
                throw new RuntimeException(e);
            }

            handleReject(input, e);
        }
    }

}

package org.talend.components.jdbc.runtime.writer;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

public class JDBCOutputInsertOrUpdateWriter extends JDBCOutputWriter {

    private String sqlQuery;

    private String sqlInsert;

    private String sqlUpdate;

    private PreparedStatement statementQuery;

    private PreparedStatement statementInsert;

    private PreparedStatement statementUpdate;

    public JDBCOutputInsertOrUpdateWriter(WriteOperation<Result> writeOperation, RuntimeContainer runtime) {
        super(writeOperation, runtime);
    }

    @Override
    public void open(String uId) throws IOException {
        try {
            conn = sink.connect(runtime);

            sqlQuery = JDBCSQLBuilder.getInstance().generateQuerySQL4InsertOrUpdate(properties.tablename.getValue(),
                    properties.main.schema.getValue());
            statementQuery = conn.prepareStatement(sqlQuery);

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
            for (Schema.Field key : keys) {
                JDBCMapping.setValue(statementQuery, key, input.get(key.pos()));
            }

            ResultSet resultSet = statementQuery.executeQuery();

            boolean dataExists = false;
            while (resultSet.next()) {
                dataExists = resultSet.getInt(1) > 0;
            }

            if (dataExists) {// do update
                for (Schema.Field value : values) {
                    JDBCMapping.setValue(statementUpdate, value, input.get(value.pos()));
                }

                for (Schema.Field key : keys) {
                    JDBCMapping.setValue(statementUpdate, key, input.get(key.pos()));
                }

                execute(input, statementUpdate);
            } else {// do insert
                for (Schema.Field field : allFields) {
                    JDBCMapping.setValue(statementInsert, field, input.get(field.pos()));
                }

                execute(input, statementInsert);
            }
        } catch (SQLException e) {
            if (dieOnError || useBatch) {
                throw new RuntimeException(e);
            }

            handleReject(input, e);
        }

        try {
            executeCommit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}

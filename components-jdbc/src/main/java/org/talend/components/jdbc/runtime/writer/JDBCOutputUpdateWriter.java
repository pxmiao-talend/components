package org.talend.components.jdbc.runtime.writer;

import java.io.IOException;
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

public class JDBCOutputUpdateWriter extends JDBCOutputWriter {

    private String sql;

    public JDBCOutputUpdateWriter(WriteOperation<Result> writeOperation, RuntimeContainer runtime) {
        super(writeOperation, runtime);
    }

    @Override
    public void open(String uId) throws IOException {
        try {
            conn = sink.connect(runtime);
            sql = JDBCSQLBuilder.getInstance().generateSQL4Update(properties.tablename.getValue(),
                    properties.main.schema.getValue());
            statement = conn.prepareStatement(sql);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void write(Object datum) throws IOException {
        super.write(datum);

        IndexedRecord input = this.getFactory(datum).convertToAvro(datum);

        List<Schema.Field> keys = JDBCTemplate.getKeyColumns(input.getSchema().getFields());
        List<Schema.Field> values = JDBCTemplate.getValueColumns(input.getSchema().getFields());

        try {
            for (Schema.Field value : values) {
                JDBCMapping.setValue(statement, value, input.get(value.pos()));
            }

            for (Schema.Field key : keys) {
                JDBCMapping.setValue(statement, key, input.get(key.pos()));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            execute(input, statement);
            executeCommit();
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

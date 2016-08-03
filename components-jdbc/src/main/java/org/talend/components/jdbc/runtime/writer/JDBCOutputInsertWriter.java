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
import org.talend.components.jdbc.runtime.sqlbuilder.JDBCSQLBuilder;
import org.talend.components.jdbc.runtime.type.JDBCMapping;

public class JDBCOutputInsertWriter extends JDBCOutputWriter {

    private String sql;

    private PreparedStatement statement;

    public JDBCOutputInsertWriter(WriteOperation<Result> writeOperation, RuntimeContainer runtime) {
        super(writeOperation, runtime);
    }

    @Override
    public void open(String uId) throws IOException {
        try {
            conn = sink.connect(runtime);
            sql = JDBCSQLBuilder.getInstance().generateSQL4Insert(properties.tablename.getValue(),
                    properties.main.schema.getValue());
            statement = conn.prepareStatement(sql);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }

        useBatch = properties.useBatch.getValue();
        batchSize = properties.batchSize.getValue();
        commitEvery = properties.commitEvery.getValue();
    }

    @Override
    public void write(Object datum) throws IOException {
        IndexedRecord input = this.getFactory(datum).convertToAvro(datum);

        List<Schema.Field> fields = input.getSchema().getFields();

        try {
            for (Schema.Field f : fields) {
                JDBCMapping.setValue(statement, f, input.get(f.pos()));
            }

            execute(input, statement);
            executeCommit();
        } catch (SQLException e) {
            if (useBatch) {
                throw new RuntimeException(e);
            }

            handleReject(input, e);
        }
    }

}

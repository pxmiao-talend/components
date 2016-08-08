package org.talend.components.cassandra.runtime_3_0;

import com.datastax.driver.core.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.cassandra.output.TCassandraOutputProperties;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class CassandraSink extends CassandraSourceOrSink implements Sink {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraSink.class);

    public CassandraSink() {
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        ValidationResult validate = super.validate(container);
        if (validate.getStatus() != ValidationResult.Result.ERROR) {
            try {
                Session session = connect(container);
                CQLManager cqlManager = new CQLManager((TCassandraOutputProperties) properties);
                List<String> kscqls = cqlManager.getKSCQLs();
                for (String kscql : kscqls) {
                    session.execute(kscql);
                }
                List<String> tableCQLs = cqlManager.getTableCQLs();
                for (String tableCQL : tableCQLs) {
                    session.execute(tableCQL);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return validate;
    }

    @Override
    public WriteOperation<?> createWriteOperation() {
        return new CassandraWriteOperation(this);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(((ComponentProperties) properties).toSerialized());
    }

    private void readObject(ObjectInputStream in) throws IOException {
        properties = (TCassandraOutputProperties) Properties.Helper.fromSerializedPersistent(in
                        .readUTF(),
                TCassandraOutputProperties.class).object;
    }
}

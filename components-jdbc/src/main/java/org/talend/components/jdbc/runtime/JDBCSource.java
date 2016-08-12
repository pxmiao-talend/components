package org.talend.components.jdbc.runtime;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jdbc.runtime.reader.JDBCInputReader;
import org.talend.components.jdbc.tjdbcinput.TJDBCInputProperties;

public class JDBCSource extends JDBCSourceOrSink implements BoundedSource {

    private static final long serialVersionUID = -9111994542816954024L;

    private static final Logger LOG = LoggerFactory.getLogger(JDBCSource.class);

    @Override
    public BoundedReader createReader(RuntimeContainer adaptor) {
        if (properties instanceof TJDBCInputProperties) {
            JDBCInputReader reader = new JDBCInputReader(adaptor, this, (TJDBCInputProperties) properties);
            return reader;
        }
        return null;
    }

    @Override
    public List<? extends BoundedSource> splitIntoBundles(long desiredBundleSizeBytes, RuntimeContainer adaptor)
            throws Exception {
        List<BoundedSource> list = new ArrayList<>();
        list.add(this);
        return list;
    }

    @Override
    public long getEstimatedSizeBytes(RuntimeContainer adaptor) {
        return 0;
    }

    @Override
    public boolean producesSortedKeys(RuntimeContainer adaptor) {
        return false;
    }

}

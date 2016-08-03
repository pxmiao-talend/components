// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.jdbc.runtime.reader;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jdbc.runtime.JDBCSource;
import org.talend.components.jdbc.runtime.JDBCTemplate;
import org.talend.components.jdbc.runtime.type.JDBCResultSetIndexedRecordConverter;
import org.talend.components.jdbc.tjdbcinput.TJDBCInputProperties;

public class JDBCInputReader extends AbstractBoundedReader<IndexedRecord> {

    private static final Logger LOG = LoggerFactory.getLogger(JDBCInputReader.class);

    protected TJDBCInputProperties properties;

    protected RuntimeContainer adaptor;

    protected Connection conn;

    protected ResultSet resultSet;

    private transient JDBCResultSetIndexedRecordConverter factory;

    private transient Schema querySchema;

    public JDBCInputReader(RuntimeContainer adaptor, JDBCSource source, TJDBCInputProperties props) {
        super(source);
        this.adaptor = adaptor;
        this.properties = props;
    }

    private Schema getSchema() throws IOException {
        if (null == querySchema) {
            querySchema = new Schema.Parser().parse(properties.schema.schema.getStringValue());
        }
        return querySchema;
    }

    private JDBCResultSetIndexedRecordConverter getFactory() throws IOException {
        if (null == factory) {
            factory = new JDBCResultSetIndexedRecordConverter();
            factory.setSchema(getSchema());
        }
        return factory;
    }

    @Override
    public boolean start() throws IOException {
        try {
            conn = JDBCTemplate.connect(properties.getJDBCConnectionModule());
            Statement statement = conn.createStatement();
            resultSet = statement.executeQuery(properties.sql.getValue());
            return resultSet.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean advance() throws IOException {
        try {
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        try {
            return getFactory().convertToAvro(resultSet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public org.joda.time.Instant getCurrentTimestamp() throws NoSuchElementException {
        return null;
    }

    @Override
    public void close() throws IOException {
        try {
            resultSet.close();
            conn.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public Double getFractionConsumed() {
        return null;
    }

    @Override
    public BoundedSource splitAtFraction(double fraction) {
        return null;
    }

    @Override
    public Map<String, Object> getReturnValues() {
        // TODO Auto-generated method stub
        return null;
    }

}

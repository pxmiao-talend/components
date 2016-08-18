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
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jdbc.runtime.JDBCSource;
import org.talend.components.jdbc.runtime.type.JDBCAvroRegistry;
import org.talend.components.jdbc.runtime.type.JDBCResultSetIndexedRecordConverter;
import org.talend.components.jdbc.tjdbcinput.TJDBCInputProperties;
import org.talend.daikon.avro.AvroUtils;

public class JDBCInputReader extends AbstractBoundedReader<IndexedRecord> {

    protected TJDBCInputProperties properties;

    protected RuntimeContainer container;

    protected Connection conn;

    protected ResultSet resultSet;

    private transient JDBCResultSetIndexedRecordConverter factory;

    private transient Schema querySchema;

    private JDBCSource source;

    private Statement statement;

    private Result result;

    public JDBCInputReader(RuntimeContainer container, JDBCSource source, TJDBCInputProperties props) {
        super(source);
        this.container = container;
        this.properties = props;
        source = (JDBCSource) getCurrentSource();
    }

    private Schema getSchema() throws IOException, SQLException {
        if (querySchema == null) {
            querySchema = properties.schema.schema.getValue();
            if (AvroUtils.isIncludeAllFields(querySchema)) {
                querySchema = JDBCAvroRegistry.get().inferSchema(resultSet.getMetaData());
            }
        }
        return querySchema;
    }

    private JDBCResultSetIndexedRecordConverter getFactory() throws IOException, SQLException {
        if (null == factory) {
            factory = new JDBCResultSetIndexedRecordConverter();
            factory.setSchema(getSchema());
        }
        return factory;
    }

    @Override
    public boolean start() throws IOException {
        result = new Result();
        try {
            conn = source.getConnection(container);
            statement = conn.createStatement();

            if (properties.useCursor.getValue()) {
                statement.setFetchSize(properties.cursor.getValue());
            }

            resultSet = statement.executeQuery(properties.sql.getValue());
            return resultSet.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean advance() throws IOException {
        result.totalCount++;
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            resultSet.close();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, Object> getReturnValues() {
        return result.toMap();
    }

}

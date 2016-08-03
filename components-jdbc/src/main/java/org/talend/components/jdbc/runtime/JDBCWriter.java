// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.jdbc.runtime;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jdbc.JDBCConnectionInfoProvider;
import org.talend.components.jdbc.ReferAnotherComponent;

public class JDBCWriter implements Writer<Result> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JDBCWriter.class);

    private WriteOperation<Result> writeOperation;

    private Connection conn;

    JDBCSink sink;

    JDBCConnectionInfoProvider properties;

    RuntimeContainer runtime;

    java.sql.PreparedStatement statment;

    private Result result;

    JDBCWriter(WriteOperation<Result> writeOperation, RuntimeContainer runtime) {
        this.writeOperation = writeOperation;
        this.runtime = runtime;
        sink = (JDBCSink) writeOperation.getSink();
        properties = sink.properties;
    }

    @Override
    public void open(String uId) throws IOException {
        try {
            conn = sink.connect(runtime);
            String sql = constructSQL();
            conn.prepareStatement(sql);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private String constructSQL() {
        return null;
    }

    @Override
    public void write(Object datum) throws IOException {

    }

    @Override
    public Result close() throws IOException {
        String refComponentId = ((ReferAnotherComponent) properties).getReferencedComponentId();
        if (refComponentId == null) {
            try {
                conn.commit();
                conn.close();
            } catch (SQLException e) {
                throw new IOException(e);
            }
        }

        return result;
    }

    @Override
    public WriteOperation<Result> getWriteOperation() {
        return writeOperation;
    }

}

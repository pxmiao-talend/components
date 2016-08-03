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
package org.talend.components.jdbc.runtime;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jdbc.ComponentConstants;
import org.talend.components.jdbc.JDBCConnectionInfoProvider;
import org.talend.components.jdbc.ReferAnotherComponent;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.properties.ValidationResult;

public class JDBCSourceOrSink implements SourceOrSink {

    private static final long serialVersionUID = -1730391293657968628L;

    private transient static final Logger LOG = LoggerFactory.getLogger(JDBCSourceOrSink.class);

    protected JDBCConnectionInfoProvider properties;

    @Override
    public void initialize(RuntimeContainer runtime, ComponentProperties properties) {
        this.properties = (JDBCConnectionInfoProvider) properties;
    }

    private static ValidationResult fillValidationResult(ValidationResult vr, Exception ex) {
        if (vr == null) {
            return null;
        }
        vr.setMessage(ex.getMessage());
        vr.setStatus(ValidationResult.Result.ERROR);
        return vr;
    }

    @Override
    public ValidationResult validate(RuntimeContainer runtime) {
        ValidationResult vr = new ValidationResult();
        Connection conn = null;
        try {
            conn = connect(runtime);
        } catch (Exception ex) {
            fillValidationResult(vr, ex);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return vr;
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer runtime) throws IOException {
        List<NamedThing> result = new ArrayList<>();
        Connection conn = null;
        try {
            conn = connect(runtime);
            DatabaseMetaData metadata = conn.getMetaData();
            ResultSet resultset = metadata.getTables(null, null, null, new String[] { "TABLE" });
            while (resultset.next()) {
                String tablename = resultset.getString("TABLE_NAME");
                result.add(new SimpleNamedThing(tablename, tablename));
            }
        } catch (Exception e) {
            throw new ComponentException(e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer runtime, String tableName) throws IOException {
        Connection conn = null;
        try {
            conn = connect(runtime);
            DatabaseMetaData metadata = conn.getMetaData();
            ResultSet resultset = metadata.getColumns(null, null, tableName, null);
            return JDBCAvroRegistry.get().inferSchema(resultset);
        } catch (Exception e) {
            throw new ComponentException(e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    protected Connection connect(RuntimeContainer runtime) throws ClassNotFoundException, SQLException {
        if (properties instanceof ReferAnotherComponent) {
            String refComponentId = ((ReferAnotherComponent) properties).getReferencedComponentId();
            // using another component's connection
            if (refComponentId != null && runtime != null) {
                Object existedConn = runtime.getComponentData(refComponentId, ComponentConstants.CONNECTION_KEY);
                if (existedConn == null) {
                    throw new RuntimeException("Referenced component: " + refComponentId + " not connected");
                }
                return (Connection) existedConn;
            } else {
                return createConnection();
            }
        } else {// connection component
            Connection conn = createConnection();
            if (runtime != null) {
                runtime.setComponentData(runtime.getCurrentComponentId(), ComponentConstants.CONNECTION_KEY, conn);
            }
            return conn;
        }
    }

    private Connection createConnection() throws ClassNotFoundException, SQLException {
        JDBCConnectionModule connectionInfo = properties.getJDBCConnectionModule();
        java.lang.Class.forName(connectionInfo.driverClass.getValue());
        Connection conn = java.sql.DriverManager.getConnection(connectionInfo.jdbcUrl.getValue(),
                connectionInfo.userPassword.userId.getValue(), connectionInfo.userPassword.password.getValue());
        return conn;
    }

}

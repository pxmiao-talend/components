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
import org.talend.components.jdbc.DBConnectionProperties;
import org.talend.components.jdbc.DBProvideConnectionProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.properties.ValidationResult;

public abstract class DBSourceOrSink implements SourceOrSink {

    private transient static final Logger LOG = LoggerFactory.getLogger(DBSourceOrSink.class);

    protected DBProvideConnectionProperties properties;

    @Override
    public void initialize(RuntimeContainer adaptor, ComponentProperties properties) {
        this.properties = (DBProvideConnectionProperties) properties;
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
    public ValidationResult validate(RuntimeContainer adaptor) {
        ValidationResult vr = new ValidationResult();
        Connection conn = null;
        try {
            DBConnectionProperties dbprops = properties.getConnectionProperties();
            conn = getDBTemplate().connect(dbprops);
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

    abstract protected DBTemplate getDBTemplate();

    abstract protected AvroRegistry getAvroRegistry();

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer adaptor) throws IOException {
        List<NamedThing> result = new ArrayList<>();
        Connection conn = null;
        try {
            DBConnectionProperties dbprops = properties.getConnectionProperties();
            conn = getDBTemplate().connect(dbprops);
            DatabaseMetaData metadata = conn.getMetaData();
            ResultSet resultset = metadata.getTables(null, dbprops.dbschema.getStringValue(), null, new String[] { "TABLE" });
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
    public Schema getEndpointSchema(RuntimeContainer adaptor, String schemaName) throws IOException {
        Connection conn = null;

        try {
            DBConnectionProperties dbprops = properties.getConnectionProperties();
            conn = getDBTemplate().connect(dbprops);
            DatabaseMetaData metadata = conn.getMetaData();
            ResultSet resultset = metadata.getColumns(null, dbprops.dbschema.getStringValue(), schemaName, null);
            return getAvroRegistry().inferSchema(resultset);
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

}

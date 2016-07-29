package org.talend.components.jdbc.runtime;

import java.sql.Connection;

import org.talend.components.jdbc.DBConnectionProperties;

public class JDBCTemplate implements DBTemplate {

    @Override
    public Connection connect(DBConnectionProperties properties) throws Exception {
        String host = properties.host.getStringValue();
        String port = properties.port.getStringValue();
        String dbname = properties.database.getStringValue();
        String parameters = properties.jdbcparameter.getStringValue();

        String username = properties.userPassword.userId.getStringValue();
        String password = properties.userPassword.password.getStringValue();

        boolean autocommit = "true".equalsIgnoreCase(properties.autocommit.getStringValue());

        StringBuilder url = new StringBuilder();
        url.append("jdbc:jdbc:thin:@").append(host).append(":").append(port).append(":").append(dbname);
        if (parameters != null) {
            url.append("?").append(parameters);
        }

        java.lang.Class.forName("jdbc.jdbc.JDBCDriver");

        Connection conn = java.sql.DriverManager.getConnection(url.toString(), username, password);
        conn.setAutoCommit(autocommit);

        return conn;
    }

}

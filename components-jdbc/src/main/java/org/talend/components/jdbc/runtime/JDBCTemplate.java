package org.talend.components.jdbc.runtime;

import java.sql.Connection;
import java.sql.SQLException;

import org.talend.components.jdbc.module.JDBCConnectionModule;

public class JDBCTemplate {

    public static Connection connect(JDBCConnectionModule properties) throws ClassNotFoundException, SQLException {
        java.lang.Class.forName(properties.driverClass.getValue());
        Connection conn = java.sql.DriverManager.getConnection(properties.jdbcUrl.getValue(),
                properties.userPassword.userId.getValue(), properties.userPassword.password.getValue());
        return conn;
    }

}

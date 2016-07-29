package org.talend.components.jdbc.runtime;

import java.sql.Connection;

import org.talend.components.jdbc.DBConnectionProperties;

interface DBTemplate {

    Connection connect(DBConnectionProperties properties) throws Exception;

}

package org.talend.components.jdbc;

import org.talend.components.jdbc.module.DataSourceModule;
import org.talend.components.jdbc.module.JDBCConnectionModule;

public interface DBProvideConnectionProperties {

    JDBCConnectionModule getJDBCConnectionModule();

    DataSourceModule getDataSourceModule();

}

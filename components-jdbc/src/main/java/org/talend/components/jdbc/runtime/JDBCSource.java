package org.talend.components.jdbc.runtime;

import org.talend.daikon.avro.AvroRegistry;


public class JDBCSource extends DBSource {

    JDBCTemplate template = new JDBCTemplate();//no state object
    
    @Override
    protected DBTemplate getDBTemplate() {
        return template;
    }

    @Override
    protected AvroRegistry getAvroRegistry() {
        return JDBCAvroRegistry.get();
    }

}

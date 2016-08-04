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
package org.talend.components.dropbox.avro;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;

/**
 * Dropbox {@link IndexedRecord}
 */
public class DropboxIndexedRecord implements IndexedRecord {
    
    private final Schema schema;
    
    private List<Object> values;
    
    private int size;
    
    public DropboxIndexedRecord(Schema schema) {
        this.schema = schema;
        size = schema.getFixedSize();
        values = new ArrayList<Object>(size);
    }

    /**
     * Returns schema of this {@link IndexedRecord}
     */
    @Override
    public Schema getSchema() {
        return schema;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void put(int index, Object v) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        values.add(index, v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        Object value = values.get(index);
        return value;
    }

}

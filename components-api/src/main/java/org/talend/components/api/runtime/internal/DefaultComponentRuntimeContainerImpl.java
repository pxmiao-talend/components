package org.talend.components.api.runtime.internal;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.talend.components.api.runtime.ComponentRuntimeContainer;
import org.talend.components.api.schema.ComponentSchemaElement;

/**
 *
 */
public class DefaultComponentRuntimeContainerImpl implements ComponentRuntimeContainer {

    private Map<String, Object> globalMap = new HashMap();

    public Map<String, Object> getGlobalMap() {
        return globalMap;
    }

    public String formatDate(Date date, String pattern) {
        return null;
    }

    public ComponentSchemaElement[] getDynamicElements(Object dynamic) {
        return null;
    }

    public void setDynamicElements(ComponentSchemaElement[] elements) {
    }

    public Object getDynamicValue(Object dynamic, String fieldName) {
        return null;
    }

}
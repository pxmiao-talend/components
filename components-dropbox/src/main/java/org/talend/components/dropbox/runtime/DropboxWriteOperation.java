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
package org.talend.components.dropbox.runtime;

import java.util.Collections;
import java.util.Map;

import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.container.RuntimeContainer;

public class DropboxWriteOperation implements WriteOperation<Result> {

    private static final long serialVersionUID = -5508699563220602554L;

    /**
     * {@link Sink} instance to which this {@link WriteOperation} writes
     */
    private final DropboxPutSink sink;

    /**
     * Constructor sets {@link DropboxPutSink} instance
     *
     * @param sink {@link DropboxPutSink} instance
     */
    public DropboxWriteOperation(DropboxPutSink sink) {
        this.sink = sink;
    }

    /**
     * Does nothing
     *
     * @param container {@link RuntimeContainer} instance
     */
    @Override
    public void initialize(RuntimeContainer adaptor) {
        // nothing to be done here
    }

    /**
     * Returns empty map, because Dropbox Put component doesn't return any return/after variables
     * except ERROR_MESSAGE 
     *
     * @param results {@link Iterable} of output results
     * @param container {@link RuntimeContainer} instance
     * @return empty map
     */
    @Override
    public Map<String, Object> finalize(Iterable<Result> results, RuntimeContainer adaptor) {
        return Collections.EMPTY_MAP;
    }

    @Override
    public Writer<Result> createWriter(RuntimeContainer adaptor) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Returns the Sink that this write operation writes to.
     *
     * @return the Sink
     */
    public DropboxPutSink getSink() {
        return this.sink;
    }

}

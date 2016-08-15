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
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.dropbox.runtime.writer.DropboxPutFileWriter;
import org.talend.components.dropbox.runtime.writer.DropboxPutStringWriter;
import org.talend.components.dropbox.runtime.writer.DropboxPutWriter;
import org.talend.components.dropbox.tdropboxput.ContentType;

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

    /**
     * Returns instance of {@link DropboxPutWriter}, which can upload files both from local file 
     * and from String/byte[] record column 
     */
    @Override
    public DropboxPutWriter createWriter(RuntimeContainer adaptor) {
        ContentType contentType = sink.getContentType();
        switch (contentType) {
        case STRING:
            return new DropboxPutStringWriter(this);
        case LOCAL_FILE:
            return new DropboxPutFileWriter(this);
        case BYTE_ARRAY:
            return null;
        default:
            throw new NullPointerException("Content type wasn't specified");
        }
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

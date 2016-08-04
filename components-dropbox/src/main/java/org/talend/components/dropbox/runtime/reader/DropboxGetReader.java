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
package org.talend.components.dropbox.runtime.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avro.generic.IndexedRecord;
import org.joda.time.Instant;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.dropbox.avro.DropboxIndexedRecord;
import org.talend.components.dropbox.runtime.DropboxGetSource;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.FileMetadata;

/**
 * Downloads one file from Dropbox server, saves it on filesystem if required.
 * Provides {@link IndexedRecord} with file name and its content
 */
public class DropboxGetReader implements Reader<IndexedRecord> {

    /**
     * {@link Source} from which this {@link Reader} reads
     */
    private DropboxGetSource source;

    /**
     * Current {@link IndexedRecord}
     */
    private DropboxIndexedRecord currentRecord;

    /**
     * Specifies whether {@link DropboxGetReader#start()} was called
     */
    private boolean started;

    /**
     * Constructor sets {@link DropboxGetSource} 
     * 
     * @param source {@link DropboxGetSource} instance
     */
    public DropboxGetReader(DropboxGetSource source) {
        this.source = source;
    }

    /**
     * Downloads file from Dropbox, saves it on filesystem and created {@link IndexedRecord}
     * 
     * @return true, if download was successful
     */
    @Override
    public boolean start() throws IOException {
        DbxClientV2 client = source.getClient();
        DbxUserFilesRequests filesClient = client.files();
        String path = source.getPath();
        try {
            DbxDownloader<FileMetadata> downloader = filesClient.download(path);
            String fileName = downloader.getResult().getName();
            InputStream content = null;
            if (source.isSaveAsFile()) {
                String saveTo = source.getSaveTo();
                File fileToSave = new File(saveTo);
                if (!fileToSave.getParentFile().exists()) {
                    fileToSave.getParentFile().mkdirs();
                }
                try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
                    downloader.download(fos);
                }
                content = new FileInputStream(fileToSave);
            } else {
                content = downloader.getInputStream();
            }
            currentRecord = new DropboxIndexedRecord(source.getSchema());
            currentRecord.put(0, fileName);
            currentRecord.put(1, content);
        } catch (DbxException e) {
            throw new IOException(e);
        }
        started = true;
        return started;
    }

    /**
     * Always returns false, because Dropbox Get component returns only one record per run
     * 
     * @return false
     */
    @Override
    public boolean advance() {
        return false;
    }

    /**
     * Returns current {@link IndexedRecord}
     * 
     * @return current {@link IndexedRecord}
     */
    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        if (!started) {
            throw new NoSuchElementException("Reader wasn't started");
        }
        return currentRecord;
    }

    /**
     * Returns null
     * Currently this method is not supported
     * 
     * @return null
     */
    @Override
    public Instant getCurrentTimestamp() {
        return null;
    }

    /**
     * Does nothing, because Dropbox client releases resources by itself
     */
    @Override
    public void close() {
        // Nothing to be done here
    }

    /**
     * Returns {@link DropboxGetSource}, from which this component reads
     * 
     * @return {@link DropboxGetSource}
     */
    @Override
    public DropboxGetSource getCurrentSource() {
        return source;
    }

    /**
     * Returns empty map, because this Dropbox Get component doesn't provide any return properties
     * except ERROR_MESSAGE, which is computed outside component in generated code
     */
    @Override
    public Map<String, Object> getReturnValues() {
        return Collections.EMPTY_MAP;
    }

}

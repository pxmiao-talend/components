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
     * {@link InputStream} from which file is read
     */
    private InputStream inputStream;

    /**
     * Name of file
     */
    private String fileName;

    /**
     * Size of chunk in bytes. Default value is 8kB
     */
    private int chunkSize = 8192;

    /**
     * Constructor sets {@link DropboxGetSource} 
     * 
     * @param source {@link DropboxGetSource} instance
     */
    public DropboxGetReader(DropboxGetSource source) {
        this.source = source;
    }

    /**
     * Obtains {@link InputStream} from which file will be read.
     * Optionally, saves file on filesystem depending on Save As File property value
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
            fileName = downloader.getResult().getName();
            if (source.isSaveAsFile()) {
                String saveTo = source.getSaveTo();
                File fileToSave = new File(saveTo);
                if (!fileToSave.getParentFile().exists()) {
                    fileToSave.getParentFile().mkdirs();
                }
                try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
                    downloader.download(fos);
                }
                inputStream = new FileInputStream(fileToSave);
            } else {
                inputStream = downloader.getInputStream();
            }
        } catch (DbxException e) {
            throw new IOException(e);
        }
        started = advance();
        return started;
    }

    /**
     * Reads next chunk in byte array and packs it in next {@link IndexedRecord}
     * 
     * @return true, next chunk was read; false if there is no more data
     * @throws IOException if some I/O error occurs during reading from {@link InputStream}
     */
    @Override
    public boolean advance() throws IOException {
        byte[] bytes = new byte[chunkSize];
        int wasRead = inputStream.read(bytes);
        if (wasRead == 0 || wasRead == -1) {
            return false;
        }
        // trim byte array if it was read less bytes than chunk size
        if (wasRead < chunkSize) {
            byte[] trimmedBytes = new byte[wasRead];
            System.arraycopy(bytes, 0, trimmedBytes, 0, wasRead);
            bytes = trimmedBytes;
        }
        currentRecord = new DropboxIndexedRecord(source.getSchema());
        currentRecord.put(0, fileName);
        currentRecord.put(1, bytes);
        return true;
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
     * @throws IOException if an I/O error occurs during reading from {@link InputStream}
     */
    @Override
    public void close() throws IOException {
        inputStream.close();
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

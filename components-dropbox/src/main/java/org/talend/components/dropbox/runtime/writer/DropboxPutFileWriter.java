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
package org.talend.components.dropbox.runtime.writer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.dropbox.runtime.DropboxWriteOperation;
import org.talend.components.dropbox.tdropboxput.UploadMode;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.UploadBuilder;

/**
 * Dropbox Put component {@link Writer} implementation, which provides writing from local file
 * Full write operation consists of following method calls:
 * {@link Writer#open()} should be called once to initialize this Writer
 * {@link Writer#write(Object)} could be called several times to write all Object/IndexedRecord(s)
 * {@link Writer#close()} should be called once in the end to finish writing and release resources
 */
public class DropboxPutFileWriter extends DropboxPutWriter {

    /**
     * Constructor sets {@link WriteOperation}
     * 
     * @param writeOperation {@link WriteOperation}, part of which this {@link Writer} is
     */
    public DropboxPutFileWriter(DropboxWriteOperation writeOperation) {
        super(writeOperation);
    }

    /**
     * {@inheritDoc}
     * 
     * It uploads local file to dropbox and changes state to closed.
     * It uses local file path and path on Dropbox properties, which are specified by user
     * There are several write modes:
     * {@link UploadMode#RENAME} - renames file if path is already exist
     * {@link UploadMode#REPLACE} - replaces file if path is already exist
     * 
     * {@link DbxException} could be thrown. It is wrapped in {@link IOException}
     * 
     * @param uId unique Id of current {@link Writer}
     * @throws IOException if {@link DbxException} is thrown. It could be thrown in case path conflict was occurred, network connection problems,
     * etc
     */
    @Override
    public void open(String uId) throws IOException {
        super.open(uId);
        String localFilePath = getWriteOperation().getSink().getFilePath();
        File localFile = new File(localFilePath);
        try (FileInputStream fis = new FileInputStream(localFile)) {
            UploadBuilder uploadBuilder = filesClient.uploadBuilder(path).withMode(dbxWriteMode).withAutorename(true);
            uploadBuilder.uploadAndFinish(fis);
        } catch (DbxException e) {
            throw new IOException(e);
        }
        opened = false;
    }

    /**
     * This {@link Writer} is not intended to handle incoming records
     * This method does nothing except throwing {@link IOException} because of closed state
     */
    @Override
    public void write(Object datum) throws IOException {
        if (!opened) {
            throw new IOException("Writer wasn't opened");
        }
    }

}

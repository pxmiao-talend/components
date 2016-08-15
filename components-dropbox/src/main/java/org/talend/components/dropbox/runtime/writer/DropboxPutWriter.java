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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.dropbox.runtime.DropboxWriteOperation;
import org.talend.components.dropbox.tdropboxput.ContentType;
import org.talend.components.dropbox.tdropboxput.UploadMode;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.UploadBuilder;
import com.dropbox.core.v2.files.UploadError;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.files.WriteMode;

public class DropboxPutWriter implements Writer<Result> {

    private static final Logger LOG = LoggerFactory.getLogger(DropboxPutWriter.class);

    /**
     * {@link WriteOperation}, part of which this {@link Writer} is 
     */
    private DropboxWriteOperation writeOperation;

    /**
     * Denotes whether this {@link Writer} is opened
     */
    private boolean opened = false;

    /**
     * Return results
     */
    private Result result;

    /**
     * Defines from which source to write file content.
     * Possible values are: String, byte[] columns or local file
     */
    private final ContentType contentType;

    /**
     * Dropbox writing/upload mode
     */
    private final WriteMode dbxWriteMode;

    /**
     * Path to upload file on Dropbox
     */
    private final String path;

    /**
     * Dropbox files client
     */
    private DbxUserFilesRequests files;

    /**
     * Constructor sets {@link WriteOperation}, Content type, upload mode and 
     * path to which upload file on Dropbox
     * 
     * @param writeOperation {@link WriteOperation}, part of which this {@link Writer} is 
     */
    public DropboxPutWriter(DropboxWriteOperation writeOperation) {
        this.writeOperation = writeOperation;
        contentType = writeOperation.getSink().getContentType();
        path = writeOperation.getSink().getPath();
        dbxWriteMode = writeOperation.getSink().getUploadMode().toWriteMode();
    }

    /**
     * 
     */
    @Override
    public void open(String uId) throws IOException {
        if (opened) {
            LOG.debug("Writer is already opened");
            return;
        }
        result = new Result(uId);

        DbxClientV2 client = writeOperation.getSink().getClient();
        files = client.files();

        if (contentType == ContentType.LOCAL_FILE) {
            uploadLocalFile();
        }

        // read file from filesystem
        opened = true;
    }

    /**
     * Uploads local file to Dropbox.
     * It uses local file path and path on Dropbox properties, which are specified by user
     * There are several write modes:
     * {@link UploadMode#RENAME} - renames file if path is already exist
     * {@link UploadMode#REPLACE} - replaces file if path is already exist
     * 
     *  {@link DbxException} could be thrown. It is wrapped in {@link IOException}
     */
    private void uploadLocalFile() throws IOException {
        String localFilePath = writeOperation.getSink().getFilePath();
        File localFile = new File(localFilePath);
        try (FileInputStream fis = new FileInputStream(localFile)) {
            UploadBuilder uploadBuilder = files.uploadBuilder(path).withMode(dbxWriteMode).withAutorename(true);
            uploadBuilder.uploadAndFinish(fis);
        } catch (DbxException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void write(Object object) throws IOException {
        // TODO Auto-generated method stub
        // collects part of files in case of byte[]
        // stores string
    }

    @Override
    public Result close() throws IOException {
        // TODO Auto-generated method stub
        // uploads file
        return null;
    }

    /**
     * Returns {@link WriteOperation}, part of which this {@link Writer} is
     */
    @Override
    public WriteOperation<Result> getWriteOperation() {
        return writeOperation;
    }

}

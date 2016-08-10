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
package org.talend.components.dropbox;

/**
 * Stores constants used in Dropbox components tests
 */
public final class DropboxTestConstants {

    /**
     * Dropbox access token used for OAuth2 connection
     */
    public static String ACCESS_TOKEN = "YKEcGph8hWAAAAAAAAAAFDgTzmtSANkKgyYvARYpDHQE4j9cIhxAMWYRxGihjqeJ";

    /**
     * Path to file, which can be downloaded from Dropbox server
     */
    public static String DOWNLOAD_FILE = "/TestFile.txt";

    /**
     * Path to file, which will be uploaded on Dropbox server
     */
    public static String UPLOAD_FILE = "/UploadFile.txt";

    /**
     * Path where to save downloaded file
     */
    public static String PATH_TO_SAVE = "d:/test/TestFile.txt";

    /**
     * Path to file, which should be uploaded
     */
    public static String PATH_TO_UPLOAD = "d:/test/UploadFile.txt";
}

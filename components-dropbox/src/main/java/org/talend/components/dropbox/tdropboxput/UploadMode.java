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
package org.talend.components.dropbox.tdropboxput;

import com.dropbox.core.v2.files.WriteMode;

/**
 * Dropbox Put component Upload Modes
 * It corresponds to Dropbox API {@link WriteMode} modes
 * Provides conversion method from {@link UploadMode} to {@link WriteMode}
 */
public enum UploadMode {
    RENAME,
    REPLACE,
    UPDATE_REVISION;
}

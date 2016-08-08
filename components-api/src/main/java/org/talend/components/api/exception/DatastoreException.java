// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.api.exception;

import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.exception.error.ErrorCode;
import org.talend.daikon.properties.ValidationResult;

public class DatastoreException extends TalendRuntimeException {

    private static final long serialVersionUID = -84662653622272070L;

    private ValidationResult validationResult;

    public DatastoreException(ErrorCode code) {
        super(code);
    }

    public DatastoreException(Throwable cause) {
        super(CommonErrorCodes.UNEXPECTED_EXCEPTION, cause);
    }

    public DatastoreException(ValidationResult vr) {
        super(CommonErrorCodes.UNEXPECTED_EXCEPTION);
        setValidationResult(vr);
    }

    public DatastoreException(ErrorCode code, Throwable cause) {
        super(code, cause);
    }

    public DatastoreException(ErrorCode code, ExceptionContext context) {
        super(code, context);
    }

    public DatastoreException(ErrorCode code, Throwable cause, ExceptionContext context) {
        super(code, cause, context);
    }

    public ValidationResult getValidationResult() {
        return validationResult;
    }

    public void setValidationResult(ValidationResult validationResult) {
        this.validationResult = validationResult;
    }
}

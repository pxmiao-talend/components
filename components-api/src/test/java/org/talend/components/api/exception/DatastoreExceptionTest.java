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

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.exception.error.DatastoresApiErrorCode;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.properties.ValidationResult;

public class DatastoreExceptionTest {

    @Test
    public void test() {
        Logger LOG = LoggerFactory.getLogger(DatastoreExceptionTest.class);

        LOG.warn("____________________");
        LOG.warn("This unit test is testing errors, so not mind the display if the test is OK.");
        DatastoreException exception = new DatastoreException(DatastoresApiErrorCode.WRONG_DATASTORE_NAME);
        assertEquals(DatastoresApiErrorCode.WRONG_DATASTORE_NAME, exception.getCode());

        exception = new DatastoreException(CommonErrorCodes.MISSING_I18N_TRANSLATOR, ExceptionContext.build());
        assertEquals(CommonErrorCodes.MISSING_I18N_TRANSLATOR, exception.getCode());

        exception = new DatastoreException(DatastoresApiErrorCode.COMPUTE_DEPENDENCIES_FAILED, new Throwable("message"),
                ExceptionContext.build());
        assertEquals(DatastoresApiErrorCode.COMPUTE_DEPENDENCIES_FAILED, exception.getCode());
        assertEquals("message", exception.getCause().getMessage());

        ValidationResult vr = new ValidationResult();
        vr.setMessage("vr");
        exception = new DatastoreException(vr);
        assertTrue(vr == exception.getValidationResult());

        ValidationResult vr2 = new ValidationResult();
        vr2.setMessage("vr2");
        exception.setValidationResult(vr2);
        assertTrue(vr2 == exception.getValidationResult());

        LOG.warn("End of test.");
        LOG.warn("____________________");
    }

}

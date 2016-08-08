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
import org.talend.components.api.exception.error.DatasetsApiErrorCode;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.properties.ValidationResult;

public class DatasetExceptionTest {

    @Test
    public void test() {
        Logger LOG = LoggerFactory.getLogger(DatasetExceptionTest.class);

        LOG.warn("____________________");
        LOG.warn("This unit test is testing errors, so not mind the display if the test is OK.");
        DatasetException exception = new DatasetException(DatasetsApiErrorCode.WRONG_DATASET_NAME);
        assertEquals(DatasetsApiErrorCode.WRONG_DATASET_NAME, exception.getCode());

        exception = new DatasetException(CommonErrorCodes.MISSING_I18N_TRANSLATOR, ExceptionContext.build());
        assertEquals(CommonErrorCodes.MISSING_I18N_TRANSLATOR, exception.getCode());

        exception = new DatasetException(DatasetsApiErrorCode.COMPUTE_DEPENDENCIES_FAILED, new Throwable("message"),
                ExceptionContext.build());
        assertEquals(DatasetsApiErrorCode.COMPUTE_DEPENDENCIES_FAILED, exception.getCode());
        assertEquals("message", exception.getCause().getMessage());

        ValidationResult vr = new ValidationResult();
        vr.setMessage("vr");
        exception = new DatasetException(vr);
        assertTrue(vr == exception.getValidationResult());

        ValidationResult vr2 = new ValidationResult();
        vr2.setMessage("vr2");
        exception.setValidationResult(vr2);
        assertTrue(vr2 == exception.getValidationResult());

        LOG.warn("End of test.");
        LOG.warn("____________________");
    }

}

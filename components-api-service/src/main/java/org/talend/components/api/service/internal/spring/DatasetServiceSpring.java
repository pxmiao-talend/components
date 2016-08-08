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
package org.talend.components.api.service.internal.spring;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.talend.components.api.component.DatasetDefinition;
import org.talend.components.api.component.DatasetImageType;
import org.talend.components.api.exception.DatasetException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.DatasetService;
import org.talend.components.api.service.internal.DatasetRegistry;
import org.talend.components.api.service.internal.DatasetServiceImpl;
import org.talend.daikon.exception.error.CommonErrorCodes;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * This is a spring only class that is instantiated by the spring framework. It delegates all its calls to the
 * DatasetServiceImpl delegate create in it's constructor. This delegate uses a Dataset registry implementation specific
 * to spring.
 */

@RestController
@Api(value = "dataset", basePath = DatasetServiceSpring.BASE_PATH, description = "Dataset services")
@Service
public class DatasetServiceSpring implements DatasetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetServiceSpring.class);

    public static final String BASE_PATH = "/dataset"; //$NON-NLS-1$

    private DatasetService datasetServiceDelegate;

    @Autowired
    public DatasetServiceSpring(final ApplicationContext context) {
        this.datasetServiceDelegate = new DatasetServiceImpl(new DatasetRegistry() {

            @Override
            public Map<String, DatasetDefinition> getDatasets() {
                Map<String, DatasetDefinition> compDefs = context.getBeansOfType(DatasetDefinition.class);
                return compDefs;
            }

        });
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/properties/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ComponentProperties getComponentProperties(
            @PathVariable(value = "name") @ApiParam(name = "name", value = "Name of the dataset") String name) {
        return datasetServiceDelegate.getComponentProperties(name);
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/components/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String[] getComponents(
            @PathVariable(value = "name") @ApiParam(name = "name", value = "Name of the dataset") String name) {
        return datasetServiceDelegate.getComponents(name);
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/validate/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Object> validate(@PathVariable(value = "name") @ApiParam(name = "name", value = "Name of the dataset") String name) {
        return datasetServiceDelegate.validate(name);
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/jsonSchema/{name}", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public @ResponseBody String getJSONSchema(
            @PathVariable(value = "name") @ApiParam(name = "name", value = "Name of the dataset") String name) {
        return datasetServiceDelegate.getJSONSchema(name);
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/definition/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody DatasetDefinition getDatasetDefinition(
            @PathVariable(value = "name") @ApiParam(name = "name", value = "Name of the dataset") String name) {
        return datasetServiceDelegate.getDatasetDefinition(name);
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/possibleDatasets", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DatasetDefinition> getPossibleDatasets(
            @ApiParam(name = "properties", value = "Dataset properties") @RequestBody ComponentProperties... properties)
            throws Throwable {
        return datasetServiceDelegate.getPossibleDatasets(properties);
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/names", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Set<String> getAllDatasetNames() {
        return datasetServiceDelegate.getAllDatasetNames();
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/definitions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Set<DatasetDefinition> getAllDatasets() {
        return datasetServiceDelegate.getAllDatasets();
    }

    private void sendStreamBack(final HttpServletResponse response, InputStream inputStream) {
        try {
            if (inputStream != null) {
                try {
                    IOUtils.copy(inputStream, response.getOutputStream());
                } catch (IOException e) {
                    throw new DatasetException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
                } finally {
                    inputStream.close();
                }
            } else {// could not get icon so respond a resource_not_found : 404
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (IOException e) {// is sendError fails or inputstream fails when closing
            LOGGER.error("sendError failed or inputstream failed when closing.", e); //$NON-NLS-1$
            throw new DatasetException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    @Override
    // this cannot be used as is as a rest api so see getWizardPngIconRest.
    public InputStream getDatasetPngImage(String componentName, DatasetImageType imageType) {
        return datasetServiceDelegate.getDatasetPngImage(componentName, imageType);
    }

    @RequestMapping(value = BASE_PATH + "/icon/{name}", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    @ApiOperation(value = "Return the icon related to the Dataset", notes = "return the png image related to the Dataset name parameter.")
    public void getDatasetsImageRest(
            @PathVariable(value = "name") @ApiParam(name = "name", value = "Name of Dataset") String name,
            @PathVariable(value = "type") @ApiParam(name = "type", value = "Type of the icon requested") DatasetImageType type,
            final HttpServletResponse response) {
        InputStream componentPngImageStream = getDatasetPngImage(name, type);
        sendStreamBack(response, componentPngImageStream);
    }

}

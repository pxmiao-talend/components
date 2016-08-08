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
import org.talend.components.api.component.DatastoreDefinition;
import org.talend.components.api.component.DatastoreImageType;
import org.talend.components.api.exception.DatastoreException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.DatastoreService;
import org.talend.components.api.service.internal.DatastoreRegistry;
import org.talend.components.api.service.internal.DatastoreServiceImpl;
import org.talend.daikon.exception.error.CommonErrorCodes;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * This is a spring only class that is instantiated by the spring framework. It delegates all its calls to the
 * DatastoreServiceImpl delegate create in it's constructor. This delegate uses a Datastore registry implementation
 * specific to spring.
 */

@RestController
@Api(value = "datastore", basePath = DatastoreServiceSpring.BASE_PATH, description = "Datastore services")
@Service
public class DatastoreServiceSpring implements DatastoreService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatastoreServiceSpring.class);

    public static final String BASE_PATH = "/datastore"; //$NON-NLS-1$

    private DatastoreService datastoreServiceDelegate;

    @Autowired
    public DatastoreServiceSpring(final ApplicationContext context) {
        this.datastoreServiceDelegate = new DatastoreServiceImpl(new DatastoreRegistry() {

            @Override
            public Map<String, DatastoreDefinition> getDatastores() {
                Map<String, DatastoreDefinition> compDefs = context.getBeansOfType(DatastoreDefinition.class);
                return compDefs;
            }

        });
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/properties/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ComponentProperties getComponentProperties(
            @PathVariable(value = "name") @ApiParam(name = "name", value = "Name of the datastore") String name) {
        return datastoreServiceDelegate.getComponentProperties(name);
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/datasets/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String[] getDatasets(
            @PathVariable(value = "name") @ApiParam(name = "name", value = "Name of the datastore") String name) {
        return datastoreServiceDelegate.getDatasets(name);
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/validate/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Object> validate(
            @PathVariable(value = "name") @ApiParam(name = "name", value = "Name of the datastore") String name) {
        return datastoreServiceDelegate.validate(name);
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/jsonSchema/{name}", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public @ResponseBody String getJSONSchema(
            @PathVariable(value = "name") @ApiParam(name = "name", value = "Name of the datastore") String name) {
        return datastoreServiceDelegate.getJSONSchema(name);
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/definition/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody DatastoreDefinition getDatastoreDefinition(
            @PathVariable(value = "name") @ApiParam(name = "name", value = "Name of the datastore") String name) {
        return datastoreServiceDelegate.getDatastoreDefinition(name);
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/possibleDatastores", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DatastoreDefinition> getPossibleDatastores(
            @ApiParam(name = "properties", value = "Datastore properties") @RequestBody ComponentProperties... properties)
            throws Throwable {
        return datastoreServiceDelegate.getPossibleDatastores(properties);
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/names", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Set<String> getAllDatastoreNames() {
        return datastoreServiceDelegate.getAllDatastoreNames();
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/definitions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Set<DatastoreDefinition> getAllDatastores() {
        return datastoreServiceDelegate.getAllDatastores();
    }

    private void sendStreamBack(final HttpServletResponse response, InputStream inputStream) {
        try {
            if (inputStream != null) {
                try {
                    IOUtils.copy(inputStream, response.getOutputStream());
                } catch (IOException e) {
                    throw new DatastoreException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
                } finally {
                    inputStream.close();
                }
            } else {// could not get icon so respond a resource_not_found : 404
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (IOException e) {// is sendError fails or inputstream fails when closing
            LOGGER.error("sendError failed or inputstream failed when closing.", e); //$NON-NLS-1$
            throw new DatastoreException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    @Override
    // this cannot be used as is as a rest api so see getWizardPngIconRest.
    public InputStream getDatastorePngImage(String componentName, DatastoreImageType imageType) {
        return datastoreServiceDelegate.getDatastorePngImage(componentName, imageType);
    }

    @RequestMapping(value = BASE_PATH + "/icon/{name}", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    @ApiOperation(value = "Return the icon related to the Datastore", notes = "return the png image related to the Datastore name parameter.")
    public void getDatastoresImageRest(
            @PathVariable(value = "name") @ApiParam(name = "name", value = "Name of Datastore") String name,
            @PathVariable(value = "type") @ApiParam(name = "type", value = "Type of the icon requested") DatastoreImageType type,
            final HttpServletResponse response) {
        InputStream componentPngImageStream = getDatastorePngImage(name, type);
        sendStreamBack(response, componentPngImageStream);
    }

}

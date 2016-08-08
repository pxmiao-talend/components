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
package org.talend.components.api.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.Set;

import org.junit.rules.ErrorCollector;
import org.talend.components.api.component.DatasetDefinition;
import org.talend.components.api.component.DatasetImageType;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.DatasetService;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.test.PropertiesTestUtils;

public class DatasetTestUtils {

    public static Properties checkSerialize(Properties props, ErrorCollector errorCollector) {
        return PropertiesTestUtils.checkSerialize(props, errorCollector);
    }

    /**
     * check all properties of a dataset for i18n, check form i18n, check ComponentProperties title is i18n
     * 
     * @param datasetService where to get all the datasets
     * @param errorCollector used to collect all errors at once. @see <a
     * href="http://junit.org/apidocs/org/junit/rules/ErrorCollector.html">ErrorCollector</a>
     */
    static public void testAlli18n(DatasetService datasetService, ErrorCollector errorCollector) {
        Set<DatasetDefinition> allDatasets = datasetService.getAllDatasets();
        for (DatasetDefinition cd : allDatasets) {
            ComponentProperties props = cd.createProperties();
            // check all properties
            if (props != null) {
                checkAllI18N(props, errorCollector);
            } else {
                System.out.println("No properties to check fo I18n for :" + cd.getName());
            }
            // check dataset definition title
            errorCollector.checkThat("missing I18n property :" + cd.getTitle(), cd.getTitle().contains("dataset."), is(false));
        }
    }

    public static void checkAllPropertyI18n(Property<?>[] propertyArray, Object parent, ErrorCollector errorCollector) {
        if (propertyArray != null) {
            for (Property<?> prop : propertyArray) {
                PropertiesTestUtils.chekProperty(errorCollector, prop, parent);
            }
        } // else no property to check so ignore.
    }

    static public void checkAllI18N(Properties checkedProps, ErrorCollector errorCollector) {
        PropertiesTestUtils.checkAllI18N(checkedProps, errorCollector);
    }

    /**
     * check that all Datasets have theirs images properly set.
     * 
     * @param datasetService service to get the datasets to be checked.
     */
    public static void testAllImages(DatasetService datasetService) {
        // check datasets
        Set<DatasetDefinition> allDatasets = datasetService.getAllDatasets();
        for (DatasetDefinition datasetDef : allDatasets) {
            for (DatasetImageType datasetIT : DatasetImageType.values()) {
                String pngImagePath = datasetDef.getPngImagePath(datasetIT);
                assertNotNull(
                        "the dataset [" + datasetDef.getName() + "] must return an image path for type [" + datasetIT + "]",
                        pngImagePath);
                InputStream resourceAsStream = datasetDef.getClass().getResourceAsStream(pngImagePath);
                assertNotNull(
                        "Failed to find the image for path [" + pngImagePath + "] for the dataset:type [" + datasetDef.getName()
                                + ":" + datasetIT + "].\nIt should be located at ["
                                + datasetDef.getClass().getPackage().getName().replace('.', '/') + "/" + pngImagePath + "]",
                        resourceAsStream);
            }
        }
    }

    /**
     * check that the depenencies file is present during integration test.
     * 
     * @param datasetService service to get the datasets to be checked.
     */
    public static void testAllDesignDependenciesPresent(DatasetService datasetService, ErrorCollector errorCollector) {
        Set<DatasetDefinition> allDatasets = datasetService.getAllDatasets();
        for (DatasetDefinition datasetDef : allDatasets) {
            errorCollector.checkThat(datasetDef.getMavenGroupId(), is(not(nullValue())));
            errorCollector.checkThat(datasetDef.getMavenArtifactId(), is(not(nullValue())));
        }
    }

}

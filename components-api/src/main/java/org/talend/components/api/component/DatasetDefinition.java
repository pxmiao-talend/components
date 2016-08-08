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
package org.talend.components.api.component;

import java.util.List;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

/**
 * Defines a dataset.
 * <p/>
 * A class implementing this interface is the definition of a dataset. Instances are registered with the
 * {@link org.talend.components.api.service.DatasetService} to allow datasets to be discovered.
 */

public interface DatasetDefinition extends NamedThing {

    /**
     * Return an array of paths that represent the categories of the dataset.
     */
    String[] getFamilies();

    /**
     * Return the list of components associated to the current dataset
     */
    String[] getComponents();

    /**
     * Return a sample of the data contained inside the dataset.
     */
    // TODO Change return type
    Object[] getSample(Integer size);

    /**
     * Return the schema associated to the current dataset
     */
    // TODO Change return type
    String getSchema();

    /**
     * Check the integrity of the current dataset. The nature of the checks are dependant of the dataset. It must allow
     * the user to know when the registered parameters are valid before creating a Dataset.
     */
    List<Object> validate();

    /**
     * Return data of current dataset as a JSONSchema compatible with the UI.
     */
    String getJSONSchema();

    /**
     * Create and initialize a suitable {@link ComponentProperties} which configures an instance of this dataset.
     */
    ComponentProperties createProperties();

    /**
     * FIXME - is this really necessary? create the ComponentProperties and initialize it's properties only and not the
     * UI Layout not usefull for runtime
     */
    ComponentProperties createRuntimeProperties();

    /**
     * Common return properties names
     */
    static final String RETURN_ERROR_MESSAGE = "errorMessage";

    static final String RETURN_TOTAL_RECORD_COUNT = "totalRecordCount";

    static final String RETURN_SUCCESS_RECORD_COUNT = "successRecordCount";

    static final String RETURN_REJECT_RECORD_COUNT = "rejectRecordCount";

    static final Property<String> RETURN_ERROR_MESSAGE_PROP = PropertyFactory.newString(RETURN_ERROR_MESSAGE);

    static final Property<Integer> RETURN_TOTAL_RECORD_COUNT_PROP = PropertyFactory.newInteger(RETURN_TOTAL_RECORD_COUNT);

    static final Property<Integer> RETURN_SUCCESS_RECORD_COUNT_PROP = PropertyFactory.newInteger(RETURN_SUCCESS_RECORD_COUNT);

    static final Property<Integer> RETURN_REJECT_RECORD_COUNT_PROP = PropertyFactory.newInteger(RETURN_REJECT_RECORD_COUNT);

    /**
     * Returns true if this {@code DatasetDefinition} will work with the specified list of {@link ComponentProperties}
     */
    boolean supportsProperties(ComponentProperties... properties);

    /**
     * A path relative to the current Dataset definition, ideally is should just be the name of the png image if placed
     * in the same resource folder as the implementing class. The
     * {@code org.talend.components.api.service.DatasetService} will compute the icon with the following code:
     * 
     * <pre>
     * {@code
     *    this.getClass().getResourceAsStream(getIconPngPath())
     * }
     * </pre>
     * 
     * @see {@link java.lang.Class#getResourceAsStream(String)}
     * @param imageType the type of image requested
     * @return the path to the png resource or null if an image is not required.
     */
    String getPngImagePath(DatasetImageType imageType);

    // FIXME - An ENUM perhaps?
    String getPartitioning();

    /**
     * Used for computing the dependencies by finding the pom.xml and dependencies.properties in the META-INF/ folder
     * 
     * @return the maven Group Id of the dataset family
     */
    String getMavenGroupId();

    /**
     * Used for computing the dependencies by finding the pom.xml and dependencies.properties in the META-INF/ folder
     * 
     * @return the maven Artifact Id of the dataset family
     */
    String getMavenArtifactId();

}

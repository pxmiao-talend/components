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
 * Defines a datastore.
 * <p/>
 * A class implementing this interface is the definition of a datastore. Instances are registered with the
 * {@link org.talend.components.api.service.DatastoreService} to allow datastores to be discovered.
 */

public interface DatastoreDefinition extends NamedThing {

    /**
     * Returns an array of paths that represent the categories of the datastore.
     */
    String[] getFamilies();

    /**
     * Returns the list of datasets associated to the current datastore
     */
    String[] getDatasets();

    /**
     * Check the integrity of the current datastore. The nature of the checks are dependant of the datastore. It must
     * allow the user to know when the registered parameters are valid before creating a Dataset.
     */
    List<Object> validate();

    /**
     * Return data of current datastore as a JSONSchema compatible with the UI.
     */
    String getJSONSchema();

    /**
     * Create and initialize a suitable {@link ComponentProperties} which configures an instance of this datastore.
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
     * Returns true if this {@code DatastoreDefinition} will work with the specified list of {@link ComponentProperties}
     */
    boolean supportsProperties(ComponentProperties... properties);

    /**
     * A path relative to the current Datastore definition, ideally is should just be the name of the png image if
     * placed in the same resource folder as the implementing class. The
     * {@code org.talend.components.api.service.DatastoreService} will compute the icon with the following code:
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
    String getPngImagePath(DatastoreImageType imageType);

    // FIXME - An ENUM perhaps?
    String getPartitioning();

    /**
     * Used for computing the dependencies by finding the pom.xml and dependencies.properties in the META-INF/ folder
     * 
     * @return the maven Group Id of the datastore family
     */
    String getMavenGroupId();

    /**
     * Used for computing the dependencies by finding the pom.xml and dependencies.properties in the META-INF/ folder
     * 
     * @return the maven Artifact Id of the datastore family
     */
    String getMavenArtifactId();

}

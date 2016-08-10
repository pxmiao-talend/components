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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.dropbox.DropboxDefinition;
import org.talend.components.dropbox.runtime.DropboxGetSource;

/**
 * Unit-tests for {@link TDropboxPutDefinition} class
 */
public class TDropboxPutDefinitionTest {

    /**
     * Check {@link TDropboxPutDefinition#getMavenGroupId()} returns "org.talend.components"
     */
    @Test
    public void testGetMavenGroupId() {
        DropboxDefinition definition = new TDropboxPutDefinition();
        String mavenGroupId = definition.getMavenGroupId();
        assertThat(mavenGroupId, equalTo("org.talend.components"));
    }

    /**
     * Check {@link TDropboxPutDefinition#getMavenArtifactId()} returns "components-dropbox"
     */
    @Test
    public void testGetMavenArtifactId() {
        DropboxDefinition definition = new TDropboxPutDefinition();
        String mavenArtifactId = definition.getMavenArtifactId();
        assertThat(mavenArtifactId, equalTo("components-dropbox"));
    }

    /**
     * Check {@link TDropboxPutDefinition#getFamilies()} returns string array, which contains "Cloud/Dropbox"
     */
    @Test
    public void testGetFamilies() {
        DropboxDefinition definition = new TDropboxPutDefinition();
        String[] families = definition.getFamilies();
        assertThat(families, arrayContaining("Cloud/Dropbox"));
    }

    /**
     * Check {@link TDropboxPutDefinition#getName()} returns "tDropboxPut"
     */
    @Test
    public void testGetName() {
        DropboxDefinition definition = new TDropboxPutDefinition();
        String componentName = definition.getName();
        assertEquals(componentName, "tDropboxPut");
    }

    /**
     * Check {@link TDropboxPutDefinition#getPropertyClass()} returns class, which canonical name is
     * "org.talend.components.dropbox.tdropboxput.TDropboxPutProperties"
     */
    @Ignore
    @Test
    public void testGetPropertyClass() {
        DropboxDefinition definition = new TDropboxPutDefinition();
        Class<?> propertyClass = definition.getPropertyClass();
        String canonicalName = propertyClass.getCanonicalName();
        assertThat(canonicalName, equalTo("org.talend.components.dropbox.tdropboxget.TDropboxPutProperties"));
    }

    /**
     * Check {@link TDropboxPutDefinition#getRuntime()} returns instance of {@link DropboxPutSink}
     */
    //    @Test
    //    public void testGetRuntime() {
    //        TDropboxPutDefinition definition = new TDropboxPutDefinition();
    //        Source source = definition.getRuntime();
    //        assertThat(source, is(instanceOf(DropboxPutSink.class)));
    //    }

}

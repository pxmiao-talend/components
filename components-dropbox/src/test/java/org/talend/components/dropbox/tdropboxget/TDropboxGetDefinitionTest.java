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
package org.talend.components.dropbox.tdropboxget;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.dropbox.DropboxDefinition;

/**
 * Unit-tests for {@link TDropboxGetDefinition} class
 */
public class TDropboxGetDefinitionTest {

    /**
     * Check {@link TDropboxGetDefinition#getMavenGroupId()} returns "org.talend.components"
     */
    @Test
    public void testGetMavenGroupId() {
        DropboxDefinition definition = new TDropboxGetDefinition();
        String mavenGroupId = definition.getMavenGroupId();
        assertThat(mavenGroupId, equalTo("org.talend.components"));
    }

    /**
     * Check {@link TDropboxGetDefinition#getMavenArtifactId()} returns "components-dropbox"
     */
    @Test
    public void testGetMavenArtifactId() {
        DropboxDefinition definition = new TDropboxGetDefinition();
        String mavenArtifactId = definition.getMavenArtifactId();
        assertThat(mavenArtifactId, equalTo("components-dropbox"));
    }

    /**
     * Check {@link TDropboxGetDefinition#getFamilies()} returns string array, which contains "Cloud/Dropbox"
     */
    @Test
    public void testGetFamilies() {
        DropboxDefinition definition = new TDropboxGetDefinition();
        String[] families = definition.getFamilies();
        assertThat(families, arrayContaining("Cloud/Dropbox"));
    }

    /**
     * Check {@link TDropboxGetDefinition#getName()} returns "tDropboxConnection"
     */
    @Test
    public void testGetName() {
        DropboxDefinition definition = new TDropboxGetDefinition();
        String componentName = definition.getName();
        assertEquals(componentName, "tDropboxGet");
    }

    /**
     * Check {@link TDropboxGetDefinition#getPropertyClass()} returns class, which canonical name is
     * "org.talend.components.jira.tjirainput.TDropboxConnectionProperties"
     */
    @Test
    public void testGetPropertyClass() {
        DropboxDefinition definition = new TDropboxGetDefinition();
        Class<?> propertyClass = definition.getPropertyClass();
        String canonicalName = propertyClass.getCanonicalName();
        assertThat(canonicalName, equalTo("org.talend.components.dropbox.tdropboxget.TDropboxGetProperties"));
    }

    /**
     * Check {@link TDropboxGetDefinition#getRuntime()} returns instance of {@link JiraSource}
     */
    //    @Test
    //    public void testGetRuntime() {
    //        TDropboxGetDefinition definition = new TDropboxGetDefinition();
    //        Source source = definition.getRuntime();
    //        assertThat(source, is(instanceOf(DropboxSourceOrSink.class)));
    //    }

}

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
package org.talend.components.api.service.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuilderFactory;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingResult;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectModelResolver;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.internal.impl.DefaultRemoteRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.graph.selector.AndDependencySelector;
import org.eclipse.aether.util.graph.selector.ExclusionDependencySelector;
import org.eclipse.aether.util.graph.selector.OptionalDependencySelector;
import org.eclipse.aether.util.graph.selector.ScopeDependencySelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.Constants;
import org.talend.components.api.component.DatasetDefinition;
import org.talend.components.api.component.DatasetImageType;
import org.talend.components.api.exception.DatasetException;
import org.talend.components.api.exception.error.DatasetsApiErrorCode;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.DatasetService;
import org.talend.daikon.NamedThing;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.properties.service.PropertiesServiceImpl;

/**
 * Main Dataset Service implementation that is not related to any framework (neither OSGI, nor Spring) it uses a
 * DatasetRegistry implementation that will be provided by framework specific Service classes
 */
public class DatasetServiceImpl extends PropertiesServiceImpl implements DatasetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetServiceImpl.class);

    private Map<Artifact, Set<Dependency>> dependenciesCache = new HashMap<>();

    private DatasetRegistry datasetRegistry;

    private ModelBuilder modelBuilder;

    public DatasetServiceImpl(DatasetRegistry datasetRegistry) {
        this.datasetRegistry = datasetRegistry;
    }

    @Override
    public Set<String> getAllDatasetNames() {
        // remove the datasets# internal prefix to return the simple name
        Collection<String> datasetsInternalNames = datasetRegistry.getDatasets().keySet();
        Set<String> datasetNames = new HashSet<>(datasetsInternalNames.size());
        for (String name : datasetsInternalNames) {
            datasetNames.add(name.substring(Constants.DATASET_BEAN_PREFIX.length()));
        }
        return datasetNames;
    }

    @Override
    public Set<DatasetDefinition> getAllDatasets() {
        return new HashSet<>(datasetRegistry.getDatasets().values());
    }

    @Override
    public ComponentProperties getComponentProperties(String name) {
        DatasetDefinition datasetDef = getDatasetDefinition(name);
        return datasetDef.createProperties();
    }

    @Override
    public String[] getComponents(String name) {
        DatasetDefinition datasetDef = getDatasetDefinition(name);
        return datasetDef.getComponents();
    }

    @Override
    public List<Object> validate(String name) {
        DatasetDefinition datasetDef = getDatasetDefinition(name);
        return datasetDef.validate();
    }

    @Override
    public String getJSONSchema(String name) {
        DatasetDefinition datasetDef = getDatasetDefinition(name);
        return datasetDef.getJSONSchema();
    }

    @Override
    public DatasetDefinition getDatasetDefinition(String name) {
        final String beanName = Constants.DATASET_BEAN_PREFIX + name;
        DatasetDefinition datasetDef = datasetRegistry.getDatasets().get(beanName);
        if (datasetDef == null) {
            throw new DatasetException(DatasetsApiErrorCode.WRONG_DATASET_NAME, ExceptionContext.build().put("name", name)); //$NON-NLS-1$
        } // else got the def so use it
        return datasetDef;
    }

    @Override
    public List<DatasetDefinition> getPossibleDatasets(ComponentProperties... properties) {
        List<DatasetDefinition> returnList = new ArrayList<>();
        for (DatasetDefinition cd : datasetRegistry.getDatasets().values()) {
            if (cd.supportsProperties(properties)) {
                returnList.add(cd);
            }
        }
        return returnList;
    }

    @Override
    public InputStream getDatasetPngImage(String datasetName, DatasetImageType imageType) {
        DatasetDefinition datasetDefinition = datasetRegistry.getDatasets().get(Constants.DATASET_BEAN_PREFIX + datasetName);
        if (datasetDefinition != null) {
            return getImageStream(datasetDefinition, datasetDefinition.getPngImagePath(imageType));
        } else {
            throw new DatasetException(DatasetsApiErrorCode.WRONG_DATASET_NAME, ExceptionContext.build().put("name", datasetName)); //$NON-NLS-1$
        }
    }

    /**
     * get the image stream or null
     * 
     * @param definition, must not be null
     * @return the stream or null if no image was defined for th dataset or the path is wrong
     */
    private InputStream getImageStream(NamedThing definition, String pngIconPath) {
        InputStream result = null;
        if (pngIconPath != null && !"".equals(pngIconPath)) { //$NON-NLS-1$
            InputStream resourceAsStream = definition.getClass().getResourceAsStream(pngIconPath);
            if (resourceAsStream == null) {// no resource found so this is an dataset error, so log it and return
                                           // null
                LOGGER.error("Failed to load the Wizard icon [" + definition.getName() + "," + pngIconPath + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } else {
                result = resourceAsStream;
            }
        } else {// no path provided so will return null but log it.
            LOGGER.warn("The defintion of [" + definition.getName() + "] did not specify any icon"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return result;
    }

    /**
     * this will locate the file META-INF/mavenGroupId/mavenArtifactId/depenencies.properties and parse it to extract
     * the design time dependencies of the dataset.
     * 
     * @param mavenGroupId group id of the dataset to locate the dep file
     * @param mavenArtifactId artifact id of the dataset to locate the dep file.
     * @param classLoader
     * @return set of string pax-url formated
     * @throws IOException if reading the file failed.
     */
    private Set<String> getDesignTimeDependencies(String mavenGroupId, String mavenArtifactId, ClassLoader classLoader)
            throws IOException {
        String depPath = computeDesignDependenciesPath(mavenGroupId, mavenArtifactId);
        if (classLoader == null) {
            classLoader = this.getClass().getClassLoader();
        }
        InputStream depStream = classLoader.getResourceAsStream(depPath);
        if (depStream == null) {
            throw new DatasetException(DatasetsApiErrorCode.COMPUTE_DEPENDENCIES_FAILED, ExceptionContext.withBuilder()
                    .put("path", depPath).build());
        } // else we found it so parse it now
        try {
            return parseDependencies(depStream);
        } finally {
            depStream.close();
        }
    }

    public String computeDesignDependenciesPath(String mavenGroupId, String mavenArtifactId) {
        return "META-INF/maven/" + mavenGroupId + "/" + mavenArtifactId + "/dependencies.txt";
    }

    /**
     * reads a stream following the maven-dependency-plugin plugin :list format
     * 
     * <pre>
     * {@code
     *     
     *     The following files have been resolved:
     *     org.apache.maven:maven-core:jar:3.3.3:compile
     *     org.springframework:spring-beans:jar:4.2.0.RELEASE:test
     *     org.talend.datasets:datasets-common:jar:0.4.0.BUILD-SNAPSHOT:compile
     *     log4j:log4j:jar:1.2.17:test
     *     org.eclipse.aether:aether-impl:jar:1.0.0.v20140518:compile
     * }
     * </pre>
     *
     * @param depStream of the dependencies file
     * @return a list of maven url strings
     * @throws IOException if read fails.
     */
    private Set<String> parseDependencies(InputStream depStream) throws IOException {
        Set<String> mvnUris = new HashSet<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(depStream, "UTF-8"));
        // java 8 version
        // reader.lines().filter(line -> StringUtils.countMatches(line, ":") > 3).//
        // filter(line -> !line.endsWith("test")).//
        // forEach(line -> mvnUris.add(parseMvnUri(line)));
        while (reader.ready()) {
            String line = reader.readLine();
            if ((org.apache.commons.lang3.StringUtils.countMatches(line, ":") > 3) && !line.endsWith("test")) {
                mvnUris.add(parseMvnUri(line));
            } // else not an expected dependencies so ignor it.
        }
        return mvnUris;
    }

    /**
     * expecting groupId:artifactId:type[:classifier]:version:scope and output.
     * 
     * <pre>
     * {@code
     * mvn-uri := 'mvn:' [ repository-url '!' ] group-id '/' artifact-id [ '/' [version] [ '/' [type] [ '/' classifier ] ] ] ]
     * }
     * </pre>
     * 
     * @param s
     * @return pax-url formatted string
     */
    String parseMvnUri(String dependencyString) {
        String s = dependencyString.trim();
        int indexOfGpSeparator = s.indexOf(':');
        String groupId = s.substring(0, indexOfGpSeparator);
        int indexOfArtIdSep = s.indexOf(':', indexOfGpSeparator + 1);
        String artifactId = s.substring(indexOfGpSeparator + 1, indexOfArtIdSep);
        int indexOfTypeSep = s.indexOf(':', indexOfArtIdSep + 1);
        String type = s.substring(indexOfArtIdSep + 1, indexOfTypeSep);
        int lastIndex = indexOfTypeSep;
        String classifier = null;
        if (StringUtils.countMatches(s, ":") > 4) {// we have a classifier too
            int indexOfClassifSep = s.indexOf(':', indexOfTypeSep + 1);
            classifier = s.substring(indexOfTypeSep + 1, indexOfClassifSep);
            lastIndex = indexOfClassifSep;
        } // else no classifier.
        int indexOfVersionSep = s.indexOf(':', lastIndex + 1);
        String version = s.substring(lastIndex + 1, indexOfVersionSep);
        // we ignor the scope here
        return "mvn:" + groupId + '/' + artifactId + '/' + version + '/' + type + (classifier != null ? '/' + classifier : "");
    }

    Set<String> computeDependenciesFromPom(InputStream mavenPomStream, String... excludedScopes)
            throws DependencyCollectionException, org.eclipse.aether.resolution.DependencyResolutionException, IOException,
            XmlPullParserException, ModelBuildingException {
        MavenBooter booter = new MavenBooter();
        // FIXME we may not have to load the model and resolve it
        MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
        mavenXpp3Reader.setAddDefaultEntities(false);
        Model pomModel = mavenXpp3Reader.read(mavenPomStream);

        // Model pomModel = loadPom(mavenPomStream, booter, Collections.EMPTY_LIST);

        // List<org.apache.maven.model.Dependency> dependencies = pomModel.getDependencies();
        MavenProject mavenProject = new MavenProject(pomModel);
        Set<Dependency> dependencies = getArtifactsDependencies(mavenProject, booter, excludedScopes);
        Set<String> depsStrings = new HashSet<>(dependencies.size());
        // depsStrings.add("mvn:" + pomModel.getGroupId() + "/" + pomModel.getArtifactId() + "/" +
        // pomModel.getVersion());
        for (Dependency dep : dependencies) {
            depsStrings.add("mvn:" + dep.getArtifact().getGroupId() + "/" + dep.getArtifact().getArtifactId() + "/" //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
                    + dep.getArtifact().getVersion() + "/"
                    + (dep.getArtifact().getExtension().equals("") ? "" : dep.getArtifact().getExtension())
                    + (dep.getArtifact().getClassifier().equals("") ? "" : ("/" + dep.getArtifact().getClassifier())));
        }
        return depsStrings;
    }

    public Set<Dependency> getArtifactsDependencies(MavenProject project, MavenBooter booter, String... excludedScopes)
            throws DependencyCollectionException, org.eclipse.aether.resolution.DependencyResolutionException {
        DefaultArtifact pomArtifact = new DefaultArtifact(project.getGroupId(), project.getArtifactId(), project.getPackaging(),
                null, project.getVersion());
        // check the cache if we already have computed the dependencies for this pom.
        if (dependenciesCache.containsKey(pomArtifact)) {
            return dependenciesCache.get(pomArtifact);
        }
        RepositorySystem repoSystem = booter.newRepositorySystem();
        DefaultRepositorySystemSession repoSession = booter.newRepositorySystemSession(repoSystem);
        DependencySelector depFilter = new AndDependencySelector(
                new ScopeDependencySelector(null, Arrays.asList(excludedScopes)), new OptionalDependencySelector(),
                new ExclusionDependencySelector());
        repoSession.setDependencySelector(depFilter);

        List<RemoteRepository> remoteRepos = booter.getRemoteRepositoriesWithAuthentification(repoSystem, repoSession);

        CollectRequest collectRequest = new CollectRequest(new Dependency(pomArtifact, "runtime"), remoteRepos);
        // collectRequest.setRequestContext(scope);
        CollectResult collectResult = repoSystem.collectDependencies(repoSession, collectRequest);
        DependencyNode root = collectResult.getRoot();
        Set<Dependency> ret = new HashSet<>();
        ret.add(root.getDependency());
        flattenDeps(root, ret);
        dependenciesCache.put(pomArtifact, ret);
        return ret;
    }

    private static void flattenDeps(DependencyNode node, Set<Dependency> ret) {
        List<DependencyNode> children = node.getChildren();
        for (DependencyNode dn : children) {
            Dependency dep = dn.getDependency();
            ret.add(dep);
            if (!dn.getChildren().isEmpty()) {
                flattenDeps(dn, ret);
            }
        }
    }

    Model loadPom(final InputStream pomStream, MavenBooter booter, List<String> profilesList) throws ModelBuildingException {

        RepositorySystem system = booter.newRepositorySystem();
        RepositorySystemSession session = booter.newRepositorySystemSession(system);
        ModelBuildingRequest modelRequest = new DefaultModelBuildingRequest();
        modelRequest.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
        modelRequest.setProcessPlugins(false);
        modelRequest.setTwoPhaseBuilding(false);
        modelRequest.setSystemProperties(toProperties(session.getUserProperties(), session.getSystemProperties()));
        // modelRequest.setModelCache( DefaultModelCache.newInstance( session ) );
        ProjectModelResolver projectModelResolver = new ProjectModelResolver(session, null, system,
                new DefaultRemoteRepositoryManager(), booter.getRemoteRepositoriesWithAuthentification(system, session), null,
                null);
        modelRequest.setModelResolver(projectModelResolver);
        modelRequest.setActiveProfileIds(profilesList);
        modelRequest.setModelSource(new ModelSource() {

            @Override
            public InputStream getInputStream() throws IOException {
                return pomStream;
            }

            @Override
            public String getLocation() {
                return "";// FIXME return the dataset name
            }
        });
        if (modelBuilder == null) {
            modelBuilder = new DefaultModelBuilderFactory().newInstance();
        }
        ModelBuildingResult builtModel = modelBuilder.build(modelRequest);
        LOGGER.debug("built problems:" + builtModel.getProblems());
        return builtModel.getEffectiveModel();
    }

    private java.util.Properties toProperties(Map<String, String> dominant, Map<String, String> recessive) {
        java.util.Properties props = new java.util.Properties();
        if (recessive != null) {
            props.putAll(recessive);
        }
        if (dominant != null) {
            props.putAll(dominant);
        }
        return props;
    }

}

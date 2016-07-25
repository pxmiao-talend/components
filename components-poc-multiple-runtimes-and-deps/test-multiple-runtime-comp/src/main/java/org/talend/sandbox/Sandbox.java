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
package org.talend.sandbox;

import java.net.URL;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.sandbox.properties.StandardPropertiesStrategyFactory;
import org.talend.sandbox.properties.ThreadIsolatedSystemProperties;

public class Sandbox {

    private static final Logger LOGGER = LoggerFactory.getLogger(Sandbox.class);

    /**
     * @param parameters Input values for job execution
     * @return Result of job execution
     * @throws com.amalto.core.jobox.util.JoboxException In case of call error.
     */
    public final Object createRuntimeInstance(String classToInstanciated, Set<URL> mavenUris) {

        ClassLoader previousCallLoader = Thread.currentThread().getContextClassLoader();
        ThreadIsolatedSystemProperties isolatedSystemProperties = ThreadIsolatedSystemProperties.getInstance();

        try {
            if (!System.getProperties().equals(isolatedSystemProperties.getThreadProperties(Thread.currentThread()))) {
                throw new IllegalStateException("Expected system properties to support thread isolation."); //$NON-NLS-1$
            }

            LOGGER.info("creating class '" + classToInstanciated + "'"); //$NON-NLS-1$ //$NON-NLS-2$

            ClassLoader sandboxClassLoader = new SandboxClassLoader(mavenUris.toArray(new URL[mavenUris.size()]));
            Thread.currentThread().setContextClassLoader(sandboxClassLoader);

            // Isolate current running thread with JVM standard properties.
            isolatedSystemProperties.isolateThread(Thread.currentThread(),
                    StandardPropertiesStrategyFactory.create().getStandardProperties());

            if (classToInstanciated == null) {
                throw new IllegalArgumentException("classToInstanciate should not be null");
            }

            // container.updateJobLoadersPool(jobInfo);
            Class<?> clazz = sandboxClassLoader.loadClass(classToInstanciated);
            return clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new TalendRuntimeException(null, e);// TODO make it a proper exception
        } finally {
            // Reintegrate thread into global system properties world.
            isolatedSystemProperties.integrateThread(Thread.currentThread());
            Thread.currentThread().setContextClassLoader(previousCallLoader);
        }
    }

}

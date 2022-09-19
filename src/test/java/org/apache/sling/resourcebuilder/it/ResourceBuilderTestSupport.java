/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.resourcebuilder.it;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.resourcebuilder.api.ResourceBuilder;
import org.apache.sling.resourcebuilder.api.ResourceBuilderFactory;
import org.apache.sling.testing.paxexam.TestSupport;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.ModifiableCompositeOption;
import org.ops4j.pax.exam.options.OptionalCompositeOption;
import org.ops4j.pax.exam.options.extra.VMOption;

import javax.inject.Inject;
import java.util.Objects;
import java.util.UUID;

import static org.apache.sling.testing.paxexam.SlingOptions.awaitility;
import static org.apache.sling.testing.paxexam.SlingOptions.logback;
import static org.apache.sling.testing.paxexam.SlingOptions.slingQuickstartOakTar;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.vmOption;
import static org.ops4j.pax.exam.CoreOptions.when;
import static org.ops4j.pax.exam.cm.ConfigurationAdminOptions.newConfiguration;

public class ResourceBuilderTestSupport extends TestSupport {

    @Inject
    protected ResourceResolverFactory resourceResolverFactory;

    @Inject
    protected ResourceBuilderFactory builderService;

    protected ResourceBuilder builder;
    protected String testRootPath;
    protected Resource parent;

    public ModifiableCompositeOption baseConfiguration() {
        return composite(
            super.baseConfiguration(),
            slingQuickstart(),
            //Sling ResourceBuilder
            testBundle("bundle.filename"),
            logback(),
            awaitility(),
            junitBundles(),
            newConfiguration("org.apache.sling.jcr.base.internal.LoginAdminWhitelist")
                    .put("whitelist.bundles.regexp", "PAXEXAM-PROBE-.*")
                    .asOption(),

            optionalRemoteDebug(),
            jacoco()
        );
    }

    protected Option slingQuickstart() {
        final int httpPort = findFreePort();
        final String workingDirectory = workingDirectory();
        return slingQuickstartOakTar(workingDirectory, httpPort);
    }

    /**
     * Optionally configure remote debugging on the port supplied by the "debugPort"
     * system property.
     */
    protected ModifiableCompositeOption optionalRemoteDebug() {
        VMOption option = null;
        String property = System.getProperty("debugPort");
        if (property != null) {
            option = vmOption(String.format("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=%s", property));
        }
        return composite(option);
    }

    // remove with Testing PaxExam 4.0
    protected OptionalCompositeOption jacoco() {
        final String jacocoCommand = System.getProperty("jacoco.command");
        final VMOption option = Objects.nonNull(jacocoCommand) && !jacocoCommand.trim().isEmpty() ? vmOption(jacocoCommand) : null;
        return when(Objects.nonNull(option)).useOptions(option);
    }

    ResourceResolver resolver() throws LoginException {
        return resourceResolverFactory.getAdministrativeResourceResolver(null);
    }

    void initializeTestResources() throws LoginException, PersistenceException {
        testRootPath = getClass().getSimpleName() + "-" + UUID.randomUUID();

        final Resource root = resolver().getResource("/");
        parent = resolver().create(root, testRootPath, null);
        builder = builderService.forParent(parent);
    }

    void cleanupTestResources() throws PersistenceException, LoginException {
        if(resolver() != null && parent != null) {
            resolver().delete(parent);
            resolver().commit();
        }
    }
}

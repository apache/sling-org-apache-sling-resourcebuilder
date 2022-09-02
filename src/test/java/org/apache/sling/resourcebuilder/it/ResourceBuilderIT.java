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
import org.apache.sling.resourcebuilder.impl.MapArgsConverter;
import org.apache.sling.resourcebuilder.test.ResourceAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Constants;

import java.io.IOException;
import java.util.Comparator;

import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.CoreOptions.options;

/** Server-side integration test for the 
 *  ResourceBuilder, acquired via the ResourceBuilderProvider
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class ResourceBuilderIT extends ResourceBuilderTestSupport {

    private ResourceAssertions resourceAssertions;

    @Before
    public void setup() throws LoginException, PersistenceException {
        initializeTestResources();
        resourceAssertions = new ResourceAssertions(parent.getPath(), resolver());
    }
    
    @After
    public void cleanup() throws PersistenceException, LoginException {
        cleanupTestResources();
    }

    @Configuration
    public Option[] configuration() {
        return options(
                baseConfiguration()
        );
    }

    @ProbeBuilder
    public TestProbeBuilder probeConfiguration(final TestProbeBuilder probeBuilder) {
        probeBuilder.setHeader(Constants.EXPORT_PACKAGE, MapArgsConverter.class.getPackage().getName());

        return probeBuilder;
    }
    
    @Test
    public void simpleResource() {
        builder.resource("foo", "title", testRootPath).commit();

        resourceAssertions.assertProperties("foo", "title", testRootPath);
    }
    
    @Test
    public void smallTreeWithFile() throws IOException {
        builder
            .resource("somefolder")
            .file("the-model.js", getClass().getResourceAsStream("/files/models.js"), "foo", 42L)
            .commit();

        resourceAssertions.assertFile("somefolder/the-model.js", "foo", "yes, it worked", 42L);
    }
    
    @Test
    public void fileAutoValues() throws IOException {
        final long startTime = System.currentTimeMillis();
        builder
            .resource("a/b/c")
            .file("model2.js", getClass().getResourceAsStream("/files/models.js"))
            .commit();
        
        final Comparator<Long> moreThanStartTime = (expected, fromResource) -> {
            if(fromResource >= startTime) {
                return 0;
            }
            fail("last-modified is not >= than current time:" + fromResource + " < " + startTime);
            return -1;
        };

        resourceAssertions.assertFile("a/b/c/model2.js", "application/javascript", "yes, it worked", startTime, moreThanStartTime);
    }
    
    @Test
    public void usingResolver() throws LoginException {
        builderService.forResolver(resolver()).resource("foo/a/b").commit();
        builderService.forResolver(resolver()).resource("foo/c/d").commit();

        resourceAssertions.assertResource("/foo/a/b");
        resourceAssertions.assertResource("/foo/c/d");
    }
    
}

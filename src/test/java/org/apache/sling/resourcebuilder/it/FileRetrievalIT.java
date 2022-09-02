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
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.resourcebuilder.test.ResourceAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.options;

/** Verify that our file structure is correct,
 *  by creating a file and retrieving it via
 *  a Sling request. 
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class FileRetrievalIT extends ResourceBuilderTestSupport {

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
    
    @Test
    public void createAndRetrieveFile() throws IOException {
        final String expected = "yes, it worked";
        final long startTime = System.currentTimeMillis();
        final String mimeType = "application/javascript";
        
        builder
            .resource("somefolder")
            .file("the-model.js", getClass().getResourceAsStream("/files/models.js"))
            .commit();
        
        final Resource r = resourceAssertions.assertFile("somefolder/the-model.js", mimeType, expected, -1L);
        
        final ResourceMetadata meta = r.getResourceMetadata();
        assertTrue("Expecting a last modified time >= startTime", meta.getModificationTime() >= startTime);
        assertEquals("Expecting the correct mime-type", mimeType, meta.getContentType());

        final InputStream is = r.adaptTo(InputStream.class);
        assertNotNull("Expecting InputStream for file resource " + r.getPath(), is);
        try {
            final String content = resourceAssertions.readFully(is);
            assertTrue("Expecting [" + expected + "] in content", content.contains(expected));
        } finally {
            is.close();
        }
    }
}
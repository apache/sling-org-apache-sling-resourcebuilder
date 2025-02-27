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
package org.apache.sling.resourcebuilder.impl;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.mime.MimeTypeService;
import org.apache.sling.resourcebuilder.api.ResourceBuilder;
import org.apache.sling.resourcebuilder.api.ResourceBuilderFactory;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * ResourceBuilderFactory service.
 */
@Component(service = ResourceBuilderFactory.class)
public class ResourceBuilderFactoryService implements ResourceBuilderFactory {

    @Reference
    private MimeTypeService mimeTypeService;

    @SuppressWarnings("null")
    @Override
    public @NotNull ResourceBuilder forParent(@NotNull Resource parent) {
        return new ResourceBuilderImpl(parent, mimeTypeService);
    }

    @Override
    public @NotNull ResourceBuilder forResolver(@NotNull ResourceResolver r) {
        final Resource root = r.getResource("/");
        if (root == null) {
            throw new IllegalStateException("Cannot read root resource");
        }
        return forParent(root);
    }
}

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

package com.epam.datalab.backendapi.modules;

import com.epam.datalab.backendapi.resources.azure.ComputationalResourceAzure;
import com.epam.datalab.backendapi.resources.azure.EdgeResourceAzure;
import com.epam.datalab.backendapi.resources.azure.ExploratoryResourceAzure;
import com.epam.datalab.backendapi.resources.azure.InfrastructureResourceAzure;
import com.epam.datalab.cloud.CloudModule;
import com.google.inject.Injector;
import io.dropwizard.setup.Environment;

public class AzureProvisioningModule extends CloudModule {

    @Override
    public void init(Environment environment, Injector injector) {
        environment.jersey().register(injector.getInstance(EdgeResourceAzure.class));
        environment.jersey().register(injector.getInstance(InfrastructureResourceAzure.class));
        environment.jersey().register(injector.getInstance(ExploratoryResourceAzure.class));
        environment.jersey().register(injector.getInstance(ComputationalResourceAzure.class));
    }
}

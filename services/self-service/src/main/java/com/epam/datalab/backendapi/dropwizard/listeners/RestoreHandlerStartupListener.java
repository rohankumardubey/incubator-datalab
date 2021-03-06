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

package com.epam.datalab.backendapi.dropwizard.listeners;

import com.epam.datalab.backendapi.domain.EndpointDTO;
import com.epam.datalab.backendapi.service.EndpointService;
import com.epam.datalab.rest.client.RESTService;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Server;

@Slf4j
public class RestoreHandlerStartupListener implements ServerLifecycleListener {

    private final RESTService provisioningService;
    private final EndpointService endpointService;

    public RestoreHandlerStartupListener(RESTService provisioningService, EndpointService endpointService) {
        this.provisioningService = provisioningService;
        this.endpointService = endpointService;
    }

    @Override
    public void serverStarted(Server server) {
        try {
            endpointService.getEndpointsWithStatus(EndpointDTO.EndpointStatus.ACTIVE)
                    .forEach(e -> provisioningService.post(e.getUrl() + "/handler/restore", StringUtils.EMPTY, Object.class));
        } catch (Exception e) {
            log.error("Exception occurred during restore handler request: {}", e.getMessage(), e);
        }
    }
}

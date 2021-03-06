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

package com.epam.datalab.backendapi.resources.azure;

import com.epam.datalab.auth.UserInfo;
import com.epam.datalab.backendapi.service.impl.SparkClusterService;
import com.epam.datalab.dto.azure.computational.SparkComputationalCreateAzure;
import com.epam.datalab.dto.computational.ComputationalClusterConfigDTO;
import com.epam.datalab.dto.computational.ComputationalStartDTO;
import com.epam.datalab.dto.computational.ComputationalStopDTO;
import com.epam.datalab.dto.computational.ComputationalTerminateDTO;
import com.epam.datalab.rest.contracts.ComputationalAPI;
import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class ComputationalResourceAzure implements ComputationalAPI {

    @Inject
    private SparkClusterService sparkClusterService;

    @POST
    @Path(ComputationalAPI.COMPUTATIONAL_CREATE_SPARK)
    public String create(@Auth UserInfo ui, SparkComputationalCreateAzure dto) {
        log.debug("Create computational Spark resources {} for user {}: {}",
                dto.getComputationalName(), ui.getName(), dto);

        return sparkClusterService.create(ui, dto);
    }


    @POST
    @Path(ComputationalAPI.COMPUTATIONAL_TERMINATE_SPARK)
    public String terminate(@Auth UserInfo ui, ComputationalTerminateDTO dto) {
        log.debug("Terminate computational Spark resources {} for user {}: {}",
                dto.getComputationalName(), ui.getName(), dto);

        return sparkClusterService.terminate(ui, dto);
    }

    @POST
    @Path(ComputationalAPI.COMPUTATIONAL_STOP_SPARK)
    public String stopSparkCluster(@Auth UserInfo ui, ComputationalStopDTO dto) {
        log.debug("Stop computational Spark resources {} for user {}: {}",
                dto.getComputationalName(), ui.getName(), dto);

        return sparkClusterService.stop(ui, dto);
    }

    @POST
    @Path(ComputationalAPI.COMPUTATIONAL_START_SPARK)
    public String startSparkCluster(@Auth UserInfo ui, ComputationalStartDTO dto) {
        log.debug("Start computational Spark resource {} for user {}: {}",
                dto.getComputationalName(), ui.getName(), dto);

        return sparkClusterService.start(ui, dto);
    }

    @POST
    @Path(ComputationalAPI.COMPUTATIONAL_RECONFIGURE_SPARK)
    public String reconfigureSparkCluster(@Auth UserInfo ui, ComputationalClusterConfigDTO config) {
        log.debug("User is reconfiguring {} spark cluster for exploratory {}", ui.getName(),
                config.getComputationalName(), config.getNotebookInstanceName());
        return sparkClusterService.updateConfig(ui, config);
    }

}

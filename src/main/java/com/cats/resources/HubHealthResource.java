package com.cats.resources;

/*
 * Copyright 2021 Comcast Cable Communications Management, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import com.cats.ir.IRDevice;
import com.cats.ir.RemoteFactory;
import com.cats.ir.gc.GCDispatcherService;
import com.cats.ir.gchealth.GCDispatcherHealthBean;
import com.cats.ir.hubhealth.HealthReport;
import com.cats.ir.hubhealth.HealthStatusBean;
import com.cats.ir.hubhealth.HubHealthBean;
import com.cats.ir.hubhealth.HubHealthCheck;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.core.Response;

import com.cats.service.RemoteProcessor;
import com.cats.service.DependencyHealthCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Resource for retrieving information about RedRatHub and RedRatDevices associated with this hub.
  */
@RestController
@Tag(name = "IR Health", description = "Health APIs for IR Service")
@RequestMapping("/health")
public class HubHealthResource {
    protected static Logger logger = LoggerFactory.getLogger(HubHealthResource.class);
    @Autowired
    HubHealthCheck hhc;
    @Autowired
    RemoteProcessor processor;

    @Autowired
    RemoteFactory factory;

    @Autowired
    DependencyHealthCheck availableIRDependencies;

    @Autowired
    GCDispatcherService gcDispatcherService;

    @Operation(summary = "Restart Health API for IR Service.", description = "Restart Health API for IR Service.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "400", description = "Cannot process request. Please try again..")
    })
    @RequestMapping(value = "/restart", method = RequestMethod.PUT)
    public Response restart() {
        hhc.restart();
        return Response.ok().build();
    }

    /**
     * Return current status of HubHealthBean and GC Dispatcher (if deployed on rack).
     * <p>
     * Checks if RedRatHub is on rack and has outdated health info or if GC Dispatcher is on rack
     * If RedRatHub Health info is outdated, reprocess health
     * If GC Dispatcher is on rack, reprocess health
     *
     * @return JSON response information about hub/devices.
     */
    @Operation(summary = "Get Health of IR on the Rack.", description = "Get info on all RedRatHub and RedRatDevices associated with the hub.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = HealthStatusBean.class)) }),
            @ApiResponse(responseCode = "404", description = "IR devices not found.")
    })
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public HealthStatusBean getHealth() {
        HubHealthBean healthBean = hhc.getHealthBean();
        boolean hasHub = availableIRDependencies.checkHubHealth();
        boolean hasGcDispatcher = availableIRDependencies.checkGCDispatcherHealth();
        boolean reprocessHubHealth = hhc.isHubHealthOutdated(healthBean.getDevices(), factory);

        if ((hasHub && reprocessHubHealth)) {
            hhc.process();
            healthBean = hhc.getHealthBean();
        }

        GCDispatcherHealthBean gcDispatcherHealthBean = null;
        if (hasGcDispatcher) {
            gcDispatcherHealthBean = gcDispatcherService.getHealth();
        }

        return new HealthStatusBean(healthBean, gcDispatcherHealthBean);
    }

    public void stats() {
        hhc.stats();
    }
}

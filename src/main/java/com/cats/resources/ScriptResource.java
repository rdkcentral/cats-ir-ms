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

import com.cats.configuration.IRDeviceConfig;
import com.cats.ir.Remote;
import com.cats.ir.exception.CustomBadRequestException;
import com.cats.ir.exception.CustomInternalErrorException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.core.Response;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cats.service.RemoteProcessor;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * Defines REST API path to make IR requests to IrNetBox ports using a groovy script.
 */

@RestController
@Tag(name = "REST API path to make IR requests to IrNetBox",
        description = "Defines REST API path to make IR requests to IrNetBox ports using a groovy script.")
@RequestMapping("/rest/{rack}/{slot}/script/{keySet}")
public class ScriptResource {

    private RemoteProcessor processor;

    public ScriptResource(RemoteProcessor processor) {
        this.processor = processor;
    }

    /**
     * Gets a LocalRemote with a connection to a port of an IRNetBox device.
     *
     * @param keySet Remote key mapping to use during transmission.
     * @return a LocalRemote object.
     */
    private Remote getRemote(String slot, String keySet) {
        return processor.getRemote(slot, keySet);
    }

    /**
     * Verifies the slot number requested matches a mapped device and port to
     * this instance of the application.
     *
     * @param slot The requested slot.
     * @throws CustomBadRequestException when slot is invalid.
     */
    private void validateSlot(String slot) {
        if (!processor.validateSlot(slot)) {
            throw new CustomBadRequestException
                    ("Requested slot is not valid. Please choose a number between 1 and " + processor.numSlots() + ".");
        }
    }

    /**
     * Runs a groovy script that makes IR requests.
     *
     * @param rack   The requested rack
     * @param slot   The requested slot
     * @param keyset The keyset setting to create a Remote object.
     * @param script The script to be executed.
     * @return 200 if script executes without timeout/exceptions.
     * @throws CustomInternalErrorException if script throws an exception when executing.
     */
    @Operation(summary = "Runs a groovy script that makes IR requests.", description = "Runs a groovy script that makes IR requests.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful"),
            @ApiResponse(responseCode = "400", description = "Exception while executing the script."),
            @ApiResponse(responseCode = "404", description = "Device/Port not found.")
    })
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public Response runMacro(@Parameter(description = "Device Rack.") @RequestParam("rack") String rack,
                             @Parameter(description = "Device Slot.") @RequestParam("slot") String slot,
                             @RequestParam("keySet") String keyset,
                             String script) {

        // Validation of slot and script parameters.
        validateSlot(slot);
        if (script.isEmpty()) {
            throw new CustomBadRequestException
                    ("No script was provided. Please submit a script to be executed as plain text in a POST body.");
        }

        GroovyShell shell = new GroovyShell();
        shell.setVariable("remote", getRemote(slot, keyset));
        Thread thread = new Thread() {
            public void run() {
                Script validatedScript = shell.parse(script);
                try {
                    validatedScript.run();
                } catch (Exception e) {
                    throw new CustomInternalErrorException("An error occurred during script execution: " + e.getMessage());
                }
            }
        };
        thread.start();
        try {
            thread.join(1000 * 60);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (thread.isAlive()) {
            thread.interrupt();
        }

        return Response.ok().build();
    }

}

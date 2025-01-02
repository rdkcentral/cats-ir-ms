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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.cats.configuration.IRDeviceConfig;
import com.cats.ir.SlotToPortMappings;
import com.cats.ir.exception.SlotMappingException;
import com.cats.service.SlotMappingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Resource for modifying slot -> device:port mappings
 */
@RestController
@Tag(name = "Modifying slot device mappings.", description = "Resource for modifying slot -> device:port mappings")
@RequestMapping("/mappings")
public class SlotMappingResource {

    protected Logger logger = LoggerFactory.getLogger(SlotMappingResource.class);
    @Autowired
    private SlotMappingService mappingService;

    @Operation(summary = "Get IR Slot Mappings.", description = "Get the slot mapping details for IR on the rack.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = SlotToPortMappings.class)) }),
            @ApiResponse(responseCode = "400", description = "No Mappings Found for Rack.")
    })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
    public SlotToPortMappings getMappings() {
        return mappingService.getMappings();
    }

    /**
     * Set slot mapping details for rack.
     *
     * @return {@link SlotToPortMappings} - Slot to Port mappings of IR for the rack.
     *
     * */
    @Operation(summary = "Set IR Slot Mappings.", description = "Set the slot mapping details for IR on the rack.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = SlotToPortMappings.class)) }),
            @ApiResponse(responseCode = "400", description = "Slot not found."),
            @ApiResponse(responseCode = "404", description = "Cannot set mappings for Rack. Please try again.")
    })
    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json")
    public SlotToPortMappings setMappings(@Parameter(description = "All Slot To Port Mappings to set for Rack.") @RequestBody SlotToPortMappings slotToPortMappings) throws IOException, SlotMappingException {
        mappingService.setMappings(slotToPortMappings.getMappings());
        return mappingService.getMappings();
    }

    @Operation(summary = "Delete IR Slot Mappings.", description = "Delete all slot mapping details for IR on the rack.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Slot mappings deleted successfully."),
            @ApiResponse(responseCode = "400", description = "Slot not found."),
            @ApiResponse(responseCode = "404", description = "Cannot delete mappings for Rack. Please try again.")
    })
    @RequestMapping(value = "", method = RequestMethod.DELETE, produces = "application/json")
    public void deleteMappings() throws IOException {
        mappingService.removeMappings();
    }

    /**
     * Get slot mapping details for a given slot on the rack.
     *
     */
    @Operation(summary = "Get IR Slot Mapping for Slot", description = "Get IR slot mapping details for a given slot on the rack.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Slot not found."),
            @ApiResponse(responseCode = "404", description = "No Mappings Found for given slot on the rack.")
    })
    @RequestMapping(value = "/{slot}", method = RequestMethod.GET, produces = "application/json")
    public Map<String, String> getMapping(@Parameter(description = "Slot to get mappings for.") @PathVariable("slot") String slot) throws SlotMappingException {
        Map<String, String> mapping = new HashMap<>();

        String deviceInfo = mappingService.getMapping(slot);
        mapping.put(slot, deviceInfo);

        return mapping;
    }

    /**
     * Set slot mapping details for a given slot on the rack.
     *
     */
    @Operation(summary = "Set IR Slot Mapping for Slot", description = "Set IR slot mapping details for a given slot on the rack.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = SlotToPortMappings.class)) }),
            @ApiResponse(responseCode = "400", description = "Slot not found."),
            @ApiResponse(responseCode = "404", description = "Cannot set mapping for given slot. Please try again.")
    })
    @RequestMapping(value = "/{slot}", method = RequestMethod.POST, produces = "application/json")
    public SlotToPortMappings setMapping(@Parameter(description = "Slot to update mappings on.") @PathVariable("slot") String slot,
                                         @Parameter(description = "Desired Mapping to set for Slot.") @QueryParam("mapping") String mapping) throws IOException, SlotMappingException {
        if (mapping == null) {
            logger.error("Mapping request for slot " + slot + " did not include query param");
            throw new SlotMappingException("Mapping request for slot " + slot + " did not include query param");
        }

        mappingService.setMapping(slot, mapping);
        return mappingService.getMappings();
    }

    /**
     * Delete slot mapping details for a slot on the rack.
     *
    */
    @Operation(summary = "Delete IR Mapping for Slot", description = "Delete IR slot mapping details for a given slot on the rack.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Slot mapping for given slot deleted successfully.",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = SlotToPortMappings.class)) }),
            @ApiResponse(responseCode = "400", description = "Slot not found."),
            @ApiResponse(responseCode = "404", description = "Cannot delete mappings for Rack. Please try again.")
    })
    @RequestMapping(value = "/{slot}", method = RequestMethod.DELETE, produces = "application/json")
    public SlotToPortMappings removeMapping(@Parameter(description = "Slot to delete mappings on.") @PathVariable("slot") String slot) throws IOException, SlotMappingException {
        return mappingService.removeMapping(slot);
    }
}
package com.cats.service;

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

import com.cats.ir.SlotToPortMappings;
import com.cats.ir.exception.SlotMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * Service to handle slot to port mappings.
 */
@Service
public class SlotMappingService {
    private static final Logger logger = LoggerFactory.getLogger(RemoteProcessor.class);

    private SlotToPortMappings slotToPortMappings;

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${slotMappingFilePath}")
    public String slotMappingFilePath;

    @PostConstruct
    private void initializePortMapping() {
        try {
            File f = new File(slotMappingFilePath);
            if (!f.exists()) {
                if (f.getParentFile() != null) {
                    f.getParentFile().mkdirs();
                } else {
                    f.mkdirs();
                }
            }
            slotToPortMappings = mapper.readValue(new File(slotMappingFilePath), SlotToPortMappings.class);
        } catch (IOException ex) {
            logger.error("Could not process slot mappings file, using default values: " + ex.getLocalizedMessage());
            slotToPortMappings = new SlotToPortMappings();
        }
    }

    public SlotToPortMappings getMappings() {
        return slotToPortMappings;
    }

    /**
     * Sets the slot to port mappings.
     *
     * @param mappings the mappings
     * @return the slot to port mappings
     * @throws IOException the io exception
     */
    public SlotToPortMappings setMappings(Map<String, String> mappings) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(slotMappingFilePath))) {
            for (Map.Entry<String, String> entry : mappings.entrySet()) {
                if (!isValidMapping(entry.getValue())) {
                    logger.error("Invalid mapping for slot " + entry.getKey() + ": " + entry.getValue());
                    writer.write(mapper.writeValueAsString(this.slotToPortMappings));
                    throw new SlotMappingException("Invalid mapping for slot " + entry.getKey() + ": " + entry.getValue());
                }
            }
            logger.info("Setting new mapping: " + mapper.writeValueAsString(mappings));

            this.slotToPortMappings.setMappings(mappings);
            writer.write(mapper.writeValueAsString(this.slotToPortMappings));

            logger.info("Slot to port mappings file updated");
            return this.slotToPortMappings;
        } catch (IOException ex) {
            logger.error("Could not update slot mappings: " + ex.getLocalizedMessage());
            throw ex;
        }
    }

    /**
     * Removes all slot to port mappings.
     *
     * @throws IOException the io exception
     */
    public void removeMappings() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(slotMappingFilePath))) {
            logger.info("Removing slot to port mappings");
            this.slotToPortMappings.removeMappings();

            logger.info("Slot to port mappings have been removed");
            writer.write(mapper.writeValueAsString(this.slotToPortMappings));

        } catch (IOException ex) {
            logger.error("Could not update slot mappings: " + ex.getLocalizedMessage());
            throw ex;
        }
    }

    /**
     * Gets the mapping for a slot.
     *
     * @param slot the slot
     * @return the mapping
     * @throws SlotMappingException the slot mapping exception
     */
    public String getMapping(String slot) throws SlotMappingException {
        try {
            return slotToPortMappings.getMapping(slot);
        } catch (SlotMappingException ex) {
            logger.error("Could not locate mapping for slot: " + slot);
            throw ex;
        }
    }

    /**
     * Sets the mapping for a slot.
     *
     * @param slot the slot
     * @param mapping the mapping
     * @return the slot to port mappings
     * @throws IOException the io exception
     * @throws SlotMappingException the slot mapping exception
     */
    public SlotToPortMappings setMapping(String slot, String mapping) throws IOException, SlotMappingException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(slotMappingFilePath))) {
            if (!isValidMapping(mapping)) {
                logger.error("Invalid mapping for slot " + slot + ": " + mapping);
                writer.write(mapper.writeValueAsString(this.slotToPortMappings));
                throw new SlotMappingException("Invalid mapping for slot " + slot + ": " + mapping);
            }
            logger.info("Setting mapping on slot " + slot + " to " + mapping);

            if (this.slotToPortMappings.getMappings().containsKey(slot)) {
                this.slotToPortMappings.removeMapping(slot);
            }
            this.slotToPortMappings.addMapping(slot, mapping);

            writer.write(mapper.writeValueAsString(this.slotToPortMappings));

            logger.info("Slot " + slot + " mapping updated");
            return this.slotToPortMappings;
        } catch (IOException | SlotMappingException ex) {
            logger.error("Could not update slot mappings: " + ex.getLocalizedMessage());
            throw ex;
        }
    }

    /**
     * Removes the mapping for a slot.
     *
     * @param slot the slot
     * @return the slot to port mappings
     * @throws IOException the io exception
     * @throws SlotMappingException the slot mapping exception
     */
    public SlotToPortMappings removeMapping(String slot) throws IOException, SlotMappingException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(slotMappingFilePath))) {
            logger.info("Removing mapping on slot " + slot);
            this.slotToPortMappings.removeMapping(slot);

            writer.write(mapper.writeValueAsString(this.slotToPortMappings));

            logger.info("Slot " + slot + " mapping removed");

            return this.slotToPortMappings;
        } catch (IOException | SlotMappingException ex) {
            logger.error("Could not update slot mappings: " + ex.getLocalizedMessage());
            throw ex;
        }
    }

    /* Checks if a mapping is valid.
     *
     * @param deviceInfo the device info
     * @return true, if is valid mapping
     */
    private boolean isValidMapping(String deviceInfo) {
        try {
            String[] deviceAndPort = deviceInfo.split(":");
            if (Integer.parseInt(deviceAndPort[0]) < 1) return false;
            if (Integer.parseInt(deviceAndPort[1]) < 1) return false;
        } catch (NumberFormatException | IndexOutOfBoundsException ex) {
            return false;
        }
        return true;
    }
}

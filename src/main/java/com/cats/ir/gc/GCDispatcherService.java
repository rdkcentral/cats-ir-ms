package com.cats.ir.gc;

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

import com.cats.ir.gchealth.GCDispatcherHealthBean;
import com.cats.configuration.IRConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * The Class GCDispatcherService.
 * It represents a service to communicate with the GC Dispatcher
 * to retrieve its health status
 */
@Slf4j
@Service
public class GCDispatcherService {

    @Autowired
    IRConfiguration irConfiguration;

    public GCDispatcherHealthBean getHealth() {
        GCDispatcherHealthBean gcDispatcherHealth = new GCDispatcherHealthBean();

        try {
            URL url = new URL(irConfiguration.gcDispatcherApiBase + "/health");
            log.info("Attempting to get health: {}", url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            String response = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);

            if (StringUtils.isNotBlank(response)) {
                gcDispatcherHealth = new ObjectMapper().readValue(response, GCDispatcherHealthBean.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Error while getting health of GC Dispatcher: {}", e.getMessage());
        }

        return gcDispatcherHealth;
    }
}

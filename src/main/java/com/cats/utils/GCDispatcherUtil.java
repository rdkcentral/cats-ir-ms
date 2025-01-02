
package com.cats.utils;

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

import com.cats.ir.exception.IRFailureException;
import com.cats.service.MeasureTimeAdvice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.OffsetDateTime;

/**
 * Utility to handle telnet connections.
 */
@Component
@Slf4j
public class GCDispatcherUtil {

    @Autowired
    private HttpServletResponse httpServletResponse;

    public Boolean sendCommand(String commandString, String expectedResult, String urlEndpoint) throws IRFailureException {
        boolean retVal;

        try {
            URL url = new URL(urlEndpoint + "?" + commandString);
            log.info("Attempting to send command: {}", url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.connect();
            retVal = readResponse(connection, expectedResult, commandString);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Error while sending command to GC Dispatcher: {}", e.getMessage());
            retVal = false;
        }
        return retVal;
    }

    private boolean readResponse(HttpURLConnection connection, String expectedResult, String commandString) throws IOException, IRFailureException {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();
        String response = content.toString();
        log.info("RESPONSE: {}", content);

        if (!response.contains(expectedResult)) {
            log.info("Error Response: " + response);
            if (response.contains("Error")) {
                log.warn("Command {} not valid for key set.", commandString);
                throw new IllegalArgumentException("Command " + commandString + " not valid for key set");
            } else {
                throw new IRFailureException("iTach did not return an expected Result. Command " + commandString
                        + " : Expected Result " + expectedResult + " : returned response " + response);
            }
        }

        try {
            String hwCommandRequestTimeHeader = connection.getHeaderField(MeasureTimeAdvice.HW_COMMAND_REQUEST_TIME_HEADER.toLowerCase());
            String hwCommandResponseTimeHeader = connection.getHeaderField(MeasureTimeAdvice.HW_COMMAND_RESPONSE_TIME_HEADER.toLowerCase());
            String hwCommandDurationHeader = connection.getHeaderField(MeasureTimeAdvice.HW_COMMAND_DURATION_HEADER.toLowerCase());

            httpServletResponse.setHeader(MeasureTimeAdvice.HW_COMMAND_REQUEST_TIME_HEADER, String.valueOf(OffsetDateTime.parse(hwCommandRequestTimeHeader)));
            httpServletResponse.setHeader(MeasureTimeAdvice.HW_COMMAND_RESPONSE_TIME_HEADER, String.valueOf(OffsetDateTime.parse(hwCommandResponseTimeHeader)));
            httpServletResponse.setHeader(MeasureTimeAdvice.HW_COMMAND_DURATION_HEADER, hwCommandDurationHeader);

        }catch(IllegalStateException e){
            log.info("Error while setting response headers: {}", e.getMessage());
        }

        return true;
    }
}

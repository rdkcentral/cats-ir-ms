
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

import java.io.*;

import com.cats.service.MeasureTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utility to handle telnet connections.
 */
@Component
@Slf4j
public class MeasuredTelnetUtil {

    @MeasureTime
    String sendCommand(PrintStream os, String value, InputStream is, String pattern) throws IOException {
        write(os,value);
        return readUntil(is, pattern);
    }


    private void write(PrintStream os, String value) {
        if (null != os) {
            os.println(value);
        }
    }

    private synchronized String readUntil(InputStream is, String pattern) throws IOException {
        String str = null;
        if (is == null) {
            log.warn("Socket Not Connected");
            throw new IOException("Socket Not connected");
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        /**
         * Build string by appending incoming strings to response string.
         */
        String line = "";
        boolean jsonLike = false;
        try {
            while ((line = in.readLine()) != null) {
                if(str == null){
                    str = "";
                }
                /**
                 * RedRatHub doesn't build formal JSON payload so add them on line break.
                 */
                if (line != null) {
                    str += line;
                    /**
                     * Handle weird queries like hq="hub version"
                     * Example Response: RedRatHubCmd (4.17), RedRatHubCore (4.3)
                     */
                    if (line.equals("OK")) {
                        //line.contains("OK") breaks for remote type ROKU
                        //changing to line.equals("OK")
                        //Handle legacy case where only OK would be returned.
                        str = "OK";
                        break;
                    } else if (line.contains("{")) {
                        str += "\n";
                        jsonLike = true;
                    } else if (line.contains("}")) {
                        break;
                    } else if (!jsonLike && line.endsWith(")")) {
                        break;
                    } else if (jsonLike) {
                        str += "\n";
                    } else if (!jsonLike && line.startsWith("Failed")) {
                        break;
                    } else if (pattern.equalsIgnoreCase("LINE")) {
                        //Support reading a single line given this pattern.
                        break;
                    }
                    log.info("ReadUtil[{}]", str);
                }
            }

            log.info("line=[{}]", line);
        } catch (Exception e) {
            log.error("readLine() Exception", e);
            throw e;
        }

        log.debug("readUntil Complete");
        return str;

    }
}

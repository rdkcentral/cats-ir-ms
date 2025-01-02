package com.cats.configuration;

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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GCDispatcherProperties {
    String gcDispatcherConfigLocation;
    InputStream inputStream;

    public String getConfigLocationProperty() throws IOException {

        try {
            Properties properties = new Properties();

            inputStream = getClass().getClassLoader().getResourceAsStream("gc-dispatcher.properties");

            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new FileNotFoundException("Property file '" + properties + "' not found in the classpath");
            }

            gcDispatcherConfigLocation = properties.getProperty("gc.dispatcher.config.location");
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            assert inputStream != null;
            inputStream.close();
        }

        return gcDispatcherConfigLocation;
    }
}

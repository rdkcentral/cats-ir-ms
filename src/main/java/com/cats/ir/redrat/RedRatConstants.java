
package com.cats.ir.redrat;

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

public class RedRatConstants {
    /**
     * The RedRatHub ip and port are available through props file.
     */
    public static final String REDRAT_PROPERTIES_FILE = "redrat.props";
    public static final String REDRAT_HOST_PROPERTY = "redratHubHost";
    public static final String REDRAT_HOST_PORT = "redratHubPort";
    public static final String REDRATHUB_POOL_SIZE = "redrathub.pool.size";

    public static final String DEFAULT_REDRAT_HOST = "localhost";
    public static final int DEFAULT_REDRAT_PORT = 40000;

    public static final int DEFAULT_POOL_SIZE = 1;
    public static final int POOL_WAIT_TIME = 2;                   // sec

    public static final String REDRAT_PROMPT_STRING_1 = "\n";
    public static final String REDRAT_PROMPT_STRING_2 = "}\n";

    /**
     * Max Idle time.
     */
    protected static final long MAX_IDLE_TIME = 5 * 60 * 1000;

}

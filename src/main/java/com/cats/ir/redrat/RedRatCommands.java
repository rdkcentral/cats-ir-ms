
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

/**
 * The commands send to the RedRat Hub to accomplish an ir send
 */
public interface RedRatCommands {
    String IPADDRESS_ARGUMENT = "<ipAddress>";
    String NAME_ARGUMENT = "<name>";
    String KEYSET_ARGUMENT = "<keyset>";
    String KEY_ARGUMENT = "<key>";
    String PORT_ARGUMENT = "<port>";
    String DURATION_ARGUMENT = "<duration>";
    String REPEAT_ARGUMENT = "<repeat>";

    String BLACKLIST_ALL_IRNETBOXES = "hubQuery=\"blacklist all irnetboxes\"";
    String WHITLELIST_IRNETBOX = "hubQuery=\"whitelist redrat\" ip=\"" + IPADDRESS_ARGUMENT + "\"";
    String BLACKLIST_IRNETBOX = "hubQuery=\"blacklist redrat\" ip=\"" + IPADDRESS_ARGUMENT + "\"";
    String ADD_IRNETBOX = "hubQuery=\"add irnetbox\" ip=\"" + IPADDRESS_ARGUMENT + "\"";
    String REMOVE_IRNETBOX = "hubQuery=\"remove irnetbox\" ip=\"" + IPADDRESS_ARGUMENT + "\"";
    String IRNETBOX_IR_COMMAND = "ip=\"" + IPADDRESS_ARGUMENT + "\" dataset=\"" + KEYSET_ARGUMENT
            + "\" signal=\"" + KEY_ARGUMENT + "\" output=\"" + PORT_ARGUMENT
            + "\"";
    String IRNETBOX_REPEAT_COMMAND_APPEND = " repeats=\"" + REPEAT_ARGUMENT + "\"";
    String IRNETBOX_DURATION_COMMAND_APPEND = " duration=\"" + DURATION_ARGUMENT + "\"";
    String REDRAT3_IR_COMMAND = "name=\"" + NAME_ARGUMENT + "\" dataset=\"" + KEYSET_ARGUMENT
            + "\" signal=\"" + KEY_ARGUMENT + "\"";
    String LIST_REDRATS = "hubQuery=\"list redrats\"";
    String LIST_DATASETS = "hubQuery=\"list datasets\"";
}

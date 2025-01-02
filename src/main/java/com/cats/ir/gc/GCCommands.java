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

/**
 * Interface for GC commands.
 * Defines a set of constants representing various command templates
 * and arguments used for sending IR commands.These commands are used
 * to send commands to python-globalcache service in the expected syntax
 * for global cache interpretation.
 */
public interface GCCommands {

    String MODULE_ARGUMENT = "<module>";
    String ID_ARGUMENT = "<id>";
    String FREQUENCY_ARGUMENT = "<frequency>";
    String OFFSET_ARGUMENT = "<offset>";
    String PORT_ARGUMENT = "<ir_port_number>";
    String REPEAT_ARGUMENT = "<repeat>";
    String ON_OFF_ARGUMENT = "<onoff>";
    String KEYSET_ARGUMENT = "<keyset>";
    String KEY_ARGUMENT = "<key>";
    String DURATION_ARGUMENT = "<duration>";
    String REPEATS_ARGUMENT = "<repeats>";

    String SEND_COMMAND = "sendir," + MODULE_ARGUMENT + ":" + PORT_ARGUMENT + "," +
            ID_ARGUMENT + "," + FREQUENCY_ARGUMENT + "," + REPEAT_ARGUMENT + "," + OFFSET_ARGUMENT;
    String PRESS_KEY_COMMAND = "host=" + ID_ARGUMENT + "&ir_port_number=" + PORT_ARGUMENT +
            "&keyset=" + KEYSET_ARGUMENT + "&key=" + KEY_ARGUMENT;
    String PRESS_KEY_DURATION_COMMAND = "&duration=" + DURATION_ARGUMENT;
    String PRESS_KEY_REPEATS_COMMAND = "&repeats=" + REPEATS_ARGUMENT;
    String COMPLETE_COMMAND = "complete," + MODULE_ARGUMENT + ":" + PORT_ARGUMENT + "," + ID_ARGUMENT;

}

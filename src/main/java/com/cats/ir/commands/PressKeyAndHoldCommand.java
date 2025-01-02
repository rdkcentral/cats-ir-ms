
package com.cats.ir.commands;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class PressKeyAndHoldCommand.
 * It represents commands to press and hold a key on an IR remote control
 */
public class PressKeyAndHoldCommand extends PressKeyCommand {

    /**
     * Repeat count;
     */
    Integer count = 0;
    Integer duration = 0;
    Integer mode = REPEAT_MODE;

    public static final String COMMAND_NAME = "PressKeyAndHold";
    public static final Integer REPEAT_MODE = 1;
    public static final Integer DURATION_MODE = 2;

    private static final Logger logger = LoggerFactory.getLogger(PressKeyAndHoldCommand.class);

    /**
     * Constructor
     *
     * @param command
     * @param irKeySet
     * @param countOrDuration
     * @param mode
     */
    public PressKeyAndHoldCommand(String command, String irKeySet, Integer countOrDuration, Integer mode) {
        super(command, irKeySet);
        setName(COMMAND_NAME);
        if (mode != null) {
            this.mode = mode;

            if (mode == REPEAT_MODE) {
                setCount(countOrDuration);
            } else if (mode == DURATION_MODE) {
                setDuration(countOrDuration);
            }
        }
        logger.trace("PressKeyAndHoldCommand countOrDuration " + countOrDuration);
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        if (count < 0) {
            count = 0;
        }
        this.count = count;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        if (duration < 0) {
            duration = 0;
        }
        this.duration = duration;
    }

    public Integer getMode() {
        return mode;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }
}

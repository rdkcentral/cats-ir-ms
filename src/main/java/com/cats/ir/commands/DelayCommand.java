
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
 * Represents a command to add delay/sleep.
 *
 */
public class DelayCommand extends CatsCommand {
    /**
     * Delay in ms.
     */
    int delay;
    /**
     * default logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(DelayCommand.class);

    /**
     * Delay in milliseconds.
     *
     * @param delay
     */
    public DelayCommand(int delayMs) {
        super("Delay Command");
        setDelay(delayMs);
        logger.trace("DelayCommand created " + delay);
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        if (delay < 0) {
            delay = 0;
        }
        this.delay = delay;
    }
}
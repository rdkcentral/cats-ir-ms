
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
 * The Class IrCommand.
 * It represents all IrCommands.
 */
public abstract class IrCommand extends CatsCommand {
    /**
     * A remoteCommand that is represented in this IrCommand.
     */
    String remoteCommand;
    /**
     * The remote type.
     */
    String irKeySet;
    /**
     * default logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(IrCommand.class);

    public IrCommand(String name, String remoteCommand, String irKeySet) {
        super(name);
        this.remoteCommand = remoteCommand;
        this.irKeySet = irKeySet;
        logger.trace("IrCommand name " + name + " remoteCommand " + remoteCommand + " irKeySet " + irKeySet);
    }

    public String getRemoteCommand() {
        return remoteCommand;
    }

    public void setRemoteCommand(String remoteCommand) {
        this.remoteCommand = remoteCommand;
    }

    public String getIrKeySet() {
        return irKeySet;
    }

    public void setIrKeySet(String irKeySet) {
        this.irKeySet = irKeySet;
    }
}

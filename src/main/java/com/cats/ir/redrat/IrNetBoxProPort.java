
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

import static com.cats.ir.redrat.RedRatCommands.DURATION_ARGUMENT;
import static com.cats.ir.redrat.RedRatCommands.IRNETBOX_DURATION_COMMAND_APPEND;
import static com.cats.ir.redrat.RedRatCommands.IRNETBOX_REPEAT_COMMAND_APPEND;
import static com.cats.ir.redrat.RedRatCommands.REPEAT_ARGUMENT;

import com.cats.ir.commands.CatsCommand;
import com.cats.ir.commands.PressKeyAndHoldCommand;
import com.cats.ir.commands.PressKeyCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cats.ir.IRCommunicatorManager;

/**
 * Represents a port of the IrNetBox device.
 */
public class IrNetBoxProPort extends RedRatDevicePort {

    private static final Logger logger = LoggerFactory.getLogger(IrNetBoxProPort.class);

    public IrNetBoxProPort(int portNumber, IrNetBoxPro irDevice, IRCommunicatorManager manager) {
        super(irDevice, manager);
        this.portNumber = portNumber;
    }

    protected String getDeviceUnderstandablePressKeyCommand(CatsCommand command) {
        String commandString = null;

        if (command instanceof PressKeyCommand) {
            PressKeyCommand pressKeyCommand = (PressKeyCommand) command;
            commandString = RedRatCommands.IRNETBOX_IR_COMMAND
                    .replace(RedRatCommands.IPADDRESS_ARGUMENT, device.getDeviceIp())
                    .replace(RedRatCommands.KEYSET_ARGUMENT, pressKeyCommand.getIrKeySet())
                    .replace(RedRatCommands.PORT_ARGUMENT, String.valueOf(portNumber));

            String key = pressKeyCommand.getRemoteCommand().toString();
            if (command instanceof PressKeyAndHoldCommand) {
                if (((PressKeyAndHoldCommand) command).getMode() == PressKeyAndHoldCommand.REPEAT_MODE) {
                    logger.debug("PressKeyAndHoldCommand.REPEAT_MODE ");
                    commandString = commandString.concat(IRNETBOX_REPEAT_COMMAND_APPEND).replace(REPEAT_ARGUMENT,
                            ((PressKeyAndHoldCommand) command).getCount().toString());
                } else if (((PressKeyAndHoldCommand) command).getMode() == PressKeyAndHoldCommand.DURATION_MODE) {
                    logger.debug("PressKeyAndHoldCommand.DURATION_MODE");
                    commandString = commandString.concat(IRNETBOX_DURATION_COMMAND_APPEND).replace(
                            DURATION_ARGUMENT, "" + (((PressKeyAndHoldCommand) command).getDuration() * 1000));
                    logger.info("PressKeyAndHoldCommand.DURATION_MODE commandString " + commandString);
                }
            }
            commandString = commandString.replace(RedRatCommands.KEY_ARGUMENT, key);
            logger.debug("Sending command[{}]  to device[{}]", commandString, device);
        }
        return commandString;
    }

}

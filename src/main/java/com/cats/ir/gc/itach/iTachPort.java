package com.cats.ir.gc.itach;

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

import com.cats.ir.commands.CatsCommand;
import com.cats.ir.commands.PressKeyAndHoldCommand;
import com.cats.ir.commands.PressKeyCommand;
import com.cats.ir.gc.GCCommands;
import com.cats.ir.gc.GCDevicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class iTachPort.
 * This class is to manage and interact with the Global Cache device port.
 * Provides methods to manage and interact with the device, including retrieving
 * device ports and handling device-specific configurations.
 */
public class iTachPort extends GCDevicePort {

    private static final Logger logger = LoggerFactory.getLogger( iTachPort.class );

    public iTachPort(int portNumber, iTach irDevice) {
        super(irDevice);
        this.portNumber = portNumber;
    }

    /**
     * Gets the device understandable press key command.
     *
     * @param command the command
     * @return the device understandable press key command
     */
    @Override
    protected String getDeviceUnderstandablePressKeyCommand(CatsCommand command) {
        String commandStr = null;

        if(command instanceof PressKeyCommand){
            PressKeyCommand pressKeyCommand = (PressKeyCommand) command;
            String key = pressKeyCommand.getRemoteCommand().replace(" ","_").toUpperCase();

            commandStr = GCCommands.PRESS_KEY_COMMAND
                    .replace( GCCommands.ID_ARGUMENT, device.getDeviceIp() )
                    .replace( GCCommands.PORT_ARGUMENT, String.valueOf(portNumber) )
                    .replace( GCCommands.KEYSET_ARGUMENT, pressKeyCommand.getIrKeySet().toUpperCase() )
                    .replace( GCCommands.KEY_ARGUMENT, key );

            if(command instanceof PressKeyAndHoldCommand){
                if (((PressKeyAndHoldCommand) command).getMode().equals(PressKeyAndHoldCommand.REPEAT_MODE)) {
                    commandStr = handleRepeats(( ( PressKeyAndHoldCommand ) command ).getCount(), commandStr);
                }
                else if (((PressKeyAndHoldCommand) command).getMode().equals(PressKeyAndHoldCommand.DURATION_MODE))
                {
                    commandStr = handleDuration(( ( PressKeyAndHoldCommand ) command ).getDuration(), commandStr);
                }
            }
        }
        return commandStr;
    }

    /**
     * Processes a command string to include a repeat count for a
     * PressKeyAndHoldCommand in REPEAT_MODE.
     *
     * @param repeatCount the repeat count
     * @param commandString the command string
     * @return the string
     */
    private String handleRepeats(Integer repeatCount, String commandString){
        logger.debug( "PressKeyAndHoldCommand.REPEAT_MODE " );
        commandString = commandString.concat( GCCommands.PRESS_KEY_REPEATS_COMMAND ).replace( GCCommands.REPEATS_ARGUMENT, repeatCount.toString() );
        logger.info( "PressKeyAndHoldCommand.REPEATS_MODE commandString " + commandString );

        return commandString;
    }

    /**
     Processes a command string to include a duration for a PressKeyAndHoldCommand
     in DURATION_MODE.
     *
     * @param duration the duration
     * @param commandString the command string
     * @return the string
     */
    private String handleDuration(Integer duration, String commandString){
        logger.debug( "PressKeyAndHoldCommand.DURATION_MODE" );
        commandString = commandString.concat( GCCommands.PRESS_KEY_DURATION_COMMAND ).replace(
                GCCommands.DURATION_ARGUMENT, "" + ( duration * 1000 ) );
        logger.info( "PressKeyAndHoldCommand.DURATION_MODE commandString " + commandString );

        return commandString;
    }
}

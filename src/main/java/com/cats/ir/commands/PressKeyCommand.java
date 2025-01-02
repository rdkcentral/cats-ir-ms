
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

/**
 * The Class PressKeyCommand.
 * It represents commands to press key on IR remote control
 */
public class PressKeyCommand extends IrCommand {
    private static final String COMMAND_NAME = "PressKey";

    public PressKeyCommand(String command, String irKeySet) {
        super(COMMAND_NAME, command, irKeySet);
    }
}
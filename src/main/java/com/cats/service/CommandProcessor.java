package com.cats.service;

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

import java.util.ArrayList;
import java.util.List;

/**
 * Process string into series of commands based on default mappings.
 */
public class CommandProcessor {
    public static List<String> commandFromSequence(String shortHand) {
        List<String> commands = new ArrayList<>();
        for (char c : shortHand.toCharArray()) {
            switch (c) {
                case '0':
                    commands.add("ZERO");
                    break;
                case '1':
                    commands.add("ONE");
                    break;
                case '2':
                    commands.add("TWO");
                    break;
                case '3':
                    commands.add("THREE");
                    break;
                case '4':
                    commands.add("FOUR");
                    break;
                case '5':
                    commands.add("FIVE");
                    break;
                case '6':
                    commands.add("SIX");
                    break;
                case '7':
                    commands.add("SEVEN");
                    break;
                case '8':
                    commands.add("EIGHT");
                    break;
                case '9':
                    commands.add("NINE");
                    break;

                case 'U':
                    commands.add("UP");
                    break;
                case 'D':
                    commands.add("DOWN");
                    break;
                case 'L':
                    commands.add("LEFT");
                    break;
                case 'R':
                    commands.add("RIGHT");
                    break;

                case 'M':
                    commands.add("MENU");
                    break;
                case 'G':
                    commands.add("GUIDE");
                    break;

                case 'X':
                    commands.add("EXIT");
                    break;
                case 'S':
                    commands.add("SEARCH");
                    break;
                case 'I':
                    commands.add("INFO");
                    break;

                case 'O':
                    commands.add("OK");
                    break;
                case 'P':
                    commands.add("PLAY");
                    break;
                case 'C':
                    commands.add("REC");
                    break;

                case '[':
                    commands.add("CHDN");
                    break;
                case ']':
                    commands.add("CHUP");
                    break;

                case '<':
                    commands.add("PGDN");
                    break;
                case '>':
                    commands.add("PGUP");
                    break;

                case '~':
                    commands.add("LAST");
                    break;
                case '!':
                    commands.add("MUTE");
                    break;

                case 'a':
                case 'b':
                case 'c':
                case 'd':
                    commands.add(Character.toString(c).toUpperCase());
                    break;
                default:
                    break;
            }
        }
        return commands;
    }
}

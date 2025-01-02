
package com.cats.ir;

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

import java.util.List;

/**
 Interface defining the remote control system used by the IRDevicePorts to have a
 * consistent set of operations for interacting with the RedRatHub.
 */
public interface Remote {
    /**
     * Constant for the maximum delay of 30000ms
     */
    Integer MAX_DELAY = 30000;

    Integer getDelay();

    String getRemoteType();

    boolean isAutoTuneEnabled();

    boolean performShorthandCommandSequence(String text);

    boolean performShorthandCommandSequence(String text, Integer delay);

    boolean pressKey(String command);

    boolean pressKey(String command, Integer delay);

    boolean pressKey(String[] commands);

    boolean pressKey(Integer count, String command);

    boolean pressKey(Integer count, String command, Integer delay);

    boolean pressKey(Integer count, Integer delay, String[] commands);

    boolean pressKey(Integer command);

    boolean pressKeyAndHold(String command, Integer count);

    boolean pressKeyAndHoldDuration(String command, Integer durationSec);

    boolean pressKeys(List<String> commands);

    boolean pressKeys(List<String> commands, Integer delay);

    boolean sendText(String text);

    void setAutoTuneEnabled(boolean autoTuneEnabled);

    void setDelay(Integer delay);

    void setRemoteType(String remoteType);

    boolean tune(Integer channel);

    boolean tune(Integer channel, Integer delay);

    boolean tune(String channel);

    boolean tune(String channel, Integer delay);

}

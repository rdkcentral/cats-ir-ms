
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import com.cats.ir.commands.CatsCommand;
import com.cats.ir.commands.DelayCommand;
import com.cats.ir.commands.PressKeyAndHoldCommand;
import com.cats.ir.commands.PressKeyCommand;
import com.cats.ir.exception.IRCommunicatorNotInitializedException;
import com.cats.ir.exception.IRFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic Local remote implementation bound against a IRDevicePort.
 */
public class LocalRemote implements Remote {
    private static final long serialVersionUID = -4973165057686246850L;
    private static final int DELAY_BETWEEN_KEYS = 100;
    public static final Logger logger = LoggerFactory.getLogger(LocalRemote.class);
    private Integer delay = 0;
    private boolean autoTuneEnabled = false;
    private final IRDevicePort devicePort;
    private String remoteType;

    /**
     * The regular expression that should be used to validate a direct tune
     * channel.
     */
    private static final Pattern CHANNEL_VALIDATOR = Pattern.compile("\\d{1,4}");

    private static final Integer REPEAT_IR_COMMAND_DELAY = 500;

    public LocalRemote(IRDevicePort devicePort) {
        this.devicePort = devicePort;
    }

    public LocalRemote(IRDevicePort devicePort, String remoteType) {
        this.devicePort = devicePort;
        this.remoteType = remoteType;
    }

    private boolean mustDelay() {
        return (delay > 0);
    }

    private boolean sleepOnTrue(boolean condition) {
        if (condition && mustDelay()) {
            sleep();
        }
        return condition;
    }

    /**
     * Verifies that the delay is within the acceptable range.
     *
     * @param delay The delay to verify.
     */
    protected void verifyDelay(int delay) {
        if (delay < 0 || delay > MAX_DELAY) {
            throw new IllegalArgumentException("Remote delay must be 0 >= delay <= " + MAX_DELAY);
        }
    }

    /**
     * Sleeps for the specified delay.
     *
     * @param delay The delay to sleep for.
     */
    protected void sleep(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            logger.warn("Interrupted during sleep operation.");
        }
    }

    protected void sleep() {
        sleep(this.delay);
    }

    @Override
    public String getRemoteType() {
        return remoteType;
    }

    /**
     * Sets the remote type.
     *
     * @param remoteType The remote type to set.
     * @throws IllegalArgumentException If the remote type is null or empty.
     */
    @Override
    public void setRemoteType(String remoteType) throws IllegalArgumentException {
        if (remoteType == null || remoteType.isEmpty()) {
            throw new IllegalArgumentException("RemoteType cant be null");
        }
        this.remoteType = remoteType;
    }

    /**
     * Checks if autoTune enabled.
     *
     * @return true if enabled.
     */
    @Override
    public boolean isAutoTuneEnabled() {
        return autoTuneEnabled;
    }

    /**
     * Sets the auto tune enabled.
     *
     * @param autoTuneEnabled The auto tune enabled to set.
     */
    @Override
    public void setAutoTuneEnabled(boolean autoTuneEnabled) {
        this.autoTuneEnabled = autoTuneEnabled;
    }

    /**
     * Presses a key on the remote.
     *
     * @param command The command to press.
     * @return true if the key was pressed successfully.
     */
    @Override
    public boolean pressKey(String command) {
        logger.info("RedRatIRServiceHandler pressKey " + " irKeySet " + remoteType + " command " + command);
        boolean response = false;
        if (devicePort != null && command != null) {
            logger.debug("irPort " + devicePort);
            PressKeyCommand pressKeyCommand = new PressKeyCommand(command, remoteType);
            try {
                response = devicePort.sendCommand(pressKeyCommand);
            } catch (IRFailureException | IRCommunicatorNotInitializedException e) {
                logger.error("Failed to send command, response:{} ", e);
                e.printStackTrace();
            }
            if (!response) {
                logger.warn("Ir operation on :{} returned :{} ", devicePort, response);
            }
        }
        return response;
    }

    /**
     * Presses a key on the remote and waits for the specified delay.
     *
     * @param command The command to press.
     * @param delay   The delay to wait after pressing the key.
     * @return true if the key was pressed successfully.
     */
    @Override
    public boolean pressKey(String command, Integer delay) {
        verifyDelay(delay);
        boolean rtn = pressKey(command);
        sleep(delay);
        return rtn;
    }


    /**
     * Presses and hold a key on an IR remote control for a specified number of times
     *
     * @param command The command to press.
     * @param count  The number of times to press the key.
     * @return true if the key was pressed successfully.
     */
    @Override
    public boolean pressKeyAndHold(String command, Integer count) {
        boolean retVal = false;
        if (devicePort != null && command != null) {
            PressKeyAndHoldCommand pressKeyHoldCommand = new PressKeyAndHoldCommand(command, remoteType, count,
                    PressKeyAndHoldCommand.REPEAT_MODE);
            try {
                retVal = devicePort.sendCommand(pressKeyHoldCommand);
                sleepOnTrue(retVal);
            } catch (IRFailureException | IRCommunicatorNotInitializedException e) {
                logger.error("Failed to send command, response:{}", e);
            }
        }
        return retVal;
    }

    /**
     * Sends a sequence of IR commands to the device.
     *
     * @param commands list of commands to press.
     * @return true if the key was pressed successfully.
     */
    @Override
    public boolean pressKeys(List<String> commands) {
        boolean retVal = false;
        if (devicePort != null && commands != null) {
            CatsCommand catsCommand = new CatsCommand("PressKeys");
            for (String command : commands) {
                catsCommand.add(new PressKeyCommand(command, remoteType)).add(new DelayCommand(0));
            }
            try {
                retVal = devicePort.sendCommand(catsCommand);
                sleepOnTrue(retVal);
            } catch (IRFailureException | IRCommunicatorNotInitializedException e) {
                logger.error("Failed to send command, response: {} ", e);
            }
        }
        return retVal;
    }

    /**
     * Sends a sequence of IR commands to the device, holds for the delay time between each command and
     * sends command again.
     *
     * @param commands list of commands to press.
     * @param delay    The delay to wait after pressing the key.
     * @return true if the key was pressed successfully.
     */
    @Override
    public boolean pressKeys(List<String> commands, Integer delay) {
        boolean retVal = false;
        if (devicePort != null && commands != null) {
            CatsCommand catsCommand = new CatsCommand("PressKeys");
            for (String command : commands) {
                catsCommand.add(new PressKeyCommand(command, remoteType)).add(new DelayCommand(delay));
            }
            try {
                retVal = devicePort.sendCommand(catsCommand);
                sleepOnTrue(retVal);
            } catch (IRFailureException | IRCommunicatorNotInitializedException e) {
                logger.error("Failed to send command, response:{} ", e);
            }
        }
        return retVal;
    }

    /**
     *Tunes the device to a specified channel.
     *
     * @param channel: The channel number to tune to.
     *
     * @return true if the key was pressed successfully.
     */
    public boolean tune(String channel) {
        return tune(channel, DELAY_BETWEEN_KEYS);
    }

    @Override
    public void setDelay(Integer delay) {
        verifyDelay(delay);
        this.delay = delay;
    }

    @Override
    public Integer getDelay() {
        return delay;
    }

    @Override
    public boolean pressKey(String[] commands) {
        return pressKeys(Arrays.asList(commands));
    }

    @Override
    public boolean pressKey(Integer count, String command) {
        boolean toReturn = true;
        for (int i = 0; i < count; i++) {
            toReturn = pressKey(command);
            if (false == toReturn) {
                break;
            }
        }
        return toReturn;
    }

    @Override
    public boolean pressKey(Integer count, String command, Integer delay) {
        verifyDelay(delay);
        boolean toReturn = true;
        for (int i = 0; i < count; i++) {
            toReturn = pressKey(command);
            if (false == toReturn) {
                break;
            }
            sleep(delay);
        }
        return toReturn;
    }

    @Override
    public boolean pressKey(Integer count, Integer delay, String[] commands) {
        verifyDelay(delay);
        boolean toReturn = true;
        for (int i = 0; i < count; i++) {
            toReturn = pressKeys(Arrays.asList(commands), delay);
            if (false == toReturn) {
                break;
            }
            sleep(delay);
        }
        return toReturn;
    }

    @Override
    public boolean pressKey(Integer command) {
        return pressKey(String.valueOf(command));
    }

    public boolean tune(Integer channel) {
        return tune(channel.toString(), DELAY_BETWEEN_KEYS);
    }

    @Override
    public boolean sendText(String text) {
        boolean returnVal = false;
        try {
            if (null != text && !text.isEmpty()) {
                char[] digits = text.toCharArray();
                List<String> commands = new ArrayList<String>();
                for (char digit : digits) {
                    commands.add(Character.toString(digit));

                }
                returnVal = pressKeys(commands, REPEAT_IR_COMMAND_DELAY);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            logger.warn("sendText exception " + e.getMessage());
        }
        return returnVal;
    }

    private void verifyChannelNumber(String channel) {
        if (!CHANNEL_VALIDATOR.matcher(channel).matches()) {
            throw new IllegalArgumentException("Invalid channel number: " + channel);
        }
    }

    public List<String> getAllRemoteTypes() {
        return null;
    }

    @Override
    public boolean performShorthandCommandSequence(String text) {
        return performShorthandCommandSequence(text, DELAY_BETWEEN_KEYS);
    }

    @Override
    public boolean performShorthandCommandSequence(String text, Integer delay) {
        throw new UnsupportedOperationException(
                "AbstractLocalRemote.performShorthandCommandSequence() is not supported");
    }

    public static String parse(char digit) {
        switch (digit) {
            case '0':
                return "ZERO";
            case '1':
                return "ONE";
            case '2':
                return "TWO";
            case '3':
                return "THREE";
            case '4':
                return "FOUR";
            case '5':
                return "FIVE";
            case '6':
                return "SIX";
            case '7':
                return "SEVEN";
            case '8':
                return "EIGHT";
            case '9':
                return "NINE";
        }
        return null;
    }

    private List<String> getRemoteCommandFromChannel(String channel) {
        if (!CHANNEL_VALIDATOR.matcher(channel).matches()) {
            return null;
        }
        char[] digits = channel.toCharArray();
        List<String> commands = new ArrayList<>();
        for (char digit : digits) {
            commands.add(parse(digit));
        }
        return commands;
    }

    @Override
    public boolean pressKeyAndHoldDuration(String command, Integer durationSec) {
        boolean retVal = false;
        if (devicePort != null && command != null) {
            PressKeyAndHoldCommand pressKeyHoldCommand = new PressKeyAndHoldCommand(command, remoteType, durationSec,
                    PressKeyAndHoldCommand.DURATION_MODE);
            try {
                retVal = devicePort.sendCommand(pressKeyHoldCommand);
                sleepOnTrue(retVal);
            } catch (IRFailureException | IRCommunicatorNotInitializedException e) {
                logger.error("Failed to send command, response:{} ", e);
            }
        }
        return retVal;
    }

    @Override
    public boolean tune(String channel, Integer delay) {
        verifyChannelNumber(channel);
        boolean retVal = false;
        if (channel != null) {
            List<String> commands = getRemoteCommandFromChannel(channel);
            if (commands == null || commands.isEmpty()) {
                return false;
            }
            if (!autoTuneEnabled) {
                commands.add("SELECT");
            }
            retVal = pressKeys(commands, delay);
        }

        return retVal;
    }

    @Override
    public boolean tune(Integer channel, Integer delay) {
        return tune(channel.toString(), delay);
    }
}

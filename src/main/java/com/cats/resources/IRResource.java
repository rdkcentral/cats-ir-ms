package com.cats.resources;

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
import java.util.StringTokenizer;

import com.cats.configuration.IRDeviceConfig;
import com.cats.ir.Remote;
import com.cats.ir.exception.CustomBadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.DefaultValue;

import com.cats.service.CommandProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cats.configuration.IRConfiguration;
import com.cats.service.RemoteProcessor;

/**
 * Defines REST API paths and actions to make IR requests to IrNetBox ports.
 */
@RestController
@Tag(name = "REST API paths and actions", description = "Defines REST API paths and actions to make IR requests to IrNetBox ports.")
@RequestMapping("/rest/slot/{slot}")
public class IRResource {

    @Autowired
    RemoteProcessor processor;
    @Autowired
    IRConfiguration irconfig;
    private final Integer MAX_DELAY = 30 * 1000; // 30 seconds in milliseconds
    //if read time out is set at 15000 we would support 15000/100 -50 repeat count
    private final Integer REPEATCOUNT_READTIMEOUT_OFFSET = 30;
    private int MAX_REPEAT_COUNT = 100;

    @PostConstruct
    public void init() {
        MAX_REPEAT_COUNT = Integer.parseInt(irconfig.getRedRatHubReadTimeout()) / 100 - REPEATCOUNT_READTIMEOUT_OFFSET;
    }

    /**
     * Gets a LocalRemote with a connection to a port of an IRNetBox device.
     *
     * @return a LocalRemote object.
     */
    private Remote getRemote(String slot, String keySet) {
        return processor.getRemote(slot, keySet);
    }

    private void validateSlot(String slot) {
        if (!processor.validateSlot(slot)) {
            throw new CustomBadRequestException("Requested slot is not valid.");
        }
    }

    /**
     * Get general path param info.
     *
     * @return the requested device rack and slot.
     */
    @Operation(summary = "Get path info", description = "Get general path param information.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful",
                    content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "400", description = "No Mappings Found for Rack."),
            @ApiResponse(responseCode = "404", description = "Device/Port not found.")
    })
    @RequestMapping(value = "/get", method = RequestMethod.GET, produces = {"text/plain","application/json"})
    public ResponseEntity<String> get(@Parameter(description = "Device Slot.") @PathVariable("slot") String slot,
                                      @Parameter(description = "Device rack.") @PathVariable("rack") String rack) {
        validateSlot(slot);
        return ResponseEntity.ok("IR [Rack=" + rack + ", Slot=" + slot + "]");
    }

    /**
     * Perform IR key press for a given keyset and remote command.
     *
     * @return true if remote command operation is successful, false otherwise.
     */
    @Operation(summary = "Perform IR key press", description = "Perform IR key press for a given keyset and remote command.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful",
                    content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid Request."),
            @ApiResponse(responseCode = "404", description = "Device/Port not found.")
    })
    @RequestMapping(value = "/pressKey", method = RequestMethod.POST, produces = {"text/plain","application/json"})
    public ResponseEntity<String> pressKey(@Parameter(description = "Device Slot.") @PathVariable("slot") String slot,
                                           @Parameter(description = "KeySet to use for sending command to the device.") @RequestParam String keySet,
                                           @Parameter(description = "Command passed to the device.") @RequestParam String command) {
        ResponseEntity<String> resp;
        Remote remote;
        boolean keyPressOk;

        validateSlot(slot);
        remote = getRemote(slot, keySet);
        try {
            keyPressOk = remote.pressKey(command);
            if (keyPressOk) {
                resp = ResponseEntity.ok(Boolean.TRUE.toString());
            } else {
                resp = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (IllegalArgumentException e) {
            resp = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Boolean.FALSE.toString());
        }

        return resp;
    }

    /**
     * Performs IR key presses.
     *
     * @return true if remote operation is successful, false if otherwise.
     */
    @Operation(summary = "Performs IR key presses.", description = "Performs IR key presses.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful",
                    content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid Request. Please check device details and retry request."),
            @ApiResponse(responseCode = "404", description = "Device/Port not found.")
    })
    @RequestMapping(value = "/pressKeys", method = RequestMethod.POST, produces = {"text/plain","application/json"})
    public ResponseEntity<String> pressKeys(@Parameter(description = "Device Slot.") @PathVariable("slot") String slot,
                                            @Parameter(description = "KeySet to use for sending command to the device.") @RequestParam("keySet") String keySet,
                                            @Parameter(description = "List of commands passed to the device.") @RequestParam("commandList") String commandList,
                                            @Parameter(description = "Delay in milliseconds.") @RequestParam("delayInMillis") Integer delay) {
        ResponseEntity<String> resp;
        Remote remote;
        boolean keyPressOk;

        validateSlot(slot);
        remote = getRemote(slot, keySet);
        keyPressOk = remote.pressKey(commandList, delay);
        if (keyPressOk) {
            resp = ResponseEntity.ok(Boolean.TRUE.toString());
        } else {
            resp = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Boolean.FALSE.toString());
        }
        return resp;
    }

    /**
     * Perform IR key press and hold key for specified time.
     *
     */
    @Operation(summary = "Performs IR key press and hold.", description = "Perform IR key press and hold key for specified time.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful",
                    content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid Request. Please check device ID and retry request."),
            @ApiResponse(responseCode = "404", description = "Device/Port not found.")
    })
    @RequestMapping(value = "/pressKeyAndHold", method = RequestMethod.POST, produces = {"text/plain","application/json"})
    public ResponseEntity<String> pressKeyAndHold(@Parameter(description = "Device Slot.") @PathVariable("slot") String slot,
                                                  @Parameter(description = "KeySet to use for sending command to the device.") @RequestParam("keySet") String keySet,
                                                  @Parameter(description = "Command passed to the device.") @RequestParam("command") String command,
                                                  @Parameter(description = "Time for which key is held during key press.") @RequestParam("holdTime") String holdTime) {
        ResponseEntity<String> resp;
        Remote remote;
        boolean keyPressOk;

        validateSlot(slot);
        if (!isStringArgValid(keySet, command, holdTime)) {
            throw new CustomBadRequestException("Arguments are either empty or null. keySet: " + keySet + ", command: "
                    + command + ", holdTime: " + holdTime);
        }

        if (!holdTime.matches("\\d+")) {
            throw new CustomBadRequestException("HoldTime is not numeric.");
        }
        if (Integer.parseInt(holdTime) > MAX_REPEAT_COUNT) {
            throw new CustomBadRequestException("Unable to process pressAndHold repeat count greater than " + MAX_REPEAT_COUNT);
        }

        remote = getRemote(slot, keySet);
        keyPressOk = remote.pressKeyAndHold(command, Integer.parseInt(holdTime));
        if (keyPressOk) {
            resp = ResponseEntity.ok(Boolean.TRUE.toString());
        } else {
            resp = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Boolean.FALSE.toString());
        }
        return resp;
    }

    /**
     * Press IR key and hold it for a specified duration.
     */
    @Operation(summary = "Perform key press and hold.", description = "Press IR key and hold it for a specified duration.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful",
                    content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid Request. Please check device details and retry request."),
            @ApiResponse(responseCode = "404", description = "Device/Port not found.")
    })
    @RequestMapping(value = "/pressKeyAndHoldDuration", method = RequestMethod.POST, produces = {"text/plain","application/json"})
    public ResponseEntity<String> pressKeyAndHoldDuration(@Parameter(description = "Device Slot.") @PathVariable("slot") String slot,
                                                          @Parameter(description = "KeySet to use for sending command to the device.") @RequestParam("keySet") String keySet,
                                                          @Parameter(description = "Command passed to the device.") @RequestParam("command") String command,
                                                          @Parameter(description = "Time for which key is held during key press.") @RequestParam("holdTime") Integer durationSec) {
        ResponseEntity<String> resp;
        Remote remote;
        boolean keyPressOk;

        validateSlot(slot);
        if (!isStringArgValid(keySet, command)) {
            throw new CustomBadRequestException(
                    "Arguments are either null or empty- keySet: " + keySet + ", command: " + command);
        }

        if (durationSec == null) {
            throw new CustomBadRequestException("holdTime is null. Include this parameter in your next request.");
        }
        int readTimeout = Integer.getInteger("readrat.read.timeout");
        if (durationSec >= readTimeout / 1000) {
            throw new CustomBadRequestException(
                    "Cannot process reqeust,pressKey duration is greater than or equal to the configured read time out");
        }

        remote = getRemote(slot, keySet);
        keyPressOk = remote.pressKeyAndHoldDuration(command, durationSec);
        if (keyPressOk) {
            resp = ResponseEntity.ok(Boolean.TRUE.toString());
        } else {
            resp = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Boolean.FALSE.toString());
        }
        return resp;
    }

    /**
     * Press the sequence of keys with specified repeat count and delays in between.
     * This delay will not account for delay associated with physical hardware sending the key.
     *
     * @param keySet      keyset/remote type to be specified.
     * @param commands    List of keys to be sent.
     * @param delay       Delay between each of the keys.
     * @param repeatCount Repeat counts of each of the keys.
     * @return true if all remote command operations are successful, false at the first failure.
     */
    @Operation(summary = "Perform key press and hold for specified duration with delays.", description = "Press the sequence of keys with specified repeat count and delays in between.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful",
                    content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid Request. Please check device details and retry request."),
            @ApiResponse(responseCode = "404", description = "Device/Port not found.")
    })
    @RequestMapping(value = "/customKeySeq", method = RequestMethod.POST, produces = {"text/plain","application/json"})
    public ResponseEntity<String> enterCustomKeySequence(@Parameter(description = "Device Slot.") @PathVariable("slot") String slot,
                                                         @Parameter(description = "KeySet to use for sending command to the device.") @RequestParam("keySet") String keySet,
                                                         @Parameter(description = "List of commands passed to the device.") @RequestParam("commands") String commands,
                                                         @Parameter(description = "Delay between each key press.") @RequestParam("delay") String delay,
                                                         @Parameter(description = "Repeat counts of each of the keys.") @RequestParam("repeatCount") String repeatCount) {
        ResponseEntity<String> resp;
        Remote remote;
        boolean keyPressOk = false;

        validateSlot(slot);
        if (!isStringArgValid(keySet, commands, repeatCount, delay)) {
            throw new CustomBadRequestException("Arguments are either null or empty- keySet: " +
                    keySet + ", commandList: " + commands + ", repeatCount: " + repeatCount +
                    ", delay: " + delay);
        }

        remote = getRemote(slot, keySet);
        List<String> commandList = getRemoteCommandList(commands);
        List<Integer> delayList = getIntegerList(delay);
        List<Integer> countList = getIntegerList(repeatCount);
        for (Integer count : countList) {
            if (count > MAX_REPEAT_COUNT) {
                throw new CustomBadRequestException("One of the repeat counts is greater than the max allowed value :" + MAX_REPEAT_COUNT);
            }
        }

        // Checking integrity of input arguments.
        if (!commandList.isEmpty() && commandList.size() == delayList.size() &&
                commandList.size() == countList.size()) {
            // Verify that any delays are not greater than max delay that would hang the system up.
            for (int delayMs : delayList) {
                if (delayMs > MAX_DELAY) {
                    throw new CustomBadRequestException("One of the delays is greater than the max delay supported. Delay: " +
                            delayMs + "ms. and max delay possible is: " + MAX_DELAY + "ms.");
                }
            }
        } else {
            throw new CustomBadRequestException("Input arguments are empty or not of equal size. " +
                    "commandList.size: " + commandList.size() + " | delayList.size: " + delayList.size() +
                    " | countList.size: " + countList.size());
        }

        //Inputs are good - make the necessary key presses.
        for (int i = 0; i < commandList.size(); i++) {
            String command = commandList.get(i);
            Integer repeatNum = countList.get(i);
            Integer delayMs = delayList.get(i);

            if (repeatNum > 0) {
                keyPressOk = remote.pressKeyAndHold(command, repeatNum);
            } else {
                keyPressOk = remote.pressKey(command);
            }

            // If one of the key presses fails, do not do the remaining ones.
            if (!keyPressOk) {
                break;
            }

            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (keyPressOk) {
            resp = ResponseEntity.ok(Boolean.TRUE.toString());
        } else {
            resp = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Boolean.FALSE.toString());
        }
        return resp;
    }

    /**
     * Press the sequence of keys with specified repeat count and delays in between.
     * This delay will not account for delay associated with physical hardware sending the key.
     *
     */
    @Operation(summary = "Perform sequence of key presses with specified repeat count and delays.", description = "Press the sequence of keys with specified repeat count and delays in between with remote commands.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful",
                    content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "400", description = "IR devices not found"),
            @ApiResponse(responseCode = "404", description = "Device/Port not found.")
    })
    @RequestMapping(value = "/remoteCommandSeq", method = RequestMethod.POST, produces = {"text/plain","application/json"})
    public ResponseEntity<String> enterRemoteCommandSequence(@Parameter(description = "Device Rack.") @PathVariable("rack") String rack,
                                                             @Parameter(description = "Device Slot.") @PathVariable("slot") String slot,
                                                             @Parameter(description = "KeySet to use for sending command to the device.") @RequestParam("keySet") String keySet,
                                                             @Parameter(description = "List of commands with the required command, repeat count and delays in between.")
                                                                 @RequestParam("command") List<String> commandList) {

        validateSlot(slot);
        if (commandList == null || commandList.isEmpty() || !isStringArgValid(keySet)) {
            throw new CustomBadRequestException("Arguments are either null or empty- keySet: " +
                    keySet + ", commandList: " + commandList);
        }

        String commandString = "";
        String repeatString = "";
        String delayString = "";

        for (String cmd : commandList) {
            if (!isStringArgValid(cmd)) {
                throw new CustomBadRequestException("Command either null or empty. command: " + cmd);
            }
            StringTokenizer tokenizer = new StringTokenizer(cmd, ",");
            commandString = tokenizer.nextToken() + ",";
            repeatString = tokenizer.nextToken() + ",";
            delayString = tokenizer.nextToken() + ",";
        }

        return enterCustomKeySequence(slot, keySet, commandString, delayString, repeatString);
    }

    /**
     * Direct tune to a channel
     *
     */
    @Operation(summary = "Direct tune to a channel.", description = "Direct tune to a channel.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful",
                    content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "400", description = "IR devices not found"),
            @ApiResponse(responseCode = "404", description = "Device/Port not found.")
    })
    @RequestMapping(value = "/tune", method = RequestMethod.POST, produces = {"text/plain","application/json"})
    public ResponseEntity<String> tune( @Parameter(description = "Device Slot.") @PathVariable("slot") String slot,
                                       @Parameter(description = "Key set passed.") @RequestParam("keySet") String keySet,
                                       @Parameter(description = "Channel number to tune to.") @RequestParam("channel") String channel,
                                       @Parameter(description = "Whether autotune is enabled or not.") @RequestParam("autoTune") String autoTuneEnabled,
                                       @Parameter(description = "Delay in milliseconds .") @RequestParam("delayInMillis") String delayInMillis) {
        ResponseEntity<String> resp;
        Remote remote;
        boolean channelTuneOk;

        validateSlot(slot);
        // Verify channel and delayInMillis are not empty/null
        if (!isStringArgValid(channel, delayInMillis)) {
            throw new CustomBadRequestException("Channel and/or delayInMillis is either null or empty- channel: " +
                    channel + ", delayInMillis: " + delayInMillis);
        }

        if (!delayInMillis.matches("\\d+")) {
            throw new CustomBadRequestException("delayInMillis does not contain all numerical values.");
        }

        remote = getRemote(slot, keySet);
        remote.setAutoTuneEnabled(Boolean.parseBoolean(autoTuneEnabled));
        remote.setDelay(Integer.parseInt(delayInMillis));
        channelTuneOk = remote.tune(channel);
        if (channelTuneOk) {
            resp = ResponseEntity.ok(Boolean.TRUE.toString());
        } else {
            resp = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Boolean.FALSE.toString());
        }
        return resp;
    }

    /**
     * Sends text and the text must be a numeric string.
     *
     * @param keySet keyset/remote type to be specified.
     * @param text   String to be entered.
     * @return true if the text message executed successfully, false otherwise.
     */
    @Operation(summary = "Sends numeric text string.", description = "Sends text and the text must be a numeric string.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful",
                    content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "400", description = "IR devices not found"),
            @ApiResponse(responseCode = "404", description = "Device/Port not found.")
    })
    @RequestMapping(value = "/sendText", method = RequestMethod.POST, produces = {"text/plain","application/json"})
    public ResponseEntity<String> sendText(@Parameter(description = "Device Slot.") @PathVariable("slot") String slot,
                                           @Parameter(description = "KeySet to use for sending command to the device.") @RequestParam("keySet") String keySet,
                                           @Parameter(description = "String to be entered.") @RequestParam("string") String text) {
        ResponseEntity<String> resp;
        Remote remote;
        boolean textSendOk;

        validateSlot(slot);
        if (!isStringArgValid(keySet, text)) {
            throw new CustomBadRequestException("Arguments either null or empty. keySet: " + keySet +
                    ", string: " + text);
        }

        //Verify text is all numeric
        if (!text.matches("\\d+")) {
            throw new CustomBadRequestException("String must be a numeric string. string: " + text);
        }

        remote = getRemote(slot, keySet);
        textSendOk = remote.sendText(text);
        if (textSendOk) {
            resp = ResponseEntity.ok(Boolean.TRUE.toString());
        } else {
            resp = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Boolean.FALSE.toString());
        }
        return resp;
    }

    /**
     * Sends the IR code. Note that the code is sent as the payload
     * of the REST request.
     */
    @Operation(summary = "Sends the IR code.", description = "Sends the IR code. Note that the code is sent as the payload of the REST request..")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)) }),
            @ApiResponse(responseCode = "400", description = "IR devices not found"),
            @ApiResponse(responseCode = "404", description = "IRCode/Slot not found.")
    })
    @RequestMapping(value = "/sendIR", method = RequestMethod.POST, produces = {"text/plain","application/json"})
    public Boolean sendIR(@Parameter(description = "IRCode ir code to be sent.") @RequestBody String irCode,
                          @Parameter(description = "Device Slot.") @PathVariable("slot") String slot) {
        validateSlot(slot);
        return false;
    }

    /**
     * Command to make transmission of key commands faster for common use cases.
     * Example Tune(1000) with OK with 1 second delay between commands.
     *
     * @param keySet  remote key set to use during transmission.
     * @param command Each character in string corresponds to key.
     * @param delay   Delay in milliseconds between keys. Default is 500ms.
     * @return true if all commands executed successfully, false at the first failing command
     */
    @Operation(summary = "Commands to make transmission of key commands faster for common use cases.", description = "Command to make transmission of key commands faster for common use cases.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "400", description = "IR devices not found"),
            @ApiResponse(responseCode = "404", description = "Device/Port not found.")
    })
    @RequestMapping(value = "/seq", method = RequestMethod.POST, produces = {"text/plain","application/json"})
    public ResponseEntity<String> seq(@Parameter(description = "Device Slot.") @PathVariable("slot") String slot,
                                      @Parameter(description = "KeySet to use for sending command to the device.") @RequestParam("keySet") String keySet,
                                      @Parameter(description = "Command passed to the device.") @RequestParam("command") String command,
                                      @Parameter(description = "Delay in milliseconds between keys. Default is 500ms.") @DefaultValue("500") @RequestParam("delay") Integer delay) {
        ResponseEntity<String> resp;
        Remote remote;
        boolean keyPressOk;

        validateSlot(slot);
        if (!isStringArgValid(keySet, command)) {
            throw new CustomBadRequestException("Arguments either null or empty. keySet: " +
                    keySet + ", command: " + command);
        }

        List<String> commands = CommandProcessor.commandFromSequence(command);
        remote = getRemote(slot, keySet);
        keyPressOk = remote.pressKeys(commands, delay);
        if (keyPressOk) {
            resp = ResponseEntity.ok(Boolean.TRUE.toString());
        } else {
            resp = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Boolean.FALSE.toString());
        }
        return resp;
    }

    /**
     * Verifies if a set of commands are valid.
     *
     * @param args
     * @return
     */
    private boolean isStringArgValid(String... args) {
        boolean isValid = true;
        for (String arg : args) {
            if (arg == null || arg.isEmpty()) {
                isValid = false;
                break;
            }
        }
        return isValid;
    }

    /**
     * Isolates individual commands from a string of commands and puts them in a list.
     *
     * @param commands commands listed in String format.
     * @return A list of commands.
     */
    private List<String> getRemoteCommandList(String commands) {
        StringTokenizer tokenizer = new StringTokenizer(commands, ",");
        List<String> commandList = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            commandList.add(tokenizer.nextToken());
        }
        return commandList;
    }

    /**
     * Parses individual integers from a string of integers.
     *
     * @param integerList a string of integers delimited by a comma.
     * @return a list of integers found in integerList.
     */
    private List<Integer> getIntegerList(String integerList) {
        StringTokenizer tokenizer = new StringTokenizer(integerList, ",");
        List<Integer> intList = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            intList.add(Integer.parseInt(tokenizer.nextToken()));
        }
        return intList;
    }
}

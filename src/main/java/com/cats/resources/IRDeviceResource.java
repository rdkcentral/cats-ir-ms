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
import com.cats.ir.IRDevice;
import com.cats.ir.IRDevicePort;
import com.cats.ir.Remote;
import com.cats.ir.exception.CustomBadRequestException;
import com.cats.service.CommandProcessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.core.MediaType;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cats.service.RemoteProcessor;

import jakarta.ws.rs.core.Response;

@RestController
@Tag(name = "IR Device Control", description = "Control APIs for Power Devices on Rack.")
@RequestMapping("rest/device/{device}/{port}")
public class IRDeviceResource {

    private final RemoteProcessor processor;
    private final Integer MAX_DELAY = 30 * 1000; // 30 seconds in milliseconds

    public IRDeviceResource(RemoteProcessor processor) {
        this.processor = processor;
    }


    /**
     * Gets a LocalRemote with a connection to a port of an IRNetBox device.
     *
     * @param keySet Remote key mapping to use during transmission.
     * @param device Device number to connect to.
     * @param port   Port number to connect to.
     *
     * @return a LocalRemote object.
     */
    private Remote getRemote(String device, String port, String keySet) {
        return processor.getRemote(Integer.parseInt(device), Integer.parseInt(port), keySet);
    }

    private void validateDeviceAndPort(String device, String port) {
        String deviceId = device;
        String irPort = port;
        Integer maxPorts = processor.findMaxPortsOfDevice(deviceId);

        if (!processor.validateDevice(deviceId)) {
            throw new CustomBadRequestException
                    ("Requested device is not valid. Please choose a number between 1 and " + processor.numDevices() + ".");
        }

        if (!processor.deviceHasPort(deviceId, irPort)) {
            throw new CustomBadRequestException
                    ("Requested port is not valid. Please choose a number between 1 and " + maxPorts + ".");
        }
    }

    /**
     * Get Device Rack and Port information for the device.
     *
     * @return the requested device rack and slot.
     */
    @Operation(summary = "Get Device Rack and Port information", description = "Get information of rack and IR port of the device.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful",
                    content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "400", description = "Device not found for given device id and port.")
    })
    @RequestMapping(value = "/get", method = RequestMethod.GET, produces = "text/plain")
    public Response get(@Parameter(description = "Device Id") @PathVariable("device") String device,
                        @Parameter(description = "Port at which the device exists ") @PathVariable("port") String port) {
        validateDeviceAndPort(device, port);
        return Response.ok("IR [Device=" + device + ", Port=" + port + "]", MediaType.TEXT_PLAIN).build();
    }

    /**
     * Perform IR key press for a given keyset and remote command.
     *
     * @param keySet  keyset/remote type to be specified.
     * @param command String command to get executed on key press.
     *
     * @return true if remote command operation is successful, false otherwise.
     */
    @Operation(summary = "Perform key press", description = "Perform IR key press for a given key set and remote command.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful",
                    content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = Boolean.class)) }),
            @ApiResponse(responseCode = "400", description = "Device not found for given device id and port."),
            @ApiResponse(responseCode = "404", description = "Device/Port not found.")
    })
    @RequestMapping(value = "/pressKey", method = RequestMethod.POST, produces = "text/plain")
    public Response pressKey(@Parameter(description = "Device to send the command.") @PathVariable("device") String device,
                             @Parameter(description = "Port of the device.") @PathVariable("port") String port,
                             @Parameter(description = "KeySet to use for sending command to the device.") @RequestParam("keySet") String keySet,
                             @Parameter(description = "Command to be sent to device.") @RequestParam("command") String command) {
        Response resp = null;
        Remote remote;
        boolean keyPressOk;

        validateDeviceAndPort(device, port);
        remote = getRemote(device, port, keySet);
        System.out.println("REMOTE: " + remote.getRemoteType());
        keyPressOk = remote.pressKey(command);
        if (keyPressOk) {
            resp = Response.ok(keyPressOk, MediaType.TEXT_PLAIN).build();
        } else {
            resp = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(keyPressOk).build();
        }
        return resp;

    }

    /**
     * Performs IR key presses.
     *
     * @param keySet      keyset / remote type to be specified
     * @param commandList List of commands to get executed on key press.
     * @param delay       delay between the commands in milliseconds.
     *
     * @return true if remote operation is successful, false if otherwise.
     */
    @Operation(summary = "Performs IR key presses.", description = "Performs IR key presses.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful",
                    content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = Boolean.class)) }),
            @ApiResponse(responseCode = "400", description = "Device not found for given device id and port."),
            @ApiResponse(responseCode = "404", description = "Device/Port not found.")
    })
    @RequestMapping(value = "/pressKeys", method = RequestMethod.POST, produces = "text/plain")
    public Response pressKeys(@Parameter(description = "Device to send the command.") @PathVariable("device") String device,
                              @Parameter(description = "Port of the device.") @PathVariable("port") String port,
                              @Parameter(description = "KeySet to use for sending command to the device") @RequestParam("keySet") String keySet,
                              @Parameter(description = "List of commands to be sent to device") @RequestParam("commandList") String commandList,
                              @Parameter(description = "Delay between the commands in milliseconds.") @RequestParam("delayInMillis") Integer delay) {
        Response resp;
        Remote remote;
        boolean keyPressOk;

        validateDeviceAndPort(device, port);
        remote = getRemote(device, port, keySet);
        keyPressOk = remote.pressKey(commandList, delay);
        if (keyPressOk) {
            resp = Response.ok(keyPressOk, MediaType.TEXT_PLAIN).build();
        } else {
            resp = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(keyPressOk).build();
        }
        return resp;
    }

    /**
     * Perform IR key press and hold key for specified time.
     *
     * @param keySet   keyset/remote type to be specified.
     * @param command  String command to get executed on key press.
     * @param holdTime Time for which key is held during key press.
     *
     * @return true if remote operation is successful, false if otherwise.
     */
    @Operation(summary = "Performs IR key press and hold.", description = "Perform IR key press and hold key for a specified time.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful",
                    content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = Boolean.class)) }),
            @ApiResponse(responseCode = "400", description = "Device not found for given device id and port."),
            @ApiResponse(responseCode = "404", description = "Device/Port not found.")
    })
    @RequestMapping(value = "/pressKeyAndHold", method = RequestMethod.POST, produces = "text/plain")
    public Response pressKeyAndHold(@Parameter(description = "Device to send the command.") @PathVariable("device") String device,
                                    @Parameter(description = "Port of the device.") @PathVariable("port") String port,
                                    @Parameter(description = "KeySet to use for sending command to the device.") @RequestParam("keySet") String keySet,
                                    @Parameter(description = "Command to be sent to device.") @RequestParam("command") String command,
                                    @Parameter(description = "Time for which key is held during key press.") @RequestParam("holdTime") String holdTime) {
        Response resp = null;
        Remote remote;
        boolean keyPressOk;

        validateDeviceAndPort(device, port);

        if (!isStringArgValid(keySet, command, holdTime)) {
            throw new CustomBadRequestException("Arguments are either empty or null. keySet: " +
                    keySet + ", command: " + command + ", holdTime: " + holdTime);
        }

        if (!holdTime.matches("\\d+")) {
            throw new CustomBadRequestException("HoldTime is not numeric.");
        }

        remote = getRemote(device, port, keySet);
        keyPressOk = remote.pressKeyAndHold(command, Integer.parseInt(holdTime));
        if (keyPressOk) {
            resp = Response.ok(keyPressOk, MediaType.TEXT_PLAIN).build();
        } else {
            resp = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(keyPressOk).build();
        }

        return resp;
    }

    /**
     * Press IR key and hold it for a specified duration.
     *
     * @param keySet      keyset/remote type to be specified.
     * @param command     String command to get executed on key press.
     * @param durationSec Duration in seconds for repeat.
     *
     * @return true if remote command operation is successful, false if otherwise.
     */
    @Operation(summary = "Performs IR key press and hold.", description = "Perform IR key press and hold key for a repeated duration.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful",
                    content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = Boolean.class)) }),
            @ApiResponse(responseCode = "400", description = "Device not found for given device id and port."),
            @ApiResponse(responseCode = "404", description = "Device/Port not found.")
    })
    @RequestMapping(value = "/pressKeyAndHoldDuration", method = RequestMethod.POST, produces = "text/plain")
    public Response pressKeyAndHoldDuration(@Parameter(description = "Device to send the command.")  @PathVariable("device") String device,
                                            @Parameter(description = "Port of the device.") @PathVariable("port") String port,
                                            @Parameter(description = "KeySet to use for sending command to the device.") @RequestParam("keySet") String keySet,
                                            @Parameter(description = "Command to be sent to device") @RequestParam("command") String command,
                                            @Parameter(description = "Duration in seconds for repeat.") @RequestParam("holdTime") Integer durationSec) {
        Response resp = null;
        Remote remote;
        boolean keyPressOk;

        validateDeviceAndPort(device, port);


        if (!isStringArgValid(keySet, command)) {
            throw new CustomBadRequestException("Arguments are either null or empty- keySet: " +
                    keySet + ", command: " + command);
        }

        if (durationSec == null) {
            throw new CustomBadRequestException("holdTime is null. Include this parameter in your next request.");
        }

        remote = getRemote(device, port, keySet);
        keyPressOk = remote.pressKeyAndHoldDuration(command, durationSec);
        if (keyPressOk) {
            resp = Response.ok(keyPressOk, MediaType.TEXT_PLAIN).build();
        } else {
            resp = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(keyPressOk).build();
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
     *
     * @return true if all remote command operations are successful, false at the first failure.
     */
    @Operation(summary = "Performs IR key press and hold.", description = "Perform a sequence of key presses with specified repeat count and delays in between.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful",
                    content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = Boolean.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid Request. Please update and retry request."),
            @ApiResponse(responseCode = "404", description = "Device/Port not found.")
    })
    @RequestMapping(value = "/customKeySeq", method = RequestMethod.POST, produces = "text/plain")
    public Response enterCustomKeySequence(@Parameter(description = "Device to send the command.") @PathVariable("device") String device,
                                           @Parameter(description = "Port of the device.") @PathVariable("port") String port,
                                           @Parameter(description = "KeySet to use for sending command to the device.") @RequestParam("keySet") String keySet,
                                           @Parameter(description = "List of commands to be sent.") @RequestParam("commands") String commands,
                                           @Parameter(description = "Delay between each of the keys.") @RequestParam("delay") String delay,
                                           @Parameter(description = "Repeat counts of each of the keys.") @RequestParam("repeatCount") String repeatCount) {
        Response resp;
        Remote remote;
        boolean keyPressOk = false;

        validateDeviceAndPort(device, port);
        if (!isStringArgValid(keySet, commands, repeatCount, delay)) {
            throw new CustomBadRequestException("Arguments are either null or empty- keySet: " +
                    keySet + ", commandList: " + commands + ", repeatCount: " + repeatCount +
                    ", delay: " + delay);
        }

        remote = getRemote(device, port, keySet);
        List<String> commandList = getRemoteCommandList(commands);
        List<Integer> delayList = getIntegerList(delay);
        List<Integer> countList = getIntegerList(repeatCount);

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
            resp = Response.ok(keyPressOk, MediaType.TEXT_PLAIN).build();
        } else {
            resp = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(keyPressOk).build();
        }
        return resp;
    }

    /**
     * Press the sequence of keys with specified repeat count and delays in between.
     * This delay will not account for delay associated with physical hardware sending the key.
     *
     * @param keySet      keyset/remote type to be specified
     * @param commandList List of commands with the required command, repeat count, and delays
     *                    in between (ie: [M,2,1000]).
     *
     * @return true if all remote command operations are successful, false at the first failure.
     */
    @Operation(summary = "Perform IR Remote Sequence.", description = "Perform a sequence of key presses with specified repeat count and delays in between that are not the delays associated with physical hardware sending the key.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful",
                    content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = Boolean.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid Request. Please update and retry request."),
            @ApiResponse(responseCode = "404", description = "Device/Port not found.")
    })
    @RequestMapping(value = "/remoteCommandSeq", method = RequestMethod.POST, produces = "text/plain")
    public Response enterRemoteCommandSequence(@Parameter(description = "Device to send the command.") @PathVariable("device") String device,
                                               @Parameter(description = "Port of the device.") @PathVariable("port") String port,
                                               @Parameter(description = "KeySet to use for sending command to the device.") @RequestParam("keySet") String keySet,
                                               @Parameter(description = "Command to be sent.") @RequestParam("command") List<String> commandList) {
        validateDeviceAndPort(device, port);
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

        return enterCustomKeySequence(device, port, keySet, commandString, delayString, repeatString);
    }

    /**
     * Direct tune to a channel
     *
     * @param keySet          keyset/remote type to be specified
     * @param channel         Channel number (as String) to tune to.
     * @param autoTuneEnabled whether autotune is enabled (as String) or not.
     * @param delayInMillis   Delay in milliseconds (as String).
     *
     * @return true if tune executed successfully, false otherwise.
     */
    @Operation(summary = "Directly tune to a channel.", description = "Directly tune to a channel.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful",
                    content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = Boolean.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid Request. Please update and retry request."),
            @ApiResponse(responseCode = "404", description = "Device/Port not found.")
    })
    @RequestMapping(value = "/tune", method = RequestMethod.POST, produces = "text/plain")
    public Response tune(@Parameter(description = "Device to send the command.") @PathVariable("device") String device,
                         @Parameter(description = "Port of the device.") @PathVariable("port") String port,
                         @Parameter(description = "KeySet to use for sending command to the device.") @RequestParam("keySet") String keySet,
                         @Parameter(description = "Channel number to tune to.") @RequestParam("channel") String channel,
                         @Parameter(description = "Define if autotune is enabled.") @RequestParam("autoTune") String autoTuneEnabled,
                         @Parameter(description = "Delay in milliseconds.") @RequestParam("delayInMillis") String delayInMillis) {
        Response resp;
        Remote remote;
        boolean channelTuneOk;

        validateDeviceAndPort(device, port);
        // Verify channel and delayInMillis are not empty/null
        if (!isStringArgValid(channel, delayInMillis)) {
            throw new CustomBadRequestException("Channel and/or delayInMillis is either null or empty- channel: " +
                    channel + ", delayInMillis: " + delayInMillis);
        }

        if (!delayInMillis.matches("\\d+")) {
            throw new CustomBadRequestException("delayInMillis does not contain all numerical values.");
        }

        remote = getRemote(device, port, keySet);
        remote.setAutoTuneEnabled(Boolean.parseBoolean(autoTuneEnabled));
        remote.setDelay(Integer.parseInt(delayInMillis));
        channelTuneOk = remote.tune(channel);
        if (channelTuneOk) {
            resp = Response.ok(channelTuneOk, MediaType.TEXT_PLAIN).build();
        } else {
            resp = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(channelTuneOk).build();
        }
        return resp;
    }

    /**
     * Sends text and the text must be a numeric string.
     *
     * @param keySet keyset/remote type to be specified.
     * @param text   String to be entered.
     *
     * @return true if the text message executed successfully, false otherwise.
     */
    @Operation(summary = "Send Text String", description = "Sends text and the text must be a numeric string.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful",
                    content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = Boolean.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid Request. Please update and retry request."),
            @ApiResponse(responseCode = "404", description = "Device/Port not found.")
    })
    @RequestMapping(value = "/sendText", method = RequestMethod.POST, produces = "text/plain")
    public Response sendText(@Parameter(description = "Device to send the command.") @PathVariable("device") String device,
                             @Parameter(description = "Port of the device.") @PathVariable("port") String port,
                             @Parameter(description = "KeySet to use for sending command to the device.")  @RequestParam("keySet") String keySet,
                             @Parameter(description = "Text to be sent to the device.")  @RequestParam("string") String text) {
        Response resp;
        Remote remote;
        boolean textSendOk;

        validateDeviceAndPort(device, port);
        if (!isStringArgValid(keySet, text)) {
            throw new CustomBadRequestException("Arguments either null or empty. keySet: " + keySet +
                    ", string: " + text);
        }

        //Verify text is all numeric
        if (!text.matches("\\d+")) {
            throw new CustomBadRequestException("String must be a numeric string. string: " + text);
        }

        remote = getRemote(device, port, keySet);
        textSendOk = remote.sendText(text);
        if (textSendOk) {
            resp = Response.ok(textSendOk, MediaType.TEXT_PLAIN).build();
        } else {
            resp = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(textSendOk).build();
        }
        return resp;
    }

    /**
     * Sends the IR code. Note that the code is sent as the payload
     * of the REST request.
     *
     * @param irCode ir code to be sent.
     *
     * @return true if the sending ircode executed successfully, false if otherwise.
     */
    @Operation(summary = " Sends the IR code.", description = "Sends the IR code. Note that the code is sent as the payload of the REST request.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful",
                    content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = Boolean.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid Request. Please update and retry request."),
            @ApiResponse(responseCode = "404", description = "Device/Port not found.")
    })
    @RequestMapping(value = "/sendIR", method = RequestMethod.POST, consumes = "text/plain", produces = "text/plain")
    public Boolean sendIR(@Parameter(description = "Device to send the command.") @PathVariable("device") String device,
                          @Parameter(description = "Port of the device.") @PathVariable("port") String port, String irCode) {
        validateDeviceAndPort(device, port);
        return false;
    }

    /**
     * Command to make transmission of key commands faster for common use cases.
     * Example Tune(1000) with OK with 1 second delay between commands.
     *
     * @param keySet  remote key set to use during transmission.
     * @param command Each character in string corresponds to key.
     * @param delay   Delay in milliseconds between keys. Default is 500ms.
     *
     * @return true if all commands executed successfully, false at the first failing command
     */
    @Operation(summary = " Perform transmission of key commands faster.", description = "Command to perform transmission of key commands faster for common use cases.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation successful",
                    content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = Boolean.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid Request. Please update and retry request."),
            @ApiResponse(responseCode = "404", description = "Device/Port not found.")
    })
    @RequestMapping(value = "/seq", method = RequestMethod.POST, produces = "text/plain")
    public Response seq(@Parameter(description = "Device to send the command.") @PathVariable("device") String device,
                        @Parameter(description = "Port of the device.") @PathVariable("port") String port,
                        @Parameter(description = "KeySet to use for sending command to the device.") @RequestParam("keySet") String keySet,
                        @Parameter(description = "Command to be sent.") @RequestParam("command") String command,
                        @Parameter(description = "Device to send the command.") @DefaultValue("500") @RequestParam("delay") Integer delay) {
        Response resp;
        Remote remote;
        boolean keyPressOk;

        validateDeviceAndPort(device, port);
        if (!isStringArgValid(keySet, command)) {
            throw new CustomBadRequestException("Arguments either null or empty. keySet: " +
                    keySet + ", command: " + command);
        }

        List<String> commands = CommandProcessor.commandFromSequence(command);
        remote = getRemote(device, port, keySet);
        keyPressOk = remote.pressKeys(commands, delay);
        if (keyPressOk) {
            resp = Response.ok(keyPressOk, MediaType.TEXT_PLAIN).build();
        } else {
            resp = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(keyPressOk).build();
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

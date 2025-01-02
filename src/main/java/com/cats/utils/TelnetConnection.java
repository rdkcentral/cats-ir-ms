
package com.cats.utils;

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

import com.cats.configuration.CustomApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;

/**
 * Represents a generic telnet connection.
 */
public class TelnetConnection {
    private InputStream is;
    private PrintStream os;
    private String host;
    private Integer port;
    private String defaultPromptString;
    private Socket socket = null;
    private int defaultReadTimeout;
    protected Date lastActiveTime;
    protected boolean isConnected = false;
    private boolean isBusy = false;
    /**
     * Typically a repeat count of 60 would take close to 8 seconds.
     * Keeping 10000 msecs to be on the safe side.
     *
     * */
    public static final String DEFAULT_READ_TIMEOUT = System.getProperty("readrat.read.timeout", "15000");
    public static final int DEFAULT_TIMEOUT = 5 * 60 * 1000;

    private static Logger logger = LoggerFactory.getLogger(TelnetConnection.class);

    /**
     * Creates a TelnetConnection instance.
     *
     * @param host
     *            : of the telnet device
     * @param port
     *            : telnet port
     * @param defaultPromptString
     *            : default prompt string to be used. Usually ">".
     *
     */
    public TelnetConnection(String host, Integer port, String defaultPromptString) {
        this.host = host;
        this.port = port;
        this.defaultPromptString = defaultPromptString;
        lastActiveTime = new Date();
        this.socket = new Socket();
        int readTimeout = Integer.parseInt(DEFAULT_READ_TIMEOUT);
        setDefaultReadTimeout(readTimeout);
    }

    /**
     * Connect to the telnet client.
     *
     * @param isEnterRequired
     *            : sometime an ENTER key maybe required to reach the prompt.
     *
     * @return true: if connected.
     *
     * @throws SocketException
     * @throws IOException
     */
    public synchronized boolean connect(boolean isEnterRequired) throws SocketException, IOException {
        try {
            if (socket == null || !socket.isConnected()) {
                logger.debug("connect() Create new Socket");
                socket = new Socket();
                logger.debug("connect() Connect Attempt");
                socket.setSoTimeout(defaultReadTimeout);
                socket.setKeepAlive(true);
                socket.connect(new InetSocketAddress(host, port), defaultReadTimeout);
                logger.debug("connect() getInputStream()");
                is = socket.getInputStream();
                os = new PrintStream(socket.getOutputStream(), true);
                if (isEnterRequired) {
                    os.println();
                }
                isConnected = true;
            }
            lastActiveTime = new Date();
            logger.debug("connect() Complete");
        } catch (Exception e) {
            logger.error("connect() Exception", e);
            closeConnection();
            isConnected = false;
        }
        return isConnected;
    }

    public synchronized void closeConnection() {
        isConnected = false;
        try {
            if (socket != null) {
                logger.debug("closeConnection() OutputStream");
                socket.close();
            }
        } catch (IOException e) {
            logger.error("Exception Socket.close() - {}", e.getMessage());
        } finally {
            logger.debug("closeConnection() Socket=NULL");
            socket = null;
        }
        try {
            if (is != null) {
                logger.debug("closeConnection() InputStream");
                is.close();
            }
        } catch (IOException e) {
            logger.error("Exception InputStream.close() - {}", e.getMessage());
        } finally {
            logger.debug("closeConnection() InputStream=NULL");
            is = null;
        }
        if (os != null) {
            logger.debug("closeConnection() OutputStream");
            os.close();
            os = null;
        }
    }

    /**
     * Status of telnet connection
     *
     * @return true if connected.
     */
    public synchronized boolean isConnected() {
        return isConnected;
    }

    /**
     * Send a command to the telnet session. Requires
     * TelnetConnection.isConnected() to be true.
     *
     * @param command
     *            to send
     * @return returned value in telnet client after execution of command.
     * @throws IOException
     */
    public synchronized String sendCommand(String command) throws IOException {
        return sendCommand(command, defaultPromptString);
    }

    /**
     * Send a command to the telnet session, and read till the following prompt
     * instead of the default prompt.
     *
     * Requires TelnetConnection.isConnected() to be true.
     *
     * @param command
     * @param prompt
     * @return
     * @throws IOException
     */
    public synchronized String sendCommand(String command, String prompt) throws IOException {
        isBusy = true;
        logger.trace("sendCommand(command:{},prompt:{})", command, prompt);
        String result = null;
        if (isConnected && command != null) {
            MeasuredTelnetUtil telnetUtil = CustomApplicationContext.getBean(MeasuredTelnetUtil.class);
            result = telnetUtil.sendCommand(os, command,is,prompt);

            if(result == null){
                closeConnection();
            }
        }
        lastActiveTime = new Date();
        isBusy = false;
        return result;
    }

    /**
     * if prompt string is not received within the default timeout value, the
     * read will be interrupted. This is important to avoid the connection to
     * hang on a read when the prompt string does not arrive. Default value is 1
     * minute.
     *
     * @return defaultTimeout value
     */
    public int getDefaultReadTimeout() {
        return defaultReadTimeout;
    }

    /**
     * Timeout to that closes the socket during a period of inactivity.
     *
     */
    public void setDefaultReadTimeout(int defaultReadTimeout) {
        this.defaultReadTimeout = defaultReadTimeout;
        try {
            logger.debug("Setting socket SO timeout to {}", defaultReadTimeout);
            socket.setSoTimeout(defaultReadTimeout);
        } catch (IOException e) {
            logger.error("Failed to set the default timeout to {} got error {} ", defaultReadTimeout, e.getMessage());

        }
    }

    /**
     * This just provides the Output stream to write to the telnet connection.
     * Should be called only after a connect is called. This is to provide users
     * with an option to write stuff to the telnet connection other than what is
     * provided through this class. Its the responsibility of the user to
     * understand the working of TelnetConnection and the proper use of the
     * PrintStream
     *
     */
    public PrintStream getPrintStream() {
        return os;
    }

    /**
     * This just provides the input stream to write to the telnet connection.
     * Should be called only after a connect is called. This is to provide users
     * with an option to read stuff from the telnet connection other than what
     * is provided through this class. Its the responsibility of the user to
     * understand the working of TelnetConnection and the proper use of the
     * input
     *
     */
    public InputStream getInputStream() {
        return is;
    }

    /**
     * Get the last time a connect, or sendCommand was sent. can be used to
     * determine timeout to disconnect based on inactivity etc.
     *
     * @return
     */
    public Date getLastActiveTime() {
        return lastActiveTime;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public boolean isBusy() {
        return isBusy;
    }
}
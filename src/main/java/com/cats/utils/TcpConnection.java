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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.Date;

/**
 * The Class TcpConnection.
 * It represents a TCP connection to a device.
 */
public class TcpConnection {

    // The input stream for reading data from the socket
    private InputStream is;

    // The output stream for sending data to the socket
    private PrintWriter os;

    // The host address of the TCP connection
    private String host;

    //The port number of the TCP connection
    private Integer port;

    // The socket representing the TCP connection
    private Socket socket = null;

    //The last time the connection was active
    protected Date lastActiveTime;

    //Flag indicating if the connection is currently established
    protected boolean isConnected = false;

    //Flag indicating if the connection is currently busy
    private boolean isBusy = false;

    private static Logger logger = LoggerFactory.getLogger(TcpConnection.class);

    /**
     * Instantiates a new TCP connection.
     *
     * @param host the host
     * @param port the port
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public TcpConnection(String host, Integer port) throws IOException {
        this.host = host;
        this.port = port;
        this.socket = new Socket(host, port);
    }

    /**
     * Connects to the device.
     *
     * @return true, if successful
     */
    public synchronized boolean connect() {
        try {
            if (!socket.isConnected()) {
                os = new PrintWriter(socket.getOutputStream(), true);
                is = socket.getInputStream();
                os.println();
                isConnected = true;
            }
            lastActiveTime = new Date();
            logger.info("connect() - Completed");
        } catch (Exception e) {
            logger.error("connect() - Exception: {}", e.getMessage());
            closeConnection();
            isConnected = false;
        }

        return isConnected;
    }

    public synchronized boolean isConnected() {
        return isConnected;
    }

    /**
     * Sends a command to the device.
     *
     * @param command the command
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public synchronized String sendCommand(String command) throws IOException {
        isBusy = true;
        logger.trace("sendCommand(command:{})", command);
        String result = null;

        if (isConnected && command != null) {
            write(command);
            result = is.toString();
        }

        lastActiveTime = new Date();
        isBusy = false;
        return result;
    }

    /**
     * Closes the connection to the device.
     */
    public synchronized void closeConnection() {
        isConnected = false;
        try {
            if (socket != null) {
                logger.info("closeConnection() OutputStream");
                socket.close();
            }
        } catch (IOException e) {
            logger.error("Exception Socket.close() - {}", e.getMessage());
        } finally {
            logger.info("closeConnection() Socket=NULL");
            socket = null;
        }


        try {
            if (is != null) {
                logger.info("closeConnection() InputStream");
                is.close();
            }
        } catch (IOException e) {
            logger.error("Exception InputStream.close() - {}", e.getMessage());
        } finally {
            logger.info("closeConnection() InputStream=NULL");
            is = null;
        }


        if (os != null) {
            logger.info("closeConnection() OutputStream");
            os.close();
            os = null;
        }
    }


    /**
     * Writes the value to the device.
     *
     * @param value the value
     */
    private synchronized void write(String value) {
        if (null != os) {
            logger.info("write[{}]", value);
            os.println(value);
        } else {
            closeConnection();
        }
    }
}

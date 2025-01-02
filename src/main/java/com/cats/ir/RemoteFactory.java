
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

/**
 Interface allow for the creation of Remote objects used to handle
 both network-connected and local USB-connected IR devices.
 */
public interface RemoteFactory {

    /**
     * Provide additional method to handle local USB connected devices with deviceName;
     * @param type - Type of IR device to return.
     * @param host - Device host.
     * @param keySet - Remote keyset to use when sending keys.
     * @param port - Port of IR device to use.
     * @return - Remote to perform key transmission.
     */
    Remote getRemote(IRHardwareEnum type, String host, String keySet, int port);

    /**
     * Provide additional method to handle local USB connected devices with deviceName;
     * @param type - Type of IR device to return.
     * @param host - Device host.
     * @param keySet - Remote keyset to use when sending keys.
     * @param port - Port of IR device to use.
     * @param deviceName - Optional deviceName for non-host devices.
     * @return - Remote to perform key transmission.
     */
    Remote getRemote(IRHardwareEnum type, String host, String keySet, int port, String deviceName);

}

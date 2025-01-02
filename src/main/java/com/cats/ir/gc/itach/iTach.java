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

import com.cats.ir.redrat.IrNetBoxPro;
import com.cats.ir.IRDevicePort;
import com.cats.ir.IRHardwareEnum;
import com.cats.ir.gc.GCDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
/**
 * The Class iTach.
 * It represents a specific type of Global Cache device(iTach),
 * identified by its module and type.
 */
public class iTach extends GCDevice {
    private static final Logger logger = LoggerFactory.getLogger(iTach.class);
    private static final int    ITACH_MAXPORTS = 3;

    /**
     * Constructs an iTach device with the specified ID, IP address
     * and GC dispatcher API base URL.
     *
     * @param id the unique identifier for the device (required)
     * @param ipAddress the IP address of the device (required)
     * @param getGcDispatcherApiBase the base URL for the GC dispatcher API (required)
     */
    public iTach(String id, String ipAddress, String getGcDispatcherApiBase) {
        super(id,getGcDispatcherApiBase);
        this.deviceIPAddr = ipAddress;
        this.deviceType = IRHardwareEnum.ITACH;
        init();
    }

    @Override
    public boolean init() {
        devicePorts = new ArrayList<IRDevicePort>( ITACH_MAXPORTS );
        for ( int i = 1; i <= ITACH_MAXPORTS; i++ )
        {
            iTachPort port = new iTachPort( i, this);
            devicePorts.add( port );
        }
        return true;
    }

    @Override
    public IRDevicePort getPort( int portNumber )
    {
        IRDevicePort retVal = null;
        if ( devicePorts != null && !devicePorts.isEmpty() )
        {
            for ( IRDevicePort irPort : devicePorts )
            {
                if ( irPort.getPortNumber() == portNumber )
                {
                    retVal = irPort;
                    break;
                }
            }
        }
        logger.debug( "getPort " + portNumber + " from irDevice " + deviceIPAddr + " : " + retVal );
        return retVal;
    }

    @Override
    public boolean equals( Object object )
    {
        boolean isEqual = false;
        if ( object instanceof IrNetBoxPro)
        {

            if ( super.equals( object ) && ( ( iTach ) object ).deviceIPAddr.equals( deviceIPAddr ) )
            {
                isEqual = true;
            }
        }
        return isEqual;
    }

    @Override
    public int hashCode()
    {
        return deviceIPAddr.hashCode();
    }

    @Override
    public boolean uninit() {
        return false;
    }
}

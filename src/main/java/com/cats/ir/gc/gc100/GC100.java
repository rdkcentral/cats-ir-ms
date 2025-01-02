package com.cats.ir.gc.gc100;

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

import com.cats.ir.IRHardwareEnum;
import com.cats.ir.gc.GCDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class GC100.
 * It represents a specific type of Global Cache device(GC100),
 * identified by its module and type.
 */
public class GC100 extends GCDevice {

    private static final Logger logger = LoggerFactory.getLogger(GC100.class);


    public GC100(String id, String module, String getGcDispatcherApiBase) {
        super(id, getGcDispatcherApiBase);
        this.deviceModule = module;
        this.deviceType = IRHardwareEnum.GC100;
        init();
    }

    @Override
    public boolean init() {
        return false;
    }

    @Override
    public boolean uninit() {
        return false;
    }
}

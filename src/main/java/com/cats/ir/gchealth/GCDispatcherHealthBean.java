package com.cats.ir.gchealth;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class GCDispatcherHealthBean.
 * It represents the health status of the GC Dispatcher.
 */
@Slf4j
public class GCDispatcherHealthBean {
    @JsonProperty("result")
    private GCDispatcherHealthDataBean result;

    public GCDispatcherHealthBean() {
    }

    @JsonProperty("result")
    public GCDispatcherHealthDataBean getResult() {
        return result;
    }

    @JsonProperty("result")
    public void setResult(GCDispatcherHealthDataBean result) {
        this.result = result;
    }

    public boolean getIsHealthy() {
        return this.result != null;
    }

}

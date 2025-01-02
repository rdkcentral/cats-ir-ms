
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

public enum IRHardwareEnum {
    GC100("gc100", 6),
    GC100_12("gc100-12", 12),
    GC100_6("gc100-6", 6),
    ITACH("itach", 3),
    IRNETBOXPRO3("irnetboxpro3", 16),
    REDRAT3("redrat3", 1);

    private String scheme;
    private Integer maxPorts;

    IRHardwareEnum(String scheme, Integer maxPorts) {
        this.setScheme(scheme);
        this.setMaxPorts(maxPorts);
    }

    public static IRHardwareEnum getByValue(String scheme) {
        return IRHardwareEnum.valueOf(scheme.toUpperCase().replace('-', '_'));
    }

    /**
     * Validate scheme is a valid enum.
     *
     * @param scheme
     *            -
     * @return
     */
    public static boolean validate(String scheme) {
        boolean isValid = false;
        try {
            if (getByValue(scheme) instanceof IRHardwareEnum) {
                isValid = true;
            }
        } catch (IllegalArgumentException iae) {
            // Make sure this is caught for invalid input.
            isValid = false;
        }
        return isValid;

    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    private void setMaxPorts(Integer maxPorts) {
        this.maxPorts = maxPorts;
    }

    public Integer getMaxPorts() {
        return maxPorts;
    }
}
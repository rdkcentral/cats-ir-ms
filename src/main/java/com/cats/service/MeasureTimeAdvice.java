package com.cats.service;

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

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;

/**
 * Aspect to measure time taken by methods annotated with @MeasureTime.
 */
@Aspect
@Component
@Slf4j
public class MeasureTimeAdvice {

    @Autowired
    private HttpServletResponse httpServletResponse;

    public static final String HW_COMMAND_REQUEST_TIME_HEADER="HW-Command-Request-Time";
    public static final String HW_COMMAND_RESPONSE_TIME_HEADER="HW-Command-Response-Time";
    public static final String HW_COMMAND_DURATION_HEADER="HW-Command-Duration-Ms";

    @Around("@annotation(com.cats.service.MeasureTime)")
    public Object measureTime(ProceedingJoinPoint point) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        Instant  startTime = Instant.now();
        stopWatch.start();
        Object object = point.proceed();
        stopWatch.stop();
        try {
            httpServletResponse.setHeader(HW_COMMAND_REQUEST_TIME_HEADER, String.valueOf(startTime));
            httpServletResponse.setHeader(HW_COMMAND_RESPONSE_TIME_HEADER, String.valueOf(Instant.now()));
            httpServletResponse.setHeader(HW_COMMAND_DURATION_HEADER, String.valueOf(stopWatch.getTotalTimeMillis()));
        }catch(IllegalStateException e){

        }
        log.info("Time take by " + point.getSignature().getName() + "() method is "
                + stopWatch.getTotalTimeMillis() + " ms");
        return object;
    }
}

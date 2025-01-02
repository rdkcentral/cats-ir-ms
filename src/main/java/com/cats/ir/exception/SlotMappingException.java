package com.cats.ir.exception;

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



import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


public class SlotMappingException extends WebApplicationException {

    public SlotMappingException(String message) {
        super(Response.status(Response.Status.NOT_FOUND)
                .entity(message).type(MediaType.TEXT_PLAIN).build());
    }

    public SlotMappingException(Throwable cause) {
        super(cause);
    }

    public Response toResponse(Exception ex) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(ex.getLocalizedMessage())
                .type(MediaType.APPLICATION_JSON).
                build();
    }

    private static final long serialVersionUID = 1L;

}
/*
This Source Code Form is subject to the terms of the MIT License.
Copyright (c) 2022 Draegerwerk AG & Co. KGaA.

SPDX-License-Identifier: MIT
*/

package com.draeger.medical.t2iapi;

import com.draeger.medical.t2iapi.device.DeviceServiceGrpc;
import com.draeger.medical.t2iapi.device.DeviceServiceGrpc.DeviceServiceBlockingStub;
import com.google.protobuf.Empty;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;

class T2IApiTest {
    private static final int PORT = 8888;
    private static final String HOST = "localhost";

    /*
       Tests that BootUpDevice rpc call invokes correct Service implementation
     */
    @Test
    void deviceBootUpTest() throws IOException {
        GrpcServer.startGrpcServer(PORT, HOST);
        ManagedChannel channel = ManagedChannelBuilder.forAddress(HOST, PORT)
                .usePlaintext()
                .build();

        DeviceServiceBlockingStub blockingStub = DeviceServiceGrpc.newBlockingStub(channel);
        BasicResponses.BasicResponse response = blockingStub.bootUpDevice(Empty.newBuilder().build());
        channel.shutdown();
        assertEquals(response.getResult(), ResponseTypes.Result.RESULT_SUCCESS);
    }
}
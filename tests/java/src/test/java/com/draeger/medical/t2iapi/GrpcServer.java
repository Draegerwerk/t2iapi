/*
This Source Code Form is subject to the terms of the MIT License.
Copyright (c) 2022 Draegerwerk AG & Co. KGaA.

SPDX-License-Identifier: MIT
*/

package com.draeger.medical.t2iapi;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.draeger.medical.t2iapi.device.DeviceServiceGrpc.DeviceServiceImplBase;
import com.draeger.medical.t2iapi.activation_state.ActivationStateServiceGrpc.ActivationStateServiceImplBase;
import com.draeger.medical.t2iapi.alert.AlertServiceGrpc.AlertServiceImplBase;
import com.draeger.medical.t2iapi.combined.CombinedServiceGrpc.CombinedServiceImplBase;
import com.draeger.medical.t2iapi.context.ContextServiceGrpc.ContextServiceImplBase;
import com.draeger.medical.t2iapi.metric.MetricServiceGrpc.MetricServiceImplBase;
import com.draeger.medical.t2iapi.operation.OperationServiceGrpc.OperationServiceImplBase;
import com.google.protobuf.Empty;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;

class GrpcServer {

    private static class DeviceServiceImpl extends DeviceServiceImplBase {
        @Override
        public void bootUpDevice(Empty request, StreamObserver<BasicResponses.BasicResponse> responseObserver) {
            ResponseTypes.Result result = ResponseTypes.Result.RESULT_SUCCESS;
            BasicResponses.BasicResponse response = BasicResponses.BasicResponse
                    .newBuilder()
                    .setResult(result)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    private static class ActivationStateServiceImpl extends ActivationStateServiceImplBase {}
    private static class AlertServiceImpl extends AlertServiceImplBase {}
    private static class CombinedServiceImpl extends CombinedServiceImplBase {}
    private static class ContextServiceImpl extends ContextServiceImplBase {}
    private static class MetricServiceImpl extends MetricServiceImplBase {}
    private static class OperationServiceImpl extends OperationServiceImplBase {}

    static void startGrpcServer(int port, String host) throws IOException {
        Server server = NettyServerBuilder.forAddress(new InetSocketAddress(host, port))
                .addService(new DeviceServiceImpl())
                .addService(new ActivationStateServiceImpl())
                .addService(new AlertServiceImpl())
                .addService(new ContextServiceImpl())
                .addService(new CombinedServiceImpl())
                .addService(new MetricServiceImpl())
                .addService(new OperationServiceImpl())
                .build();

        server.start();
    }
}
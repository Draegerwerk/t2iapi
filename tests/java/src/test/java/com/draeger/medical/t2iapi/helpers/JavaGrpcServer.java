/*
This Source Code Form is subject to the terms of the MIT License.
Copyright (c) 2026 Draegerwerk AG & Co. KGaA.

SPDX-License-Identifier: MIT
*/

package com.draeger.medical.t2iapi.helpers;

import com.draeger.medical.t2iapi.integration.IntegrationServiceGrpc;
import com.draeger.medical.t2iapi.integration.IntegrationServiceProto.*;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.draeger.medical.t2iapi.helpers.Common.TEST_DATA_PATH;

public class JavaGrpcServer {


    public static void main(String[] args) throws Exception {
        if (args.length > 2) {
            System.err.println("Usage: JavaGrpcServer [port] [testdata_path]");
            System.exit(1);
        }
        int port = args.length >= 1 ? Integer.parseInt(args[0]) : 0;
        Path testdataPath = args.length == 2
                ? Path.of(args[1])
                : TEST_DATA_PATH;

        List<String> validationErrors = new java.util.ArrayList<>();
        JavaGrpcServer server = new JavaGrpcServer(port, testdataPath, validationErrors);
        System.out.println(server.getPort());
        System.out.flush();
        System.in.read();
        server.stop();
        validationErrors.forEach(System.err::println);
        System.exit(validationErrors.isEmpty() ? 0 : 1);
    }

    private final Server server;

    /*
       Start the integration gRPC server and validate received data.
    */
    public JavaGrpcServer(int port, Path testdataPath, List<String> validationErrors) throws IOException {
        Common.loadTestData(testdataPath);
        server = ServerBuilder.forPort(port)
                .addService(new IntegrationServiceImpl(validationErrors))
                .build()
                .start();
    }

    public int getPort() {
        return server.getPort();
    }

    public void stop() throws InterruptedException {
        server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    private static class IntegrationServiceImpl
            extends IntegrationServiceGrpc.IntegrationServiceImplBase {

        private final List<String> validationErrors;

        IntegrationServiceImpl(List<String> validationErrors) {
            this.validationErrors = validationErrors;
        }

        /*
           Check received against the expected scenario, record error on mismatch.
        */
        private void validate(String rpcCall, Message received) {
            if (!Common.cases.containsKey(rpcCall)) {
                validationErrors.add("unknown rpcCall: '" + rpcCall + "'");
                return;
            }
            try {
                var builder = received.newBuilderForType();
                JsonFormat.parser().merge(Common.buildItemJson(rpcCall, Common.cases.get(rpcCall).value()), builder);
                Message expected = builder.build();
                if (!received.equals(expected)) {
                    validationErrors.add("Mismatch for '" + rpcCall + "':\n"
                            + "  expected: " + expected + "\n"
                            + "  received: " + received);
                }
            } catch (Exception e) {
                validationErrors.add("Parse error for '" + rpcCall + "': " + e.getMessage());
            }
        }

        /*
           Merge the next scenario into builder.
        */
        private void buildResponse(String rpcCall, Message.Builder builder) {
            try {
                Common.getExpectedResponseAndMerge(rpcCall, builder);
            } catch (InvalidProtocolBufferException e) {
                validationErrors.add("Parse error building next response for '" + rpcCall + "': " + e.getMessage());
            }
        }

        private <T extends Message> void handleRequest(
                Message received,
                Message.Builder responseBuilder,
                Supplier<T> builderCall,
                String rpcCall,
                StreamObserver<T> responseObserver
        ) {
            validate(rpcCall, received);
            buildResponse(rpcCall, responseBuilder);
            responseObserver.onNext(builderCall.get());
            responseObserver.onCompleted();
        }


        @Override
        public void testString(StringCase received, StreamObserver<StringCase> responseObserver) {
            var builder = StringCase.newBuilder();
            handleRequest(received, builder, builder::build, received.getRpcCall(), responseObserver);
        }

        @Override
        public void testBool(BoolCase received, StreamObserver<BoolCase> responseObserver) {
            var builder = BoolCase.newBuilder();
            handleRequest(received, builder, builder::build, received.getRpcCall(), responseObserver);
        }

        @Override
        public void testUint32(Uint32Case received, StreamObserver<Uint32Case> responseObserver) {
            var builder = Uint32Case.newBuilder();
            handleRequest(received, builder, builder::build, received.getRpcCall(), responseObserver);
        }

        @Override
        public void testEnum(EnumCase received, StreamObserver<EnumCase> responseObserver) {
            var builder = EnumCase.newBuilder();
            handleRequest(received, builder, builder::build, received.getRpcCall(), responseObserver);
        }

        @Override
        public void testRepeatedString(
                RepeatedStringCase received,
                StreamObserver<RepeatedStringCase> responseObserver
        ) {
            var builder = RepeatedStringCase.newBuilder();
            handleRequest(received, builder, builder::build, received.getRpcCall(), responseObserver);
        }

        @Override
        public void testRepeatedEnum(RepeatedEnumCase received, StreamObserver<RepeatedEnumCase> responseObserver) {
            var builder = RepeatedEnumCase.newBuilder();
            handleRequest(received, builder, builder::build, received.getRpcCall(), responseObserver);
        }

        @Override
        public void testRepeatedMessage(
                RepeatedMessageCase received,
                StreamObserver<RepeatedMessageCase> responseObserver
        ) {
            var builder = RepeatedMessageCase.newBuilder();
            handleRequest(received, builder, builder::build, received.getRpcCall(), responseObserver);
        }

        @Override
        public void testMessage(MessageCase received, StreamObserver<MessageCase> responseObserver) {
            var builder = MessageCase.newBuilder();
            handleRequest(received, builder, builder::build, received.getRpcCall(), responseObserver);
        }

        @Override
        public void testOptionalString(
                OptionalStringCase received,
                StreamObserver<OptionalStringCase> responseObserver
        ) {
            var builder = OptionalStringCase.newBuilder();
            handleRequest(received, builder, builder::build, received.getRpcCall(), responseObserver);
        }

        @Override
        public void testOptionalUint64(
                OptionalUint64Case received,
                StreamObserver<OptionalUint64Case> responseObserver
        ) {
            var builder = OptionalUint64Case.newBuilder();
            handleRequest(received, builder, builder::build, received.getRpcCall(), responseObserver);
        }

        @Override
        public void testDuration(DurationCase received, StreamObserver<DurationCase> responseObserver) {
            var builder = DurationCase.newBuilder();
            handleRequest(received, builder, builder::build, received.getRpcCall(), responseObserver);
        }

        @Override
        public void testDeepNested(
                DeepNestedMessageCase received,
                StreamObserver<DeepNestedMessageCase> responseObserver
        ) {
            var builder = DeepNestedMessageCase.newBuilder();
            handleRequest(received, builder, builder::build, received.getRpcCall(), responseObserver);
        }
    }
}

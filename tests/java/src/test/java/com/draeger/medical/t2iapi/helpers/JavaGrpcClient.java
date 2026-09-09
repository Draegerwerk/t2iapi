/*
This Source Code Form is subject to the terms of the MIT License.
Copyright (c) 2026 Draegerwerk AG & Co. KGaA.

SPDX-License-Identifier: MIT
*/

package com.draeger.medical.t2iapi.helpers;

import com.draeger.medical.t2iapi.integration.IntegrationServiceGrpc;
import com.draeger.medical.t2iapi.integration.IntegrationServiceProto.*;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.Assertions;

import java.nio.file.Path;
import java.util.Map;

import static com.draeger.medical.t2iapi.helpers.Common.TEST_DATA_PATH;

public class JavaGrpcClient {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: JavaIntegrationClient <server_address> <testdata_path>");
            System.exit(1);
        }
        var serverAddress = args[0];
        var testdataPath = TEST_DATA_PATH;
        if (args.length == 2) {
            testdataPath = Path.of(args[1]);
        }
        String[] parts = serverAddress.split(":", 2);
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        run(host, port, testdataPath);
    }

    /*
       Connect to Grpc server and send all test scenarios, assert no validation errors occurred.
    */
    public static void run(String host, int port, Path testdataPath) throws Exception {
        Common.loadTestData(testdataPath);
        var responseResults = new StringBuilder();

        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        try {
            IntegrationServiceGrpc.IntegrationServiceBlockingStub stub =
                    IntegrationServiceGrpc.newBlockingStub(channel);
            send(stub, responseResults);
        } finally {
            channel.shutdown();
            Assertions.assertTrue(responseResults.isEmpty(), responseResults.toString());
        }
    }

    /*
       Compare received against the next scenario in the scenario type group, return an error string or empty.
    */
    private static String validateResponse(String rpcCall, Message received) {
        try {
            var builder = received.newBuilderForType();
            Common.getExpectedResponseAndMerge(rpcCall, builder);
            Message expected = builder.build();
            if (!received.equals(expected)) {
                return "Validation failed for rpcCall: " + rpcCall + "\nexpected: " + expected + "received: " +
                        received;
            }
        } catch (Exception e) {
            return "Error validating response for rpcCall: " + rpcCall + ". " + e.getMessage() + "\n";
        }
        return "";
    }

    /*
       Dispatch all test scenarios and validate each response against the next scenario in its type group.
    */
    private static void send(
            IntegrationServiceGrpc.IntegrationServiceBlockingStub stub,
            StringBuilder errors) throws Exception {
        for (Map.Entry<String, Common.ScenarioEntry> entry : Common.cases.entrySet()) {
            String rpcCall = entry.getKey();
            String itemJson = Common.buildItemJson(rpcCall, entry.getValue().value());
            final Message result;
            if (rpcCall.startsWith("TestRepeatedString")) {
                var b = RepeatedStringCase.newBuilder();
                JsonFormat.parser().merge(itemJson, b);
                result = stub.testRepeatedString(b.build());
            } else if (rpcCall.startsWith("TestRepeatedEnum")) {
                var b = RepeatedEnumCase.newBuilder();
                JsonFormat.parser().merge(itemJson, b);
                result = stub.testRepeatedEnum(b.build());
            } else if (rpcCall.startsWith("TestRepeatedMessage")) {
                var b = RepeatedMessageCase.newBuilder();
                JsonFormat.parser().merge(itemJson, b);
                result = stub.testRepeatedMessage(b.build());
            } else if (rpcCall.startsWith("TestOptionalString")) {
                var b = OptionalStringCase.newBuilder();
                JsonFormat.parser().merge(itemJson, b);
                result = stub.testOptionalString(b.build());
            } else if (rpcCall.startsWith("TestOptionalUint64")) {
                var b = OptionalUint64Case.newBuilder();
                JsonFormat.parser().merge(itemJson, b);
                result = stub.testOptionalUint64(b.build());
            } else if (rpcCall.startsWith("TestString")) {
                var b = StringCase.newBuilder();
                JsonFormat.parser().merge(itemJson, b);
                result = stub.testString(b.build());
            } else if (rpcCall.startsWith("TestBool")) {
                var b = BoolCase.newBuilder();
                JsonFormat.parser().merge(itemJson, b);
                result = stub.testBool(b.build());
            } else if (rpcCall.startsWith("TestUint32")) {
                var b = Uint32Case.newBuilder();
                JsonFormat.parser().merge(itemJson, b);
                result = stub.testUint32(b.build());
            } else if (rpcCall.startsWith("TestEnum")) {
                var b = EnumCase.newBuilder();
                JsonFormat.parser().merge(itemJson, b);
                result = stub.testEnum(b.build());
            } else if (rpcCall.startsWith("TestMessage")) {
                var b = MessageCase.newBuilder();
                JsonFormat.parser().merge(itemJson, b);
                result = stub.testMessage(b.build());
            } else if (rpcCall.startsWith("TestDuration")) {
                var b = DurationCase.newBuilder();
                JsonFormat.parser().merge(itemJson, b);
                result = stub.testDuration(b.build());
            } else if (rpcCall.startsWith("TestDeepNested")) {
                var b = DeepNestedMessageCase.newBuilder();
                JsonFormat.parser().merge(itemJson, b);
                result = stub.testDeepNested(b.build());
            } else {
                throw new IllegalArgumentException("No RPC mapped for rpcCall: '" + rpcCall + "'");
            }
            errors.append(validateResponse(rpcCall, result));
        }
    }
}

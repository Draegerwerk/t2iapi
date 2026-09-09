/*
This Source Code Form is subject to the terms of the MIT License.
Copyright (c) 2026 Draegerwerk AG & Co. KGaA.

SPDX-License-Identifier: MIT
*/

package com.draeger.medical.t2iapi.helpers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Common {

    record ScenarioEntry(Optional<JsonElement> value, String successor) { }

    public static final Path TEST_DATA_PATH = Path.of("src/test/resources/integration_scenarios.json")
            .toAbsolutePath().normalize();
    static Map<String, ScenarioEntry> cases;

    private static final String EXPECTED = "expected";
    private static final String SUFFIX = "suffix";
    private static final String RPC_CALL = "rpcCall";

    private Common() {
    }

    /*
       Read the JSON and populate the cases map. Each entry carries its expected value and the rpcCall of the
       next scenario .
    */
    static void loadTestData(Path testDataPath) throws IOException {
        String json = Files.readString(testDataPath);
        Map<String, ScenarioEntry> result = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> type : JsonParser.parseString(json).getAsJsonObject().entrySet()) {
            List<String> keys = new ArrayList<>();
            List<Optional<JsonElement>> values = new ArrayList<>();
            for (JsonElement element : type.getValue().getAsJsonArray()) {
                JsonObject scenario = element.getAsJsonObject();
                keys.add(type.getKey() + "_" + scenario.get(SUFFIX).getAsString());
                values.add(scenario.has(EXPECTED) ? Optional.of(scenario.get(EXPECTED)) : Optional.empty());
            }
            for (int i = 0; i < keys.size(); i++) {
                result.put(keys.get(i), new ScenarioEntry(values.get(i), keys.get((i + 1) % keys.size())));
            }
        }
        cases = result;
    }

    /*
       Merge the scenario that follows rpcCall in its type group into builder.
    */
    static void getExpectedResponseAndMerge(String rpcCall, Message.Builder builder)
            throws InvalidProtocolBufferException {
        var nextKey = cases.get(rpcCall).successor();
        JsonFormat.parser().merge(buildItemJson(nextKey, cases.get(nextKey).value()), builder);
    }

    /*
       Build the test case JSON. Absent Optional means the expected key is omitted entirely.
    */
    static String buildItemJson(String rpcCall, Optional<JsonElement> raw) {
        JsonObject obj = new JsonObject();
        obj.addProperty(RPC_CALL, rpcCall);
        raw.ifPresent(e -> obj.add(EXPECTED, e));
        return obj.toString();
    }
}

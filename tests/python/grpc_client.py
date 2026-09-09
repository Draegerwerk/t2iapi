# This Source Code Form is subject to the terms of the MIT License.
#
# Copyright (c) 2026 Draegerwerk AG & Co. KGaA.
# SPDX-License-Identifier: MIT

"""gRPC client for cross-language integration testing. Intended to be invoked by JavaClientPythonServerTest."""

import grpc
import sys
from google.protobuf import json_format

import common
from common import DEFAULT_TEST_DATA_PATH, build_json, load_testdata
from t2iapi.integration import service_pb2
from t2iapi.integration import service_pb2_grpc


def _validate(received):
    """Return an error string if received does not match its scenario in cases, else empty string."""
    received_rpc_call = received.rpc_call
    entry = common.cases.get(received_rpc_call)
    if entry is None:
        return f"Unknown rpcCall in response: '{received_rpc_call}'"
    try:
        expected = json_format.Parse(build_json(received_rpc_call, entry.scenario), type(received)())
        if received != expected:
            return (f"Validation failed for rpcCall: '{received_rpc_call}'\n"
                    f"expected: {str(expected).rstrip()}\n"
                    f"received: {str(received)}")
    except Exception as e:
        return f"Error validating response for rpcCall: '{received_rpc_call}': {e}"
    return ""


def _send_and_validate_response(stub, rpc_call, item_json):
    """Dispatch rpc_call to the matching stub method, validate the response, and return any error string."""
    if rpc_call.startswith('TestRepeatedString'):
        response = stub.TestRepeatedString(json_format.Parse(item_json, service_pb2.RepeatedStringCase()))
    elif rpc_call.startswith('TestRepeatedEnum'):
        response = stub.TestRepeatedEnum(json_format.Parse(item_json, service_pb2.RepeatedEnumCase()))
    elif rpc_call.startswith('TestRepeatedMessage'):
        response = stub.TestRepeatedMessage(json_format.Parse(item_json, service_pb2.RepeatedMessageCase()))
    elif rpc_call.startswith('TestOptionalString'):
        response = stub.TestOptionalString(json_format.Parse(item_json, service_pb2.OptionalStringCase()))
    elif rpc_call.startswith('TestOptionalUint64'):
        response = stub.TestOptionalUint64(json_format.Parse(item_json, service_pb2.OptionalUint64Case()))
    elif rpc_call.startswith('TestString'):
        response = stub.TestString(json_format.Parse(item_json, service_pb2.StringCase()))
    elif rpc_call.startswith('TestBool'):
        response = stub.TestBool(json_format.Parse(item_json, service_pb2.BoolCase()))
    elif rpc_call.startswith('TestUint32'):
        response = stub.TestUint32(json_format.Parse(item_json, service_pb2.Uint32Case()))
    elif rpc_call.startswith('TestEnum'):
        response = stub.TestEnum(json_format.Parse(item_json, service_pb2.EnumCase()))
    elif rpc_call.startswith('TestMessage'):
        response = stub.TestMessage(json_format.Parse(item_json, service_pb2.MessageCase()))
    elif rpc_call.startswith('TestDuration'):
        response = stub.TestDuration(json_format.Parse(item_json, service_pb2.DurationCase()))
    elif rpc_call.startswith('TestDeepNested'):
        response = stub.TestDeepNested(json_format.Parse(item_json, service_pb2.DeepNestedMessageCase()))
    else:
        raise ValueError(f"No RPC mapped for rpcCall: '{rpc_call}'")
    return _validate(response)


def run(server_address, testdata_path):
    """Connect to server_address, send all test scenarios, and raise on any validation error."""
    load_testdata(testdata_path)
    errors = []

    with grpc.insecure_channel(server_address) as channel:
        stub = service_pb2_grpc.IntegrationServiceStub(channel)
        for rpc_call, entry in common.cases.items():
            error = _send_and_validate_response(stub, rpc_call, build_json(rpc_call, entry.scenario))
            if error:
                errors.append(error)

    if errors:
        raise AssertionError("\n".join(errors))


if __name__ == '__main__':
    if len(sys.argv) < 2:
        print(f"Usage: {sys.argv[0]} <server> [testdata_path]", file=sys.stderr)
        sys.exit(1)
    _server_address = sys.argv[1]
    _testdata_path = sys.argv[2] if len(sys.argv) == 3 else DEFAULT_TEST_DATA_PATH

    run(_server_address, _testdata_path)

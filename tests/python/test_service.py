# This Source Code Form is subject to the terms of the MIT License.
#
# Copyright (c) 2022 Draegerwerk AG & Co. KGaA.
# SPDX-License-Identifier: MIT

import contextlib
import queue
import threading
import unittest
from concurrent import futures
from unittest import mock

import grpc
from google.protobuf import empty_pb2
from t2iapi import basic_responses_pb2, response_types_pb2
from t2iapi.activation_state import service_pb2_grpc as activation_state_service_pb2_grpc

from t2iapi.alert import service_pb2_grpc as alert_service_pb2_grpc
from t2iapi.combined import service_pb2_grpc as combined_service_pb2_grpc
from t2iapi.context import service_pb2_grpc as context_service_pb2_grpc
from t2iapi.device import service_pb2_grpc as device_service_pb2_grpc
from t2iapi.metric import service_pb2_grpc as metric_service_pb2_grpc
from t2iapi.operation import service_pb2_grpc as operation_service_pb2_grpc


def serve(server_object_queue, port_queue):
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=1))

    activation_state_service_pb2_grpc.add_ActivationStateServiceServicer_to_server(
        activation_state_service_pb2_grpc.ActivationStateServiceServicer(), server)

    alert_service_pb2_grpc.add_AlertServiceServicer_to_server(
        alert_service_pb2_grpc.AlertServiceServicer(), server)

    context_service_pb2_grpc.add_ContextServiceServicer_to_server(
        context_service_pb2_grpc.ContextServiceServicer(), server)

    device_service_pb2_grpc.add_DeviceServiceServicer_to_server(
        device_service_pb2_grpc.DeviceServiceServicer(), server)

    metric_service_pb2_grpc.add_MetricServiceServicer_to_server(
        metric_service_pb2_grpc.MetricServiceServicer(), server)

    operation_service_pb2_grpc.add_OperationServiceServicer_to_server(
        operation_service_pb2_grpc.OperationServiceServicer(), server)

    combined_service_pb2_grpc.add_CombinedServiceServicer_to_server(
        combined_service_pb2_grpc.CombinedServiceServicer(), server)

    # port 0 is used to prevent collision of multiple builds on the same machine
    port = server.add_insecure_port('localhost:0')

    server.start()

    server_object_queue.put(server)
    port_queue.put(port)

    server.wait_for_termination()


def start():
    server_object_queue = queue.Queue(maxsize=1)
    port_queue = queue.Queue(maxsize=1)

    t = threading.Thread(target=serve, args=(server_object_queue, port_queue), daemon=True)

    t.start()

    return port_queue.get(), server_object_queue.get()


@contextlib.contextmanager
def running_server_context():
    port, server = start()
    yield port
    server.stop(None).wait()


class ServiceSmokeTest(unittest.TestCase):

    def test_BootUpDevice_works(self):
        """
        Tests that BootUpDevice rpc call invokes DeviceServiceServicer
        """

        response = basic_responses_pb2.BasicResponse(result=response_types_pb2.RESULT_SUCCESS)
        with mock.patch.object(target=device_service_pb2_grpc.DeviceServiceServicer,
                               attribute='BootUpDevice', return_value=response) as server_side:
            with running_server_context() as port:
                _target = f'localhost:{port}'
                with grpc.insecure_channel(_target) as channel:
                    stub = device_service_pb2_grpc.DeviceServiceStub(channel)
                    stub.BootUpDevice(empty_pb2.Empty())

                server_side.assert_called_once()


if __name__ == "__main__":
    unittest.main()

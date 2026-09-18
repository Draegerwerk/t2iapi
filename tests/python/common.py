# This Source Code Form is subject to the terms of the MIT License.
#
# Copyright (c) 2026 Draegerwerk AG & Co. KGaA.
# SPDX-License-Identifier: MIT

"""Shared utilities for grpc_client and grpc_server."""

import json
import pathlib
from dataclasses import dataclass

from google.protobuf import json_format

EXPECTED = 'expected'
SUFFIX = 'suffix'
RPC_CALL = 'rpcCall'
DEFAULT_TEST_DATA_PATH = (pathlib.Path(__file__).resolve().parent.parent
                          / 'java' / 'src' / 'test' / 'resources' / 'integration_scenarios.json')

cases = {}


@dataclass
class ScenarioEntry:
    scenario: dict
    successor: str


def load_testdata(testdata_path):
    """Read the JSON and populate the module-level cases dict with pre-computed round-robin successors."""
    global cases
    with open(testdata_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    result = {}
    for type_name, type_scenarios in data.items():
        keys = [f"{type_name}_{s[SUFFIX]}" for s in type_scenarios]
        for i, (key, scenario) in enumerate(zip(keys, type_scenarios)):
            result[key] = ScenarioEntry(scenario=scenario, successor=keys[(i + 1) % len(keys)])
    cases = result


def build_json(rpc_call, scenario):
    """Build proto3 JSON. Omits expected if the key is absent in the scenario."""
    if EXPECTED not in scenario:
        return json.dumps({RPC_CALL: rpc_call})
    return json.dumps({RPC_CALL: rpc_call, EXPECTED: scenario[EXPECTED]})


def get_expected_response_and_merge(rpc_call, msg):
    """Parse the scenario that follows rpc_call in its type group into msg."""
    next_key = cases[rpc_call].successor
    return json_format.Parse(build_json(next_key, cases[next_key].scenario), msg)

# Integration Tests

## Prerequisites

Before running any reference implementation or automated test, build the required artifacts
as described in [README](../README.md):


## Validating compatibility

If you implement t2iapi in a language other than Java or Python, or if you build the
package yourself using different versions of gRPC or protobuf than those released, we
strongly recommend verifying that your implementation correctly serializes and deserializes
all field types used by t2iapi. The repository provides reference client and server
implementations in both Python and Java together with test scenarios covering all field
types used by t2iapi.

Generate stubs from `src/t2iapi/integration/service.proto`, then use the reference
client or server against your own implementation, with
`tests/java/src/test/resources/integration_scenarios.json` as test data.

**Python** (`tests/python/`):

Run from the `tests/python/` directory with the t2iapi package installed.
Both arguments are optional: port defaults to a random free port, testdata defaults to
`tests/java/src/test/resources/integration_scenarios.json`.

```bash
# Run the reference Python client against your server
python grpc_client.py <your_server_address> <optional/path/to/integration_scenarios.json>

# Run the reference Python server for your client to connect to
python grpc_server.py <optional_port> <optional/path/to/integration_scenarios.json>
```

**Java** (`tests/java/`):

Run from the `tests/java/` directory.
Both arguments are optional: port defaults to a random free port, testdata defaults to
`src/test/resources/integration_scenarios.json` inside the `tests/java/`.

```bash
# Run the reference Java client against your server
./gradlew runJavaClient -Pserver=<your_server_address> -Ptestdata=<optional/path/to/integration_scenarios.json>

# Run the reference Java server for your client to connect to
./gradlew runJavaServer -Pport=<optional_port>  -Ptestdata=<optional/path/to/integration_scenarios.json>
```

Both servers print the bound port to stdout once ready, then run until stdin is closed.

## Automated cross-language tests

The repository contains JUnit 5 tests that run the Java and Python implementations against each
other automatically. There are two test classes, each covering one direction:

| Test class | Client | Server |
|---|---|---|
| `JavaClientPythonServerTest` | Java | Python |
| `PythonClientJavaServerTest` | Python | Java |

Each test spawns the other language's component as a subprocess and sends every scenario from
`tests/java/src/test/resources/integration_scenarios.json` through it. The server responds to each
incoming scenario with the next scenario in the same type group, cycling back to the first after
the last one (round-robin). The client validates the response against that expected next scenario.
Both sides collect validation errors independently; the test fails if either side reports any.

### Scenario file

The scenario file is a JSON object keyed by type name. Each type holds an ordered array of scenarios,
each with a `suffix` and an optional `expected` value. The full set of scenarios is in
[`tests/java/src/test/resources/integration_scenarios.json`](java/src/test/resources/integration_scenarios.json).

The `suffix` is a human-readable label that, combined with the type name, forms a unique RPC call
identifier (e.g. `TestString_populated`). The `expected` field controls what the scenario sends:

- no `expected` key - the field is omitted entirely, producing the proto3 default.
- **`null`** - for wrapper types (`StringValue`, `UInt64Value`) this leaves the field unset;
  for scalar types the result is the proto3 default.
- **any other value** - the field is set to that value.

The `null` and absent cases exist to verify that both ways of expressing an absent field in
JSON are handled identically by the parser on each side.

### Running the tests

The tests require a Python interpreter with the t2iapi gRPC stubs installed. Pass its path as a JVM
system property:

```bash
# Linux / macOS
./gradlew test -Dpython.executable=/path/to/venv/bin/python

# Windows
.\gradlew.bat test "-Dpython.executable=C:\path\to\venv\Scripts\python.exe"
```

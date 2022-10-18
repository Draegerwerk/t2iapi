# t2iapi

t2iapi describes a product-independent interface to manipulate devices
which utilize ISO/IEEE 11073 SDC during verification.

It is utilizing gRPC to allow for language-independent implementations
of the manipulation interface at an appropriate location, be it in the
device under test or within an already existing remote control
application.

[More information on SDC.](https://en.wikipedia.org/wiki/IEEE_11073_service-oriented_device_connectivity)

## Installation

### Building the package locally (Linux)

#### Python

t2iapi wheels can be built locally using the following steps:

```shell
cd python
./build_protobuf.sh
python setup.py bdist_wheel
```

Wheels will be available in `t2iapi/python/dist`.

Note that this requires protoc to be in your `PATH`.


#### Java

t2iapi jars can be built locally using the following steps.
Note that specifying versions for protoc, grpc and the jar itself is mandatory.
Only use supported combinations of protoc and grpc, typically this can be determined
from the protoc version used by the respective [grpc-java](https://github.com/grpc/grpc-java) release.

```shell
cd java
mvn package -Dpackage.version=<VERSION> -Ddependency.protobuf.version=<PROTOC_VERSION> -Ddependency.grpc.version=<GRPC_VERSION>
```

Jars will be available in `t2iapi/java/target`.

Note that this requires protoc to be in your `PATH`.

## Usage

t2iapi usage always consists of two parties, the t2iapi server and
the t2iapi client. When running tests for a provider, the test 
engineer provides an implementation of the t2iapi server, which,
when requested, makes changes to the Device under Test to reach a
specific device state.

```mermaid
graph LR
  subgraph dut["Test Engineer responsibility"]
    A["Device under test"]
    C(t2iapi server)
    C <--manipulation--> A 
  end
  subgraph testtool["Test tool"]
    B(Test case)
    D(t2iapi client)
    B --> D
  end
  D <--grpc manipulation call--> C
  B --SDC--> A
```

## Workflow
Changes to t2iapi are guided by requirements of Dräger test tools, including [SDCcc](https://github.com/Draegerwerk/sdccc).
As such, they are only done by Dräger employees.

## Notices
The t2iapi library is not intended for use in medical products.

### ISO 9001
t2iapi was not developed according to ISO 9001.

## License
[MIT](https://choosealicense.com/licenses/mit/)
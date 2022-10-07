# t2iapi

t2iapi describes a product-independent interface to manipulate devices
which utilize ISO/IEEE 11073 SDC during verification.

It is utilizing gRPC to allow for language-independent implementations
of the manipulation interface at an appropriate location, be it in the
device under test or within an already existing remote control
application.

[More information on SDC.](https://en.wikipedia.org/wiki/IEEE_11073_service-oriented_device_connectivity)

## Installation

TODO once clear

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
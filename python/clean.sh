#!/bin/bash

echo "Clearing previous data"
# only remove generated protobuf files from source folder
find . -type f -name '*pb2.py' -exec rm {} +
find . -type f -name '*pb2_grpc.py' -exec rm {} +
rm -rf build
rm -rf dist

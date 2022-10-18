#!/bin/bash

source ../config/versions.txt

python3.8 -m pip install "grpcio==$PYTHON_GRPC_VERSION" "grpcio-tools==$PYTHON_GRPC_VERSION" || exit 1
./clean.sh

echo "Generating model and service data"
python3.8 -m grpc_tools.protoc -I=../src --python_out=src --grpc_python_out=src ../src/t2iapi/*.proto ../src/t2iapi/**/*.proto || exit 2
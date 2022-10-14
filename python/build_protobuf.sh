#!/bin/bash

GRPC_VERSION_PYTHON=1.46.1

python3.8 -m pip install "grpcio==$GRPC_VERSION_PYTHON" "grpcio-tools==$GRPC_VERSION_PYTHON" || exit 1
./clean.sh

echo "Generating model and service data"
python3.8 -m grpc_tools.protoc -I=../src --python_out=src --grpc_python_out=src ../src/t2iapi/*.proto ../src/t2iapi/**/*.proto || exit 2
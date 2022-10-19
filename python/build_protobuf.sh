#!/bin/bash

source ../config/versions.txt

python -m pip install "grpcio==$PYTHON_GRPC_VERSION" "grpcio-tools==$PYTHON_GRPC_VERSION" "wheel==0.37.0" || exit 1
./clean.sh

echo "Generating model and service data"
python -m grpc_tools.protoc -I=../src --python_out=src --grpc_python_out=src ../src/t2iapi/*.proto ../src/t2iapi/**/*.proto || exit 2
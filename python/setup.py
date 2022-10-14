# This Source Code Form is subject to the terms of the MIT License.
#
# Copyright (c) 2022 Draegerwerk AG & Co. KGaA.
# SPDX-License-Identifier: MIT

import os
import setuptools

def read_config_map():
    """
    Reads the config map into a dict.

    @return: a dictionary representing the version config
    """
    data = {}
    with open("../config/versions.txt") as f:
        lines = f.read().splitlines()
        for line in lines:
            key, value = line.split("=")
            data[key] = value
    return data

def get_build_version():
    """
    Get the package version from the respective environment parameter, if available.
    Otherwise, return '0.0.0'.

    @return: package version
    """
    return os.getenv('build_version', default='0.0.0')

config_map = read_config_map()

setuptools.setup(
    name="t2iapi",
    version=get_build_version(),
    author="T2I Team",
    author_email="DLCDE-ODDS-T2I@draeger.com",
    description="T2I API for device communication in test scenarios",
    long_description='''
    Contains generated python files created from protobuf files.
    The protobuf files contain the specification of the serialization used to communicate information in test scenarios.
    ''',
    zip_safe=True,
    license='MIT',
    url='https://github.com/Draegerwerk/t2iapi',
    package_dir={'': 'src'},
    packages=setuptools.find_packages(where='src'),
    install_requires=['protobuf==' + config_map["PYTHON_PROTOC_VERSION"], 'grpcio==' + config_map["PYTHON_GRPC_VERSION"]],
    classifiers=[
        'Development Status :: 5 - Production/Stable',
        'Programming Language :: Python :: 3 :: Only',
        'Intended Audience :: Developers',
        'Natural Language :: English',
        'Topic :: Software Development :: Testing',
        'License :: OSI Approved :: MIT License',
    ]
)

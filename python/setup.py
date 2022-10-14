# This Source Code Form is subject to the terms of the MIT License.
#
# Copyright (c) 2022 Draegerwerk AG & Co. KGaA.
# SPDX-License-Identifier: MIT

import os
import setuptools


def get_build_version():
    """
    Get the package version from the respective environment parameter, if available.
    Otherwise, return '0.0.0'.

    @return: package version
    """
    return os.getenv('build_version', default='0.0.0')


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
    install_requires=['protobuf==3.19.4', 'grpcio==1.46.1'],
    classifiers=[
        'Development Status :: 5 - Production/Stable',
        'Programming Language :: Python :: 3 :: Only',
        'Intended Audience :: Developers',
        'Natural Language :: English',
        'Topic :: Software Development :: Testing',
        'License :: OSI Approved :: MIT License',
    ]
)

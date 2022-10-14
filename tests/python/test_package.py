# This Source Code Form is subject to the terms of the MIT License.
#
# Copyright (c) 2022 Draegerwerk AG & Co. KGaA.
# SPDX-License-Identifier: MIT

import os
import unittest


class T2iApiPackageTest(unittest.TestCase):

    def test_all_packages(self):
        """
        Tests verifies that all necessary packages will be present in the wheel
         by verifying that all directories have __init__.py
        """

        current_dir = os.path.dirname(os.path.abspath(__file__))
        relative_python_dir = os.path.sep.join(['..', '..', 'python', 'src', 't2iapi'])
        package_location = os.path.join(current_dir, relative_python_dir)
        missing__init__py = []
        for dir_path, _, filenames in os.walk(package_location):
            if '__init__.py' not in filenames:
                missing__init__py.append(dir_path)

        self.assertFalse(missing__init__py, msg="Package is missing '__init__.py'")


if __name__ == "__main__":
    unittest.main()

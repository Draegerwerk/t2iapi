# This Source Code Form is subject to the terms of the MIT License.
#
# Copyright (c) 2022 Draegerwerk AG & Co. KGaA.
# SPDX-License-Identifier: MIT

import os
import sys
import unittest

import HtmlTestRunner

EXPECTED_NBR_TESTS = 2

if __name__ == '__main__':
    loader = unittest.TestLoader()
    start_dir = os.path.sep.join(['tests', 'python'])
    all_tests = [ts for ts in list(loader.discover(start_dir)) if ts.countTestCases() > 0]
    nbr_tests = sum([ts.countTestCases() for ts in all_tests])

    runner = HtmlTestRunner.HTMLTestRunner(output='unittest_results',
                                           combine_reports=True,
                                           report_name=f"T2IAPIPythonReport",
                                           report_title=f'T2IAPI Unit Test Summary')

    res = runner.run(loader.suiteClass(all_tests))

    assert nbr_tests == EXPECTED_NBR_TESTS, "The number of discovered tests is not as expected."
    assert res.testsRun == EXPECTED_NBR_TESTS, "The number of executed tests is not as expected."
    assert not res.skipped, "At least one test was skipped."

    sys.exit(0 if res.wasSuccessful() else 1)

/*
This Source Code Form is subject to the terms of the MIT License.
Copyright (c) 2026 Draegerwerk AG & Co. KGaA.

SPDX-License-Identifier: MIT
*/

package com.draeger.medical.t2iapi;

import com.draeger.medical.t2iapi.helpers.Common;
import com.draeger.medical.t2iapi.helpers.JavaGrpcServer;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Python client -> Java server integration test.
 *
 * Starts the Java grpc server in a background thread, spawns the Python client as a subprocess, waits for it
 * to complete, then asserts the server exited cleanly with no validation errors.
 *
 * For running locally add path to your python's venv e.g.
 * -Dpython.executable=/venv/Scripts/python.exe
 */
class PythonClientJavaServerTest {

    private static final String PYTHON_CLIENT_PY = Path.of("../python/grpc_client.py")
            .toAbsolutePath().normalize().toString();

    @Test
    void pythonClientIntegrationTest() throws Exception {
        String pythonExe = System.getProperty("python.executable");
        assertNotNull(pythonExe, "python.executable system property must be set");

        List<String> validationErrors = Collections.synchronizedList(new ArrayList<>());
        JavaGrpcServer server = new JavaGrpcServer(0, Common.TEST_DATA_PATH, validationErrors);
        try {
            int port = server.getPort();
            Process proc = new ProcessBuilder(pythonExe, PYTHON_CLIENT_PY,
                    "localhost:" + port, Common.TEST_DATA_PATH.toString())
                    .start();

            List<String> stderrLines = new ArrayList<>();
            Thread stderrReader = new Thread(() -> {
                try (BufferedReader r = proc.errorReader()) {
                    String line;
                    while ((line = r.readLine()) != null) {
                        stderrLines.add(line);
                    }
                } catch (Exception ignored) {
                }
            });
            stderrReader.setDaemon(true);
            stderrReader.start();

            boolean finished = proc.waitFor(30, TimeUnit.SECONDS);
            stderrReader.join(5000);
            assertTrue(finished, "Python client subprocess timed out after 30 seconds");
            assertEquals(0, proc.exitValue(),
                    "Python client subprocess failed:\n" + String.join("\n", stderrLines));
            assertTrue(validationErrors.isEmpty(),
                    "Server validation errors:\n" + String.join("\n", validationErrors));
        } finally {
            server.stop();
        }
    }
}

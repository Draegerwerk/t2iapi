/*
This Source Code Form is subject to the terms of the MIT License.
Copyright (c) 2026 Draegerwerk AG & Co. KGaA.

SPDX-License-Identifier: MIT
*/

package com.draeger.medical.t2iapi;

import com.draeger.medical.t2iapi.helpers.Common;
import com.draeger.medical.t2iapi.helpers.JavaGrpcClient;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Java client -> Python server integration test.

 * Spawns the Python grpc server as a subprocess, runs the Java client in-process against it, then asserts the
 * server exited cleanly with no validation errors.
 *
 * For running locally add VM options add path to your python's venv e.g.
 * -Dpython.executable=/venv/Scripts/python.exe
 */
class JavaClientPythonServerTest {

    private static final String PYTHON_GRPC_SERVER_PY = Path.of("../python/grpc_server.py")
            .toAbsolutePath().normalize().toString();

    @Test
    void javaClientIntegrationTest() throws Exception {
        String pythonExe = System.getProperty("python.executable");
        assertNotNull(pythonExe, "python.executable system property must be set");

        Process serverProcess =
                new ProcessBuilder(pythonExe, PYTHON_GRPC_SERVER_PY, "0", Common.TEST_DATA_PATH.toString())
                        .start();

        List<String> stderrLines = new ArrayList<>();
        Thread stderrReader = new Thread(() -> {
            try (BufferedReader r = serverProcess.errorReader()) {
                String line;
                while ((line = r.readLine()) != null) {
                    stderrLines.add(line);
                }
            } catch (Exception ignored) {
            }
        });
        stderrReader.setDaemon(true);
        stderrReader.start();

        try (BufferedReader stdout = serverProcess.inputReader()) {
            int port = Integer.parseInt(stdout.readLine().trim());
            JavaGrpcClient.run("localhost", port, Common.TEST_DATA_PATH);
            serverProcess.getOutputStream().close();
            stderrReader.join(5000);
        } finally {
            if (serverProcess.isAlive()) {
                serverProcess.destroyForcibly();
            }
        }

        boolean finished = serverProcess.waitFor(30, TimeUnit.SECONDS);
        assertTrue(finished, "Python server subprocess timed out after 30 seconds");
        assertEquals(0, serverProcess.exitValue(),
                "Python server validation errors:\n" + String.join("\n", stderrLines));
    }
}

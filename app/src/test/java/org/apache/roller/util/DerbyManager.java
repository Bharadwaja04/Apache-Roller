/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */
package org.apache.roller.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.roller.weblogger.business.startup.SQLScriptRunner;


public class DerbyManager {
    private final String databaseDir;
    private final String databaseScriptsDir;
    private final int port;

    public DerbyManager(String databaseDir, String databaseScriptsDir, int port) {
        this.databaseDir = databaseDir;
        this.databaseScriptsDir = databaseScriptsDir;
        this.port = port;
    }

    public void startDerby() throws Exception {
        try {
                System.out.println("================");
                System.out.println("Starting Embedded Derby (In-Memory)");
                System.out.println("================");

                System.setProperty("derby.system.home", databaseDir);

                Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
                Connection conn = DriverManager.getConnection(
                    "jdbc:derby:memory:rollerdb;create=true","APP", "APP");

                // create roller tables
                SQLScriptRunner runner1 = new SQLScriptRunner(
                    databaseScriptsDir + File.separator + "droptables.sql");
                runner1.runScript(conn, false);

                SQLScriptRunner runner = new SQLScriptRunner(
                    databaseScriptsDir + File.separator + "derby" + File.separator + "createdb.sql");
                try {
                    runner.runScript(conn, true);
                } catch (Exception ignored) {
                    for (String message : runner.getMessages()) {
                        System.out.println(message);
                    }
                    ignored.printStackTrace();
                }

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("ERROR starting Derby");
        }
    }

    public void stopDerby() throws Exception {
        try {
                System.out.println("================");
                System.out.println("Stopping Embedded Derby (In-Memory)");
                System.out.println("================");

                try {
                    DriverManager.getConnection("jdbc:derby:memory:rollerdb;shutdown=true");
                } catch (Exception ignored) {
                    // Derby throws an exception on successful shutdown
                }

        } catch (Exception e) {
            // Ignore errors during shutdown in memory mode
        }
    }

}
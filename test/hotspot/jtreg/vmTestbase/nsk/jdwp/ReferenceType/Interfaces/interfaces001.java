/*
<<<<<<< HEAD
 * Copyright (c) 2001, 2020, Oracle and/or its affiliates. All rights reserved.
=======
 * Copyright (c) 2001, 2024, Oracle and/or its affiliates. All rights reserved.
>>>>>>> d032521c17215a93395974cf933ceea0982be2a9
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package nsk.jdwp.ReferenceType.Interfaces;

import java.io.*;
import java.util.*;

import nsk.share.*;
import nsk.share.jpda.*;
import nsk.share.jdwp.*;

public class interfaces001 {
    static final int JCK_STATUS_BASE = 95;
    static final int PASSED = 0;
    static final int FAILED = 2;
    static final String PACKAGE_NAME = "nsk.jdwp.ReferenceType.Interfaces";
    static final String TEST_CLASS_NAME = PACKAGE_NAME + "." + "interfaces001";
    static final String DEBUGEE_CLASS_NAME = TEST_CLASS_NAME + "a";

    static final String JDWP_COMMAND_NAME = "ReferenceType.Interfaces";
    static final int JDWP_COMMAND_ID = JDWP.Command.ReferenceType.Interfaces;

    static final String TESTED_CLASS_NAME = DEBUGEE_CLASS_NAME + "$" + "TestedClass";
    static final String TESTED_CLASS_SIGNATURE = "L" + TESTED_CLASS_NAME.replace('.', '/') + ";";

    static final String class_interfaces [][] = {
                    { DEBUGEE_CLASS_NAME + "$" + "TestedClassInterface1", "" },
                    { DEBUGEE_CLASS_NAME + "$" + "TestedClassInterface2", "" }
                };
    static final int DECLARED_INTERFACES = class_interfaces.length;
    static final long interfaceIDs[] = new long[DECLARED_INTERFACES];

    public static void main (String argv[]) {
        int result = run(argv, System.out);
        if (result != 0) {
            throw new RuntimeException("Test failed");
        }
    }

    public static int run(String argv[], PrintStream out) {
    return new interfaces001().runIt(argv, out);
    }

    public int runIt(String argv[], PrintStream out) {

        boolean success = true;

        try {
            ArgumentHandler argumentHandler = new ArgumentHandler(argv);
            Log log = new Log(out, argumentHandler);

            try {

                Binder binder = new Binder(argumentHandler, log);
                log.display("Start debugee VM");
                Debugee debugee = binder.bindToDebugee(DEBUGEE_CLASS_NAME);
                Transport transport = debugee.getTransport();
                IOPipe pipe = debugee.createIOPipe();

                log.display("Waiting for VM_INIT event");
                debugee.waitForVMInit();

                log.display("Querying for IDSizes");
                debugee.queryForIDSizes();

                log.display("Resume debugee VM");
                debugee.resume();

                log.display("Waiting for command: " + "ready");
                String cmd = pipe.readln();
                log.display("Received command: " + cmd);

                try {

                    log.display("Getting ReferenceTypeID for class signature: " + TESTED_CLASS_SIGNATURE);
                    long typeID = debugee.getReferenceTypeID(TESTED_CLASS_SIGNATURE);

                    for (int i = 0; i < DECLARED_INTERFACES; i++) {
                        class_interfaces[i][1] = "L" + class_interfaces[i][0].replace('.', '/') + ";";
                        log.display("Getting ReferenceTypeID for interface signature: " + class_interfaces[i][1]);
                        interfaceIDs[i] = debugee.getReferenceTypeID(class_interfaces[i][1]);
                    }

                    // begin test of JDWP command

                    log.display("Create command " + JDWP_COMMAND_NAME
                                + " with ReferenceTypeID: " + typeID);
                    CommandPacket command = new CommandPacket(JDWP_COMMAND_ID);
                    command.addReferenceTypeID(typeID);
                    command.setLength();

                    log.display("Sending command packet:\n" + command);
                    transport.write(command);

                    log.display("Waiting for reply packet");
                    ReplyPacket reply = new ReplyPacket();
                    transport.read(reply);
                    log.display("Reply packet received:\n" + reply);

                    log.display("Checking reply packet header");
                    reply.checkHeader(command.getPacketID());

                    log.display("Parsing reply packet:");
                    reply.resetPosition();

                    int interfaces = reply.getInt();
                    log.display("  interfaces: " + interfaces);

                    if (interfaces != DECLARED_INTERFACES) {
                        log.complain("Unexpected number of declared interfaces in the reply packet:" + interfaces
                                    + " (expected: " + DECLARED_INTERFACES + ")");
                        success = false;
                    }

                    for (int i = 0; i < interfaces; i++ ) {

                        log.display("  interface #" + i);

                        long interfaceID = reply.getReferenceTypeID();
                        log.display("    interfaceID: " + interfaceID);

                        if (interfaceID != interfaceIDs[i]) {
                            log.complain("Unexpected interface ID for interface #" + i + " in the reply packet: " + interfaceID
                                         + " (expected: " + interfaceIDs[i] + ")");
                            success = false;
                        }

                    }

                    if (! reply.isParsed()) {
                        log.complain("Extra trailing bytes found in reply packet at: " + reply.currentPosition());
                        success = false;
                    } else {
                        log.display("Reply packet parsed successfully");
                    }

                    // end test of JDWP command

                } catch (Exception e) {
                    log.complain("Caught exception while testing JDWP command: " + e);
                    success = false;
                } finally {
                    log.display("Sending command: " + "quit");
                    pipe.println("quit");

                    log.display("Waiting for debugee exits");
                    int code = debugee.waitFor();
                    if (code == JCK_STATUS_BASE + PASSED) {
                        log.display("Debugee PASSED with exit code: " + code);
                    } else {
                        log.complain("Debugee FAILED with exit code: " + code);
                        success = false;
                    }
                }

            } catch (Exception e) {
                log.complain("Caught unexpected exception while communicating with debugee: " + e);
                e.printStackTrace(out);
                success = false;
            }

            if (!success) {
                log.complain("TEST FAILED");
                return FAILED;
            }

        } catch (Exception e) {
            out.println("Caught unexpected exception while starting the test: " + e);
            e.printStackTrace(out);
            out.println("TEST FAILED");
            return FAILED;
        }

        out.println("TEST PASSED");
        return PASSED;

    }

}

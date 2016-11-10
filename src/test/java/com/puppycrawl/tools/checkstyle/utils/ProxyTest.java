////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2016 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////

package com.puppycrawl.tools.checkstyle.utils;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Date;

public final class ProxyTest {
    private static final byte[] BUFFER = new byte[4096];

    private static boolean started;
    private static int port = 8080;

    private ProxyTest() {
    }

    private static String getResourcePath(String filename) throws IOException {
        return new File("src/main/resources/com/puppycrawl/tools/checkstyle/checks" + filename)
                .getCanonicalPath();
    }

    private static String getSitePath(String filename) throws IOException {
        return new File("src/site/resources/files" + filename).getCanonicalPath();
    }

    public static void start() throws Exception {
        if (!started) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try (ServerSocket ss = new ServerSocket(port)) {
                        started = true;

                        System.setProperty("http.proxyHost", "127.0.0.1");
                        System.setProperty("http.proxyPort", String.valueOf(port));
                        System.setProperty("https.proxyHost", "127.0.0.1");
                        System.setProperty("https.proxyPort", String.valueOf(port));

                        while (!ss.isClosed()) {
                            workConnection(ss.accept());
                        }
                    }
                    catch (BindException ex) {
                        if (port < 10000) {
                            port++;
                            run();
                        }
                        else {
                            ex.printStackTrace();
                        }
                    }
                    catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();

            while (!started) {
                Thread.sleep(1000);
            }
        }
    }

    public static void workConnection(Socket client) throws IOException {
        try (InputStream fromClient = client.getInputStream();
                OutputStream toClient = client.getOutputStream()) {
            System.out.println("------------------------------------------------------");
            System.out.println("Working");
            System.out.println();
            System.out.flush();

            while (true) {
                if (client.isClosed()) {
                    break;
                }

                String content = read(fromClient);

                if (content == null) {
                    continue;
                }

                if (content.startsWith("GET ")) {
                    content = content.substring(4);

                    if (content.startsWith("http://checkstyle.sourceforge.net/dtds/")) {
                        final String fileName = content.substring(39, content.indexOf(' ', 39));

                        if (fileName.startsWith("import")) {
                            System.out.println("Managed");
                            writeFile(toClient, getResourcePath("/imports/" + fileName));
                        }
                    }
                    else if (content.indexOf("/suppressions_none.xml") != -1) {
                        System.out.println("Managed");
                        writeFile(toClient, getSitePath("/suppressions_none.xml"));
                    }
                }

                // no-writes will result in:
                // Caused by:
                // com.puppycrawl.tools.checkstyle.api.CheckstyleException:
                // unable to find <url>
                // Caused by: java.net.SocketException:
                // Unexpected end of file from server
                break;
            }
        }
        finally {
            client.close();
        }

        System.out.println("------------------------------------------------------");
        System.out.flush();
    }

    private static String read(InputStream input) throws IOException {
        String results = null;

        if (input.available() > 0) {
            final int bytesRead = input.read(BUFFER);
            if (bytesRead != -1) {
                results = new String(BUFFER, 0, bytesRead);

                System.out.println(results);
            }
        }

        return results;
    }

    private static void writeFile(OutputStream output, String fileName) throws IOException {
        final String content = new String(Files.readAllBytes(new File(fileName).toPath()), UTF_8);

        write(output,
                "HTTP/1.1 200 OK\r\nDate: " + new Date()
                        + "\r\nContent-Type: text/html\r\nContent-Length: " + content.length()
                        + "\r\n\r\n" + content);
        output.close();
    }

    private static void write(OutputStream out, String content) throws IOException {
        out.write(content.getBytes());
        out.flush();
    }
}

/*
 * IndiHTTP
 * Copyright (C) 2012 IndiPlex
 * 
 * IndiHTTP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.indiplex.indihttp.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author IndiPlex <Cartan12@indiplex.de>
 */
public class ClientThread extends Thread {

    static final String[][] mimetypes = {
        {"html", "text/html"},
        {"htm", "text/html"},
        {"txt", "text/plain"},
        {"gif", "image/gif"},
        {"jpg", "image/jpeg"},
        {"png", "image/png"},
        {"jpeg", "image/jpeg"},
        {"jnlp", "application/x-java-jnlp-file"}
    };
    private Socket socket;
    private int id;
    private PrintStream out;
    private InputStream in;
    private String cmd;
    private String url;
    private String httpversion;
    private WebServer httpd;
    private String sessionId;
    private Map<String, String> params = new HashMap<String, String>();

    public ClientThread(int id, Socket socket, WebServer httpd) {
        this.id = id;
        this.socket = socket;
        this.httpd = httpd;
    }

    @Override
    public void run() {
        try {
            in = socket.getInputStream();
            out = new PrintStream(socket.getOutputStream());
            if (readRequest()) {
                createResponse();
            }
            socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private boolean readRequest() throws IOException {
        ArrayList<String> request = new ArrayList<String>();
        StringBuilder buffer = new StringBuilder(200);

        int c = in.read();
        while (c != -1) {
            if (c == '\r') {
            } else if (c == '\n') {
                if (buffer.length() <= 0) {
                    break;
                } else {
                    request.add(buffer.toString());
                    buffer = new StringBuilder(200);
                }
            } else {
                buffer.append((char) c);
            }
            c = in.read();
        }
        if (request.isEmpty()) {
            return false;
        }

        String s = request.get(0);
        int p = s.indexOf(" ");
        int p2 = s.lastIndexOf(" ");
        if (p != -1) {
            cmd = s.substring(0, p);
            if (p != -1) {
                url = s.substring(p + 1, p2);
                httpversion = s.substring(p2);
            } else {
                url = s.substring(p + 1);
            }
        } else {
            url = s;
        }
        for (String r:request) {
            if (r.startsWith("Cookie: ")) {
                String cookie = r.substring(8);
                sessionId = cookie.split(";")[0];
            }
        }
        if (url.contains("?")) {
            String[] split = url.split("\\?", 2);
            url = split[0];
            String[] parts = split[1].split("&");
            for (String pa:parts) {
                String[] vals = pa.split("=", 2);
                params.put(vals[0], vals[1]);
            }
        }
        if (url.equalsIgnoreCase("/ihttplogin.html")) {
            String user = params.get("user");
            String pass = params.get("pass");
            if (user!=null && pass!=null) {
                if (user.equalsIgnoreCase("name") && pass.equals("passw")) {
                    httpd.getPerms().put(sessionId, true);
                }
            }
        }
        if (new File(httpd.getBaseFolder() + url).isDirectory()) {
            url = url + "index.html";
        }
        return true;
    }

    private void createResponse() {
        if (cmd.equals("GET") || cmd.equals("HEAD")) {
            if (!url.startsWith("/")) {
                out.print("HTTP/1.0 200 OK\r\n");
                out.print("Server: IndiHTTP-WebServer 0.5\r\n");
                out.print("Content-type: text/html\r\n\r\n");
                out.print("Bad request!");
                return;
            } else {
                String mimestring = "application/octet-stream";
                for (int i = 0; i < mimetypes.length; ++i) {
                    if (url.endsWith(mimetypes[i][0])) {
                        mimestring = mimetypes[i][1];
                        break;
                    }
                }
                out.print("HTTP/1.0 200 OK\r\n");
                out.print("Server: IndiHTTP-WebServer 0.5\r\n");
                FileInputStream is;
                try {
                    out.print("Content-type: " + mimestring + "\r\n\r\n");
                    if (cmd.equals("GET")) {
                        if (!mimestring.equals("text/html")) {
                            is = new FileInputStream(httpd.getBaseFolder() + url);
                            byte[] buf = new byte[256];
                            int len;
                            while ((len = is.read(buf)) != -1) {
                                out.write(buf, 0, len);
                            }
                            is.close();
                        } else {
                            BufferedReader br = new BufferedReader(new FileReader("web" + url));
                            while (br.ready()) {
                                String line = br.readLine();
                                if (line==null) {
                                    break;
                                }
                                Pattern patt = Pattern.compile("\\$\\{.*\\}");
                                Matcher matcher = patt.matcher(line);
                                while (matcher.find()) {
                                    String s = matcher.group();
                                    s = s.substring(2);
                                    s = s.substring(0, s.length()-1);
                                    String[] split = s.split("\\.");
                                    if (split.length==1) {
                                        Object g = httpd.getVars().get(s);
                                        if (g==null) {
                                            g = "Var "+s+" not found!";
                                        }
                                        line = matcher.replaceFirst(g.toString());
                                        matcher.reset(line);
                                    } else if (split.length==2) {
                                        String result = httpd.invokeMethod(split[0], split[1], sessionId);
                                        if (result==null) {
                                            result = "Invalid method or object!";
                                        }
                                        line = matcher.replaceFirst(result);
                                        matcher.reset(line);
                                    }
                                }
                                out.print(line + "\r\n");
                            }
                            out.flush();
                            br.close();
                        }
                    }
                } catch (FileNotFoundException ex) {
                    if (!url.contains("favicon.ico")) {
                        mimestring = "text/html";
                        out.print("Content-type: " + mimestring + "\r\n\r\n");
                        if (!url.endsWith("index.html")) {
                            out.print("Can't find " + url);
                        }
                    }
                } catch (IOException e) {
                    out.print("Internal server error!");
                }
            }
        } else {
            System.out.println("Error, not supported!");
        }
    }
}

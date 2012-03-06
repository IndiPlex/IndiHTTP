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

import de.indiplex.indihttp.iHTTPFunction;
import de.indiplex.indihttp.iHTTPStandardFunctions;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author IndiPlex <Cartan12@indiplex.de>
 */
public class WebServer {

    private ServerSocket httpd;
    private Listener listener;
    private String baseFolder;
    private Map<String, Object> vars;
    private Map<String, Object> clazz = new HashMap<String, Object>();
    private Map<Object, Map<String, Method>> methods = new HashMap<Object, Map<String, Method>>();
    private Map<String, Boolean> perms = new HashMap<String, Boolean>();

    public String invokeMethod(String clazz, String fName, String sessionId) {
        Object c = this.clazz.get(clazz);
        if (c == null) {
            System.out.println(clazz);
            return null;
        }
        Method m = methods.get(c).get(fName);
        if (m == null) {
            System.out.println(fName);
            return null;
        }
        try {
            if (m.getParameterTypes().length == 1) {
                Boolean b = perms.get(sessionId);
                if (b != null) {
                    return m.invoke(c, new Object[]{b}).toString();
                } else {
                    return m.invoke(c, new Object[]{false}).toString();
                }
            } else {
                return m.invoke(c, new Object[]{}).toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public Map<String, Boolean> getPerms() {
        return perms;
    }

    public void registerClass(String key, Object clazz) {
        this.clazz.put(key, clazz);
        Method[] meths = clazz.getClass().getMethods();
        Map<String, Method> get = methods.get(clazz);
        if (get == null) {
            get = new HashMap<String, Method>();
            methods.put(clazz, get);
        }

        for (Method m : meths) {
            if (m.isAnnotationPresent(iHTTPFunction.class)) {
                get.put(m.getName(), m);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Map<String, Object> vars = new HashMap<String, Object>();
        WebServer app = new WebServer(vars, "web");
        app.registerClass("ihttp", new iHTTPStandardFunctions(null));
        app.start(4800);
        Thread.sleep(60 * 1000);
        app.stop();
    }

    public Map<Object, Map<String, Method>> getMethods() {
        return methods;
    }

    public WebServer(Map<String, Object> vars, String baseFolder) {
        this.vars = vars;
        this.baseFolder = baseFolder;
        File file = new File(baseFolder);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public String getBaseFolder() {
        return baseFolder;
    }

    public Map<String, Object> getClazz() {
        return clazz;
    }

    public Map<String, Object> getVars() {
        return vars;
    }

    public void start(int port) {
        try {
            httpd = new ServerSocket(port);
            listener = new Listener(this);
            new Thread(listener).start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void stop() {
        try {
            httpd.close();
            listener.setRunning(false);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private class Listener implements Runnable {

        private boolean running = true;
        private WebServer ht;

        public Listener(WebServer ht) {
            this.ht = ht;
        }

        @Override
        public void run() {
            int currClient = 0;
            while (running) {
                Socket sock;
                try {
                    sock = httpd.accept();
                    (new ClientThread(++currClient, sock, ht)).start();
                } catch (IOException ex) {
                    break;
                }
            }
        }

        public void setRunning(boolean running) {
            this.running = running;
        }
    }
}

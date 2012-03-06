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
package de.indiplex.indihttp;

import de.indiplex.indihttp.webserver.WebServer;
import de.indiplex.manager.IPMAPI;
import de.indiplex.manager.IPMPlugin;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author IndiPlex <Cartan12@indiplex.de>
 */
public class IHTTP extends IPMPlugin {
    
    private static final String pre = "[IHTTP] ";
    private WebServer httpd;
    private Map<String, Object> vars;

    @Override
    public void onDisable() {
        printDisabled(pre);
    }

    @Override
    public void onEnable() {
        setupConfig();
        readVars();
        int port = getAPI().getConfig().getInt("options.port", 4800);
        String baseDir = getAPI().getConfig().getString("options.dir", "web");
        
        httpd = new WebServer(vars, baseDir);
        httpd.start(port);
        log.info(pre+"Started webserver on port "+port);
        ihttpAPI api = new ihttpAPI(this);
        getAPI().registerAPI("ihttp", api);
        printEnabled(pre);
        api.registerClass("ihttp", new iHTTPStandardFunctions(this));
    }
    
    private void setupConfig() {
        YamlConfiguration config = getAPI().getConfig();
        if (config.getInt("options.port", -1)==-1) {
            config.set("options.port", 4800);
        }
    }

    public WebServer getHttpd() {
        return httpd;
    }
    
    private void readVars() {
        try {
            Properties props = new Properties();
            File fProps = new File(getAPI().getDataFolder(), "vars");
            if (!fProps.exists()) {
                fProps.createNewFile();
            }
            props.load(new FileReader(fProps));
            vars = (Map) props;
        } catch (IOException ex) {
            log.warning(pre+"Can't read property file!");
        }
    }

    public Map<String, Object> getVars() {
        return vars;
    }
    
    private static IPMAPI API;

    @Override
    protected void init(IPMAPI API) {
        IHTTP.API = API;
    }

    public static IPMAPI getAPI() {
        return API;
    }
    
}

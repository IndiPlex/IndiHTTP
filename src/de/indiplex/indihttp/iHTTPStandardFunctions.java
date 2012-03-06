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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 *
 * @author IndiPlex <Cartan12@indiplex.de>
 */
public class iHTTPStandardFunctions {
    
    private IHTTP ihttp;

    public iHTTPStandardFunctions(IHTTP ihttp) {
        this.ihttp = ihttp;
    }
    
    @iHTTPFunction
    public String test() {
        return "Just a test ;)";
    }
    
    @iHTTPFunction
    public String getPlayers() {
        String retString = "";
        Player[] onlinePlayers = Bukkit.getOnlinePlayers();
        for (Player p:onlinePlayers) {
            retString += p.getName()+"<br>";
        }
        return retString;
    }
    
    @iHTTPFunction
    public String hasPerms(boolean perms) {
        if (perms) {
            return "You are logged in!";
        } else {
            return "You are not logged in!";
        }
    }
    
}

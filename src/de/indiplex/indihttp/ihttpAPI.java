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

import de.indiplex.manager.API;

/**
 *
 * @author IndiPlex <Cartan12@indiplex.de>
 */
public class ihttpAPI implements API {
    private IHTTP ihttp;

    public ihttpAPI(IHTTP ihttp) {
        this.ihttp = ihttp;
    }
    
    public void registerVar(String key, Object val) {
        ihttp.getVars().put(key, val);
    }
    
    public void removeVar(String key) {
        ihttp.getVars().remove(key);
    }
    
    public void registerClass(String key, Object clazz) {
        ihttp.getHttpd().registerClass(key, clazz);
    }
    
    public void removeClass(String key) {
        ihttp.getHttpd().getClazz().remove(key);
    }
    
}

/*
 * Copyright 2015 Tomáš Šmíd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cz.muni.fi.VBoxAndVMsManager.machines;

import java.util.Objects;

/**
 *
 * @author Tomáš Šmíd
 */
public final class PhysicalMachine implements Comparable<PhysicalMachine>{
    
    private final String addressIP;
    private final String portOfVBoxWebServer;
    private final String username;
    private final String userPassword;
    
    public PhysicalMachine(String addressIP, String vboxWebServerPort,
                           String username, String userPassword){
        
        this.portOfVBoxWebServer = vboxWebServerPort;
        this.addressIP = addressIP;
        this.username = username;
        this.userPassword = userPassword;
    }

    public String getAddressIP() {
        return addressIP;
    }
    
    public String getVBoxWebServerPort(){
        return portOfVBoxWebServer;
    }

    public String getUsername() {
        return username;
    }

    public String getUserPassword() {
        return userPassword;
    }
    
    @Override
    public boolean equals(Object other){
        if(other == this) return true;
        if(other == null) return false;
        if(getClass() != other.getClass()) return false;
        PhysicalMachine pm = (PhysicalMachine)other;
        return (this.addressIP == pm.addressIP ||
                (this.addressIP != null && this.addressIP.equals(pm.addressIP))) &&
               (this.portOfVBoxWebServer == pm.portOfVBoxWebServer) ||
                (this.portOfVBoxWebServer != null &&
                 this.portOfVBoxWebServer.equals(pm.portOfVBoxWebServer));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.addressIP);
        hash = 67 * hash + Objects.hashCode(this.portOfVBoxWebServer);
        return hash;
    }    

    @Override
    public int compareTo(PhysicalMachine pm) {
        int comp_ip = addressIP.compareTo(pm.addressIP);
        int comp_ws = portOfVBoxWebServer.compareTo(pm.portOfVBoxWebServer);
        
        if(comp_ip > 0) return 1;
        if(comp_ip < 0) return -1;
        if(comp_ws > 0) return 1;
        if(comp_ws < 0) return -1;
        return 0;
    }
    
    
    
}

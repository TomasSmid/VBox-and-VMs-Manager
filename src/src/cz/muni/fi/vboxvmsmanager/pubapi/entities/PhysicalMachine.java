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
package cz.muni.fi.vboxvmsmanager.pubapi.entities;

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
    
    public PhysicalMachine(String addressIP, String websrvPort, String username, String userPassword){
        this.addressIP = addressIP;
        this.portOfVBoxWebServer = websrvPort;
        this.username = username;
        this.userPassword = userPassword;
    }

    public String getAddressIP() {
        return addressIP;
    }

    public String getPortOfVBoxWebServer() {
        return portOfVBoxWebServer;
    }

    public String getUsername() {
        return username;
    }

    public String getUserPassword() {
        return userPassword;
    }
    
    @Override
    public boolean equals(Object obj){
        if(obj == this) return true;
        if(!(obj instanceof PhysicalMachine)) return false;
        PhysicalMachine pm = (PhysicalMachine)obj;
        return ((this.addressIP == pm.addressIP) || 
                (this.addressIP != null && this.addressIP.equals(pm.addressIP))) &&
               ((this.portOfVBoxWebServer == pm.portOfVBoxWebServer) ||
                (this.portOfVBoxWebServer != null && this.portOfVBoxWebServer.equals(pm.portOfVBoxWebServer)));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.addressIP);
        hash = 89 * hash + Objects.hashCode(this.portOfVBoxWebServer);
        return hash;
    }
    
    @Override
    public String toString(){
        return "[" + "Physical machine: IP address=" + this.addressIP + "]"; 
    }
    
    @Override
    public int compareTo(PhysicalMachine pm){
        int result = this.addressIP.compareTo(pm.addressIP);
        return (result == 0 ? this.portOfVBoxWebServer.compareTo(pm.portOfVBoxWebServer) : result);
    }

    /*@Override
    public int compareTo(PhysicalMachine pm) {
        int[] dec1 = getIPDecimals(this.addressIP);
        int[] dec2 = getIPDecimals(pm.addressIP);
        int websrvPort1 = Integer.parseInt(this.portOfVBoxWebServer);
        int websrvPort2 = Integer.parseInt(this.portOfVBoxWebServer);
        
        for(int i = 0; i < 4; ++i){
            if(dec1[i] < dec2[i]) return -1;
            if(dec1[i] > dec2[i]) return 1;
        }
        if(websrvPort1 < websrvPort2) return -1;
        if(websrvPort1 > websrvPort2) return 1;
        return 0;
    }
    
    private int[] getIPDecimals(String ip){
        String[] strDec = ip.split(".");
        int[] decimals = new int[4];
        
        for(int i = 0; i < 4; ++i){
            decimals[i] = Integer.parseInt(strDec[i]);
        }
        
        return decimals;
    }*/
}

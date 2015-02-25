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

package cz.muni.fi.VBoxAndVMsManager.pubcon;

import java.util.Objects;

/**
 *
 * @author Tomáš Šmíd
 */
public final class PortRule implements Comparable<PortRule>{
    
    private final String ruleName;
    private final String protocol;
    private final String hostIP;
    private final int hostPort;
    private final String guestIP;
    private final int guestPort;
    
    public PortRule(String ruleName, String protocol, String hostIP,
                             int hostPort, String guestIP, int guestPort){
        this.ruleName = ruleName;
        this.protocol = protocol;
        this.hostIP = hostIP;
        this.hostPort = hostPort;
        this.guestIP = guestIP;
        this.guestPort = guestPort;
    }

    public String getRuleName() {
        return ruleName;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHostIP() {
        return hostIP;
    }

    public int getHostPort() {
        return hostPort;
    }

    public String getGuestIP() {
        return guestIP;
    }

    public int getGuestPort() {
        return guestPort;
    }
    
    @Override
    public boolean equals(Object other){
        if(other == this) return true;
        if(other == null) return false;
        if(getClass() != other.getClass()) return false;
        PortRule rule = (PortRule)other;
        return (this.ruleName == rule.ruleName ||
                this.ruleName != null && this.ruleName.equals(rule.ruleName));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.ruleName);
        return hash;
    }
    
    @Override
    public int compareTo(PortRule rule){
        return this.ruleName.compareTo(rule.ruleName);
    }
}

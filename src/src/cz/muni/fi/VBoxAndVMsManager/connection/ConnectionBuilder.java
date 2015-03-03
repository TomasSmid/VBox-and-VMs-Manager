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

package cz.muni.fi.VBoxAndVMsManager.connection;

import cz.muni.fi.VBoxAndVMsManager.machines.PhysicalMachine;
import org.virtualbox_4_3.VBoxException;
import org.virtualbox_4_3.VirtualBoxManager;

/**
 *
 * @author Tomáš Šmíd
 */
public final class ConnectionBuilder {
    
    private ConnectionBuilder(){}
    
    public static VirtualBoxManager connectTo(PhysicalMachine physicalMachine){
        VirtualBoxManager vbm = VirtualBoxManager.createInstance(null);
        
        try{
            System.out.println("Connecting to http://" + 
                               physicalMachine.getAddressIP() + ":" +
                               physicalMachine.getVBoxWebServerPort());
            String url = "http://" + physicalMachine.getAddressIP() +
                         ":" + physicalMachine.getVBoxWebServerPort();
            vbm.connect(url, physicalMachine.getUsername(),
                        physicalMachine.getUserPassword());
            System.out.println("Connection succeeded");
        }catch(VBoxException ex){
            System.err.println("Connection failed:");
            System.err.println(ex.getMessage());
            System.err.println("Check, whether used IP address, VirtualBox " +
                               "web server port,username and user password " +
                               "are correct and whether there is VBox" +
                               "webserver running on a machine you " +
                               "want to connect.");
            return null;
        }
        
        return vbm;
    }
}

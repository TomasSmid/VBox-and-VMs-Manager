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
package cz.muni.fi.vboxvmsmanager.logicimpl;

import cz.muni.fi.vboxvmsmanager.pubapi.entities.PhysicalMachine;
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.ConnectionFailureException;
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.DisconnectionFailureException;
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.IncompatibleVirtToolAPIVersionException;
import cz.muni.fi.vboxvmsmanager.pubapi.managers.ConnectionManager;
import cz.muni.fi.vboxvmsmanager.pubapi.managers.VirtualizationToolManager;
import java.util.List;

/**
 *
 * @author Tomáš Šmíd
 */
public class ConnectionManagerImpl implements ConnectionManager{

    @Override
    public VirtualizationToolManager connectTo(PhysicalMachine physicalMachine) {
        NativeVBoxAPIConnection natapiCon = NativeVBoxAPIConnection.getInstance();
        
        try{
            natapiCon.connectTo(physicalMachine);
        }catch(ConnectionFailureException | IncompatibleVirtToolAPIVersionException | InterruptedException ex){
            System.err.println(ex.getMessage());
            return null;
        }
        
        System.out.println("Physical machine " + physicalMachine + " successfully connected");
        
        return new VirtualizationToolManagerImpl(physicalMachine);
    }

    @Override
    public void disconnectFrom(PhysicalMachine physicalMachine) {
        NativeVBoxAPIConnection natapiCon = NativeVBoxAPIConnection.getInstance();
        boolean error = false;
        
        try{
            natapiCon.disconnectFrom(physicalMachine);
        }catch(DisconnectionFailureException ex){
            System.err.println(ex.getMessage());
            error = true;
        }
        
        if(!error){
            System.out.println("Physical machine " + physicalMachine + " disconnected");
        }
    }

    @Override
    public void close() {
        NativeVBoxAPIConnection natapiCon = NativeVBoxAPIConnection.getInstance();
        List<PhysicalMachine> conMachs = natapiCon.getConnectedPhysicalMachines();
        
        if(!conMachs.isEmpty()){
            conMachs.stream().forEach((pm) -> {
                disconnectFrom(pm);
            });
        }        
    }
    
}

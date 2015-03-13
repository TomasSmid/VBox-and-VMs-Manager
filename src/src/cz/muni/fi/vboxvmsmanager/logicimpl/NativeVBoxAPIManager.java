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
import cz.muni.fi.vboxvmsmanager.pubapi.entities.VirtualMachine;
import java.util.List;

/**
 *
 * @author Tomáš Šmíd
 */
public class NativeVBoxAPIManager {
    //pri implementaci tridy udelat ze tridy singleton
    
    public VirtualMachine getVirtualMachineById(PhysicalMachine physicalMachine, Long id){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public VirtualMachine getVirtualMachineByName(PhysicalMachine physicalMachine, String name){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public List<VirtualMachine> getVirtualMachines(PhysicalMachine physicalMachine){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getVBoxVersion(PhysicalMachine physicalMachine){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void createFullClone(VirtualMachine virtualMachine){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void createLinkedClone(VirtualMachine virtualMachine){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void transportVirtualMachine(VirtualMachine virtualMachine, PhysicalMachine physicalMachine){
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

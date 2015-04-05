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
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.ConnectionFailureException;
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.IncompatibleVirtToolAPIVersionException;
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.UnexpectedVMStateException;
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.UnknownVirtualMachineException;
import cz.muni.fi.vboxvmsmanager.pubapi.managers.VirtualMachineManager;
import cz.muni.fi.vboxvmsmanager.pubapi.managers.VirtualizationToolManager;
import cz.muni.fi.vboxvmsmanager.pubapi.types.CloneType;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Tomáš Šmíd
 */
public class VirtualizationToolManagerImpl implements VirtualizationToolManager{
    
    private PhysicalMachine hostMachine;
    
    public VirtualizationToolManagerImpl(PhysicalMachine hostMachine){
        this.hostMachine = hostMachine;
    }
    
    @Override
    public VirtualMachine findVirtualMachineById(UUID id) {
        NativeVBoxAPIManager natapiMan = NativeVBoxAPIManager.getInstance();
        VirtualMachine virtualMachine = null;
        
        try{
            virtualMachine = natapiMan.getVirtualMachineById(hostMachine, id);
        } catch (InterruptedException | ConnectionFailureException | IncompatibleVirtToolAPIVersionException
                | UnknownVirtualMachineException | UnexpectedVMStateException | IllegalArgumentException ex) {

            System.err.println(ex.getMessage());
        }

        return virtualMachine;
    }

    @Override
    public VirtualMachine findVirtualMachineByName(String name) {
        NativeVBoxAPIManager natapiMan = NativeVBoxAPIManager.getInstance();
        VirtualMachine virtualMachine = null;
        
        try{
            virtualMachine = natapiMan.getVirtualMachineByName(hostMachine, name);
        } catch (InterruptedException | ConnectionFailureException | IncompatibleVirtToolAPIVersionException
                | UnknownVirtualMachineException | UnexpectedVMStateException | IllegalArgumentException ex) {

            System.err.println(ex.getMessage());
        }
        
        return virtualMachine;
    }

    @Override
    public List<VirtualMachine> getVirtualMachines() {
        NativeVBoxAPIManager natapiMan = NativeVBoxAPIManager.getInstance();
        List<VirtualMachine> virtualMachines = null;
        
        try{
            virtualMachines = natapiMan.getVirtualMachines(hostMachine);
        } catch (InterruptedException | ConnectionFailureException | IncompatibleVirtToolAPIVersionException
                | UnexpectedVMStateException | IllegalArgumentException ex) {
            System.err.println(ex.getMessage());
        }
        
        return virtualMachines;
    }

    @Override
    public void removeVirtualMachine(VirtualMachine virtualMachine) {
        NativeVBoxAPIManager natapiMan = NativeVBoxAPIManager.getInstance();
        boolean error = false;
        
        if(virtualMachine != null){
            System.out.println("Removing virtual machine " + virtualMachine + " from physical machine " + hostMachine);
            try{
                natapiMan.removeVirtualMachine(virtualMachine);
            } catch (InterruptedException | ConnectionFailureException | IncompatibleVirtToolAPIVersionException
                    | UnknownVirtualMachineException | UnexpectedVMStateException | IllegalArgumentException ex) {
                
                System.err.println(ex.getMessage());
                error = true;
            }
            
            if(!error){
                System.out.println("Removing finished successfully");
            }
        }else{
            System.err.println("Removing virtual machine failure: There was made an attempt to remove a null virtual machine.");
        }
    }

    @Override
    public VirtualMachine cloneVirtualMachine(VirtualMachine virtualMachine, CloneType type) {
        NativeVBoxAPIManager natapiMan = NativeVBoxAPIManager.getInstance();
        VirtualMachine cloneMachine = null;
        boolean error = false;
        
        if(virtualMachine != null){
            System.out.println("Cloning virtual machine " + virtualMachine + " on physical machine " + hostMachine);
            try{
                cloneMachine = natapiMan.createVMClone(virtualMachine, type);
            } catch (InterruptedException | ConnectionFailureException | IncompatibleVirtToolAPIVersionException
                    | UnknownVirtualMachineException | UnexpectedVMStateException | IllegalArgumentException ex) {
                
                System.err.println(ex.getMessage());
                error = true;
            }
            
            if(!error){
                System.out.println("Cloning finished successfully");
            }
        }else{
            System.err.println("Cloning virtual machine failure: There was made an attempt to clone a null virtual machine.");
        }
        
        return cloneMachine;
    }

    @Override
    public VirtualMachineManager getVirtualMachineManager() {
        return VirtualMachineManagerImpl.getInstance();
    }
}

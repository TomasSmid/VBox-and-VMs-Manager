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

package cz.muni.fi.VBoxAndVMsManager.logic;

import cz.muni.fi.VBoxAndVMsManager.connection.ConnectionBuilder;
import cz.muni.fi.VBoxAndVMsManager.machines.IVirtualMachine;
import cz.muni.fi.VBoxAndVMsManager.machines.PhysicalMachine;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.virtualbox_4_3.IMachine;
import org.virtualbox_4_3.IVirtualBox;
import org.virtualbox_4_3.VirtualBoxManager;

/**
 *
 * @author Tomáš Šmíd
 */
public class VirtualMachinesManager {
    
    private static boolean instanceCreated = false;        
    
    public static void setToInstanceCreated(){
        instanceCreated = true;
    }
    
    public static boolean getInstanceCreationInfo(){
        return instanceCreated;
    }
    
    
    
    private Map<PhysicalMachine, MansAndVMsContainer> machinesAndVBoxManager;
    
    public VirtualMachinesManager(){
        this(null);
    }    
    
    public VirtualMachinesManager(Collection<PhysicalMachine> physicalMachines){
        if(!getInstanceCreationInfo()){                    
            machinesAndVBoxManager = new HashMap<>();

            setToInstanceCreated();
            if(physicalMachines != null){
                connectTo(physicalMachines);
            }
        }
    }
    
    public void close(){
        if(!machinesAndVBoxManager.isEmpty()){
            for(PhysicalMachine pm : machinesAndVBoxManager.keySet()){                
                System.out.println("Terminating work with all virtual " +
                                   "machines on physical machine \"" + 
                                   pm.getAddressIP() + "\" and disconnecting from");
                MansAndVMsContainer container = machinesAndVBoxManager.get(pm);
                if(container != null)
                    terminateWorkWithVMsOn(container);
                else
                    System.out.println("\"c\" is empty");
                disconnect(machinesAndVBoxManager.get(pm));   
            }
            machinesAndVBoxManager.clear();
            System.out.println("All virtual machines and VirtualBox managers " +
                               "were terminated successfully");
        }
    }
    
    public void connectTo(PhysicalMachine physicalMachine){
        VirtualBoxManager vbm = ConnectionBuilder.connectTo(physicalMachine);
        if(vbm != null){
            MansAndVMsContainer container = new MansAndVMsContainer(vbm);
            machinesAndVBoxManager.put(physicalMachine, container);
        }
    }
    
    public final void connectTo(Collection<PhysicalMachine> physicalMachines){
        for(PhysicalMachine pm : physicalMachines){
            connectTo(pm);
        }
    }
    
    public Map<PhysicalMachine, Collection<String>> getAllVirtualMachinesImages(){        
        Map<PhysicalMachine,Collection<String>> allImages = new HashMap<>();
        
        for(PhysicalMachine pm : machinesAndVBoxManager.keySet()){
            Collection<String> machImages = null;
            try{
                machImages = getVMsImagesFrom(machinesAndVBoxManager.get(pm));
            }catch(NullPointerException ex){
                System.err.println(ex);
                System.err.println("Unexpected state: Possibly there is " +
                                   "not correctly installed Virtual Box " +
                                   "on a host machine " + pm.getAddressIP());
            }
            if(machImages != null){
                allImages.put(pm, machImages);
            }
        }
        
        if(allImages.isEmpty())
            return null;
        else
            return allImages;
    }
    
    public Collection<String> getVirtualMachinesImagesFrom(PhysicalMachine pm){
        Collection<String> images = null;
        
        if(!machinesAndVBoxManager.containsKey(pm)){
            connectTo(pm);
            if(!machinesAndVBoxManager.containsKey(pm)){
                return null;
            }
        }  
        
        try{
            images = getVMsImagesFrom(machinesAndVBoxManager.get(pm));
        }catch(NullPointerException ex){
            System.err.println(ex);
            System.err.println("Unexpected state: Possibly there is " +
                               "not correctly installed Virtual Box " +
                               "on a host machine " + pm.getAddressIP());
        }   
        
        return images;
    }
    
    public IVirtualMachine getVirtualMachineInstance(PhysicalMachine physicalMachine,
                                                     String virtualMachineName){
        IVirtualMachine virtualMachine = null;
        MansAndVMsContainer container = machinesAndVBoxManager.get(physicalMachine);
        if(container != null){
            Collection<IVirtualMachine> instMachines = 
                                    container.getVirtualMachines();
            
            for(IVirtualMachine vm : instMachines){
                if(virtualMachineName.equals(vm.getName())) return null;
            }
            
            virtualMachine = new VirtualMachine(physicalMachine,
                                                container.getVirtualBoxManager(),
                                                virtualMachineName);
            machinesAndVBoxManager.get(physicalMachine).addVirtualMachine(virtualMachine);
        }        
        
        return virtualMachine;
    }
    
    private Collection<String> getVMsImagesFrom(MansAndVMsContainer container){
        Collection<String> images = null;
        IVirtualBox vbox = container.getVirtualBoxManager().getVBox();
        if(vbox != null){
            images = new ArrayList();
            List<IMachine> machines = vbox.getMachines();
            for(IMachine m : machines){
                images.add(m.getName());
            }
        }  
        return images;
    }
    
    private void terminateWorkWithVMsOn(MansAndVMsContainer container){
        Collection<IVirtualMachine> virtualMachines = container.getVirtualMachines();
        
        if(virtualMachines != null && !virtualMachines.isEmpty()){
            for(IVirtualMachine vm : virtualMachines){
                if (vm != null){
                    if(vm.getState().equals("Running") || vm.getState().equals("Paused") ||
                       vm.getState().equals("Stuck")){
                        vm.shutDown();             
                    }
                }
            }
        }        
    }
    
    private void disconnect(MansAndVMsContainer container){
        container.getVirtualBoxManager().disconnect();
    }
}

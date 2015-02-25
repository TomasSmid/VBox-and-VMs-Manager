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


import cz.muni.fi.VBoxAndVMsManager.machines.IVirtualMachine;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import org.virtualbox_4_3.VirtualBoxManager;

/**
 *
 * @author Tomáš Šmíd
 */
final class MansAndVMsContainer{
    
    private VirtualBoxManager virtualBoxManager;
    private Collection<IVirtualMachine> virtualMachines;
    
    public MansAndVMsContainer(VirtualBoxManager vbm){
        this(vbm, null);
    }
    
    public MansAndVMsContainer(VirtualBoxManager vbm, 
                               Collection<IVirtualMachine> virtualMachines){
        this.virtualBoxManager = vbm;
        if(virtualMachines == null)
            this.virtualMachines = new HashSet<>();
        else
            this.virtualMachines = new HashSet<>(virtualMachines);
    }

    public VirtualBoxManager getVirtualBoxManager() {
        return virtualBoxManager;
    }

    public Collection<IVirtualMachine> getVirtualMachines() {
        return virtualMachines;
    }
    
    public boolean addVirtualMachine(IVirtualMachine vm){
        return virtualMachines.add(vm);
    }
    
    public boolean removeVirtualMachine(IVirtualMachine vm){
        return virtualMachines.remove(vm);
    }
    
    @Override
    public boolean equals(Object other){
        if(other == this) return true;
        if(other == null) return false;
        if(getClass() != other.getClass()) return false;
        MansAndVMsContainer container = (MansAndVMsContainer)other;
        return (this.virtualBoxManager == container.virtualBoxManager ||
                this.virtualBoxManager != null &&
                 this.virtualBoxManager.equals(container.virtualBoxManager)); 
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.virtualBoxManager);
        return hash;
    }
}

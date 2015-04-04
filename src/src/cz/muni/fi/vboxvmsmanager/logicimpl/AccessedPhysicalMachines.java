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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tomáš Šmíd
 */
class AccessedPhysicalMachines {
    
    private static final AccessedPhysicalMachines INSTANCE = new AccessedPhysicalMachines();
    private static List<PhysicalMachine> accessedPhysicalMachines = new ArrayList<>();
    
    public static AccessedPhysicalMachines getInstance(){
        return INSTANCE;
    }
    
    private static void addAPM(PhysicalMachine physicalMachine){
        accessedPhysicalMachines.add(physicalMachine);
    }
    
    private static boolean removeAPM(PhysicalMachine physicalMachine){       
        accessedPhysicalMachines.remove(physicalMachine);
        return true;
    }
    
    private static boolean isAccessedPM(PhysicalMachine physicalMachine){
        return accessedPhysicalMachines.contains(physicalMachine);
    }
    
    private static List<PhysicalMachine> getAccessedPMs(){
        return accessedPhysicalMachines;
    }
    
    private AccessedPhysicalMachines(){ }
    
    public void add(PhysicalMachine physicalMachine){
        if(physicalMachine == null){
            throw new IllegalArgumentException("Adding new accessed PM failure: "
                    + "There was made an attempt to add an illegal (null) physical "
                    + "machine object to the list of accessed physical machines.");
        }else{
            addAPM(physicalMachine);
        }
    }
    
    public boolean remove(PhysicalMachine physicalMachine){
        if(physicalMachine == null){
            throw new IllegalArgumentException("Removing accessed PM failure: "
                    + "There was made an attempt to remove an illegal (null) "
                    + "physical machine object from the list of accessed physical machines.");
        }else{
            return (isAccessedPM(physicalMachine) ? removeAPM(physicalMachine) : false);
        }
    }
    
    public boolean isAccessed(PhysicalMachine physicalMachine){
        if(physicalMachine == null){
            throw new IllegalArgumentException("Method isAccessed failure: There was made an attempt "
                    + "to query with illegal (null) physical machine object.");
        }else{
            return isAccessedPM(physicalMachine);
        }
    }
    
    public List<PhysicalMachine> getAccessedPhysicalMachines(){
        return getAccessedPMs();
    }
}

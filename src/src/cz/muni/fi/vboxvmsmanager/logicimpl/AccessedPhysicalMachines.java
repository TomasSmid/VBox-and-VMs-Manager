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
public class AccessedPhysicalMachines {
    
    private static final AccessedPhysicalMachines INSTANCE = new AccessedPhysicalMachines();
    private static List<PhysicalMachine> accessedPhysicalMachines = new ArrayList<>();
    
    public static AccessedPhysicalMachines getInstance(){
        return INSTANCE;
    }
    
    private static void addAPM(PhysicalMachine physicalMachine){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private static boolean removeAPM(PhysicalMachine physicalMachine){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private static boolean isAccessedPM(PhysicalMachine physicalMachine){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private AccessedPhysicalMachines(){ }
    
    public void add(PhysicalMachine physicalMachine){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public boolean remove(PhysicalMachine physicalMachine){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public boolean isAccessed(PhysicalMachine physicalMachine){
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

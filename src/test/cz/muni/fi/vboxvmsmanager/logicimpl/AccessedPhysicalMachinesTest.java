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
import java.util.List;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tomáš Šmíd
 */
public class AccessedPhysicalMachinesTest {

    private List<PhysicalMachine> physicalMachines;
    
    @After
    public void cleanup(){
        if(physicalMachines != null){
            physicalMachines.stream().forEach((pm) -> {
                AccessedPhysicalMachines.remove(pm);
            });
            physicalMachines.clear();
        }
    }
    
    @Test
    public void addAccessedMachine(){
        PhysicalMachine pm = new PhysicalMachine("140.150.12.0","10000","Hornd","140nb48");
        physicalMachines.add(pm); //for after test cleanup
        
        assertNotNull("Physical machine " + pm.toString() + " should be correctly instantiated, not null",pm);
        assertFalse("Physical machine " + pm.toString() + " should not be accessed", AccessedPhysicalMachines.isAccessed(pm));
        AccessedPhysicalMachines.add(pm);
        assertTrue("Physical machine " + pm.toString() + " should be accessed",AccessedPhysicalMachines.isAccessed(pm));
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void addAccessedMachineNull(){
        AccessedPhysicalMachines.add(null);
    }
    
    @Test
    public void removeAccessedMachine(){
        PhysicalMachine pm1 = new PhysicalMachine("140.150.12.0","10000","Hornd","140nb48");
        PhysicalMachine pm2 = new PhysicalMachine("140.150.10.10","10000","","");
        physicalMachines.add(pm1);//for after test cleanup
        physicalMachines.add(pm2);//for after test cleanup
        
        assertFalse("Physical machine " + pm1.toString() + " should not be accessed", AccessedPhysicalMachines.isAccessed(pm1));
        assertFalse("Physical machine " + pm2.toString() + " should not be accessed", AccessedPhysicalMachines.isAccessed(pm2));
        
        AccessedPhysicalMachines.add(pm1);
        AccessedPhysicalMachines.add(pm2);
        
        assertTrue("Physical machine " + pm1.toString() + " should be accessed",AccessedPhysicalMachines.isAccessed(pm1));
        assertTrue("Physical machine " + pm2.toString() + " should be accessed",AccessedPhysicalMachines.isAccessed(pm2));
        
        AccessedPhysicalMachines.remove(pm1);
        
        assertFalse("Physical machine " + pm1.toString() + " should not be accessed", AccessedPhysicalMachines.isAccessed(pm1));
        assertTrue("Physical machine " + pm2.toString() + " should be accessed",AccessedPhysicalMachines.isAccessed(pm2));
    }
    
    @Test
    public void removeAccessedMachineNull(){
        PhysicalMachine pm = new PhysicalMachine("140.150.12.0","10000","Hornd","140nb48");
        physicalMachines.add(pm);//for after test cleanup
        
        AccessedPhysicalMachines.add(pm);
        try{
            AccessedPhysicalMachines.remove(null);
            fail();
        }catch(IllegalArgumentException ex){
            assertTrue("Physical machine " + pm.toString() + " should be accessed",AccessedPhysicalMachines.isAccessed(pm));
        }
    }
    
    @Test
    public void isNotMachineAccessed(){
        PhysicalMachine pm = new PhysicalMachine("140.150.12.0","10000","Hornd","140nb48");
        
        assertNotNull("Physical machine " + pm.toString() + " should be correctly instantiated, not null",pm);
        assertFalse("Physical machine " + pm.toString() + " should not be accessed", AccessedPhysicalMachines.isAccessed(pm));        
    }
    
    @Test
    public void isMachineAccessed(){
        PhysicalMachine pm = new PhysicalMachine("140.150.12.0","10000","Hornd","140nb48");
        physicalMachines.add(pm);//for after test cleanup
        
        assertNotNull("Physical machine " + pm.toString() + " should be correctly instantiated, not null",pm);
        AccessedPhysicalMachines.add(pm);
        assertTrue("Physical machine " + pm.toString() + " should be accessed",AccessedPhysicalMachines.isAccessed(pm));
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void isMachineAccessedNull(){
        AccessedPhysicalMachines.isAccessed(null);
    }
}

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
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 *
 * @author Tomáš Šmíd
 */
public class AccessedPhysicalMachinesTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    
    private AccessedPhysicalMachines sut;
    private List<PhysicalMachine> physicalMachines;
    
    public AccessedPhysicalMachinesTest(){
        physicalMachines = new ArrayList<>();
    }
    
    @Before
    public void setUp(){
        sut = AccessedPhysicalMachines.getInstance();
    }
    
    @After
    public void cleanup(){
        if(physicalMachines != null){
            physicalMachines.stream().forEach((pm) -> {
                sut.remove(pm);
            });
            physicalMachines.clear();
        }
    }
    
    @Test
    public void addAccessedMachineWithValidArgument(){
        PhysicalMachine pm = new PMBuilder().build();
        physicalMachines.add(pm); //for after test cleanup
        
        assertNotNull("Physical machine " + pm.toString() + " should be correctly instantiated, not null",pm);
        assertFalse("Physical machine " + pm.toString() + " should not be accessed", sut.isAccessed(pm));
        
        sut.add(pm);
        assertTrue("Physical machine " + pm.toString() + " should be accessed",sut.isAccessed(pm));
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void addAccessedMachineWithNullArgument(){
        sut.add(null);
    }
    
    @Test
    public void removeAccessedMachineWithValidAccessedArgument(){
        PhysicalMachine pm1 = new PMBuilder().build();
        PhysicalMachine pm2 = new PMBuilder().addressIP("140.150.10.10").username("").userPassword("").build();
        physicalMachines.add(pm1);//for after test cleanup
        physicalMachines.add(pm2);//for after test cleanup
        
        assertFalse("Physical machine " + pm1.toString() + " should not be accessed", sut.isAccessed(pm1));
        assertFalse("Physical machine " + pm2.toString() + " should not be accessed", sut.isAccessed(pm2));
        
        sut.add(pm1);
        sut.add(pm2);
        
        assertTrue("Physical machine " + pm1.toString() + " should be accessed",sut.isAccessed(pm1));
        assertTrue("Physical machine " + pm2.toString() + " should be accessed",sut.isAccessed(pm2));
        
        assertTrue("Physical machine " + pm1.toString() + "should be accessed "
                 + "and therefore removable, but is not",sut.remove(pm1));
        
        assertFalse("Physical machine " + pm1.toString() + " should not be accessed", sut.isAccessed(pm1));
        assertTrue("Physical machine " + pm2.toString() + " should be accessed",sut.isAccessed(pm2));
    }
    
    @Test
    public void removeAccessedMachineWithValidInaccessedArgument(){
        PhysicalMachine pm1 = new PMBuilder().build();
        PhysicalMachine pm2 = new PMBuilder().addressIP("140.150.10.10").username("").userPassword("").build();
        physicalMachines.add(pm2);//for after test cleanup        
        
        sut.add(pm2);
        
        assertFalse("Physical machine " + pm1.toString() + " should not be accessed"
                  + " nor removable, but it is",sut.remove(pm1));
        
        assertFalse("Physical machine " + pm1.toString() + " should not be accessed", sut.isAccessed(pm1));
        assertTrue("Physical machine " + pm2.toString() + " should be accessed",sut.isAccessed(pm2));
    }
    
    @Test
    public void removeAccessedMachineWithNullArgument(){
        PhysicalMachine pm = new PhysicalMachine("140.150.12.0","10000","Hornd","140nb48");
        physicalMachines.add(pm);//for after test cleanup
        
        sut.add(pm);
        
        exception.expect(IllegalArgumentException.class);
        sut.remove(null);
        
        exception = ExpectedException.none();
        assertTrue("Physical machine " + pm.toString() + " should be accessed",sut.isAccessed(pm));
    }
    
    @Test
    public void isMachineAccessedWithValidInaccessedArgument(){
        PhysicalMachine pm = new PMBuilder().build();
        
        assertNotNull("Physical machine " + pm.toString() + " should be correctly instantiated, not null",pm);
        assertFalse("Physical machine " + pm.toString() + " should not be accessed", sut.isAccessed(pm));        
    }
    
    @Test
    public void isMachineAccessedWithValidAccessedArgument(){
        PhysicalMachine pm = new PMBuilder().build();
        physicalMachines.add(pm);//for after test cleanup        
        
        sut.add(pm);
        assertTrue("Physical machine " + pm.toString() + " should be accessed",sut.isAccessed(pm));
    }
    
    @Test
    public void isMachineAccessedWithNullArgument(){
        PhysicalMachine pm = new PMBuilder().build();
        physicalMachines.add(pm);//for after test cleanup
        
        sut.add(pm);
        
        exception.expect(IllegalArgumentException.class);
        sut.isAccessed(null);
        
        exception = ExpectedException.none();
        assertTrue("Physical machine " + pm.toString() + " should be accessed",sut.isAccessed(pm));
    }
    
    class PMBuilder{
        private String addressIP = "180.148.14.10";
        private String portOfVBoxWebServer = "18083";
        private String username = "Jack";
        private String userPassword = "tr1h15jk7";

        public PMBuilder(){

        }

        public PMBuilder addressIP(String value){
            this.addressIP = value;
            return this;
        }

        public PMBuilder webserverPort(String value){
            this.portOfVBoxWebServer = value;
            return this;
        }

        public PMBuilder username(String value){
            this.username = value;
            return this;
        }

        public PMBuilder userPassword(String value){
            this.userPassword = value;
            return this;
        }

        public PhysicalMachine build(){
            return new PhysicalMachine(this.addressIP,this.portOfVBoxWebServer,
                                       this.username,this.userPassword);
        }
    }
}



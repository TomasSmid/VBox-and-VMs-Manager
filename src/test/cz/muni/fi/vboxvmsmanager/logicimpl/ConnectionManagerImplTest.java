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
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.IncompatibleVirtToolAPIVersionException;
import cz.muni.fi.vboxvmsmanager.pubapi.managers.VirtualizationToolManager;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/**
 *
 * @author Tomáš Šmíd
 */
public class ConnectionManagerImplTest {
    
    private ConnectionManagerImpl sut;
    private NativeVBoxAPIConnection conMocked;
    
    
    @Before
    public void setUp() {
        sut = new ConnectionManagerImpl();
        conMocked = mock(NativeVBoxAPIConnection.class);
    }

    @Test
    public void connectToWithAllValidValues(){
        PhysicalMachine pm = new PMBuilder().build();
        
        VirtualizationToolManager vtm = sut.connectTo(pm);
        
        assertNotNull("There should be returned particular virtualization tool"
                    + "manager, not null", vtm);
    }
    
    @Test
    public void connectToValidAccessiblePhysicalMachineWithInvalidVBoxVersion() throws ConnectionFailureException, IncompatibleVirtToolAPIVersionException, InterruptedException{
        PhysicalMachine pm = new PMBuilder().build();
        doThrow(IncompatibleVirtToolAPIVersionException.class).when(conMocked).connectTo(pm);
        
        VirtualizationToolManager vtm = sut.connectTo(pm);
        
        assertNull("There should be returned null virtualization tool manager object", vtm);
    }
    
    @Test
    public void connectToAlreadyConnectedPhysicalMachineWithAvailableConnectionAndValidVBoxVersion(){
        PhysicalMachine pm = new PMBuilder().build();
        
        VirtualizationToolManager vtm = sut.connectTo(pm);
        VirtualizationToolManager vtm2 = sut.connectTo(pm);
        
        assertNotNull("There should be returned particular virtualization tool"
                    + "manager, not null", vtm);
        assertNotNull("There should be returned particular virtualization tool"
                    + "manager, not null", vtm2);
    }
    
    @Test
    public void connectToAlreadyConnectedPhysicalMachineWithAvailableConnectionAndInvalidVBoxVersion() throws ConnectionFailureException, IncompatibleVirtToolAPIVersionException, InterruptedException{
        PhysicalMachine pm = new PMBuilder().build();
        doNothing().doThrow(IncompatibleVirtToolAPIVersionException.class).when(conMocked).connectTo(pm);
        
        VirtualizationToolManager vtm = sut.connectTo(pm);
        VirtualizationToolManager vtm2 = sut.connectTo(pm);
        
        assertNotNull("There should be returned particular virtualization tool"
                    + "manager, not null", vtm);
        assertNull("There should be returned null virtualization tool manager object", vtm2);
    }
    
    @Test
    public void connectToAlreadyConnectedPhysicalMachineWithUnavailableConnection() throws ConnectionFailureException, IncompatibleVirtToolAPIVersionException, InterruptedException{
        PhysicalMachine pm = new PMBuilder().build();
        doNothing().doThrow(ConnectionFailureException.class).when(conMocked).connectTo(pm);
        
        VirtualizationToolManager vtm = sut.connectTo(pm);
        VirtualizationToolManager vtm2 = sut.connectTo(pm);
        
        assertNotNull("There should be returned particular virtualization tool"
                    + "manager, not null", vtm);
        assertNull("There should be returned null virtualization tool manager object", vtm2);
    }
    
    @Test
    public void connectToValidInaccessiblePhysicalMachine() throws ConnectionFailureException, IncompatibleVirtToolAPIVersionException, InterruptedException{
        PhysicalMachine pm = new PMBuilder().addressIP("180.148.14").build();
        doThrow(ConnectionFailureException.class).when(conMocked).connectTo(pm);
        
        VirtualizationToolManager vtm = sut.connectTo(pm);
        
        assertNull("There should be returned null virtualization tool manager object", vtm);
    }
    
    @Test
    public void connectToNullPhysicalMachine() throws ConnectionFailureException, IncompatibleVirtToolAPIVersionException, InterruptedException{
        doThrow(IllegalArgumentException.class).when(conMocked).connectTo(null);
        
        VirtualizationToolManager vtm = sut.connectTo(null);
        
        assertNull("There should be returned null virtualization tool manager object", vtm);
    }
    
    @Test
    public void disconnectFromConnectedPhysicalMachineWithValidConnection(){
        PhysicalMachine pm = new PMBuilder().build();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String errMsg = "";
        
        sut.disconnectFrom(pm);
        
        assertEquals(errMsg, errContent);
        
        System.setErr(null);
    }
    
    @Test
    public void disconnectFromConnectedPhysicalMachineWithInvalidConnection(){
        PhysicalMachine pm = new PMBuilder().build();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));        
        String errMsg = "Disconnection failure: Incorrect disconnection from \"http://" + pm.getAddressIP()
                + ":" + pm.getPortOfVTWebServer() + "\". Most probably there could be one of "
                + "two possible problems - network connection is not working or remote VirtualBox"
                + "web server is not running. Possible solution: check both network connection and"
                + " remote VirtualBox web server are running and working correctly,"
                + "then try to connect to \"http://" + pm.getAddressIP()
                + ":" + pm.getPortOfVTWebServer() + "\" again and then disconnect from that"
                + "physical machine in order to ensure correct end of work with that one.";
        
        sut.disconnectFrom(pm);
        
        assertEquals(errMsg, errContent);
        
        System.setErr(null);
    }
    
    @Test
    public void disconnectFromNotConnectedPhysicalMachine(){
        PhysicalMachine pm = new PMBuilder().build();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String errMsg = "Disconnection failure: There was made an attempt to disconnect from "
                + "\"http://" + pm.getAddressIP() + ":" + pm.getPortOfVTWebServer() + "\""
                + ", but this address is not associated with any connected machine.";
        
        sut.disconnectFrom(pm);
        
        assertEquals(errMsg, errContent);
        
        System.setErr(null);
    }
    
    @Test
    public void disconnectFromNullPhysicalMachine(){
        PhysicalMachine pm = new PMBuilder().build();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String errMsg = "Disconnection failure: There was not specified any physical machine which should be disconnect from.";
        
        sut.disconnectFrom(null);
        
        assertEquals(errMsg, errContent);
        
        System.setErr(null);
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

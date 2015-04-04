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
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.DisconnectionFailureException;
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.IncompatibleVirtToolAPIVersionException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import static org.mockito.Mockito.*;
import org.virtualbox_4_3.IVirtualBox;
import org.virtualbox_4_3.VBoxException;
import org.virtualbox_4_3.VirtualBoxManager;

/**
 *
 * @author Tomáš Šmíd
 */
public class NativeVBoxAPIConnectionTest {
    
    @Rule
    public ExpectedException exception = ExpectedException.none();
    
    private NativeVBoxAPIConnection sut;
    private AccessedPhysicalMachines apmMocked;
    
    @Before
    public void setUp() {        
        sut = NativeVBoxAPIConnection.getInstance();
        apmMocked = mock(AccessedPhysicalMachines.class);
    }
    
    @Test
    public void connectToValidAccessiblePhysicalMachineWithNoThrownException() throws ConnectionFailureException, IncompatibleVirtToolAPIVersionException, InterruptedException{
        PhysicalMachine pm = new PMBuilder().build();
        String url = "http://" + pm.getAddressIP() + ":" + pm.getPortOfVTWebServer();
        VirtualBoxManager vbmMocked = mock(VirtualBoxManager.class);
        IVirtualBox vboxMocked = mock(IVirtualBox.class);
        doReturn("4_3").when(vboxMocked).getAPIVersion();
                
        sut.connectTo(pm);
        
        verify(vbmMocked).connect(url, pm.getUsername(), pm.getUserPassword());
        verify(vboxMocked).getAPIVersion();
        verify(apmMocked).add(pm);
    }
    
    /*@Test
    public void connectToValidAccessiblePhysicalMachineWithValidVBoxVersion() throws ConnectionFailureException, IncompatibleVirtToolAPIVersionException{
        PhysicalMachine pm = new PMBuilder().build();
        VirtualBoxManager vbmMocked = mock(VirtualBoxManager.class);
        IVirtualBox vboxMocked = mock(IVirtualBox.class);
        when(vboxMocked.getAPIVersion()).thenReturn("4_3");
        //doReturn(true).when(apmMocked).isAccessed(pm);
        
        sut.connectTo(pm);
        assertTrue("Physical machine should be connected",sut.isConnected(pm));
    }
    
    @Test
    public void connectToValidAccessiblePhysicalMachineWithInvalidVBoxVersion() throws ConnectionFailureException, IncompatibleVirtToolAPIVersionException{
        PhysicalMachine pm = new PMBuilder().build();
        VirtualBoxManager vbmMocked = mock(VirtualBoxManager.class);
        IVirtualBox vboxMocked = mock(IVirtualBox.class);
        when(vboxMocked.getAPIVersion()).thenReturn("4_2");
        //doReturn(false).when(apmMocked).isAccessed(pm);
        
        exception.expect(IncompatibleVirtToolAPIVersionException.class);
        sut.connectTo(pm);
        
        exception = ExpectedException.none();
        assertFalse("Physical machine should not be connected",sut.isConnected(pm));
        verify(apmMocked, never()).add(pm);
    }
    
    @Test
    public void connectToAlreadyConnectedPhysicalMachineWithAvailableConnectionAndValidVBoxVersion() throws ConnectionFailureException, IncompatibleVirtToolAPIVersionException{
        PhysicalMachine pm = new PMBuilder().build();
        VirtualBoxManager vbmMocked = mock(VirtualBoxManager.class);
        IVirtualBox vboxMocked = mock(IVirtualBox.class);
        when(vboxMocked.getAPIVersion()).thenReturn("4_3");
        
        sut.connectTo(pm);
        assertTrue("Physical machine should be connected",sut.isConnected(pm));
        verify(apmMocked, never()).add(pm);
    }
    
    @Test
    public void connectToAlreadyConnectedPhysicalMachineWithAvailableConnectionAndInvalidVBoxVersion() throws ConnectionFailureException, IncompatibleVirtToolAPIVersionException{
        PhysicalMachine pm = new PMBuilder().build();
        VirtualBoxManager vbmMocked = mock(VirtualBoxManager.class);
        IVirtualBox vboxMocked = mock(IVirtualBox.class);
        when(vboxMocked.getAPIVersion()).thenReturn("4_2");
        
        exception.expect(IncompatibleVirtToolAPIVersionException.class);
        sut.connectTo(pm);
        
        exception = ExpectedException.none();
        assertFalse("Physical machine should not be connected",sut.isConnected(pm));
        verify(apmMocked, never()).add(pm);
    }
    
    @Test
    public void connectToAlreadyConnectedPhysicalMachineWithUnavailableConnection() throws ConnectionFailureException, IncompatibleVirtToolAPIVersionException{
        PhysicalMachine pm = new PMBuilder().build();
        String url = "http://" + pm.getAddressIP() + ":" + pm.getPortOfVTWebServer();
        VirtualBoxManager vbmMocked = mock(VirtualBoxManager.class);
        IVirtualBox vboxMocked = mock(IVirtualBox.class);
        doThrow(VBoxException.class).when(vbmMocked).connect(url, pm.getUsername(), pm.getUserPassword());
        
        exception.expect(ConnectionFailureException.class);
        sut.connectTo(pm);
        
        exception = ExpectedException.none();
        assertTrue("Physical machine should be connected",sut.isConnected(pm));
        verify(vboxMocked, never()).getAPIVersion();
        verify(apmMocked, never()).add(pm);
    }
    
    @Test
    public void connectToValidInaccessiblePhysicalMachine() throws ConnectionFailureException, IncompatibleVirtToolAPIVersionException{
        PhysicalMachine pm = new PMBuilder().addressIP("14.255.15").build();
        String url = "http://" + pm.getAddressIP() + ":" + pm.getPortOfVTWebServer();
        VirtualBoxManager vbmMocked = mock(VirtualBoxManager.class);
        IVirtualBox vboxMocked = mock(IVirtualBox.class);
        doThrow(VBoxException.class).when(vbmMocked).connect(url, pm.getUsername(), pm.getUserPassword());
        
        exception.expect(ConnectionFailureException.class);
        sut.connectTo(pm);
        
        exception = ExpectedException.none();
        assertFalse("Physical machine should not be connected",sut.isConnected(pm));
        verify(vbmMocked, times(3)).connect(url, pm.getUsername(), pm.getUserPassword());
        verify(vboxMocked, never()).getAPIVersion();
        verify(apmMocked, never()).add(pm);
    }
    
    @Test
    public void connectToNullPhysicalMachine() throws ConnectionFailureException, IncompatibleVirtToolAPIVersionException{
        PhysicalMachine pm = null;
        VirtualBoxManager vbmMocked = mock(VirtualBoxManager.class);
        IVirtualBox vboxMocked = mock(IVirtualBox.class);
        
        exception.expect(IllegalArgumentException.class);
        sut.connectTo(pm);
        
        exception = ExpectedException.none();
        assertFalse("Physical machine should not be connected",sut.isConnected(pm));
        verify(vbmMocked, never()).connect(anyString(), anyString(), anyString());
        verify(vboxMocked, never()).getAPIVersion();
        verify(apmMocked, never()).add(pm);
    }
    
    @Test
    public void disconnectFromConnectedPhysicalMachineWithValidConnection(){
        PhysicalMachine pm = new PMBuilder().build();
        List<VirtualMachine> vms = createVMsList(pm);
        ConnectionManagerImpl conMocked = mock(ConnectionManagerImpl.class);
        NativeVBoxAPIManager natapimanMocked = mock(NativeVBoxAPIManager.class);
        NativeVBoxAPIMachine natmachMocked = mock(NativeVBoxAPIMachine.class);
        doReturn(new VirtualizationToolManagerImpl(pm)).when(conMocked).connectTo(pm);
        doReturn(vms).when(natapimanMocked).getVirtualMachines(pm);
        
        assertTrue("Physical machine should be connected",sut.isConnected(pm));
        
        sut.disconnectFrom(pm);
        
        assertFalse("Physical machine should not be connected",sut.isConnected(pm));
        
        verify(conMocked).connectTo(pm);
        verify(natapimanMocked).getVirtualMachines(pm);
        verify(natmachMocked, times(2)).shutDownVM(any(VirtualMachine.class));
        verify(natmachMocked).shutDownVM(vms.get(0));
        verify(natmachMocked).shutDownVM(vms.get(1));
    }
    
    @Test
    public void disconnectFromConnectedPhysicalMachineWithInvalidConnection(){
        PhysicalMachine pm = new PMBuilder().build();
        ConnectionManagerImpl conMocked = mock(ConnectionManagerImpl.class);
        NativeVBoxAPIManager natapimanMocked = mock(NativeVBoxAPIManager.class);
        NativeVBoxAPIMachine natmachMocked = mock(NativeVBoxAPIMachine.class);
        doReturn(null).when(conMocked).connectTo(pm);
        
        assertTrue("Physical machine should be connected",sut.isConnected(pm));
        
        exception.expect(DisconnectionFailureException.class);
        sut.disconnectFrom(pm);
        
        exception = ExpectedException.none();
        assertFalse("Physical machine should not be connected",sut.isConnected(pm));
        
        verify(apmMocked).remove(pm);
        verify(natapimanMocked, never()).getVirtualMachines(pm);
        verify(natmachMocked, never()).shutDownVM(any(VirtualMachine.class));
    }
    
    @Test
    public void disconnectFromNotConnectedPhysicalMachine(){
        PhysicalMachine pm = new PMBuilder().build();
        ConnectionManagerImpl conMocked = mock(ConnectionManagerImpl.class);
        NativeVBoxAPIManager natapimanMocked = mock(NativeVBoxAPIManager.class);
        NativeVBoxAPIMachine natmachMocked = mock(NativeVBoxAPIMachine.class);
        doReturn(null).when(conMocked).connectTo(pm);
        
        assertFalse("Physical machine should not be connected",sut.isConnected(pm));
        
        exception.expect(DisconnectionFailureException.class);
        sut.disconnectFrom(pm);
        
        exception = ExpectedException.none();
        assertFalse("Physical machine should not be connected",sut.isConnected(pm));
        
        verify(apmMocked, never()).remove(pm);
        verify(natapimanMocked, never()).getVirtualMachines(pm);
        verify(natmachMocked, never()).shutDownVM(any(VirtualMachine.class));
    }
    
    @Test
    public void disconnectFromNullPhysicalMachine(){        
        PhysicalMachine pm = null;
        ConnectionManagerImpl conMocked = mock(ConnectionManagerImpl.class);
        NativeVBoxAPIManager natapimanMocked = mock(NativeVBoxAPIManager.class);
        NativeVBoxAPIMachine natmachMocked = mock(NativeVBoxAPIMachine.class);
        
        exception.expect(IllegalArgumentException.class);
        sut.disconnectFrom(pm);
        
        exception = ExpectedException.none();
        verify(conMocked, never()).connectTo(pm);
        verify(natapimanMocked, never()).getVirtualMachines(pm);
        verify(natmachMocked, never()).shutDownVM(any(VirtualMachine.class));
    }
    
    @Test
    public void isMachineConnectedWithValidConnectedMachine(){
        PhysicalMachine pm = new PMBuilder().build();
        doReturn(true).when(apmMocked).isAccessed(pm);
        
        assertTrue("Physical machine should be connected", sut.isConnected(pm));
        
        verify(apmMocked).isAccessed(pm);        
    }
    
    @Test
    public void isMachineConnectedWithValidNotConnectedMachine(){
        PhysicalMachine pm = new PMBuilder().build();
        doReturn(false).when(apmMocked).isAccessed(pm);
        
        assertFalse("Physical machine should not be connected", sut.isConnected(pm));
        
        verify(apmMocked).isAccessed(pm);
    }
    
    @Test
    public void isMachineConnectedWithNullMachine(){
        exception.expect(IllegalArgumentException.class);
        sut.isConnected(null);
        
        exception = ExpectedException.none();
        verify(apmMocked, never()).isAccessed(any(PhysicalMachine.class));
    }*/
    
    private List<VirtualMachine> createVMsList(PhysicalMachine pm){
        List<VirtualMachine> vms = new ArrayList<>();
        UUID uuid1 = UUID.fromString("670e746d-abea-4ba6-ad02-2a3b043810a5");
        UUID uuid2 = UUID.fromString("00ee15d8-45b8-1299-aae7-48ae9b54daed");
        VirtualMachine vm1 = new VirtualMachine.Builder(uuid1, "Fedora20", pm)
                                               .countOfCPU(1L).countOfMonitors(1L)
                                               .cpuExecutionCap(100L).hardDiskFreeSpaceSize(150_015_488_979L)
                                               .hardDiskTotalSize(200_000_000_216L).sizeOfRAM(409_612_548L)
                                               .sizeOfVRAM(12_458L).typeOfOS("Linux").versionOfOS("Fedora(64bit)")
                                               .build();
        VirtualMachine vm2 = new VirtualMachine.Builder(uuid2, "Windows10", pm)
                                               .countOfCPU(1L).countOfMonitors(1L)
                                               .cpuExecutionCap(100L).hardDiskFreeSpaceSize(100_015_488_979L)
                                               .hardDiskTotalSize(150_000_000_216L).sizeOfRAM(210_612_548L)
                                               .sizeOfVRAM(12_458L).typeOfOS("Windows").versionOfOS("Win10(64bit)")
                                               .build();
        vms.add(vm1);
        vms.add(vm2);
        
        return vms;
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


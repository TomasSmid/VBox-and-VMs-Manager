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
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.UnknownVirtualMachineException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import static org.mockito.Mockito.*;
import org.virtualbox_4_3.CleanupMode;
import org.virtualbox_4_3.CloneMode;
import org.virtualbox_4_3.IMachine;
import org.virtualbox_4_3.IMedium;
import org.virtualbox_4_3.IVirtualBox;
import org.virtualbox_4_3.VBoxException;
import org.virtualbox_4_3.VirtualBoxManager;

/**
 *
 * @author Tomáš Šmíd
 */
public class NativeVBoxAPIManagerTest {
    
    @Rule
    public ExpectedException exception = ExpectedException.none();
    
    private NativeVBoxAPIManager sut;
    private VirtualBoxManager vbmMocked;
    private IVirtualBox vboxMocked;
    private IMachine machMocked;
    private IMedium medMocked;
    
    @Before
    public void setUp() {
        sut = new NativeVBoxAPIManager();
        vbmMocked = mock(VirtualBoxManager.class);
        vboxMocked = mock(IVirtualBox.class);
        machMocked = mock(IMachine.class);
        medMocked = mock(IMedium.class);
    }
    
    @Test
    public void getVMByIdWithValidIdAndSomeMatch(){
        PhysicalMachine pm = new PhysicalMachine("150.150.14.87","18083","John","trio158hy7");
        doReturn("793d084a-0189-4a55-a9b7-531c455570a1").when(machMocked).getId();
        doReturn("Fedora-21-WS").when(machMocked).getName();
        doReturn(1L).when(machMocked).getCPUCount();
        doReturn(1L).when(machMocked).getMonitorCount();
        doReturn(100L).when(machMocked).getCPUExecutionCap();
        doReturn(7187988480L).when(medMocked).getSize();
        doReturn(21474836480L).when(medMocked).getLogicalSize();
        doReturn(4096L).when(machMocked).getMemorySize();
        doReturn(12L).when(machMocked).getVRAMSize();
        doReturn("Fedora_64").when(machMocked).getOSTypeId();
        
        VirtualMachine expVM = new VirtualMachine.Builder(UUID.fromString("793d084a-0189-4a55-a9b7-531c455570a1"), "Fedora-21-WS", pm)
                                                 .countOfCPU(1L).countOfMonitors(1L).cpuExecutionCap(100L)
                                                 .hardDiskFreeSpaceSize(14286848000L).hardDiskTotalSize(21474836480L)
                                                 .sizeOfRAM(4096L).sizeOfVRAM(12L).typeOfOS("Linux").versionOfOS("Fedora_64").build();
        VirtualMachine actVM = sut.getVirtualMachineById(pm, UUID.fromString("793d084a-0189-4a55-a9b7-531c455570a1"));
        
        assertNotNull("Returned virtual machine should not be null",actVM);
        assertDeepVMsEquals(expVM,actVM);
    }
    
    @Test
    public void getVMByIdWithValidIdAndNoMatch(){
        PhysicalMachine pm = new PhysicalMachine("150.150.14.87","18083","John","trio158hy7");
        doThrow(VBoxException.class).when(vboxMocked).findMachine("793d084a-0189-4a55-a9b7-531c455570a1");
        
        exception.expect(UnknownVirtualMachineException.class);
        VirtualMachine actVM = sut.getVirtualMachineById(pm, UUID.fromString("793d010c-0189-4a55-a9b7-531c455570a1"));
    }
    
    @Test
    public void getVMByIdWithNullId(){
        PhysicalMachine pm = new PhysicalMachine("150.150.14.87","18083","John","trio158hy7");
        
        exception.expect(IllegalArgumentException.class);
        sut.getVirtualMachineById(pm, null);
        
        exception = ExpectedException.none();
        verify(vbmMocked, never()).connect(anyString(), anyString(), anyString());
    }
    
    @Test
    public void getVMByIdInvalidConnection(){
        PhysicalMachine pm = new PhysicalMachine("150.150.14.87","18083","John","trio158hy7");
        doThrow(VBoxException.class).when(vbmMocked).connect("http://150.150.14.87:18083", "John", "trio158hy7");
        
        exception.expect(ConnectionFailureException.class);
        sut.getVirtualMachineById(pm, UUID.fromString("793d084a-0189-4a55-a9b7-531c455570a1"));
        
        exception = ExpectedException.none();
        verify(vbmMocked, times(3)).connect("http://150.150.14.87:18083", "John", "trio158hy7");
        verify(vbmMocked, never()).getVBox();
    }
    
    @Test
    public void getVMByNameWithValidNameAndSomeMatch(){
        PhysicalMachine pm = new PhysicalMachine("150.150.14.87","18083","John","trio158hy7");
        doReturn("793d084a-0189-4a55-a9b7-531c455570a1").when(machMocked).getId();
        doReturn("Fedora-21-WS").when(machMocked).getName();
        doReturn(1L).when(machMocked).getCPUCount();
        doReturn(1L).when(machMocked).getMonitorCount();
        doReturn(100L).when(machMocked).getCPUExecutionCap();
        doReturn(7187988480L).when(medMocked).getSize();
        doReturn(21474836480L).when(medMocked).getLogicalSize();
        doReturn(4096L).when(machMocked).getMemorySize();
        doReturn(12L).when(machMocked).getVRAMSize();
        doReturn("Fedora_64").when(machMocked).getOSTypeId();
        
        VirtualMachine expVM = new VirtualMachine.Builder(UUID.fromString("793d084a-0189-4a55-a9b7-531c455570a1"), "Fedora-21-WS", pm)
                                                 .countOfCPU(1L).countOfMonitors(1L).cpuExecutionCap(100L)
                                                 .hardDiskFreeSpaceSize(14286848000L).hardDiskTotalSize(21474836480L)
                                                 .sizeOfRAM(4096L).sizeOfVRAM(12L).typeOfOS("Linux").versionOfOS("Fedora_64").build();
        VirtualMachine actVM = sut.getVirtualMachineByName(pm, "Fedora-21-WS");
        
        assertNotNull("Returned virtual machine should not be null",actVM);
        assertDeepVMsEquals(expVM,actVM);
    }
    
    @Test
    public void getVMByNameWithValidNameAndNoMatch(){
        PhysicalMachine pm = new PhysicalMachine("150.150.14.87","18083","John","trio158hy7");
        doThrow(VBoxException.class).when(vboxMocked).findMachine("Fedora-21-WS");
        
        exception.expect(UnknownVirtualMachineException.class);
        VirtualMachine actVM = sut.getVirtualMachineByName(pm, "Fedora-21-WS");
    }
    
    @Test
    public void getVMByNameWithEmptyNameString(){
        PhysicalMachine pm = new PhysicalMachine("150.150.14.87","18083","John","trio158hy7");
        
        exception.expect(IllegalArgumentException.class);
        sut.getVirtualMachineByName(pm, "");
        
        exception = ExpectedException.none();
        verify(vbmMocked, never()).connect(anyString(), anyString(), anyString());
    }
    
    @Test
    public void getVMByNameWithNullName(){
        PhysicalMachine pm = new PhysicalMachine("150.150.14.87","18083","John","trio158hy7");
        
        exception.expect(IllegalArgumentException.class);
        sut.getVirtualMachineByName(pm, null);
        
        exception = ExpectedException.none();
        verify(vbmMocked, never()).connect(anyString(), anyString(), anyString());
    }
    
    @Test
    public void getVMByNameWithInvalidConnection(){
        PhysicalMachine pm = new PhysicalMachine("150.150.14.87","18083","John","trio158hy7");
        doThrow(VBoxException.class).when(vbmMocked).connect("http://150.150.14.87:18083", "John", "trio158hy7");
        
        exception.expect(ConnectionFailureException.class);
        sut.getVirtualMachineByName(pm, "Fedora-21-WS");
        
        exception = ExpectedException.none();
        verify(vbmMocked, times(3)).connect("http://150.150.14.87:18083", "John", "trio158hy7");
        verify(vbmMocked, never()).getVBox();
    }
    
    @Test
    public void getVMsWithValidConnectionAndSomeReturnedMachines(){
        PhysicalMachine pm = new PhysicalMachine("150.150.14.87","18083","John","trio158hy7");
        doReturn("793d084a-0189-4a55-a9b7-531c455570a1")
                .doReturn("793d047c-1148-4a55-a9b7-00aa455570a1").when(machMocked).getId();
        doReturn("Fedora-21-WS")
                .doReturn("Ubuntu 14.04 LTS").when(machMocked).getName();
        doReturn(1L).when(machMocked).getCPUCount();//same for both
        doReturn(1L).when(machMocked).getMonitorCount();//same for both
        doReturn(100L).when(machMocked).getCPUExecutionCap();//same for both
        doReturn(7187988480L)
                .doReturn(3548741580L).when(medMocked).getSize();
        doReturn(21474836480L)
                .doReturn(15478784512L).when(medMocked).getLogicalSize();
        doReturn(4096L)
                .doReturn(1024L).when(machMocked).getMemorySize();
        doReturn(12L).when(machMocked).getVRAMSize();//same for both
        doReturn("Fedora_64")
                .doReturn("Ubuntu_64").when(machMocked).getOSTypeId();
        
        VirtualMachine expVM1 = new VirtualMachine.Builder(UUID.fromString("793d084a-0189-4a55-a9b7-531c455570a1"), "Fedora-21-WS", pm)
                                                  .countOfCPU(1L).countOfMonitors(1L).cpuExecutionCap(100L)
                                                  .hardDiskFreeSpaceSize(14286848000L).hardDiskTotalSize(21474836480L)
                                                  .sizeOfRAM(4096L).sizeOfVRAM(12L).typeOfOS("Linux").versionOfOS("Fedora_64").build();
        VirtualMachine expVM2 = new VirtualMachine.Builder(UUID.fromString("793d047c-1148-4a55-a9b7-00aa455570a1"), "Ubuntu 14.04 LTS", pm)
                                                  .countOfCPU(1L).countOfMonitors(1L).cpuExecutionCap(100L)
                                                  .hardDiskFreeSpaceSize(11930042932L).hardDiskTotalSize(15478784512L)
                                                  .sizeOfRAM(1024L).sizeOfVRAM(12L).typeOfOS("Linux").versionOfOS("Ubuntu_64").build();
        
        List<VirtualMachine> expVMs = Arrays.asList(expVM1,expVM2);
        List<VirtualMachine> actVMs = sut.getVirtualMachines(pm);
        
        Collections.sort(expVMs, vmComparator);
        Collections.sort(actVMs, vmComparator);
        
        assertEquals("There should be two virtual machines in list",2,actVMs.size());
        assertNotNull("Returned virtual machine should not be null",actVMs.get(0));
        assertNotNull("Returned virtual machine should not be null",actVMs.get(1));
        assertDeepVMsEquals(expVMs,actVMs);
    }
    
    @Test
    public void getVMsWithValidConnectionAndNoReturnedMachines(){
        PhysicalMachine pm = new PhysicalMachine("150.150.14.87","18083","John","trio158hy7");
        List<IMachine> list = new ArrayList<>();
        doReturn(list).when(vboxMocked).getMachines();
        
        List<VirtualMachine> actVMs = sut.getVirtualMachines(pm);
        assertTrue("List should be empty",actVMs.isEmpty());
    }
    
    @Test
    public void getVMsWithInvalidConnection(){
        PhysicalMachine pm = new PhysicalMachine("150.150.14.87","18083","John","trio158hy7");
        doThrow(VBoxException.class).when(vbmMocked).connect("http://150.150.14.87:18083", "John", "trio158hy7");
        
        exception.expect(ConnectionFailureException.class);
        sut.getVirtualMachines(pm);
        
        exception = ExpectedException.none();
        verify(vbmMocked, times(3)).connect("http://150.150.14.87:18083", "John", "trio158hy7");
        verify(vbmMocked, never()).getVBox();
    }
    
    @Test
    public void removeVMWithValidExistingVM(){
        PhysicalMachine pm = new PhysicalMachine("150.150.14.87","18083","John","trio158hy7");        
        VirtualMachine vmToRem = new VirtualMachine.Builder(UUID.fromString("793d047c-1148-4a55-a9b7-00aa455570a1"), "Fedora-21-WS", pm)
                                                     .countOfCPU(1L).countOfMonitors(1L).cpuExecutionCap(100L)
                                                     .hardDiskFreeSpaceSize(14286848000L).hardDiskTotalSize(21474836480L)
                                                     .sizeOfRAM(4096L).sizeOfVRAM(12L).typeOfOS("Linux").versionOfOS("Fedora_64").build();
        
        sut.removeVM(vmToRem);
        
        verify(vbmMocked).connect("http://150.150.14.87:18083", "John", "trio158hy7");
        verify(machMocked).unregister(CleanupMode.DetachAllReturnHardDisksOnly);
        verify(machMocked).deleteConfig(any(List.class));
    }
    
    @Test
    public void removeVMWithValidNotExistingVM(){
        PhysicalMachine pm = new PhysicalMachine("150.150.14.87","18083","John","trio158hy7");
        doThrow(VBoxException.class).when(vboxMocked).findMachine("Fedora-21-WS");
        
        VirtualMachine vmToRem = new VirtualMachine.Builder(UUID.fromString("793d047c-1148-4a55-a9b7-00aa455570a1"), "Fedora-21-WS", pm)
                                                     .countOfCPU(1L).countOfMonitors(1L).cpuExecutionCap(100L)
                                                     .hardDiskFreeSpaceSize(14286848000L).hardDiskTotalSize(21474836480L)
                                                     .sizeOfRAM(4096L).sizeOfVRAM(12L).typeOfOS("Linux").versionOfOS("Fedora_64").build();
        
        exception.expect(UnknownVirtualMachineException.class);
        sut.removeVM(vmToRem);
        
        exception = ExpectedException.none();
        verify(machMocked, never()).unregister(any(CleanupMode.class));
        verify(machMocked, never()).deleteConfig(any(List.class));
    }
    
    @Test
    public void removeVMWithNullVM(){        
        exception.expect(IllegalArgumentException.class);
        sut.removeVM(null);
        
        exception = ExpectedException.none();
        verify(vbmMocked, never()).connect(anyString(), anyString(), anyString());
    }
    
    @Test
    public void removeVMWithInvalidConnection(){
        PhysicalMachine pm = new PhysicalMachine("150.150.14.87","18083","John","trio158hy7");
        doThrow(VBoxException.class).when(vbmMocked).connect("http://150.150.14.87:18083", "John", "trio158hy7");
        
        VirtualMachine vmToRem = new VirtualMachine.Builder(UUID.fromString("793d047c-1148-4a55-a9b7-00aa455570a1"), "Fedora-21-WS", pm)
                                                     .countOfCPU(1L).countOfMonitors(1L).cpuExecutionCap(100L)
                                                     .hardDiskFreeSpaceSize(14286848000L).hardDiskTotalSize(21474836480L)
                                                     .sizeOfRAM(4096L).sizeOfVRAM(12L).typeOfOS("Linux").versionOfOS("Fedora_64").build();
        
        exception.expect(ConnectionFailureException.class);
        sut.removeVM(vmToRem);
        
        exception = ExpectedException.none();
        verify(vbmMocked, times(3)).connect("http://150.150.14.87:18083", "John", "trio158hy7");
        verify(vbmMocked, never()).getVBox();
    }
    
    @Test
    public void createFullCloneWithValidConnectionAndExistingVM(){
        PhysicalMachine pm = new PhysicalMachine("150.150.14.87","18083","John","trio158hy7");
        doReturn("793d084a-0189-4a55-a9b7-531c455570a1").when(machMocked).getId();
        doReturn("Fedora-21-WS_FCopy1").when(machMocked).getName();
        doReturn(1L).when(machMocked).getCPUCount();
        doReturn(1L).when(machMocked).getMonitorCount();
        doReturn(100L).when(machMocked).getCPUExecutionCap();
        doReturn(7187988480L).when(medMocked).getSize();
        doReturn(21474836480L).when(medMocked).getLogicalSize();
        doReturn(4096L).when(machMocked).getMemorySize();
        doReturn(12L).when(machMocked).getVRAMSize();
        doReturn("Fedora_64").when(machMocked).getOSTypeId();
        
        VirtualMachine vmToClone = new VirtualMachine.Builder(UUID.fromString("793d047c-1148-4a55-a9b7-00aa455570a1"), "Fedora-21-WS", pm)
                                                     .countOfCPU(1L).countOfMonitors(1L).cpuExecutionCap(100L)
                                                     .hardDiskFreeSpaceSize(14286848000L).hardDiskTotalSize(21474836480L)
                                                     .sizeOfRAM(4096L).sizeOfVRAM(12L).typeOfOS("Linux").versionOfOS("Fedora_64").build();
        VirtualMachine expVM = new VirtualMachine.Builder(UUID.fromString("793d084a-0189-4a55-a9b7-531c455570a1"), "Fedora-21-WS_FCopy1", pm)
                                                 .countOfCPU(1L).countOfMonitors(1L).cpuExecutionCap(100L)
                                                 .hardDiskFreeSpaceSize(14286848000L).hardDiskTotalSize(21474836480L)
                                                 .sizeOfRAM(4096L).sizeOfVRAM(12L).typeOfOS("Linux").versionOfOS("Fedora_64").build();
        VirtualMachine actVM = sut.createFullClone(vmToClone);
        
        assertNotNull("Returned virtual machine should not be null",actVM);
        assertDeepVMsEquals(expVM,actVM);
    }
    
    @Test
    public void createFullCloneWithValidConnectionAndNotExistingVM(){
        PhysicalMachine pm = new PhysicalMachine("150.150.14.87","18083","John","trio158hy7");
        doThrow(VBoxException.class).when(vboxMocked).findMachine("Fedora-21-WS");
        
        VirtualMachine vmToClone = new VirtualMachine.Builder(UUID.fromString("793d047c-1148-4a55-a9b7-00aa455570a1"), "Fedora-21-WS", pm)
                                                     .countOfCPU(1L).countOfMonitors(1L).cpuExecutionCap(100L)
                                                     .hardDiskFreeSpaceSize(14286848000L).hardDiskTotalSize(21474836480L)
                                                     .sizeOfRAM(4096L).sizeOfVRAM(12L).typeOfOS("Linux").versionOfOS("Fedora_64").build();
        
        exception.expect(UnknownVirtualMachineException.class);
        VirtualMachine actVM = sut.createFullClone(vmToClone);
        
        exception = ExpectedException.none();
        verify(machMocked, never()).cloneTo(any(IMachine.class), any(CloneMode.class), any(List.class));
        verify(machMocked, never()).saveSettings();
        verify(vboxMocked, never()).registerMachine(any(IMachine.class));
    }
    
    @Test
    public void createFullCloneWithValidConnectionAndNullVM(){
        exception.expect(IllegalArgumentException.class);
        VirtualMachine actVM = sut.createFullClone(null);
        
        exception = ExpectedException.none();
        verify(vbmMocked, never()).connect(anyString(), anyString(), anyString());
    }
    
    @Test
    public void createFullCloneWithInvalidConnection(){
        PhysicalMachine pm = new PhysicalMachine("150.150.14.87","18083","John","trio158hy7");
        doThrow(VBoxException.class).when(vbmMocked).connect("http://150.150.14.87:18083", "John", "trio158hy7");
        
        VirtualMachine vmToClone = new VirtualMachine.Builder(UUID.fromString("793d047c-1148-4a55-a9b7-00aa455570a1"), "Fedora-21-WS", pm)
                                                     .countOfCPU(1L).countOfMonitors(1L).cpuExecutionCap(100L)
                                                     .hardDiskFreeSpaceSize(14286848000L).hardDiskTotalSize(21474836480L)
                                                     .sizeOfRAM(4096L).sizeOfVRAM(12L).typeOfOS("Linux").versionOfOS("Fedora_64").build();
        
        exception.expect(ConnectionFailureException.class);
        VirtualMachine actVM = sut.createFullClone(vmToClone);
        
        exception = ExpectedException.none();
        verify(vbmMocked, times(3)).connect("http://150.150.14.87:18083", "John", "trio158hy7");
        verify(vbmMocked, never()).getVBox();
    }
    
    @Test
    public void createLinkedCloneWithValidConnectionAndExistingVM(){
        PhysicalMachine pm = new PhysicalMachine("150.150.14.87","18083","John","trio158hy7");
        doReturn("793d084a-0189-4a55-a9b7-531c455570a1").when(machMocked).getId();
        doReturn("Fedora-21-WS_LCopy1").when(machMocked).getName();
        doReturn(1L).when(machMocked).getCPUCount();
        doReturn(1L).when(machMocked).getMonitorCount();
        doReturn(100L).when(machMocked).getCPUExecutionCap();
        doReturn(7187988480L).when(medMocked).getSize();
        doReturn(21474836480L).when(medMocked).getLogicalSize();
        doReturn(4096L).when(machMocked).getMemorySize();
        doReturn(12L).when(machMocked).getVRAMSize();
        doReturn("Fedora_64").when(machMocked).getOSTypeId();
        
        VirtualMachine vmToClone = new VirtualMachine.Builder(UUID.fromString("793d047c-1148-4a55-a9b7-00aa455570a1"), "Fedora-21-WS", pm)
                                                     .countOfCPU(1L).countOfMonitors(1L).cpuExecutionCap(100L)
                                                     .hardDiskFreeSpaceSize(14286848000L).hardDiskTotalSize(21474836480L)
                                                     .sizeOfRAM(4096L).sizeOfVRAM(12L).typeOfOS("Linux").versionOfOS("Fedora_64").build();
        VirtualMachine expVM = new VirtualMachine.Builder(UUID.fromString("793d084a-0189-4a55-a9b7-531c455570a1"), "Fedora-21-WS_LCopy1", pm)
                                                 .countOfCPU(1L).countOfMonitors(1L).cpuExecutionCap(100L)
                                                 .hardDiskFreeSpaceSize(14286848000L).hardDiskTotalSize(21474836480L)
                                                 .sizeOfRAM(4096L).sizeOfVRAM(12L).typeOfOS("Linux").versionOfOS("Fedora_64").build();
        VirtualMachine actVM = sut.createLinkedClone(vmToClone);
        
        assertNotNull("Returned virtual machine should not be null",actVM);
        assertDeepVMsEquals(expVM,actVM);
    }
    
    @Test
    public void createLinkedCloneWithValidConnectionAndNotExistingVM(){
        PhysicalMachine pm = new PhysicalMachine("150.150.14.87","18083","John","trio158hy7");
        doThrow(VBoxException.class).when(vboxMocked).findMachine("Fedora-21-WS");
        
        VirtualMachine vmToClone = new VirtualMachine.Builder(UUID.fromString("793d047c-1148-4a55-a9b7-00aa455570a1"), "Fedora-21-WS", pm)
                                                     .countOfCPU(1L).countOfMonitors(1L).cpuExecutionCap(100L)
                                                     .hardDiskFreeSpaceSize(14286848000L).hardDiskTotalSize(21474836480L)
                                                     .sizeOfRAM(4096L).sizeOfVRAM(12L).typeOfOS("Linux").versionOfOS("Fedora_64").build();
        
        exception.expect(UnknownVirtualMachineException.class);
        VirtualMachine actVM = sut.createFullClone(vmToClone);
        
        exception = ExpectedException.none();
        verify(machMocked, never()).cloneTo(any(IMachine.class), any(CloneMode.class), any(List.class));
        verify(machMocked, never()).saveSettings();
        verify(vboxMocked, never()).registerMachine(any(IMachine.class));
    }
    
    @Test
    public void createLinkedCloneWithValidConnectionAndNullVM(){
        PhysicalMachine pm = new PhysicalMachine("150.150.14.87","18083","John","trio158hy7");        
        
        exception.expect(IllegalArgumentException.class);
        VirtualMachine actVM = sut.createFullClone(null);
        
        exception = ExpectedException.none();
        verify(vbmMocked, never()).connect(anyString(), anyString(), anyString());
    }
    
    @Test
    public void createLinkedCloneWithInvalidConnection(){
        PhysicalMachine pm = new PhysicalMachine("150.150.14.87","18083","John","trio158hy7");
        doThrow(VBoxException.class).when(vbmMocked).connect("http://150.150.14.87:18083", "John", "trio158hy7");
        
        VirtualMachine vmToClone = new VirtualMachine.Builder(UUID.fromString("793d047c-1148-4a55-a9b7-00aa455570a1"), "Fedora-21-WS", pm)
                                                     .countOfCPU(1L).countOfMonitors(1L).cpuExecutionCap(100L)
                                                     .hardDiskFreeSpaceSize(14286848000L).hardDiskTotalSize(21474836480L)
                                                     .sizeOfRAM(4096L).sizeOfVRAM(12L).typeOfOS("Linux").versionOfOS("Fedora_64").build();
        
        exception.expect(ConnectionFailureException.class);
        VirtualMachine actVM = sut.createFullClone(vmToClone);
        
        exception = ExpectedException.none();
        verify(vbmMocked, times(3)).connect("http://150.150.14.87:18083", "John", "trio158hy7");
        verify(vbmMocked, never()).getVBox();
    }

    private void assertDeepVMsEquals(List<VirtualMachine> expVMs, List<VirtualMachine> actVMs){
        for(int i = 0; i < expVMs.size(); ++i){
            VirtualMachine expVM = expVMs.get(i);
            VirtualMachine actVM = actVMs.get(i);
            assertDeepVMsEquals(expVM,actVM);
        }
    }
    
    private void assertDeepVMsEquals(VirtualMachine expVM, VirtualMachine actVM){
        assertEquals("VMs should have same id",expVM.getId(),actVM.getId());
        assertEquals("VMs should have same name",expVM.getVmName(),actVM.getVmName());
        assertEquals("VMs should have same host machine",expVM.getHostMachine(),actVM.getHostMachine());
        assertEquals("VMs should have same count of CPUs",expVM.getCountOfCPU(),actVM.getCountOfCPU());
        assertEquals("VMs should have same count of monitors",expVM.getCountOfMonitors(),actVM.getCountOfMonitors());
        assertEquals("VMs should have same CPUExecutionCap",expVM.getCPUExecutionCap(),actVM.getCPUExecutionCap());
        assertEquals("VMs should have same HDD free space size",expVM.getHardDiskFreeSpaceSize(),actVM.getHardDiskFreeSpaceSize());
        assertEquals("VMs should have same HDD total size",expVM.getHardDiskTotalSize(),actVM.getHardDiskTotalSize());
        assertEquals("VMs should have same RAM size",expVM.getSizeOfRAM(),actVM.getSizeOfRAM());
        assertEquals("VMs should have same video RAM size",expVM.getSizeOfVRAM(),actVM.getSizeOfVRAM());
        assertEquals("VMs should have same type of OS",expVM.getTypeOfOS(),actVM.getTypeOfOS());
        assertEquals("VMs should have same version of OS",expVM.getVersionOfOS(),actVM.getVersionOfOS());
    }
    
    private static Comparator<VirtualMachine> vmComparator = (VirtualMachine o1, VirtualMachine o2) -> {
        int res = o1.getId().compareTo(o2.getId());
        
        if(res == 0){
            return (o1.getVmName().compareTo(o2.getVmName())); 
        }
        
        return res;
    };
    
}

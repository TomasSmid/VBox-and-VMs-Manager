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
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.UnknownVirtualMachineException;
import cz.muni.fi.vboxvmsmanager.pubapi.types.CloneType;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/**
 *
 * @author Tomáš Šmíd
 */
public class VirtualizationToolManagerImplTest {
    
    private VirtualizationToolManagerImpl sut;
    private NativeVBoxAPIManager natmanMocked;
    
    @Before
    public void setUp() {
        PhysicalMachine pm = new PhysicalMachine("150.150.14.87","18083","John","trio158hy7");
        sut = new VirtualizationToolManagerImpl(pm);
        natmanMocked = mock(NativeVBoxAPIManager.class);
    }

    @Test
    public void findVMByIdWithValidConnectionAndExistingVM() throws ConnectionFailureException, InterruptedException, IncompatibleVirtToolAPIVersionException, UnknownVirtualMachineException{
        VirtualMachine expVM = new VMBuilder().build();
        doReturn(expVM).when(natmanMocked).getVirtualMachineById(expVM.getHostMachine(), expVM.getId());
        
        VirtualMachine actVM = sut.findVirtualMachineById(UUID.fromString("793d084a-0189-4a55-a9b7-531c455570a1"));
        
        assertNotNull("Returned virtual machine should not be null",actVM);
        assertDeepVMsEquals(expVM,actVM);
    }
    
    @Test
    public void findVMByIdWithValidConnectionAndNotExistingVM() throws ConnectionFailureException{
        VirtualMachine expVM = new VMBuilder().build();
        doThrow(UnknownVirtualMachineException.class).when(natmanMocked).getVirtualMachineById(expVM.getHostMachine(), expVM.getId());
        
        VirtualMachine actVM = sut.findVirtualMachineById(UUID.fromString("793d084a-0189-4a55-a9b7-531c455570a1"));
        
        assertNull("Returned virtual machine should be null",actVM);
    }
    
    @Test
    public void findVMByIdWithValidConnectionAndNullId() throws ConnectionFailureException{
        VirtualMachine expVM = new VMBuilder().build();
        doThrow(IllegalArgumentException.class).when(natmanMocked).getVirtualMachineById(expVM.getHostMachine(), null);
        
        VirtualMachine actVM = sut.findVirtualMachineById(null);
        
        assertNull("Returned virtual machine should be null",actVM);
    }
    
    @Test
    public void findVMByIdWithInvalidConnection() throws ConnectionFailureException{
        VirtualMachine expVM = new VMBuilder().build();
        doThrow(ConnectionFailureException.class).when(natmanMocked).getVirtualMachineById(expVM.getHostMachine(), expVM.getId());
        
        VirtualMachine actVM = sut.findVirtualMachineById(UUID.fromString("793d084a-0189-4a55-a9b7-531c455570a1"));
        
        assertNull("Returned virtual machine should be null",actVM);
    }
    
    @Test
    public void findVMByNameWithValidConnectionAndExistingVM(){
        VirtualMachine expVM = new VMBuilder().build();
        doReturn(expVM).when(natmanMocked).getVirtualMachineByName(expVM.getHostMachine(), expVM.getVMName());
        
        VirtualMachine actVM = sut.findVirtualMachineByName("Fedora-21-WS");
        
        assertNotNull("Returned virtual machine should not be null",actVM);
        assertDeepVMsEquals(expVM,actVM);
    }
    
    @Test
    public void findVMByNameWithValidConnectionAndNotExistingVM(){
        VirtualMachine expVM = new VMBuilder().build();
        doThrow(UnknownVirtualMachineException.class).when(natmanMocked).getVirtualMachineByName(expVM.getHostMachine(), expVM.getVMName());
        
        VirtualMachine actVM = sut.findVirtualMachineByName("Fedora-21-WS");
        
        assertNull("Returned virtual machine should be null",actVM);
    }
    
    @Test
    public void findVMByNameWithValidConnectionAndEmptyNameString(){
        VirtualMachine expVM = new VMBuilder().build();
        doThrow(IllegalArgumentException.class).when(natmanMocked).getVirtualMachineByName(expVM.getHostMachine(), "");
        
        VirtualMachine actVM = sut.findVirtualMachineByName("");
        
        assertNull("Returned virtual machine should be null",actVM);
    }
    
    @Test
    public void findVMByNameWithValidConnectionAndNullName(){
        VirtualMachine expVM = new VMBuilder().build();
        doThrow(IllegalArgumentException.class).when(natmanMocked).getVirtualMachineByName(expVM.getHostMachine(), null);
        
        VirtualMachine actVM = sut.findVirtualMachineByName(null);
        
        assertNull("Returned virtual machine should be null",actVM);
    }
    
    @Test
    public void findVMByNameWithInvalidConnection(){
        VirtualMachine expVM = new VMBuilder().build();
        doThrow(ConnectionFailureException.class).when(natmanMocked).getVirtualMachineByName(expVM.getHostMachine(), expVM.getVMName());
        
        VirtualMachine actVM = sut.findVirtualMachineByName("Fedora-21-WS");
        
        assertNull("Returned virtual machine should be null",actVM);
    }
    
    @Test
    public void getVMsWithValidConnectionAndSomeReturnedMachines(){
        VirtualMachine expVM1 = new VMBuilder().build();
        VirtualMachine expVM2 = new VMBuilder().id(UUID.fromString("793d000a-1111-89de-a9b7-531c455570a1"))
                                               .vmName("Windows 10").typeOfOS("Windows").versionOfOS("Win10_64").build();
        List<VirtualMachine> expVMs = Arrays.asList(expVM1,expVM2);
        
        assertEquals(expVM1.getHostMachine(),expVM2.getHostMachine());
        doReturn(expVMs).when(natmanMocked).getVirtualMachines(expVM1.getHostMachine());
        
        List<VirtualMachine> actVMs = sut.getVirtualMachines();
        
        assertEquals("List should contain 2 virtual machines",2,actVMs.size());
        
        Collections.sort(expVMs, vmComparator);
        Collections.sort(actVMs, vmComparator);
        
        assertDeepVMsEquals(expVMs,actVMs);
    }
    
    @Test
    public void getVMsWithValidConnectionAndNoReturnedMachines(){
        VirtualMachine expVM = new VMBuilder().build();
        List<VirtualMachine> emptyList = new ArrayList<>();
        doReturn(emptyList).when(natmanMocked).getVirtualMachines(expVM.getHostMachine());
        
        List<VirtualMachine> actVMs = sut.getVirtualMachines();
        
        assertTrue("Returned list of virtual machines should be empty",actVMs.isEmpty());        
    }
    
    @Test
    public void getVMsWithInvalidConnection(){
        VirtualMachine expVM = new VMBuilder().build();
        doThrow(ConnectionFailureException.class).when(natmanMocked).getVirtualMachines(expVM.getHostMachine());
        
        List<VirtualMachine> actVMs = sut.getVirtualMachines();
        
        assertNull("Returned list of virtual machines should be null",actVMs);
    }
    
    @Test
    public void removeVMWithValidExistingVM(){
        VirtualMachine vm = new VMBuilder().build();        
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        String outMsg = "Virtual machine " + vm.toString() + " removed successfully";
        
        sut.removeVirtualMachine(vm);
        
        assertEquals("Output messages should be same",outMsg,outContent);
        
        System.setOut(null);
    }
    
    @Test
    public void removeVMWithValidNotExistingVM(){
        VirtualMachine vm = new VMBuilder().build();        
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String errMsg = "Removing VM failure: There is no virtual machine " + vm.toString()
                + ", which could be removed";
        doThrow(UnknownVirtualMachineException.class).when(natmanMocked).removeVM(vm);
        
        sut.removeVirtualMachine(vm);
        
        assertEquals("Output messages should be same",errMsg,errContent);
        
        System.setErr(null);
    }
    
    @Test
    public void removeVMWithNullVM(){
        VirtualMachine vm = new VMBuilder().build();        
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String errMsg = "Removing VM failure: There was made an attempt to remove an "
                + "illegal (null) virtual machine object";
        doThrow(IllegalArgumentException.class).when(natmanMocked).removeVM(null);
        
        sut.removeVirtualMachine(null);
        
        assertEquals("Output messages should be same",errMsg,errContent);
        
        System.setErr(null);
    }
    
    @Test
    public void removeVMWithInvalidConnection(){
        VirtualMachine vm = new VMBuilder().build();        
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String errMsg = "Removing VM failure: There is a connection problem to "
                + "\"http://" + vm.getHostMachine().getAddressIP() + ":"
                + vm.getHostMachine().getPortOfVTWebServer() + "\". "
                + "Most probably there could be one of two possible problems - "
                + "network connection is not working or remote VirtualBox"
                + "web server is not running.\nPossible solution: check both "
                + "network connection and remote VirtualBox web server are"
                + " running and working correctly and then repeat removing "
                + "virtual machine operation.";
        doThrow(ConnectionFailureException.class).when(natmanMocked).removeVM(vm);
        
        sut.removeVirtualMachine(vm);
        
        assertEquals("Output messages should be same",errMsg,errContent);
        
        System.setErr(null);
    }
    
    @Test
    public void cloneVMAsFullCopyWithValidConnectionAndExistingVM(){
        VirtualMachine origVM = new VMBuilder().build();
        VirtualMachine copyVM = new VMBuilder().id(UUID.fromString("78eea41d-0204-abae-a9b7-531c455570a1"))
                                               .vmName(origVM.getVMName()+"_FCopy1").build();
        doReturn(copyVM).when(natmanMocked).createFullClone(origVM);
        
        VirtualMachine actCopyVM = sut.cloneVirtualMachine(origVM, CloneType.FULL);
        
        assertNotNull("The clone should exist and not be null", actCopyVM);
        assertDeepVMsEquals(copyVM, actCopyVM);
    }
    
    @Test
    public void cloneVMAsFullCopyWithValidConnectionAndNotExistingVM(){
        VirtualMachine origVM = new VMBuilder().build();
        doThrow(UnknownVirtualMachineException.class).when(natmanMocked).createFullClone(origVM);
        
        VirtualMachine actCopyVM = sut.cloneVirtualMachine(origVM, CloneType.FULL);
        
        assertNull("The clone should be null", actCopyVM);
    }
    
    @Test
    public void cloneVMAsFullCopyWithValidConnectionAndNullVM(){
        VirtualMachine origVM = new VMBuilder().build();
        doThrow(IllegalArgumentException.class).when(natmanMocked).createFullClone(null);
        
        VirtualMachine actCopyVM = sut.cloneVirtualMachine(null, CloneType.FULL);
        
        assertNull("The clone should be null", actCopyVM);
    }
    
    @Test
    public void cloneVMAsFullCopyWithInvalidConnection(){
        VirtualMachine origVM = new VMBuilder().build();
        doThrow(ConnectionFailureException.class).when(natmanMocked).createFullClone(origVM);
        
        VirtualMachine actCopyVM = sut.cloneVirtualMachine(origVM, CloneType.FULL);
        
        assertNull("The clone should be null", actCopyVM);
    }
    
   @Test
    public void cloneVMAsLinkedCopyWithValidConnectionAndExistingVM(){
        VirtualMachine origVM = new VMBuilder().build();
        VirtualMachine copyVM = new VMBuilder().id(UUID.fromString("78eea41d-0204-abae-a9b7-531c455570a1"))
                                               .vmName(origVM.getVMName()+"_LCopy1").build();
        doReturn(copyVM).when(natmanMocked).createLinkedClone(origVM);
        
        VirtualMachine actCopyVM = sut.cloneVirtualMachine(origVM, CloneType.LINKED);
        
        assertNotNull("The clone should exist and not be null", actCopyVM);
        assertDeepVMsEquals(copyVM, actCopyVM);
    }
    
    @Test
    public void cloneVMAsLinkedCopyWithValidConnectionAndNotExistingVM(){
        VirtualMachine origVM = new VMBuilder().build();
        doThrow(UnknownVirtualMachineException.class).when(natmanMocked).createLinkedClone(origVM);
        
        VirtualMachine actCopyVM = sut.cloneVirtualMachine(origVM, CloneType.LINKED);
        
        assertNull("The clone should be null", actCopyVM);
    }
    
    @Test
    public void cloneVMAsLinkedCopyWithValidConnectionAndNullVM(){
        VirtualMachine origVM = new VMBuilder().build();
        doThrow(IllegalArgumentException.class).when(natmanMocked).createLinkedClone(null);
        
        VirtualMachine actCopyVM = sut.cloneVirtualMachine(null, CloneType.LINKED);
        
        assertNull("The clone should be null", actCopyVM);
    }
    
    @Test
    public void cloneVMAsLinkedCopyWithInvalidConnection(){
        VirtualMachine origVM = new VMBuilder().build();
        doThrow(ConnectionFailureException.class).when(natmanMocked).createLinkedClone(origVM);
        
        VirtualMachine actCopyVM = sut.cloneVirtualMachine(origVM, CloneType.LINKED);
        
        assertNull("The clone should be null", actCopyVM);
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
        assertEquals("VMs should have same name",expVM.getVMName(),actVM.getVMName());
        assertEquals("VMs should have same host machine",expVM.getHostMachine(),actVM.getHostMachine());
        assertEquals("VMs should have same count of CPUs",expVM.getCountOfCPU(),actVM.getCountOfCPU());
        assertEquals("VMs should have same count of monitors",expVM.getCountOfMonitors(),actVM.getCountOfMonitors());
        assertEquals("VMs should have same CPUExecutionCap",expVM.getCPUExecutionCap(),actVM.getCPUExecutionCap());
        assertEquals("VMs should have same HDD free space size",expVM.getHardDiskFreeSpaceSize(),actVM.getHardDiskFreeSpaceSize());
        assertEquals("VMs should have same HDD total size",expVM.getHardDiskTotalSize(),actVM.getHardDiskTotalSize());
        assertEquals("VMs should have same RAM size",expVM.getSizeOfRAM(),actVM.getSizeOfRAM());
        assertEquals("VMs should have same video RAM size",expVM.getSizeOfVRAM(),actVM.getSizeOfVRAM());
        assertEquals("VMs should have same type of OS",expVM.getTypeOfOS(),actVM.getTypeOfOS());
        assertEquals("VMs should have same version of OS",expVM.getIdentifierOfOS(),actVM.getIdentifierOfOS());
    }
    
    private static Comparator<VirtualMachine> vmComparator = (VirtualMachine o1, VirtualMachine o2) -> {
        int res = o1.getId().compareTo(o2.getId());
        
        if(res == 0){
            return (o1.getVMName().compareTo(o2.getVMName())); 
        }
        
        return res;
    };
    
    class VMBuilder{
        private UUID id = UUID.fromString("793d084a-0189-4a55-a9b7-531c455570a1");
        private String vmName = "Fedora-21-WS";
        private PhysicalMachine hostMachine = new PhysicalMachine("150.150.14.87","18083","John","trio158hy7");
        private Long countOfCPU = 1L;
        private Long countOfMonitors = 1L;
        private Long cpuExecutionCap = 100L;
        private Long hardDiskFreeSpaceSize = 14286848000L;
        private Long hardDiskTotalSize = 21474836480L;
        private Long sizeOfRAM = 4096L;
        private Long sizeOfVRAM = 12L;
        private String typeOfOS = "Linux";
        private String versionOfOS = "Fedora_64";
        
        public VMBuilder(){
        
        }
        
        public VMBuilder id(UUID value){
            this.id = value;
            return this;
        }
        
        public VMBuilder vmName(String value){
            this.vmName = value;
            return this;
        }
        
        public VMBuilder hostMachine(PhysicalMachine value){
            this.hostMachine = value;
            return this;
        }
        
        public VMBuilder countOfCPU(Long value){
            this.countOfCPU = value;
            return this;
        }
        
        public VMBuilder countOfMonitors(Long value){
            this.countOfMonitors = value;
            return this;
        }
        
        public VMBuilder cpuExecutionCap(Long value){
            this.cpuExecutionCap = value;
            return this;
        }
        
        public VMBuilder hardDiskFreeSpaceSize(Long value){
            this.hardDiskFreeSpaceSize = value;
            return this;
        }
        
        public VMBuilder hardDiskTotalSize(Long value){
            this.hardDiskTotalSize = value;
            return this;
        }
        
        public VMBuilder sizeOfRAM(Long value){
            this.sizeOfRAM = value;
            return this;
        }
        
        public VMBuilder sizeOfVRAM(Long value){
            this.sizeOfVRAM = value;
            return this;
        }
        
        public VMBuilder typeOfOS(String value){
            this.typeOfOS = value;
            return this;
        }
        
        public VMBuilder versionOfOS(String value){
            this.versionOfOS = value;
            return this;
        }
        
        public VirtualMachine build(){
            VirtualMachine vm = new VirtualMachine.Builder(id, vmName, hostMachine)
                                                  .countOfCPU(countOfCPU)
                                                  .countOfMonitors(countOfMonitors)
                                                  .cpuExecutionCap(cpuExecutionCap)
                                                  .hardDiskFreeSpaceSize(hardDiskFreeSpaceSize)
                                                  .hardDiskTotalSize(hardDiskTotalSize)
                                                  .sizeOfRAM(sizeOfRAM)
                                                  .sizeOfVRAM(sizeOfVRAM)
                                                  .typeOfOS(typeOfOS)
                                                  .versionOfOS(versionOfOS).build();
            return vm;
        }
    }
}

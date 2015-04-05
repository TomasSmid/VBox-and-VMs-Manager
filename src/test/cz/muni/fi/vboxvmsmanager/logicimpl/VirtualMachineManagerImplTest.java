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
import cz.muni.fi.vboxvmsmanager.pubapi.entities.PortRule;
import cz.muni.fi.vboxvmsmanager.pubapi.entities.VirtualMachine;
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.ConnectionFailureException;
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.PortRuleDuplicityException;
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.UnexpectedVMStateException;
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.UnknownPortRuleException;
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.UnknownVirtualMachineException;
import cz.muni.fi.vboxvmsmanager.pubapi.types.ProtocolType;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.rules.ExpectedException;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.virtualbox_4_3.INATEngine;
import org.virtualbox_4_3.NATProtocol;
import org.virtualbox_4_3.VBoxException;

/**
 *
 * @author Tomáš Šmíd
 */
public class VirtualMachineManagerImplTest {
    
    private VirtualMachineManagerImpl sut;
    private NativeVBoxAPIMachine natmachMocked;
    
    @Before
    public void setUp() {
        sut = new VirtualMachineManagerImpl();
        natmachMocked = mock(NativeVBoxAPIMachine.class);
    }
    
    @Test
    public void startVMWithValidConnectionAndExistingVM(){
        VirtualMachine vm = new VMBuilder().build();
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));        
        String outMsg = "Starting " + vm.toString() + "\n"
            + "\"" + vm.getName() + "\" is Running";        
        
        sut.startVM(vm);
        
        assertEquals("Messages should be same", outMsg, outContent);
        
        System.setOut(null);
    }

    @Test
    public void startVMWithValidConnectionAndNotExistingVM(){
        VirtualMachine vm = new VMBuilder().build();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String errMsg = "Starting VM failure: There is no virtual machine "
                + vm.toString() + " in the list of known virtual machines to "
                + "VirtualBox.";
        doThrow(UnknownVirtualMachineException.class).when(natmachMocked).startVM(vm);
        
        sut.startVM(vm);
        
        assertEquals("Messages should be same",errMsg,errContent);
        
        System.setErr(null);
    }
    
    @Test
    public void startVMWithValidConnectionAndNullVM(){
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String errMsg = "Starting VM failure: There was made an attempt to start an "
                + "illegal (null) virtual machine object.";
        doThrow(IllegalArgumentException.class).when(natmachMocked).startVM(null);
        
        sut.startVM(null);
        
        assertEquals("Messages should be same",errMsg,errContent);
        
        System.setErr(null);
    }
    
    @Test
    public void startAlreadyRunningVM(){
        VirtualMachine vm = new VMBuilder().build();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String errMsg = "Starting VM failure: Virtual machine " + vm.toString()
                + " is already running.";
        doThrow(UnexpectedVMStateException.class).when(natmachMocked).startVM(vm);
        
        sut.startVM(vm);
        
        assertEquals("Messages should be same",errMsg,errContent);
        
        System.setErr(null);
    }
    
    @Test
    public void startVMWithInvalidConnection(){
        VirtualMachine vm = new VMBuilder().build();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String errMsg = "Starting VM failure: There is a connection problem to "
                + "\"http://" + vm.getHostMachine().getAddressIP() + ":"
                + vm.getHostMachine().getPortOfVTWebServer() + "\". "
                + "Most probably there could be one of two possible problems - "
                + "network connection is not working or remote VirtualBox"
                + "web server is not running.\nPossible solution: check both "
                + "network connection and remote VirtualBox web server are"
                + " running and working correctly and then repeat starting "
                + "virtual machine operation.";
        doThrow(ConnectionFailureException.class).when(natmachMocked).startVM(vm);
        
        sut.startVM(vm);
        
        assertEquals("Messages should be same",errMsg,errContent);
        
        System.setErr(null);
    }
    
    @Test
    public void shutDownVMWithValidConnectionAndExistingVM(){
        VirtualMachine vm = new VMBuilder().build();
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        String outMsg = "Shutting down " + vm.toString() + "\n"
            + "\"" + vm.getName() + "\" is PoweredOff";
        
        sut.shutDownVM(vm);
        
        assertEquals("Messages should be same", outMsg, outContent);
        
        System.setOut(null);
    }
    
    @Test
    public void shutDownVMWithValidConnectionAndNotExistingVM(){
        VirtualMachine vm = new VMBuilder().build();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String errMsg = "Shutdown VM failure: There is no virtual machine "
                + vm.toString() + " in the list of known virtual machines to "
                + "VirtualBox.";
        doThrow(UnknownVirtualMachineException.class).when(natmachMocked).shutDownVM(vm);
        
        sut.shutDownVM(vm);
        
        assertEquals("Messages should be same",errMsg,errContent);
        
        System.setErr(null);
    }
    
    @Test
    public void shutDownVMWithValidConnectionAndNullVM(){
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String errMsg = "Shutdown VM failure: There was made an attempt to shut down an "
                + "illegal (null) virtual machine object.";
        doThrow(IllegalArgumentException.class).when(natmachMocked).shutDownVM(null);
        
        sut.shutDownVM(null);
        
        assertEquals("Messages should be same",errMsg,errContent);
        
        System.setErr(null);
    }
    
    @Test
    public void shutDownAlreadyPoweredOffVM(){
        VirtualMachine vm = new VMBuilder().build();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String errMsg = "Shutdown VM failure: Virtual machine " + vm.toString()
                + " is already powered off.";
        doThrow(UnexpectedVMStateException.class).when(natmachMocked).shutDownVM(vm);
        
        sut.shutDownVM(vm);
        
        assertEquals("Messages should be same",errMsg,errContent);
        
        System.setErr(null);
    }
    
    @Test
    public void shutDownVMWithInvalidConnection(){
        VirtualMachine vm = new VMBuilder().build();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String errMsg = "Shutdown VM failure: There is a connection problem to "
                + "\"http://" + vm.getHostMachine().getAddressIP() + ":"
                + vm.getHostMachine().getPortOfVTWebServer() + "\". "
                + "Most probably there could be one of two possible problems - "
                + "network connection is not working or remote VirtualBox"
                + "web server is not running.\nPossible solution: check both "
                + "network connection and remote VirtualBox web server are"
                + " running and working correctly and then repeat shutdown "
                + "virtual machine operation.";
        doThrow(ConnectionFailureException.class).when(natmachMocked).shutDownVM(vm);
        
        sut.shutDownVM(vm);
        
        assertEquals("Messages should be same",errMsg,errContent);
        
        System.setErr(null);
    }
    
    @Test
    public void addPortRuleWithValidUniqueValues(){
        VirtualMachine vm = new VMBuilder().build();
        PortRule pr = new PortRule.Builder("Rule1", 2222, 22).build();
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        String outMsg = "Port rule \"" + pr.getName() + "\" added successfully";
        
        sut.addPortRule(vm, pr);
        
        assertEquals("Messages should be same", outMsg, outContent);        
    }
    
    @Test
    public void addPortRuleWithDuplicateRuleNameOnSingleIP(){
        VirtualMachine vm = new VMBuilder().build();
        PortRule pr = new PortRule.Builder("Rule1", 2222, 22).build();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String errMsg = "Adding port rule failure: Port rule named \"" + pr.getName() + "\""
                + " associated with virtual machine " + vm.toString() + " already exists."
                + " Name of port rule must be unique within a single virtual machine.";
        doThrow(PortRuleDuplicityException.class).when(natmachMocked).addPortRule(vm, pr);
        
        sut.addPortRule(vm, pr);
        
        assertEquals("Messages should be same", errMsg, errContent);   
    }
    
    @Test
    public void addPortRuleWithDuplicateUseOfHostPortOnSingleIP(){
        VirtualMachine vm = new VMBuilder().build();
        PortRule pr = new PortRule.Builder("Rule1", 2222, 22).build();
        PortRule pr2 = new PortRule.Builder("Rule2", 2222, 23).build();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String errMsg = "Adding port rule failure: Port rule named \"" + pr2.getName() + "\""
                + " uses host port number \"" + pr2.getHostPort() + "\", which is already used"
                + " by any other port rule within the same virtual machine. Host port number can"
                + " be used only once within a single virtual machine.";
        doThrow(PortRuleDuplicityException.class).when(natmachMocked).addPortRule(vm, pr2);
        
        sut.addPortRule(vm, pr);
        sut.addPortRule(vm, pr2);
        
        assertEquals("Messages should be same", errMsg, errContent); 
    }
    
    @Test
    public void addPortRuleWithNotExistingVM(){
        VirtualMachine vm = new VMBuilder().build();
        PortRule pr = new PortRule.Builder("Rule1", 2222, 22).build();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String errMsg = "Adding port rule failure: There is no virtual machine "
                + vm.toString() + " in the list of known virtual machines to "
                + "VirtualBox.";
        doThrow(UnknownVirtualMachineException.class).when(natmachMocked).addPortRule(vm, pr);
        
        sut.addPortRule(vm, pr);
        
        assertEquals("Messages should be same", errMsg, errContent); 
    }
    
    @Test
    public void addPortRuleWithEmptyRuleName(){
        VirtualMachine vm = new VMBuilder().build();
        PortRule pr = new PortRule.Builder("", 2222, 22).build();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String errMsg = "Adding port rule failure: Port rule has an illegal (empty string) name."
                + " Every single port rule must have specified non-empty name.";
        doThrow(IllegalArgumentException.class).when(natmachMocked).addPortRule(vm, pr);
        
        sut.addPortRule(vm, pr);
        
        assertEquals("Messages should be same", errMsg, errContent); 
    }
    
    @Test
    public void addPortRuleWithNullPortRuleName(){
        VirtualMachine vm = new VMBuilder().build();
        PortRule pr = new PortRule.Builder(null, 2222, 22).build();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String errMsg = "Adding port rule failure: Port rule has an illegal (null) name."
                + " Every single port rule must have specified non-empty name.";
        doThrow(IllegalArgumentException.class).when(natmachMocked).addPortRule(vm, pr);
        
        sut.addPortRule(vm, pr);
        
        assertEquals("Messages should be same", errMsg, errContent); 
    }
    
    @Test
    public void addPortRuleWithNegativeHostPortNumber(){
        VirtualMachine vm = new VMBuilder().build();
        PortRule pr = new PortRule.Builder("Rule1", -1, 22).build();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String errMsg = "Adding port rule failure: Port rule named \"" + pr.getName() + "\""
                + " has negative host port number. Port number can only be some of the range 0-65535.";
        doThrow(IllegalArgumentException.class).when(natmachMocked).addPortRule(vm, pr);
        
        sut.addPortRule(vm, pr);
        
        assertEquals("Messages should be same", errMsg, errContent);
    }
    
    @Test
    public void addPortRuleWithTooBigHostPortNumber(){
        VirtualMachine vm = new VMBuilder().build();
        PortRule pr = new PortRule.Builder("Rule1", 65536, 22).build();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String errMsg = "Adding port rule failure: Port rule named \"" + pr.getName() + "\""
                + " has too big host port number. Port number can only be some of the range 0-65535.";
        doThrow(IllegalArgumentException.class).when(natmachMocked).addPortRule(vm, pr);
        
        sut.addPortRule(vm, pr);
        
        assertEquals("Messages should be same", errMsg, errContent);
    }
    
    @Test
    public void addPortRuleWithNegativeGuestPortNumber(){
        VirtualMachine vm = new VMBuilder().build();
        PortRule pr = new PortRule.Builder("Rule1", 2222, -1).build();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String errMsg = "Adding port rule failure: Port rule named \"" + pr.getName() + "\""
                + " has negative guest port number. Port number can only be some of the range 0-65535.";
        doThrow(IllegalArgumentException.class).when(natmachMocked).addPortRule(vm, pr);
        
        sut.addPortRule(vm, pr);
        
        assertEquals("Messages should be same", errMsg, errContent);
    }
    
    @Test
    public void addPortRuleWithTooHighGuestPortNumber(){
        VirtualMachine vm = new VMBuilder().build();
        PortRule pr = new PortRule.Builder("Rule1", 2222, 65536).build();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String errMsg = "Adding port rule failure: Port rule named \"" + pr.getName() + "\""
                + " has too big guest port number. Port number can only be some of the range 0-65535.";
        doThrow(IllegalArgumentException.class).when(natmachMocked).addPortRule(vm, pr);
        
        sut.addPortRule(vm, pr);
        
        assertEquals("Messages should be same", errMsg, errContent);
    }
    
    @Test
    public void addPortRuleWithNullPortRule(){
        VirtualMachine vm = new VMBuilder().build();
        PortRule pr = null;
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String errMsg = "Adding port rule failure: There was made an attempt to "
                + " add a new illegal (null) port rule to virtual machine "
                + vm.toString() + ".";
        doThrow(IllegalArgumentException.class).when(natmachMocked).addPortRule(vm, pr);
        
        sut.addPortRule(vm, pr);
        
        assertEquals("Messages should be same", errMsg, errContent);
    }
    
    @Test
    public void addPortRuleWithNullVM(){
        VirtualMachine vm = null;
        PortRule pr = new PortRule.Builder("Rule1", 2222, 22).build();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String errMsg = "Adding port rule failure: There was made an attempt to add"
                + "a new port rule to illegal (null) virtual machine object.";
        doThrow(IllegalArgumentException.class).when(natmachMocked).addPortRule(vm, pr);
        
        sut.addPortRule(vm, pr);
        
        assertEquals("Messages should be same", errMsg, errContent);
    }
    
    @Test
    public void addPortRuleWithBothNullArguments(){
        VirtualMachine vm = null;
        PortRule pr = null;
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String errMsg = "Adding port rule failure: There was made an attempt to add"
                + " a new illegal (null) port rule to an illegal (null) virtual machine object.";
        doThrow(IllegalArgumentException.class).when(natmachMocked).addPortRule(vm, pr);
        
        sut.addPortRule(vm, pr);
        
        assertEquals("Messages should be same", errMsg, errContent);
    }
    
    @Test
    public void addPortRuleWithInvalidConection(){
        VirtualMachine vm = new VMBuilder().build();
        PortRule pr = new PortRule.Builder("Rule1", 2222, 22).build();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String errMsg = "Adding port rule failure: There is a connection problem to "
                + "\"http://" + vm.getHostMachine().getAddressIP() + ":"
                + vm.getHostMachine().getPortOfVTWebServer() + "\". "
                + "Most probably there could be one of two possible problems - "
                + "network connection is not working or remote VirtualBox"
                + "web server is not running.\nPossible solution: check both "
                + "network connection and remote VirtualBox web server are"
                + " running and working correctly and then repeat adding port rule "
                + "operation.";
        doThrow(ConnectionFailureException.class).when(natmachMocked).shutDownVM(vm);
        
        sut.shutDownVM(vm);
        
        assertEquals("Messages should be same",errMsg,errContent);
        
        System.setErr(null);
    }
    
    @Test
    public void deletePortRuleWithValidValues(){
        
    }
    
    @Test
    public void deletePortRuleWithEmptyRuleName(){
        
    }
    
    @Test
    public void deletePortRuleWithNullRuleName(){
        
    }
    
    @Test
    public void deletePortRuleWithNotExistingPortRuleName(){
        
    }
    
    @Test
    public void deletePortRuleWithNotExistingVM(){
        
    }
    
    @Test
    public void deletePortRuleWithNullVM(){
        
    }
    
    @Test
    public void deletePortRuleWithInvalidConnection(){
        
    }
    
    @Test
    public void getPortRulesWithSomeExistingRules(){
        
    }
    
    @Test
    public void getPortRulesWithNoExistingRules(){
        
    }
    
    @Test
    public void getPortRulesWithInvalidConnection(){
        
    }
    
    private void assertDeepPREquals(List<PortRule> expPortRules, List<PortRule> actPortRules){
        for(int i = 0; i < expPortRules.size(); ++i){
            assertDeepPREquals(expPortRules.get(i),actPortRules.get(i));
        }
    }
    
    private void assertDeepPREquals(PortRule expPR, PortRule actPR){
        assertEquals("Port rules names should be same",expPR.getName(),actPR.getName());
        assertEquals("Port rules protocols should be same",expPR.getProtocol(),actPR.getProtocol());
        assertEquals("Port rules host IPs should be same",expPR.getHostIP(),actPR.getHostIP());
        assertEquals("Port rules host ports should be same",expPR.getHostPort(),actPR.getHostPort());
        assertEquals("Port rules guest IPs should be same",expPR.getGuestIP(),actPR.getGuestIP());
        assertEquals("Port rules guest ports should be same",expPR.getGuestPort(),actPR.getGuestPort());
    }
    
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

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
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
import org.virtualbox_4_3.IConsole;
import org.virtualbox_4_3.IMachine;
import org.virtualbox_4_3.INATEngine;
import org.virtualbox_4_3.ISession;
import org.virtualbox_4_3.IVirtualBox;
import org.virtualbox_4_3.NATProtocol;
import org.virtualbox_4_3.VBoxException;
import org.virtualbox_4_3.VirtualBoxManager;

/**
 *
 * @author Tomáš Šmíd
 */
public class NativeVBoxAPIMachineTest {
    
    @Rule
    public ExpectedException exception = ExpectedException.none();
    
    private NativeVBoxAPIMachine sut;
    private VirtualBoxManager vbmMocked;
    private IVirtualBox vboxMocked;
    private IMachine machMocked;
    
    @Before
    public void setUp() {
        sut = new NativeVBoxAPIMachine();
        vbmMocked = mock(VirtualBoxManager.class);
        vboxMocked = mock(IVirtualBox.class);
        machMocked = mock(IMachine.class);
    }
    
    @Test
    public void startVMWithValidConnectionAndExistingVM(){
        VirtualMachine vm = new VMBuilder().build();
        String url = "http://" + vm.getHostMachine().getAddressIP() + ":"
                        + vm.getHostMachine().getPortOfVTWebServer();
        String user = vm.getHostMachine().getUsername();
        String pswd = vm.getHostMachine().getUserPassword();
        ISession sesMocked = mock(ISession.class);
        
        sut.startVM(vm);
        
        verify(vbmMocked).connect(url, user, pswd);
        verify(machMocked).launchVMProcess(sesMocked, "gui", "");
        verify(sesMocked).unlockMachine();
    }
    
    @Test
    public void startVMWithValidConnectionAndNotExistingVM(){
        VirtualMachine vm = new VMBuilder().build();
        
        exception.expect(UnknownVirtualMachineException.class);
        sut.startVM(vm);
        
        exception = ExpectedException.none();
        verify(machMocked, never()).launchVMProcess(any(ISession.class), anyString(), anyString());
    }
    
    @Test
    public void startVMWithValidConnectionAndNullVM(){
        exception.expect(IllegalArgumentException.class);
        sut.startVM(null);
        
        exception = ExpectedException.none();
        verify(vbmMocked, never()).connect(anyString(), anyString(), anyString());
    }
    
    @Test
    public void startAlreadyRunningVM(){
        VirtualMachine vm = new VMBuilder().build();
        
        exception.expect(UnexpectedVMStateException.class);
        sut.startVM(vm);
        
        exception = ExpectedException.none();
        verify(machMocked, never()).launchVMProcess(any(ISession.class), anyString(), anyString());
    }
    
    @Test
    public void startVMWithInvalidConnection(){
        VirtualMachine vm = new VMBuilder().build();
        String url = "http://" + vm.getHostMachine().getAddressIP() + ":"
                        + vm.getHostMachine().getPortOfVTWebServer();
        String user = vm.getHostMachine().getUsername();
        String pswd = vm.getHostMachine().getUserPassword();
        
        
        exception.expect(ConnectionFailureException.class);
        sut.startVM(vm);
        
        exception = ExpectedException.none();
        verify(vbmMocked, times(3)).connect(url, user, pswd);
        verify(vbmMocked, never()).getVBox();
    }
    
    @Test
    public void shutDownVMWithValidConnectionAndExistingVM(){
        VirtualMachine vm = new VMBuilder().build();
        String url = "http://" + vm.getHostMachine().getAddressIP() + ":"
                        + vm.getHostMachine().getPortOfVTWebServer();
        String user = vm.getHostMachine().getUsername();
        String pswd = vm.getHostMachine().getUserPassword();
        ISession sesMocked = mock(ISession.class);
        IConsole consoleMocked = mock(IConsole.class);
        
        sut.shutDownVM(vm);
        
        verify(vbmMocked).connect(url, user, pswd);
        verify(consoleMocked).powerDown();
        verify(sesMocked).unlockMachine();
    }
    
    @Test
    public void shutDownVMWithValidConnectionAndNotExistingVM(){
        VirtualMachine vm = new VMBuilder().build();
        IConsole consoleMocked = mock(IConsole.class);
        
        exception.expect(UnknownVirtualMachineException.class);
        sut.shutDownVM(vm);
        
        exception = ExpectedException.none();
        verify(consoleMocked, never()).powerDown();
    }
    
    @Test
    public void shutDownVMWithValidConnectionAndNullVM(){
        exception.expect(IllegalArgumentException.class);
        sut.shutDownVM(null);
        
        exception = ExpectedException.none();
        verify(vbmMocked, never()).connect(anyString(), anyString(), anyString());
    }
    
    @Test
    public void shutDownAlreadyPoweredOffVM(){
        VirtualMachine vm = new VMBuilder().build();
        ISession sesMocked = mock(ISession.class);
        IConsole consoleMocked = mock(IConsole.class);
        
        exception.expect(UnexpectedVMStateException.class);
        sut.startVM(vm);
        
        exception = ExpectedException.none();
        verify(sesMocked, never()).getConsole();
        verify(consoleMocked, never()).powerDown();
    }
    
    @Test
    public void shutDownVMWithInvalidConnection(){
        VirtualMachine vm = new VMBuilder().build();
        String url = "http://" + vm.getHostMachine().getAddressIP() + ":"
                        + vm.getHostMachine().getPortOfVTWebServer();
        String user = vm.getHostMachine().getUsername();
        String pswd = vm.getHostMachine().getUserPassword();
        
        
        exception.expect(ConnectionFailureException.class);
        sut.shutDownVM(vm);
        
        exception = ExpectedException.none();
        verify(vbmMocked, times(3)).connect(url, user, pswd);
        verify(vbmMocked, never()).getVBox();
    }
    
    @Test
    public void addPortRuleWithValidUniqueValues(){
        VirtualMachine vm = new VMBuilder().build();
        PortRule pr = new PortRule.Builder("Rule1",2222,22).build();
        String url = "http://" + vm.getHostMachine().getAddressIP() + ":"
                        + vm.getHostMachine().getPortOfVTWebServer();
        String user = vm.getHostMachine().getUsername();
        String pswd = vm.getHostMachine().getUserPassword();
        INATEngine natenMocked = mock(INATEngine.class);
        
        sut.addPortRule(vm, pr);
        
        verify(vbmMocked).connect(url, user, pswd);
        verify(natenMocked).addRedirect(pr.getName(), NATProtocol.TCP, pr.getHostIP(),
                                        pr.getHostPort(), pr.getGuestIP(), pr.getGuestPort());        
    }
    
    @Test
    public void addPortRuleWithDuplicateRuleNameOnSingleIP(){
        VirtualMachine vm = new VMBuilder().build();
        PortRule pr = new PortRule.Builder("Rule1",2222,22).build();
        PortRule pr2 = new PortRule.Builder("Rule1", 3333, 33).build();
        INATEngine natenMocked = mock(INATEngine.class);
        doThrow(VBoxException.class).when(natenMocked).addRedirect(pr2.getName(), NATProtocol.TCP, pr2.getHostIP(),
                                                                   pr2.getHostPort(), pr2.getGuestIP(), pr2.getGuestPort());
        
        sut.addPortRule(vm, pr);
        
        exception.expect(PortRuleDuplicityException.class);
        sut.addPortRule(vm, pr2);
    }
    
    @Test
    public void addPortRuleWithDuplicateUseOfHostPortOnSingleIP(){
        VirtualMachine vm = new VMBuilder().build();
        PortRule pr = new PortRule.Builder("Rule1",2222,22).build();
        PortRule pr2 = new PortRule.Builder("Rule2", 2222, 33).build();
        INATEngine natenMocked = mock(INATEngine.class);
        doThrow(VBoxException.class).when(natenMocked).addRedirect(pr2.getName(), NATProtocol.TCP, pr2.getHostIP(),
                                                                   pr2.getHostPort(), pr2.getGuestIP(), pr2.getGuestPort());
        
        sut.addPortRule(vm, pr);
        
        exception.expect(PortRuleDuplicityException.class);
        sut.addPortRule(vm, pr2);
    }
    
    @Test
    public void addPortRuleWithNotExistingVM(){
        VirtualMachine vm = new VMBuilder().build();
        PortRule pr = new PortRule.Builder("Rule1",2222,22).build();
        INATEngine natenMocked = mock(INATEngine.class);
        doThrow(VBoxException.class).when(vboxMocked).findMachine(vm.getId().toString());
        
        exception.expect(UnknownVirtualMachineException.class);
        sut.addPortRule(vm, pr);
        
        exception = ExpectedException.none();
        verify(vbmMocked, never()).getSessionObject();
        verify(natenMocked, never()).addRedirect(anyString(), any(NATProtocol.class), anyString(),
                                                 anyInt(), anyString(), anyInt());
    }
    
    @Test
    public void addPortRuleWithEmptyRuleName(){
        VirtualMachine vm = new VMBuilder().build();
        PortRule pr = new PortRule.Builder("",2222,22).build();
        INATEngine natenMocked = mock(INATEngine.class);
        
        exception.expect(IllegalArgumentException.class);
        sut.addPortRule(vm, pr);
        
        exception = ExpectedException.none();
        verify(natenMocked, never()).addRedirect(anyString(), any(NATProtocol.class), anyString(),
                                                 anyInt(), anyString(), anyInt());
    }
    
    @Test
    public void addPortRuleWithNullPortRuleName(){
        VirtualMachine vm = new VMBuilder().build();
        PortRule pr = new PortRule.Builder(null,2222,22).build();
        INATEngine natenMocked = mock(INATEngine.class);
        
        exception.expect(IllegalArgumentException.class);
        sut.addPortRule(vm, pr);
        
        exception = ExpectedException.none();
        verify(natenMocked, never()).addRedirect(anyString(), any(NATProtocol.class), anyString(),
                                                 anyInt(), anyString(), anyInt());
    }
    
    @Test
    public void addPortRuleWithNegativeHostPortNumber(){
        VirtualMachine vm = new VMBuilder().build();
        PortRule pr = new PortRule.Builder("Rule1",-1,22).build();
        INATEngine natenMocked = mock(INATEngine.class);
        
        exception.expect(IllegalArgumentException.class);
        sut.addPortRule(vm, pr);
        
        exception = ExpectedException.none();
        verify(natenMocked, never()).addRedirect(anyString(), any(NATProtocol.class), anyString(),
                                                 anyInt(), anyString(), anyInt());
    }
    
    @Test
    public void addPortRuleWithTooBigHostPortNumber(){
        VirtualMachine vm = new VMBuilder().build();
        PortRule pr = new PortRule.Builder("Rule1",65536,22).build();
        INATEngine natenMocked = mock(INATEngine.class);
        
        exception.expect(IllegalArgumentException.class);
        sut.addPortRule(vm, pr);
        
        exception = ExpectedException.none();
        verify(natenMocked, never()).addRedirect(anyString(), any(NATProtocol.class), anyString(),
                                                 anyInt(), anyString(), anyInt());
    }
    
    @Test
    public void addPortRuleWithNegativeGuestPortNumber(){
        VirtualMachine vm = new VMBuilder().build();
        PortRule pr = new PortRule.Builder("Rule1",2222,-1).build();
        INATEngine natenMocked = mock(INATEngine.class);
        
        exception.expect(IllegalArgumentException.class);
        sut.addPortRule(vm, pr);
        
        exception = ExpectedException.none();
        verify(natenMocked, never()).addRedirect(anyString(), any(NATProtocol.class), anyString(),
                                                 anyInt(), anyString(), anyInt());
    }
    
    @Test
    public void addPortRuleWithTooBigGuestPortNumber(){
        VirtualMachine vm = new VMBuilder().build();
        PortRule pr = new PortRule.Builder("Rule1",2222,65536).build();
        INATEngine natenMocked = mock(INATEngine.class);
        
        exception.expect(IllegalArgumentException.class);
        sut.addPortRule(vm, pr);
        
        exception = ExpectedException.none();
        verify(natenMocked, never()).addRedirect(anyString(), any(NATProtocol.class), anyString(),
                                                 anyInt(), anyString(), anyInt());
    }
    
    @Test
    public void addPortRuleWithNullPortRule(){
        VirtualMachine vm = new VMBuilder().build();
        INATEngine natenMocked = mock(INATEngine.class);
        
        exception.expect(IllegalArgumentException.class);
        sut.addPortRule(vm, null);
        
        exception = ExpectedException.none();
        verify(vbmMocked, never()).connect(anyString(), anyString(), anyString());
        verify(natenMocked, never()).addRedirect(anyString(), any(NATProtocol.class), anyString(),
                                                 anyInt(), anyString(), anyInt());
    }
    
    @Test
    public void addPortRuleWithNullVM(){
        PortRule pr = new PortRule.Builder("Rule1",2222,65536).build();
        INATEngine natenMocked = mock(INATEngine.class);
        
        exception.expect(IllegalArgumentException.class);
        sut.addPortRule(null, pr);
        
        exception = ExpectedException.none();
        verify(vbmMocked, never()).connect(anyString(), anyString(), anyString());
        verify(natenMocked, never()).addRedirect(anyString(), any(NATProtocol.class), anyString(),
                                                 anyInt(), anyString(), anyInt());
    }
    
    @Test
    public void addPortRuleWithBothNullArguments(){
        INATEngine natenMocked = mock(INATEngine.class);
        
        exception.expect(IllegalArgumentException.class);
        sut.addPortRule(null, null);
        
        exception = ExpectedException.none();
        verify(vbmMocked, never()).connect(anyString(), anyString(), anyString());
        verify(natenMocked, never()).addRedirect(anyString(), any(NATProtocol.class), anyString(),
                                                 anyInt(), anyString(), anyInt());
    }
    
    @Test
    public void addPortRuleWithInvalidConection(){
        VirtualMachine vm = new VMBuilder().build();
        PortRule pr = new PortRule.Builder("Rule1",2222,22).build();
        String url = "http://" + vm.getHostMachine().getAddressIP() + ":"
                        + vm.getHostMachine().getPortOfVTWebServer();
        String user = vm.getHostMachine().getUsername();
        String pswd = vm.getHostMachine().getUserPassword();
        INATEngine natenMocked = mock(INATEngine.class);
        
        exception.expect(ConnectionFailureException.class);
        sut.addPortRule(vm, pr);
        
        exception = ExpectedException.none();
        verify(vbmMocked, times(3)).connect(url, user, pswd);
        verify(vbmMocked, never()).getVBox();
        verify(natenMocked, never()).addRedirect(pr.getName(), NATProtocol.TCP, pr.getHostIP(),
                                        pr.getHostPort(), pr.getGuestIP(), pr.getGuestPort());
    }
    
    @Test
    public void deletePortRuleWithValidValues(){
        VirtualMachine vm = new VMBuilder().build();
        String ruleName = "Rule1";
        INATEngine natenMocked = mock(INATEngine.class);
        
        sut.deletePortRule(vm, ruleName);
        
        verify(natenMocked).removeRedirect(ruleName);
    }
    
    @Test
    public void deletePortRuleWithEmptyRuleName(){
        VirtualMachine vm = new VMBuilder().build();
        String ruleName = "";
        INATEngine natenMocked = mock(INATEngine.class);
        
        exception.expect(IllegalArgumentException.class);
        sut.deletePortRule(vm, ruleName);
        
        exception = ExpectedException.none();
        verify(vbmMocked, never()).connect(anyString(), anyString(), anyString());
        verify(natenMocked, never()).removeRedirect(ruleName);
    }
    
    @Test
    public void deletePortRuleWithNullRuleName(){
        VirtualMachine vm = new VMBuilder().build();
        String ruleName = null;
        INATEngine natenMocked = mock(INATEngine.class);
        
        exception.expect(IllegalArgumentException.class);
        sut.deletePortRule(vm, ruleName);
        
        exception = ExpectedException.none();
        verify(vbmMocked, never()).connect(anyString(), anyString(), anyString());
        verify(natenMocked, never()).removeRedirect(ruleName);
    }
    
    @Test
    public void deletePortRuleWithNotExistingPortRuleName(){
        VirtualMachine vm = new VMBuilder().build();
        String ruleName = "SSHRule";
        INATEngine natenMocked = mock(INATEngine.class);
        doThrow(VBoxException.class).when(natenMocked).removeRedirect(ruleName);
        
        exception.expect(UnknownPortRuleException.class);
        sut.deletePortRule(vm, ruleName);
    }
    
    @Test
    public void deletePortRuleWithNotExistingVM(){
        VirtualMachine vm = new VMBuilder().build();
        String ruleName = "Rule1";
        INATEngine natenMocked = mock(INATEngine.class);
        doThrow(VBoxException.class).when(vboxMocked).findMachine(vm.getId().toString());
        
        exception.expect(UnknownVirtualMachineException.class);
        sut.deletePortRule(vm, ruleName);
        
        exception = ExpectedException.none();
        verify(vbmMocked, never()).getSessionObject();
        verify(natenMocked, never()).removeRedirect(anyString());
    }
    
    @Test
    public void deletePortRuleWithNullVM(){
        String ruleName = "Rule1";
        INATEngine natenMocked = mock(INATEngine.class);
        
        exception.expect(IllegalArgumentException.class);
        sut.deletePortRule(null, ruleName);
        
        exception = ExpectedException.none();
        verify(vbmMocked, never()).connect(anyString(), anyString(), anyString());
        verify(natenMocked, never()).removeRedirect(anyString());
    }
    
    @Test
    public void deletePortRuleWithInvalidConnection(){
        VirtualMachine vm = new VMBuilder().build();
        String ruleName = "Rule1";
        String url = "http://" + vm.getHostMachine().getAddressIP() + ":"
                        + vm.getHostMachine().getPortOfVTWebServer();
        String user = vm.getHostMachine().getUsername();
        String pswd = vm.getHostMachine().getUserPassword();
        INATEngine natenMocked = mock(INATEngine.class);
        
        exception.expect(ConnectionFailureException.class);
        sut.deletePortRule(vm, ruleName);
        
        exception = ExpectedException.none();
        verify(vbmMocked, times(3)).connect(url, user, pswd);
        verify(vbmMocked, never()).getVBox();
        verify(natenMocked, never()).removeRedirect(anyString());
    }
    
    @Test
    public void getPortRulesWithSomeExistingRules(){
        VirtualMachine vm = new VMBuilder().build();
        List<String> redirects = Arrays.asList("Rule1,1,,2222,,22","Rule2,0,,15890,,3698");
        PortRule expPR1 = new PortRule.Builder("Rule1", 2222, 22).build();
        PortRule expPR2 = new PortRule.Builder("Rule2", 15890, 3698).protocol(ProtocolType.UDP).build();
        INATEngine natenMocked = mock(INATEngine.class);
        doReturn(redirects).when(natenMocked).getRedirects();        
        
        List<PortRule> expPortRules = Arrays.asList(expPR1,expPR2);
        List<PortRule> actPortRules = sut.getPortRules(vm);
        
        Collections.sort(expPortRules);
        Collections.sort(actPortRules);
        
        assertFalse("List of actual port rules should not be empty",actPortRules.isEmpty());
        assertDeepPREquals(expPortRules,actPortRules);
        
    }
    
    @Test
    public void getPortRulesWithNoExistingRules(){
        VirtualMachine vm = new VMBuilder().build();
        List<String> redirects = new ArrayList<>();
        INATEngine natenMocked = mock(INATEngine.class);
        doReturn(redirects).when(natenMocked).getRedirects(); 
        
        List<PortRule> actPortRules = sut.getPortRules(vm);
        
        assertTrue("List of actual port rules should be empty",actPortRules.isEmpty());
    }
    
    @Test
    public void getPortRulesWithInvalidConnection(){
        VirtualMachine vm = new VMBuilder().build();
        String ruleName = "Rule1";
        String url = "http://" + vm.getHostMachine().getAddressIP() + ":"
                        + vm.getHostMachine().getPortOfVTWebServer();
        String user = vm.getHostMachine().getUsername();
        String pswd = vm.getHostMachine().getUserPassword();
        INATEngine natenMocked = mock(INATEngine.class);
        
        exception.expect(ConnectionFailureException.class);
        List<PortRule> actPortRules = sut.getPortRules(vm);
        
        exception = ExpectedException.none();
        verify(vbmMocked, times(3)).connect(url, user, pswd);
        verify(vbmMocked, never()).getVBox();
        verify(natenMocked, never()).getRedirects();
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

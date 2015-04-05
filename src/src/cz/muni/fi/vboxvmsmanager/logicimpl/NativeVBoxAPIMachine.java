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
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.IncompatibleVirtToolAPIVersionException;
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.PortRuleDuplicityException;
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.UnexpectedVMStateException;
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.UnknownPortRuleException;
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.UnknownVirtualMachineException;
import cz.muni.fi.vboxvmsmanager.pubapi.types.ProtocolType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.virtualbox_4_3.IConsole;
import org.virtualbox_4_3.IMachine;
import org.virtualbox_4_3.INATEngine;
import org.virtualbox_4_3.INetworkAdapter;
import org.virtualbox_4_3.IProgress;
import org.virtualbox_4_3.ISession;
import org.virtualbox_4_3.IVirtualBox;
import org.virtualbox_4_3.LockType;
import org.virtualbox_4_3.MachineState;
import org.virtualbox_4_3.NATProtocol;
import org.virtualbox_4_3.SessionState;
import org.virtualbox_4_3.VBoxException;
import org.virtualbox_4_3.VirtualBoxManager;

/**
 *
 * @author Tomáš Šmíd
 */
class NativeVBoxAPIMachine {
    
    private static final NativeVBoxAPIMachine INSTANCE = new NativeVBoxAPIMachine();
    
    public static NativeVBoxAPIMachine getInstance(){
        return INSTANCE;
    }
    
    private NativeVBoxAPIMachine(){
        
    }
    
    public void startVM(VirtualMachine virtualMachine) throws InterruptedException, ConnectionFailureException,
            IncompatibleVirtToolAPIVersionException, UnknownVirtualMachineException, UnexpectedVMStateException{
        
        String errMsgForVMNullCheck = "Starting virtual machine failure: There was made an attempt to start a null virtual machine.";
        String errMsgForPMNullCheck = "Starting virtual machine failure: There was made an attempt to start virtual machine " + virtualMachine + " on a null physical machine.";
        String errMsgForVMIdCheck = "Starting virtual machine failure: Virtual machine " + virtualMachine + " has a null or an empty id.";
        String errMsgForVMNameCheck = "Starting virtual machine failure: Virtual machine " + virtualMachine + " has a null or an empty name.";
        String errMsgForNotConnectedPM = "Connection failure while trying to start virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + ": There cannot be started any virtual machine on this physical machine now, because it is not connected.";
        String errMsgForPMConError = "Connection failure while trying to start virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + ": ";
        String errMsgForUnknownVM = "Starting virtual machine failure: There is no virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + " known to VirtualBox.";
        String errMsgForVMAccessCheck = "Starting virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + " failure: ";
        String errMsgForVMStateCheck = "Starting virtual machine failure: Virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + " cannot be started, because virtual machine is already running.";
        String errMsgForUnusableVM = "Starting virtual machine failure: Virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + " cannot be started now. There is another process that has locked this virtual machine for itself earlier or this virtual machine is already running.";
        NativeVBoxAPIConnection natapiCon = NativeVBoxAPIConnection.getInstance();
        
        checkVMIsNotNull(virtualMachine, errMsgForVMNullCheck);
        checkPMIsNotNull(virtualMachine.getHostMachine(), errMsgForPMNullCheck);
        checkVMIdIsNotNullNorEmpty(virtualMachine.getId(), errMsgForVMIdCheck);
        checkVMNameIsNotNullNorEmpty(virtualMachine.getName(), errMsgForVMNameCheck);
        checkPMIsConnected(virtualMachine.getHostMachine(), errMsgForNotConnectedPM);
        
        VirtualBoxManager vbm = natapiCon.getVirtualBoxManager(virtualMachine.getHostMachine(), errMsgForPMConError);
        IVirtualBox vbox = vbm.getVBox();
        IMachine vboxMachine = null;
        try{
            vboxMachine = vbox.findMachine(virtualMachine.getId().toString());
        }catch(VBoxException ex){
            vbm.disconnect();
            vbm.cleanup();
            throw new UnknownVirtualMachineException(errMsgForUnknownVM);
        }
        
        if(!vboxMachine.getAccessible()){
            vbm.disconnect();
            vbm.cleanup();
            throw new UnexpectedVMStateException(errMsgForVMAccessCheck + vboxMachine.getAccessError().getText());
        }
        
        try{
            checkVMStateIsValidForStart(vboxMachine.getState(), errMsgForVMStateCheck);
        }catch(UnexpectedVMStateException ex){
            vbm.disconnect();
            vbm.cleanup();
            throw ex;
        }        
        
        ISession session = vbm.getSessionObject();
        try{
            IProgress progress = vboxMachine.launchVMProcess(session, "gui", "");
            while(!progress.getCompleted()){
                vbm.waitForEvents(0l);
                progress.waitForCompletion(200);
            }
            while(vboxMachine.getState() != MachineState.Running){
                //just loop until that is true
            }
        }catch(VBoxException ex){
            vbm.disconnect();
            vbm.cleanup();
            throw new UnexpectedVMStateException(errMsgForUnusableVM);
        }
        
        session.unlockMachine();
        vbm.disconnect();
        vbm.cleanup();
    }
    
    public void shutDownVM(VirtualMachine virtualMachine) throws ConnectionFailureException, InterruptedException,
            IncompatibleVirtToolAPIVersionException, UnknownVirtualMachineException, UnexpectedVMStateException{
        
        String errMsgForVMNullCheck = "Shutdown virtual machine failure: There was made an attempt to shut down a null virtual machine.";
        String errMsgForPMNullCheck = "Shutdown virtual machine failure: There was made an attempt to shut down virtual machine " + virtualMachine + " on a null physical machine.";
        String errMsgForVMIdCheck = "Shutdown virtual machine failure: Virtual machine " + virtualMachine + " has a null or an empty id.";
        String errMsgForVMNameCheck = "Shutdown virtual machine failure: Virtual machine " + virtualMachine + " has a null or an empty name.";
        String errMsgForNotConnectedPM = "Connection failure while trying to shut down virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + ": There cannot be shut down any virtual machine on this physical machine now, because it is not connected.";
        String errMsgForPMConError = "Connection failure while trying to shut down virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + ": ";
        String errMsgForUnknownVM = "Shutdown virtual machine failure: There is no virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + " known to VirtualBox.";
        String errMsgForVMAccessCheck = "Shutdown virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + " failure: ";
        String errMsgForVMStateCheck = "Shutdown virtual machine failure: Virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + " cannot be shut down, because virtual machine is already powered off.";        
        NativeVBoxAPIConnection natapiCon = NativeVBoxAPIConnection.getInstance();
        
        checkVMIsNotNull(virtualMachine, errMsgForVMNullCheck);
        checkPMIsNotNull(virtualMachine.getHostMachine(), errMsgForPMNullCheck);
        checkVMIdIsNotNullNorEmpty(virtualMachine.getId(), errMsgForVMIdCheck);
        checkVMNameIsNotNullNorEmpty(virtualMachine.getName(), errMsgForVMNameCheck);
        checkPMIsConnected(virtualMachine.getHostMachine(), errMsgForNotConnectedPM);
        
        VirtualBoxManager vbm = natapiCon.getVirtualBoxManager(virtualMachine.getHostMachine(), errMsgForPMConError);
        IVirtualBox vbox = vbm.getVBox();
        IMachine vboxMachine = null;
        try{
            vboxMachine = vbox.findMachine(virtualMachine.getId().toString());
        }catch(VBoxException ex){
            vbm.disconnect();
            vbm.cleanup();
            throw new UnknownVirtualMachineException(errMsgForUnknownVM);
        }
        
        if(!vboxMachine.getAccessible()){
            vbm.disconnect();
            vbm.cleanup();
            throw new UnexpectedVMStateException(errMsgForVMAccessCheck + vboxMachine.getAccessError().getText());
        }
        
        try{
            checkVMStateIsValidForShutdown(vboxMachine.getState(), errMsgForUnknownVM);
        }catch(UnexpectedVMStateException ex){
            vbm.disconnect();
            vbm.cleanup();
            throw ex;
        }
        
        ISession session = vbm.getSessionObject();
        vboxMachine.lockMachine(session, LockType.Shared);
        IConsole console = session.getConsole();
        IProgress progress = console.powerDown();
        while(!progress.getCompleted()){
            vbm.waitForEvents(0l);
            progress.waitForCompletion(200);
        }
        session.unlockMachine();
        while(vboxMachine.getState() != MachineState.PoweredOff){
            
        }
        while(session.getState() != SessionState.Unlocked){
            
        }
        
        vbm.disconnect();
        vbm.cleanup();
    }
    
    public void addPortRule(VirtualMachine virtualMachine, PortRule portRule) throws ConnectionFailureException,
            InterruptedException, IncompatibleVirtToolAPIVersionException, UnknownVirtualMachineException,
            IllegalArgumentException, PortRuleDuplicityException, UnexpectedVMStateException{     
        
        String errMsgForVMNullCheck = "Creating new port forwarding rule failure: There was made an attempt to create a new port forwarding rule for a null virtual machine.";
        String errMsgForPMNullCheck = "Creating a new port forwarding rule failure: There was made an attempt to create a new port forwarding rule for virtual machine " + virtualMachine + " on a null physical machine.";
        String errMsgForVMIdCheck = "Creating new port forwarding rule failure: Virtual machine " + virtualMachine + " has a null or an empty id.";
        String errMsgForVMNameCheck = "Creating new port forwarding rule failure: Virtual machine " + virtualMachine + " has a null or an empty name.";
        String errMsgForNotConnectedPM = "Connection failure while trying to create a new port forwarding rule for virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + ": There cannot be created any new port forwarding rule for this virtual machine on this physical machine now, because it is not connected.";
        String errMsgForPMConError = "Connection failure while trying to create new port forwarding rule for virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + ": ";
        String errMsgForUnknownVM = "Craeting new port forwarding rule failure: There is no virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + " known to VirtualBox.";
        NativeVBoxAPIConnection natapiCon = NativeVBoxAPIConnection.getInstance();
        
        checkVMIsNotNull(virtualMachine, errMsgForVMNullCheck);
        checkPMIsNotNull(virtualMachine.getHostMachine(), errMsgForPMNullCheck);
        checkVMIdIsNotNullNorEmpty(virtualMachine.getId(), errMsgForVMIdCheck);
        checkVMNameIsNotNullNorEmpty(virtualMachine.getName(), errMsgForVMNameCheck);
        checkPMIsConnected(virtualMachine.getHostMachine(), errMsgForNotConnectedPM);
        
        VirtualBoxManager vbm = natapiCon.getVirtualBoxManager(virtualMachine.getHostMachine(), errMsgForPMConError);
        IVirtualBox vbox = vbm.getVBox();
        IMachine vboxMachine = null;
        try{
            vboxMachine = vbox.findMachine(virtualMachine.getId().toString());
        }catch(VBoxException ex){
            vbm.disconnect();
            vbm.cleanup();
            throw new UnknownVirtualMachineException(errMsgForUnknownVM);
        }
        
        try{
            checkPortRuleValidity(virtualMachine, portRule, vboxMachine);
        }catch(IllegalArgumentException | PortRuleDuplicityException ex){
            vbm.disconnect();
            vbm.cleanup();
            throw ex;
        }
        
        INetworkAdapter adapter = vboxMachine.getNetworkAdapter(0L);
        INATEngine natEngine = adapter.getNATEngine();
        NATProtocol natp = (portRule.getProtocol() == ProtocolType.TCP ? NATProtocol.TCP : NATProtocol.UDP);
        String hostIP = (portRule.getHostIP() == null ? "" : portRule.getHostIP());
        String guestIP = (portRule.getGuestIP() == null ? "" : portRule.getGuestIP());
        
        natEngine.addRedirect(portRule.getName(), natp, hostIP, portRule.getHostPort(),
                              guestIP, portRule.getGuestPort());
        
        vbm.disconnect();
        vbm.cleanup();        
        
    }
    
    public void deletePortRule(VirtualMachine virtualMachine, String ruleName) throws ConnectionFailureException,
            InterruptedException, IncompatibleVirtToolAPIVersionException, UnknownVirtualMachineException,
            UnknownPortRuleException, UnexpectedVMStateException{
        
        String errMsgForVMNullCheck = "Deleting port forwarding rule failure: There was made an attempt to delete a port forwarding rule of a null virtual machine.";
        String errMsgForPMNullCheck = "Deleting port forwarding rule failure: There was made an attempt to delete a port forwarding rule of virtual machine " + virtualMachine + " on a null physical machine.";
        String errMsgForVMIdCheck = "Deleting new port forwarding rule failure: Virtual machine " + virtualMachine + " has a null or an empty id.";
        String errMsgForVMNameCheck = "Deleting new port forwarding rule failure: Virtual machine " + virtualMachine + " has a null or an empty name.";
        String errMsgForNotConnectedPM = "Connection failure while trying to delete port forwarding rule of virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + ": There cannot be deleted any port forwarding rule of this virtual machine on this physical machine now, because this physical machine is not connected.";
        String errMsgForPMConError = "Connection failure while trying to delete port forwarding rule of virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + ": ";
        String errMsgForUnknownVM = "Deleting port forwarding rule failure: There is no virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + " known to VirtualBox.";
        String errMsgForPRNameCheck = "Deleting port forwarding rule failure: Port forwarding rule to delete has null or empty name.";
        String errMsgForPRNameDeleting = "Deleting port forwarding rule failure: There is no port rule with name = " + ruleName + " of virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + " which could be deleted.";
        NativeVBoxAPIConnection natapiCon = NativeVBoxAPIConnection.getInstance();
        
        checkVMIsNotNull(virtualMachine, errMsgForVMNullCheck);
        checkPMIsNotNull(virtualMachine.getHostMachine(), errMsgForPMNullCheck);
        checkVMIdIsNotNullNorEmpty(virtualMachine.getId(), errMsgForVMIdCheck);
        checkVMNameIsNotNullNorEmpty(virtualMachine.getName(), errMsgForVMNameCheck);
        checkPMIsConnected(virtualMachine.getHostMachine(), errMsgForNotConnectedPM);
        checkPortRuleNameIsNotNullNorEmpty(ruleName, errMsgForPRNameCheck);
        
        VirtualBoxManager vbm = natapiCon.getVirtualBoxManager(virtualMachine.getHostMachine(), errMsgForPMConError);
        IVirtualBox vbox = vbm.getVBox();
        IMachine vboxMachine = null;
        try{
            vboxMachine = vbox.findMachine(virtualMachine.getId().toString());
        }catch(VBoxException ex){
            vbm.disconnect();
            vbm.cleanup();
            throw new UnknownVirtualMachineException(errMsgForUnknownVM);
        }
        
        INetworkAdapter adapter = vboxMachine.getNetworkAdapter(0L);
        INATEngine natEngine = adapter.getNATEngine();
        
        try{
            natEngine.removeRedirect(ruleName);
        }catch(VBoxException ex){
            vbm.disconnect();
            vbm.cleanup();
            throw new UnknownPortRuleException(errMsgForPRNameDeleting);
        }
        
        vbm.disconnect();
        vbm.cleanup();
    }
    
    public List<PortRule> getPortRules(VirtualMachine virtualMachine) throws ConnectionFailureException,
            InterruptedException, IncompatibleVirtToolAPIVersionException, UnknownVirtualMachineException,
            UnexpectedVMStateException{
        
        String errMsgForVMNullCheck = "Retrieving all port forwarding rules failure: There was made an attempt to retrieve all port forwarding rules of a null virtual machine.";
        String errMsgForPMNullCheck = "Retrieving all port forwarding rules failure: There was made an attempt to retrieve all port forwarding rules of virtual machine " + virtualMachine + " on a null physical machine.";
        String errMsgForVMIdCheck = "Retrieving all port forwarding rules failure: Virtual machine " + virtualMachine + " has a null or an empty id.";
        String errMsgForVMNameCheck = "Retrieving all port forwarding rules failure: Virtual machine " + virtualMachine + " has a null or an empty name.";
        String errMsgForNotConnectedPM = "Connection failure while trying to retrieve all port forwarding rules of virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + ": There cannot be retrieved any port forwarding rule of this virtual machine on this physical machine now, because this physical machine is not connected.";
        String errMsgForPMConError = "Connection failure while trying to retrieve all port forwarding rules of virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + ": ";
        String errMsgForUnknownVM = "Retrieving all port forwarding rules failure: There is no virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + " known to VirtualBox.";
        NativeVBoxAPIConnection natapiCon = NativeVBoxAPIConnection.getInstance();
        
        checkVMIsNotNull(virtualMachine, errMsgForVMNullCheck);
        checkPMIsNotNull(virtualMachine.getHostMachine(), errMsgForPMNullCheck);
        checkVMIdIsNotNullNorEmpty(virtualMachine.getId(), errMsgForVMIdCheck);
        checkVMNameIsNotNullNorEmpty(virtualMachine.getName(), errMsgForVMNameCheck);
        checkPMIsConnected(virtualMachine.getHostMachine(), errMsgForNotConnectedPM);
        
        VirtualBoxManager vbm = natapiCon.getVirtualBoxManager(virtualMachine.getHostMachine(), errMsgForPMConError);
        IVirtualBox vbox = vbm.getVBox();
        IMachine vboxMachine = null;
        try{
            vboxMachine = vbox.findMachine(virtualMachine.getId().toString());
        }catch(VBoxException ex){
            vbm.disconnect();
            vbm.cleanup();
            throw new UnknownVirtualMachineException(errMsgForUnknownVM);
        }
        
        INetworkAdapter adapter = vboxMachine.getNetworkAdapter(0L);
        INATEngine natEngine = adapter.getNATEngine();
        List<String> redirects = natEngine.getRedirects();
        List<PortRule> portRules = new ArrayList<>();
        
        for(String redirect : redirects){
            portRules.add(redirectToPortRule(redirect));
        }
        
        vbm.disconnect();
        vbm.cleanup();
        
        return portRules;
    }
    
    public String getVMState(VirtualMachine virtualMachine) throws ConnectionFailureException, InterruptedException,
            IncompatibleVirtToolAPIVersionException, UnknownVirtualMachineException, UnexpectedVMStateException{
        String errMsgForVMNullCheck = "Retrieving virtual machine state failure: There was made an attempt to retrieve state of a null virtual machine.";
        String errMsgForPMNullCheck = "Retrieving virtual machine state failure: There was made an attempt to retrieve state of virtual machine " + virtualMachine + " on a null physical machine.";
        String errMsgForVMIdCheck = "Retrieving virtual machine state failure: Virtual machine " + virtualMachine + " has a null or an empty id.";
        String errMsgForVMNameCheck = "Retrieving virtual machine state failure: Virtual machine " + virtualMachine + " has a null or an empty name.";
        String errMsgForNotConnectedPM = "Connection failure while trying to retrieve state of virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + ": There cannot be retrieved state of this virtual machine on this physical machine now, because this physical machine is not connected.";
        String errMsgForPMConError = "Connection failure while trying to retrieve state of virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + ": ";
        String errMsgForUnknownVM = "Retrieving virtual machine state failure: There is no virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + " known to VirtualBox.";
        String errMsgForVMAccessCheck = "Retrieving state of virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + " failure: ";
        NativeVBoxAPIConnection natapiCon = NativeVBoxAPIConnection.getInstance();
        
        checkVMIsNotNull(virtualMachine, errMsgForVMNullCheck);
        checkPMIsNotNull(virtualMachine.getHostMachine(), errMsgForPMNullCheck);
        checkVMIdIsNotNullNorEmpty(virtualMachine.getId(), errMsgForVMIdCheck);
        checkVMNameIsNotNullNorEmpty(virtualMachine.getName(), errMsgForVMNameCheck);
        checkPMIsConnected(virtualMachine.getHostMachine(), errMsgForNotConnectedPM);
        
        VirtualBoxManager vbm = natapiCon.getVirtualBoxManager(virtualMachine.getHostMachine(), errMsgForPMConError);
        IVirtualBox vbox = vbm.getVBox();
        IMachine vboxMachine = null;
        try{
            vboxMachine = vbox.findMachine(virtualMachine.getId().toString());
        }catch(VBoxException ex){
            vbm.disconnect();
            vbm.cleanup();
            throw new UnknownVirtualMachineException(errMsgForUnknownVM);
        }
        
        if(!vboxMachine.getAccessible()){
            vbm.disconnect();
            vbm.cleanup();
            throw new UnexpectedVMStateException(errMsgForVMAccessCheck + vboxMachine.getAccessError().getText());
        }
        
        String state = vboxMachine.getState().name();
        
        vbm.disconnect();
        vbm.cleanup();
        
        return state;
    }
    
    private void checkVMIsNotNull(VirtualMachine vm, String errMsg){
        if(vm == null){
            throw new IllegalArgumentException(errMsg);
        }
    }
    
    private void checkPMIsNotNull(PhysicalMachine pm, String errMsg){
        if(pm == null){
            throw new IllegalArgumentException(errMsg);
        }
    }
    
    private void checkVMIdIsNotNullNorEmpty(UUID id, String errMsg){
        if(id == null || id.toString().isEmpty()){
            throw new IllegalArgumentException(errMsg);
        }
    }
    
    private void checkVMNameIsNotNullNorEmpty(String name, String errMsg){
        if(name == null || name.isEmpty()){
            throw new IllegalArgumentException(errMsg);
        }
    }
    
    private void checkPMIsConnected(PhysicalMachine pm, String errMsg) throws UnexpectedVMStateException{
        NativeVBoxAPIConnection natapiCon = NativeVBoxAPIConnection.getInstance();
        
        if(!natapiCon.isConnected(pm)){
            throw new UnexpectedVMStateException(errMsg);
        }
    }
    
    private void checkVMStateIsValidForStart(MachineState state, String errMsg) throws UnexpectedVMStateException{
        switch(state){
            case Running:
            case Paused : throw new UnexpectedVMStateException(errMsg);
            default     : break;
        }
    }
    
    private void checkVMStateIsValidForShutdown(MachineState state, String errMsg) throws UnexpectedVMStateException{
        switch(state){
            case Running:
            case Paused :
            case Stuck  : break;
            default     : throw new UnexpectedVMStateException(errMsg);
        }
    }
    
    private void checkPortRuleValidity(VirtualMachine virtualMachine, PortRule portRule, IMachine vboxMachine) throws PortRuleDuplicityException{
        String errMsgForPRNullCheck = "Creating new port forwarding rule failure: There was made an attempt to create a null port forwarding rule for virtual machine " + virtualMachine + ".";
        String errMsgForPRNameCheck = "Creating new port forwarding rule failure: Name of port rule " + portRule + " is null or empty.";
        String errMsgForPRHostPortCheck = "Creating new port forwarding rule failure: Host port number of new port forwarding rule " + portRule + " is negative or too big. Host port number can be from the range 0-65535.";
        String errMsgForPRGuestPortCheck = "Creating new port forwarding rule failure: Guest port number of new port forwarding rule " + portRule + " is negative or too big. Guest port number can be from the range 0-65535.";
        String errMsgForPRNameDuplicityCheck = "Crating new port forwarding rule failure: There already exists port forwarding rule with name = " + portRule.getName() + " on virtual machine " + virtualMachine + ".";
        String errMsgForPRHPDuplicityCheck = "Creating new port forwarding rule failure: There already exists port forwarding rule using host port number = " + portRule.getHostPort() + " on virtual machine " + virtualMachine + ".";
        
        checkPortRuleIsNotNull(portRule,errMsgForPRNullCheck);
        checkPortRuleNameIsNotNullNorEmpty(portRule.getName(),errMsgForPRNameCheck);
        checkPortRuleHostOrGuestPortIsValid(portRule.getHostPort(),errMsgForPRHostPortCheck);
        checkPortRuleHostOrGuestPortIsValid(portRule.getGuestPort(),errMsgForPRGuestPortCheck);
        checkPortRuleNameDuplicity(vboxMachine, portRule.getName(),errMsgForPRNameDuplicityCheck);
        checkPortRuleHostPortDuplicity(vboxMachine, portRule.getHostPort(),errMsgForPRHPDuplicityCheck);
    }
    
    private void checkPortRuleIsNotNull(PortRule portRule, String errMsg){
        if(portRule == null){
            throw new IllegalArgumentException(errMsg);
        }
    }
    
    private void checkPortRuleNameIsNotNullNorEmpty(String name, String errMsg){
        if(name == null || name.isEmpty()){
            throw new IllegalArgumentException(errMsg);
        }
    }
    
    private void checkPortRuleHostOrGuestPortIsValid(int port, String errMsg){
        if(port < 0 || port > 65535){
            throw new IllegalArgumentException(errMsg);
        }
    }
    
    private void checkPortRuleNameDuplicity(IMachine vboxMachine, String name, String errMsg) throws PortRuleDuplicityException{
        INetworkAdapter adapter = vboxMachine.getNetworkAdapter(0L);
        INATEngine natEngine = adapter.getNATEngine();
        List<String> redirects = natEngine.getRedirects();
        
        for(String redirect : redirects){
            String[] parts = redirect.split(",");
            if(parts[0].equals(name)){
                throw new PortRuleDuplicityException(errMsg);
            }
        }
    }
    
    private void checkPortRuleHostPortDuplicity(IMachine vboxMachine, int port, String errMsg) throws PortRuleDuplicityException{
        INetworkAdapter adapter = vboxMachine.getNetworkAdapter(0L);
        INATEngine natEngine = adapter.getNATEngine();
        List<String> redirects = natEngine.getRedirects();
        
        for(String redirect : redirects){
            String[] parts = redirect.split(",");
            int redHostPort = Integer.parseInt(parts[3]);
            if(redHostPort == port){
                throw new PortRuleDuplicityException(errMsg);
            }
        }
    }
    
    private PortRule redirectToPortRule(String redirect){
        String parts[] = redirect.split(",");
        String name = parts[0];
        ProtocolType protocol = ProtocolType.valueOf(parts[1]);
        String hostIP = parts[2];
        int hostPort = Integer.parseInt(parts[3]);
        String guestIP = parts[4];
        int guestPort = Integer.parseInt(parts[5]);
        
        return new PortRule.Builder(name, hostPort, guestPort).protocol(protocol)
                           .hostIP(hostIP).guestIP(guestIP).build();
    }
}

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
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.UnexpectedVMStateException;
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.UnknownVirtualMachineException;
import java.util.List;
import java.util.UUID;
import org.virtualbox_4_3.IConsole;
import org.virtualbox_4_3.IMachine;
import org.virtualbox_4_3.IProgress;
import org.virtualbox_4_3.ISession;
import org.virtualbox_4_3.IVirtualBox;
import org.virtualbox_4_3.LockType;
import org.virtualbox_4_3.MachineState;
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
        checkVMNameIsNotNullNorEmpty(virtualMachine.getVMName(), errMsgForVMNameCheck);
        checkPMIsConnected(virtualMachine.getHostMachine(), errMsgForNotConnectedPM);
        
        VirtualBoxManager vbm = natapiCon.getVirtualBoxManager(virtualMachine.getHostMachine(), errMsgForPMConError);
        IVirtualBox vbox = vbm.getVBox();
        IMachine vboxMachine = null;
        try{
            vboxMachine = vbox.findMachine(virtualMachine.getId().toString());
        }catch(VBoxException ex){
            vbm.disconnect();
            throw new UnknownVirtualMachineException(errMsgForUnknownVM);
        }
        
        if(!vboxMachine.getAccessible()){
            vbm.disconnect();
            throw new UnexpectedVMStateException(errMsgForVMAccessCheck + vboxMachine.getAccessError().getText());
        }
        
        try{
            checkVMStateIsValidForStart(vboxMachine.getState(), errMsgForUnknownVM);
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
        checkVMNameIsNotNullNorEmpty(virtualMachine.getVMName(), errMsgForVMNameCheck);
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
    
    public void addPortRule(VirtualMachine virtualMachine, PortRule portRule){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void deletePortRule(VirtualMachine virtualMachine, String ruleName){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public List<PortRule> getPortRules(VirtualMachine virtualMachine){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getVMState(VirtualMachine virtualMachine){
        throw new UnsupportedOperationException("Not supported yet.");
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
    
    private void checkPMIsConnected(PhysicalMachine pm, String errMsg){
        NativeVBoxAPIConnection natapiCon = NativeVBoxAPIConnection.getInstance();
        
        if(!natapiCon.isConnected(pm)){
            throw new IllegalStateException(errMsg);
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
}

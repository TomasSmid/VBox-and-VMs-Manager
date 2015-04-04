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
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.UnexpectedVMStateException;
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.UnknownVirtualMachineException;
import cz.muni.fi.vboxvmsmanager.pubapi.types.CloneType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.virtualbox_4_3.CleanupMode;
import org.virtualbox_4_3.CloneMode;
import org.virtualbox_4_3.CloneOptions;
import org.virtualbox_4_3.IConsole;
import org.virtualbox_4_3.IGuestOSType;
import org.virtualbox_4_3.IMachine;
import org.virtualbox_4_3.IMedium;
import org.virtualbox_4_3.IProgress;
import org.virtualbox_4_3.ISession;
import org.virtualbox_4_3.ISnapshot;
import org.virtualbox_4_3.IVirtualBox;
import org.virtualbox_4_3.LockType;
import org.virtualbox_4_3.VBoxException;
import org.virtualbox_4_3.VirtualBoxManager;
import org.virtualbox_4_3.SessionState;

/**
 *
 * @author Tomáš Šmíd
 */
final class NativeVBoxAPIManager {
    
    private static final NativeVBoxAPIManager INSTANCE = new NativeVBoxAPIManager();
    
    public static NativeVBoxAPIManager getInstance(){
        return INSTANCE;
    }
    
    public VirtualMachine getVirtualMachineById(PhysicalMachine physicalMachine, UUID id) throws InterruptedException,
            ConnectionFailureException, IncompatibleVirtToolAPIVersionException, UnknownVirtualMachineException{
        
        String errMsgForPMNullCheck = "Retrieving virtual machine by id failure: There was made an attempt to retrieve virtual machine with id = " + id + " from a null physical machine.";
        String errMsgForVMIdCheck = "Retrieving virtual machine by id failure: There was made an attempt to retrieve virtual machine by a null id.";
        String errMsgForNotConnectedPM = "Connection failure while trying to retrieve virtual machine by id: There cannot be retrieved any virtual machine from physical machine " + physicalMachine + " now, because this physical machine is not connected.";
        String errMsgForPMConError = "Connection failure while trying to retrieve virtual machine by id from physical machine " + physicalMachine + ": ";
        String errMsgForUnknownVM = "Retrieving virtual machine by id failure: There is no virtual machine with id = " + id + " on physical machine " + physicalMachine + " known to VirtualBox.";
        String[] errMsgs = {(errMsgForPMConError),(errMsgForUnknownVM)};
        
        checkPMIsNotNull(physicalMachine, errMsgForPMNullCheck);
        checkVMIdIsNotNullNorEmpty(id, errMsgForVMIdCheck);
        checkPMIsConnected(physicalMachine, errMsgForNotConnectedPM);
        
        return getVM(physicalMachine, id.toString(), errMsgs);
    }
    
    public VirtualMachine getVirtualMachineByName(PhysicalMachine physicalMachine, String name) throws InterruptedException,
            ConnectionFailureException, IncompatibleVirtToolAPIVersionException, UnknownVirtualMachineException{
        
        String errMsgForPMNullCheck = "Retrieving virtual machine by name failure: There was made an attempt to retrieve virtual machine with name = " + name + " from a null physical machine.";
        String errMsgForVMNameCheck = "Retrieving virtual machine by name failure: There was made an attempt to retrieve virtual machine by a null or empty name.";
        String errMsgForNotConnectedPM = "Connection failure while trying to retrieve virtual machine by name: There cannot be retrieved any virtual machine from physical machine " + physicalMachine + " now, because this physical machine is not connected.";
        String errMsgForPMConError = "Connection failure while trying to retrieve virtual machine by name from physical machine " + physicalMachine + ": ";
        String errMsgForUnknownVM = "Retrieving virtual machine by name failure: There is no virtual machine with name = " + name + " on physical machine " + physicalMachine + " known to VirtualBox.";
        String[] errMsgs = {(errMsgForPMConError),(errMsgForUnknownVM)};
        
        checkPMIsNotNull(physicalMachine, errMsgForPMNullCheck);
        checkVMNameIsNotNullNorEmpty(name, errMsgForVMNameCheck);
        checkPMIsConnected(physicalMachine, errMsgForNotConnectedPM);
        
        return getVM(physicalMachine, name, errMsgs);
    }
    
    public List<VirtualMachine> getVirtualMachines(PhysicalMachine physicalMachine) throws InterruptedException,
            ConnectionFailureException, IncompatibleVirtToolAPIVersionException{
        
        String errMsgForPMNullCheck = "Retrieving all virtual machines failure: There was made an attempt to retrieve all virtual machines from a null physical machine.";
        String errMsgForNotConnectedPM = "Connection failure while trying to retrieve all virtual machines from physical machine " + physicalMachine + ": There cannot be retrieved any virtual machine from this physical machine now, because it is not connected.";
        String errMsgForPMConError = "Connection failure while trying to retrieve all virtual machines from physical machine " + physicalMachine + ": ";
        NativeVBoxAPIConnection natapiCon = NativeVBoxAPIConnection.getInstance();
        List<VirtualMachine> vms = new ArrayList<>();
        
        checkPMIsNotNull(physicalMachine, errMsgForPMNullCheck);
        checkPMIsConnected(physicalMachine, errMsgForNotConnectedPM);
        
        VirtualBoxManager vbm = natapiCon.getVirtualBoxManager(physicalMachine, errMsgForPMConError);
        IVirtualBox vbox = vbm.getVBox();
        try{
            List<IMachine> vboxMachines = vbox.getMachines();
            if(!vboxMachines.isEmpty()){
                for(IMachine vboxMachine : vboxMachines){
                    IGuestOSType gost = vbox.getGuestOSType(vboxMachine.getOSTypeId());
                    VirtualMachine vm = createVirtualMachine(vboxMachine,gost,physicalMachine);
                    vms.add(vm);
                }
            }
        }catch(VBoxException ex){
            System.err.println(ex.getMessage());
        }finally{
            vbm.disconnect();
            vbm.cleanup();
        }
        
        return vms;
    }
    
    public void removeVirtualMachine(VirtualMachine virtualMachine) throws InterruptedException, ConnectionFailureException,
            IncompatibleVirtToolAPIVersionException, UnknownVirtualMachineException, UnexpectedVMStateException{
        
        String errMsgForVMNullCheck = "Removing virtual machine failure: There was made an attempt to remove a null virtual machine.";
        String errMsgForPMNullCheck = "Removing virtual machine failure: There was made an attempt to remove virtual machine " + virtualMachine + " from a null physical machine.";
        String errMsgForNotConnectedPM = "Connection failure while trying to remove virtual machine " + virtualMachine + " from physical machine " + virtualMachine.getHostMachine() + ": There cannot be removed any virtual machine from this physical machine now, because it is not connected.";
        String errMsgForPMConError = "Connection failure while trying to remove virtual machine " + virtualMachine + " from physical machine " + virtualMachine.getHostMachine() + ": ";
        String errMsgForUnknownVM = "Removing virtual machine failure: There is no virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + " known to VirtualBox. Most probably has been this virtual machine removed recently."; 
        String errMsgForVMStateCheck = "Removing virtual machine failure: Virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + " cannot be removed, because it is not powered off.";
        NativeVBoxAPIConnection natapiCon = NativeVBoxAPIConnection.getInstance();
        
        checkVMIsNotNull(virtualMachine, errMsgForVMNullCheck);
        checkPMIsNotNull(virtualMachine.getHostMachine(), errMsgForPMNullCheck);
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
            try{
                vboxMachine.unregister(CleanupMode.DetachAllReturnHardDisksOnly);
            }catch(VBoxException ex){ /*machine was not registered*/ }
        }else{
            checkVMStateForRemoving(vboxMachine.getState().toString(), errMsgForVMStateCheck);

            if(isLinkedClone(vboxMachine, vbox)){
                ISession session = vbm.getSessionObject();
                removeVMAsSnapshot(vboxMachine, vbox, session);
            }else{
                removeVMAsStandaloneUnit(vboxMachine, vbox);
            }
        }
        
        vbm.disconnect();
        vbm.cleanup();
    }
    
    public VirtualMachine createVMClone(VirtualMachine virtualMachine, CloneType cloneType) throws InterruptedException,
            ConnectionFailureException, IncompatibleVirtToolAPIVersionException, UnknownVirtualMachineException, UnexpectedVMStateException{
        
        String errMsgForVMNullCheck = "Cloning virtual machine failure: There was made an attempt to clone a null virtual machine.";
        String errMsgForPMNullCheck = "Cloning virtual machine failure: There was made an attempt to clone virtual machine " + virtualMachine + " on a null physical machine.";
        String errMsgForNotConnectedPM = "Connection failure while trying to clone virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + ": There cannot be cloned any virtual machine on this physical machine now, because it is not connected.";
        String errMsgForPMConError = "Connection failure while trying to clone virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + ": ";
        String errMsgForUnknownVM = "Cloning virtual machine failure: There is no virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + " known to VirtualBox. Most probably has been this virtual machine removed recently."; 
        String errMsgForCloneTypeNullCheck = "Cloning virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + " failure: There is not specified (is null) a type of a clone should be created.";
        String errMsgForVMStateCheck = "Cloning virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + " failure: Virtual machine cannot be cloned, because it is not in one of required state (PoweredOff, Saved, Running, Paused).";
        NativeVBoxAPIConnection natapiCon = NativeVBoxAPIConnection.getInstance();
        
        checkVMIsNotNull(virtualMachine, errMsgForVMNullCheck);
        checkPMIsNotNull(virtualMachine.getHostMachine(), errMsgForPMNullCheck);
        checkPMIsConnected(virtualMachine.getHostMachine(), errMsgForNotConnectedPM);
        checkCloneTypeIsNotNull(cloneType, errMsgForCloneTypeNullCheck);
        
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
            throw new UnexpectedVMStateException("Cloning virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + " failure: " + vboxMachine.getAccessError().getText());
        }
        checkVMStateForCloning(vboxMachine.getState().toString(), errMsgForVMStateCheck);
        
        String cloneName = getNewCloneName(vboxMachine.getName(), vbox, cloneType);
        
        IMachine clonableVBoxMachine;
        if(cloneType == CloneType.LINKED){
            ISession session = vbm.getSessionObject();
            try{
                takeSnapshot(vboxMachine,session,cloneName);
            }catch(VBoxException ex){
                if(session.getState() == SessionState.Locked){
                    session.unlockMachine();
                }
                vbm.disconnect();
                vbm.cleanup();
                throw new UnexpectedVMStateException("Cloning virtual machine " + virtualMachine + " on physical machine " + virtualMachine.getHostMachine() + " failure: " + ex.getMessage());
            }        
            ISnapshot snapshot = vboxMachine.getCurrentSnapshot();
            clonableVBoxMachine = snapshot.getMachine();
            
        }else{
            clonableVBoxMachine = vbox.findMachine(vboxMachine.getId());
        }
        
        IMachine vboxMachineClone = vbox.createMachine(null, cloneName, null, clonableVBoxMachine.getOSTypeId(), null);
        List<CloneOptions> clops = getCloneOptions(cloneType);
        CloneMode cloneMode = getCloneMode(cloneType);
        
        IProgress progress = clonableVBoxMachine.cloneTo(vboxMachineClone, cloneMode, clops);        
        Long pp = 0L;
        while(!progress.getCompleted()){
            if(progress.getPercent() > pp){
                pp = progress.getPercent();
                System.out.println(pp + "%");
            }
        }
        vboxMachineClone.saveSettings();        
        vbox.registerMachine(vboxMachineClone);
        
        IGuestOSType gost = vbox.getGuestOSType(vboxMachineClone.getOSTypeId());
        VirtualMachine vm = createVirtualMachine(vboxMachineClone, gost, virtualMachine.getHostMachine());
        
        vbm.disconnect();
        vbm.cleanup();
        
        return vm;
    }
    
    private void checkPMIsNotNull(PhysicalMachine pm, String errMsg){
        if(pm == null){
            throw new IllegalArgumentException(errMsg);
        }
    }
    
    private void checkVMIsNotNull(VirtualMachine vm, String errMsg){
        if(vm == null){
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
    
    private void checkCloneTypeIsNotNull(CloneType cloneType, String errMsg){
        if(cloneType == null){
            throw new IllegalArgumentException(errMsg);
        }
    }
    
    private void checkVMStateForCloning(String state, String errMsg) throws UnexpectedVMStateException{
        switch(state){
            case "PoweredOff":
            case "Saved"     :
            case "Running"   :
            case "Paused"    :
            default          : throw new UnexpectedVMStateException(errMsg);
        }
    }
    
    private void checkVMStateForRemoving(String state, String errMsg) throws UnexpectedVMStateException{
        if(!state.equals("PoweredOff")){
            throw new UnexpectedVMStateException(errMsg);
        }
    }
    
    private VirtualMachine createVirtualMachine(IMachine vboxMachine, IGuestOSType gost, PhysicalMachine pm){
        IMedium medium = vboxMachine.getMedium("SATA", 0, 0);
        UUID id = UUID.fromString(vboxMachine.getId());
        String name = vboxMachine.getName();
        Long cpuCount = vboxMachine.getCPUCount();
        Long monitorCount = vboxMachine.getMonitorCount();
        Long cpuExecCap = vboxMachine.getCPUExecutionCap();
        Long hardDiskFreeSpaceSize = medium.getLogicalSize()-medium.getSize();
        Long hardDiskTotalSize = medium.getLogicalSize();
        Long ram = vboxMachine.getMemorySize();
        Long vram = vboxMachine.getVRAMSize();
        String typeOfOS = gost.getFamilyId();
        String identifierOfOS = gost.getId();
        
        VirtualMachine vm = new VirtualMachine.Builder(id, name, pm)
                                              .countOfCPU(cpuCount)
                                              .countOfMonitors(monitorCount)
                                              .cpuExecutionCap(cpuExecCap)
                                              .hardDiskFreeSpaceSize(hardDiskFreeSpaceSize)
                                              .hardDiskTotalSize(hardDiskTotalSize)
                                              .sizeOfRAM(ram)
                                              .sizeOfVRAM(vram)
                                              .typeOfOS(typeOfOS)
                                              .identifierOfOS(identifierOfOS)
                                              .build();
        return vm;
    }
    
    private VirtualMachine getVM(PhysicalMachine pm, String key, String[] errMsgs) throws InterruptedException,
             ConnectionFailureException, IncompatibleVirtToolAPIVersionException, UnknownVirtualMachineException{
        
        NativeVBoxAPIConnection natapiCon = NativeVBoxAPIConnection.getInstance();
        
        VirtualBoxManager vbm = natapiCon.getVirtualBoxManager(pm, errMsgs[0]);
        IVirtualBox vbox = vbm.getVBox();
        IMachine vboxMachine = null;
        IGuestOSType gost = null;
        
        try{
            vboxMachine = vbox.findMachine(key);
            gost = vbox.getGuestOSType(vboxMachine.getOSTypeId());
        }catch(VBoxException ex){
            vbm.disconnect();
            vbm.cleanup();
            throw new UnknownVirtualMachineException(errMsgs[1]);
        }
        
        VirtualMachine vm = createVirtualMachine(vboxMachine,gost,pm);
        vbm.disconnect();
        vbm.cleanup();
        
        return vm;
    }
    
    private boolean isLinkedClone(IMachine vboxMachine, IVirtualBox vbox){
        IMedium baseMedium = vboxMachine.getMedium("SATA", 0, 0).getBase();
        IMachine baseVBoxMachine = vbox.findMachine(baseMedium.getMachineIds().get(0));
        
        return (vboxMachine.getName().contains("_LinkClone") || !baseVBoxMachine.getId().equals(vboxMachine.getId()));
    }
    
    private void removeVMAsSnapshot(IMachine vboxMachine, IVirtualBox vbox, ISession session){
        String machineName = vboxMachine.getName();
        IMedium medium = vboxMachine.getMedium("SATA", 0, 0);
        IMedium parentMedium;
        
        while(medium.getParent().getMachineIds().get(0).equals(vboxMachine.getId())){
            IMedium m = medium.getParent();
            medium = m;
        }        
        parentMedium = medium.getParent();
        
        removeLinkedCloneChildren(medium,vbox);
        removeVBoxMachine(vboxMachine);
        deleteSnapshot(parentMedium, vbox, machineName, session);
    }
    
    private void removeLinkedCloneChildren(IMedium medium, IVirtualBox vbox){
        List<IMedium> meds = medium.getChildren();
        IMachine machine = vbox.findMachine(medium.getMachineIds().get(0));
        
        if(!meds.isEmpty()){
            for(IMedium med : meds){
                IMachine am = vbox.findMachine(med.getMachineIds().get(0));
                if(am.getId().equals(machine.getId())){
                    am = null;
                }
                removeLinkedCloneChildren(med,vbox);
                if(am != null){
                    removeVBoxMachine(am);
                }
            }
        }
    }
    
    private void removeVBoxMachine(IMachine vboxMachine){
        List<IMedium> mediums = vboxMachine.unregister(CleanupMode.DetachAllReturnHardDisksOnly);
        vboxMachine.deleteConfig(mediums);
    }
    
    private void removeVMAsStandaloneUnit(IMachine vboxMachine, IVirtualBox vbox){
        IMedium medium = vboxMachine.getMedium("SATA", 0, 0);
        
        while(medium.getParent().getMachineIds().get(0).equals(vboxMachine.getId())){
            IMedium m = medium.getParent();
            medium = m;
        }
        
        removeLinkedCloneChildren(medium,vbox);
        removeVBoxMachine(vboxMachine);
    }
    
    private void deleteSnapshot(IMedium parentMedium, IVirtualBox vbox, String machineName, ISession session){
        IMachine parentMachine = vbox.findMachine(parentMedium.getMachineIds().get(0));
        ISnapshot snapshot = parentMachine.findSnapshot(null);
        
        for(long i = 0; i < parentMachine.getSnapshotCount(); ++i){            
            ISnapshot tmp;
            if(!snapshot.getChildren().isEmpty()){
                tmp = snapshot.getChildren().get(0);
            }else{
                tmp = null;
            }
            
            if(snapshot.getName().contains(machineName)){
                parentMachine.lockMachine(session, LockType.Write);
                IConsole console = session.getConsole();
                IProgress p = console.deleteSnapshot(snapshot.getId());
                while(!p.getCompleted()){
                    //do nothing, just loop while condition is true
                }
                session.unlockMachine();
            }
            
            snapshot = tmp;
        }
    }
    
    private String getNewCloneName(String origName, IVirtualBox vbox, CloneType cloneType){
        String sufix = null;
        
        switch(cloneType){
            case FULL_FROM_MACHINE_AND_CHILD_STATES :
            case FULL_FROM_MACHINE_STATE            :
            case FULL_FROM_ALL_STATES               : sufix = "_FullClone"; break;
            case LINKED                             : sufix = "_LinkClone"; break;
            default: throw new IllegalStateException("Cloning virtual machine " + origName + " failure: There was used illegal type of clone.");
        }
        
        int count = 1;
        String cloneName = origName + sufix + count;
        
        for(;;){
            try{
                vbox.findMachine(cloneName);
                break;
            }catch(VBoxException ex){
                ++count;
                cloneName = origName + sufix + count;
            }
        }
        
        return cloneName;
        
    }
    
    private void takeSnapshot(IMachine vboxMachine, ISession session, String cloneName){
        vboxMachine.lockMachine(session, LockType.Shared);
        IConsole c = session.getConsole();
        IProgress p = c.takeSnapshot("Linked Base For " + vboxMachine.getName() + " and " + cloneName, null);
        while(!p.getCompleted()){
            //do nothing, just loop until snapshot is created
        }
        session.unlockMachine();
    }
    
    private List<CloneOptions> getCloneOptions(CloneType cloneType){
        List<CloneOptions> clops = new ArrayList<>();
        
        if(cloneType == CloneType.LINKED){
            clops.add(CloneOptions.Link);
        }
        
        return clops;
    }
    
    private CloneMode getCloneMode(CloneType cloneType){
        switch(cloneType){
            case FULL_FROM_MACHINE_STATE            : return CloneMode.MachineState;
            case FULL_FROM_MACHINE_AND_CHILD_STATES : return CloneMode.MachineAndChildStates;
            case FULL_FROM_ALL_STATES               : return CloneMode.AllStates;
            case LINKED                             : return CloneMode.MachineState;
            default                                 : return CloneMode.MachineState;
        }
    }
}

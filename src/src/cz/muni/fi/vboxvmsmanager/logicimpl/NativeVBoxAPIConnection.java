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
import cz.muni.fi.vboxvmsmanager.pubapi.managers.VirtualMachineManager;
import cz.muni.fi.vboxvmsmanager.pubapi.managers.VirtualizationToolManager;
import java.util.List;
import org.virtualbox_4_3.IVirtualBox;
import org.virtualbox_4_3.VBoxException;
import org.virtualbox_4_3.VirtualBoxManager;

/**
 *
 * @author Tomáš Šmíd
 */
class NativeVBoxAPIConnection {
    
    private static final NativeVBoxAPIConnection INSTANCE = new NativeVBoxAPIConnection();
    
    public static NativeVBoxAPIConnection getInstance(){
        return INSTANCE;
    }
    
    private NativeVBoxAPIConnection(){
        
    }
    
    public void connectTo(PhysicalMachine physicalMachine) throws ConnectionFailureException, InterruptedException,
            IncompatibleVirtToolAPIVersionException{
        
        String errMsgForNullPM = "Connection failure: There was made an attempt to connect to a null physical machine.";
        String errMsgForInvalidCon = "Connecting to physical machine " + physicalMachine + " failure: ";
        AccessedPhysicalMachines apm = AccessedPhysicalMachines.getInstance();
        
        checkPMIsNotNull(physicalMachine,errMsgForNullPM);
        VirtualBoxManager vbm = getVirtualBoxManager(physicalMachine,errMsgForInvalidCon); //tests connection, if everything ok, then not null object is returned
        
        if(!isConnected(physicalMachine)){
            apm.add(physicalMachine);
        }
        
        vbm.disconnect();
        vbm.cleanup();
    }
    
    public void disconnectFrom(PhysicalMachine physicalMachine) throws DisconnectionFailureException{
        String msg0 = "Disconnection failure: There was made an attempt to disconnect from a null physical machine.";
        String msg1 = "Connecting to physical machine " + physicalMachine + " failure: ";
        AccessedPhysicalMachines apm = AccessedPhysicalMachines.getInstance();
        VirtualBoxManager vbm;
        
        checkPMIsNotNull(physicalMachine, msg0);
        if(!isConnected(physicalMachine)){
            throw new DisconnectionFailureException("Disconnection failure: Physical machine "
                    + physicalMachine + " cannot be disconnected because it is not connected.");
        }
        
        try{
            vbm = validateConnectionToPM(physicalMachine,msg1);
        }catch(ConnectionFailureException ex){
            apm.remove(physicalMachine);
            throw new DisconnectionFailureException("Disconnection failure: Incorrect disconnection "
                    + "from physical machine " + physicalMachine + ". Most probably there could be one of "
                    + "two possible problems - network connection is not working or remote VirtualBox"
                    + "web server is not running.\nPossible solution: check both network connection and"
                    + " remote VirtualBox web server are running and working correctly,"
                    + "then try to connect to this physical machine again and after that disconnect from this"
                    + "physical machine in order to ensure correct end of work with this one.");
        }catch(IncompatibleVirtToolAPIVersionException ex){
            apm.remove(physicalMachine);
            throw new DisconnectionFailureException("Disconnection failure: Incorrect disconnection "
                    + "from physical machine " + physicalMachine + ". This physical machine could not be "
                    + "disconnected correctly and thus there could not be ensured correct end of work with this one, "
                    + "because there is wrong VirtualBox API version on this physical machine.");
        }        
        vbm.disconnect();
        vbm.cleanup();
        stopRunningVMs(physicalMachine);
        apm.remove(physicalMachine);
    }
    
    public boolean isConnected(PhysicalMachine physicalMachine){
        AccessedPhysicalMachines apm = AccessedPhysicalMachines.getInstance();
        
        return apm.isAccessed(physicalMachine);
    }
    
    public List<PhysicalMachine> getConnectedPhysicalMachines(){
        AccessedPhysicalMachines apm = AccessedPhysicalMachines.getInstance();
        
        return apm.getAccessedPhysicalMachines();
    }
    
    VirtualBoxManager getVirtualBoxManager(PhysicalMachine physicalMachine, String errMsg)
            throws ConnectionFailureException, InterruptedException, IncompatibleVirtToolAPIVersionException{        
        
        VirtualBoxManager vbm = tryToConnectTo(physicalMachine, errMsg);
        return vbm;
    }
    
    private void checkPMIsNotNull(PhysicalMachine pm, String errMsg){
        if(pm == null){
            throw new IllegalArgumentException(errMsg);
        }
    }
    
    private VirtualBoxManager validateConnectionToPM(PhysicalMachine pm, String partOfErrMsg) 
            throws ConnectionFailureException, IncompatibleVirtToolAPIVersionException{
        
        String url = "http://" + pm.getAddressIP() + ":" + pm.getPortOfVTWebServer();
        VirtualBoxManager vbm = VirtualBoxManager.createInstance(null);
        IVirtualBox vbox = null;        
        
        try{
            vbm.connect(url, pm.getUsername(), pm.getUserPassword());
            vbox = vbm.getVBox();
        }catch(VBoxException ex){
            throw new ConnectionFailureException(partOfErrMsg + "Most probably there "
                    + "could be one of two possible problems - "
                    + "network connection is not working or remote VirtualBox "
                    + "web server is not running.");
        }
        
        if(!vbox.getAPIVersion().equals("4_3")){
            throw new IncompatibleVirtToolAPIVersionException("Incompatible version of "
                    + "VirtualBox API: Required VBox API version is 4_3, but actual "
                    + "VirtualBox API version is " + vbox.getAPIVersion() + ". "
                    + "There is no guarantee this API would work with incompatible "
                    + "VirtualBox API version correctly, that's why this physical machine "
                    + " has not been connected and thus cannot be operated with.");
        }
        
        return vbm;
    }
    
    private VirtualBoxManager tryToConnectTo(PhysicalMachine pm, String partOfErrMsg) 
            throws ConnectionFailureException, InterruptedException, IncompatibleVirtToolAPIVersionException{
        
        VirtualBoxManager vbm = null;        
        AccessedPhysicalMachines apm = AccessedPhysicalMachines.getInstance();
        int attempt = 0;
        
        while(attempt < 3){
            try{
                vbm = validateConnectionToPM(pm,partOfErrMsg);
                break;
            }catch(ConnectionFailureException ex){
                ++attempt;
                if(attempt == 3){
                    apm.remove(pm);//if connected, then will be removed, otherwise nothing will happen
                    throw ex;
                }else{
                    Thread.sleep(5000l);
                }
            }catch(IncompatibleVirtToolAPIVersionException ex){
                apm.remove(pm);//if connected, then will be removed, otherwise nothing will happen 
                throw ex;
            }
        }
        
        return vbm;
    }
    
    private void stopRunningVMs(PhysicalMachine pm){
        VirtualizationToolManager vtm = new VirtualizationToolManagerImpl(pm);
        List<VirtualMachine> vms = vtm.getVirtualMachines();
        VirtualMachineManager vmm = vtm.getVirtualMachineManager();
        
        if(!vms.isEmpty()){
            vms.stream().forEach((vm) -> {
                String ms = vmm.getVMState(vm);
                if (ms.equals("Running") || ms.equals("Paused") || ms.equals("Stuck")) {
                    vmm.shutDownVM(vm);
                }
            });
        }
    }
}

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

package cz.muni.fi.VBoxAndVMsManager.logic;

import cz.muni.fi.VBoxAndVMsManager.machines.IVirtualMachine;
import cz.muni.fi.VBoxAndVMsManager.machines.PhysicalMachine;
import cz.muni.fi.VBoxAndVMsManager.pubcon.PortRule;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.virtualbox_4_3.IConsole;
import org.virtualbox_4_3.IMachine;
import org.virtualbox_4_3.IMedium;
import org.virtualbox_4_3.INATEngine;
import org.virtualbox_4_3.INetworkAdapter;
import org.virtualbox_4_3.IProgress;
import org.virtualbox_4_3.ISession;
import org.virtualbox_4_3.IVirtualBox;
import org.virtualbox_4_3.LockType;
import org.virtualbox_4_3.MachineState;
import org.virtualbox_4_3.NATProtocol;
import org.virtualbox_4_3.VBoxException;
import org.virtualbox_4_3.VirtualBoxManager;

/**
 *
 * @author Tomáš Šmíd
 */
final class VirtualMachine implements IVirtualMachine, Comparable<IVirtualMachine>{

    /*private final String vmName;
    private final Long countOfCPU;
    private final Long countOfMonitors;
    private final Long cpuExecutionCap;
    private final Long hardDiskTotalSize;
    private final Long hardDiskFreeSpaceSize;*/
    private Collection<PortRule> portRules;
    /*private final Long sizeOfRAM;
    private final Long sizeOfVideoRAM;
    private final String typeOfOS;*/
    
    private final VirtualBoxManager virtualBoxManager;
    private final PhysicalMachine hostMachine;
    private final IMachine guestMachine;
    private final ISession session;
    private final String vmName;
    private final Long hardDiskTotalSize;
    private final Long hardDiskFreeSpaceSize;

    public VirtualMachine(PhysicalMachine pm, VirtualBoxManager vbm, String vmName){        
        this.vmName = vmName;
        this.hostMachine = pm;
        this.virtualBoxManager = vbm;
        
        IVirtualBox vbox = this.virtualBoxManager.getVBox();
        this.guestMachine = vbox.findMachine(vmName);
        this.session = this.virtualBoxManager.getSessionObject();
        
        IMedium medium = getMedium(vbox,vmName);
        if(medium != null){
            this.hardDiskTotalSize = medium.getLogicalSize(); //v bytech
            this.hardDiskFreeSpaceSize = medium.getLogicalSize()-medium.getSize(); //v bytech
        }else{
            this.hardDiskTotalSize = 0L; //v bytech
            this.hardDiskFreeSpaceSize = 0L; //v bytech
        }
        this.portRules = getNATPortRules();
        
        /*this.vmName = machine.getName();
        this.countOfCPU = machine.getCPUCount();
        this.countOfMonitors = machine.getMonitorCount();
        this.cpuExecutionCap = machine.getCPUExecutionCap();
        if(medium != null){
            this.hardDiskTotalSize = medium.getLogicalSize(); //v bytech
            this.hardDiskFreeSpaceSize = medium.getLogicalSize()-medium.getSize(); //v bytech
        }else{
            this.hardDiskTotalSize = 0L; //v bytech
            this.hardDiskFreeSpaceSize = 0L; //v bytech
        }
        this.portRules = new HashSet<>();
        this.sizeOfRAM = machine.getMemorySize(); // v MB
        this.sizeOfVideoRAM = machine.getVRAMSize();
        this.typeOfOS = machine.getOSTypeId();*/
    }

    @Override
    public void addPortRule(String ruleName, Protocol protocol, int hostPort,
                            int guestPort) {
        INetworkAdapter adapter = guestMachine.getNetworkAdapter(1L);
        INATEngine natEngine = adapter.getNATEngine();
        NATProtocol natp = (protocol == Protocol.TCP) ? NATProtocol.TCP : 
                                                        NATProtocol.UDP;        
        
        System.out.println("Adding a new port forwarding rule " + ruleName);
        if(checkRuleName(ruleName)){
            if(checkPortRange(hostPort) && checkPortRange(guestPort)){
                if(checkHostPort(hostPort)){
                    natEngine.addRedirect(ruleName, natp, "", hostPort, "", guestPort);
                    String p = (protocol == Protocol.TCP) ? "TCP" : "UDP";
                    PortRule rule = new PortRule(ruleName, p, "", hostPort, "",
                                                 guestPort);
                    portRules.add(rule);
                    System.out.println("Port forwarding rule \"" + ruleName +
                                       "\" successfully added");
                }else{
                    System.err.println("Adding a new port forwarding rule failed:");
                    System.err.println("Port forwarding rule for host port " +
                                       "number \"" + hostPort + "\" on host " +
                                       "machine with IP address " +
                                       hostMachine.getAddressIP() + "already " +
                                       "exists.");
                }
            }else{
                System.err.println("Adding a new port forwarding rule failed:");
                System.err.println("Bad value of host or guest port number " +
                                   "-> port number must be from interval " +
                                   "0-65535.");
            }
        }else{
            System.err.println("Adding a new port forwarding rule failed:");
            System.err.println("Port forwarding rule name \"" + ruleName +
                               "\" already exists. Rule name must be unique.");
        }
    }

    @Override
    public Long getCountOfCPU() {
        return guestMachine.getCPUCount();
    }

    @Override
    public Long getCountOfMonitors() {
        return guestMachine.getMonitorCount();
    }

    @Override
    public Long getCPUExecutionCap() {
        return guestMachine.getCPUExecutionCap();
    }

    @Override
    public Long getHardDiskFreeSpaceSize() {
        return hardDiskFreeSpaceSize;
    }

    @Override
    public Long getHardDiskTotalSize() {
        return hardDiskTotalSize;
    }

    @Override
    public String getName() {
        return vmName;
    }

    @Override
    public Collection<PortRule> getOwnPortRules() {
        return portRules;
    }

    @Override
    public Collection<String> getOwnPortRulesNames() {
        Collection<String> rulesNames = null;
        
        if(!portRules.isEmpty()){
            rulesNames = new ArrayList<>();
            for(PortRule rule : portRules){
                rulesNames.add(rule.getRuleName());
            }
        }
        
        return rulesNames;
    }

    @Override
    public Long getSizeOfRAM() {
        return guestMachine.getMemorySize();
    }

    @Override
    public String getTypeOfOS() {
        return guestMachine.getOSTypeId();
    }

    @Override
    public Long getVideoMemorySize() {
        return guestMachine.getVRAMSize();
    }

    @Override
    public void shutDown() {        
        MachineState ms = guestMachine.getState();
        if(ms != MachineState.PoweredOff && ms != MachineState.Aborted
                && ms != MachineState.Stuck){
            guestMachine.lockMachine(session, LockType.Shared);
            System.out.println("Shutting down the virtual machine " +  vmName);
            IConsole console = session.getConsole();
            IProgress progress = console.powerDown();
            progressBar(progress,10000);
            ms = guestMachine.getState();
            System.out.println(vmName + " is " + ms.toString());
            session.unlockMachine();            
        }else{
            System.err.println("Virtual machine " + vmName + " cannot be " +
                               "shutted down, because it is in a state, which " +
                               "indicates the machine is already powered off.");
        }
    }

    @Override
    public void removeAllOwnPortRules() {
        INetworkAdapter adapter = guestMachine.getNetworkAdapter(1L);
        INATEngine natEngine = adapter.getNATEngine();
        
        System.out.println("Removing all port forwarding rules");
        for(PortRule rule : portRules){
            natEngine.removeRedirect(rule.getRuleName());
        }
        portRules.clear();
        System.out.println("Removing finished successfully");
        
    }

    @Override
    public void removeOneOwnPortRule(String ruleName) {
        INetworkAdapter adapter = guestMachine.getNetworkAdapter(1L);
        INATEngine natEngine = adapter.getNATEngine();
        PortRule ruleToRem = null;
        
        System.out.println("Removing a port forwarding rule \"" + ruleName + "\"");
        for(PortRule rule : portRules){
            if(rule.getRuleName().equals(ruleName)){
                ruleToRem = rule;
                break;
            }
        }
        if(ruleToRem != null){
            natEngine.removeRedirect(ruleName);
            portRules.remove(ruleToRem);
            System.out.println("Port forwarding rule \"" + ruleName + "\"" +
                               " successfully removed");
        }else{
            System.err.println("Removing port forwarding rule failed:");
            System.err.println("Port forwarding rule \"" + ruleName + "\"" +
                               "does not exist.");
        }
        
    }

    @Override
    public void start() {
        try{
            /*IVirtualBox vbox = virtualBoxManager.getVBox();
            if(vbox == null){
                System.out.println("Starting of the virtual machine " + this.vmName +
                                   "was interrupted.");
                System.out.println("There is not able to access the Virtual Box " +
                                   "on the machine with IP address " +
                                   hostMachine.getAddressIP() + ".");
                System.out.println("Check whether a virtual box web server is " +
                                   "running on the host machine. Also check " +
                                   "whether there is correctly installed" +
                                   "Virtual Box on the host machine and " +
                                   "the version of Virtual Box is 4.3.xx .");
                return;
            }*/
            //IMachine machine = vbox.findMachine(vmName);
            MachineState ms = guestMachine.getState();
            if(ms == MachineState.Running || ms == MachineState.Paused){
                System.err.println("Starting of the virtual machine " +
                                   vmName + " was interrupted -> " +
                                   "machine has already been running.");
                return;
            }
            //ISession session = virtualBoxManager.getSessionObject();
            System.out.println("Starting the virtual machine " + vmName);
            IProgress progress = guestMachine.launchVMProcess(session, "gui", "");
            progressBar(progress,10000);
            ms = guestMachine.getState();
            System.out.println(vmName + " is " + ms.toString());
            session.unlockMachine();            
        }catch(VBoxException ex){
            System.err.println("Starting the virtual machine " + vmName +
                               " was interrupted.");
            System.out.println(ex.getMessage());
            if(ex.toString().contains("is already locked by a session " +
                                      "(or being locked or unlocked)")){
                System.out.println("Try to restart the web server on the " +
                                   "host machine " + hostMachine);
            }
        }
    }

    @Override
    public boolean equals(Object other){
        if(other == this) return true;
        if(other == null) return false;
        if(getClass() != other.getClass()) return false;
        IVirtualMachine vm = (IVirtualMachine)other;
        return (this.vmName == vm.getName() ||
                (this.vmName != null && this.vmName.equals(vm.getName())));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.vmName);
        return hash;
    }

    @Override
    public int compareTo(IVirtualMachine vm) {
        return this.vmName.compareTo(vm.getName());
    }
    
    private IMedium getMedium(IVirtualBox vbox, String vmName){
        List<IMachine> machines = vbox.getMachines();
        int index = -1;
        for(int i = 0; i < machines.size(); ++i){
            if(machines.get(i).getName().equals(vmName)){
                index = i;
                break;
            }
        }
        if(index < 0)
            return null;
        IMedium medium = vbox.getHardDisks().get(index);
        return medium;
    }
    
    private boolean progressBar(IProgress p, long waitMillis)
    {
        long end = System.currentTimeMillis() + waitMillis;
        while (!p.getCompleted())
        {
            virtualBoxManager.waitForEvents(0);
            p.waitForCompletion(200);
            if (System.currentTimeMillis() >= end)
                return false;
        }
        return true;
    }
    
    private boolean checkRuleName(String ruleName){
        for(PortRule rule : portRules){
            if(rule.getRuleName().equals(ruleName))
                return false;
        }
        return true;
    }
    
    private boolean checkPortRange(int port){
        return (port >= 0 && port <= 65535);
    }
    
    private boolean checkHostPort(int hostPort){
        for(PortRule rule : portRules){
            if(rule.getHostPort() == hostPort)
                return false;
        }
        return true;
    }
    
    private Collection<PortRule> getNATPortRules(){
        INetworkAdapter adapter = guestMachine.getNetworkAdapter(1L);
        INATEngine natEngine = adapter.getNATEngine();
        Collection<PortRule> rulesToRet = new ArrayList<>();
        
        Collection<String> natRules = natEngine.getRedirects();
        if(natRules != null && !natRules.isEmpty()){
            for(String r : natRules){
                rulesToRet.add(parsePortRule(r));
            }
        }
        
        return rulesToRet;
    }
    
    private PortRule parsePortRule(String rule){
        String[] parts = rule.split(",");
        int hp = Integer.parseInt(parts[3]);
        int gp = Integer.parseInt(parts[5]);
        
        return new PortRule(parts[0],parts[1],parts[2],hp,parts[4],gp);
    }
}
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

import cz.muni.fi.vboxvmsmanager.pubapi.entities.PortRule;
import cz.muni.fi.vboxvmsmanager.pubapi.entities.VirtualMachine;
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.ConnectionFailureException;
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.IncompatibleVirtToolAPIVersionException;
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.PortRuleDuplicityException;
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.UnexpectedVMStateException;
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.UnknownPortRuleException;
import cz.muni.fi.vboxvmsmanager.pubapi.exceptions.UnknownVirtualMachineException;
import cz.muni.fi.vboxvmsmanager.pubapi.managers.VirtualMachineManager;
import java.util.List;

/**
 *
 * @author Tomáš Šmíd
 */
public class VirtualMachineManagerImpl implements VirtualMachineManager{

    private static VirtualMachineManagerImpl INSTANCE = new VirtualMachineManagerImpl();
    
    static VirtualMachineManagerImpl getInstance(){
        return INSTANCE;
    }
    
    private VirtualMachineManagerImpl(){
        
    }
    
    @Override
    public void startVM(VirtualMachine virtualMachine) {
        NativeVBoxAPIMachine natapiMach = NativeVBoxAPIMachine.getInstance();
        boolean error = false;
        
        if(virtualMachine != null){
            System.out.println("Starting virtual machine " + virtualMachine);
            try{
                natapiMach.startVM(virtualMachine);
            } catch (InterruptedException | ConnectionFailureException | IncompatibleVirtToolAPIVersionException
                    | UnknownVirtualMachineException | UnexpectedVMStateException | IllegalArgumentException ex) {

                System.err.println(ex.getMessage());
                error = true;
            }

            if(!error){
                System.out.println("Virtual machine \"" + virtualMachine.getName() + "\" is running");
            }
        }else{
            System.err.println("Starting virtual machine failure: There was made an attempt to start a null virtual machine.");
        }
    }

    @Override
    public void shutDownVM(VirtualMachine virtualMachine) {
        NativeVBoxAPIMachine natapiMach = NativeVBoxAPIMachine.getInstance();
        boolean error = false;
        
        if(virtualMachine != null){
            System.out.println("Shutting down virtual machine " + virtualMachine);
            try{
                natapiMach.shutDownVM(virtualMachine);
            } catch (ConnectionFailureException | InterruptedException | IncompatibleVirtToolAPIVersionException
                    | UnknownVirtualMachineException | UnexpectedVMStateException | IllegalArgumentException ex) {

                System.err.println(ex.getMessage());
                error = true;
            }

            if(!error){
                System.out.println("Virtual machine \"" + virtualMachine.getName() + "\" is powered off");
            }
        }else{
            System.err.println("Shutdown virtual machine failure: There was made an attempt to shut down a null virtual machine.");
        }
    }

    @Override
    public void addPortRule(VirtualMachine virtualMachine, PortRule rule) {
        NativeVBoxAPIMachine natapiMach = NativeVBoxAPIMachine.getInstance();
        boolean error = false;
        
        if(virtualMachine != null){
            if(rule != null){
                System.out.println("Adding new port forwarding rule " + rule + " to virtual machine " + virtualMachine);
                try{
                    natapiMach.addPortRule(virtualMachine, rule);
                } catch (ConnectionFailureException | InterruptedException | IncompatibleVirtToolAPIVersionException
                        | UnknownVirtualMachineException | IllegalArgumentException | PortRuleDuplicityException
                        | UnexpectedVMStateException ex) {

                    System.err.println(ex.getMessage());
                    error = true;
                }

                if(!error){
                    System.out.println("New port forwarding rule \"" + rule.getName() + "\" added successfully");
                }
            }else{
                System.err.println("Creating new port forwarding rule failure: There was made an attempt to create a null port forwarding rule for virtual machine " + virtualMachine + ".");
            }
        }else{
            System.err.println("Creating new port forwarding rule failure: There was made an attempt to create a new port forwarding rule for a null virtual machine.");
        }
    }

    @Override
    public void deletePortRule(VirtualMachine virtualMachine, PortRule rule) {
        NativeVBoxAPIMachine natapiMach = NativeVBoxAPIMachine.getInstance();
        boolean error = false;
        
        if(virtualMachine != null){
            if(rule != null){
                System.out.println("Deleting port forwarding rule " + rule + " from virtual machine " + virtualMachine);
                try{
                    natapiMach.deletePortRule(virtualMachine, rule.getName());
                } catch (ConnectionFailureException | InterruptedException | IncompatibleVirtToolAPIVersionException
                        | UnknownVirtualMachineException | UnknownPortRuleException | IllegalArgumentException
                        | UnexpectedVMStateException ex) {

                    System.err.println(ex.getMessage());
                    error = true;
                }

                if(!error){
                    System.out.println("Deleting finished successfully");
                }
            }else{
                System.err.println("Creating new port forwarding rule failure: There was made an attempt to create a null port forwarding rule for virtual machine " + virtualMachine + ".");
            }
        }else{
            System.err.println("Deleting port forwarding rule failure: There was made an attempt to delete a port forwarding rule of a null virtual machine.");
        }
    }

    @Override
    public void deleteAllPortRules(VirtualMachine virtualMachine) {
        NativeVBoxAPIMachine natapiMach = NativeVBoxAPIMachine.getInstance();
        List<PortRule> portRules = null;
        boolean error = false;
        
        if(virtualMachine != null){
            System.out.println("Deleting all port forwarding rules from virtual machine " + virtualMachine);
            try{
                portRules = natapiMach.getPortRules(virtualMachine);
            } catch (ConnectionFailureException | InterruptedException | IncompatibleVirtToolAPIVersionException
                    | UnknownVirtualMachineException | IllegalArgumentException | UnexpectedVMStateException ex) {

                System.err.println("Deleting all port forwarding rules failure -> " + ex.getMessage());
                error = true;
            }

            if(!error && portRules != null && !portRules.isEmpty()){
                try{
                    for(PortRule rule : portRules){
                        natapiMach.deletePortRule(virtualMachine, rule.getName());
                    }
                } catch (ConnectionFailureException | InterruptedException | IncompatibleVirtToolAPIVersionException
                        | UnknownVirtualMachineException | UnknownPortRuleException | IllegalArgumentException
                        | UnexpectedVMStateException ex) {

                    System.err.println(ex.getMessage());
                    error = true;
                }
            }

            if(!error){
                System.out.println("All port forwarding rules from virtual machine " + virtualMachine + " deleted successfully");
            }
        }else{
            System.err.println("Deleting all port forwarding rules failure: There was made an attempt to delete all port forwarding rules of a null virtual machine.");
        }
    }

    @Override
    public List<PortRule> getPortRules(VirtualMachine virtualMachine) {
        NativeVBoxAPIMachine natapiMach = NativeVBoxAPIMachine.getInstance();
        List<PortRule> portRules = null;
        boolean error = false;
        
        if(virtualMachine != null){
            System.out.println("Retrieving all port forwarding rules from virtual machine " + virtualMachine);
            try{
                portRules = natapiMach.getPortRules(virtualMachine);
            } catch (ConnectionFailureException | InterruptedException | IncompatibleVirtToolAPIVersionException
                    | UnknownVirtualMachineException | IllegalArgumentException | UnexpectedVMStateException ex) {

                System.err.println(ex.getMessage());
                error = true;
            }

            if(!error){
                System.out.println("All port rules from virtual machine " + virtualMachine + " retrieved successfully");
            }
        }else{
            System.err.println("Retrieving all port forwarding rules failure: There was made an attempt to retrieve all port forwarding rules of a null virtual machine.");
        }
        
        return portRules;
    }

    @Override
    public String getVMState(VirtualMachine virtualMachine) {
        NativeVBoxAPIMachine natapiMach = NativeVBoxAPIMachine.getInstance();
        String state = null;
        
        try{
            state = natapiMach.getVMState(virtualMachine);
        } catch (ConnectionFailureException | InterruptedException | IncompatibleVirtToolAPIVersionException
                | UnknownVirtualMachineException | UnexpectedVMStateException | IllegalArgumentException ex) {
            
            System.err.println(ex.getMessage());
        }
        
        return state;
    }
    
}

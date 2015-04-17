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
import cz.muni.fi.vboxvmsmanager.pubapi.entities.SearchCriteria;
import cz.muni.fi.vboxvmsmanager.pubapi.entities.VirtualMachine;
import cz.muni.fi.vboxvmsmanager.pubapi.managers.SearchManager;
import cz.muni.fi.vboxvmsmanager.pubapi.types.SearchCriterionType;
import cz.muni.fi.vboxvmsmanager.pubapi.types.SearchMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Tomáš Šmíd
 */
public class SearchManagerImpl implements SearchManager{

    @Override
    public List<VirtualMachine> search(SearchCriteria searchCriteria, SearchMode mode,
                                       List<SearchCriterionType> inSearchOrder) {        
        
        if(!isValidationSuccessfull(searchCriteria, mode)){
            return null;
        }                
        List<VirtualMachine> vms = getAllVirtualMachines();        
        if(vms.isEmpty()){
            return new ArrayList<>();
        }
        
        List<SearchCriterionType> searchOrder = getValidSearchOrder(inSearchOrder, searchCriteria);
        
        return findAllSuitableVMs(vms,searchCriteria,mode,searchOrder);
    }
    
    private boolean isAnyPMConnected(){
        AccessedPhysicalMachines apm = AccessedPhysicalMachines.getInstance();
        List<PhysicalMachine> connectedPMs = apm.getAccessedPhysicalMachines();
        
        return !connectedPMs.isEmpty();
    }
    
    private boolean hasAnySpecifiedParameter(SearchCriteria searchCriteria){        
        UUID id = searchCriteria.getVmId();
        String name = searchCriteria.getVmName();
        String versionOfOS = searchCriteria.getIdentifierOfOS();
        String typeOfOS = searchCriteria.getTypeOfOS();
        Long cpuCount = searchCriteria.getCountOfCPU();
        Long monitorCount = searchCriteria.getCountOfMonitors();
        Long cpuExecCap = searchCriteria.getCpuExecutionCap();
        Long hddFreeSpace = searchCriteria.getHardDiskFreeSpaceSize();
        Long hddTotalSize = searchCriteria.getHardDiskTotalSize();
        Long ram = searchCriteria.getSizeOfRAM();
        Long vram = searchCriteria.getSizeOfVRAM();
        
        if(id != null && !id.toString().trim().isEmpty()){ return true; }
        
        if(name != null && !name.trim().isEmpty()){ return true; }
        
        if(versionOfOS != null && !versionOfOS.trim().isEmpty()){ return true; }
        
        if(typeOfOS != null && !typeOfOS.trim().isEmpty()){ return true; }
        
        if(cpuCount != null && cpuCount >= 0){ return true; }
        
        if(monitorCount != null && monitorCount >= 0){ return true; }
        
        if(cpuExecCap != null && cpuExecCap >= 0){ return true; }
        
        if(hddFreeSpace != null && hddFreeSpace >= 0){ return true; }
        
        if(hddTotalSize != null && hddTotalSize >= 0){ return true; }
        
        if(ram != null && ram >= 0){ return true; }
        
        if(vram != null && vram >= 0){ return true; }
        
        return false;
    }
    
    private boolean isValidationSuccessfull(SearchCriteria searchCriteria, SearchMode mode){
        if(!isAnyPMConnected()){
            System.err.println("Searching virtual machine failure: There cannot be done virtual machine search, because there is no physical machine connected.");
            return false;
        }
        
        if(searchCriteria == null){
            System.err.println("Searching virtual machine failure: There was made an attempt to search virtual machine with a null search criteria.");
            return false;
        }
        
        if(!hasAnySpecifiedParameter(searchCriteria)){
            System.err.println("Searching virtual machine failure: There was made an attempt to search virtual machine with no specified parameter of search criteria.");
            return false;
        }
        
        if(mode == null){
            System.err.println("Searching virtual machine failure: Search mode is null.");
            return false;
        }
        
        if(mode != SearchMode.PRECISE && mode != SearchMode.TOLERANT){
            System.err.println("Searching virtual machine failure: Invalid search mode used.");
            return false;
        }
        
        return true;
    }
    
    private List<VirtualMachine> getAllVirtualMachines(){
        AccessedPhysicalMachines apm = AccessedPhysicalMachines.getInstance();
        List<PhysicalMachine> connectedPMs = apm.getAccessedPhysicalMachines();
        List<VirtualMachine> allVMs = new ArrayList<>();        
        
        for(PhysicalMachine pm : connectedPMs){
            VirtualizationToolManagerImpl vtm = new VirtualizationToolManagerImpl(pm);
            List<VirtualMachine> vmsFromOnePM = vtm.getVirtualMachines();
            if(!vmsFromOnePM.isEmpty()){
                allVMs.addAll(vmsFromOnePM);
            }
        }
        
        return allVMs;
    }
    
    private List<SearchCriterionType> getValidSearchOrder(List<SearchCriterionType> so,
                                                          SearchCriteria sc){
        List<SearchCriterionType> searchOrder;
        
        if(so == null || so.isEmpty()){
            searchOrder = getDefaultSearchOrder();
        }else{
            searchOrder = new ArrayList<>();
            for(SearchCriterionType sct : so){
                if(sct != null){
                    searchOrder.add(sct);
                }
            }
            if(searchOrder.isEmpty()){
                searchOrder = getDefaultSearchOrder();
            }
        }
        
        return createValidSearchOrder(searchOrder, sc);
    }
    
    private List<SearchCriterionType> getDefaultSearchOrder(){
        return new ArrayList(Arrays.asList(SearchCriterionType.ID,
                                           SearchCriterionType.NAME,
                                           SearchCriterionType.OS_TYPE,
                                           SearchCriterionType.OS_IDENTIFIER,
                                           SearchCriterionType.CPU_COUNT,
                                           SearchCriterionType.CPU_EXEC_CAP,
                                           SearchCriterionType.RAM,
                                           SearchCriterionType.HDD_FREE_SPACE,
                                           SearchCriterionType.VRAM,
                                           SearchCriterionType.MONITOR_COUNT,
                                           SearchCriterionType.HDD_TOTAL_SIZE));
    }
    
    private List<SearchCriterionType> createValidSearchOrder(List<SearchCriterionType> searchOrder,
                                                             SearchCriteria sc){
        
        List<SearchCriterionType> defaultOrder = getDefaultSearchOrder();
        List<Integer> indexes = new ArrayList<>();
        
        for(SearchCriterionType scType : searchOrder){
            if(!defaultOrder.remove(scType)){
                indexes.add(searchOrder.indexOf(scType));
            }
        }
        
        if(!indexes.isEmpty()){
            for(Integer index : indexes) {
                int ind = index;
                searchOrder.remove(ind);
            }
        } 
        
        if(!defaultOrder.isEmpty()){
            searchOrder.addAll(defaultOrder);
        }
        
        if(sc.getVmId() == null || sc.getVmId().toString().trim().isEmpty()) {
            searchOrder.remove(SearchCriterionType.ID);
        }
        if(sc.getVmName() == null || sc.getVmName().trim().isEmpty()){
            searchOrder.remove(SearchCriterionType.NAME);
        }
        if(sc.getTypeOfOS() == null || sc.getTypeOfOS().trim().isEmpty()){
            searchOrder.remove(SearchCriterionType.OS_TYPE);
        }
        if(sc.getIdentifierOfOS() == null || sc.getIdentifierOfOS().trim().isEmpty()){
            searchOrder.remove(SearchCriterionType.OS_IDENTIFIER);
        }
        if(sc.getCountOfCPU() == null || sc.getCountOfCPU() < 0){
            searchOrder.remove(SearchCriterionType.CPU_COUNT);
        }
        if(sc.getCpuExecutionCap() == null || sc.getCpuExecutionCap() < 0){
            searchOrder.remove(SearchCriterionType.CPU_EXEC_CAP);
        }
        if(sc.getCountOfMonitors() == null || sc.getCountOfMonitors() < 0){
            searchOrder.remove(SearchCriterionType.MONITOR_COUNT);
        }
        if(sc.getHardDiskFreeSpaceSize() == null || sc.getHardDiskFreeSpaceSize() < 0){
            searchOrder.remove(SearchCriterionType.HDD_FREE_SPACE);
        }
        if(sc.getHardDiskTotalSize() == null || sc.getHardDiskTotalSize() < 0){
            searchOrder.remove(SearchCriterionType.HDD_TOTAL_SIZE);
        }
        if(sc.getSizeOfRAM() == null || sc.getSizeOfRAM() < 0){
            searchOrder.remove(SearchCriterionType.RAM);
        }
        if(sc.getSizeOfVRAM() == null || sc.getSizeOfVRAM() < 0){
            searchOrder.remove(SearchCriterionType.VRAM);
        }
        
        return searchOrder;
    }
    
    private List<VirtualMachine> getMatchedVMs(SearchCriterionType sct, SearchCriteria searchCriteria,
                                               List<VirtualMachine> vms, SearchMode mode){
        List<VirtualMachine> matchedVMs = new ArrayList<>();
        
        for(VirtualMachine vm : vms){
            switch(sct){
                case ID:{
                    if(vm.getId().equals(searchCriteria.getVmId())){
                        matchedVMs.add(vm);
                    }
                    break;
                }
                
                case NAME:{
                    if(vm.getName().equals(searchCriteria.getVmName())){
                        matchedVMs.add(vm);
                    }
                    break;
                }
                
                case OS_TYPE:{
                    if(vm.getTypeOfOS().equals(searchCriteria.getTypeOfOS())){
                        matchedVMs.add(vm);
                    }
                    break;
                }
                
                case OS_IDENTIFIER:{
                    if(vm.getIdentifierOfOS().equals(searchCriteria.getIdentifierOfOS())){
                        matchedVMs.add(vm);
                    }
                    break;
                }
                
                case CPU_COUNT:{
                    if(vm.getCountOfCPU().equals(searchCriteria.getCountOfCPU())){
                        matchedVMs.add(vm);
                    }
                    break;
                }
                
                case CPU_EXEC_CAP:{
                    if(vm.getCPUExecutionCap().equals(searchCriteria.getCpuExecutionCap())){
                        matchedVMs.add(vm);
                    }
                    break;
                }
                
                case HDD_FREE_SPACE:{
                    long n = (mode == SearchMode.PRECISE ? 0 : (vm.getHardDiskFreeSpaceSize()/100));
                    if(searchCriteria.getHardDiskFreeSpaceSize() <= vm.getHardDiskFreeSpaceSize() &&
                            vm.getHardDiskFreeSpaceSize() <= (searchCriteria.getHardDiskFreeSpaceSize() + n)){
                        
                        matchedVMs.add(vm);
                    }
                    break;
                }
                
                case HDD_TOTAL_SIZE:{
                    long n = (mode == SearchMode.PRECISE ? 0 : (vm.getHardDiskTotalSize()/100));
                    if((searchCriteria.getHardDiskTotalSize() - n) <= vm.getHardDiskTotalSize() &&
                            vm.getHardDiskTotalSize() <= (searchCriteria.getHardDiskTotalSize() + n)){
                        
                        matchedVMs.add(vm);
                    }
                    break;
                }
                
                case RAM:{
                    long n = (mode == SearchMode.PRECISE ? 0 : (vm.getSizeOfRAM()/100));
                    if(searchCriteria.getSizeOfRAM() <= vm.getSizeOfRAM() && 
                            vm.getSizeOfRAM() <= (searchCriteria.getSizeOfRAM() + n)){
                        
                        matchedVMs.add(vm);
                    }
                    break;
                }
                
                case VRAM:{
                    long n = (mode == SearchMode.PRECISE ? 0 : (vm.getSizeOfVRAM()/100));
                    if(searchCriteria.getSizeOfVRAM() <= vm.getSizeOfVRAM() &&
                            vm.getSizeOfVRAM() <= (searchCriteria.getSizeOfVRAM() + n)){
                        
                        matchedVMs.add(vm);
                    }
                    break;
                }
                
                case MONITOR_COUNT:{
                    if(vm.getCountOfMonitors().equals(searchCriteria.getCountOfMonitors())){
                        matchedVMs.add(vm);
                    }
                }
                
                default: return new ArrayList<>();
            }
        }
        
        return matchedVMs;
    }
    
    private List<VirtualMachine> findAllSuitableVMs(List<VirtualMachine> allVMs, SearchCriteria searchCriteria,
                                                    SearchMode mode, List<SearchCriterionType> searchOrder){
        List<VirtualMachine> tmp;
        List<VirtualMachine> emptyList = new ArrayList<>();
        boolean retEmpty = true;
        
        for(SearchCriterionType sct : searchOrder){
            tmp = getMatchedVMs(sct, searchCriteria ,allVMs, mode);
            if(tmp.isEmpty()){
                if(mode == SearchMode.PRECISE){
                    return emptyList;
                }
            }else{
                allVMs = tmp;
                if(retEmpty) { retEmpty = false; }
            }
        }
        
        return (retEmpty == true ? emptyList : allVMs);        
    }
    
}

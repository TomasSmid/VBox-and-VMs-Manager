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
package cz.muni.fi.vboxvmsmanager.pubapi.entities;

import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author Tomáš Šmíd
 */
public final class VirtualMachine implements Comparable<VirtualMachine>{
    private final UUID id;
    private final String vmName;
    private final PhysicalMachine hostMachine;
    private final Long countOfCPU;
    private final Long countOfMonitors;
    private final Long cpuExecutionCap;
    private final Long hardDiskFreeSpaceSize;
    private final Long hardDiskTotalSize;
    private final Long sizeOfRAM;
    private final Long sizeOfVRAM;
    private final String typeOfOS;
    private final String versionOfOS;
    
    //builder for more transparent set up VirtualMachine attributes
    public static class Builder{
        private final UUID id;
        private final String vmName;
        private final PhysicalMachine hostMachine;
        private Long countOfCPU = 0L;
        private Long countOfMonitors = 0L;
        private Long cpuExecutionCap = 0L;
        private Long hardDiskFreeSpaceSize = 0L;
        private Long hardDiskTotalSize = 0L;
        private Long sizeOfRAM = 0L;
        private Long sizeOfVRAM = 0L;
        private String typeOfOS = "Unknown";
        private String versionOfOS = "Unknown";
        
        public Builder(UUID id, String vmName, PhysicalMachine hostMachine){
            this.id = id;
            this.vmName = vmName;
            this.hostMachine = hostMachine;
        }
        
        public Builder countOfCPU(Long value){
            countOfCPU = value;
            return this;
        }
        
        public Builder countOfMonitors(Long value){
            countOfMonitors = value;
            return this;
        }
        
        public Builder cpuExecutionCap(Long value){
            cpuExecutionCap = value;
            return this;
        }
        
        public Builder hardDiskFreeSpaceSize(Long value){
            hardDiskFreeSpaceSize = value;
            return this;
        }
        
        public Builder hardDiskTotalSize(Long value){
            hardDiskTotalSize = value;
            return this;
        }
        
        public Builder sizeOfRAM(Long value){
            sizeOfRAM = value;
            return this;
        }
        
        public Builder sizeOfVRAM(Long value){
            sizeOfVRAM = value;
            return this;
        }
        
        public Builder typeOfOS(String value){
            typeOfOS = value;
            return this;
        }
        
        public Builder versionOfOS(String value){
            versionOfOS = value;
            return this;
        }
        
        public VirtualMachine build(){
            return new VirtualMachine(this);
        }
    }
    
    private VirtualMachine(Builder builder){
        this.id = builder.id;
        this.vmName = builder.vmName;
        this.hostMachine = builder.hostMachine;
        this.countOfCPU = builder.countOfCPU;
        this.countOfMonitors = builder.countOfMonitors;
        this.cpuExecutionCap = builder.cpuExecutionCap;
        this.hardDiskFreeSpaceSize = builder.hardDiskFreeSpaceSize;
        this.hardDiskTotalSize = builder.hardDiskTotalSize;
        this.sizeOfRAM = builder.sizeOfRAM;
        this.sizeOfVRAM = builder.sizeOfVRAM;
        this.typeOfOS = builder.typeOfOS;
        this.versionOfOS = builder.versionOfOS;
    }

    public UUID getId() {
        return id;
    }

    public String getVmName() {
        return vmName;
    }

    public PhysicalMachine getHostMachine() {
        return hostMachine;
    }

    public Long getCountOfCPU() {
        return countOfCPU;
    }

    public Long getCountOfMonitors() {
        return countOfMonitors;
    }

    public Long getCpuExecutionCap() {
        return cpuExecutionCap;
    }

    public Long getHardDiskFreeSpaceSize() {
        return hardDiskFreeSpaceSize;
    }

    public Long getHardDiskTotalSize() {
        return hardDiskTotalSize;
    }

    public Long getSizeOfRAM() {
        return sizeOfRAM;
    }

    public Long getSizeOfVRAM() {
        return sizeOfVRAM;
    }

    public String getTypeOfOS() {
        return typeOfOS;
    }

    public String getVersionOfOS() {
        return versionOfOS;
    }    
    
    @Override
    public boolean equals(Object obj){
        if(obj == this) return true;
        if(!(obj instanceof VirtualMachine)) return false;
        VirtualMachine vm = (VirtualMachine)obj;
        return ((this.id == vm.id) || 
                (this.id != null && this.id.equals(vm.id))) &&
               ((this.vmName == vm.vmName) ||
                (this.vmName != null && this.vmName.equals(vm.vmName)));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.id);
        hash = 97 * hash + Objects.hashCode(this.vmName);
        return hash;
    }
    
    @Override
    public String toString(){
        return "[" + "Virtual machine: id=" + this.id + ", name=" + this.vmName +
               ", host machine=" + this.hostMachine + "]";
    }
    
    @Override
    public int compareTo(VirtualMachine vm) {
        int result = this.id.compareTo(vm.id);
        return (result == 0 ? this.vmName.compareTo(vm.vmName) : result);
    }
}

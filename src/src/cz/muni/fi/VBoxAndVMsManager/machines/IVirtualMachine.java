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

package cz.muni.fi.VBoxAndVMsManager.machines;

import cz.muni.fi.VBoxAndVMsManager.logic.Protocol;
import cz.muni.fi.VBoxAndVMsManager.pubcon.PortRule;
import java.util.Collection;

/**
 *
 * @author Tomáš Šmíd
 */
public interface IVirtualMachine {
    
    public void addPortRule(String ruleName, Protocol protocol,
                            int hostPort, int guestPort);
    
    public Long getCountOfCPU();
    
    public Long getCountOfMonitors();
    
    public Long getCPUExecutionCap();
    
    public Long getHardDiskFreeSpaceSize();
     
    public Long getHardDiskTotalSize();
    
    public String getName();
    
    public Collection<PortRule> getOwnPortRules();
    
    public Collection<String> getOwnPortRulesNames();
    
    public Long getSizeOfRAM();
    
    public String getTypeOfOS();
    
    public Long getVideoMemorySize();
    
    public void shutDown();
    
    public void removeAllOwnPortRules();
    
    public void removeOneOwnPortRule(String ruleName);
    
    public void start();
    
}

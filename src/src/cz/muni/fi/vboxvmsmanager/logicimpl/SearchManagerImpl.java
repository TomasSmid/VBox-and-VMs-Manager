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

import cz.muni.fi.vboxvmsmanager.pubapi.entities.SearchCriteria;
import cz.muni.fi.vboxvmsmanager.pubapi.entities.VirtualMachine;
import cz.muni.fi.vboxvmsmanager.pubapi.managers.SearchManager;
import java.util.List;

/**
 *
 * @author Tomáš Šmíd
 */
public class SearchManagerImpl implements SearchManager{

    @Override
    public List<VirtualMachine> searchAbsoluteEquality(SearchCriteria searchCriteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<VirtualMachine> searchMaximalSimilarity(SearchCriteria searchCriteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

/**
 * Copyright (C) 2010-2012 Eugen Feller, INRIA <eugen.feller@inria.fr>
 *
 * This file is part of Snooze, a scalable, autonomic, and
 * energy-aware virtual machine (VM) management framework.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 */
package org.inria.myriads.snoozecommon.communication.rest.api;

import org.inria.myriads.snoozecommon.communication.groupmanager.GroupManagerDescription;
import org.inria.myriads.snoozecommon.communication.groupmanager.repository.GroupLeaderRepositoryInformation;
import org.restlet.resource.Get;

/**
 * Bootstrap resource interface.
 * 
 * @author Eugen Feller
 */
public interface BootstrapAPI
{
    /** 
     * gets the group leadre description to a group manager.
     * (called by the local controller)
     *  
     * @return   The group leader description
     */
    @Get("?getGroupLeaderDescription")
    GroupManagerDescription getGroupLeaderDescription();
    
    /**
     * 
     * gets the complee hierarchy from group leader to virtual machines
     * by decorating the GroupLeaderRepositoryInformation structure.
     * 
     * @return  The hierarchy in the GroupLeaderRepositoryInformation class 
     * 
     * */
    @Get("?getCompleteHierarchy")
    GroupLeaderRepositoryInformation getCompleteHierarchy();
    
}

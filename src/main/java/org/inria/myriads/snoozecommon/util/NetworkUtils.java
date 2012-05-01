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
package org.inria.myriads.snoozecommon.util;

import org.inria.myriads.snoozecommon.communication.NetworkAddress;

/**
 * Network utils.
 * 
 * @author Eugen Feller
 */
public final class NetworkUtils 
{    
    /** Hide constructor. */
    private NetworkUtils()
    {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Creates a new network address.
     * 
     * @param address   The address
     * @param port      The port
     * @return          The entity network address
     */
    public static NetworkAddress createNetworkAddress(String address, int port)
    {
        NetworkAddress entityAddress = new NetworkAddress();
        entityAddress.setAddress(address);
        entityAddress.setPort(port);
        return entityAddress;
    }
}

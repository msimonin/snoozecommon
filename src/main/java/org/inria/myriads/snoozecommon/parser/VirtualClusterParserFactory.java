/**
 * Copyright (C) 2010-2013 Eugen Feller, INRIA <eugen.feller@inria.fr>
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
package org.inria.myriads.snoozecommon.parser;

import javax.xml.bind.JAXBException;

import org.inria.myriads.snoozecommon.communication.NetworkAddress;
import org.inria.myriads.snoozecommon.parser.api.VirtualClusterParser;
import org.inria.myriads.snoozecommon.parser.api.impl.LibVirtXMLParser;
import org.inria.myriads.snoozecommon.parser.api.impl.LibvirtConfigParser;



/**
 * Virtual cluster parser factory.
 * 
 * @author Eugen Feller
 */
public final class VirtualClusterParserFactory 
{
    /** Hide constructor. */
    private VirtualClusterParserFactory()
    {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Creates a new virtual cluster parser.
     * 
     * @return              The virtual cluster parser
     */
    public static VirtualClusterParser newVirtualClusterParser()
    {
        //return new LibVirtXMLParser();
        return new LibvirtConfigParser();
    }
}

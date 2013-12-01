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
package org.inria.myriads.snoozecommon.parser.api.impl;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.inria.myriads.snoozecommon.communication.localcontroller.hypervisor.HypervisorDriver;
import org.inria.myriads.snoozecommon.communication.virtualcluster.VirtualMachineMetaData;
import org.inria.myriads.snoozecommon.communication.virtualcluster.monitoring.NetworkDemand;
import org.inria.myriads.snoozecommon.communication.virtualcluster.submission.VirtualClusterSubmissionRequest;
import org.inria.myriads.snoozecommon.communication.virtualcluster.submission.VirtualMachineTemplate;
import org.inria.myriads.snoozecommon.communication.virtualmachine.ResizeRequest;
import org.inria.myriads.snoozecommon.exception.VirtualClusterParserException;
import org.inria.myriads.snoozecommon.exception.VirtualMachineTemplateException;
import org.inria.myriads.snoozecommon.guard.Guard;
import org.inria.myriads.snoozecommon.parser.api.VirtualClusterParser;
import org.inria.myriads.snoozecommon.parser.util.VirtualClusterParserUtils;
import org.inria.myriads.snoozecommon.util.MathUtils;
import org.inria.myriads.snoozecommon.virtualmachineimage.VirtualMachineImage;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * LibVirt XML Parser.
 * 
 * @author Eugen Feller
 */
public final class LibVirtXMLParser
    implements VirtualClusterParser
{
    /** Define the logger. */
    private static final Logger log_ = LoggerFactory.getLogger(LibVirtXMLParser.class);
            
    /**
     * Constructor.
     */
    public LibVirtXMLParser() 
    {
        log_.debug("Starting libvirt XML parser");       
    }
    
    /**
     * @deprecated
     * Creates virtual machine meta data.
     * 
     * @param cluster                        The virual cluster description
     * @return                               The virtual machine meta data map
     * @throws VirtualClusterParserException 
     */
    public ArrayList<VirtualMachineMetaData> createVirtualMachineMetaData(VirtualClusterSubmissionRequest cluster) 
        throws VirtualClusterParserException 
    {
        Guard.check(cluster);
        log_.debug("Creating virtual machine meta data");
        
        ArrayList<VirtualMachineMetaData> metaData = new ArrayList<VirtualMachineMetaData>();
        List<VirtualMachineTemplate> virtualMachineDescriptions = cluster.getVirtualMachineTemplates();
        for (VirtualMachineTemplate description : virtualMachineDescriptions)
        {
            VirtualMachineMetaData virtualMachine;
            try 
            {
                virtualMachine = parseDescription(description);
            } 
            catch (Exception exception) 
            {
                throw new VirtualClusterParserException(String.format("Failed parsing libvirt template: %s", 
                                                                      exception.getMessage()));
            }        
            metaData.add(virtualMachine);
        }
        
        return metaData; 
    }
    
    /**
     * Start processing the file.
     * 
     * @param virtualMachineDescription     The virtual machine description
     * @return                              The virtual machine meta data
     * @throws Exception 
     */
    public VirtualMachineMetaData parseDescription(VirtualMachineTemplate virtualMachineDescription) 
        throws Exception 
    {
        Guard.check(virtualMachineDescription);
        log_.debug(String.format("Starting to parse virtual machine description: %s", virtualMachineDescription));
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        Document document = 
            documentBuilder.parse(new InputSource(new StringReader(virtualMachineDescription.getLibVirtTemplate())));

        VirtualMachineMetaData virtualMachineMetaData = 
            parseDocument(document, virtualMachineDescription.getNetworkCapacityDemand());  
        virtualMachineMetaData.setXmlRepresentation(virtualMachineDescription.getLibVirtTemplate());
        return virtualMachineMetaData;
    }
 
    /** 
     * Parse the dom representation.
     * 
     * @param document                  The document file
     * @param networkCapacityDemand     The network capacity demand
     * @return                          The virtual machine meta data
     * @throws Exception 
     */
    private VirtualMachineMetaData parseDocument(Document document, NetworkDemand networkCapacityDemand) 
        throws Exception
    {
        Guard.check(document);
        log_.debug("Parsing the DOM file now");
                                        
        Element root = document.getDocumentElement();
        String virtualMachineId = getInformation(root, "name");
        
        VirtualMachineMetaData virtualMachineMetaData = new VirtualMachineMetaData();
        virtualMachineMetaData.getVirtualMachineLocation().setVirtualMachineId(virtualMachineId);
        
        ArrayList<Double> requestedCapacity = generateRequestedCapacity(root, networkCapacityDemand);
        virtualMachineMetaData.setRequestedCapacity(requestedCapacity);
        return virtualMachineMetaData;
    }
        
    /**
     * Generates the requested capacity.
     * 
     * @param root                  The root element
     * @param networkCapacity       The network capacity
     * @return                      The requested capacity
     * @throws Exception 
     */
    private ArrayList<Double> generateRequestedCapacity(Element root,  NetworkDemand networkCapacity) 
        throws Exception 
    {          
        Guard.check(root);
        int memorySize = Integer.valueOf(getInformation(root, "memory"));
        int numberOfVCPUs = Integer.valueOf(getInformation(root, "vcpu"));
        if (memorySize == 0 || numberOfVCPUs == 0)
        {
            throw new VirtualMachineTemplateException("Memory information is not available");
        }
                
        ArrayList<Double> resourceRequirements = MathUtils.createCustomVector(numberOfVCPUs, 
                                                                              memorySize, 
                                                                              networkCapacity);       
        return resourceRequirements;
    }
    
    /**
     * Returns information from tag.
     * 
     * @param root          The root element
     * @param tag           The tag
     * @return              The information
     */
    private String getInformation(Element root, String tag) 
    {
        Guard.check(root, tag);
        NodeList nodes = root.getElementsByTagName(tag);
        
        Element element = (Element) nodes.item(0);
        String information = getDataFromElement(element);
        
        return information;
    }
     
    /**
     * Retrieves data from element.
     * 
     * @param element       The element
     * @return              The data
     */
    private String getDataFromElement(Element element) 
    {
        Guard.check(element);
        Node child = element.getFirstChild();

        if (child instanceof CharacterData) 
        {
            CharacterData characterData = (CharacterData) child;
            return characterData.getData();
        }
        
        return null;
    }

    @Override
    public List<String> getNetworkInterfaces(String xmlDesc) throws VirtualClusterParserException 
    {
        Guard.check(xmlDesc);   
        List<String> networkInterfaces = new ArrayList<String>();
        try 
        {  
            Document doc = VirtualClusterParserUtils.stringToDom(xmlDesc);
            NodeList nodes = doc.getElementsByTagName("interface");
            for (int i = 0; i < nodes.getLength(); i++) 
            {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element element = (Element) node;
                    NodeList snodes = element.getElementsByTagName("target");
                    
                    for (int j = 0; j < snodes.getLength(); j++)
                    {
                        Node snode = snodes.item(j);                   
                        if (snode.getNodeType() == Node.ELEMENT_NODE)
                        {
                            networkInterfaces.add(((Element) snode).getAttribute("dev"));                    
                        }
                    }
                }
            }
            return networkInterfaces;
        }
        catch (Exception exception)
        { 
            throw new VirtualClusterParserException(String.format("Unable to get network interface for XML : %s",
                exception.getMessage()));
        }
    }

    
    /**
     * Gets the MAC of the libvirt template.
     * 
     * Note: return the first one found in the template
     * 
     * @param xmlDescription      The template
     * @return                    The mac address
     */
    public String getMacAddress(String xmlDescription)
    {
        Guard.check(xmlDescription);
        String finalMacAddress = null;
        
        Document doc = VirtualClusterParserUtils.stringToDom(xmlDescription);
        
        NodeList nodes = doc.getElementsByTagName("mac");
        if (nodes.getLength() > 0 && nodes.item(0).getNodeType() == Node.ELEMENT_NODE)
        {
            Element element = (Element) nodes.item(0);
            finalMacAddress = element.getAttribute("address");
            
        }
        return finalMacAddress;
    }
    
   
    
    /**
     * Replaces the MAC address inside libvirt template.
     * 
     * @param xmlDescription          The template
     * @param newMacAddress           The new mac address
     * @return                        Modified template string
     */
    public String replaceMacAddressInTemplate(String xmlDescription, String newMacAddress)
    {
        Guard.check(xmlDescription, newMacAddress);        
        log_.debug("Replacing MAC address in the libvirt template with: " + newMacAddress);
       
        Document doc = VirtualClusterParserUtils.stringToDom(xmlDescription);
        
        NodeList nodes = doc.getElementsByTagName("mac");
        if (nodes.getLength() > 0 && nodes.item(0).getNodeType() == Node.ELEMENT_NODE)
        {
            Element element = (Element) nodes.item(0);
            element.setAttribute("address", newMacAddress);
            
        }
        
        String newTemplate = VirtualClusterParserUtils.domToString(doc);
        
        return newTemplate;
    }
        
    
    /**
     *  Handle the Resize request.
     * 
     * @param xmlDescription    template
     * @param resizeRequest     the resize request
     * @return                  the new xml description of the domain
     */
    public String handleResizeRequest(String xmlDescription, ResizeRequest resizeRequest)
    {
        Guard.check(xmlDescription, resizeRequest);
        
        String newTemplate = xmlDescription;
        try
        {
            int vcpu = new Double(resizeRequest.getResizedCapacity().get(0)).intValue();
            int memory = new Double(resizeRequest.getResizedCapacity().get(1)).intValue();
            int tx = new Double(resizeRequest.getResizedCapacity().get(2)).intValue();
            int  rx = new Double(resizeRequest.getResizedCapacity().get(3)).intValue();
            log_.debug("Modifying the libvirt to handle the resize request");
            log_.debug("requested vcpu : " + vcpu);
            log_.debug("requested memory : " + memory);
            log_.debug("requested tx : " + tx);
            log_.debug("requested rx : " + rx);
            
            Document doc = VirtualClusterParserUtils.stringToDom(xmlDescription);
            
            
            NodeList nodes = doc.getElementsByTagName("vcpu");
            if (nodes.getLength() > 0 && nodes.item(0).getNodeType() == Node.ELEMENT_NODE && vcpu > 0)
            {
                Node node = nodes.item(0);
                node = node.getFirstChild();
                node.setNodeValue(String.valueOf(vcpu));
            }
            nodes = doc.getElementsByTagName("memory");
            if (nodes.getLength() > 0 && nodes.item(0).getNodeType() == Node.ELEMENT_NODE  && memory > 0)
            {
                Node node = nodes.item(0);
                node = node.getFirstChild();
                node.setNodeValue(String.valueOf(memory));
            }
    
            newTemplate = VirtualClusterParserUtils.domToString(doc);
            return newTemplate;
        }
        catch (Exception e)
        {
            log_.error("error while modifyong the template");
            return newTemplate;
        }
        
        
    }


    @Override
    public String addDiskImage(String xmlDesc, VirtualMachineImage image, String bus, String dev) 
    {
        log_.debug("Adding a disk is not implemented for this parser");
        return null;
    }

    @Override
    public String addSerial(String xmlDescription, String type, String targetPort) 
    {
        log_.debug("Adding a serial is not implemented for this parser");
        return xmlDescription;
    }

    @Override
    public String addConsole(String xmlDescription, String type,
            String targetPort, String targetType) {
        log_.debug("Adding a console is not implemented for this parser");
        return xmlDescription;
    }

    @Override
    public String setDomainType(String xmlDescription, HypervisorDriver driver) 
    {
        log_.debug("Adding a domain type is not implemented for this parser");
        return xmlDescription;
    }

    @Override
    public String setOsType(String xmlDescription, HypervisorDriver driver) 
    {
        log_.debug("Adding an os type is not implemented for this parser");
        return xmlDescription;
    }

    
    @Override
    public String addCdRomImage(String xmlDescription, String path, String bus, String dev) 
    {
        log_.debug("Adding an os type is not implemented for this parser");
        return xmlDescription;
    }

    @Override
    public VirtualMachineImage getFirstDiskImage(String xmlDescription) 
    {
        log_.debug("Getting the first disk image isn't implemented for this parser.");
        return null;
    }

    @Override
    public String removeDisk(String xmlRepresentation, String name) {
        log_.debug("Removing the disk image isn't implemented for this parser.");
        return null;
    }

    @Override
    public String addGraphics(String xmlDescription, String type, String address, String port, String keymap)
    {
        log_.debug("Adding graphics isn't implemented for this parser.");
        return null;
    }
}

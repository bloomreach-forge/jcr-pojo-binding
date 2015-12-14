/*
 *  Copyright 2015-2015 Hippo B.V. (http://www.onehippo.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.onehippo.forge.content.pojo.model;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.hippoecm.repository.HippoStdNodeType;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ContentNodeTest {

    private static Logger log = LoggerFactory.getLogger(ContentNodeTest.class);

    private ContentNode handleNode;
    private ContentNode liveNews1;
    private ContentNode previewNews1;

    @Before
    public void setUp() throws Exception {
        liveNews1 = new ContentNode("news1", "myhippoproject:news");
        liveNews1.addMixinType("myhippoproject:taggable");
        liveNews1.addMixinType("myhippoproject:classifiable");

        ContentProperty contentProp = new ContentProperty(HippoStdNodeType.HIPPOSTD_STATE, ContentPropertyType.STRING);
        contentProp.addValue(HippoStdNodeType.PUBLISHED);
        liveNews1.setProperty(contentProp);

        contentProp = new ContentProperty("myhippoproject:title", ContentPropertyType.STRING);
        contentProp.addValue("News 1");
        liveNews1.setProperty(contentProp);

        contentProp = new ContentProperty("myhippoproject:summary", ContentPropertyType.STRING);
        contentProp.addValue("Summary of News 1");
        liveNews1.setProperty(contentProp);

        contentProp = new ContentProperty("myhippoproject:date", ContentPropertyType.DATE);
        contentProp.addValue("2015-12-01T12:34:56-05:00");
        liveNews1.setProperty(contentProp);

        ContentNode bodyNode = new ContentNode("myhippoproject:body", "hippostd:html");
        contentProp = new ContentProperty("hippostd:content", ContentPropertyType.STRING);
        contentProp.addValue("<p>Hello, World!</p>");
        bodyNode.setProperty(contentProp);
        liveNews1.addNode(bodyNode);

        ContentNode mirrorNode = new ContentNode("myhippoproject:related", "hippo:mirror");
        contentProp = new ContentProperty("hippo:docbase", ContentPropertyType.STRING);
        contentProp.addValue("00000000-0000-0000-0000-000000000001");
        mirrorNode.setProperty(contentProp);
        liveNews1.addNode(mirrorNode);

        mirrorNode = new ContentNode("myhippoproject:related", "hippo:mirror");
        contentProp = new ContentProperty("hippo:docbase", ContentPropertyType.STRING);
        contentProp.addValue("00000000-0000-0000-0000-000000000002");
        mirrorNode.setProperty(contentProp);
        liveNews1.addNode(mirrorNode);

        previewNews1 = (ContentNode) liveNews1.clone();
        previewNews1.getProperty(HippoStdNodeType.HIPPOSTD_STATE).setValue(HippoStdNodeType.UNPUBLISHED);

        handleNode = new ContentNode(liveNews1.getName(), "hippo:handle");
        handleNode.addNode(liveNews1);
        handleNode.addNode(previewNews1);
    }

    @Test
    public void testQueryByXPath() throws Exception {
        assertEquals(liveNews1, handleNode.queryObjectByXPath("nodes[@primaryType='myhippoproject:news'][1]"));
        assertEquals(liveNews1, handleNode.queryObjectByXPath("nodes[@mixinTypes='myhippoproject:taggable'][1]"));
        assertEquals(liveNews1, handleNode.queryObjectByXPath("nodes[@mixinTypes='myhippoproject:classifiable'][1]"));
        assertEquals(liveNews1, handleNode.queryObjectByXPath("nodes[properties[@itemName='hippostd:state']/value='published']"));

        assertEquals(previewNews1, handleNode.queryObjectByXPath("nodes[@primaryType='myhippoproject:news'][2]"));
        assertEquals(previewNews1, handleNode.queryObjectByXPath("nodes[@mixinTypes='myhippoproject:taggable'][2]"));
        assertEquals(previewNews1, handleNode.queryObjectByXPath("nodes[@mixinTypes='myhippoproject:classifiable'][2]"));
        assertEquals(previewNews1, handleNode.queryObjectByXPath("nodes[properties[@itemName='hippostd:state']/value='unpublished']"));

        ContentProperty contentProp = (ContentProperty) liveNews1.queryObjectByXPath("properties[@itemName='myhippoproject:title']");
        assertEquals("News 1", contentProp.getValue());

        contentProp = (ContentProperty) liveNews1.queryObjectByXPath("properties[@itemName='myhippoproject:summary']");
        assertEquals("Summary of News 1", contentProp.getValue());

        contentProp = (ContentProperty) liveNews1.queryObjectByXPath("properties[@itemName='myhippoproject:date']");
        assertEquals("2015-12-01T12:34:56-05:00", contentProp.getValue());

        ContentNode contentNode = (ContentNode) liveNews1.queryObjectByXPath("nodes[1]");
        assertEquals("myhippoproject:body", contentNode.getName());
        assertEquals(liveNews1.queryObjectByXPath("nodes[1]"), liveNews1.queryObjectByXPath("nodes[@primaryType='hippostd:html']"));
        assertEquals(liveNews1.queryObjectByXPath("nodes[1]"), liveNews1.queryObjectByXPath("nodes[@itemName='myhippoproject:body']"));

        contentProp = (ContentProperty) liveNews1.queryObjectByXPath("nodes[1]/properties[@itemName='hippostd:content']");
        assertEquals("<p>Hello, World!</p>", contentProp.getValue());

        String content = (String) liveNews1.queryObjectByXPath("nodes[1]/properties[@itemName='hippostd:content']/value");
        assertEquals("<p>Hello, World!</p>", content);

        contentNode = (ContentNode) liveNews1.queryObjectByXPath("nodes[2]");
        assertEquals("myhippoproject:related", contentNode.getName());
        assertEquals(liveNews1.queryObjectByXPath("nodes[2]"), liveNews1.queryObjectByXPath("nodes[@primaryType='hippo:mirror']"));
        assertEquals(liveNews1.queryObjectByXPath("nodes[2]"), liveNews1.queryObjectByXPath("nodes[@itemName='myhippoproject:related']"));

        contentNode = (ContentNode) liveNews1.queryObjectByXPath("nodes[3]");
        assertEquals("myhippoproject:related", contentNode.getName());
        assertEquals(liveNews1.queryObjectByXPath("nodes[3]"), liveNews1.queryObjectByXPath("nodes[@primaryType='hippo:mirror'][2]"));
        assertEquals(liveNews1.queryObjectByXPath("nodes[3]"), liveNews1.queryObjectByXPath("nodes[@itemName='myhippoproject:related'][2]"));
    }

    @Test
    public void testDocumentContentSerializationInJSON() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(liveNews1);
        log.debug("jsonString from liveNews1: {}", jsonString);

        jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(previewNews1);
        log.debug("jsonString from previewNews1: {}", jsonString);

        ContentNode documentContent = mapper.readValue(jsonString, ContentNode.class);
        log.debug("documentContent: {}", documentContent);

        jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(documentContent);
        log.debug("jsonString from documentContent: {}", jsonString);
    }

    @Test
    public void testDocumentContentSerializationInJAXB() throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(ContentNode.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter sw = new StringWriter();
        jaxbMarshaller.marshal(liveNews1, sw);
        String xmlString = sw.toString();
        log.debug("xmlString from liveNews1: {}", xmlString);

        sw = new StringWriter();
        jaxbMarshaller.marshal(previewNews1, sw);
        xmlString = sw.toString();
        log.debug("xmlString from previewNews1: {}", xmlString);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        ContentNode documentContent = (ContentNode) jaxbUnmarshaller.unmarshal(new StringReader(xmlString));
        log.debug("documentContent: {}", documentContent);

        sw = new StringWriter();
        jaxbMarshaller.marshal(documentContent, sw);
        xmlString = sw.toString();
        log.debug("xmlString from documentContent: {}", xmlString);
    }

    @Test
    public void testSameDocumentContentSerializationInEitherJsonOrJAXB() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(liveNews1);
        log.debug("jsonString: {}", jsonString);
        ContentNode documentContentFromJSON = mapper.readValue(jsonString, ContentNode.class);

        JAXBContext jaxbContext = JAXBContext.newInstance(ContentNode.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter sw = new StringWriter();
        jaxbMarshaller.marshal(liveNews1, sw);
        String xmlString = sw.toString();
        log.debug("xmlString: {}", xmlString);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        ContentNode documentContentFromJaxb = (ContentNode) jaxbUnmarshaller.unmarshal(new StringReader(xmlString));

        assertEquals(documentContentFromJSON, documentContentFromJaxb);
    }

}

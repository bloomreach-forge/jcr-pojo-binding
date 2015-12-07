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

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SerializeContentNodeTest {

    private static Logger log = LoggerFactory.getLogger(SerializeContentNodeTest.class);

    private ContentNode liveNews1;
    private ContentNode previewNews1;

    @Before
    public void setUp() throws Exception {
        liveNews1 = new ContentNode();
        liveNews1.setPrimaryType("myhippoproject:news");
        liveNews1.addMixinType("myhippoproject:taggable");
        liveNews1.addMixinType("myhippoproject:classifiable");
        liveNews1.setName("news1");

        ContentProperty contentProp = new ContentProperty();
        contentProp.setName("myhippoproject:title");
        contentProp.setType(ContentPropertyType.STRING);
        contentProp.addValue("News 1");
        liveNews1.addProperty(contentProp);

        contentProp = new ContentProperty();
        contentProp.setName("myhippoproject:summary");
        contentProp.setType(ContentPropertyType.STRING);
        contentProp.addValue("Summary of News 1");
        liveNews1.addProperty(contentProp);

        contentProp = new ContentProperty();
        contentProp.setName("myhippoproject:date");
        contentProp.setType(ContentPropertyType.DATE);
        contentProp.addValue("2015-12-01T12:34:56-05:00");
        liveNews1.addProperty(contentProp);

        ContentNode bodyNode = new ContentNode();
        bodyNode.setName("myhippoproject:body");
        bodyNode.setPrimaryType("hippostd:html");
        contentProp = new ContentProperty();
        contentProp.setName("hippo:content");
        contentProp.setType(ContentPropertyType.STRING);
        contentProp.addValue("<p>Hello, World!</p>");
        bodyNode.addProperty(contentProp);
        liveNews1.addNode(bodyNode);

        previewNews1 = (ContentNode) liveNews1.clone();
    }

    @Test
    public void testDocumentContentSerialization() throws Exception {
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

}

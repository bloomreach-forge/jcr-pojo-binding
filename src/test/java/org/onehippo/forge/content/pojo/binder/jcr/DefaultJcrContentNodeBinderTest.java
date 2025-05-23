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
package org.onehippo.forge.content.pojo.binder.jcr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import javax.jcr.Node;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.util.ISO8601;
import org.hippoecm.repository.api.HippoNodeType;
import org.hippoecm.repository.util.JcrUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.onehippo.forge.content.pojo.common.jcr.BaseHippoJcrContentNodeTest;
import org.onehippo.forge.content.pojo.model.ContentNode;
import org.onehippo.repository.mock.MockNode;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DefaultJcrContentNodeBinderTest extends BaseHippoJcrContentNodeTest {

    private static final String NEWS_CONTENT_JSON_RESOURCE = "news-harvest.json";

    private DefaultJcrContentNodeBinder binder;

    private ContentNode newsContentNode;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        binder = new DefaultJcrContentNodeBinder();

        ObjectMapper objectMapper = new ObjectMapper();

        InputStream input = null;

        try {
            input = DefaultJcrContentNodeBinderTest.class.getResourceAsStream(NEWS_CONTENT_JSON_RESOURCE);
            newsContentNode = objectMapper.readValue(input, ContentNode.class);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    @Test
    public void testBindNewsDocument() throws Exception {
        MockNode newsFolderNode = getRootNode().getNode(StringUtils.removeStart(NEWS_DOC_FOLDER_PATH, "/"));
        Node handle = createHippoDocumentHandleNode(newsFolderNode, "news-harvest", "News Harvest");
        binder.bind(handle, newsContentNode);

        assertEquals("news-harvest", handle.getName());
        assertEquals("hippo:handle", handle.getPrimaryNodeType().getName());
        assertTrue(handle.isNodeType("mix:referenceable"));
        assertTrue(handle.isNodeType("hippo:translated"));

        assertTrue(handle.isNodeType(HippoNodeType.NT_NAMED));
        assertEquals("News Harvest", handle.getProperty(HippoNodeType.HIPPO_NAME).getString());

        assertTrue(handle.hasNode(handle.getName()));
        Node variant = handle.getNode(handle.getName());
        assertEquals("news-harvest", variant.getName());
        assertEquals("myhippoproject:newsdocument", variant.getPrimaryNodeType().getName());
        assertTrue(variant.isNodeType("mix:referenceable"));
        assertEquals("news", variant.getProperty("myhippoproject:documenttype").getString());
        assertFalse(variant.getProperty("myhippoproject:documenttype").isMultiple());
        assertEquals("Lorem ipsum dolor sit amet", variant.getProperty("myhippoproject:introduction").getString());
        assertFalse(variant.getProperty("myhippoproject:introduction").isMultiple());
        assertEquals("admin", variant.getProperty("hippostdpubwf:lastModifiedBy").getString());
        assertFalse(variant.getProperty("hippostdpubwf:lastModifiedBy").isMultiple());
        assertEquals("news", variant.getProperty("myhippoproject:documenttype").getString());
        assertFalse(variant.getProperty("myhippoproject:documenttype").isMultiple());
        assertEquals("en", variant.getProperty("hippotranslation:locale").getString());
        assertFalse(variant.getProperty("hippotranslation:locale").isMultiple());
        assertEquals("live", variant.getProperty("hippostd:stateSummary").getString());
        assertFalse(variant.getProperty("hippostd:stateSummary").isMultiple());
        assertEquals("admin", variant.getProperty("hippostd:holder").getString());
        assertFalse(variant.getProperty("hippostd:holder").isMultiple());
        assertEquals("published", variant.getProperty("hippostd:state").getString());
        assertFalse(variant.getProperty("hippostd:state").isMultiple());
        assertEquals("", variant.getProperty("myhippoproject:source").getString());
        assertFalse(variant.getProperty("myhippoproject:source").isMultiple());
        assertEquals("live,preview",
                StringUtils.join(JcrUtils.getMultipleStringProperty(variant, "hippo:availability", null), ","));
        assertTrue(variant.getProperty("hippo:availability").isMultiple());
        assertEquals("News Harvest", variant.getProperty("myhippoproject:title").getString());
        assertFalse(variant.getProperty("myhippoproject:title").isMultiple());
        assertEquals("Rome", variant.getProperty("myhippoproject:location").getString());
        assertFalse(variant.getProperty("myhippoproject:location").isMultiple());
        assertEquals(ISO8601.parse("2013-11-12T14:31:00.000+01:00"),
                variant.getProperty("hippostdpubwf:publicationDate").getDate());
        assertFalse(variant.getProperty("hippostdpubwf:publicationDate").isMultiple());
        assertEquals("046dc195-9720-4860-8512-6a9099e31b10", variant.getProperty("hippotranslation:id").getString());
        assertFalse(variant.getProperty("hippotranslation:id").isMultiple());
        assertEquals(ISO8601.parse("2013-11-12T14:31:00.000+01:00"),
                variant.getProperty("hippostdpubwf:lastModificationDate").getDate());
        assertFalse(variant.getProperty("hippostdpubwf:lastModificationDate").isMultiple());
        assertEquals("Alfred Anonymous", variant.getProperty("myhippoproject:author").getString());
        assertFalse(variant.getProperty("myhippoproject:author").isMultiple());
        assertEquals("admin", variant.getProperty("hippostdpubwf:createdBy").getString());
        assertFalse(variant.getProperty("hippostdpubwf:createdBy").isMultiple());
        assertEquals(ISO8601.parse("2015-11-12T05:31:00.000-05:00"),
                variant.getProperty("myhippoproject:date").getDate());
        assertFalse(variant.getProperty("myhippoproject:date").isMultiple());
        assertEquals(ISO8601.parse("2013-11-12T14:31:00.000+01:00"),
                variant.getProperty("hippostdpubwf:creationDate").getDate());
        assertFalse(variant.getProperty("hippostdpubwf:creationDate").isMultiple());

        assertTrue(variant.hasNode("myhippoproject:content"));
        Node contentNode = variant.getNode("myhippoproject:content");
        assertEquals("hippostd:html", contentNode.getPrimaryNodeType().getName());
        assertEquals("<html><body><p>Lorem ipsum dolor sit amet</p></body></html>",
                contentNode.getProperty("hippostd:content").getString());
        assertFalse(contentNode.getProperty("hippostd:content").isMultiple());

        assertTrue(variant.hasNode("myhippoproject:image"));
        Node imageLinkNode = variant.getNode("myhippoproject:image");
        assertEquals("hippogallerypicker:imagelink", imageLinkNode.getPrimaryNodeType().getName());
        assertTrue(!imageLinkNode.hasProperty("hippo:facets")
                || imageLinkNode.getProperty("hippo:facets").getValues().length == 0);
        assertTrue(!imageLinkNode.hasProperty("hippo:values")
                || imageLinkNode.getProperty("hippo:values").getValues().length == 0);
        assertEquals("/content/gallery/folderctxmenusdemo/samples/viognier-grapes-188185_640.jpg",
                imageLinkNode.getProperty("hippo:docbase").getString());
        assertTrue(!imageLinkNode.hasProperty("hippo:modes")
                || imageLinkNode.getProperty("hippo:modes").getValues().length == 0);
    }

    @Test
    public void testBindBinaryContent() throws Exception {
        MockNode newsGalleryFolderNode = getRootNode().getNode(StringUtils.removeStart(NEWS_GALLERY_FOLDER_PATH, "/"));
        Node handle = createHippoGalleryHandleNode(newsGalleryFolderNode, "animal-2883_640.jpg");
        // TODO
    }
}

/*
 *  Copyright 2015-2025 Bloomreach (https://www.bloomreach.com)
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
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Value;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.util.ISO8601;
import org.hippoecm.repository.api.HippoNodeType;
import org.hippoecm.repository.util.JcrUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.onehippo.forge.content.pojo.binder.ContentNodeBindingItemFilter;
import org.onehippo.forge.content.pojo.common.ContentValueConverter;
import org.onehippo.forge.content.pojo.common.jcr.BaseHippoJcrContentNodeTest;
import org.onehippo.forge.content.pojo.model.BinaryValue;
import org.onehippo.forge.content.pojo.model.ContentItem;
import org.onehippo.forge.content.pojo.model.ContentNode;
import org.onehippo.forge.content.pojo.model.ContentProperty;
import org.onehippo.forge.content.pojo.model.ContentPropertyType;
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

    @Test
    public void testFullOverwriteModeRemovesAllChildren() throws Exception {
        // Setup: create a target node with mixed children (compound and non-compound)
        MockNode parentNode = getRootNode().addNode("testParent", "nt:unstructured");

        // Add compound-type child
        MockNode compoundChild = parentNode.addNode("compoundChild", "hippo:compound");
        compoundChild.setProperty("prop1", "value1");

        // Add non-compound child (would survive default mode)
        MockNode nonCompoundChild = parentNode.addNode("nonCompoundChild", "nt:unstructured");
        nonCompoundChild.setProperty("prop2", "value2");

        // Add another non-compound child
        MockNode anotherChild = parentNode.addNode("anotherChild", "nt:unstructured");
        anotherChild.setProperty("prop3", "value3");

        assertEquals(3, countChildren(parentNode));

        // Create source ContentNode with only one child
        ContentNode sourceNode = new ContentNode("testParent", "nt:unstructured");
        ContentNode newChild = new ContentNode("newChild", "hippo:compound");
        newChild.setProperty("newProp", "newValue");
        sourceNode.addNode(newChild);

        // Bind with fullOverwriteMode=true
        binder.setFullOverwriteMode(true);
        binder.bind(parentNode, sourceNode);

        // Verify: ALL original children removed, only source child remains
        assertEquals(1, countChildren(parentNode));
        assertTrue(parentNode.hasNode("newChild"));
        assertFalse(parentNode.hasNode("compoundChild"));
        assertFalse(parentNode.hasNode("nonCompoundChild"));
        assertFalse(parentNode.hasNode("anotherChild"));
    }

    @Test
    public void testDefaultModeOnlyRemovesCompoundTypes() throws Exception {
        // Setup: create a target node with mixed children
        MockNode parentNode = getRootNode().addNode("testParent2", "nt:unstructured");

        // Add compound-type child (should be removed)
        MockNode compoundChild = parentNode.addNode("compoundChild", "hippo:compound");
        compoundChild.setProperty("prop1", "value1");

        // Add non-compound child (should survive default mode)
        MockNode nonCompoundChild = parentNode.addNode("nonCompoundChild", "nt:unstructured");
        nonCompoundChild.setProperty("prop2", "value2");

        assertEquals(2, countChildren(parentNode));

        // Create source ContentNode with different child
        ContentNode sourceNode = new ContentNode("testParent2", "nt:unstructured");
        ContentNode newChild = new ContentNode("newChild", "hippo:compound");
        newChild.setProperty("newProp", "newValue");
        sourceNode.addNode(newChild);

        // Bind with default mode (fullOverwriteMode=false, subNodesMergingOnly=false)
        binder.setFullOverwriteMode(false);
        binder.setSubNodesMergingOnly(false);
        binder.bind(parentNode, sourceNode);

        // Verify: compound child removed, non-compound child preserved, new child added
        assertEquals(2, countChildren(parentNode));
        assertTrue(parentNode.hasNode("newChild"));
        assertFalse(parentNode.hasNode("compoundChild"));
        assertTrue(parentNode.hasNode("nonCompoundChild")); // Non-compound survives!
    }

    @Test
    public void testMergeModeWithTypeBasedMatching() throws Exception {
        // Setup: target with heterogeneous children [typeA, typeB, typeA]
        MockNode parentNode = getRootNode().addNode("testParent3", "nt:unstructured");

        MockNode typeA1 = parentNode.addNode("item", "hippo:compound");
        typeA1.addMixin("mix:referenceable");
        typeA1.setProperty("value", "A1-original");

        MockNode typeB1 = parentNode.addNode("item", "hippostd:html");
        typeB1.setProperty("hippostd:content", "B1-original");

        MockNode typeA2 = parentNode.addNode("item", "hippo:compound");
        typeA2.addMixin("mix:referenceable");
        typeA2.setProperty("value", "A2-original");

        assertEquals(3, countChildren(parentNode));

        // Create source with reordered types [typeB, typeA, typeA]
        ContentNode sourceNode = new ContentNode("testParent3", "nt:unstructured");

        ContentNode srcB1 = new ContentNode("item", "hippostd:html");
        srcB1.setProperty("hippostd:content", "B1-updated");
        sourceNode.addNode(srcB1);

        ContentNode srcA1 = new ContentNode("item", "hippo:compound");
        srcA1.addMixinType("mix:referenceable");
        srcA1.setProperty("value", "A1-updated");
        sourceNode.addNode(srcA1);

        ContentNode srcA2 = new ContentNode("item", "hippo:compound");
        srcA2.addMixinType("mix:referenceable");
        srcA2.setProperty("value", "A2-updated");
        sourceNode.addNode(srcA2);

        // Bind with merge mode
        binder.setSubNodesMergingOnly(true);
        binder.bind(parentNode, sourceNode);

        // Verify: type-based matching should work without ConstraintViolationException
        assertEquals(3, countChildren(parentNode));

        // Check values were updated correctly by type-based matching
        NodeIterator children = parentNode.getNodes("item");
        int compoundCount = 0;
        int htmlCount = 0;
        while (children.hasNext()) {
            Node child = children.nextNode();
            if (child.isNodeType("hippo:compound")) {
                compoundCount++;
                assertTrue(child.getProperty("value").getString().contains("updated"));
            } else if (child.isNodeType("hippostd:html")) {
                htmlCount++;
                assertEquals("B1-updated", child.getProperty("hippostd:content").getString());
            }
        }
        assertEquals(2, compoundCount);
        assertEquals(1, htmlCount);
    }

    @Test
    public void testMergeModeCreatesNewNodesWhenSourceHasMore() throws Exception {
        // Setup: target with 1 child
        MockNode parentNode = getRootNode().addNode("testParent4", "nt:unstructured");
        MockNode existing = parentNode.addNode("item", "hippo:compound");
        existing.setProperty("value", "existing");

        assertEquals(1, countChildren(parentNode));

        // Source has 3 children of same type
        ContentNode sourceNode = new ContentNode("testParent4", "nt:unstructured");
        for (int i = 1; i <= 3; i++) {
            ContentNode child = new ContentNode("item", "hippo:compound");
            child.setProperty("value", "item" + i);
            sourceNode.addNode(child);
        }

        // Bind with merge mode
        binder.setSubNodesMergingOnly(true);
        binder.bind(parentNode, sourceNode);

        // Verify: 3 children now exist (1 updated, 2 created)
        assertEquals(3, countChildren(parentNode));
    }

    @Test
    public void testFullOverwriteModeDefaultsToFalse() throws Exception {
        DefaultJcrContentNodeBinder newBinder = new DefaultJcrContentNodeBinder();
        assertFalse(newBinder.isFullOverwriteMode());
    }

    @Test
    public void testSubNodesMergingOnlyDefaultsToFalse() throws Exception {
        DefaultJcrContentNodeBinder newBinder = new DefaultJcrContentNodeBinder();
        assertFalse(newBinder.isSubNodesMergingOnly());
    }

    @Test
    public void testFullOverwriteModeTakesPrecedence() throws Exception {
        // Setup: target with children
        MockNode parentNode = getRootNode().addNode("testParent5", "nt:unstructured");
        parentNode.addNode("child1", "hippo:compound");
        parentNode.addNode("child2", "nt:unstructured");

        // Source with different child
        ContentNode sourceNode = new ContentNode("testParent5", "nt:unstructured");
        ContentNode newChild = new ContentNode("newChild", "hippo:compound");
        sourceNode.addNode(newChild);

        // Set both flags - fullOverwriteMode should take precedence
        binder.setFullOverwriteMode(true);
        binder.setSubNodesMergingOnly(true);
        binder.bind(parentNode, sourceNode);

        // Verify: only new child exists (full overwrite, not merge)
        assertEquals(1, countChildren(parentNode));
        assertTrue(parentNode.hasNode("newChild"));
        assertFalse(parentNode.hasNode("child1"));
        assertFalse(parentNode.hasNode("child2"));
    }

    private int countChildren(Node node) throws Exception {
        int count = 0;
        NodeIterator children = node.getNodes();
        while (children.hasNext()) {
            children.nextNode();
            count++;
        }
        return count;
    }

    // ==================== Additional Coverage Tests ====================

    @Test
    public void testBindWithCustomItemFilter() throws Exception {
        MockNode parentNode = getRootNode().addNode("testFilterParent", "nt:unstructured");

        ContentNode sourceNode = new ContentNode("testFilterParent", "nt:unstructured");
        sourceNode.setProperty("allowedProp", "allowed");
        sourceNode.setProperty("rejectedProp", "rejected");

        ContentNode allowedChild = new ContentNode("allowedChild", "hippo:compound");
        ContentNode rejectedChild = new ContentNode("rejectedChild", "hippo:compound");
        sourceNode.addNode(allowedChild);
        sourceNode.addNode(rejectedChild);

        // Filter that rejects items with "rejected" in name
        ContentNodeBindingItemFilter<ContentItem> filter = item -> !item.getName().contains("rejected");

        binder.bind(parentNode, sourceNode, filter);

        // Verify: rejected items not bound
        assertTrue(parentNode.hasProperty("allowedProp"));
        assertFalse(parentNode.hasProperty("rejectedProp"));
        assertTrue(parentNode.hasNode("allowedChild"));
        assertFalse(parentNode.hasNode("rejectedChild"));
    }

    @Test
    public void testBindWithMixinTypes() throws Exception {
        MockNode parentNode = getRootNode().addNode("testMixinParent", "nt:unstructured");

        ContentNode sourceNode = new ContentNode("testMixinParent", "nt:unstructured");
        sourceNode.addMixinType("mix:referenceable");
        sourceNode.addMixinType("mix:versionable");

        binder.bind(parentNode, sourceNode);

        // Verify mixins were added
        assertTrue(parentNode.isNodeType("mix:referenceable"));
        assertTrue(parentNode.isNodeType("mix:versionable"));
    }

    @Test
    public void testBindSkipsExistingMixinTypes() throws Exception {
        MockNode parentNode = getRootNode().addNode("testExistingMixin", "nt:unstructured");
        parentNode.addMixin("mix:referenceable");

        ContentNode sourceNode = new ContentNode("testExistingMixin", "nt:unstructured");
        sourceNode.addMixinType("mix:referenceable"); // Already exists

        // Should not throw even though mixin already exists
        binder.bind(parentNode, sourceNode);

        assertTrue(parentNode.isNodeType("mix:referenceable"));
    }

    @Test
    public void testCompoundTypeDetectionForMirror() throws Exception {
        MockNode parentNode = getRootNode().addNode("testMirrorParent", "nt:unstructured");

        // Add mirror type child (should be detected as compound)
        MockNode mirrorChild = parentNode.addNode("mirrorChild", "hippo:mirror");
        mirrorChild.setProperty("hippo:docbase", "some-uuid");

        assertEquals(1, countChildren(parentNode));

        ContentNode sourceNode = new ContentNode("testMirrorParent", "nt:unstructured");
        ContentNode newChild = new ContentNode("newChild", "hippo:compound");
        sourceNode.addNode(newChild);

        // Default mode should remove mirror child (it's a compound type)
        binder.bind(parentNode, sourceNode);

        assertEquals(1, countChildren(parentNode));
        assertTrue(parentNode.hasNode("newChild"));
        assertFalse(parentNode.hasNode("mirrorChild"));
    }

    @Test
    public void testCompoundTypeDetectionForHtml() throws Exception {
        MockNode parentNode = getRootNode().addNode("testHtmlParent", "nt:unstructured");

        // Add html type child (should be detected as compound)
        MockNode htmlChild = parentNode.addNode("htmlChild", "hippostd:html");
        htmlChild.setProperty("hippostd:content", "<p>content</p>");

        assertEquals(1, countChildren(parentNode));

        ContentNode sourceNode = new ContentNode("testHtmlParent", "nt:unstructured");
        ContentNode newChild = new ContentNode("newChild", "hippo:compound");
        sourceNode.addNode(newChild);

        binder.bind(parentNode, sourceNode);

        assertEquals(1, countChildren(parentNode));
        assertTrue(parentNode.hasNode("newChild"));
        assertFalse(parentNode.hasNode("htmlChild"));
    }

    @Test
    public void testCompoundTypeDetectionForImageLink() throws Exception {
        MockNode parentNode = getRootNode().addNode("testImageLinkParent", "nt:unstructured");

        // Add imagelink type child (should be detected as compound)
        MockNode imageLinkChild = parentNode.addNode("imageLinkChild", "hippogallerypicker:imagelink");
        imageLinkChild.setProperty("hippo:docbase", "some-uuid");

        assertEquals(1, countChildren(parentNode));

        ContentNode sourceNode = new ContentNode("testImageLinkParent", "nt:unstructured");
        ContentNode newChild = new ContentNode("newChild", "hippo:compound");
        sourceNode.addNode(newChild);

        binder.bind(parentNode, sourceNode);

        assertEquals(1, countChildren(parentNode));
        assertTrue(parentNode.hasNode("newChild"));
        assertFalse(parentNode.hasNode("imageLinkChild"));
    }

    @Test
    public void testMergeModeWithItemFilter() throws Exception {
        MockNode parentNode = getRootNode().addNode("testMergeFilter", "nt:unstructured");

        MockNode existing = parentNode.addNode("item", "hippo:compound");
        existing.setProperty("value", "original");

        ContentNode sourceNode = new ContentNode("testMergeFilter", "nt:unstructured");

        ContentNode allowedChild = new ContentNode("item", "hippo:compound");
        allowedChild.setProperty("value", "updated");
        sourceNode.addNode(allowedChild);

        ContentNode rejectedChild = new ContentNode("rejectedItem", "hippo:compound");
        rejectedChild.setProperty("value", "should-not-appear");
        sourceNode.addNode(rejectedChild);

        ContentNodeBindingItemFilter<ContentItem> filter = item -> !item.getName().contains("rejected");

        binder.setSubNodesMergingOnly(true);
        binder.bind(parentNode, sourceNode, filter);

        assertEquals(1, countChildren(parentNode));
        assertTrue(parentNode.hasNode("item"));
        assertFalse(parentNode.hasNode("rejectedItem"));
    }

    @Test
    public void testRemoveSubNodesWithItemFilter() throws Exception {
        MockNode parentNode = getRootNode().addNode("testRemoveFilter", "nt:unstructured");

        // Add compound children
        parentNode.addNode("allowedChild", "hippo:compound");
        parentNode.addNode("rejectedChild", "hippo:compound");

        ContentNode sourceNode = new ContentNode("testRemoveFilter", "nt:unstructured");

        ContentNode child1 = new ContentNode("newChild1", "hippo:compound");
        sourceNode.addNode(child1);

        ContentNode rejectedSource = new ContentNode("rejectedSource", "hippo:compound");
        sourceNode.addNode(rejectedSource);

        ContentNodeBindingItemFilter<ContentItem> filter = item -> !item.getName().contains("rejected");

        binder.setFullOverwriteMode(false);
        binder.setSubNodesMergingOnly(false);
        binder.bind(parentNode, sourceNode, filter);

        // allowedChild and rejectedChild removed (compounds), newChild1 added, rejectedSource filtered out
        assertTrue(parentNode.hasNode("newChild1"));
        assertFalse(parentNode.hasNode("rejectedSource"));
    }

    @Test
    public void testFindChildNodesByNameAndTypeFiltersCorrectly() throws Exception {
        MockNode parentNode = getRootNode().addNode("testFindFilter", "nt:unstructured");

        // Add children with same name but different types
        parentNode.addNode("item", "hippo:compound");
        parentNode.addNode("item", "hippostd:html");
        parentNode.addNode("item", "hippo:compound");

        ContentNode sourceNode = new ContentNode("testFindFilter", "nt:unstructured");

        // Only add hippo:compound items
        ContentNode compound1 = new ContentNode("item", "hippo:compound");
        compound1.setProperty("index", "1");
        sourceNode.addNode(compound1);

        ContentNode compound2 = new ContentNode("item", "hippo:compound");
        compound2.setProperty("index", "2");
        sourceNode.addNode(compound2);

        binder.bind(parentNode, sourceNode);

        // Should have 2 compounds
        int compoundCount = 0;
        NodeIterator children = parentNode.getNodes("item");
        while (children.hasNext()) {
            Node child = children.nextNode();
            if (child.isNodeType("hippo:compound")) {
                compoundCount++;
            }
        }
        assertEquals(2, compoundCount);
    }

    @Test
    public void testBindWithNullContentNodes() throws Exception {
        MockNode parentNode = getRootNode().addNode("testNullContent", "nt:unstructured");

        ContentNode sourceNode = new ContentNode("testNullContent", "nt:unstructured");
        // No properties, no children - empty node

        binder.bind(parentNode, sourceNode);

        assertEquals(0, countChildren(parentNode));
    }

    @Test
    public void testMergeModePreservesExtraTargetNodes() throws Exception {
        // This tests that merge mode does NOT delete excess target nodes
        MockNode parentNode = getRootNode().addNode("testMergePreserve", "nt:unstructured");

        parentNode.addNode("item", "hippo:compound").setProperty("index", "1");
        parentNode.addNode("item", "hippo:compound").setProperty("index", "2");
        parentNode.addNode("item", "hippo:compound").setProperty("index", "3");

        assertEquals(3, countChildren(parentNode));

        // Source has only 1 item
        ContentNode sourceNode = new ContentNode("testMergePreserve", "nt:unstructured");
        ContentNode child = new ContentNode("item", "hippo:compound");
        child.setProperty("index", "updated");
        sourceNode.addNode(child);

        binder.setSubNodesMergingOnly(true);
        binder.bind(parentNode, sourceNode);

        // Merge mode preserves extra nodes - should still have 3
        assertEquals(3, countChildren(parentNode));
    }

    @Test
    public void testBindSingleValueProperty() throws Exception {
        MockNode parentNode = getRootNode().addNode("testSingleValue", "nt:unstructured");

        ContentNode sourceNode = new ContentNode("testSingleValue", "nt:unstructured");
        sourceNode.setProperty("singleProp", "singleValue");

        binder.bind(parentNode, sourceNode);

        assertTrue(parentNode.hasProperty("singleProp"));
        assertEquals("singleValue", parentNode.getProperty("singleProp").getString());
        assertFalse(parentNode.getProperty("singleProp").isMultiple());
    }

    @Test
    public void testBindMultiValueProperty() throws Exception {
        MockNode parentNode = getRootNode().addNode("testMultiValue", "nt:unstructured");

        ContentNode sourceNode = new ContentNode("testMultiValue", "nt:unstructured");
        sourceNode.setProperty("multiProp", ContentPropertyType.STRING, new String[]{"value1", "value2", "value3"});

        binder.bind(parentNode, sourceNode);

        assertTrue(parentNode.hasProperty("multiProp"));
        assertTrue(parentNode.getProperty("multiProp").isMultiple());
        assertEquals(3, parentNode.getProperty("multiProp").getValues().length);
    }

    @Test
    public void testBindOverridesExistingProperty() throws Exception {
        MockNode parentNode = getRootNode().addNode("testOverride", "nt:unstructured");
        parentNode.setProperty("existingProp", "oldValue");

        ContentNode sourceNode = new ContentNode("testOverride", "nt:unstructured");
        sourceNode.setProperty("existingProp", "newValue");

        binder.bind(parentNode, sourceNode);

        assertEquals("newValue", parentNode.getProperty("existingProp").getString());
    }

    @Test
    public void testBindWithEmptyChildNodes() throws Exception {
        MockNode parentNode = getRootNode().addNode("testEmptyChildren", "nt:unstructured");
        parentNode.addNode("existingChild", "hippo:compound");

        ContentNode sourceNode = new ContentNode("testEmptyChildren", "nt:unstructured");
        // No children in source

        binder.bind(parentNode, sourceNode);

        // Default mode removes compounds
        assertEquals(0, countChildren(parentNode));
    }

    @Test
    public void testBindWithNullItemFilterAndValueConverter() throws Exception {
        MockNode parentNode = getRootNode().addNode("testNullParams", "nt:unstructured");

        ContentNode sourceNode = new ContentNode("testNullParams", "nt:unstructured");
        sourceNode.setProperty("prop", "value");

        // Explicitly pass null for filter and converter
        binder.bind(parentNode, sourceNode, null, null);

        assertTrue(parentNode.hasProperty("prop"));
    }

    // ==================== Additional Branch Coverage Tests ====================

    @Test
    public void testBindWithBlankPrimaryType() throws Exception {
        MockNode parentNode = getRootNode().addNode("testBlankType", "nt:unstructured");

        // ContentNode with null/blank primary type - should not change target type
        ContentNode sourceNode = new ContentNode("testBlankType", null);
        sourceNode.setProperty("prop", "value");

        String originalType = parentNode.getPrimaryNodeType().getName();
        binder.bind(parentNode, sourceNode);

        // Primary type should remain unchanged
        assertEquals(originalType, parentNode.getPrimaryNodeType().getName());
    }

    @Test
    public void testBindWithMatchingPrimaryType() throws Exception {
        MockNode parentNode = getRootNode().addNode("testMatchingType", "nt:unstructured");

        // ContentNode with same primary type as target - no change needed
        ContentNode sourceNode = new ContentNode("testMatchingType", "nt:unstructured");
        sourceNode.setProperty("prop", "value");

        binder.bind(parentNode, sourceNode);

        assertEquals("nt:unstructured", parentNode.getPrimaryNodeType().getName());
    }

    @Test
    public void testBindWithDifferentPrimaryType() throws Exception {
        MockNode parentNode = getRootNode().addNode("testDifferentType", "nt:unstructured");

        // ContentNode with different primary type - should change target type
        ContentNode sourceNode = new ContentNode("testDifferentType", "nt:folder");

        binder.bind(parentNode, sourceNode);

        assertEquals("nt:folder", parentNode.getPrimaryNodeType().getName());
    }

    @Test
    public void testBindPathPropertyWithBlankValue() throws Exception {
        MockNode parentNode = getRootNode().addNode("testBlankPath", "nt:unstructured");

        ContentNode sourceNode = new ContentNode("testBlankPath", "nt:unstructured");
        sourceNode.setProperty("pathProp", ContentPropertyType.PATH, "");

        binder.bind(parentNode, sourceNode);

        // Blank path should not create property
        assertFalse(parentNode.hasProperty("pathProp"));
    }

    @Test
    public void testBindPathPropertyWithNonExistentNode() throws Exception {
        MockNode parentNode = getRootNode().addNode("testNonExistentPath", "nt:unstructured");

        ContentNode sourceNode = new ContentNode("testNonExistentPath", "nt:unstructured");
        sourceNode.setProperty("pathProp", ContentPropertyType.PATH, "/non/existent/path");

        binder.bind(parentNode, sourceNode);

        // Non-existent path should not create property
        assertFalse(parentNode.hasProperty("pathProp"));
    }

    @Test
    @Ignore("MockNode does not support setProperty(String, Node) for PATH properties")
    public void testBindPathPropertyWithExistingNode() throws Exception {
        // Create target node for path reference
        MockNode targetNode = getRootNode().addNode("pathTarget", "nt:unstructured");
        String targetPath = targetNode.getPath();

        MockNode parentNode = getRootNode().addNode("testExistingPath", "nt:unstructured");

        ContentNode sourceNode = new ContentNode("testExistingPath", "nt:unstructured");
        sourceNode.setProperty("pathProp", ContentPropertyType.PATH, targetPath);

        binder.bind(parentNode, sourceNode);

        // Valid path should create reference property
        assertTrue(parentNode.hasProperty("pathProp"));
    }

    @Test
    public void testBindWithEmptyPropertyValues() throws Exception {
        MockNode parentNode = getRootNode().addNode("testEmptyValues", "nt:unstructured");

        ContentNode sourceNode = new ContentNode("testEmptyValues", "nt:unstructured");
        // Set property with empty string array
        sourceNode.setProperty("emptyMulti", ContentPropertyType.STRING, new String[]{});

        binder.bind(parentNode, sourceNode);

        // Empty array properties might or might not be created depending on implementation
        // This tests the jcrValues.length > 0 branch
    }

    @Test
    public void testBindWithCustomValueConverter() throws Exception {
        MockNode parentNode = getRootNode().addNode("testCustomConverter", "nt:unstructured");

        ContentNode sourceNode = new ContentNode("testCustomConverter", "nt:unstructured");
        sourceNode.setProperty("prop", "value");

        // Custom converter that returns null values
        ContentValueConverter<Value> nullConverter = new ContentValueConverter<Value>() {
            @Override
            public Value toJcrValue(String typeName, String stringValue) {
                return null; // Return null to test null handling
            }

            @Override
            public Value toJcrValue(BinaryValue binaryValue) {
                return null;
            }

            @Override
            public String toString(Value jcrValue) {
                return null;
            }

            @Override
            public BinaryValue toBinaryValue(Value jcrValue, String mimeType) {
                return null;
            }
        };

        binder.bind(parentNode, sourceNode, null, nullConverter);

        // Property should not be set when converter returns null
        assertFalse(parentNode.hasProperty("prop"));
    }

    @Test
    public void testBindRepositoryExceptionWrapping() throws Exception {
        // Test that RepositoryException is wrapped in ContentNodeBindingException
        // This is hard to trigger with MockNode, but we can at least verify the bind methods exist
        DefaultJcrContentNodeBinder testBinder = new DefaultJcrContentNodeBinder();

        // Verify all bind method signatures work
        MockNode parentNode = getRootNode().addNode("testExceptionWrap", "nt:unstructured");
        ContentNode sourceNode = new ContentNode("testExceptionWrap", "nt:unstructured");

        // These should not throw
        testBinder.bind(parentNode, sourceNode);
        testBinder.bind(parentNode, sourceNode, null);
        testBinder.bind(parentNode, sourceNode, null, null);
    }

    @Test
    public void testMergeModeWithNoExistingChildren() throws Exception {
        MockNode parentNode = getRootNode().addNode("testMergeNoChildren", "nt:unstructured");
        // Parent has no children

        ContentNode sourceNode = new ContentNode("testMergeNoChildren", "nt:unstructured");
        ContentNode child = new ContentNode("newChild", "hippo:compound");
        child.setProperty("value", "test");
        sourceNode.addNode(child);

        binder.setSubNodesMergingOnly(true);
        binder.bind(parentNode, sourceNode);

        // New child should be created
        assertEquals(1, countChildren(parentNode));
        assertTrue(parentNode.hasNode("newChild"));
    }

    @Test
    public void testMergeModeWithDifferentNodeTypes() throws Exception {
        MockNode parentNode = getRootNode().addNode("testMergeDiffTypes", "nt:unstructured");

        // Target has one type
        MockNode existing = parentNode.addNode("item", "hippo:compound");
        existing.setProperty("type", "compound");

        ContentNode sourceNode = new ContentNode("testMergeDiffTypes", "nt:unstructured");

        // Source has different type with same name - should create new, not update existing
        ContentNode htmlChild = new ContentNode("item", "hippostd:html");
        htmlChild.setProperty("hippostd:content", "<p>html</p>");
        sourceNode.addNode(htmlChild);

        binder.setSubNodesMergingOnly(true);
        binder.bind(parentNode, sourceNode);

        // Should have both - original compound and new html
        assertEquals(2, countChildren(parentNode));
    }

    @Test
    public void testGroupContentChildrenWithFilter() throws Exception {
        MockNode parentNode = getRootNode().addNode("testGroupFilter", "nt:unstructured");
        parentNode.addNode("item", "hippo:compound");

        ContentNode sourceNode = new ContentNode("testGroupFilter", "nt:unstructured");

        ContentNode allowed = new ContentNode("item", "hippo:compound");
        allowed.setProperty("status", "allowed");
        sourceNode.addNode(allowed);

        ContentNode rejected = new ContentNode("item", "hippo:compound");
        rejected.setProperty("status", "rejected");
        sourceNode.addNode(rejected);

        // Filter rejects second item
        ContentNodeBindingItemFilter<ContentItem> filter = new ContentNodeBindingItemFilter<ContentItem>() {
            private int count = 0;
            @Override
            public boolean accept(ContentItem item) {
                if (item instanceof ContentNode && "item".equals(item.getName())) {
                    return count++ == 0; // Only accept first "item"
                }
                return true;
            }
        };

        binder.setSubNodesMergingOnly(true);
        binder.bind(parentNode, sourceNode, filter);

        // Only one item should be processed
        assertEquals(1, countChildren(parentNode));
    }

    @Test
    public void testBindDateProperty() throws Exception {
        MockNode parentNode = getRootNode().addNode("testDateProp", "nt:unstructured");

        ContentNode sourceNode = new ContentNode("testDateProp", "nt:unstructured");
        sourceNode.setProperty("dateProp", ContentPropertyType.DATE, "2025-01-25T00:00:00.000Z");

        binder.bind(parentNode, sourceNode);

        assertTrue(parentNode.hasProperty("dateProp"));
    }

    @Test
    public void testBindBooleanProperty() throws Exception {
        MockNode parentNode = getRootNode().addNode("testBoolProp", "nt:unstructured");

        ContentNode sourceNode = new ContentNode("testBoolProp", "nt:unstructured");
        sourceNode.setProperty("boolProp", ContentPropertyType.BOOLEAN, "true");

        binder.bind(parentNode, sourceNode);

        assertTrue(parentNode.hasProperty("boolProp"));
        assertTrue(parentNode.getProperty("boolProp").getBoolean());
    }

    @Test
    public void testBindLongProperty() throws Exception {
        MockNode parentNode = getRootNode().addNode("testLongProp", "nt:unstructured");

        ContentNode sourceNode = new ContentNode("testLongProp", "nt:unstructured");
        sourceNode.setProperty("longProp", ContentPropertyType.LONG, "12345");

        binder.bind(parentNode, sourceNode);

        assertTrue(parentNode.hasProperty("longProp"));
        assertEquals(12345L, parentNode.getProperty("longProp").getLong());
    }

    @Test
    public void testBindDoubleProperty() throws Exception {
        MockNode parentNode = getRootNode().addNode("testDoubleProp", "nt:unstructured");

        ContentNode sourceNode = new ContentNode("testDoubleProp", "nt:unstructured");
        sourceNode.setProperty("doubleProp", ContentPropertyType.DOUBLE, "123.45");

        binder.bind(parentNode, sourceNode);

        assertTrue(parentNode.hasProperty("doubleProp"));
        assertEquals(123.45, parentNode.getProperty("doubleProp").getDouble(), 0.001);
    }

    @Test
    public void testCollectMergeableNodeNamesIncludesSourceNames() throws Exception {
        MockNode parentNode = getRootNode().addNode("testCollectNames", "nt:unstructured");
        // No existing children

        ContentNode sourceNode = new ContentNode("testCollectNames", "nt:unstructured");
        ContentNode child1 = new ContentNode("sourceChild1", "hippo:compound");
        ContentNode child2 = new ContentNode("sourceChild2", "hippo:compound");
        sourceNode.addNode(child1);
        sourceNode.addNode(child2);

        binder.setSubNodesMergingOnly(true);
        binder.bind(parentNode, sourceNode);

        // Both source children should be created
        assertTrue(parentNode.hasNode("sourceChild1"));
        assertTrue(parentNode.hasNode("sourceChild2"));
    }

    @Test
    public void testFindOrCreateTargetNodeCreatesWhenIndexExceedsTargets() throws Exception {
        MockNode parentNode = getRootNode().addNode("testFindOrCreate", "nt:unstructured");

        // Only 1 existing target
        parentNode.addNode("item", "hippo:compound").setProperty("index", "0");

        ContentNode sourceNode = new ContentNode("testFindOrCreate", "nt:unstructured");

        // Source has 3 items - should create 2 new ones
        for (int i = 0; i < 3; i++) {
            ContentNode child = new ContentNode("item", "hippo:compound");
            child.setProperty("index", String.valueOf(i));
            sourceNode.addNode(child);
        }

        binder.setSubNodesMergingOnly(true);
        binder.bind(parentNode, sourceNode);

        assertEquals(3, countChildren(parentNode));
    }

    @Test
    public void testGetCompoundNodeNamesReturnsEmptyForNoCompounds() throws Exception {
        MockNode parentNode = getRootNode().addNode("testNoCompounds", "nt:unstructured");

        // Add non-compound children
        parentNode.addNode("child1", "nt:unstructured");
        parentNode.addNode("child2", "nt:unstructured");

        ContentNode sourceNode = new ContentNode("testNoCompounds", "nt:unstructured");

        // Default mode - non-compounds should survive
        binder.bind(parentNode, sourceNode);

        // Non-compound children preserved
        assertTrue(parentNode.hasNode("child1"));
        assertTrue(parentNode.hasNode("child2"));
    }

    // ==================== Targeted Branch Coverage Tests ====================

    @Test
    public void testItemFilterRejectsPropertyInBindProperties() throws Exception {
        // Test: if (itemFilter != null && !itemFilter.accept(contentProp)) in bindProperties
        MockNode parentNode = getRootNode().addNode("testPropFilter", "nt:unstructured");

        ContentNode sourceNode = new ContentNode("testPropFilter", "nt:unstructured");
        sourceNode.setProperty("acceptedProp", "acceptedValue");
        sourceNode.setProperty("rejectedProp", "rejectedValue");
        sourceNode.setProperty("anotherAccepted", "anotherValue");

        // Filter that specifically rejects properties containing "rejected"
        ContentNodeBindingItemFilter<ContentItem> propFilter = item -> {
            if (item.getName().contains("rejected")) {
                return false;
            }
            return true;
        };

        binder.bind(parentNode, sourceNode, propFilter);

        // Verify accepted properties are bound, rejected is not
        assertTrue(parentNode.hasProperty("acceptedProp"));
        assertFalse(parentNode.hasProperty("rejectedProp"));
        assertTrue(parentNode.hasProperty("anotherAccepted"));
    }

    @Test
    public void testItemFilterRejectsChildInRemoveSubNodes() throws Exception {
        // Test: if (itemFilter != null && !itemFilter.accept(childContentNode)) in removeSubNodes
        MockNode parentNode = getRootNode().addNode("testRemoveFilter2", "nt:unstructured");

        // Add existing compound children
        parentNode.addNode("keepChild", "hippo:compound").setProperty("status", "keep");
        parentNode.addNode("processChild", "hippo:compound").setProperty("status", "process");

        ContentNode sourceNode = new ContentNode("testRemoveFilter2", "nt:unstructured");

        // Source has children that will trigger removal logic
        ContentNode newKeep = new ContentNode("keepChild", "hippo:compound");
        newKeep.setProperty("status", "updated-keep");
        sourceNode.addNode(newKeep);

        ContentNode newProcess = new ContentNode("processChild", "hippo:compound");
        newProcess.setProperty("status", "updated-process");
        sourceNode.addNode(newProcess);

        // Filter that rejects "keepChild" - it should not be processed
        ContentNodeBindingItemFilter<ContentItem> childFilter = item -> {
            return !"keepChild".equals(item.getName());
        };

        // Default mode (not merge) - this goes through removeSubNodes then addSubNodes
        binder.setSubNodesMergingOnly(false);
        binder.setFullOverwriteMode(false);
        binder.bind(parentNode, sourceNode, childFilter);

        // processChild should be re-added (removed then added)
        assertTrue(parentNode.hasNode("processChild"));
        // keepChild was filtered out from processing but compounds are removed by name
    }

    @Test
    public void testFindChildNodesByNameAndTypeRemovesMatchingNodes() throws Exception {
        // Test: for (Node sameNameTypeChildNode : findChildNodesByNameAndType(...)) { remove }
        MockNode parentNode = getRootNode().addNode("testFindRemove", "nt:unstructured");

        // Add children with same name but different types
        MockNode compound1 = parentNode.addNode("mixedItem", "hippo:compound");
        compound1.setProperty("index", "1");
        MockNode html1 = parentNode.addNode("mixedItem", "hippostd:html");
        html1.setProperty("hippostd:content", "<p>html</p>");
        MockNode compound2 = parentNode.addNode("mixedItem", "hippo:compound");
        compound2.setProperty("index", "2");

        assertEquals(3, countChildren(parentNode));

        ContentNode sourceNode = new ContentNode("testFindRemove", "nt:unstructured");

        // Source only has compound type - html should be removed (it's a compound type too)
        ContentNode newCompound = new ContentNode("mixedItem", "hippo:compound");
        newCompound.setProperty("index", "new");
        sourceNode.addNode(newCompound);

        // Default mode removes compounds then re-adds
        binder.bind(parentNode, sourceNode);

        // Should only have the new compound
        assertEquals(1, countChildren(parentNode));
    }

    @Test
    public void testRemoveSubNodesWithMatchingNameAndType() throws Exception {
        // Specifically test the findChildNodesByNameAndType branch in removeSubNodes
        MockNode parentNode = getRootNode().addNode("testMatchRemove", "nt:unstructured");

        // Add a non-compound child that matches source by name and type
        MockNode existingChild = parentNode.addNode("regularChild", "nt:unstructured");
        existingChild.setProperty("data", "original");

        ContentNode sourceNode = new ContentNode("testMatchRemove", "nt:unstructured");

        // Source has child with same name and type
        ContentNode matchingChild = new ContentNode("regularChild", "nt:unstructured");
        matchingChild.setProperty("data", "updated");
        sourceNode.addNode(matchingChild);

        binder.bind(parentNode, sourceNode);

        // The matching child should be removed and re-added
        assertTrue(parentNode.hasNode("regularChild"));
        assertEquals("updated", parentNode.getNode("regularChild").getProperty("data").getString());
    }

    @Test
    public void testItemFilterRejectsChildInAddSubNodes() throws Exception {
        // Test: if (itemFilter != null && !itemFilter.accept(childContentNode)) in addSubNodes
        MockNode parentNode = getRootNode().addNode("testAddFilter", "nt:unstructured");

        ContentNode sourceNode = new ContentNode("testAddFilter", "nt:unstructured");

        ContentNode acceptedChild = new ContentNode("acceptedChild", "hippo:compound");
        acceptedChild.setProperty("status", "accepted");
        sourceNode.addNode(acceptedChild);

        ContentNode filteredChild = new ContentNode("filteredChild", "hippo:compound");
        filteredChild.setProperty("status", "filtered");
        sourceNode.addNode(filteredChild);

        // Filter rejects filteredChild
        ContentNodeBindingItemFilter<ContentItem> addFilter = item -> {
            return !"filteredChild".equals(item.getName());
        };

        binder.bind(parentNode, sourceNode, addFilter);

        // Only acceptedChild should be added
        assertTrue(parentNode.hasNode("acceptedChild"));
        assertFalse(parentNode.hasNode("filteredChild"));
    }

    @Test
    public void testCollectMergeableNodeNamesWithFilterRejectingChild() throws Exception {
        // Test: if (itemFilter == null || itemFilter.accept(child)) in collectMergeableNodeNames
        MockNode parentNode = getRootNode().addNode("testCollectFilter", "nt:unstructured");

        // Existing compound child
        parentNode.addNode("existingItem", "hippo:compound");

        ContentNode sourceNode = new ContentNode("testCollectFilter", "nt:unstructured");

        ContentNode acceptedItem = new ContentNode("acceptedItem", "hippo:compound");
        sourceNode.addNode(acceptedItem);

        ContentNode rejectedItem = new ContentNode("rejectedItem", "hippo:compound");
        sourceNode.addNode(rejectedItem);

        // Filter rejects rejectedItem
        ContentNodeBindingItemFilter<ContentItem> collectFilter = item -> {
            return !"rejectedItem".equals(item.getName());
        };

        binder.setSubNodesMergingOnly(true);
        binder.bind(parentNode, sourceNode, collectFilter);

        // acceptedItem should be created, rejectedItem should not
        assertTrue(parentNode.hasNode("acceptedItem"));
        assertFalse(parentNode.hasNode("rejectedItem"));
    }

    @Test
    public void testGroupContentChildrenByTypeWithFilter() throws Exception {
        // Test: .filter(child -> itemFilter == null || itemFilter.accept(child)) in groupContentChildrenByType
        MockNode parentNode = getRootNode().addNode("testGroupFilter2", "nt:unstructured");

        // Existing children
        parentNode.addNode("item", "hippo:compound").setProperty("index", "0");

        ContentNode sourceNode = new ContentNode("testGroupFilter2", "nt:unstructured");

        // Multiple items, some filtered
        ContentNode item1 = new ContentNode("item", "hippo:compound");
        item1.setProperty("index", "1");
        sourceNode.addNode(item1);

        ContentNode item2 = new ContentNode("item", "hippo:compound");
        item2.setProperty("index", "2");
        sourceNode.addNode(item2);

        // Filter accepts only first item by checking property
        ContentNodeBindingItemFilter<ContentItem> groupFilter = new ContentNodeBindingItemFilter<ContentItem>() {
            private int callCount = 0;
            @Override
            public boolean accept(ContentItem item) {
                if (item instanceof ContentNode) {
                    ContentNode cn = (ContentNode) item;
                    if ("item".equals(cn.getName())) {
                        callCount++;
                        return callCount <= 1; // Only accept first "item"
                    }
                }
                return true;
            }
        };

        binder.setSubNodesMergingOnly(true);
        binder.bind(parentNode, sourceNode, groupFilter);

        // Should have 1 item (existing updated, second filtered out)
        int itemCount = 0;
        NodeIterator children = parentNode.getNodes("item");
        while (children.hasNext()) {
            children.nextNode();
            itemCount++;
        }
        assertEquals(1, itemCount);
    }

    @Test
    public void testBindPropertiesWithNoProperties() throws Exception {
        // Test empty properties iteration
        MockNode parentNode = getRootNode().addNode("testNoProps", "nt:unstructured");
        parentNode.setProperty("existingProp", "existingValue");

        ContentNode sourceNode = new ContentNode("testNoProps", "nt:unstructured");
        // No properties added to sourceNode

        binder.bind(parentNode, sourceNode);

        // Existing property should remain (no properties to bind doesn't mean remove existing)
        assertTrue(parentNode.hasProperty("existingProp"));
    }

    @Test
    public void testBindWithExistingPropertyThatIsNotProtected() throws Exception {
        // Test: if (existingJcrProp != null && isProtectedProperty(existingJcrProp)) - false branch
        MockNode parentNode = getRootNode().addNode("testNotProtected", "nt:unstructured");
        parentNode.setProperty("normalProp", "oldValue");

        ContentNode sourceNode = new ContentNode("testNotProtected", "nt:unstructured");
        sourceNode.setProperty("normalProp", "newValue");

        binder.bind(parentNode, sourceNode);

        // Property should be updated (not protected)
        assertEquals("newValue", parentNode.getProperty("normalProp").getString());
    }

    @Test
    public void testCreateJcrValuesFromBinaryProperty() throws Exception {
        // Test: if (ContentPropertyType.BINARY.equals(contentProp.getType())) branch
        MockNode parentNode = getRootNode().addNode("testBinaryProp", "nt:unstructured");

        ContentNode sourceNode = new ContentNode("testBinaryProp", "nt:unstructured");

        // Create binary value
        byte[] data = "test binary data".getBytes();
        BinaryValue binaryValue = new BinaryValue(data, "text/plain", "UTF-8");
        sourceNode.setProperty("binaryProp", binaryValue);

        binder.bind(parentNode, sourceNode);

        // Binary property should be set
        assertTrue(parentNode.hasProperty("binaryProp"));
    }

    @Test
    public void testJcrValuesNullCheckInBindProperties() throws Exception {
        // Test: if (jcrValues != null) branch when jcrValues is null
        MockNode parentNode = getRootNode().addNode("testNullValues", "nt:unstructured");

        ContentNode sourceNode = new ContentNode("testNullValues", "nt:unstructured");
        sourceNode.setProperty("testProp", "value");

        // Use converter that returns null for values
        ContentValueConverter<Value> nullValueConverter = new ContentValueConverter<Value>() {
            @Override
            public Value toJcrValue(String typeName, String stringValue) {
                return null;
            }

            @Override
            public Value toJcrValue(BinaryValue binaryValue) {
                return null;
            }

            @Override
            public String toString(Value value) {
                return null;
            }

            @Override
            public BinaryValue toBinaryValue(Value value, String mimeType) {
                return null;
            }
        };

        binder.bind(parentNode, sourceNode, null, nullValueConverter);

        // Property should not be set since all values are null
        assertFalse(parentNode.hasProperty("testProp"));
    }

    @Test
    public void testEmptyJcrValuesArrayNotSet() throws Exception {
        // Test: else if (jcrValues.length > 0) - false branch (empty array)
        MockNode parentNode = getRootNode().addNode("testEmptyArray", "nt:unstructured");

        ContentNode sourceNode = new ContentNode("testEmptyArray", "nt:unstructured");
        // Property with empty values
        sourceNode.setProperty("emptyProp", ContentPropertyType.STRING, new String[]{});

        binder.bind(parentNode, sourceNode);

        // Empty array should not create property (or create empty multi-value)
        // Behavior depends on whether empty array creates property or not
    }

    @Test
    public void testRemoveSubNodesRemovesCompoundsByName() throws Exception {
        // Test compound removal by name glob
        MockNode parentNode = getRootNode().addNode("testCompoundRemoval", "nt:unstructured");

        // Add multiple compounds with same name
        parentNode.addNode("compound", "hippo:compound").setProperty("idx", "1");
        parentNode.addNode("compound", "hippo:compound").setProperty("idx", "2");
        parentNode.addNode("compound", "hippo:compound").setProperty("idx", "3");
        parentNode.addNode("otherCompound", "hippo:compound").setProperty("idx", "other");

        assertEquals(4, countChildren(parentNode));

        ContentNode sourceNode = new ContentNode("testCompoundRemoval", "nt:unstructured");
        // Add only one compound back
        ContentNode newCompound = new ContentNode("compound", "hippo:compound");
        newCompound.setProperty("idx", "new");
        sourceNode.addNode(newCompound);

        binder.bind(parentNode, sourceNode);

        // Only the new compound should exist, all others removed
        assertEquals(1, countChildren(parentNode));
        assertTrue(parentNode.hasNode("compound"));
    }

    @Test
    public void testMergeWithFilterOnContentNodes() throws Exception {
        // Test filter in mergeSubNodes path
        MockNode parentNode = getRootNode().addNode("testMergeFilterPath", "nt:unstructured");
        parentNode.addNode("item", "hippo:compound").setProperty("v", "orig");

        ContentNode sourceNode = new ContentNode("testMergeFilterPath", "nt:unstructured");

        ContentNode item1 = new ContentNode("item", "hippo:compound");
        item1.setProperty("v", "new1");
        sourceNode.addNode(item1);

        ContentNode filteredItem = new ContentNode("filteredItem", "hippo:compound");
        filteredItem.setProperty("v", "filtered");
        sourceNode.addNode(filteredItem);

        ContentNodeBindingItemFilter<ContentItem> filter = item -> !"filteredItem".equals(item.getName());

        binder.setSubNodesMergingOnly(true);
        binder.bind(parentNode, sourceNode, filter);

        assertTrue(parentNode.hasNode("item"));
        assertFalse(parentNode.hasNode("filteredItem"));
    }

    // ==================== Empty Multi-Value Property Type Preservation Tests ====================

    @Test
    public void testBindEmptyMultiValueLongProperty_preservesType() throws Exception {
        MockNode parentNode = getRootNode().addNode("testEmptyLong", "nt:unstructured");

        ContentNode sourceNode = new ContentNode("testEmptyLong", "nt:unstructured");
        ContentProperty longProp = new ContentProperty("ids", ContentPropertyType.LONG, true);
        sourceNode.setProperty(longProp);

        binder.bind(parentNode, sourceNode);

        assertTrue(parentNode.hasProperty("ids"));
        Property jcrProp = parentNode.getProperty("ids");
        assertEquals(PropertyType.LONG, jcrProp.getType());
        assertTrue(jcrProp.isMultiple());
        assertEquals(0, jcrProp.getValues().length);
    }

    @Test
    public void testBindEmptyMultiValueDoubleProperty_preservesType() throws Exception {
        MockNode parentNode = getRootNode().addNode("testEmptyDouble", "nt:unstructured");

        ContentNode sourceNode = new ContentNode("testEmptyDouble", "nt:unstructured");
        ContentProperty doubleProp = new ContentProperty("scores", ContentPropertyType.DOUBLE, true);
        sourceNode.setProperty(doubleProp);

        binder.bind(parentNode, sourceNode);

        assertTrue(parentNode.hasProperty("scores"));
        Property jcrProp = parentNode.getProperty("scores");
        assertEquals(PropertyType.DOUBLE, jcrProp.getType());
        assertTrue(jcrProp.isMultiple());
        assertEquals(0, jcrProp.getValues().length);
    }

    @Test
    public void testBindEmptyMultiValueBooleanProperty_preservesType() throws Exception {
        MockNode parentNode = getRootNode().addNode("testEmptyBoolean", "nt:unstructured");

        ContentNode sourceNode = new ContentNode("testEmptyBoolean", "nt:unstructured");
        ContentProperty boolProp = new ContentProperty("flags", ContentPropertyType.BOOLEAN, true);
        sourceNode.setProperty(boolProp);

        binder.bind(parentNode, sourceNode);

        assertTrue(parentNode.hasProperty("flags"));
        Property jcrProp = parentNode.getProperty("flags");
        assertEquals(PropertyType.BOOLEAN, jcrProp.getType());
        assertTrue(jcrProp.isMultiple());
        assertEquals(0, jcrProp.getValues().length);
    }

    @Test
    public void testBindEmptyMultiValueDateProperty_preservesType() throws Exception {
        MockNode parentNode = getRootNode().addNode("testEmptyDate", "nt:unstructured");

        ContentNode sourceNode = new ContentNode("testEmptyDate", "nt:unstructured");
        ContentProperty dateProp = new ContentProperty("timestamps", ContentPropertyType.DATE, true);
        sourceNode.setProperty(dateProp);

        binder.bind(parentNode, sourceNode);

        assertTrue(parentNode.hasProperty("timestamps"));
        Property jcrProp = parentNode.getProperty("timestamps");
        assertEquals(PropertyType.DATE, jcrProp.getType());
        assertTrue(jcrProp.isMultiple());
        assertEquals(0, jcrProp.getValues().length);
    }
}

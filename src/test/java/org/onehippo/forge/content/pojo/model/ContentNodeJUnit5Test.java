/*
 * Copyright 2015-2026 Bloomreach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onehippo.forge.content.pojo.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit 5 tests for ContentNode pure-Java behavior.
 * Complements the existing ContentNodeTest (JUnit 4).
 */
class ContentNodeJUnit5Test {

    private ContentNode root;

    @BeforeEach
    void setUp() {
        root = new ContentNode("root", "nt:folder");
    }

    // -----------------------------------------------------------------------
    // Basic construction and accessors
    // -----------------------------------------------------------------------

    @Test
    void constructor_setsNameAndPrimaryType() {
        assertEquals("root", root.getName());
        assertEquals("nt:folder", root.getPrimaryType());
    }

    @Test
    void defaultConstructor_allowsDeserialization() {
        ContentNode node = new ContentNode();
        assertNull(node.getName());
        assertNull(node.getPrimaryType());
    }

    @Test
    void isNode_returnsTrue() {
        assertTrue(root.isNode());
    }

    // -----------------------------------------------------------------------
    // Mixin types
    // -----------------------------------------------------------------------

    @Test
    void getMixinTypes_returnsNonNull_whenNoneAdded() {
        assertNotNull(root.getMixinTypes());
        assertTrue(root.getMixinTypes().isEmpty());
    }

    @Test
    void addMixinType_addsToSet() {
        root.addMixinType("mix:versionable");
        assertTrue(root.getMixinTypes().contains("mix:versionable"));
    }

    @Test
    void removeMixinType_removesFromSet() {
        root.addMixinType("mix:versionable");
        root.removeMixinType("mix:versionable");
        assertFalse(root.getMixinTypes().contains("mix:versionable"));
    }

    @Test
    void removeMixinType_onEmptySet_doesNotThrow() {
        // guard: removing from null/empty set must be safe
        root.removeMixinType("mix:versionable");
        assertTrue(root.getMixinTypes().isEmpty());
    }

    // -----------------------------------------------------------------------
    // Properties
    // -----------------------------------------------------------------------

    @Test
    void getProperties_returnsNonNullEmpty_byDefault() {
        assertNotNull(root.getProperties());
        assertTrue(root.getProperties().isEmpty());
    }

    @Test
    void hasProperty_false_beforeAdding() {
        assertFalse(root.hasProperty("jcr:title"));
    }

    @Test
    void setPropertyByName_andRetrieve() {
        root.setProperty("jcr:title", "Hello");
        assertTrue(root.hasProperty("jcr:title"));
        assertEquals("Hello", root.getProperty("jcr:title").getValue());
    }

    @Test
    void setProperty_replacesExistingWithSameName() {
        root.setProperty("jcr:title", "First");
        root.setProperty("jcr:title", "Second");
        assertEquals(1, root.getProperties().size());
        assertEquals("Second", root.getProperty("jcr:title").getValue());
    }

    @Test
    void setProperty_withTypeAndValue() {
        root.setProperty("count", ContentPropertyType.LONG, "42");
        assertEquals("42", root.getProperty("count").getValue());
        assertEquals(ContentPropertyType.LONG, root.getProperty("count").getType());
    }

    @Test
    void setProperty_withStringArray() {
        root.setProperty("tags", new String[]{"news", "sport"});
        ContentProperty tags = root.getProperty("tags");
        assertNotNull(tags);
        assertEquals(2, tags.getValueCount());
        assertTrue(tags.isMultiple());
    }

    @Test
    void setProperty_withNullStringArray_createsEmptyMultiple() {
        root.setProperty("tags", (String[]) null);
        ContentProperty tags = root.getProperty("tags");
        assertNotNull(tags);
        assertEquals(0, tags.getValueCount());
    }

    @Test
    void getProperty_returnsNull_forUnknownName() {
        assertNull(root.getProperty("nonexistent"));
    }

    // -----------------------------------------------------------------------
    // Child nodes
    // -----------------------------------------------------------------------

    @Test
    void hasAnyNode_false_beforeAdding() {
        assertFalse(root.hasAnyNode());
    }

    @Test
    void addNode_andHasAnyNode_returnsTrue() {
        root.addNode(new ContentNode("child", "nt:unstructured"));
        assertTrue(root.hasAnyNode());
    }

    @Test
    void addNode_setsParentReference() {
        ContentNode child = new ContentNode("child", "nt:unstructured");
        root.addNode(child);
        assertEquals(root, child.getParent());
    }

    @Test
    void getParent_nullForRootNode() {
        assertNull(root.getParent());
    }

    @Test
    void hasNode_byName() {
        root.addNode(new ContentNode("body", "hippostd:html"));
        assertTrue(root.hasNode("body"));
        assertFalse(root.hasNode("nonexistent"));
    }

    @Test
    void getNode_returnsFirstMatchByName() {
        ContentNode child1 = new ContentNode("body", "hippostd:html");
        ContentNode child2 = new ContentNode("other", "nt:unstructured");
        root.addNode(child1);
        root.addNode(child2);
        assertEquals(child1, root.getNode("body"));
    }

    @Test
    void getNode_returnsNull_forUnknownName() {
        assertNull(root.getNode("nonexistent"));
    }

    @Test
    void getNodes_returnsNonNullEmpty_byDefault() {
        assertNotNull(root.getNodes());
        assertTrue(root.getNodes().isEmpty());
    }

    // -----------------------------------------------------------------------
    // Index for same-name siblings
    // -----------------------------------------------------------------------

    @Test
    void getIndex_returns1_forNodeWithoutParent() {
        assertEquals(1, root.getIndex());
    }

    @Test
    void getIndex_returns1_forUniqueNameChild() {
        ContentNode child = new ContentNode("unique", "nt:folder");
        root.addNode(child);
        assertEquals(1, child.getIndex());
    }

    @Test
    void getIndex_incrementsForSameNameSiblings() {
        ContentNode s1 = new ContentNode("item", "nt:unstructured");
        ContentNode s2 = new ContentNode("item", "nt:unstructured");
        ContentNode s3 = new ContentNode("item", "nt:unstructured");
        root.addNode(s1);
        root.addNode(s2);
        root.addNode(s3);
        assertEquals(1, s1.getIndex());
        assertEquals(2, s2.getIndex());
        assertEquals(3, s3.getIndex());
    }

    // -----------------------------------------------------------------------
    // Clone
    // -----------------------------------------------------------------------

    @Test
    void clone_isEqualButNotSame() {
        root.addMixinType("mix:versionable");
        root.setProperty("title", "Test");
        ContentNode child = new ContentNode("child", "nt:unstructured");
        root.addNode(child);

        ContentNode clone = (ContentNode) root.clone();
        assertEquals(root, clone);
        assertFalse(root == clone);
    }

    @Test
    void clone_childrenAreIndependent() {
        ContentNode child = new ContentNode("child", "nt:unstructured");
        root.addNode(child);
        ContentNode clone = (ContentNode) root.clone();
        clone.addNode(new ContentNode("extra", "nt:unstructured"));

        assertEquals(1, root.getNodes().size());
        assertEquals(2, clone.getNodes().size());
    }

    // -----------------------------------------------------------------------
    // equals / hashCode
    // -----------------------------------------------------------------------

    @Test
    void equals_reflexive() {
        assertEquals(root, root);
    }

    @Test
    void equals_differentPrimaryType_notEqual() {
        ContentNode other = new ContentNode("root", "hippostd:folder");
        assertFalse(root.equals(other));
    }

    @Test
    void equals_nonContentNode_returnsFalse() {
        assertFalse(root.equals("not a node"));
    }

    @Test
    void hashCode_consistentForEqualObjects() {
        ContentNode a = new ContentNode("doc", "myhippo:news");
        ContentNode b = new ContentNode("doc", "myhippo:news");
        assertEquals(a.hashCode(), b.hashCode());
    }

    // -----------------------------------------------------------------------
    // toString
    // -----------------------------------------------------------------------

    @Test
    void toString_containsNameAndPrimaryType() {
        String result = root.toString();
        assertTrue(result.contains("root"));
        assertTrue(result.contains("nt:folder"));
    }

    // -----------------------------------------------------------------------
    // setPrimaryType
    // -----------------------------------------------------------------------

    @Test
    void setPrimaryType_updatesType() {
        root.setPrimaryType("hippostd:folder");
        assertEquals("hippostd:folder", root.getPrimaryType());
    }
}

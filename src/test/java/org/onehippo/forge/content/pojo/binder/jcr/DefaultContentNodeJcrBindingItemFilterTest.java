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
package org.onehippo.forge.content.pojo.binder.jcr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onehippo.forge.content.pojo.binder.ContentNodeBindingException;
import org.onehippo.forge.content.pojo.model.ContentNode;
import org.onehippo.forge.content.pojo.model.ContentProperty;
import org.onehippo.forge.content.pojo.model.ContentPropertyType;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for DefaultContentNodeJcrBindingItemFilter.
 * No live JCR session — tests use pure POJO ContentNode / ContentProperty objects.
 */
class DefaultContentNodeJcrBindingItemFilterTest {

    private DefaultContentNodeJcrBindingItemFilter filter;

    @BeforeEach
    void setUp() {
        filter = new DefaultContentNodeJcrBindingItemFilter();
    }

    // -----------------------------------------------------------------------
    // Node acceptance — no configuration (accept all)
    // -----------------------------------------------------------------------

    @Test
    void acceptNode_withNoFilters_returnsTrue() throws ContentNodeBindingException {
        ContentNode node = new ContentNode("myhippo:body", "hippostd:html");
        assertTrue(filter.accept(node));
    }

    // -----------------------------------------------------------------------
    // Node acceptance — node exclude pattern
    // -----------------------------------------------------------------------

    @Test
    void acceptNode_excludedByNamePattern_returnsFalse() throws ContentNodeBindingException {
        filter.addNodePathExclude("hippostd:*");
        ContentNode node = new ContentNode("hippostd:holder", "hippostd:folder");
        assertFalse(filter.accept(node));
    }

    @Test
    void acceptNode_notMatchingExcludePattern_returnsTrue() throws ContentNodeBindingException {
        filter.addNodePathExclude("hippostd:*");
        ContentNode node = new ContentNode("myhippo:body", "hippostd:html");
        assertTrue(filter.accept(node));
    }

    @Test
    void acceptNode_includedByIncludePattern_returnsTrue() throws ContentNodeBindingException {
        filter.addNodePathInclude("myhippo:*");
        ContentNode node = new ContentNode("myhippo:body", "hippostd:html");
        assertTrue(filter.accept(node));
    }

    @Test
    void acceptNode_notMatchingIncludePattern_returnsFalse() throws ContentNodeBindingException {
        filter.addNodePathInclude("myhippo:*");
        ContentNode node = new ContentNode("hippostd:holder", "hippostd:folder");
        assertFalse(filter.accept(node));
    }

    // -----------------------------------------------------------------------
    // Property acceptance — no configuration (accept all)
    // -----------------------------------------------------------------------

    @Test
    void acceptProperty_withNoFilters_returnsTrue() throws ContentNodeBindingException {
        ContentProperty prop = new ContentProperty("hippo:title", ContentPropertyType.STRING);
        assertTrue(filter.accept(prop));
    }

    // -----------------------------------------------------------------------
    // Property acceptance — property type filtering
    // Note: the binder filter checks ContentPropertyType.toString() which returns
    // the enum name (e.g. "STRING", "DATE"). We exclude/include using those names.
    // -----------------------------------------------------------------------

    @Test
    void acceptProperty_excludedByTypeName_returnsFalse() throws ContentNodeBindingException {
        filter.addPropertyTypeNameExclude("DATE");
        ContentProperty prop = new ContentProperty("hippo:published", ContentPropertyType.DATE);
        assertFalse(filter.accept(prop));
    }

    @Test
    void acceptProperty_typeNotExcluded_returnsTrue() throws ContentNodeBindingException {
        filter.addPropertyTypeNameExclude("DATE");
        ContentProperty prop = new ContentProperty("hippo:title", ContentPropertyType.STRING);
        assertTrue(filter.accept(prop));
    }

    @Test
    void acceptProperty_includedByTypeName_returnsTrue() throws ContentNodeBindingException {
        filter.addPropertyTypeNameInclude("STRING");
        ContentProperty prop = new ContentProperty("hippo:title", ContentPropertyType.STRING);
        assertTrue(filter.accept(prop));
    }

    @Test
    void acceptProperty_typeNotInIncludeSet_returnsFalse() throws ContentNodeBindingException {
        filter.addPropertyTypeNameInclude("STRING");
        ContentProperty prop = new ContentProperty("hippo:date", ContentPropertyType.DATE);
        assertFalse(filter.accept(prop));
    }

    // -----------------------------------------------------------------------
    // Property acceptance — property path filtering
    // -----------------------------------------------------------------------

    @Test
    void acceptProperty_excludedByPath_returnsFalse() throws ContentNodeBindingException {
        filter.addPropertyPathExclude("jcr:*");
        ContentProperty prop = new ContentProperty("jcr:uuid", ContentPropertyType.STRING);
        assertFalse(filter.accept(prop));
    }

    @Test
    void acceptProperty_notMatchingExcludePath_returnsTrue() throws ContentNodeBindingException {
        filter.addPropertyPathExclude("jcr:*");
        ContentProperty prop = new ContentProperty("hippo:title", ContentPropertyType.STRING);
        assertTrue(filter.accept(prop));
    }

    @Test
    void acceptProperty_includedByPath_returnsTrue() throws ContentNodeBindingException {
        filter.addPropertyPathInclude("hippo:*");
        ContentProperty prop = new ContentProperty("hippo:title", ContentPropertyType.STRING);
        assertTrue(filter.accept(prop));
    }

    @Test
    void acceptProperty_notMatchingIncludePath_returnsFalse() throws ContentNodeBindingException {
        filter.addPropertyPathInclude("hippo:*");
        ContentProperty prop = new ContentProperty("jcr:uuid", ContentPropertyType.STRING);
        assertFalse(filter.accept(prop));
    }

    // -----------------------------------------------------------------------
    // Combined type + path filters
    // -----------------------------------------------------------------------

    @Test
    void acceptProperty_typeExcluded_returnsFalse_regardlessOfPath() throws ContentNodeBindingException {
        filter.addPropertyTypeNameExclude("DATE");
        filter.addPropertyPathExclude("hippo:paths");
        // type exclusion applies before path check
        ContentProperty prop = new ContentProperty("hippo:published", ContentPropertyType.DATE);
        assertFalse(filter.accept(prop));
    }
}

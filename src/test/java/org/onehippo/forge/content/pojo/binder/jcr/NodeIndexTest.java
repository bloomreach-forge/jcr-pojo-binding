/*
 *  Copyright 2026 Bloomreach (https://www.bloomreach.com)
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class NodeIndexTest {

    // ── get: miss cases ───────────────────────────────────────────────────────

    @Test
    public void getReturnsEmptyListForUnknownName() {
        NodeIndex<String> index = new NodeIndex<>();
        assertTrue(index.get("missing", "nt:base").isEmpty());
    }

    @Test
    public void getReturnsEmptyListForKnownNameButUnknownType() {
        NodeIndex<String> index = new NodeIndex<>();
        index.add("node", "nt:base", "a");
        assertTrue(index.get("node", "nt:other").isEmpty());
    }

    // ── add + get ─────────────────────────────────────────────────────────────

    @Test
    public void addAndGetSingleEntry() {
        NodeIndex<String> index = new NodeIndex<>();
        index.add("news", "myhippoproject:news", "nodeA");
        List<String> result = index.get("news", "myhippoproject:news");
        assertEquals(1, result.size());
        assertEquals("nodeA", result.get(0));
    }

    @Test
    public void addPreservesInsertionOrderForSameNameAndType() {
        NodeIndex<String> index = new NodeIndex<>();
        index.add("para", "hippostd:html", "n1");
        index.add("para", "hippostd:html", "n2");
        index.add("para", "hippostd:html", "n3");
        List<String> result = index.get("para", "hippostd:html");
        assertEquals(List.of("n1", "n2", "n3"), result);
    }

    @Test
    public void addKeepsSeparateListsForDifferentTypesUnderSameName() {
        NodeIndex<String> index = new NodeIndex<>();
        index.add("content", "hippostd:html", "htmlNode");
        index.add("content", "hippostd:richtext", "richNode");
        assertEquals(1, index.get("content", "hippostd:html").size());
        assertEquals(1, index.get("content", "hippostd:richtext").size());
    }

    // ── getByName ─────────────────────────────────────────────────────────────

    @Test
    public void getByNameReturnsMapOfAllTypesForThatName() {
        NodeIndex<String> index = new NodeIndex<>();
        index.add("content", "hippostd:html", "n1");
        index.add("content", "hippostd:richtext", "n2");
        Map<String, List<String>> byName = index.getByName("content");
        assertEquals(2, byName.size());
        assertTrue(byName.containsKey("hippostd:html"));
        assertTrue(byName.containsKey("hippostd:richtext"));
    }

    @Test
    public void getByNameReturnsEmptyMapForUnknownName() {
        NodeIndex<String> index = new NodeIndex<>();
        Map<String, List<String>> byName = index.getByName("nonexistent");
        assertNotNull(byName);
        assertTrue(byName.isEmpty());
    }

    // ── getNames ──────────────────────────────────────────────────────────────

    @Test
    public void getNamesReflectsAllAddedNamesInInsertionOrder() {
        NodeIndex<String> index = new NodeIndex<>();
        index.add("alpha", "t:a", "n1");
        index.add("beta", "t:b", "n2");
        index.add("gamma", "t:c", "n3");
        Set<String> names = index.getNames();
        assertArrayEquals(new String[]{"alpha", "beta", "gamma"}, names.toArray(new String[0]));
    }

    @Test
    public void getNamesIsEmptyForFreshIndex() {
        assertTrue(new NodeIndex<>().getNames().isEmpty());
    }

    // ── addCompound + getCompounds ────────────────────────────────────────────

    @Test
    public void getCompoundsIsEmptyByDefault() {
        assertTrue(new NodeIndex<>().getCompounds().isEmpty());
    }

    @Test
    public void addCompoundAndGetCompoundsPreservesOrder() {
        NodeIndex<String> index = new NodeIndex<>();
        index.addCompound("compound1");
        index.addCompound("compound2");
        index.addCompound("compound3");
        assertEquals(List.of("compound1", "compound2", "compound3"), index.getCompounds());
    }

    @Test
    public void compoundsAndNamedEntriesAreIndependent() {
        NodeIndex<String> index = new NodeIndex<>();
        index.add("named", "nt:type", "namedNode");
        index.addCompound("compoundNode");
        assertEquals(1, index.get("named", "nt:type").size());
        assertEquals(1, index.getCompounds().size());
    }
}

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
package org.onehippo.forge.content.pojo.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for BasePathBasedContentNodeItemFilter using a minimal concrete subclass
 * (no JCR or Spring context required).
 */
class BasePathBasedContentNodeItemFilterTest {

    /** Minimal concrete subclass exposing protected methods for direct testing. */
    static class TestableFilter extends BasePathBasedContentNodeItemFilter {

        boolean isPathIncludablePublic(String path,
                java.util.Collection<Pattern> includes,
                java.util.Collection<Pattern> excludes) {
            return isPathIncludable(path, includes, excludes);
        }

        boolean isPropertyIncludableByTypePublic(String typeName) {
            return isPropertyIncludableByType(typeName);
        }
    }

    private TestableFilter filter;

    @BeforeEach
    void setUp() {
        filter = new TestableFilter();
    }

    // -----------------------------------------------------------------------
    // isPropertyIncludableByType
    // -----------------------------------------------------------------------

    @Test
    void propertyType_includable_whenNoIncludesAndNoExcludes() {
        assertTrue(filter.isPropertyIncludableByTypePublic("String"));
    }

    @Test
    void propertyType_excludedByExcludeSet() {
        filter.addPropertyTypeNameExclude("Reference");
        assertFalse(filter.isPropertyIncludableByTypePublic("Reference"));
    }

    @Test
    void propertyType_notExcluded_whenNotInExcludeSet() {
        filter.addPropertyTypeNameExclude("Reference");
        assertTrue(filter.isPropertyIncludableByTypePublic("String"));
    }

    @Test
    void propertyType_includable_whenInIncludeSet() {
        filter.addPropertyTypeNameInclude("String");
        filter.addPropertyTypeNameInclude("Date");
        assertTrue(filter.isPropertyIncludableByTypePublic("String"));
        assertTrue(filter.isPropertyIncludableByTypePublic("Date"));
    }

    @Test
    void propertyType_notIncludable_whenNotInIncludeSet() {
        filter.addPropertyTypeNameInclude("String");
        assertFalse(filter.isPropertyIncludableByTypePublic("Reference"));
    }

    @Test
    void setPropertyTypeNameIncludes_replacesSet() {
        filter.setPropertyTypeNameIncludes(new HashSet<>(Arrays.asList("String", "Boolean")));
        assertTrue(filter.isPropertyIncludableByTypePublic("Boolean"));
        assertFalse(filter.isPropertyIncludableByTypePublic("Reference"));
    }

    @Test
    void setPropertyTypeNameExcludes_replacesSet() {
        filter.setPropertyTypeNameExcludes(new HashSet<>(Arrays.asList("WeakReference")));
        assertFalse(filter.isPropertyIncludableByTypePublic("WeakReference"));
        assertTrue(filter.isPropertyIncludableByTypePublic("String"));
    }

    // -----------------------------------------------------------------------
    // isPathIncludable — no patterns (null/empty)
    // -----------------------------------------------------------------------

    @Test
    void pathIncludable_whenNoPatternsConfigured() {
        assertTrue(filter.isPathIncludablePublic("hippo:paths", null, null));
    }

    @Test
    void pathIncludable_whenEmptyPatternLists() {
        assertTrue(filter.isPathIncludablePublic("hippo:paths",
                java.util.Collections.emptyList(), java.util.Collections.emptyList()));
    }

    // -----------------------------------------------------------------------
    // isPathIncludable — exclude patterns
    // -----------------------------------------------------------------------

    @Test
    void pathExcluded_whenExcludePatternMatches() {
        java.util.List<Pattern> excludes = Arrays.asList(Pattern.compile("hippo:paths"));
        assertFalse(filter.isPathIncludablePublic("hippo:paths", null, excludes));
    }

    @Test
    void pathIncluded_whenExcludePatternDoesNotMatch() {
        java.util.List<Pattern> excludes = Arrays.asList(Pattern.compile("hippo:paths"));
        assertTrue(filter.isPathIncludablePublic("hippo:handle", null, excludes));
    }

    // -----------------------------------------------------------------------
    // isPathIncludable — include patterns
    // -----------------------------------------------------------------------

    @Test
    void pathIncluded_whenIncludePatternMatches() {
        java.util.List<Pattern> includes = Arrays.asList(Pattern.compile("hippo:.*"));
        assertTrue(filter.isPathIncludablePublic("hippo:handle", includes, null));
    }

    @Test
    void pathExcluded_whenIncludePatternsDefinedButNoneMatch() {
        java.util.List<Pattern> includes = Arrays.asList(Pattern.compile("hippo:.*"));
        assertFalse(filter.isPathIncludablePublic("myhippo:title", includes, null));
    }

    // -----------------------------------------------------------------------
    // Exclude wins over include (exclude is checked first)
    // -----------------------------------------------------------------------

    @Test
    void excludeWins_whenBothIncludeAndExcludeMatch() {
        java.util.List<Pattern> includes = Arrays.asList(Pattern.compile("hippo:.*"));
        java.util.List<Pattern> excludes = Arrays.asList(Pattern.compile("hippo:handle"));
        assertFalse(filter.isPathIncludablePublic("hippo:handle", includes, excludes));
    }

    // -----------------------------------------------------------------------
    // getPropertyIncludePatterns / getNodeIncludePatterns lazy compilation
    // -----------------------------------------------------------------------

    @Test
    void propertyIncludePatterns_nullWhenNoSourcesSet() {
        assertNull(filter.getPropertyIncludePatterns());
    }

    @Test
    void propertyIncludePatterns_compiledFromSources() {
        filter.addPropertyPathInclude("hippo:*");
        java.util.List<Pattern> patterns = filter.getPropertyIncludePatterns();
        assertFalse(patterns.isEmpty());
        assertTrue(patterns.get(0).matcher("hippo:title").matches());
    }

    @Test
    void propertyExcludePatterns_compiledFromSources() {
        filter.addPropertyPathExclude("jcr:*");
        java.util.List<Pattern> patterns = filter.getPropertyExcludePatterns();
        assertFalse(patterns.isEmpty());
        assertTrue(patterns.get(0).matcher("jcr:uuid").matches());
    }

    @Test
    void nodeIncludePatterns_compiledFromSources() {
        filter.addNodePathInclude("myhippo:*");
        java.util.List<Pattern> patterns = filter.getNodeIncludePatterns();
        assertFalse(patterns.isEmpty());
        assertTrue(patterns.get(0).matcher("myhippo:body").matches());
    }

    @Test
    void nodeExcludePatterns_compiledFromSources() {
        filter.addNodePathExclude("hippostd:*");
        java.util.List<Pattern> patterns = filter.getNodeExcludePatterns();
        assertFalse(patterns.isEmpty());
        assertTrue(patterns.get(0).matcher("hippostd:holder").matches());
    }

    // -----------------------------------------------------------------------
    // mismatchPatternsFromSources
    // -----------------------------------------------------------------------

    @Test
    void mismatch_whenPatternsNullAndSourcesNonEmpty() {
        assertTrue(filter.mismatchPatternsFromSources(null, Arrays.asList("hippo:*")));
    }

    @Test
    void noMismatch_whenBothNull() {
        assertFalse(filter.mismatchPatternsFromSources(null, null));
    }

    @Test
    void noMismatch_whenBothEmpty() {
        assertFalse(filter.mismatchPatternsFromSources(
                java.util.Collections.emptyList(), java.util.Collections.emptyList()));
    }

    @Test
    void mismatch_whenSizeDiffers() {
        java.util.List<Pattern> compiled = Arrays.asList(Pattern.compile(".*"));
        assertTrue(filter.mismatchPatternsFromSources(compiled, Arrays.asList("a", "b")));
    }

    // -----------------------------------------------------------------------
    // setters for path lists
    // -----------------------------------------------------------------------

    @Test
    void setPropertyPathIncludes_replacesAndCompiles() {
        filter.setPropertyPathIncludes(Arrays.asList("hippo:*", "jcr:*"));
        java.util.List<Pattern> patterns = filter.getPropertyIncludePatterns();
        assertFalse(patterns.isEmpty());
        assertTrue(patterns.stream().anyMatch(p -> p.matcher("hippo:title").matches()));
    }

    @Test
    void setPropertyPathExcludes_replacesAndCompiles() {
        filter.setPropertyPathExcludes(Arrays.asList("jcr:uuid"));
        java.util.List<Pattern> patterns = filter.getPropertyExcludePatterns();
        assertFalse(patterns.isEmpty());
    }

    @Test
    void setNodePathIncludes_replacesAndCompiles() {
        filter.setNodePathIncludes(Arrays.asList("myhippo:*"));
        java.util.List<Pattern> patterns = filter.getNodeIncludePatterns();
        assertFalse(patterns.isEmpty());
    }

    @Test
    void setNodePathExcludes_replacesAndCompiles() {
        filter.setNodePathExcludes(Arrays.asList("hippostd:*"));
        java.util.List<Pattern> patterns = filter.getNodeExcludePatterns();
        assertFalse(patterns.isEmpty());
    }
}

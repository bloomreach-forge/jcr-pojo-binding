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
package org.onehippo.forge.content.pojo.common.util;

import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobPatternTest {

    private GlobPattern glob;

    @BeforeEach
    void setUp() {
        glob = new GlobPattern();
    }

    // --- wildcard '*' tests ---

    @Test
    void asterisk_matchesEmptyString() {
        Pattern p = glob.compile("*");
        assertTrue(p.matcher("").matches());
    }

    @Test
    void asterisk_matchesAnyString() {
        Pattern p = glob.compile("*");
        assertTrue(p.matcher("anything").matches());
    }

    @Test
    void prefixWildcard_matchesPrefix() {
        Pattern p = glob.compile("hippo:*");
        assertTrue(p.matcher("hippo:handle").matches());
        assertTrue(p.matcher("hippo:").matches());
        assertFalse(p.matcher("hippox:handle").matches());
    }

    @Test
    void suffixWildcard_matchesSuffix() {
        Pattern p = glob.compile("*.xml");
        assertTrue(p.matcher("config.xml").matches());
        assertTrue(p.matcher(".xml").matches());
        assertFalse(p.matcher("config.json").matches());
    }

    @Test
    void doubleWildcard_matchesEmbeddedSeparator() {
        Pattern p = glob.compile("hippo*Path");
        assertTrue(p.matcher("hippoPaths/foo/Path").matches());
        assertFalse(p.matcher("hippoFoo").matches());
    }

    // --- question mark '?' tests ---

    @Test
    void questionMark_matchesExactlyOneCharacter_byDefault() {
        glob.setQuestionMatchesZero(false);
        Pattern p = glob.compile("?at");
        assertTrue(p.matcher("Cat").matches());
        assertTrue(p.matcher("bat").matches());
        assertFalse(p.matcher("at").matches());
    }

    @Test
    void questionMark_matchesZeroOrOneCharacter_whenFlagSet() {
        glob.setQuestionMatchesZero(true);
        Pattern p = glob.compile("?at");
        assertTrue(p.matcher("Cat").matches());
        assertTrue(p.matcher("at").matches());
    }

    @Test
    void questionMatchesZero_defaultIsFalse() {
        assertFalse(new GlobPattern().isQuestionMatchesZero());
    }

    @Test
    void setQuestionMatchesZero_togglesToTrue() {
        glob.setQuestionMatchesZero(true);
        assertTrue(glob.isQuestionMatchesZero());
    }

    // --- character class '[]' tests ---

    @Test
    void charClass_matchesOneOfListedChars() {
        Pattern p = glob.compile("[CB]at");
        assertTrue(p.matcher("Cat").matches());
        assertTrue(p.matcher("Bat").matches());
        assertFalse(p.matcher("cat").matches());
        assertFalse(p.matcher("bat").matches());
    }

    @Test
    void negatedCharClass_excludesListedChars() {
        Pattern p = glob.compile("[!CB]at");
        assertTrue(p.matcher("cat").matches());
        assertTrue(p.matcher("bat").matches());
        assertFalse(p.matcher("Cat").matches());
    }

    @Test
    void caretNegatedCharClass_excludesListedChars() {
        Pattern p = glob.compile("[^CB]at");
        assertTrue(p.matcher("cat").matches());
        assertFalse(p.matcher("Cat").matches());
    }

    // --- regex meta character escaping ---

    @Test
    void dotInGlob_isLiteralDot() {
        Pattern p = glob.compile("foo.bar");
        assertTrue(p.matcher("foo.bar").matches());
        // A regex '.' would match 'fooXbar'; literal dot should not
        assertFalse(p.matcher("fooXbar").matches());
    }

    @Test
    void plusInGlob_isLiteralPlus() {
        Pattern p = glob.compile("a+b");
        assertTrue(p.matcher("a+b").matches());
        assertFalse(p.matcher("ab").matches());
    }

    // --- compile with options ---

    @Test
    void compileWithOptions_caseInsensitive() {
        Pattern p = glob.compile("hippo:*", Pattern.CASE_INSENSITIVE);
        assertTrue(p.matcher("HIPPO:handle").matches());
        assertTrue(p.matcher("hippo:handle").matches());
    }

    @Test
    void compile_returnsNonNull() {
        assertNotNull(glob.compile("some:*"));
    }

    // --- concrete property name patterns (representative of real usage) ---

    @Test
    void propertyNameExcludePattern_hippoPath() {
        Pattern p = glob.compile("hippo:paths");
        assertTrue(p.matcher("hippo:paths").matches());
        assertFalse(p.matcher("hippo:handle").matches());
    }

    @Test
    void wildcardStar_matchesNamespacedName() {
        Pattern p = glob.compile("myhippo:*");
        assertTrue(p.matcher("myhippo:title").matches());
        assertTrue(p.matcher("myhippo:summary").matches());
        assertFalse(p.matcher("hippo:title").matches());
    }
}

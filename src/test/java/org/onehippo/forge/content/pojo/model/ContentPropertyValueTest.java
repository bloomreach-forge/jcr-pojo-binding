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

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit 5 tests for ContentProperty value conversion methods.
 * No JCR or Spring context required.
 */
class ContentPropertyValueTest {

    // -----------------------------------------------------------------------
    // getValue / setValue
    // -----------------------------------------------------------------------

    @Test
    void getValue_returnsNull_whenNoValues() {
        ContentProperty prop = new ContentProperty("test", ContentPropertyType.STRING);
        assertNull(prop.getValue());
    }

    @Test
    void getValue_returnsFirstValue_whenMultipleAdded() {
        ContentProperty prop = new ContentProperty("test", ContentPropertyType.STRING);
        prop.addValues("first", "second", "third");
        assertEquals("first", prop.getValue());
    }

    @Test
    void setValue_replacesExistingValues() {
        ContentProperty prop = new ContentProperty("test", ContentPropertyType.STRING);
        prop.addValue("old");
        prop.setValue("new");
        assertEquals("new", prop.getValue());
        assertEquals(1, prop.getValueCount());
    }

    @Test
    void setValue_null_clearsValues() {
        ContentProperty prop = new ContentProperty("test", ContentPropertyType.STRING);
        prop.addValue("existing");
        prop.setValue((String) null);
        assertEquals(0, prop.getValueCount());
    }

    // -----------------------------------------------------------------------
    // getValueCount
    // -----------------------------------------------------------------------

    @Test
    void getValueCount_returnsZero_forNewProperty() {
        ContentProperty prop = new ContentProperty("test", ContentPropertyType.STRING);
        assertEquals(0, prop.getValueCount());
    }

    @Test
    void getValueCount_incrementsWithEachAddValue() {
        ContentProperty prop = new ContentProperty("test", ContentPropertyType.STRING, true);
        prop.addValue("a");
        prop.addValue("b");
        assertEquals(2, prop.getValueCount());
    }

    @Test
    void removeValues_clearsAll() {
        ContentProperty prop = new ContentProperty("test", ContentPropertyType.STRING, true);
        prop.addValues("a", "b", "c");
        prop.removeValues();
        assertEquals(0, prop.getValueCount());
    }

    // -----------------------------------------------------------------------
    // getValueAsObject — STRING
    // -----------------------------------------------------------------------

    @Test
    void getValueAsObject_string_returnsString() {
        ContentProperty prop = new ContentProperty("title", ContentPropertyType.STRING);
        prop.setValue("Hello World");
        Object result = prop.getValueAsObject();
        assertInstanceOf(String.class, result);
        assertEquals("Hello World", result);
    }

    @Test
    void getValueAsObject_returnsNull_whenEmpty() {
        ContentProperty prop = new ContentProperty("title", ContentPropertyType.STRING);
        assertNull(prop.getValueAsObject());
    }

    // -----------------------------------------------------------------------
    // getValueAsObject — BOOLEAN
    // -----------------------------------------------------------------------

    @Test
    void getValueAsObject_boolean_true() {
        ContentProperty prop = new ContentProperty("flag", ContentPropertyType.BOOLEAN);
        prop.setValue("true");
        assertInstanceOf(Boolean.class, prop.getValueAsObject());
        assertEquals(Boolean.TRUE, prop.getValueAsObject());
    }

    @Test
    void getValueAsObject_boolean_false() {
        ContentProperty prop = new ContentProperty("flag", ContentPropertyType.BOOLEAN);
        prop.setValue("false");
        assertEquals(Boolean.FALSE, prop.getValueAsObject());
    }

    // -----------------------------------------------------------------------
    // getValueAsObject — LONG
    // -----------------------------------------------------------------------

    @Test
    void getValueAsObject_long() {
        ContentProperty prop = new ContentProperty("count", ContentPropertyType.LONG);
        prop.setValue("42");
        Object result = prop.getValueAsObject();
        assertInstanceOf(Long.class, result);
        assertEquals(42L, result);
    }

    @Test
    void getValueAsObject_long_negative() {
        ContentProperty prop = new ContentProperty("offset", ContentPropertyType.LONG);
        prop.setValue("-1000");
        assertEquals(-1000L, prop.getValueAsObject());
    }

    // -----------------------------------------------------------------------
    // getValueAsObject — DOUBLE
    // -----------------------------------------------------------------------

    @Test
    void getValueAsObject_double() {
        ContentProperty prop = new ContentProperty("price", ContentPropertyType.DOUBLE);
        prop.setValue("3.14");
        Object result = prop.getValueAsObject();
        assertInstanceOf(Double.class, result);
        assertEquals(3.14, (Double) result, 0.0001);
    }

    // -----------------------------------------------------------------------
    // getValueAsObject — DECIMAL
    // -----------------------------------------------------------------------

    @Test
    void getValueAsObject_decimal() {
        ContentProperty prop = new ContentProperty("amount", ContentPropertyType.DECIMAL);
        prop.setValue("12345.6789");
        Object result = prop.getValueAsObject();
        assertInstanceOf(BigDecimal.class, result);
        assertEquals(new BigDecimal("12345.6789"), result);
    }

    // -----------------------------------------------------------------------
    // getValueAsObject — DATE
    // -----------------------------------------------------------------------

    @Test
    void getValueAsObject_date_returnsCalendar() {
        // Jackrabbit ISO8601 requires milliseconds: yyyy-MM-ddTHH:mm:ss.SSSZ
        ContentProperty prop = new ContentProperty("published", ContentPropertyType.DATE);
        prop.setValue("2015-12-01T12:34:56.000-05:00");
        Object result = prop.getValueAsObject();
        assertInstanceOf(Calendar.class, result);
    }

    // -----------------------------------------------------------------------
    // getValueAsObject — UNDEFINED throws
    // -----------------------------------------------------------------------

    @Test
    void getValueAsObject_undefinedType_throwsUnsupportedOperationException() {
        ContentProperty prop = new ContentProperty("x", ContentPropertyType.UNDEFINED);
        prop.addValue("anything");
        assertThrows(UnsupportedOperationException.class, prop::getValueAsObject);
    }

    // -----------------------------------------------------------------------
    // getValuesAsObject — multi-value
    // -----------------------------------------------------------------------

    @Test
    void getValuesAsObject_string_returnsAllValues() {
        ContentProperty prop = new ContentProperty("tags", ContentPropertyType.STRING, true);
        prop.addValues("news", "sport", "tech");
        List<Object> results = prop.getValuesAsObject();
        assertEquals(3, results.size());
        assertEquals("news", results.get(0));
        assertEquals("sport", results.get(1));
        assertEquals("tech", results.get(2));
    }

    @Test
    void getValuesAsObject_emptyProperty_returnsEmptyList() {
        ContentProperty prop = new ContentProperty("tags", ContentPropertyType.STRING, true);
        List<Object> results = prop.getValuesAsObject();
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    // -----------------------------------------------------------------------
    // clone
    // -----------------------------------------------------------------------

    @Test
    void clone_producesEqualButDistinctObject() {
        ContentProperty original = new ContentProperty("title", ContentPropertyType.STRING, false);
        original.addValue("Hello");
        ContentProperty clone = (ContentProperty) original.clone();

        assertEquals(original, clone);
        assertFalse(original == clone);
    }

    @Test
    void clone_valuesAreIndependent() {
        ContentProperty original = new ContentProperty("tags", ContentPropertyType.STRING, true);
        original.addValues("a", "b");
        ContentProperty clone = (ContentProperty) original.clone();

        clone.addValue("c");
        assertEquals(2, original.getValueCount());
        assertEquals(3, clone.getValueCount());
    }

    // -----------------------------------------------------------------------
    // isMultiple / setMultiple
    // -----------------------------------------------------------------------

    @Test
    void isMultiple_defaultFalse() {
        ContentProperty prop = new ContentProperty("x", ContentPropertyType.STRING);
        assertFalse(prop.isMultiple());
    }

    @Test
    void setMultiple_toTrue() {
        ContentProperty prop = new ContentProperty("x", ContentPropertyType.STRING);
        prop.setMultiple(true);
        assertTrue(prop.isMultiple());
    }

    // -----------------------------------------------------------------------
    // isNode
    // -----------------------------------------------------------------------

    @Test
    void isNode_returnsFalse() {
        assertFalse(new ContentProperty("p", ContentPropertyType.STRING).isNode());
    }

    // -----------------------------------------------------------------------
    // equals / hashCode
    // -----------------------------------------------------------------------

    @Test
    void equals_reflexive() {
        ContentProperty p = new ContentProperty("title", ContentPropertyType.STRING);
        p.setValue("v");
        assertEquals(p, p);
    }

    @Test
    void equals_differentValues_notEqual() {
        ContentProperty p1 = new ContentProperty("title", ContentPropertyType.STRING);
        p1.setValue("A");
        ContentProperty p2 = new ContentProperty("title", ContentPropertyType.STRING);
        p2.setValue("B");
        assertFalse(p1.equals(p2));
    }

    @Test
    void equals_differentTypes_notEqual() {
        ContentProperty p1 = new ContentProperty("x", ContentPropertyType.STRING);
        ContentProperty p2 = new ContentProperty("x", ContentPropertyType.LONG);
        assertFalse(p1.equals(p2));
    }
}

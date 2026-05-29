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
package org.onehippo.forge.content.pojo.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;

public class ContentPropertyTest {

    private static final String DATA_URI =
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4" +
            "//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg==";

    // ── Constructors ──────────────────────────────────────────────────────────

    @Test
    public void defaultConstructorYieldsUndefinedNonMultiple() {
        ContentProperty p = new ContentProperty();
        assertNull(p.getName());
        assertEquals(ContentPropertyType.UNDEFINED, p.getType());
        assertFalse(p.isMultiple());
        assertFalse(p.isNode());
        assertEquals(0, p.getValueCount());
        assertNull(p.getValue());
    }

    @Test
    public void twoArgConstructorSetsNameAndType() {
        ContentProperty p = new ContentProperty("title", ContentPropertyType.STRING);
        assertEquals("title", p.getName());
        assertEquals(ContentPropertyType.STRING, p.getType());
        assertFalse(p.isMultiple());
    }

    @Test
    public void threeArgConstructorSetsMultipleFlag() {
        ContentProperty p = new ContentProperty("tags", ContentPropertyType.STRING, true);
        assertTrue(p.isMultiple());
    }

    // ── Type / Multiple setters ───────────────────────────────────────────────

    @Test
    public void setTypeUpdatesType() {
        ContentProperty p = new ContentProperty("p", ContentPropertyType.STRING);
        p.setType(ContentPropertyType.LONG);
        assertEquals(ContentPropertyType.LONG, p.getType());
    }

    @Test
    public void setMultipleUpdatesFlag() {
        ContentProperty p = new ContentProperty("p", ContentPropertyType.STRING, false);
        p.setMultiple(true);
        assertTrue(p.isMultiple());
    }

    // ── Value list lazy-init ──────────────────────────────────────────────────

    @Test
    public void getValuesInitializesLazily() {
        ContentProperty p = new ContentProperty();
        assertNotNull(p.getValues());
        assertTrue(p.getValues().isEmpty());
    }

    // ── setValue(String) ─────────────────────────────────────────────────────

    @Test
    public void setValueReplacesExistingValues() {
        ContentProperty p = new ContentProperty("p", ContentPropertyType.STRING);
        p.addValue("first");
        p.setValue("second");
        assertEquals(1, p.getValueCount());
        assertEquals("second", p.getValue());
    }

    @Test
    public void setNullValueClearsAll() {
        ContentProperty p = new ContentProperty("p", ContentPropertyType.STRING);
        p.addValue("x");
        p.setValue((String) null);
        assertEquals(0, p.getValueCount());
        assertNull(p.getValue());
    }

    // ── setValue(BinaryValue) ────────────────────────────────────────────────

    @Test
    public void setValueBinaryValueStoresUriString() {
        BinaryValue bv = BinaryValue.fromDataURI(DATA_URI);
        ContentProperty p = new ContentProperty("img", ContentPropertyType.BINARY);
        p.setValue(bv);
        assertEquals(1, p.getValueCount());
        assertTrue(p.getValue().startsWith("data:image/png"));
    }

    // ── addValue / addValues ─────────────────────────────────────────────────

    @Test
    public void addValueAppendsSingleEntry() {
        ContentProperty p = new ContentProperty("p", ContentPropertyType.STRING, true);
        p.addValue("a");
        p.addValue("b");
        assertEquals(2, p.getValueCount());
        assertEquals("a", p.getValue());
    }

    @Test
    public void addValuesStringVarargAppendsAll() {
        ContentProperty p = new ContentProperty("p", ContentPropertyType.STRING, true);
        p.addValues("x", "y", "z");
        assertEquals(3, p.getValueCount());
    }

    @Test
    public void addValuesStringNullIsNoOp() {
        ContentProperty p = new ContentProperty("p", ContentPropertyType.STRING);
        p.addValues((String[]) null);
        assertEquals(0, p.getValueCount());
    }

    @Test
    public void addValuesStringEmptyArrayIsNoOp() {
        ContentProperty p = new ContentProperty("p", ContentPropertyType.STRING);
        p.addValues(new String[0]);
        assertEquals(0, p.getValueCount());
    }

    @Test
    public void addValueBinaryValueAppendsUriString() {
        BinaryValue bv1 = BinaryValue.fromDataURI(DATA_URI);
        BinaryValue bv2 = BinaryValue.fromDataURI(DATA_URI);
        ContentProperty p = new ContentProperty("p", ContentPropertyType.BINARY, true);
        p.addValue(bv1);
        p.addValue(bv2);
        assertEquals(2, p.getValueCount());
    }

    @Test
    public void addValuesBinaryValueVarargAppendsAll() {
        BinaryValue bv1 = BinaryValue.fromDataURI(DATA_URI);
        BinaryValue bv2 = BinaryValue.fromDataURI(DATA_URI);
        ContentProperty p = new ContentProperty("p", ContentPropertyType.BINARY, true);
        p.addValues(bv1, bv2);
        assertEquals(2, p.getValueCount());
    }

    @Test
    public void addValuesBinaryValueNullIsNoOp() {
        ContentProperty p = new ContentProperty("p", ContentPropertyType.BINARY);
        p.addValues((BinaryValue[]) null);
        assertEquals(0, p.getValueCount());
    }

    // ── removeValues ─────────────────────────────────────────────────────────

    @Test
    public void removeValuesClearsAll() {
        ContentProperty p = new ContentProperty("p", ContentPropertyType.STRING, true);
        p.addValues("a", "b", "c");
        p.removeValues();
        assertEquals(0, p.getValueCount());
        assertNull(p.getValue());
    }

    @Test
    public void removeValuesOnUninitialisedListIsNoOp() {
        new ContentProperty().removeValues(); // must not throw
    }

    // ── getValueCount ────────────────────────────────────────────────────────

    @Test
    public void getValueCountReflectsListSize() {
        ContentProperty p = new ContentProperty("p", ContentPropertyType.STRING);
        assertEquals(0, p.getValueCount());
        p.addValue("a");
        assertEquals(1, p.getValueCount());
        p.addValue("b");
        assertEquals(2, p.getValueCount());
    }

    // ── getValueAsObject / getValuesAsObject ──────────────────────────────────

    @Test
    public void getValueAsObjectReturnsNullWhenEmpty() {
        assertNull(new ContentProperty("p", ContentPropertyType.STRING).getValueAsObject());
    }

    @Test
    public void getValueAsObjectString() {
        ContentProperty p = new ContentProperty("p", ContentPropertyType.STRING);
        p.setValue("hello");
        assertEquals("hello", p.getValueAsObject());
    }

    @Test
    public void getValueAsObjectDate() {
        ContentProperty p = new ContentProperty("p", ContentPropertyType.DATE);
        p.setValue("2015-12-01T12:34:56.000-05:00");
        Object obj = p.getValueAsObject();
        assertNotNull(obj);
        assertTrue(obj instanceof Calendar);
    }

    @Test
    public void getValueAsObjectBoolean() {
        ContentProperty p = new ContentProperty("p", ContentPropertyType.BOOLEAN);
        p.setValue("true");
        assertEquals(Boolean.TRUE, p.getValueAsObject());
    }

    @Test
    public void getValueAsObjectLong() {
        ContentProperty p = new ContentProperty("p", ContentPropertyType.LONG);
        p.setValue("42");
        assertEquals(42L, p.getValueAsObject());
    }

    @Test
    public void getValueAsObjectDouble() {
        ContentProperty p = new ContentProperty("p", ContentPropertyType.DOUBLE);
        p.setValue("3.14");
        assertEquals(3.14, (Double) p.getValueAsObject(), 0.001);
    }

    @Test
    public void getValueAsObjectDecimal() {
        ContentProperty p = new ContentProperty("p", ContentPropertyType.DECIMAL);
        p.setValue("123.456");
        assertEquals(new BigDecimal("123.456"), p.getValueAsObject());
    }

    @Test
    public void getValueAsObjectBinaryDataUri() {
        ContentProperty p = new ContentProperty("p", ContentPropertyType.BINARY);
        p.setValue(DATA_URI);
        Object obj = p.getValueAsObject();
        assertNotNull(obj);
        assertTrue(obj instanceof BinaryValue);
        assertEquals("image/png", ((BinaryValue) obj).getMediaType());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getValueAsObjectUndefinedTypeThrows() {
        ContentProperty p = new ContentProperty("p", ContentPropertyType.UNDEFINED);
        p.setValue("x");
        p.getValueAsObject();
    }

    @Test
    public void getValuesAsObjectReturnsMappedList() {
        ContentProperty p = new ContentProperty("p", ContentPropertyType.LONG, true);
        p.addValues("1", "2", "3");
        List<Object> objs = p.getValuesAsObject();
        assertEquals(3, objs.size());
        assertEquals(1L, objs.get(0));
        assertEquals(2L, objs.get(1));
        assertEquals(3L, objs.get(2));
    }

    // ── clone ────────────────────────────────────────────────────────────────

    @Test
    public void cloneIsDeepCopy() {
        ContentProperty p = new ContentProperty("title", ContentPropertyType.STRING, true);
        p.addValues("a", "b");
        ContentProperty clone = (ContentProperty) p.clone();
        assertEquals(p, clone);
        assertNotSame(p, clone);
        p.addValue("c");
        assertEquals(2, clone.getValueCount()); // clone unaffected
    }

    // ── equals / hashCode ────────────────────────────────────────────────────

    @Test
    public void equalPropertyiesAreEqual() {
        ContentProperty p1 = new ContentProperty("title", ContentPropertyType.STRING);
        p1.setValue("hello");
        ContentProperty p2 = new ContentProperty("title", ContentPropertyType.STRING);
        p2.setValue("hello");
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void notEqualWhenNameDiffers() {
        assertNotEquals(
                new ContentProperty("a", ContentPropertyType.STRING),
                new ContentProperty("b", ContentPropertyType.STRING));
    }

    @Test
    public void notEqualWhenTypeDiffers() {
        assertNotEquals(
                new ContentProperty("a", ContentPropertyType.STRING),
                new ContentProperty("a", ContentPropertyType.LONG));
    }

    @Test
    public void notEqualWhenMultipleDiffers() {
        assertNotEquals(
                new ContentProperty("a", ContentPropertyType.STRING, false),
                new ContentProperty("a", ContentPropertyType.STRING, true));
    }

    @Test
    public void notEqualWhenValuesDiffer() {
        ContentProperty p1 = new ContentProperty("a", ContentPropertyType.STRING);
        p1.setValue("x");
        ContentProperty p2 = new ContentProperty("a", ContentPropertyType.STRING);
        p2.setValue("y");
        assertNotEquals(p1, p2);
    }

    @Test
    public void notEqualToNonContentProperty() {
        assertNotEquals(new ContentProperty("a", ContentPropertyType.STRING), "string");
    }

    // ── toString ─────────────────────────────────────────────────────────────

    @Test
    public void toStringContainsTypeAndValues() {
        ContentProperty p = new ContentProperty("title", ContentPropertyType.STRING);
        p.setValue("hello");
        String s = p.toString();
        assertNotNull(s);
        assertTrue(s.contains("STRING"));
        assertTrue(s.contains("hello"));
    }
}

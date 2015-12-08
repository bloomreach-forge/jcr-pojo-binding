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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.jackrabbit.util.ISO8601;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ContentProperty extends ContentItem {

    private static final long serialVersionUID = 1L;

    private ContentPropertyType type = ContentPropertyType.UNDEFINED;

    private boolean multiple;

    private List<String> values;

    public ContentProperty() {
        super();
    }

    public ContentProperty(String name, ContentPropertyType type) {
        this(name, type, false);
    }

    public ContentProperty(String name, ContentPropertyType type, boolean multiple) {
        super(name);
        this.type = type;
        this.multiple = multiple;
    }

    public ContentPropertyType getType() {
        return type;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public List<String> getValues() {
        if (values == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(values);
    }

    @JsonIgnore
    public String getValue() {
        if (values != null && !values.isEmpty()) {
            return values.get(0);
        }

        return null;
    }

    public void setValue(String value) {
        if (values == null) {
            values = new LinkedList<>();
        }

        if (!values.isEmpty()) {
            values.clear();
        }

        if (value != null) {
            values.add(value);
        }
    }

    public void addValue(String value) {
        if (values == null) {
            values = new LinkedList<>();
        }

        values.add(value);
    }

    public void removeValues() {
        if (values != null) {
            values.clear();
        }
    }

    @JsonIgnore
    public int getValueCount() {
        return values == null ? 0 : values.size();
    }

    @JsonIgnore
    public List<Object> getObjectValues() {
        List<Object> objectValues = new LinkedList<>();
        int valueCount = getValueCount();

        for (int i = 0; i < valueCount; i++) {
            objectValues.add(getObjectValueAt(i));
        }

        return objectValues;
    }

    @JsonIgnore
    public Object getFirstObjectValue() {
        if (values != null && !values.isEmpty()) {
            return getObjectValueAt(0);
        }

        return null;
    }

    public Object getObjectValueAt(final int index) {
        Object objectValue = null;

        final String stringifiedValue = values.get(index);

        switch (type) {
        case STRING: {
            objectValue = stringifiedValue;
            break;
        }
        case DATE: {
            objectValue = ISO8601.parse(stringifiedValue);
            break;
        }
        case BOOLEAN: {
            objectValue = Boolean.parseBoolean(stringifiedValue);
            break;
        }
        case LONG: {
            objectValue = Long.parseLong(stringifiedValue);
            break;
        }
        case DOUBLE: {
            objectValue = Double.parseDouble(stringifiedValue);
            break;
        }
        case DECIMAL: {
            objectValue = new BigDecimal(stringifiedValue);
            break;
        }
        default: {
            throw new UnsupportedOperationException(
                    "Unsupported type, " + type + ". Only primitive number/string values are currently supported.");
        }
        }

        return objectValue;
    }

    @Override
    public Object clone() {
        ContentProperty clone = new ContentProperty(getName(), type, multiple);

        if (values != null) {
            for (String value : values) {
                clone.addValue(value);
            }
        }

        return clone;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(type).append(multiple).append(values).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ContentProperty)) {
            return false;
        }

        ContentProperty that = (ContentProperty) o;

        if (!type.equals(that.type)) {
            return false;
        }

        if (multiple != that.multiple) {
            return false;
        }

        return ListUtils.isEqualList(values, that.values);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("type", type).append("multiple", multiple).append("values", values)
                .toString();
    }

}

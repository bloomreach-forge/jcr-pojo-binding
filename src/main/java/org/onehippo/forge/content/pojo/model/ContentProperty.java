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
import java.util.LinkedList;
import java.util.List;

import org.apache.jackrabbit.util.ISO8601;

public class ContentProperty extends ContentItem {

    private static final long serialVersionUID = 1L;

    private ContentPropertyType type = ContentPropertyType.UNDEFINED;

    private boolean multiple;

    private List<String> values;

    public ContentProperty() {
        super();
    }

    public ContentPropertyType getType() {
        return type;
    }

    public void setType(ContentPropertyType type) {
        this.type = type;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public List<String> getValues() {
        return values;
    }

    public String getFirstValue() {
        if (values != null && !values.isEmpty()) {
            return values.get(0);
        }

        return null;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public void addValue(String value) {
        if (values == null) {
            values = new LinkedList<>();
        }

        values.add(value);
    }

    public int getValueCount() {
        return values == null ? 0 : values.size();
    }

    public List<Object> getObjectValues() {
        List<Object> objectValues = new LinkedList<>();
        int valueCount = getValueCount();

        for (int i = 0; i < valueCount; i++) {
            objectValues.add(getObjectValueAt(i));
        }

        return objectValues;
    }

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

}

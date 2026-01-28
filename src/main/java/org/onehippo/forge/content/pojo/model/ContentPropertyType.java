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

import javax.jcr.PropertyType;

/**
 * Supported Content Property Types.
 */
public enum ContentPropertyType {

    STRING,
    BINARY,
    LONG,
    DOUBLE,
    DATE,
    BOOLEAN,
    DECIMAL,
    PATH,
    UNDEFINED;

    /**
     * Converts this ContentPropertyType to the corresponding JCR PropertyType constant.
     * @param type the ContentPropertyType to convert
     * @return the JCR PropertyType constant
     */
    public static int toJcrPropertyType(ContentPropertyType type) {
        return switch (type) {
            case STRING -> PropertyType.STRING;
            case BINARY -> PropertyType.BINARY;
            case LONG -> PropertyType.LONG;
            case DOUBLE -> PropertyType.DOUBLE;
            case DATE -> PropertyType.DATE;
            case BOOLEAN -> PropertyType.BOOLEAN;
            case DECIMAL -> PropertyType.DECIMAL;
            case PATH -> PropertyType.PATH;
            default -> PropertyType.UNDEFINED;
        };
    }
}

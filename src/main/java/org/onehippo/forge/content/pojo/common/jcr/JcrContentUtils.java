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
package org.onehippo.forge.content.pojo.common.jcr;

import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.jackrabbit.util.ISO8601;

public class JcrContentUtils {

    private JcrContentUtils() {
    }

    public static boolean isProtected(final Property prop) throws RepositoryException {
        try {
            return prop.getDefinition().isProtected();
        } catch (UnsupportedOperationException e) {
            // Ignore if org.onehippo.repository.mock.MockItemDefinition#isProtected() is unsupported.
        }

        return false;
    }

    public static String jcrValueToString(final Value value) throws RepositoryException {
        String stringifiedValue = null;

        switch (value.getType()) {
        case PropertyType.STRING:
        {
            stringifiedValue = value.getString();
            break;
        }
        case PropertyType.BINARY:
        {
            // TODO: NOT SUPPORTED YET
            // Just ignore for now...
            break;
        }
        case PropertyType.LONG:
        {
            stringifiedValue = Long.toString(value.getLong());
            break;
        }
        case PropertyType.DOUBLE:
        {
            stringifiedValue = Double.toString(value.getDouble());
            break;
        }
        case PropertyType.DATE:
        {
            stringifiedValue = ISO8601.format(value.getDate());
            break;
        }
        case PropertyType.BOOLEAN:
        {
            stringifiedValue = Boolean.toString(value.getBoolean());
            break;
        }
        case PropertyType.NAME:
        case PropertyType.PATH:
        case PropertyType.URI:
        {
            stringifiedValue = value.getString();
            break;
        }
        case PropertyType.DECIMAL:
            stringifiedValue = value.getDecimal().toString();
            break;
        }

        return stringifiedValue;
    }

}

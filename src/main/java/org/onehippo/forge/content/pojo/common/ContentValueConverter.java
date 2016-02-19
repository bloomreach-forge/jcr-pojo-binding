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
package org.onehippo.forge.content.pojo.common;

import org.onehippo.forge.content.pojo.model.BinaryValue;

/**
 * Content Value converter interface.
 *
 * @param <V> physical content value type
 */
public interface ContentValueConverter<V> {

    /**
     * Converts the {@code value} to a string.
     * @param value content data value (e.g, {@link javax.jcr.Value})
     * @return stringified value
     * @throws ContentNodeException if content node exception occurs
     */
    String toString(V value) throws ContentNodeException;

    /**
     * Converts the {@code value} (e.g, {@link javax.jcr.Value}) of {@code mimeType} to a {@link BinaryValue}.
     * @param value content data value
     * @param mimeType mime type
     * @return a {@link BinaryValue}
     * @throws ContentNodeException if content node exception occurs
     */
    BinaryValue toBinaryValue(V value, String mimeType) throws ContentNodeException;

    /**
     * Converts the given stringified value ({@code stringValue}) of the type ({@code typeName})
     * to a proper JCR value.
     * @param typeName content value type name
     * @param stringValue stringified content value
     * @return a proper JCR value from the stringified value
     * @throws ContentNodeException if content node exception occurs
     */
    V toJcrValue(String typeName, String stringValue) throws ContentNodeException;

    /**
     * Converts the given {@link BinaryValue} object to a JCR {@link javax.jcr.Binary} value.
     * @param binaryValue content binary value
     * @return a JCR {@link javax.jcr.Binary} value
     * @throws ContentNodeException if content node exception occurs
     */
    V toJcrValue(BinaryValue binaryValue) throws ContentNodeException;

}

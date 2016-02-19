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
package org.onehippo.forge.content.pojo.mapper;

import java.io.Serializable;

import org.onehippo.forge.content.pojo.common.ContentValueConverter;
import org.onehippo.forge.content.pojo.model.ContentNode;

/**
 * Content Node Mapper interface, mapping from a physical <code>dataNode</code> to a {@link ContentNode} instance.
 *
 * @param <D> physical data node (e.g, {@link javax.jcr.Node}) from which a {@link ContentNode} should be converted to.
 * @param <I> physical data item (e.g, {@link javax.jcr.Item}) to be used in filtering in conversion.
 * @param <V> value converter from the physical data value (e.g, {@link javax.jcr.Value}) to be used in converting a physical value.
 */
public interface ContentNodeMapper<D, I, V> extends Serializable {

    /**
     * Maps {@code dataNode} to a {@link ContentNode}.
     * @param dataNode physical data node
     * @return a converted {@link ContentNode}
     * @throws ContentNodeMappingException if any content node mapping exception occurs
     */
    public ContentNode map(D dataNode) throws ContentNodeMappingException;

    /**
     * Maps {@code dataNode} to a {@link ContentNode} using {@code itemFilter}.
     * @param dataNode physical data node
     * @param itemFilter filter for a physical data item
     * @return a converted {@link ContentNode}
     * @throws ContentNodeMappingException if any content node mapping exception occurs
     */
    public ContentNode map(D dataNode, ContentNodeMappingItemFilter<I> itemFilter) throws ContentNodeMappingException;

    /**
     * Maps {@code dataNode} to a {@link ContentNode} using {@code itemFilter} and {@code valueConverter}.
     * @param dataNode physical data node
     * @param itemFilter filter for a physical data item
     * @param valueConverter converter used in converting a physical data value
     * @return a converted {@link ContentNode}
     * @throws ContentNodeMappingException if any content node mapping exception occurs
     */
    public ContentNode map(D dataNode, ContentNodeMappingItemFilter<I> itemFilter, ContentValueConverter<V> valueConverter) throws ContentNodeMappingException;

}

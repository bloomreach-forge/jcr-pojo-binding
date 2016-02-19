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
package org.onehippo.forge.content.pojo.binder;

import java.io.Serializable;

import org.onehippo.forge.content.pojo.common.ContentValueConverter;
import org.onehippo.forge.content.pojo.model.ContentItem;
import org.onehippo.forge.content.pojo.model.ContentNode;
import org.onehippo.forge.content.pojo.model.ContentProperty;

/**
 * Content Node Binder interface, binding from a {@link ContentNode} to a physical data node.
 *
 * @param <D> physical data node (e.g, {@link javax.jcr.Node}) to which the {@link ContentNode} should bind.
 * @param <I> content item (e.g, {@link ContentItem}) used when filtering a content item (e.g, {@link ContentItem}).
 * @param <V> content value used when converting a value of {@link ContentProperty}.
 */
public interface ContentNodeBinder<D, I, V> extends Serializable {

    /**
     * Binds the {@code source} to the {@code dataNode}.
     * @param dataNode physical data node to bind to.
     * @param source {@link ContentNode} source to bind from.
     * @throws ContentNodeBindingException if content node binding exception occurs
     */
    public void bind(D dataNode, ContentNode source) throws ContentNodeBindingException;

    /**
     * Binds the {@code source} to the {@code dataNode} with the given {@code itemFilter}.
     * @param dataNode physical data node to bind to.
     * @param source {@link ContentNode} source to bind from.
     * @param itemFilter content item filter
     * @throws ContentNodeBindingException if content node binding exception occurs
     */
    public void bind(D dataNode, ContentNode source, ContentNodeBindingItemFilter<I> itemFilter)
            throws ContentNodeBindingException;

    /**
     * Binds the {@code source} to the {@code dataNode} with the given {@code itemFilter} and {@code valueConverter}.
     * @param dataNode physical data node to bind to.
     * @param source {@link ContentNode} source to bind from.
     * @param itemFilter content item filter
     * @param valueConverter value converter
     * @throws ContentNodeBindingException if content node binding exception occurs
     */
    public void bind(D dataNode, ContentNode source, ContentNodeBindingItemFilter<I> itemFilter,
            ContentValueConverter<V> valueConverter) throws ContentNodeBindingException;

}

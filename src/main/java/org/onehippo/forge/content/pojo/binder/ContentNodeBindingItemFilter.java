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

import org.onehippo.forge.content.pojo.model.ContentItem;
import org.onehippo.forge.content.pojo.model.ContentNode;

/**
 * Filter used when filtering a {@link ContentItem} in binding from a {@link ContentNode} to a physical data node.
 *
 * @param <I> content item (e.g, {@link ContentItem}) filter.
 */
public interface ContentNodeBindingItemFilter<I> {

    /**
     * Returns true if the given {@code item} can be accepted in the binding process.
     * @param item content item
     * @return true if the given {@code item} can be accepted in the binding process
     * @throws ContentNodeBindingException if any content node binding exception occurs
     */
    boolean accept(I item) throws ContentNodeBindingException;

}

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

import org.onehippo.forge.content.pojo.model.ContentNode;

/**
 * Filter used when filtering a physical data item in mapping from a physical data to a {@link ContentNode}.
 *
 * @param <I> physical data item (e.g, {@link javax.jcr.Item}).
 */
public interface ContentNodeMappingItemFilter<I> {

    /**
     * Returns true if the given {@code item} can be accepted in the mapping process.
     * @param item physical content data item (e.g, {@link javax.jcr.Item}) 
     * @return true if the given {@code item} can be accepted in the mapping process
     * @throws ContentNodeMappingException if any content node mapping exception occurs
     */
    boolean accept(I item) throws ContentNodeMappingException;

}

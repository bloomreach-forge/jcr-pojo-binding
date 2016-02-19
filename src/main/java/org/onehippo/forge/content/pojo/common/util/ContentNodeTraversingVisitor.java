/**
 * Copyright 2015-2015 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onehippo.forge.content.pojo.common.util;

import org.onehippo.forge.content.pojo.model.ContentNode;

/**
 * {@link ContentNode} traversing visitor interface.
 */
public interface ContentNodeTraversingVisitor {

    /**
     * Returns true if the given {@code contentNode} is acceptable in this traversing to continue.
     * @param contentNode content node
     * @return true if the given {@code contentNode} is acceptable in this traversing to continue
     */
    boolean isAcceptable(ContentNode contentNode);

    /**
     * Visits the given {@code contentNode}.
     * @param contentNode content node
     */
    void accept(ContentNode contentNode);

    /**
     * Returns true if the traversing should continue with the descendant content nodes.
     * @param contentNode content node
     * @return true if the traversing should continue with the descendant content nodes
     */
    boolean isDownTraversable(ContentNode contentNode);

}

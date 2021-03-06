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

import org.onehippo.forge.content.pojo.common.ContentNodeException;

/**
 * ContentNodeMappingException.
 */
public class ContentNodeMappingException extends ContentNodeException {

    private static final long serialVersionUID = 1L;

    public ContentNodeMappingException() {
        super();
    }

    public ContentNodeMappingException(String message) {
        super(message);
    }

    public ContentNodeMappingException(Throwable nested) {
        super(nested);
    }

    public ContentNodeMappingException(String msg, Throwable nested) {
        super(msg, nested);
    }
}

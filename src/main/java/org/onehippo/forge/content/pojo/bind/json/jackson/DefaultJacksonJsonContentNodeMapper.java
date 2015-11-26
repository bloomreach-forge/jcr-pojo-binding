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
package org.onehippo.forge.content.pojo.bind.json.jackson;

import org.onehippo.forge.content.pojo.bind.ContentNodeMapper;
import org.onehippo.forge.content.pojo.bind.ContentNodeMappingException;
import org.onehippo.forge.content.pojo.bind.ItemFilter;
import org.onehippo.forge.content.pojo.model.ContentNode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DefaultJacksonJsonContentNodeMapper extends BaseJacksonJsonContentNodeHandler implements ContentNodeMapper<JsonNode, JsonNode> {

    private static final long serialVersionUID = 1L;

    private ObjectMapper objectMapper;

    public DefaultJacksonJsonContentNodeMapper() {
        super();
    }

    @Override
    public ContentNode map(JsonNode dataNode) throws ContentNodeMappingException {
        return map(dataNode, null);
    }

    @Override
    public ContentNode map(JsonNode dataNode, ItemFilter<JsonNode> itemFilter) throws ContentNodeMappingException {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }

        ContentNode contentNode = null;

        try {
            contentNode = objectMapper.treeToValue(dataNode, ContentNode.class);
        } catch (JsonProcessingException e) {
            throw new ContentNodeMappingException(e.toString(), e);
        }

        return contentNode;
    }

}

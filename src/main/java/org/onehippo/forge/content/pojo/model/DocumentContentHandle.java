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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DocumentContentHandle extends ContentNode {

    private static final long serialVersionUID = 1L;

    private Map<String, DocumentContent> documents;

    public DocumentContentHandle() {
        super();
    }

    public Map<String, DocumentContent> getDocuments() {
        if (documents == null) {
            return Collections.emptyMap();
        }

        return documents;
    }

    public void setDocuments(Map<String, DocumentContent> documents) {
        this.documents = documents;
    }

    public void putDocument(String rendition, DocumentContent document) {
        if (documents == null) {
            documents = new LinkedHashMap<>();
        }

        documents.put(rendition, document);
    }
}

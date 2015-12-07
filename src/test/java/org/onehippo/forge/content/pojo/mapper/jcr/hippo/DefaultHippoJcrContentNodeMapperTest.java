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
package org.onehippo.forge.content.pojo.mapper.jcr.hippo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.hippoecm.repository.HippoStdNodeType;
import org.hippoecm.repository.api.HippoNodeType;
import org.junit.Before;
import org.junit.Test;
import org.onehippo.forge.content.pojo.common.jcr.hippo.BaseHippoJcrContentNodeTest;
import org.onehippo.forge.content.pojo.mapper.ContentNodeMappingItemFilter;
import org.onehippo.forge.content.pojo.mapper.jcr.hippo.DefaultHippoJcrContentNodeMapper;
import org.onehippo.forge.content.pojo.mapper.jcr.hippo.SpecificVariantAndNonVariantNodeMappingFilter;
import org.onehippo.forge.content.pojo.model.ContentNode;
import org.onehippo.forge.content.pojo.model.ContentProperty;
import org.onehippo.forge.content.pojo.model.DocumentContentHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultHippoJcrContentNodeMapperTest extends BaseHippoJcrContentNodeTest {

    private static Logger log = LoggerFactory.getLogger(DefaultHippoJcrContentNodeMapperTest.class);

    private DefaultHippoJcrContentNodeMapper mapper;
    private ContentNodeMappingItemFilter<Item> nonLiveVariantNodeFilter = new SpecificVariantAndNonVariantNodeMappingFilter(HippoStdNodeType.UNPUBLISHED);

    @Before
    public void setUp() throws Exception {
        super.setUp();
        mapper = new DefaultHippoJcrContentNodeMapper();
    }

    @Test
    public void testMapDocumentVariant() throws Exception {
        Node liveVariantNode = getRootNode().getNode(StringUtils.removeStart(NEWS1_DOC_HANDLE_PATH + "/news1", "/"));
        Node previewVariantNode = getRootNode().getNode(StringUtils.removeStart(NEWS1_DOC_HANDLE_PATH + "/news1[2]", "/"));

        ContentNode liveContentNode = mapper.map(liveVariantNode);
        assertNotNull(liveContentNode);
        assertDocumentVariantContentNode(liveContentNode, HippoStdNodeType.PUBLISHED);

        ContentNode previewContentNode = mapper.map(previewVariantNode);
        assertNotNull(previewContentNode);
        assertDocumentVariantContentNode(previewContentNode, HippoStdNodeType.UNPUBLISHED);
    }

    @Test
    public void testMapDocumentHandle() throws Exception {
        Node handleNode = getRootNode().getNode(StringUtils.removeStart(NEWS1_DOC_HANDLE_PATH, "/"));

        DocumentContentHandle contentHandle = (DocumentContentHandle) mapper.map(handleNode);
        assertEquals(HippoNodeType.NT_HANDLE, contentHandle.getPrimaryType());

        ContentNode translationContentNode = contentHandle.getNode(HippoNodeType.HIPPO_TRANSLATION);
        assertEquals(HippoNodeType.NT_TRANSLATION, translationContentNode.getPrimaryType());
        assertEquals("en", translationContentNode.getProperty(HippoNodeType.HIPPO_LANGUAGE).getFirstValue());
        assertEquals("News 1", translationContentNode.getProperty(HippoNodeType.HIPPO_MESSAGE).getFirstValue());

        assertEquals(2, contentHandle.getDocuments().size());

        ContentNode liveContentNode = contentHandle.getDocuments().get(HippoStdNodeType.PUBLISHED);
        assertNotNull(liveContentNode);
        assertDocumentVariantContentNode(liveContentNode, HippoStdNodeType.PUBLISHED);

        ContentNode previewContentNode = contentHandle.getDocuments().get(HippoStdNodeType.UNPUBLISHED);
        assertNotNull(previewContentNode);
        assertDocumentVariantContentNode(previewContentNode, HippoStdNodeType.UNPUBLISHED);
    }

    @Test
    public void testMapDocumentHandleWithPreviewOnly() throws Exception {
        Node handleNode = getRootNode().getNode(StringUtils.removeStart(NEWS1_DOC_HANDLE_PATH, "/"));

        DocumentContentHandle contentHandle = (DocumentContentHandle) mapper.map(handleNode, nonLiveVariantNodeFilter);
        assertEquals(HippoNodeType.NT_HANDLE, contentHandle.getPrimaryType());

        assertEquals(1, contentHandle.getDocuments().size());

        ContentNode previewContentNode = contentHandle.getDocuments().get(HippoStdNodeType.UNPUBLISHED);
        assertNotNull("documents key set: " + contentHandle.getDocuments().keySet(), previewContentNode);
        assertDocumentVariantContentNode(previewContentNode, HippoStdNodeType.UNPUBLISHED);
    }

    @Test
    public void testMapDocumentFolder() throws Exception {
        Node handleNode = getRootNode().getNode(StringUtils.removeStart(NEWS1_DOC_HANDLE_PATH, "/"));
        Node folderNode = handleNode.getParent();

        ContentNode folderContentNode = mapper.map(folderNode);
        assertEquals(HippoStdNodeType.NT_FOLDER, folderContentNode.getPrimaryType());
        assertTrue(folderContentNode.getProperty("hippostd:foldertype").getValues().contains("new-translated-folder"));
        assertTrue(folderContentNode.getProperty("hippostd:foldertype").getValues().contains("new-document"));

        ContentNode translationContentNode = folderContentNode.getNode(HippoNodeType.HIPPO_TRANSLATION);
        assertEquals(HippoNodeType.NT_TRANSLATION, translationContentNode.getPrimaryType());
        assertEquals("en", translationContentNode.getProperty(HippoNodeType.HIPPO_LANGUAGE).getFirstValue());
        assertEquals("2015", translationContentNode.getProperty(HippoNodeType.HIPPO_MESSAGE).getFirstValue());

        DocumentContentHandle contentHandle = (DocumentContentHandle) folderContentNode.getNode("news1");
        assertEquals(HippoNodeType.NT_HANDLE, contentHandle.getPrimaryType());

        translationContentNode = contentHandle.getNode(HippoNodeType.HIPPO_TRANSLATION);
        assertEquals(HippoNodeType.NT_TRANSLATION, translationContentNode.getPrimaryType());
        assertEquals("en", translationContentNode.getProperty(HippoNodeType.HIPPO_LANGUAGE).getFirstValue());
        assertEquals("News 1", translationContentNode.getProperty(HippoNodeType.HIPPO_MESSAGE).getFirstValue());

        assertEquals(2, contentHandle.getDocuments().size());

        ContentNode liveContentNode = contentHandle.getDocuments().get(HippoStdNodeType.PUBLISHED);
        assertNotNull(liveContentNode);
        assertDocumentVariantContentNode(liveContentNode, HippoStdNodeType.PUBLISHED);

        ContentNode previewContentNode = contentHandle.getDocuments().get(HippoStdNodeType.UNPUBLISHED);
        assertNotNull(previewContentNode);
        assertDocumentVariantContentNode(previewContentNode, HippoStdNodeType.UNPUBLISHED);
    }

    private void assertDocumentVariantContentNode(ContentNode varContentNode, String state) throws RepositoryException {
        log.debug("===== varContentNode: {}", ReflectionToStringBuilder.toString(varContentNode));

        for (ContentProperty contentProp : varContentNode.getProperties()) {
            log.debug("----- contentProp: {}", ReflectionToStringBuilder.toString(contentProp));
        }

        if (HippoStdNodeType.PUBLISHED.equals(state)) {
            assertEquals(HippoStdNodeType.PUBLISHED,
                    varContentNode.getProperty(HippoStdNodeType.HIPPOSTD_STATE).getFirstValue());
            assertTrue(varContentNode.getProperty(HippoNodeType.HIPPO_AVAILABILITY).getValues().contains("live"));
            assertTrue(varContentNode.getProperty(HippoNodeType.HIPPO_AVAILABILITY).getValues().contains("preview"));
            assertEquals("live",
                    varContentNode.getProperty(HippoStdNodeType.HIPPOSTD_STATESUMMARY).getFirstValue());
        } else if (HippoStdNodeType.UNPUBLISHED.equals(state)) {
            assertEquals(HippoStdNodeType.UNPUBLISHED,
                    varContentNode.getProperty(HippoStdNodeType.HIPPOSTD_STATE).getFirstValue());
            assertFalse(varContentNode.getProperty(HippoNodeType.HIPPO_AVAILABILITY).getValues().contains("live"));
            assertTrue(varContentNode.getProperty(HippoNodeType.HIPPO_AVAILABILITY).getValues().contains("preview"));
            assertEquals("changed",
                    varContentNode.getProperty(HippoStdNodeType.HIPPOSTD_STATESUMMARY).getFirstValue());
        }

        assertEquals(NEWS_NODE_TYPE, varContentNode.getPrimaryType());
        assertEquals(DOC_VARIANT_NODE_MIXIN_TYPES, varContentNode.getMixinTypes());

        assertEquals(NEWS_TITLE_PROP_VALUE, varContentNode.getProperty(NEWS_TITLE_PROP_NAME).getFirstValue());
        assertEquals(NEWS_DATE_PROP_STRING_VALUE, varContentNode.getProperty(NEWS_DATE_PROP_NAME).getFirstValue());
        assertEquals(NEWS_SUMMARY_PROP_VALUE, varContentNode.getProperty(NEWS_SUMMARY_PROP_NAME).getFirstValue());

        ContentNode bodyContentNode = varContentNode.getNode(NEWS_BODY_NODE_NAME);
        assertNotNull(bodyContentNode);
        log.debug("bodyContentNode: {}", ReflectionToStringBuilder.toString(bodyContentNode));

        assertEquals("hippostd:html", bodyContentNode.getPrimaryType());
        assertEquals(NEWS_BODY_CONTENT_VALUE, bodyContentNode.getProperty(HippoStdNodeType.HIPPOSTD_CONTENT).getFirstValue());

        ContentNode imageLinkContentNode = varContentNode.getNode(NEWS_IMAGE_LINK_NODE_NAME);
        assertNotNull(imageLinkContentNode);
        log.debug("imageLinkContentNode: {}", ReflectionToStringBuilder.toString(imageLinkContentNode));

        assertEquals("hippogallerypicker:imagelink", imageLinkContentNode.getPrimaryType());
        Node newsImageSetHandleNode = getRootNode().getNode(StringUtils.removeStart(NEWS1_IMAGE_SET_HANDLE_PATH, "/"));
        assertEquals(newsImageSetHandleNode.getIdentifier(), imageLinkContentNode.getProperty(HippoNodeType.HIPPO_DOCBASE).getFirstValue());
    }

}

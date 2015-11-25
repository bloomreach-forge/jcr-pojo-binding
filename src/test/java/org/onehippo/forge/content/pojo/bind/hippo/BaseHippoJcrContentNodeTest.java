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
package org.onehippo.forge.content.pojo.bind.hippo;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.jackrabbit.util.ISO8601;
import org.hippoecm.repository.HippoStdNodeType;
import org.hippoecm.repository.api.HippoNodeType;
import org.junit.Before;
import org.onehippo.repository.mock.MockBinary;
import org.onehippo.repository.mock.MockNode;

public class BaseHippoJcrContentNodeTest {

    protected static final String MY_HIPPO_PROJECT_NS_PREFIX = "myhippoproject";

    protected static final String NEWS1_DOC_HANDLE_PATH =
            "/content/documents/" + MY_HIPPO_PROJECT_NS_PREFIX + "/news/2015/news1";

    protected static final String NEWS1_IMAGE_SET_HANDLE_PATH =
            "/content/gallery/" + MY_HIPPO_PROJECT_NS_PREFIX + "/news/2015/news-image-1.jpg";

    protected static final String NEWS_NODE_TYPE = MY_HIPPO_PROJECT_NS_PREFIX + ":news";

    protected static final Set<String> DOC_VARIANT_NODE_MIXIN_TYPES = new LinkedHashSet<>(
            Arrays.asList(
                    HippoNodeType.NT_DOCUMENT,
                    HippoStdNodeType.NT_PUBLISHABLE,
                    "hippotranslation:translated",
                    "mix:referenceable",
                    "hippo:container",
                    "hippo:derived",
                    "hippostd:container",
                    "hippostd:publishable",
                    "hippostd:publishableSummary",
                    "hippostd:relaxed",
                    "hippostdpubwf:document",
                    "hippo:translated"));

    protected static final String NEWS_TITLE_PROP_NAME = MY_HIPPO_PROJECT_NS_PREFIX + ":title";
    protected static final String NEWS_DATE_PROP_NAME = MY_HIPPO_PROJECT_NS_PREFIX + ":date";
    protected static final String NEWS_SUMMARY_PROP_NAME = MY_HIPPO_PROJECT_NS_PREFIX + ":summary";

    protected static final String NEWS_BODY_NODE_NAME = MY_HIPPO_PROJECT_NS_PREFIX + ":body";
    protected static final String NEWS_IMAGE_LINK_NODE_NAME = MY_HIPPO_PROJECT_NS_PREFIX + ":image";

    protected static final String NEWS_TITLE_PROP_VALUE = "Solar power: the sky is the limit";
    protected static final String NEWS_DATE_PROP_STRING_VALUE = "2015-11-25T00:00:00.000-05:00";
    protected static final String NEWS_SUMMARY_PROP_VALUE = "This week’s record-breaking night flight by the Solar Impulse aircraft has been widely acclaimed, but few of us are ever likely to fly in such a plane.";
    protected static final String NEWS_BODY_CONTENT_VALUE = "<html><body><p><strong>Twice the size, ten times the problems</strong></p></body></html>";

    protected MockBinary newsImageBinary;

    private MockNode rootNode;

    @Before
    public void setUp() throws Exception {
        rootNode = MockNode.root();

        MockNode contentNode = createHippoFolderNode(rootNode, "content", null, null);

        newsImageBinary = new MockBinary(new ByteArrayInputStream(new byte[0]));

        MockNode galleryNode = createHippoGalleryFolderNode(contentNode, "gallery", null, null);
        MockNode myhippoprojectGalleryNode = createHippoGalleryFolderNode(galleryNode, MY_HIPPO_PROJECT_NS_PREFIX, null, null);
        MockNode newsGalleryNode = createHippoGalleryFolderNode(myhippoprojectGalleryNode, "news", "News", "en");
        MockNode newsGallery2015Node = createHippoGalleryFolderNode(newsGalleryNode, "2015", "2015", "en");
        MockNode newsImageSetHandleNode = createHippoGalleryHandleNode(newsGallery2015Node, "news-image-1.jpg");
        MockNode newsImageSetNode = createHippoGalleryImageSetNode(newsImageSetHandleNode);
        MockNode newsImageThumbnailNode = createHippoGalleryImageNode(newsImageSetNode, "thumbnail", "image/jpeg", newsImageBinary, 10, 10);
        MockNode newsImageOriginalNode = createHippoGalleryImageNode(newsImageSetNode, "original", "image/jpeg", newsImageBinary, 10, 10);

        MockNode documentsNode = createHippoFolderNode(contentNode, "documents", null, null);
        MockNode myhippoprojectNode = createHippoFolderNode(documentsNode, MY_HIPPO_PROJECT_NS_PREFIX, null, null);
        MockNode newsNode = createHippoFolderNode(myhippoprojectNode, "news", "News", "en");
        MockNode newsFolder2015Node = createHippoFolderNode(newsNode, "2015", "2015", "en");
        MockNode newsArticleHandleNode = createHippoDocumentHandleNode(newsFolder2015Node, "news1", "News 1", "en");

        MockNode newsArticleLiveNode = createHippoDocumentVariantNode(newsArticleHandleNode, NEWS_NODE_TYPE,
                HippoStdNodeType.PUBLISHED);
        MockNode newsArticlePreviewNode = createHippoDocumentVariantNode(newsArticleHandleNode, NEWS_NODE_TYPE,
                HippoStdNodeType.UNPUBLISHED);

        MockNode [] variants = new MockNode [] { newsArticleLiveNode, newsArticlePreviewNode };

        for (MockNode variant : variants) {
            variant.setProperty(NEWS_TITLE_PROP_NAME, NEWS_TITLE_PROP_VALUE);
            variant.setProperty(NEWS_DATE_PROP_NAME, ISO8601.parse(NEWS_DATE_PROP_STRING_VALUE));
            variant.setProperty(NEWS_SUMMARY_PROP_NAME, NEWS_SUMMARY_PROP_VALUE);

            MockNode htmlNode = variant.addNode(NEWS_BODY_NODE_NAME, "hippostd:html");
            htmlNode.setProperty(HippoStdNodeType.HIPPOSTD_CONTENT, NEWS_BODY_CONTENT_VALUE);

            MockNode imageLinkNode = variant.addNode(NEWS_IMAGE_LINK_NODE_NAME, "hippogallerypicker:imagelink");
            imageLinkNode.setProperty(HippoNodeType.HIPPO_DOCBASE, newsImageSetHandleNode.getIdentifier());
        }
    }

    protected MockNode createHippoGalleryFolderNode(MockNode baseNode, String name, String translationName, String translationLanguage) throws Exception {
        MockNode galleryFolderNode = baseNode.addNode(name, "hippogallery:stdImageGallery");
        galleryFolderNode.addMixin("mix:referenceable");
        galleryFolderNode.setProperty("hippostd:foldertype", new String [] { "new-image-folder" });
        galleryFolderNode.setProperty("hippostd:gallerytype", new String [] { "hippogallery:imageset" });

        if (translationName != null) {
            MockNode translationNode = galleryFolderNode.addNode(HippoNodeType.HIPPO_TRANSLATION, HippoNodeType.NT_TRANSLATION);
            translationNode.setProperty(HippoNodeType.HIPPO_LANGUAGE, translationLanguage);
            translationNode.setProperty(HippoNodeType.HIPPO_MESSAGE, translationName);
        }

        return galleryFolderNode;
    }

    protected MockNode createHippoGalleryHandleNode(MockNode baseNode, String name) throws Exception {
        MockNode handleNode = baseNode.addNode(name, HippoNodeType.NT_HANDLE);
        handleNode.addMixin("mix:referenceable");
        return handleNode;
    }

    protected MockNode createHippoGalleryImageSetNode(MockNode handleNode) throws Exception {
        MockNode imageSetNode = handleNode.addNode(handleNode.getName(), "hippogallery:imageset");
        imageSetNode.addMixin("mix:referenceable");
        imageSetNode.setProperty(HippoNodeType.HIPPO_AVAILABILITY, new String [] { "live", "preview" });
        imageSetNode.setProperty("hippogallery:filename", handleNode.getName());
        return imageSetNode;
    }

    protected MockNode createHippoGalleryImageNode(MockNode imageSetNode, String name, String mimeType, MockBinary data,
            long width, long height) throws Exception {
        MockNode imageNode = imageSetNode.addNode(name, "hippogallery:image");
        imageNode.setProperty("hippogallery:width", width);
        imageNode.setProperty("hippogallery:height", height);
        imageNode.setProperty("jcr:mimeType", mimeType);
        imageNode.setProperty("jcr:data", data);
        return imageNode;
    }

    protected MockNode createHippoFolderNode(MockNode baseNode, String name, String translationName, String translationLanguage) throws Exception {
        MockNode folderNode = baseNode.addNode(name, HippoStdNodeType.NT_FOLDER);
        folderNode.addMixin("hippo:translated");
        folderNode.addMixin("mix:referenceable");
        folderNode.setProperty("hippostd:foldertype", new String [] { "new-translated-folder", "new-document" });

        if (translationName != null) {
            MockNode translationNode = folderNode.addNode(HippoNodeType.HIPPO_TRANSLATION, HippoNodeType.NT_TRANSLATION);
            translationNode.setProperty(HippoNodeType.HIPPO_LANGUAGE, translationLanguage);
            translationNode.setProperty(HippoNodeType.HIPPO_MESSAGE, translationName);
        }

        return folderNode;
    }

    protected MockNode createHippoDocumentHandleNode(MockNode baseNode, String name, String translationName, String translationLanguage) throws Exception {
        MockNode handleNode = baseNode.addNode(name, HippoNodeType.NT_HANDLE);
        handleNode.addMixin("mix:referenceable");
        handleNode.addMixin(HippoNodeType.NT_TRANSLATED);

        if (translationName != null) {
            MockNode translationNode = handleNode.addNode(HippoNodeType.HIPPO_TRANSLATION, HippoNodeType.NT_TRANSLATION);
            translationNode.setProperty(HippoNodeType.HIPPO_LANGUAGE, translationLanguage);
            translationNode.setProperty(HippoNodeType.HIPPO_MESSAGE, translationName);
        }

        return handleNode;
    }

    protected MockNode createHippoDocumentVariantNode(MockNode handleNode, String primaryType, String state) throws Exception {
        MockNode variantNode = handleNode.addNode(handleNode.getName(), primaryType);

        for (String mixin : DOC_VARIANT_NODE_MIXIN_TYPES) {
            variantNode.addMixin(mixin);
        }

        if (HippoStdNodeType.PUBLISHED.equals(state)) {
            variantNode.setProperty(HippoNodeType.HIPPO_AVAILABILITY, new String [] { "live", "preview" });
            variantNode.setProperty(HippoStdNodeType.HIPPOSTD_STATE, HippoStdNodeType.PUBLISHED);
            variantNode.setProperty(HippoStdNodeType.HIPPOSTD_STATESUMMARY, "live");
        } else {
            variantNode.setProperty(HippoNodeType.HIPPO_AVAILABILITY, new String [] { "preview" });
            variantNode.setProperty(HippoStdNodeType.HIPPOSTD_STATE, HippoStdNodeType.UNPUBLISHED);
            variantNode.setProperty(HippoStdNodeType.HIPPOSTD_STATESUMMARY, "changed");
        }

        return variantNode;
    }

    protected MockNode getRootNode() {
        return rootNode;
    }

}

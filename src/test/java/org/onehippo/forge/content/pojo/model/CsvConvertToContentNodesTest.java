/*
 *  Copyright 2015-2024 Hippo B.V. (http://www.onehippo.com)
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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Iterator;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.hippoecm.repository.HippoStdNodeType;
import org.hippoecm.repository.api.StringCodec;
import org.hippoecm.repository.api.StringCodecFactory;
import org.hippoecm.repository.translation.HippoTranslationNodeType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CsvConvertToContentNodesTest {

    private static Logger log = LoggerFactory.getLogger(CsvConvertToContentNodesTest.class);

    private URL NEWS_CSV_URL = CsvConvertToContentNodesTest.class.getResource("news.csv");

    @Test
    public void testReadCsvAndConvertToContentNodes() throws Exception {

        try (InputStream input = NEWS_CSV_URL.openStream();InputStreamReader reader = new InputStreamReader(input, "UTF-8");){
            // 1. Open a reader from a CSV file.



            // 2. Create CSV parser to parse the CSV data with column headers.
            CSVParser parser = CSVFormat.DEFAULT.withHeader("Title", "Introduction", "Date", "Content")
                    .withSkipHeaderRecord().parse(reader);
            CSVRecord record;

            // 3. StringCodec to generate a JCR node name from the title column,
            //    and ObjectMapper instance to log a ContentNode to JSON.
            final StringCodec codec = new StringCodecFactory.UriEncoding();
            final ObjectMapper objectMapper = new ObjectMapper();

            String name;
            String title;
            String introduction;
            String date;
            String content;

            String translationId;
            String translationLocale = "en";

            String targetDocumentLocation;

            // 4. Iterate each data record and create a ContentNode for a news article with setting properties and child nodes.
            for (Iterator<CSVRecord> it = parser.iterator(); it.hasNext(); ) {
                record = it.next();

                // 4.1. Read each column from a CSV record.
                title = record.get("Title");
                name = codec.encode(title);
                introduction = record.get("Introduction");
                date = record.get("Date");
                content = record.get("Content");

                // 4.2. Create a ContentNode for a news article and set primitive property values.
                ContentNode newsNode = new ContentNode(name, "ns1:newsdocument");
                newsNode.setProperty("ns1:title", title);
                newsNode.setProperty("ns1:introduction", introduction);
                newsNode.setProperty("ns1:date", ContentPropertyType.DATE, date);

                // 4.3. Create/add a child hippostd:html content node and set the content.
                ContentNode htmlNode = new ContentNode("ns1:content", HippoStdNodeType.NT_HTML);
                newsNode.addNode(htmlNode);
                htmlNode.setProperty(HippoStdNodeType.HIPPOSTD_CONTENT, content);

                // 4.4. In Hippo CMS, the internal translation UUID and locale string are important in most cases.
                //      So, let's generate a translation UUID and use 'en' for simplicity for now.
                translationId = UUID.randomUUID().toString();
                newsNode.setProperty(HippoTranslationNodeType.ID, translationId);
                newsNode.setProperty(HippoTranslationNodeType.LOCALE, translationLocale);

                // 4.5. (Optional) Set kind of meta property for localized document name which is displayed in folder view later.
                //      This meta property is not used by Hippo CMS, but can be read/used by a higher level content importing application
                //      to set a localized (translated) name of the document (e.g, using Hippo TranslationWorkflow).
                newsNode.setProperty("jcr:localizedName", title);

                // 4.6. (Optional) Determine the target document location where this content should be generated and
                //      store it in a meta property, jcr:path.
                //      This meta property cannot be used in JCR repository in importing process, but can be read/used by a higher level
                //      content importing application to create a document using Hippo DocumentWorkflow for instance.
                targetDocumentLocation = "/content/documents/ns1/news/" + name;
                newsNode.setProperty("jcr:path", targetDocumentLocation);

                // 4.7. (Optional) Log the JSON-ized string of the news ContentNode instance.
                StringWriter stringWriter = new StringWriter(256);
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(stringWriter, newsNode);
                log.debug("newsNode: \n{}\n", stringWriter.toString());
            }
        }
    }

}

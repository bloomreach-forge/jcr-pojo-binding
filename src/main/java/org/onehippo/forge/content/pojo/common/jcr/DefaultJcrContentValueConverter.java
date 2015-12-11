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
package org.onehippo.forge.content.pojo.common.jcr;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.jcr.Binary;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.apache.jackrabbit.util.ISO8601;
import org.onehippo.forge.content.pojo.common.ContentNodeException;
import org.onehippo.forge.content.pojo.common.ContentValueConverter;
import org.onehippo.forge.content.pojo.model.BinaryValue;

public class DefaultJcrContentValueConverter implements ContentValueConverter<Value> {

    private static final String FILE_NAME_PREFIX = DefaultJcrContentValueConverter.class.getSimpleName() + "_";
    private static final String FILE_NAME_SUFFIX = "";

    private Session session;
    private long dataUrlSizeThreashold = 20 * 1024; // 20KB
    private FileObject binaryValueFileFolder;

    public DefaultJcrContentValueConverter(final Session session) {
        this.session = session;
    }

    public long getDataUrlSizeThreashold() {
        return dataUrlSizeThreashold;
    }

    public void setDataUrlSizeThreashold(long dataUrlSizeThreashold) {
        this.dataUrlSizeThreashold = dataUrlSizeThreashold;
    }

    public FileObject getBinaryValueFileFolder() {
        return binaryValueFileFolder;
    }

    public void setBinaryValueFileFolder(FileObject binaryValueFileFolder) {
        this.binaryValueFileFolder = binaryValueFileFolder;
    }

    @Override
    public String toString(Value value) throws ContentNodeException {
        try {
            String stringifiedValue = null;

            switch (value.getType()) {
            case PropertyType.STRING: {
                stringifiedValue = value.getString();
                break;
            }
            case PropertyType.LONG: {
                stringifiedValue = Long.toString(value.getLong());
                break;
            }
            case PropertyType.DOUBLE: {
                stringifiedValue = Double.toString(value.getDouble());
                break;
            }
            case PropertyType.DATE: {
                stringifiedValue = ISO8601.format(value.getDate());
                break;
            }
            case PropertyType.BOOLEAN: {
                stringifiedValue = Boolean.toString(value.getBoolean());
                break;
            }
            case PropertyType.DECIMAL: {
                stringifiedValue = value.getDecimal().toString();
                break;
            }
            case PropertyType.NAME:
            case PropertyType.URI: {
                stringifiedValue = value.getString();
                break;
            }
            }

            return stringifiedValue;
        } catch (RepositoryException e) {
            throw new ContentNodeException(e.toString(), e);
        }
    }

    @Override
    public BinaryValue toBinaryValue(Value value) throws ContentNodeException {
        try {
            Binary binary = value.getBinary();
            long size = binary.getSize();

            if (size < getDataUrlSizeThreashold()) {
                InputStream input = null;
                ByteArrayOutputStream output = null;

                try {
                    input = binary.getStream();
                    output = new ByteArrayOutputStream((int) size);
                    IOUtils.copy(input, output);

                    input.close();
                    input = null;
                    binary.dispose();
                    binary = null;

                    return new BinaryValue(output.toByteArray());
                } finally {
                    IOUtils.closeQuietly(output);
                    IOUtils.closeQuietly(input);

                    if (binary != null) {
                        binary.dispose();
                    }
                }
            } else {
                FileObject binaryFile = createBinaryValueFileObject();
                InputStream input = null;
                OutputStream output = null;

                try {
                    input = binary.getStream();
                    output = binaryFile.getContent().getOutputStream();
                    IOUtils.copy(input, output);

                    output.close();
                    output = null;
                    input.close();
                    input = null;
                    binary.dispose();
                    binary = null;

                    return new BinaryValue(binaryFile);
                } finally {
                    IOUtils.closeQuietly(output);
                    IOUtils.closeQuietly(input);

                    if (binary != null) {
                        binary.dispose();
                    }
                }
            }
        } catch (Exception e) {
            throw new ContentNodeException(e.toString(), e);
        }
    }

    @Override
    public Value toValue(String stringValue) throws ContentNodeException {
        return null;
    }

    @Override
    public Value toValue(BinaryValue binaryValue) throws ContentNodeException {
        return null;
    }

    protected Session getSession() {
        return session;
    }

    protected FileObject createBinaryValueFileObject() throws IOException {
        if (getBinaryValueFileFolder() == null) {
            File binaryFile = File.createTempFile(FILE_NAME_PREFIX, FILE_NAME_SUFFIX);
            return VFS.getManager().toFileObject(binaryFile);
        } else {
            getBinaryValueFileFolder().createFolder();
            FileObject binaryFileObject = getBinaryValueFileFolder()
                    .resolveFile(FILE_NAME_PREFIX + System.currentTimeMillis());
            return binaryFileObject;
        }
    }

}

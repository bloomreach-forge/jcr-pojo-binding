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
import java.math.BigDecimal;

import javax.jcr.Binary;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.apache.jackrabbit.util.ISO8601;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.onehippo.forge.content.pojo.common.ContentNodeException;
import org.onehippo.forge.content.pojo.common.ContentValueConverter;
import org.onehippo.forge.content.pojo.model.BinaryValue;
import org.onehippo.forge.content.pojo.model.ContentPropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link ContentValueConverter} implementation for JCR.
 * <P>
 * Especially for binary data, this converts JCR {@link javax.jcr.Binary} value to
 * a <code>data:</code> URL if the data size is less than {@link #getDataUrlSizeThreashold()}.
 * Otherwise, this stores the binary data in an external {@link FileObject} in a randomly generated
 * file under the {@link #getBinaryValueFileFolder()} with the file name prefix, {@link #getBinaryFileNamePrefix()},
 * and keeps the file URL string instead of the whole data.
 * </P>
 */
public class DefaultJcrContentValueConverter implements ContentValueConverter<Value> {

    private static Logger log = LoggerFactory.getLogger(DefaultJcrContentValueConverter.class);

    private static final String DEFAULT_BINARY_FILE_NAME_PREFIX = "_hipojo_bin_";
    private static final String DEFAULT_BINARY_FILE_NAME_SUFFIX = ".dat";

    private Session session;
    private long dataUrlSizeThreashold = 20 * 1024; // 20KB
    private FileObject binaryValueFileFolder;
    private String binaryFileNamePrefix = DEFAULT_BINARY_FILE_NAME_PREFIX;
    private String defaultBinaryFileNameSuffix = DEFAULT_BINARY_FILE_NAME_SUFFIX;
    private MimeTypes mimeTypes = MimeTypes.getDefaultMimeTypes();

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

    public String getBinaryFileNamePrefix() {
        return binaryFileNamePrefix;
    }

    public void setBinaryFileNamePrefix(String binaryFileNamePrefix) {
        if (StringUtils.isBlank(binaryFileNamePrefix)) {
            throw new IllegalArgumentException("Invalid binary file name prefix.");
        }

        this.binaryFileNamePrefix = binaryFileNamePrefix;
    }

    public String getDefaultBinaryFileNameSuffix() {
        return defaultBinaryFileNameSuffix;
    }

    public void setDefaultBinaryFileNameSuffix(String defaultBinaryFileNameSuffix) {
        this.defaultBinaryFileNameSuffix = StringUtils.defaultString(defaultBinaryFileNameSuffix);
    }

    public MimeTypes getMimeTypes() {
        return mimeTypes;
    }

    public void setMimeTypes(MimeTypes mimeTypes) {
        this.mimeTypes = mimeTypes;
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
    public BinaryValue toBinaryValue(Value value, String mimeType) throws ContentNodeException {
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
                FileObject binaryFile = createRandomBinaryValueFileObject(mimeType);
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
    public Value toJcrValue(String typeName, String stringValue) throws ContentNodeException {
        if (stringValue == null) {
            throw new IllegalArgumentException("Cannot convert null to a JCR value.");
        }

        ContentPropertyType type = ContentPropertyType.valueOf(typeName);
        Value jcrValue = null;

        try {
            final ValueFactory valueFactory = getSession().getValueFactory();

            switch (type) {
            case STRING:
                jcrValue = valueFactory.createValue(stringValue);
                break;
            case DATE:
                jcrValue = valueFactory.createValue(ISO8601.parse(stringValue));
                break;
            case BOOLEAN:
                jcrValue = valueFactory.createValue(BooleanUtils.toBoolean(stringValue));
                break;
            case LONG:
                jcrValue = valueFactory.createValue(NumberUtils.toLong(stringValue));
                break;
            case DOUBLE:
                jcrValue = valueFactory.createValue(NumberUtils.toDouble(stringValue));
                break;
            case DECIMAL:
                jcrValue = valueFactory.createValue(new BigDecimal(stringValue));
                break;
            default:
                break;
            }
        } catch (RepositoryException e) {
            throw new ContentNodeException(e.toString(), e);
        }

        return jcrValue;
    }

    @Override
    public Value toJcrValue(BinaryValue binaryValue) throws ContentNodeException {
        InputStream input = null;
        Binary binary = null;

        try {
            ValueFactory valueFactory = getSession().getValueFactory();
            input = binaryValue.getStream();
            binary = valueFactory.createBinary(input);
            return valueFactory.createValue(binary);
        } catch (IOException | RepositoryException e) {
            IOUtils.closeQuietly(input);
            throw new ContentNodeException(e.toString(), e);
        }
    }

    protected Session getSession() {
        return session;
    }

    protected FileObject createRandomBinaryValueFileObject(final String mimeType) throws IOException {
        String fileNameSuffix = getDefaultBinaryFileNameSuffix();
        String extension = findDefaultFileExtensionByMimeType(mimeType);

        if (StringUtils.isNotEmpty(extension)) {
            fileNameSuffix = extension;
        }

        if (getBinaryValueFileFolder() == null) {
            File binaryFile = File.createTempFile(getBinaryFileNamePrefix(), fileNameSuffix);
            return VFS.getManager().toFileObject(binaryFile);
        } else {
            getBinaryValueFileFolder().createFolder();
            FileObject binaryFileObject = getBinaryValueFileFolder()
                    .resolveFile(getBinaryFileNamePrefix() + System.currentTimeMillis() + fileNameSuffix);
            return binaryFileObject;
        }
    }

    protected String findDefaultFileExtensionByMimeType(final String mimeTypeValue) {
        String extension = null;

        try {
            if (StringUtils.isNotEmpty(mimeTypeValue)) {
                final MimeType mimeType = getMimeTypes().forName(mimeTypeValue);
                extension = mimeType.getExtension();
            }
        } catch (MimeTypeException e) {
            log.warn("Failed to determine a file extension by the mimeType: '{}'", mimeTypeValue, e);
        }

        return StringUtils.trim(extension);
    }
}

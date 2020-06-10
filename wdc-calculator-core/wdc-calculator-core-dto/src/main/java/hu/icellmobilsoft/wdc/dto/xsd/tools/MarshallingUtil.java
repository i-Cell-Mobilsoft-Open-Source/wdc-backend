/*-
 * #%L
 * WDC
 * %%
 * Copyright (C) 2020 i-Cell Mobilsoft Zrt.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package hu.icellmobilsoft.wdc.dto.xsd.tools;

import java.io.InputStream;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

/**
 * XML/XSD marshalling helper
 *
 * @author ferenc.lutischan
 */
public class MarshallingUtil {
    private static final String W3C_XML_SCHEMA_URI = "http://www.w3.org/2001/XMLSchema";
    private static final Logger LOGGER = Logger.getLogger(MarshallingUtil.class.getName());

    private MarshallingUtil() {
        throw new AssertionError();
    }

    /**
     * Validate with given xsd and convert xml InputStream to given type
     *
     * @param clazz
     *          target Type
     * @param is
     *          xml InputStream
     * @param xsdFileWithPath
     *          xsd for validate
     * @param <T>
     *          return type
     * @return the result object or null on error
     * @throws RuntimeException when not DOMImplementation found
     */
    public static <T> T unmarshalXml(Class<T> clazz, InputStream is, String xsdFileWithPath) {
        if (clazz == null || is == null || xsdFileWithPath == null) {
            LOGGER.warning("Null parameters are not accepted!");
            return null;
        }
        try {
            Schema schema = createSchema(xsdFileWithPath);
            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(schema);
            return (T) unmarshaller.unmarshal(is);
        } catch (SAXException | JAXBException e) {
            LOGGER.severe("Error parsing XML: " + e.getMessage());
        }
        return null;
    }

    private static Schema createSchema(String xsdFileWithPath) throws SAXException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_URI);
        XsdResourceResolver xsdResourceResolver = new XsdResourceResolver(xsdFileWithPath);
        schemaFactory.setResourceResolver(xsdResourceResolver);
        return schemaFactory.newSchema(new StreamSource(MarshallingUtil.class.getClassLoader().getResourceAsStream(xsdFileWithPath)));
    }
}

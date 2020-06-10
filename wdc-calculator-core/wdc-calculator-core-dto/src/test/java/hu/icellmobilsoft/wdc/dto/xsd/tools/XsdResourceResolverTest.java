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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.w3c.dom.ls.LSInput;

class XsdResourceResolverTest {

    public static final String XSD_PATH = "xsd/hu/icellmobilsoft/wdc/core/dto/datafile/workdayData.xsd";

    @Test
    void whenGoodResourceWithDotedRelPathThenGoodResult() {
        // Given
        XsdResourceResolver xsdResourceResolver = new XsdResourceResolver(XSD_PATH);
        // When
        LSInput lsInput = xsdResourceResolver.resolveResource("", "", "", "../common/common.xsd", null);
        // Then
        assertEquals("../common/common.xsd", lsInput.getSystemId());
        assertNotNull(lsInput.getByteStream());
    }

    @Test
    void whenWrongSystemId() {
        // Given
        XsdResourceResolver xsdResourceResolver = new XsdResourceResolver(XSD_PATH);
        // When
        LSInput lsInput = xsdResourceResolver.resolveResource("", "", "", "../common/comm.xsd", null);
        // Then
        assertNull(lsInput);
    }

    @Test
    void whenNullResourcePath() {
        // Given
        XsdResourceResolver xsdResourceResolver = new XsdResourceResolver(null);
        // When
        LSInput lsInput = xsdResourceResolver.resolveResource("", "", "", "../common/common.xsd", null);
        // Then
        assertNull(lsInput);
    }

    @Test
    void whenNullSystemId() {
        // Given
        XsdResourceResolver xsdResourceResolver = new XsdResourceResolver(XSD_PATH);
        // When
        LSInput lsInput = xsdResourceResolver.resolveResource("", "", "", null, null);
        // Then
        assertNull(lsInput);
    }
}

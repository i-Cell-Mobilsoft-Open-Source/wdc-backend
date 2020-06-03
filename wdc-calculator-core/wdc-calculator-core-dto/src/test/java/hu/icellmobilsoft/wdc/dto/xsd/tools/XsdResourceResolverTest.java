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
package hu.icellmobilsoft.wdc.dto.xsd.tools;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.DOMImplementationSource;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * LSResourceResolver implementation for local xsd resources
 *
 *  * @author ferenc.lutischan
 */
public class XsdResourceResolver implements LSResourceResolver {
    private static final Logger LOGGER = Logger.getLogger(XsdResourceResolver.class.getName());
    private final String xsdPath;

    /**
     * Creates an XsdResourceResolver instance
     *
     * @param xsdPath
     */
    public XsdResourceResolver(String xsdPath) {
        this.xsdPath = xsdPath;
    }

    /**
     * Resolve xsd resource
     *
     * @param type
     *          The type of the resource being resolved
     * @param namespaceURI
     *          target namespace of the XML Schema
     * @param publicId
     *          Public identifier of the external entity or <code>null</code>.
     * @param systemId
     *          System identifier or <code>null</code>.
     * @param baseURI
     *          Absolute base URI of the resource being parsed or <code>null</code>.
     * @return An LSInput instance describing the new input source. On error returns <code>null</code>.
     * @throws RuntimeException when not DOMImplementation found
     */
    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        if (systemId == null || xsdPath == null) {
            LOGGER.warning("Invalid xsd params!");
            return null;
        }

        InputStream resStream = resolveXsdToStream(systemId);
        if (resStream == null) {
            LOGGER.log(Level.WARNING, "Resource not found in classpath: {0}", systemId);
            return null;
        }
        return createLsInput(systemId, resStream);
    }

    private InputStream resolveXsdToStream(String systemId) {
        String mainPath = substringBeforeLast(xsdPath, '/');
        String xsd = systemId;
        if (systemId.contains("..")) {
            mainPath = substringBeforeLast(mainPath, '/');
            xsd = substringAfterFirst(systemId, "..");
        } else {
            mainPath = mainPath + '/';
        }
        return this.getClass().getClassLoader().getResourceAsStream(mainPath + xsd);
    }

    private String substringBeforeLast(String input, char lastChar) {
        int pos = input.lastIndexOf(lastChar);
        return pos == -1 ? input : input.substring(0, pos);
    }

    private String substringAfterFirst(String str, String separator) {
        int pos = str.indexOf(separator);
        return pos == -1 ? "" : str.substring(pos + separator.length());
    }

    private LSInput createLsInput(String systemId, InputStream resStream) {
        LSInput input = getDOMImplementationLS().createLSInput();
        input.setByteStream(resStream);
        input.setSystemId(systemId);
        return input;
    }

    private DOMImplementationLS getDOMImplementationLS() {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try {
            final DOMImplementationRegistry registry;
            registry = DOMImplementationRegistry.newInstance();
            return (DOMImplementationLS) registry.getDOMImplementation("LS");
        } catch (ClassNotFoundException cnfe) {
            // This is an ugly workaround of this bug: https://issues.jboss.org/browse/WFLY-4416
            try {
                Class<?> sysImpl = classLoader.loadClass("com.sun.org.apache.xerces.internal.dom.DOMXSImplementationSourceImpl");
                DOMImplementationSource source = (DOMImplementationSource) sysImpl.newInstance();
                return (DOMImplementationLS) source.getDOMImplementation("LS");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }
}
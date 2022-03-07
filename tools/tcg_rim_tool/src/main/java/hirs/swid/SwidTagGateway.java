package hirs.swid;

import hirs.swid.utils.HashSwid;
import hirs.swid.xjc.Directory;
import hirs.swid.xjc.Entity;
import hirs.swid.xjc.Link;
import hirs.swid.xjc.ObjectFactory;
import hirs.swid.xjc.ResourceCollection;
import hirs.swid.xjc.SoftwareIdentity;
import hirs.swid.xjc.SoftwareMeta;
import org.w3c.dom.Document;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyName;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * This class provides interaction with the SWID Tag schema as defined in
 * http://standards.iso.org/iso/19770/-2/2015/schema.xsd
 */
public class SwidTagGateway {

    private final ObjectFactory objectFactory = new ObjectFactory();
    private Marshaller marshaller;
    private String attributesFile;
    private boolean defaultCredentials;
    private String jksTruststoreFile;
    private String pemPrivateKeyFile;
    private String pemCertificateFile;
    private boolean embeddedCert;
    private String rimEventLog;

    /**
     * Default constructor initializes jaxbcontext, marshaller, and unmarshaller
     */
    public SwidTagGateway() {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(SwidTagConstants.SCHEMA_PACKAGE);
            marshaller = jaxbContext.createMarshaller();
            attributesFile = SwidTagConstants.DEFAULT_ATTRIBUTES_FILE;
            defaultCredentials = true;
            pemCertificateFile = "";
            embeddedCert = false;
            rimEventLog = "";
        } catch (JAXBException e) {
            System.out.println("Error initializing jaxbcontext: " + e.getMessage());
        }
    }

    /**
     * Setter for String holding attributes file path
     *
     * @param attributesFile
     */
    public void setAttributesFile(final String attributesFile) {
        this.attributesFile = attributesFile;
    }

    /**
     * Setter for boolean governing signing credentials
     *
     * @param defaultCredentials
     * @return
     */
    public void setDefaultCredentials(final boolean defaultCredentials) {
        this.defaultCredentials = defaultCredentials;
    }

    /**
     * Setter for JKS keystore file
     *
     * @param jksTruststoreFile
     */
    public void setJksTruststoreFile(final String jksTruststoreFile) {
        this.jksTruststoreFile = jksTruststoreFile;
    }

    /**
     * Setter for private key file in PEM format
     *
     * @param pemPrivateKeyFile
     */
    public void setPemPrivateKeyFile(final String pemPrivateKeyFile) {
        this.pemPrivateKeyFile = pemPrivateKeyFile;
    }

    /**
     * Setter for certificate file in PEM format
     *
     * @param pemCertificateFile
     */
    public void setPemCertificateFile(final String pemCertificateFile) {
        this.pemCertificateFile = pemCertificateFile;
    }

    /**
     * Setter to embed certificate file in signature block
     *
     * @param embeddedCert
     */
    public void setEmbeddedCert(final boolean embeddedCert) {
        this.embeddedCert = embeddedCert;
    }

    /**
     * Setter for event log support RIM
     *
     * @param rimEventLog
     */
    public void setRimEventLog(final String rimEventLog) {
        this.rimEventLog = rimEventLog;
    }

    /**
     * This method generates a base RIM from the values in a JSON file.
     *
     * @param filename
     */
    public void generateSwidTag(final String filename) {
        SoftwareIdentity swidTag = null;
        try {
            InputStream is = new FileInputStream(attributesFile);
            JsonReader reader = Json.createReader(is);
            JsonObject configProperties = reader.readObject();
            reader.close();
            //SoftwareIdentity
            swidTag = createSwidTag(configProperties.getJsonObject(SwidTagConstants.SOFTWARE_IDENTITY));
            //Entity
            JAXBElement<Entity> entity = objectFactory.createSoftwareIdentityEntity(
                    createEntity(configProperties.getJsonObject(SwidTagConstants.ENTITY)));
            swidTag.getEntityOrEvidenceOrLink().add(entity);
            //Link
            JAXBElement<Link> link = objectFactory.createSoftwareIdentityLink(
                    createLink(configProperties.getJsonObject(SwidTagConstants.LINK)));
            swidTag.getEntityOrEvidenceOrLink().add(link);
            //Meta
            JAXBElement<SoftwareMeta> meta = objectFactory.createSoftwareIdentityMeta(
                    createSoftwareMeta(configProperties.getJsonObject(SwidTagConstants.META)));
            swidTag.getEntityOrEvidenceOrLink().add(meta);
            //File
            hirs.swid.xjc.File file = createFile(
                    configProperties.getJsonObject(SwidTagConstants.PAYLOAD)
                            .getJsonObject(SwidTagConstants.DIRECTORY)
                            .getJsonObject(SwidTagConstants.FILE));
            //Directory
            Directory directory = createDirectory(
                    configProperties.getJsonObject(SwidTagConstants.PAYLOAD)
                            .getJsonObject(SwidTagConstants.DIRECTORY));
            directory.getDirectoryOrFile().add(file);
            //Payload
            ResourceCollection payload = createPayload(
                    configProperties.getJsonObject(SwidTagConstants.PAYLOAD));
            payload.getDirectoryOrFileOrProcess().add(directory);
            JAXBElement<ResourceCollection> jaxbPayload =
                    objectFactory.createSoftwareIdentityPayload(payload);
            swidTag.getEntityOrEvidenceOrLink().add(jaxbPayload);

        } catch (FileNotFoundException e) {
            System.out.println("File does not exist or cannot be read: " + e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        Document signedSoftwareIdentity = signXMLDocument(objectFactory.createSoftwareIdentity(swidTag));
        writeSwidTagFile(signedSoftwareIdentity, filename);
    }

    /**
     * This method writes a Document object out to the file specified by generatedFile.
     *
     * @param swidTag
     */
    public void writeSwidTagFile(final Document swidTag, final String output) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            Source source = new DOMSource(swidTag);
            if (output.isEmpty()) {
                transformer.transform(source, new StreamResult(System.out));
            } else {
                transformer.transform(source, new StreamResult(new FileOutputStream(output)));
            }
        } catch (FileNotFoundException e) {
            System.out.println("Unable to write to file: " + e.getMessage());
        } catch (TransformerConfigurationException e) {
            System.out.println("Error instantiating TransformerFactory class: " + e.getMessage());
        } catch (TransformerException e) {
            System.out.println("Error instantiating Transformer class: " + e.getMessage());
        }
    }

    /**
     * This method creates SoftwareIdentity element based on the parameters read in from
     * a properties file.
     *
     * @param jsonObject the Properties object containing parameters from file
     * @return SoftwareIdentity object created from the properties
     */
    private SoftwareIdentity createSwidTag(final JsonObject jsonObject) {
        SoftwareIdentity swidTag = objectFactory.createSoftwareIdentity();
        swidTag.setLang(SwidTagConstants.DEFAULT_ENGLISH);
        String name = jsonObject.getString(SwidTagConstants.NAME, "");
        if (!name.isEmpty()) {
            swidTag.setName(name);
        }
        String tagId = jsonObject.getString(SwidTagConstants.TAGID, "");
        if (!tagId.isEmpty()) {
            swidTag.setTagId(tagId);
        }
        swidTag.setTagVersion(new BigInteger(jsonObject.getString(SwidTagConstants.TAGVERSION, "0")));
        swidTag.setVersion(jsonObject.getString(SwidTagConstants.VERSION, "0.0"));
        swidTag.setCorpus(jsonObject.getBoolean(SwidTagConstants.CORPUS, false));
        swidTag.setPatch(jsonObject.getBoolean(SwidTagConstants.PATCH, false));
        swidTag.setSupplemental(jsonObject.getBoolean(SwidTagConstants.SUPPLEMENTAL, false));
        if (!swidTag.isCorpus() && !swidTag.isPatch()
                && !swidTag.isSupplemental() && swidTag.getVersion() != "0.0") {
            swidTag.setVersionScheme(jsonObject.getString(SwidTagConstants.VERSION_SCHEME, "multipartnumeric"));
        }

        return swidTag;
    }

    /**
     * This method creates an Entity object based on the parameters read in from
     * a properties file.
     *
     * @param jsonObject the Properties object containing parameters from file
     * @return Entity object created from the properties
     */
    private Entity createEntity(final JsonObject jsonObject) {
        boolean isTagCreator = false;
        Entity entity = objectFactory.createEntity();
        String name = jsonObject.getString(SwidTagConstants.NAME, "");
        if (!name.isEmpty()) {
            entity.setName(name);
        }
        String[] roles = jsonObject.getString(SwidTagConstants.ROLE, "").split(",");
        for (int i = 0; i < roles.length; i++) {
            entity.getRole().add(roles[i]);
            if (roles[i].equals("tagCreator")) {
                isTagCreator = true;
            }
        }
        if (isTagCreator) {
            String regid = jsonObject.getString(SwidTagConstants.REGID, "");
            if (regid.isEmpty()) {
                //throw exception that regid is required
            } else {
                entity.setRegid(regid);
            }
        } else {
            entity.setRegid(jsonObject.getString(SwidTagConstants.REGID, "invalid.unavailable"));
        }
        String thumbprint = jsonObject.getString(SwidTagConstants.THUMBPRINT, "");
        if (!thumbprint.isEmpty()) {
            entity.setThumbprint(thumbprint);
        }
        return entity;
    }

    /**
     * Thsi method creates a Link element based on the parameters read in from a properties
     * file.
     *
     * @param jsonObject the Properties object containing parameters from file
     * @return Link element created from the properties
     */
    private Link createLink(final JsonObject jsonObject) {
        Link link = objectFactory.createLink();
        String href = jsonObject.getString(SwidTagConstants.HREF, "");
        if (!href.isEmpty()) {
            link.setHref(href);
        }
        String rel = jsonObject.getString(SwidTagConstants.REL, "");
        if (!rel.isEmpty()) {
            link.setRel(rel);
        }

        return link;
    }

    /**
     * This method creates a Meta element based on the parameters read in from a properties
     * file.
     *
     * @param jsonObject the Properties object containing parameters from file
     * @return the Meta element created from the properties
     */
    private SoftwareMeta createSoftwareMeta(final JsonObject jsonObject) {
        SoftwareMeta softwareMeta = objectFactory.createSoftwareMeta();
        Map<QName, String> attributes = softwareMeta.getOtherAttributes();
        addNonNullAttribute(attributes, SwidTagConstants._COLLOQUIAL_VERSION,
                jsonObject.getString(SwidTagConstants.COLLOQUIAL_VERSION, ""));
        addNonNullAttribute(attributes, SwidTagConstants._EDITION,
                jsonObject.getString(SwidTagConstants.EDITION, ""));
        addNonNullAttribute(attributes, SwidTagConstants._PRODUCT,
                jsonObject.getString(SwidTagConstants.PRODUCT, ""));
        addNonNullAttribute(attributes, SwidTagConstants._REVISION,
                jsonObject.getString(SwidTagConstants.REVISION, ""));
        addNonNullAttribute(attributes, SwidTagConstants._PAYLOAD_TYPE,
                jsonObject.getString(SwidTagConstants.PAYLOAD_TYPE, ""));
        addNonNullAttribute(attributes, SwidTagConstants._PLATFORM_MANUFACTURER_STR,
                jsonObject.getString(SwidTagConstants.PLATFORM_MANUFACTURER_STR, ""));
        addNonNullAttribute(attributes, SwidTagConstants._PLATFORM_MANUFACTURER_ID,
                jsonObject.getString(SwidTagConstants.PLATFORM_MANUFACTURER_ID, ""));
        addNonNullAttribute(attributes, SwidTagConstants._PLATFORM_MODEL,
                jsonObject.getString(SwidTagConstants.PLATFORM_MODEL, ""));
        addNonNullAttribute(attributes, SwidTagConstants._PLATFORM_VERSION,
                jsonObject.getString(SwidTagConstants.PLATFORM_VERSION, ""));
        addNonNullAttribute(attributes, SwidTagConstants._FIRMWARE_MANUFACTURER_STR,
                jsonObject.getString(SwidTagConstants.FIRMWARE_MANUFACTURER_STR, ""));
        addNonNullAttribute(attributes, SwidTagConstants._FIRMWARE_MANUFACTURER_ID,
                jsonObject.getString(SwidTagConstants.FIRMWARE_MANUFACTURER_ID, ""));
        addNonNullAttribute(attributes, SwidTagConstants._FIRMWARE_MODEL,
                jsonObject.getString(SwidTagConstants.FIRMWARE_MODEL, ""));
        addNonNullAttribute(attributes, SwidTagConstants._FIRMWARE_VERSION,
                jsonObject.getString(SwidTagConstants.FIRMWARE_VERSION, ""));
        addNonNullAttribute(attributes, SwidTagConstants._BINDING_SPEC,
                jsonObject.getString(SwidTagConstants.BINDING_SPEC, ""));
        addNonNullAttribute(attributes, SwidTagConstants._BINDING_SPEC_VERSION,
                jsonObject.getString(SwidTagConstants.BINDING_SPEC_VERSION, ""));
        addNonNullAttribute(attributes, SwidTagConstants._PC_URI_LOCAL,
                jsonObject.getString(SwidTagConstants.PC_URI_LOCAL, ""));
        addNonNullAttribute(attributes, SwidTagConstants._PC_URI_GLOBAL,
                jsonObject.getString(SwidTagConstants.PC_URI_GLOBAL, ""));
        addNonNullAttribute(attributes, SwidTagConstants._RIM_LINK_HASH,
                jsonObject.getString(SwidTagConstants.RIM_LINK_HASH, ""));

        return softwareMeta;
    }

    /**
     * This method creates a Payload from the parameters read in from a properties file.
     *
     * @param jsonObject the Properties object containing parameters from file
     * @return the Payload object created
     */
    private ResourceCollection createPayload(final JsonObject jsonObject) {
        ResourceCollection payload = objectFactory.createResourceCollection();
        Map<QName, String> attributes = payload.getOtherAttributes();
        addNonNullAttribute(attributes, SwidTagConstants._N8060_ENVVARPREFIX,
                jsonObject.getString(SwidTagConstants._N8060_ENVVARPREFIX.getLocalPart(), ""));
        addNonNullAttribute(attributes, SwidTagConstants._N8060_ENVVARSUFFIX,
                jsonObject.getString(SwidTagConstants._N8060_ENVVARSUFFIX.getLocalPart(), ""));
        addNonNullAttribute(attributes, SwidTagConstants._N8060_PATHSEPARATOR,
                jsonObject.getString(SwidTagConstants._N8060_PATHSEPARATOR.getLocalPart(), ""));
        addNonNullAttribute(attributes, SwidTagConstants._SUPPORT_RIM_FORMAT,
                jsonObject.getString(SwidTagConstants.SUPPORT_RIM_FORMAT, ""));
        addNonNullAttribute(attributes, SwidTagConstants._SUPPORT_RIM_TYPE,
                jsonObject.getString(SwidTagConstants.SUPPORT_RIM_TYPE, ""));
        addNonNullAttribute(attributes, SwidTagConstants._SUPPORT_RIM_URI_GLOBAL,
                jsonObject.getString(SwidTagConstants.SUPPORT_RIM_URI_GLOBAL, ""));

        return payload;
    }

    /**
     * This method creates a Directory from the parameters read in from a properties file.
     *
     * @param jsonObject the Properties object containing parameters from file
     * @return Directory object created from the properties
     */
    private Directory createDirectory(final JsonObject jsonObject) {
        Directory directory = objectFactory.createDirectory();
        directory.setName(jsonObject.getString(SwidTagConstants.NAME, ""));

        return directory;
    }

    /**
     * This method creates a hirs.swid.xjc.File from an indirect payload type
     * using parameters read in from a properties file and then
     * calculating the hash of a given event log support RIM.
     *
     * @param jsonObject the Properties object containing parameters from file
     * @return File object created from the properties
     */
    private hirs.swid.xjc.File createFile(final JsonObject jsonObject) {
        hirs.swid.xjc.File file = objectFactory.createFile();
        file.setName(jsonObject.getString(SwidTagConstants.NAME, ""));
        File rimEventLogFile = new File(rimEventLog);
        file.setSize(new BigInteger(Long.toString(rimEventLogFile.length())));
        Map<QName, String> attributes = file.getOtherAttributes();
        addNonNullAttribute(attributes, SwidTagConstants._SHA256_HASH,
                HashSwid.get256Hash(rimEventLog));

        return file;
    }

    /**
     * This utility method checks if an attribute value is empty before adding it to the map.
     *
     * @param attributes
     * @param key
     * @param value
     */
    private void addNonNullAttribute(final Map<QName, String> attributes,
                                     final QName key, String value) {
        if (!value.isEmpty()) {
            attributes.put(key, value);
        }
    }

    /**
     * This method signs a SoftwareIdentity with an xmldsig in compatibility mode.
     * Current assumptions: digest method SHA256, signature method SHA256, enveloped signature
     */
    private Document signXMLDocument(final JAXBElement<SoftwareIdentity> swidTag) {
        Document doc = null;
        try {
            XMLSignatureFactory sigFactory = XMLSignatureFactory.getInstance("DOM");
            Reference reference = sigFactory.newReference(
                    "",
                    sigFactory.newDigestMethod(DigestMethod.SHA256, null),
                    Collections.singletonList(sigFactory.newTransform(Transform.ENVELOPED,
                            (TransformParameterSpec) null)),
                    null,
                    null
            );
            SignedInfo signedInfo = sigFactory.newSignedInfo(
                    sigFactory.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE,
                            (C14NMethodParameterSpec) null),
                    sigFactory.newSignatureMethod(SwidTagConstants.SIGNATURE_ALGORITHM_RSA_SHA256,
                            null),
                    Collections.singletonList(reference)
            );
            List<XMLStructure> keyInfoElements = new ArrayList<XMLStructure>();

            KeyInfoFactory kiFactory = sigFactory.getKeyInfoFactory();
            PrivateKey privateKey;
            CredentialParser cp = new CredentialParser();
            if (defaultCredentials) {
                cp.parseJKSCredentials(jksTruststoreFile);
                privateKey = cp.getPrivateKey();
                KeyName keyName = kiFactory.newKeyName(cp.getCertificateSubjectKeyIdentifier());
                keyInfoElements.add(keyName);
            } else {
                cp.parsePEMCredentials(pemCertificateFile, pemPrivateKeyFile);
                X509Certificate certificate = cp.getCertificate();
                privateKey = cp.getPrivateKey();
                if (embeddedCert) {
                    ArrayList<Object> x509Content = new ArrayList<Object>();
                    x509Content.add(certificate.getSubjectX500Principal().getName());
                    x509Content.add(certificate);
                    X509Data data = kiFactory.newX509Data(x509Content);
                    keyInfoElements.add(data);
                } else {
                    keyInfoElements.add(kiFactory.newKeyValue(certificate.getPublicKey()));
                }
            }
            KeyInfo keyinfo = kiFactory.newKeyInfo(keyInfoElements);

            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            marshaller.marshal(swidTag, doc);
            DOMSignContext context = new DOMSignContext(privateKey, doc.getDocumentElement());
            XMLSignature signature = sigFactory.newXMLSignature(signedInfo, keyinfo);
            signature.sign(context);
        } catch (FileNotFoundException e) {
            System.out.println("Keystore not found! " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error loading keystore: " + e.getMessage());
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException |
                ParserConfigurationException e) {
            System.out.println(e.getMessage());
        } catch (CertificateException e) {
            System.out.println(e.getMessage());
        } catch (JAXBException e) {
            System.out.println("Error marshaling signed swidtag: " + e.getMessage());
        } catch (MarshalException | XMLSignatureException e) {
            System.out.println("Error while signing SoftwareIdentity: " + e.getMessage());
        } catch (KeyException e) {
            System.out.println("Public key algorithm not recognized or supported: "
                    + e.getMessage());
        }

        return doc;
    }
}

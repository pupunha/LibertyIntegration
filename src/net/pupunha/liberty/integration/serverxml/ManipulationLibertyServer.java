package net.pupunha.liberty.integration.serverxml;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import net.pupunha.liberty.integration.serverxml.annotation.Attribute;
import org.apache.commons.beanutils.PropertyUtils;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@Log
public class ManipulationLibertyServer {

    private static final String ENTERPRISE_APPLICATION = "enterpriseApplication";
    private static final String SERVER = "server";

    @Getter
    @Setter
    private String filePath;

    public ManipulationLibertyServer(String filePath) {
        this.filePath = filePath;
    }


    public List<EnterpriseApplication> listApplications() throws Exception {

        List<EnterpriseApplication> applications = new ArrayList<>();
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.parse(filePath);

        Node server = doc.getElementsByTagName(SERVER).item(0);

        NodeList childNodes = server.getChildNodes();
        for(int i=0; i< childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (ENTERPRISE_APPLICATION.equals(node.getNodeName())) {
                NamedNodeMap attributes = node.getAttributes();
                EnterpriseApplication application = new EnterpriseApplication();
                Field[] declaredFields = application.getClass().getDeclaredFields();
                for (Field field : declaredFields) {
                    Attribute attribute = field.getAnnotation(Attribute.class);
                    String value;
                    if (attribute != null) {
                        value = attributes.getNamedItem(attribute.value()).getNodeValue();
                    } else {
                        value = attributes.getNamedItem(field.getName()).getNodeValue();
                    }
                    PropertyUtils.setProperty(application, field.getName(), value);
                }
                applications.add(application);
            }
        }

        return applications;
    }

    public void addApplication(EnterpriseApplication application) throws IOException, SAXException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, TransformerException, ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.parse(filePath);

        Node server = doc.getElementsByTagName(SERVER).item(0);

        Element elementApplication = doc.createElement(ENTERPRISE_APPLICATION);
        Field[] declaredFields = application.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            Attribute attribute = field.getAnnotation(Attribute.class);
            Object value = PropertyUtils.getProperty(application, field.getName());
            if (attribute != null) {
                elementApplication.setAttribute(attribute.value(), value.toString());
            } else {
                elementApplication.setAttribute(field.getName(), value.toString());
            }
        }

        server.appendChild(elementApplication);

        save(doc);
    }


    public void removeApplication(String id) throws IOException, SAXException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, TransformerException, ParserConfigurationException, XPathExpressionException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.parse(filePath);

        Node server = doc.getElementsByTagName(SERVER).item(0);

        String expression = String.format("/server/enterpriseApplication[@id='%s']", id);
        XPath xPath = XPathFactory.newInstance().newXPath();

        Node application = (Node) xPath.compile(expression).evaluate(doc, XPathConstants.NODE);

        server.removeChild(application);

        save(doc);
    }

    private void save(Document doc) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        DOMSource source = new DOMSource(doc);
        doc.setXmlStandalone(true);
        StreamResult result = new StreamResult(new File(filePath));
        transformer.transform(source, result);
    }

}

package net.pupunha.liberty.integration.util;

import net.pupunha.liberty.integration.constants.MavenConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.nio.file.Path;
import java.util.Optional;

public class ReadPom {

    public static String getPackaging(Path pomXML) {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlDocument = builder.parse(pomXML.toFile());
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expression = "/project/packaging";
            Node node = (Node) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODE);
            Optional<Node> nodeOptional = Optional.ofNullable(node);
            String textContent = nodeOptional.map(Node::getTextContent).orElse(MavenConstants.JAR);
            return textContent;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}

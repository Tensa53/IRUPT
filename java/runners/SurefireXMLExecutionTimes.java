package org.example.runners;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class SurefireXMLExecutionTimes {
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {

        String xmlPath = args[0];
        String outputPath = args[1];

        File dir = new File(xmlPath);
        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".xml");
            }
        });

        TreeMap<String, Double> map = new TreeMap<>();

        for (File file : files) {
            Map<String, Double> map1 = new HashMap<>();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = null;
            try {
                doc = builder.parse(file);
            } catch (SAXException | IOException e) {
                throw new RuntimeException(e);
            }

            Node testsuite = doc.getElementsByTagName("testsuite").item(0);
            Double testSuiteExecutionTime = Double.valueOf(testsuite.getAttributes().getNamedItem("time").getNodeValue());
            String testSuiteName = testsuite.getAttributes().getNamedItem("name").getNodeValue();
            System.out.println("Test Suite Name: " + testSuiteName);

            NodeList testCaseNodes = doc.getElementsByTagName("testcase");

            for (int i = 0; i < testCaseNodes.getLength(); i++) {
                Node node = testCaseNodes.item(i);
                String testCaseName = node.getAttributes().getNamedItem("name").getNodeValue();
                Double testCaseExecutionTime = Double.valueOf(node.getAttributes().getNamedItem("time").getNodeValue());
                System.out.println("Test Case: " + testCaseName + " - Execution time: " + testCaseExecutionTime);
                map.put(testSuiteName+"."+testCaseName, testCaseExecutionTime);
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new FileWriter(outputPath), map);

    }
}

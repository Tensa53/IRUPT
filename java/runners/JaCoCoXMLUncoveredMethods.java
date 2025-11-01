package org.example.runners;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import static org.example.runners.MethodSignatureRetriever.getMethodSignatureParsed;

public class JaCoCoXMLUncoveredMethods {

    public static void main(String[] args) {

        String xmlPath = args[0];
        String outputPath = args[1];

        try {

            File xmlFile = new File(xmlPath);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            NodeList classNodes = doc.getElementsByTagName("class");

            TreeMap<String, Set<String>> uncoveredMethods = new TreeMap<>();

            for (int i = 0; i < classNodes.getLength(); i++) {
                Node node = classNodes.item(i);
                String className = node.getAttributes().getNamedItem("name").getNodeValue();
                String classDottedName = className.replace("/", ".");
                System.out.println("Class: " + classDottedName);
                NodeList classNodeChildNodes = node.getChildNodes();
                Set<String> methods = new HashSet<>();
                for (int j = 0; j < classNodeChildNodes.getLength(); j++) {
                    Node childNode = classNodeChildNodes.item(j);
                    if (childNode.getNodeName().equals("method")) {
                        String methodName = childNode.getAttributes().getNamedItem("name").getNodeValue();
                        String descriptor = childNode.getAttributes().getNamedItem("desc").getNodeValue();
                        int methodLine = 0;
                        if (methodName.equals("<init>")) {
                            methodLine = Integer.parseInt(childNode.getAttributes().getNamedItem("line").getNodeValue());
                        } else {
                            methodLine = Integer.parseInt(childNode.getAttributes().getNamedItem("line").getNodeValue()) - 1;
                        }
                        String signature = getMethodSignatureParsed(methodLine, className);
                        String fullMethodName = "";
                        if (methodName.equals("<init>")) {
                            fullMethodName = classDottedName + classDottedName.substring(classDottedName.lastIndexOf(".")) + "(" + signature + ")";
                        } else {
                            fullMethodName = classDottedName + "." + methodName + "(" + signature + ")";
                        }
                        Node methodCounterNode = childNode.getLastChild();
                        Integer counter = Integer.valueOf(methodCounterNode.getAttributes().getNamedItem("missed").getNodeValue());
                        if (counter > 0) {
                            System.out.println("Uncovered: " + fullMethodName);
                            methods.add(fullMethodName);
                        }
                    }

                }

                uncoveredMethods.put(classDottedName, methods);

            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new FileWriter(outputPath), uncoveredMethods);
        }catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }

    }
}

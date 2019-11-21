package org.grobid.core.annotater;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.grobid.core.data.AcknowledgmentItem;
import org.grobid.core.engines.Engine;
import org.grobid.core.factory.AbstractEngineFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import shadedwipo.org.apache.commons.lang3.StringEscapeUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AcknowledgmentAnnotator {
    // read json file
    public List<AcknowledgmentAnnotated> annotate(String fileInput) throws IOException {
        List<AcknowledgmentAnnotated> acknowledgmentAnnotateds = new ArrayList<>();
        Engine engine = new Engine(true);

        byte[] jsonData = Files.readAllBytes(Paths.get(fileInput));
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode rootNode = objectMapper.readTree(jsonData);
        Iterator<JsonNode> elements = rootNode.elements();
        int i = 0;
        while (elements.hasNext()) {

            AcknowledgmentAnnotated acknowledgmentAnnotated = new AcknowledgmentAnnotated();
            List<String> text = elements.next().findValuesAsText("text");
            for (String txt : text) {
                //System.out.println("Text: " + txt);
                List<AcknowledgmentItem> annotations = engine.processAcknowledgment(txt);
                acknowledgmentAnnotated.setText(txt);
                acknowledgmentAnnotated.setAnnotations(annotations);
                acknowledgmentAnnotateds.add(acknowledgmentAnnotated);
            }
            i++;
        }
        System.out.println("Total extracted : " + i);
        return acknowledgmentAnnotateds;
    }

    public void writeToXML(List<AcknowledgmentAnnotated> acknowledgments, String fileOutput) {
        StringBuilder sb = new StringBuilder();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document document;
        try {
            List<AcknowledgmentItem> annotations = null;
            builder = factory.newDocumentBuilder();
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");

            // root element
            sb.append("<acknowledgments>\n");

            for (AcknowledgmentAnnotated acknowledgment : acknowledgments) {
                String text = StringEscapeUtils.escapeXml(acknowledgment.getText());
                annotations = acknowledgment.getAnnotations();
                sb.append("<acknowledgment>");
                if (annotations != null && annotations.size() > 0) {
                    // acknowledgment element
                    String combinedText = null;
                    // iterate through annotations
                    for (int i = 0; i < annotations.size(); i++) {
                        // get label text
                        String label = annotations.get(i).getLabel();
                        String subText = StringEscapeUtils.escapeXml(annotations.get(i).getText());
                        int lengthSubText = subText.length();

                        // get offsets
                        int start_offset = text.indexOf(subText);
                        int end_offset = start_offset + lengthSubText;

                        if (start_offset >= 0 && start_offset <= text.length()) {
                            // substring and escaping special character when generating an XML file
                            String beginText = text.substring(0, start_offset);
                            String afterText = text.substring(end_offset);

                            combinedText = beginText + "<" + label + ">" + subText + "</" + label + ">" + afterText;

                            text = combinedText;
                        } else {
                            continue;
                        }
                    }
                    sb.append(combinedText);

                } else {
                    sb.append(StringEscapeUtils.escapeXml(acknowledgment.getText()));
                }
                sb.append("</acknowledgment>");
                sb.append("\n");
            }

            // close the document
            sb.append("</acknowledgments>");

            // create a file xml
            document = builder.parse(new InputSource(new StringReader(sb.toString())));
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);

            File outputFile = new File(fileOutput);
            StreamResult streamResult = new StreamResult(outputFile);
            transformer.transform(domSource, streamResult);

            System.out.println("File is saved in " + outputFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        AbstractEngineFactory.init();
        String inputFile = "grobid-trainer/resources/dataset/acknowledgment/corpus/raw/acknowledgements.json";
        String outputFile = "grobid-trainer/resources/dataset/acknowledgment/corpus/annotated/acknowledgementsAnnotatedAutomaticallyByGrobid.xml";

        AcknowledgmentAnnotator acknowledgmentAnnotator = new AcknowledgmentAnnotator();
        List<AcknowledgmentAnnotated> results = acknowledgmentAnnotator.annotate(inputFile);
        System.out.println("Total annotated = " + results.size());
        acknowledgmentAnnotator.writeToXML(results, outputFile);
    }
}



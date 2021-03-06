package g2html;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

final public class Loc {

	// parsing a local node means copying the value into its own file
	// but also remembering some details (reachability, line data, file path)
	static public void parseLocNode(XMLStreamReader parser, Result res, ResultStats resultStats, FileWriter lostAndFound) throws IOException, XMLStreamException {

		// first copy basic information
		String file = parser.getAttributeValue("", "file");
		String line = parser.getAttributeValue("", "line");
		String id   = parser.getAttributeValue("", "id");

		// compute the file-id from the full path
		String shortFile = file.replaceAll("/", "%2F");

		// look up the database for the file
		FileStats fileStats = resultStats.getStats(shortFile);

		// add line data and file object
		fileStats.addLineData(id,Integer.valueOf(line));
		fileStats.setCFile(file);

		// open a stream for the node file
		File xmlOut = res.getNodeFile(id);
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, false);
		XMLStreamWriter xmlOutStream = factory.createXMLStreamWriter(new BufferedOutputStream(new FileOutputStream(xmlOut)));

		// write the header
		xmlOutStream.writeStartDocument();
		xmlOutStream.writeCharacters("\n");
		xmlOutStream.writeProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"../node.xsl\"");
		xmlOutStream.writeCharacters("\n");
		xmlOutStream.writeStartElement("loc");

		// now copy all data to the stream while parseing from the input
		XMLStreamCC readcc = new XMLStreamCC(parser,xmlOutStream);

		// look through the input and ...
		boolean pathFound = false;
		boolean notDead = false;
		while(readcc.hasNext()){
			int eventType = readcc.getEventType();
			// add function name to the loc xml-node
			if (eventType==XMLStreamConstants.START_ELEMENT && readcc.getLocalName() != null && readcc.getLocalName().equals("call")){
				String funName = fileStats.getNodeFun(id);
				if (null==funName) {
					lostAndFound.append(id).append("\n");
					funName = "unknown";
				}
				xmlOutStream.writeAttribute("fun",funName);
			// stop at the conclusion of the loc xml-node
			} else if (eventType==XMLStreamConstants.END_ELEMENT && readcc.getLocalName().equals("call")){
				break;
			// if we found an path then the context has been already parsed
			} else if (eventType==XMLStreamConstants.START_ELEMENT && readcc.getLocalName().equals("path")){
				pathFound = true;
			// if we found an analysis tag in the path, then this means that the line is not dead
			} else if (eventType==XMLStreamConstants.START_ELEMENT && readcc.getLocalName().equals("analysis") && pathFound){
				notDead = true;
			}
			// skip to the next event
			readcc.next();
		}

		// update reachability information for the file
		if (notDead) {
			resultStats.getStats(shortFile).addLive(Integer.parseInt(line));
		} else {
			resultStats.getStats(shortFile).addDead(Integer.parseInt(line));
		}


		// close the stream
		xmlOutStream.writeEndElement();
		xmlOutStream.writeEndDocument();
		xmlOutStream.close();
	}
}

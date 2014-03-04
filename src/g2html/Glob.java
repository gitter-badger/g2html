package g2html;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.util.logging.Logger;

public class Glob {
	static public void parseGlobNode(XMLStreamReader parser, XMLStreamWriter globals) throws XMLStreamException {
		XMLStreamCC readcc = new XMLStreamCC(parser,globals);

		while(readcc.hasNext()){
			if (readcc.getEventType()== XMLStreamConstants.END_ELEMENT &&
							readcc.getLocalName()=="glob"){
				break;
			}
			readcc.next();
		}
	}
}
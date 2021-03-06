package g2html;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

// result stats encapsulates all databases for all files
public class ResultStats {
	private Map<String,FileStats> fm;

	// create a map of databases
	public ResultStats(){
		fm = new TreeMap<>();
	}

	// return (and possibly allocate) a nw database for the file
	public FileStats getStats(String s){
		FileStats fs = fm.get(s);
		if (fs==null){
			fs = new FileStats();
			fm.put(s, fs);
		}
		return fs;
	}

	// return the set of all files
	public Set<String> allFiles(){
		return fm.keySet();
	}
	
	// prepare the report
	public void printReport(Result r)
					throws IOException, XMLStreamException {

		// open the output xml stream
		XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
		XMLStreamWriter report = outFactory.createXMLStreamWriter(new FileOutputStream(r.getReportFile()));

		// write the preamble
		report.writeStartDocument();
		report.writeCharacters("\n");
		report.writeProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"report.xsl\"");
		report.writeCharacters("\n");
		report.writeStartElement("report");

		// for each file
		for(String file : fm.keySet()){
			if (fm.get(file).getCFile()==null)
				continue;
			// write the file-name
			report.writeStartElement("file");
			report.writeAttribute("name", file.replaceAll("%2F", "/"));
			
			// for each function
			for (String fun : fm.get(file).getFunctions()){
				// write the function name
				report.writeStartElement("function");
				report.writeAttribute("name", fun);
				report.writeEndElement();
			}

			report.writeEndElement();
		}

		// close the stream
		report.writeEndElement();
		report.writeEndDocument();
		report.close();
	}
}

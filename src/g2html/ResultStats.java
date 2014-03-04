package g2html;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

public class ResultStats {
	private Map<String,FileStats> fm;
	
	public ResultStats(){
		fm = new TreeMap<>();
	}
	
	public FileStats getStats(String s){
		FileStats fs = fm.get(s);
		if (fs==null){
			fs = new FileStats();
			fm.put(s, fs);
		}
		return fs;
	}

	public Set<String> allFiles(){
		return fm.keySet();
	}
	
	public void printJson(Result r) throws IOException{
		FileWriter os = new FileWriter(r.getDataFile());
		os.write("fileData = {\n");
		boolean first = true;
		for(String s : fm.keySet()){
			if (!first)
				os.append(",\n");
			first = false;
			os.append("\"");
			os.append(s);
			os.write("\" : \n");
			fm.get(s).writeJson(os);
		}

		os.write("}\n");
		os.close();
	}
	public void printReport(Result r) throws IOException, XMLStreamException {
		XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
		XMLStreamWriter report = outFactory.createXMLStreamWriter(new FileOutputStream(r.getReportFile()));
		report.writeStartDocument();
		report.writeCharacters("\n");
		report.writeProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"report.xsl\"");
		report.writeCharacters("\n");
		report.writeStartElement("report");

		for(String file : fm.keySet()){
			report.writeStartElement("file");
			report.writeAttribute("name", file);	
			for (String fun : fm.get(file).getFunctions()){
				report.writeStartElement("function");
				report.writeAttribute("name", fun);
				report.writeEndElement();
			}

			report.writeEndElement();
		}
		report.writeEndElement();
		report.writeEndDocument();
		report.close();
	}
}
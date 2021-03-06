package g2html;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;

import static java.nio.file.Files.copy;

// handles the result directory structure
public class Result {
	File resDir;
	File cfgDir;
	File filDir;
	File nodDir;
	File warDir;

	// recursive delete
	public static boolean deleteDirectory(File directory) {
	    if(directory.exists()){
	        File[] files = directory.listFiles();
	        if(null!=files){
		        for (File file : files) {
			        if (file.isDirectory()) {
				        deleteDirectory(file);
			        } else {
				        file.delete();
			        }
		        }
	        }
	    }
	    return(directory.delete());
	}

	// delete the result directory and recreate it anew
	private void prepareDirectories(String resultDir) throws IOException {
		resDir = new File(resultDir);
		deleteDirectory(resDir);
		if (!resDir.mkdir()) throw new IOException("prepareDirectories - resDir");
		
		cfgDir = new File(resDir,Config.cfgSubdir);
		if (!cfgDir.mkdir()) throw new IOException("prepareDirectories - cfgDir");

		filDir = new File(resDir,Config.sourceFilesSubdir);
		if (!filDir.mkdir()) throw new IOException("prepareDirectories - filDir");

		nodDir = new File(resDir,Config.nodesSubdir);
		if (!nodDir.mkdir()) throw new IOException("prepareDirectories - nodDir");
		
		warDir = new File(resDir,Config.warningsSubdir);
		if (!warDir.mkdir()) throw new IOException("prepareDirectories - warDir");
	}

	// take a file from the jar
	private static void copyFromJar(String f,File t) throws IOException{
		InputStream stream = Main.class.getResourceAsStream(f);
	    if (stream == null) {
	        throw new IOException("Cannot open resource: "+f);
	    }
	    OutputStream resStreamOut = null;
	    int readBytes;
	    byte[] buffer = new byte[4096];
	    try {
	        resStreamOut = new FileOutputStream(t);
	        while ((readBytes = stream.read(buffer)) > 0) {
	            resStreamOut.write(buffer, 0, readBytes);
	        }
	    } finally {
	        stream.close();
			    if (resStreamOut != null) {
				    resStreamOut.close();
			    }
	    }
	}

  // generic file copy
	public static void copyFile( File from, File to ) throws IOException {
		Log.printf("copy resource from '%s' to '%s'\n", from, to);
		copy(Paths.get(from.getPath()), Paths.get(to.getPath()));
	}

	// copy a file either form the jar or from a special resources directory
	public static void copyResource(String f, File to) throws IOException {
		try {
				/* try jar first ... */
			copyFromJar("/resources/" + f, to);
		} catch (IOException e){
				/* .. no? This means that we are debugging in an IDE? */
			copyFile(new File(".."+File.separator + "resources" + File.separator+f), to);
		}
	}

	// create a result object and prepare its directories
	public Result(String resDir) throws IOException {
		prepareDirectories(resDir);
		for (String f : Config.preparedFiles){
			copyResource(f,new File(resDir,f));
		}
	}

	// return a listing file to be created
	public File getListingFile(String path) {
		path = path.replaceAll("/","%2F");
		File f = new File(filDir, path+".xml");
		return f;
	}

	// return a svg file to be created
	public File getSvgFile(String file, String fun) {
		File dir = new File(cfgDir,file) ;
		dir.mkdir();
		File f = new File(dir,fun+".svg");
		return f;
	}

	// return a node file to be created
	public File getNodeFile(String node) {
		File f = new File(nodDir,node+".xml");
		return f;
	}

	// return a warnings file to be created
	public File getWarningFile(String war) {
		File f = new File(warDir,war+".xml");
		return f;
	}

	// return a globals file to be created
	public File getGlobalFile() {
		File f = new File(nodDir,"globals.xml");
		return f;
	}

	// return a report file to be created
	public File getReportFile() {
		File f = new File(resDir,"index.xml");
		return f;
	}
}

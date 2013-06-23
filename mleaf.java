//import java.applet.Applet;
import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import java.util.Scanner;
 
public class MovieLeaf extends JApplet implements Runnable {
  Thread runner;
	private JProgressBar progressBar;
	private JLabel label;
	String MovieLeafDir = new String(readreg("HKCU\\Software\\MovieLeaf","InstalledDirectory")+"\\");
	String DownloadServer = new String(readreg("HKCU\\Software\\MovieLeaf","Downloadsite"));
	String lockFile = new String(MovieLeafDir+".lock");
	String status = "Initialising";
	Boolean test = false;
	String Version = "0.9";
	String build = "08/02/2012 22:48 Version :"+Version+" Test: "+test.toString();
	Long numReadtotal = 0L; Long lastnumReadtotal = 0L;
	Long longfileSize = 6080000L;
	

	public void start() {
		if (runner == null){
			runner =new Thread (this);
			runner.start();
		}
	}
	
	public void stop() {
		if(runner !=null) {
			runner.stop();
			runner = null;
		}
	}
	
	public void run() {
		progressBar.setValue(0);
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		String fileName =  getParameter("FILE");
		String fileSize = getParameter("SIZE");
		writeLog("run: Build date "+build);
		if(test){
			fileName = "booklist.iso";	
			fileSize = "6080 KB";
			MovieLeafDir = "c:\\MovieLeaf\\";
			DownloadServer = "http://127.0.0.1/";	
		}
		longfileSize = convertSize(fileSize);
		handlePayload(DownloadServer+"/"+fileName); 		
		return;	
		}
	
  public void init() {
	  getContentPane().setBackground(Color.WHITE);
	  getContentPane().setLayout(new FlowLayout());
	  progressBar = new JProgressBar();
	  getContentPane().add(progressBar);
	  label = new JLabel("Label");
	  getContentPane().add(label, BorderLayout.SOUTH);
	  }
  

  public void xpaint(Graphics g) {
	Font font = new Font("Arial",Font.BOLD,15);
	g.setFont(font);
	if (test) {
		for (int i=0; i<60;i++) {
			g.clearRect(0, 0, 10000, 10000);
			g.drawString(status,20,20);
			repaint();
			try {
				Thread.sleep(1000L);
			} catch (Exception e) {}
		}
		return;
	}

	while(true) {
		g.clearRect(0, 0, 10000, 10000);
		g.drawString(status,20,20);
		repaint();
		try {
			Thread.sleep(1000L);
		} catch (Exception e) {}
	}

  }

private void handlePayload(String url){
	OutputStream out = null;
	URLConnection conn = null;
	InputStream in = null;
	String words[] = url.split("/");
	String filename = words[words.length-1];
	String isoDrive = ""; 
	String driveList = "a:b:c:";

	if (new File(lockFile).exists()){
		writeLog("handlePayload: Lock File ("+lockFile+") set");
		return;
	}
	try {
		new File(lockFile).createNewFile();
	} catch (IOException e){
		writeLog("handlePayload: Could not create lock file "+lockFile);
		return;
	}
	
	try {
		writeLog("handlePayload: Starting to download "+filename+" from "+url);
	    // Get the URL
	    URL urld = new URL(url);
	    // Open an output stream to the destination file on our local filesystem	    
	    out = new BufferedOutputStream(new FileOutputStream(MovieLeafDir+filename));
	    conn = urld.openConnection();
	    in = conn.getInputStream();

	    byte[] buffer = new byte[1024];
	    int numRead;
	    status = "Starting Downloading "+filename;
	    while ((numRead = in.read(buffer)) != -1) {
	        out.write(buffer, 0, numRead);
	        lastnumReadtotal = numReadtotal;
	        numReadtotal += numRead;
	        status = "Download Time Left "+calcRemainingTime();
	        label.setText(status);
	        progressBar.setValue(percentDone());
	        Integer PC = percentDone();
	        progressBar.setString(PC.toString());
	        progressBar.setStringPainted(true);
	    }
	    writeLog("handlePayload: Total bytes downloaded is "+numReadtotal);
	} catch (Exception exception) {
		writeLog("handlePayload: Download Error at urld Connection or getInputStream");
	} finally {
	    try {
	        if (in != null) {
	            in.close();
	        }
	        if (out != null) {
	            out.close();
	        }
	    } catch (IOException ioe) {
	    	writeLog("handlePayload: IOException "+ioe.toString());
	    }
	}

	writeLog("handlePayload: Finished Downloading "+filename);

		try 
		{ 
		  isoDrive = readreg("HKCU\\Software\\MovieLeaf","InstalledDrive");
		  isoDrive = isoDrive.substring(0, 2).toLowerCase();	  
		  if(test) {
			  isoDrive = "d:";
		  }
		} 
		catch(Exception e){
			writeLog("handlePayload: Exception "+e.toString());
		}
		
if (numReadtotal > 10) {
	if (driveList.indexOf(isoDrive) >= 0){
		writeLog("Can't ISOWrite to drive "+isoDrive.toUpperCase());
		status = "Can't ISOWrite to drive "+isoDrive.toUpperCase();
	} else {
		writeLog("handlePayload: Starting to burn "+filename+" to DVD");
		status = "Starting to burn "+filename+" to DVD";
		ISOWrite(filename, isoDrive);
		status = "Finished Burning "+filename+" to DVD";
	}
    } else {
    	writeLog("handlePayload: halting because filesize "+numReadtotal+" is less than 10 bytes");
    	status = "halting because filesize "+numReadtotal+" is less than 10 bytes";
    }
	if (!new File(lockFile).exists()){
		writeLog("handlePayload: Lock File ("+lockFile+") should have existed but didn't");
	}
	try {
		new File(lockFile).delete();
	} catch (Exception e){
		writeLog("handlePayload: Unsuccessful attempt to delete lock file "+lockFile);
	}
	eject(isoDrive);
	status = "ejecting DVD";	
		
// Call Javascript terminating function	
	finishedFunction();
	
// Removing .iso file
	try {
		new File(MovieLeafDir+filename).delete();
	} catch (Exception e) {
		writeLog("handlePayload: Was not able to delete file "+MovieLeafDir+filename);
	}
}
private void finishedFunction() {
	  try {
	      getAppletContext().showDocument
	        (new URL("javascript:finishedfunction()"));
	      	writeLog("finishedFunction: javascript ran successfully");
	      }
	    catch (MalformedURLException me) { 
	    	writeLog("finishedFunction: Exception "+me.toString());
	    }
}

private void ISOWrite(String filename, String isoDrive){
	filename = MovieLeafDir+filename;
	String cmd = MovieLeafDir+"ISOWriter -r "+isoDrive+" -e -x "+filename; 
	writeLog("ISOWrite: invoking Command "+cmd);
	String str = "InitializingCalibratingWritingFinalizingCompletedErasing";
	String line = null;
	try { 

		Process p = Runtime.getRuntime().exec(cmd);
//		BufferedReader is;
		Scanner scanner = new Scanner(p.getInputStream());
		scanner.useDelimiter("\\s+");		
//		is = new BufferedReader(new InputStreamReader(p.getInputStream()));
		while(scanner.hasNext()) {
			line = scanner.next();
			if (line.length()<=0)
				continue;
			Integer PC = convertBurn(line);
	        status = "ISOWrite: "+line;
			if (PC > 0 ) {
				progressBar.setValue(PC);	        
				progressBar.setString(PC.toString());
				progressBar.setStringPainted(true);
				label.setText("DVD Burning: "+PC+"%");
			} else {
				label.setText(status);
			}
	        writeLog(status);
		}

	}
	catch (IOException e){
		writeLog("ISOWrite: Exception "+e.toString());
	}
	}

	private void writeLog(String line){
	PrintStream out = null;
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Date date = new Date();
	String datetime = dateFormat.format(date);
	try {
	    out = new PrintStream(new FileOutputStream(MovieLeafDir+"Log.txt",true));
	    String line2 = datetime+" "+line;
//	    status = line2;
	    out.println(line2);
  
	}
	catch(FileNotFoundException e) {
		// nothing to do
	}
	finally {
	    if (out != null) out.close();
	}
	}

	private void dirCheck(){
	File file=new File(MovieLeafDir);
	boolean exists = file.exists();
	boolean status = false;
	if (!exists) {
//		showError(MovieLeafDir+" does not exist");
		writeLog("dirCheck: "+MovieLeafDir+" does not exist");
//		status = new File(MovieLeafDir).mkdir();
//		if (status) {
//			showError("created directory");
//		} else {
//			showError("failed to create directory");
//		}
	//} else {
//		showError(MovieLeafDir+" exists");
	}
	}

	private String readreg (String Branch, String Leaf){
	String x = "REG QUERY \""+Branch+"\" /v "+Leaf; 
	String line="abc";
	try { 
		Process p = Runtime.getRuntime().exec(x);
		BufferedReader is;
		is = new BufferedReader(new InputStreamReader(p.getInputStream()));
		line = is.readLine();
		line = is.readLine();
		line = is.readLine();
	}
	catch (IOException e){
		writeLog("readReg: "+x+" Failed");
	}
	if (line != null) {
	String words[] = line.split("\\s+");
	return words[3];
	} else {
	return "";	
	}
	}
	
	private void eject(String isoDrive){
		String cmd = MovieLeafDir+"eject "+isoDrive;
		String line = "";
		writeLog("eject: Waiting for 5 seconds");
		for (int i= 0; i< 3; i++) {
		stopFor(5);
		try { 
			Process p = Runtime.getRuntime().exec(cmd);
			writeLog("eject: running "+cmd);
			BufferedReader is;
			is = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while((line=is.readLine())!=null) {
				writeLog("eject: "+line);
			}
		}
		catch (IOException e){
			writeLog("eject: "+cmd+" Failed");
		}
		}
	}
	private void stopFor(double Sec){
		long mSec = (long) Sec*1000;
		try {
			Thread.sleep(mSec);
		} catch (Exception e) {}

	}
	private long convertSize(String fileSize) {
		long size = 1000000L;
		if (fileSize == null) {
			return (size);
		}
		Pattern sizePattern = Pattern.compile("(\\d+)\\s*(\\S+)");
		Matcher sizeMatcher = sizePattern.matcher(fileSize);
		if (sizeMatcher.find()) {
			Long Obj = Long.valueOf(sizeMatcher.group(1));
			size = Obj.longValue();
			String units = sizeMatcher.group(2);
			if (units.equalsIgnoreCase("kb")) {
				size *= 1000;
			} else if (units.equalsIgnoreCase("mb")) {
				size *= 1000000;
			} else if (units.equalsIgnoreCase("gb")) {
				size *= 1000000000;
			}
			return (size);
		}
		sizePattern = Pattern.compile("(\\d+)");
		sizeMatcher = sizePattern.matcher(fileSize);
		if (sizeMatcher.find()) {
			Long Obj = Long.valueOf(sizeMatcher.group(1));
			size = Obj.longValue();
			return (size);
		}
		writeLog("convertSize: Can't convert "+fileSize+" to long. Try using non-decimals");
		return (size);
	}
	private String calcRemainingTime (){
		int secRemain = 360000;
		String remainString = "";
		if (numReadtotal > lastnumReadtotal) {
			secRemain = (int)((longfileSize-numReadtotal)/(numReadtotal-lastnumReadtotal));
		}
		
		Integer Seconds   = secRemain % 60 ;
		Integer InMinutes = secRemain / 60 ;
		Integer Minutes   = InMinutes % 60 ;
		Integer InHours   = InMinutes / 60 ;
		Integer Hours     = InHours % 24 ;
		Integer Days      = InHours / 24;
		
		if (Days > 0) {
			remainString = Days.toString() + "days ";
		}
		if (Hours > 0) {
			remainString = remainString+Hours.toString() + "hrs ";
		}
		if (Minutes > 0) {
			remainString = remainString+Minutes.toString() + "mins ";
		}
		if (Seconds > 0) {
			remainString = remainString+Seconds.toString() + "sec ";
		}
		if (remainString.length()< 1){
			remainString = "N/A";
		}
		return remainString;
	}
	private int percentDone (){
		if (longfileSize < 1){
			return 0;
		}
		return (int) (numReadtotal*100/longfileSize);
	}
	private int convertBurn(String line) {
		int burnPC = -1;
		Pattern burnPattern = Pattern.compile("(\\d+)%");
		Matcher burnMatcher = burnPattern.matcher(line);
		while (burnMatcher.find()) {
			Integer Obj = Integer.valueOf(burnMatcher.group(1));
			burnPC = Obj.intValue();
		}
	return(burnPC);
	}
	
	public static boolean removeDirectory(File directory) {

		  // System.out.println("removeDirectory " + directory);

		  if (directory == null)
		    return false;
		  if (!directory.exists())
		    return true;
		  if (!directory.isDirectory())
		    return false;

		  String[] list = directory.list();

		  // Some JVMs return null for File.list() when the
		  // directory is empty.
		  if (list != null) {
		    for (int i = 0; i < list.length; i++) {
		      File entry = new File(directory, list[i]);

		      //        System.out.println("\tremoving entry " + entry);

		      if (entry.isDirectory())
		      {
		        if (!removeDirectory(entry))
		          return false;
		      }
		      else
		      {
		        if (!entry.delete())
		          return false;
		      }
		    }
		  }

		  return directory.delete();
		}
}

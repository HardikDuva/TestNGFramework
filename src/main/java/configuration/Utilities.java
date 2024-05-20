package configuration;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utilities {
	static AutoLogger logger = new AutoLogger(Utilities.class);
	List<String> filesListInDir = new ArrayList<>();

	public static String getTimeStamp() {
		return (new SimpleDateFormat("yyMMddHHmmssSSS")).format(new Date());
	}

	public static void deleteDir(String path) {
		Path directory = Paths.get(path);
		try {
			Files.walkFileTree(directory, new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
					Files.delete(file); // this will work because it's always a File
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir); //this will work because Files in the directory are already deleted
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			System.out.println("-");
		}
	}

	public static String getCurrentDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		return dateFormat.format(date);
	}

	public static void appendTextToFile(String filePath, String text) {
		try {
			File fileObj = new File(filePath);
			fileObj.getParentFile().mkdirs();
		} catch (Exception e) {
			logger.e(e);
		}
		try (PrintWriter out = new PrintWriter(new BufferedWriter(
				new FileWriter(filePath, true)))) {
			out.println(text);
		} catch (IOException e) {
			logger.e(e);
		}
	}
	
	public static void appendHashMapToFile(String filePath, Map<String, String> orderSummary) {
		try {
			File fileObj = new File(filePath);
			fileObj.getParentFile().mkdirs();
		} catch (Exception e) {
			logger.e(e);
		}
		try (PrintWriter out = new PrintWriter(new BufferedWriter(
				new FileWriter(filePath, true)))) {
			for (Map.Entry<String, String> entry : orderSummary.entrySet()) {
				out.println(entry.getKey()+" is : "+entry.getValue());
			}
			
		} catch (IOException e) {
			logger.e(e);
		}
	}

	public static int generateRandomIntList(int stringLen) {
		int numList=0;
		try {
			
		   int max=9;
		   int min=1;
		   Random rand = new Random();
		   StringBuilder num= new StringBuilder();
		   	for(int i = 0; i < stringLen; i++){
		   		int x = rand.nextInt((max - min) + 1) + min;
				num.append(x);
		   	}
		   	numList=Integer.parseInt(num.toString());
		}
		catch(Exception e) {
			logger.e(e.getMessage());
		}	
		return numList;
	}
		
	public void zipFile(String zipDirName,String path) {
		
		try {
			File dir = new File(path);
	       this.zipDirectory(dir, zipDirName);
		}
		catch(Exception e) {
			logger.e(e.getMessage());
		}	
	}
	
	private void zipDirectory(File dir, String zipDirName) {
	        try {
	            populateFilesList(dir);
	            //now zip files one by one
	            //create ZipOutputStream to write to the zip file
	            FileOutputStream fos = new FileOutputStream(zipDirName);
	            ZipOutputStream zos = new ZipOutputStream(fos);
	            for(String filePath : filesListInDir){
	                System.out.println("Zipping "+filePath);
	                //for ZipEntry we need to keep only relative file path, so we used substring on absolute path
	                ZipEntry ze = new ZipEntry(filePath.substring(dir.getAbsolutePath().length()+1
					));
	                zos.putNextEntry(ze);
	                //read the file and write to ZipOutputStream
	                FileInputStream fis = new FileInputStream(filePath);
	                byte[] buffer = new byte[1024];
	                int len;
	                while ((len = fis.read(buffer)) > 0) {
	                    zos.write(buffer, 0, len);
	                }
	                zos.closeEntry();
	                fis.close();
	            }
	            zos.close();
	            fos.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	private void populateFilesList(File dir) throws IOException {
	        File[] files = dir.listFiles();
		if (files != null) {
			for(File file : files){
				if(file.isFile()) filesListInDir.add(file.getAbsolutePath());
				else populateFilesList(file);
			}
		}
	}


	public static String getStringFromArray(String[] arr) {
		StringBuilder str = new StringBuilder();
		for (String s : arr) {
			str.append(s).append(",");
		}
		if (str.toString().endsWith(",")) {
			str = new StringBuilder(str.substring(0, str.length() - 1));
		}
		return str.toString();
	}
	
	}

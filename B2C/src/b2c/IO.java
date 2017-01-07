package b2c;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class IO {
	public static void writeToFile(File file, String content, boolean deleteFile) {
		try {
			if (deleteFile) {
				file.delete();
			}
			file.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile(), true));
			bw.write(content);
			bw.close();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}
	
	public static String readFromRelativeFile(String fileName) {
		byte[] encoded = null;
		try {
			//For some reason it appends a '/' to the beginning of the string, making the file path invalid
			String relativePath = B2C.class.getClassLoader().getResource(fileName).getPath().substring(1);
			encoded = Files.readAllBytes(Paths.get(relativePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		String result = null;
		try {
			result = new String(encoded, "Cp1252");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static String readFromFile(String path) {
		/*String content = "";
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "Cp1252"))) {
		    content = br.
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		String content = "";
		byte[] encoded;
		try {
			encoded = Files.readAllBytes(Paths.get(path));
			content = new String(encoded, "Cp1252");
		} catch (IOException e) {
			e.printStackTrace();
		}
		//remove header
		content = content.substring(32);
		
		//due to unicode encoding, some characters get encoded as others
		
		content = content.replaceAll("\\u2020", new String(new char[]{0x86}));
		content = content.replaceAll("\\u2021", new String(new char[]{0x87}));
		content = content.replaceAll("\\u02C6", new String(new char[]{0x88}));
		content = content.replaceAll("\\u2030", new String(new char[]{0x89}));
		content = content.replaceAll("\\uFFFD", new String(new char[]{0x8F}));
		content = content.replaceAll("\\u2019", new String(new char[]{0x92}));
		content = content.replaceAll("\\u201D", new String(new char[]{0x94}));
		content = content.replaceAll("\\u2122", new String(new char[]{0x99}));
		content = content.replaceAll("\\u0161", new String(new char[]{0x9A}));
		content = content.replaceAll("\\u203A", new String(new char[]{0x9B}));
		content = content.replaceAll("\\u017E", new String(new char[]{0x9E}));
		//TODO actually parse the g1m
		String[] programs = content.split("PROGRAM[\\s\\S]{13}system[\\s\\S]{2}");
		
		String result = "";
		for (int i = 1; i < programs.length; i++) {
			String str = programs[i];
			
			//removes \0 's at the end of the file
			result += "\n" + G1MParser.convert(str.substring(0, str.indexOf("\0", str.length()-4)));
		}
		
		
		return result;
	}
}

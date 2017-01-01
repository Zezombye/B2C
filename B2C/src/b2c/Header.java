package b2c;

import java.io.File;

public class Header {
	
	static String headerDefines = "";
	static String headerPrototypes = "";
	static String headerGlobals = "";
	String headerVars = "";
	
	public static void addDefine(String content) {
		headerDefines += "#define "+content+"\n";
	}
	
	public static void addPrototype(String prototype) {
		headerPrototypes += prototype;
	}
	
	public static void addGlobal(String global) {
		headerGlobals += global;
	}
	
	public static void create() {
		
		String header = 
				"#ifndef MAIN_H\n" +
				"#define MAIN_H\n\n" + 
				headerDefines + "\n\n" + 
				"typedef unsigned char BCDvar[24]; //this defines BCDvar as an array of 24 unsigned chars\n" +
				"typedef struct {\n" +
				"\tint nbElements;\n" +
				"\tBCDvar *data;\n" +
				"} List;\n\n" +
				"typedef struct {\n" +
				"\tint width;\n" +
				"\tint height;\n" +
				"\tBCDvar *data;\n" +
				"} Mat;\n\n" +
				//"typedef unsigned short Fontchar;\n" +
				"typedef struct {\n" +
				"\tint length;\n" +
				"\tunsigned char* data;\n" +
				"} Str;\n\n" +
				"typedef struct {\n" +
				"\tunsigned char data[4];\n" +
				"} SmallStr;\n\n" +
				headerPrototypes + "\n" +
				headerGlobals +
				"\n#endif //MAIN_H";
		IO.writeToFile(new File(B2C.path + "/main.h"), header, true);
		
	}
	
	
}

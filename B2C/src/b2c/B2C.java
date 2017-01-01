package b2c;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/* TODO 's:
 * 
 * put each program in a separate file <program>.c
 * generate .g1w
 * 
 */

public class B2C {
	
	final static boolean debugMode = true;
	
	static String path = "C:\\Users\\Catherine\\Documents\\CASIO\\fx-9860G SDK\\TestB2C\\";
	static String mainProgramName = "__uiss4_";
	static String pathToG1M = "C:\\Users\\Catherine\\Desktop\\puiss4.g1m";
	static boolean isRealTimeGame = true;
	static boolean assureOS1Compatibility = true;
	static boolean usesAcOnTimer = true;
	static String main_c;
	
	/**
	 * Main method of B2C.
	 * args[0]: path to g1m file
	 * args[1]: main program name	 
	 * args[2]: path to project folder
	 */
	public static void main(String[] args) {
		if (!debugMode) {
			Scanner sc = new Scanner(System.in);
			System.out.println("Enter the path to the .g1m file:");
			while (true) {
				pathToG1M = ""+sc.nextLine().charAt(0);
				if (new File(pathToG1M).isFile()) {
					break;
				} else {
					System.out.println("File not found.");
				}
			}
			pathToG1M = pathToG1M.replaceAll("\\\\", "/");
			System.out.println("Enter the name of the main program.\nReplace 'r' with \"radius\", 'θ' by \"theta\" and non-ASCII characters by '_'.");
			mainProgramName = Parser.parseProgName(sc.nextLine());
			String programName = pathToG1M.substring(pathToG1M.lastIndexOf('/')+1, pathToG1M.lastIndexOf('.'));
			path = System.getProperty("user.home") + "\\Documents\\CASIO\\fx-9860G SDK\\"
					+ programName + System.getProperty("file.separator");
			System.out.println("Enter the destination path. Write \"default\" to set to:\n" + path);
			String destinationPath = sc.nextLine();
			if (!destinationPath.equals("default")) {
				path = destinationPath;
			}
			new File(path).mkdir();
	
			//TODO create custom image
			try {
				Files.copy(new File(B2C.class.getClassLoader().getResource("MainIcon.bmp").getPath().substring(1)).toPath(),
						new File(path + "/MainIcon.bmp").toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			IO.writeToFile(new File(path + File.separator + programName + ".g1w"),
					IO.readFromRelativeFile("Default.g1w").replaceAll("%PROG_NAME%", programName), true);
			IO.writeToFile(new File(path + File.separator + "AddinInfo.txt"), 
					IO.readFromRelativeFile("AddinInfo.txt").replaceAll("%PROG_NAME%", programName), true);
			
		}
		if (args.length > 0) {
			pathToG1M = args[0];
			if (!pathToG1M.matches(".+\\.g[12][mr]")) {
				Parser.error("File provided (" + pathToG1M + ") is not of g1m type!");
			}
		}
		if (args.length > 1) {
			mainProgramName = args[1];
		}
		if (args.length > 2) {
			path = args[2];
		}
		long startTime = System.currentTimeMillis();
		
		//Add some constants for functions
		Constants.add("0");
		Constants.add("1");
		Constants.add("-1");
		/*Constants.add("53123523");
		Constants.add("0.3");
		Constants.add("2.304");
		Constants.add("-1");
		Constants.add("0.0456");
		Constants.add("-0.00786");*/
		
		main_c =
				"#include <stdlib.h>\n" +
				"#include <stdarg.h>\n" +
				"#include <math.h>\n" +
				"#include <limits.h>\n" +
				"#include <string.h>\n" +
				"#include \"fxlib.h\"\n" +
				"#include \"main.h\"\n\n" +
				"unsigned int key;\n" +
				"int i;\n"+
				"BCDvar var[29] = {0}; //A-Z, r, theta, Ans\n"+
				"Str strings[20];\n" + 
				"Mat mat[26]; //Important thing: matrixes are (height, width) not (width, height)\n" +
				"List list[26];\n" +
				"char dummyOpCode[2] = {5, 8};\n" +
				"//These are buffers for syscalls that do not return a value.\n" +
				"BCDvar alphaVarBuffer;\n" +
				"BCDvar expressionBuffer;\n" +
				"BCDvar getkeyBuffer;\n" +
				"unsigned char stringBuffer[256] = {0};\n";
				/*"const BCDvar ZERO = {0};\n";
				for (int i = 1; i <= 9; i++) {
					main_c += "const BCDvar " + Parser.consts.get(i) + " = {0x10, 0x0" + i + "};\n";
				}*/
		main_c+="\nint AddIn_main(int isAppli, unsigned short OptionNum) {\n" +
					"\t//Initialize strings\n" +
					"\tfor (i = 0; i < 20; i++) {\n" +
						"\t\tstrings[i].length = 0;\n" +
					"\t}\n" +
					"\t#ifdef USES_INTERRUPTION_TIMER\n" +
					"\t//Timer allowing AC/ON to be pressed at any moment\n" +
					"\tSetTimer(INTERRUPTION_TIMER, 50, (void (*)(void))exitTimerHandler);\n" +
					"\t#endif\n" +
					"\tprog_"+mainProgramName+"();\n\n" +
					"\tdo {\n" +
					"\t\tGetKey(&key);\n" +
					"\t} while (key != KEY_CTRL_EXE && key != KEY_CTRL_AC);\n" +
					"\treturn 1;\n" +
				"}\n\n";
		
		main_c += IO.readFromFile(pathToG1M);
		
		System.out.println("Result:\n-------------\n"+main_c);
		
		//pragma stuff
		main_c += "\n\n#pragma section _BR_Size\nunsigned long BR_Size;\n#pragma section\n\n"
				+ "#pragma section _TOP\n"
				+ "int InitializeSystem(int isAppli, unsigned short OptionNum) {"
					+ "\n\treturn INIT_ADDIN_APPLICATION(isAppli, OptionNum);\n}\n"
				+ "#pragma section\n";
		
		//GetKey handling
		/*if (isRealTimeGame) {
			main_c = main_c.replaceAll(
					"(do \\{\\n+([\\t ]+)?([\\w\\[\\]])+? \\= )Getkey_Temp(\\(\\);\n([\\t ]+)?} while \\()",
					"$1Getkey_Block$4"
					);
			main_c = main_c.replaceAll("Getkey_Temp\\(\\);", "Getkey_NoBlock();");
		} else {
			main_c = main_c.replaceAll("Getkey_Temp\\(\\);", "Getkey_Block();");
		}*/
		

		main_c += Functions.getFunctions();
		
		IO.writeToFile(new File(path + "/main.c"), main_c, true);
		
		//Syscalls asm file
		
		/*writeToFile(new File(path + "syscalls.src"),
				  "\t.SECTION P,CODE,ALIGN=4\n\n"
				+ "\t.MACRO SYSCALL FUNO, SYSCALLNAME, TAIL=nop\n\n"
				+ "\t.export \\SYSCALLNAME'\n"
				+ "\\SYSCALLNAME'\n"
				+ "\tmov.l #h'\\FUNO, r0\n"
				+ "\tmov.l #H'80010070, r2\n"
				+ "\tjmp @r2\n"
				+ "\t\\TAIL'\n"
				+ "\t.ENDM\n\n"
				+ "\tSYSCALL "
				, true);*/
		Syscalls.addSyscall("bcdToStr", "4F0");
		Syscalls.addSyscall("intToBCD", "5A6");
		Syscalls.addSyscall("calcExp", "645");
		Syscalls.addSyscall("getAlphaVar", "4DF");
		Syscalls.addSyscall("setAlphaVar", "4E0");
		Syscalls.addSyscall("prgmGetkey", "6C4");
		Syscalls.addSyscall("putMatrixCode", "24F");
		//Syscalls.addSyscall("installTimer", "118");
		//Syscalls.addSyscall("startTimer", "11A");
		//Syscalls.addSyscall("uninstallTimer", "119");
		Syscalls.addSyscall("putInternalItem", "82A");
		Syscalls.addSyscall("deleteInternalItem", "835");
		Syscalls.addSyscall("openItem", "83B");
		Syscalls.addSyscall("getItemData", "372");
		Syscalls.addSyscall("getItemSize", "840");
		Syscalls.addSyscall("overwriteItemData", "830");
		Syscalls.addSyscall("setSetupEntry", "4DD");
		Syscalls.addSyscall("getSetupEntry", "4DC");
		Syscalls.addSyscall("dispErrorMessage", "954");
		Syscalls.createSyscallFile();
		
		String[] externalLibs = {"MonochromeLib.c", "MonochromeLib.h", "memory.c", "memory.h"};
		for (int i = 0; i <= 1; i++) {
			IO.writeToFile(new File(path+externalLibs[i]), IO.readFromRelativeFile(externalLibs[i]), true);
		}
		for (char c = 'A'; c <= 'Z'; c++) {
			Header.addDefine(c+" "+(int)c);
		}
		Header.addDefine("FALSE 0");
		Header.addDefine("TRUE 1");
		Header.addDefine("A_GREATER_THAN_B 1");
		Header.addDefine("A_EQUALS_B 0");
		Header.addDefine("A_LESS_THAN_B -1");
		Header.addDefine("NO_ERROR 1");
		Header.addDefine("MEMORY_ERROR 4");
		Header.addDefine("INTERRUPTION_TIMER 2");
		if (usesAcOnTimer) {
			Header.addDefine("USES_INTERRUPTION_TIMER");
		}
		Header.addDefine("DIR_PROG 0x01");
		Header.addDefine("DIR_LIST 0x05");
		Header.addDefine("DIR_MAT 0x06");
		Header.addDefine("DIR_PICT 0x07");
		Header.addDefine("DIR_CAPT 0x0A");
		
		Header.addDefine("LIST_START 0x10");
		Header.addDefine("MAT_START 0x10");
		
		Header.addDefine("ANS 28");
		Header.addDefine("THETA 27");
		Header.addDefine("RADIUS 26");
		
		Header.addDefine("SETUP_LISTFILE 0x2E");
		
		Header.addDefine("free_str(x) if(!isString){free(x->data); free(x);}");
		Header.addDefine("getDigit(BCDvar, i) (((i)%2) ? (*(BCDvar))[((i)+1)/2+1]>>4 : (*(BCDvar))[((i)+1)/2+1]&0x0F)");
		Header.addDefine("getExp(BCDvar) (((*(a))[0]>>4) * 100 + ((*(a))[0]&0x0F) * 10 + ((*(a))[1]>>4))");
		Header.create();
		
		System.out.println("Parsing done in " + (System.currentTimeMillis()-startTime) + " ms.");
		if (!assureOS1Compatibility) {
			System.out.println("WARNING: This program uses OS 2 functions. It won't run properly on the SDK emulator!");
		}
	}
}

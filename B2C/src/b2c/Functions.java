package b2c;

//This class contains all B2C functions.

public class Functions {
	
	static String functions = "\n//B2C functions\n\n";
	
	//This is for automatically generated methods; for unique methods, write them manually in the local B2CFunctions.c file.
	public static void addFunctions() {
		/*
		//Calculation functions
		String[] operators = {
				"7F\"\"\\xB4",
				"10", // <=
				"11", // !=
				"12", // >=
				"3D", // =
				"3C", // <
				"3E", // >
				"A8", // ^
				"A9", // *
				"B9", // /
				"89", // +
				"99"  // -
		};
		String[] calcFunctions = {
				"xor",
				"lessOrEqualThan",
				"notEqualTo",
				"greaterOrEqualThan",
				"equalTo",
				"lessThan",
				"greaterThan",
				"pow",
				"mult",
				"div",
				"add",
				"sub",
		};
		for (int i = 0; i < operators.length; i++) {
			functions += addMethod( 
					"BCDvar B2C_" + calcFunctions[i] + "(BCDvar a, BCDvar b) {\n" +
						"\tBCDvar result;\n" +
						"\tconst char *function = \"A\\x" + operators[i] + "\"\"B\";\n" +
						"\tsetAlphaVar('A', &a);\n" +
						"\tsetAlphaVar('B', &b);\n" +
						"\tcalcExp(&function" +
						//_"+calcFunctions[i] +
						", dummyOpCode, &result, 1);\n" +
						"\treturn result;\n}\n");
			//Header.addGlobal("char *function_"+calcFunctions[i]+" = \"A\"\"\\x" + operators[i] + "\"\"B\";\n");
					
		}
		String[] logicalOperators = {
				"&&", "and",
				"||", "or",
		};
		for (int i = 0; i < logicalOperators.length; i+=2) {
			functions += addMethod(
					"BCDvar B2C_" + logicalOperators[i+1] + "(BCDvar a, BCDvar b) {\n" +
					"\tif (a.bytes[1] " + logicalOperators[i] + " b.bytes[1]) {\n" +
						"\t\treturn _1_;\n" +
					"\t}\n" +
					"\treturn _0_;\n}\n"
					);
		}
		*/
		/*functions += addMethod(
				"BCDvar B2C_not(BCDvar a) {\n" +
				"\tif (a.bytes[1])\n" +
					"\t\treturn ZERO;" +
				"\treturn ONE;\n}\n"
				);
		
		functions += addMethod(
				"char* B2C_convToStr(BCDvar nb) {\n" +
				"\tchar* result = calloc(15, 1);\n" +
				"\tbcdToStr(&nb, result);\n" +
				"\treturn result;\n}\n");
		
		functions += addMethod(
				"BCDvar B2C_convToBCD(char* str) {\n" +
				"\tBCDvar result;\n" +
				"\tcalcExp(&str, dummyOpCode, &result, 1);\n" +
				"\treturn result;\n}\n");
		
		functions += addMethod(
				"void B2C_setListRow(int nbList, int row, BCDvar value) {\n" +
				"\tif (row > list[nbList].nbElements) {\n" +
					"\t\tBCDvar *tempPtr = realloc(list[nbList].data, (list[nbList].nbElements+1)*sizeof(BCDvar));\n" +
					"\t\tif (tempPtr == NULL) {\n" +
						"\t\t\tfree(list[nbList].data);\n" +
						"\t\t\tB2C_stop();\n\t\t}\n" +
					"\t\tlist[nbList].data = tempPtr;\n" +
					"\t\tlist[nbList].nbElements++;\n\t}\n" +
				"\tlist[nbList].data[row] = value;\n}\n"
				);
		functions += addMethod(
				"void B2C_setDimList(int nbList, int nbElements) {\n" +
				"\tfree(list[nbList].data);\n" +
				"\tlist[nbList].data = calloc(nbElements+1, sizeof(BCDvar));\n}\n"
				);
		
		functions += addMethod(
				"List B2C_newList(int nbElements, ...) {\n" +
				"\tList list;\n" +
				"\tva_list vaList;\n" +
				"\tlist.nbElements = nbElements;\n" +
				"\tlist.data = calloc(nbElements+1, sizeof(BCDvar));\n" +
				"\tva_start(vaList, nbElements);\n" +
				"\tlist.data[0] = ZERO;\n" +
				"\tfor (i = 1; i <= nbElements; i++) {\n" +
					"\t\tlist.data[i] = va_arg(vaList, BCDvar);\n" +
				"\t}\n" +
				"\tva_end(vaList);\n" +
				"\treturn list;\n}\n"
				);
		
		//TODO: add method which takes (matrix, height, width) as argument, could lead to a faster conversion than initializing a list
		functions += addMethod(
				"void B2C_setDimMat(int matrix, List list) {\n" +
				"\tif (mat[matrix].data) free(mat[matrix].data);\n" +
				"\tmat[matrix].data = calloc((B2C_convToUInt(list.data[2])+1)*(B2C_convToUInt(list.data[1])+1), sizeof(BCDvar));\n" +
				"\tmat[matrix].width = B2C_convToUInt(list.data[1]);\n" +
				"\tmat[matrix].height = B2C_convToUInt(list.data[2]);\n" +
				"}\n"
				);
		
		functions += addMethod(
				"int B2C_convToUInt(BCDvar nb) {\n" +
				"\tint result = 0;\n" +
				"\tint power = (nb.bytes[1]>>4) + 1;\n" +
				"\tfor (i = 1; i <= power; i++) {\n" +
					"\t\tif (i%2) {\n" +
						"\t\t\tresult += (nb.bytes[i/2+1]&0xF) * pow(10, power-i);\n" +
					"\t\t} else {\n" +
						"\t\t\tresult += (nb.bytes[i/2+1]>>4) * pow(10, power-i);\n" +
					"\t\t}\n" +
				"\t}\n" +
				"\treturn result;\n}\n"
				);
		
		functions += addMethod(
				"BCDvar B2C_Getkey() {\n" +
				"\tBCDvar result;\n" +
				"\tif (!prgmGetkey(&result)) {\n" +
					"\t\tB2C_stop();\n" +
				"\t}\n" +
				"\treturn result;\n}\n"
				);
		
		functions += addMethod(
				"void timerHandler() {\n" +
				"\tshort menuCode = 0x0308;\n" +
				"\tputMatrixCode(&menuCode);" +
				"\n}\n"
				);
		
		functions += addMethod(
				"void B2C_stop() {\n" +
				"\tinstallTimer(6, (void*)&timerHandler, 1);\n" +
				"\tstartTimer(6);\n" +
				"\tGetKey(&key);\n" +
				"\tuninstallTimer(6);\n" +
				"\tPopUpWin(4);\n" +
				"\tlocate(5,3); Print(\"Interruption\");\n" +
				"\tlocate(4,5); Print(\"Appuyer:[MENU]\");\n" +
				"\twhile(1)\n" +
					"\t\tGetKey(&key);\n" +
				"\n}\n"
				);
		
		//TODO actually randomize
		functions += addMethod(
				"BCDvar B2C_ranInt(int a, int b) {\n" +
				"\treturn ONE;\n}\n"
				);*/
	}
	
	public static String getFunctions() {
		addFunctions();
		String functionsBuffer = IO.readFromRelativeFile("B2CFunctions.c");
		Integer[] functionsPositions = Parser.parseBrackets(functionsBuffer);

		//System.out.println(functionsBuffer.substring(0, functionsPositions[2]));
		functions += addMethod(functionsBuffer.substring(0, functionsPositions[3]+1));
		for (int i = 3; i < functionsPositions.length-4; i+=4) {
			//System.out.println(functionsBuffer.substring(functionsPositions[i], functionsPositions[i+4]));
			functions += addMethod(functionsBuffer.substring(functionsPositions[i]+1, functionsPositions[i+4]+1));
		}
		return functions;
	}
	
	//Automatically generates the prototypes for the given method.
	public static String addMethod(String method) {
		
		//This should normally get the method name.
		//String methodName = method.substring(method.substring(0, method.indexOf('(')).indexOf(' '), method.indexOf('('));
		Header.addPrototype(method.substring(0, method.indexOf('{')).trim() + ";\n");
		return method;
	}
}

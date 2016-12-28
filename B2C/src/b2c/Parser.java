package b2c;

import java.util.ArrayList;
import java.util.Arrays;

public class Parser {
	
	final static int NO_OPTION = 0;
	final static int WHOLE_INSTRUCTION = 1;
	final static int CONV_TO_BCD = 2;
	final static int CONV_TO_INT = 3;

	static String tabs = "\t";
	
	//Be sure to clear these variables between programs.
	static ArrayList<String> instructions = new ArrayList<String>();
	static int instructionNumber = 0;
	static int nbBuffers = 0;
	
	
	public static String parse(String content) {
		return parse(content, NO_OPTION);
	}
	
	/* This is the main method for the parsing.
	 * It recursively parses each instruction it receives.
	 * Note that instructions must not have the \r at the end, or any \r at all.
	 * It obviously assumes that the instruction is valid Basic Casio code.
	 * 
	 * When adding a method returning a value, always declare it like this:
	 * 
	 * if (content.startsWith(method) {
	 *     return supportAns(isWholeInstruction, <parsing result>); 
	 * }
	 * 
	 * This is to provide support for the Ans variable.
	 */
	
	public static String parse(String content, int option) {
		
		String instruction = "";
		for (int i = 0; i < content.length(); i++) {
			if (content.charAt(i) >= 32 && content.charAt(i) < 127) {
				instruction += content.charAt(i) + " ";
			} else {
				String hex = Integer.toHexString(content.charAt(i));
				instruction += "0x" + hex + " ";
				if (hex.length() > 2) {
					error("Unhandled unicode character u+" + hex.length());
				}
			}
		}
		System.out.println("Parsing instruction: " + instruction);
		
		

		if (content.equals("")) {
			return "";
		}
		
		int matchResult = -1;
		
		//comment
		if (content.startsWith("'")) {
			return "//"+content.substring(1);
		}
		
		//instruction colon ':' that counts as a \r
		matchResult = checkMatch(content, ":");
		if (matchResult >= 0) {
			instructions.add(instructionNumber, content.substring(matchResult+1));
			return parse(content.substring(0, matchResult), WHOLE_INSTRUCTION) + "\n" + tabs + 
					parse(content.substring(matchResult+1), WHOLE_INSTRUCTION);
		}
		
		if (content.startsWith("\"") && option == WHOLE_INSTRUCTION) {
			System.out.println("Hardcoded string detected, removing it...");
			return "";
		}
		
		
		
		//The easy functions: those that always have their arguments after, AND that don't return anything. (exception of =>)
		//Functions like Int or Frac have their arguments after but return a value so they can be used in a calculation.
		//Those are listed in the alphabetical order of their opcodes. 
		//So List (0x7F51) is above Lbl (0xE2) which is above If (0xF700). (again, exception of => which is after IfEnd)
				
		//RanInt#(
		/*if (content.startsWith(new String(new char[]{0x7F,0x87}))) {
			if (content.charAt(content.length()-1) == ')') {
				content = content.substring(0, content.length()-1);
			}
			Integer[] args = parseArgs(content);
			if (args.length != 1) {
				error("RanInt# method doesn't have 2 arguments!");
			}
			return supportAns("B2C_ranInt(" + parse(content.substring(2, args[0]), CONV_TO_BCD) + ","
					+ parse(content.substring(args[0]+1), CONV_TO_BCD) + ")", option);
			
		}*/
		
		//Getkey
		if (content.startsWith(new String(new char[]{0x7F,0x8F}))) {
			if (content.length() > 2) {
				error("Instruction begins by GetKey but includes something else!");
			}
			
			return supportAns("B2C_Getkey()", option);
		}
		
		//Lbl
		if (content.startsWith(new String(new char[]{0xE2}))) {
			content = content.substring(1); //remove the Lbl to exploit the variable
			if (content.equals(new String(new char[]{0xCD}))) {
				content = "radius";
			} else if (content.equals(new String(new char[]{0xCE}))) {
				content = "theta";
			}
			return "Lbl_"+content+":;"; //the ';' is needed because you can place Lbls just before IfEnd/WhileEnd/Next
		}
		
		//Goto
		if (content.startsWith(new String(new char[]{0xEC}))) {
			content = content.substring(1); //remove the Goto to exploit the variable
			if (content.equals(new String(new char[]{0xCD}))) {
				content = "radius";
			} else if (content.equals(new String(new char[]{0xCE}))) {
				content = "theta";
			}
			if (content.length() > 2) {
				error("Instruction begins by Goto but includes something else!");
			}
			String[] gotosAreBad = {
					"Gotos are the root of all evil.",
					"I know you're writing in basic but still... gotos?",
					"Get that goto out of there, we're not in assembly.",
					"I sincerely hope you're not using that goto for a loop. Else you need to learn about do/while.",
					"This is justified if and only if you are using this goto for a Menu.",
					"You're lucky gotos work in C the exact way they do in basic.",
					"Gotos are bad and you should feel bad.",
					"The use of gotos in your program makes it read like a \"Chose your own adventure\" book.",
					"http://xkcd.com/292/",
					"If you're using that to break out of nested loops... I'll allow it.",
					"Like this converted code wasn't unreadable enough.",
			};
			String result = "goto Lbl_"+content+";";
			if (Math.random() > 0.9) {
				result += " //" + Arrays.asList(gotosAreBad).get((int)(Math.random()*gotosAreBad.length));
			}
			return result;
		}
		
		//Prog
		if (content.startsWith(new String(new char[]{0xED}))) {
			String result = content.substring(1);
			
			if (result.charAt(0) == '"') {
				result = result.substring(1);
			}
			if (result.charAt(result.length()-1) == '"') {
				result = result.substring(0,  result.length()-1);
			}
			
			return "prog_"+parseProgName(result)+"();";
		}
		
		//If
		if (content.startsWith(new String(new char[]{0xF7,0x00}))) {
			incrementTabs();
			return "if ((*" + parse(content.substring(2), CONV_TO_BCD) + ")[1]) {";
		}
		
		//Then
		if (content.startsWith(new String(new char[]{0xF7,0x01}))) {
			return parse(content.substring(2), WHOLE_INSTRUCTION);
		}
		
		//Else
		if (content.startsWith(new String(new char[]{0xF7,0x02}))) {
			decrementTabs();
			String result = "\n" + tabs + "} else {\n";
			incrementTabs();
			return result + tabs + parse(content.substring(2), WHOLE_INSTRUCTION);
			
		}
		
		//IfEnd ; it is always a single instruction
		if (content.startsWith(new String(new char[]{0xF7,0x03}))) {
			decrementTabs();
			if (content.length() > 2) {
				error("Instruction begins by IfEnd but includes something else!");
			}
			return "\n" + tabs + "}";
		}
		
		//inline if (double arrow '=>')
		matchResult = checkMatch(content, new String(new char[]{0x13}));
		if (matchResult >= 0) {
			incrementTabs();
			String result = "if ((*" + parse(content.substring(0, matchResult), CONV_TO_BCD)
			+ ")[1]) {\n" + tabs + parse(content.substring(matchResult+1), WHOLE_INSTRUCTION) + "\n";
			decrementTabs();
			return result + "\n" + tabs + "}";
		}
		
		//For, To, Step
		if (content.startsWith(new String(new char[]{0xF7,0x04}))) {
			//Stocks the position of the "To", no need to check for strings because there are no strings in a for
			int toPosition = content.indexOf(new String(new char[]{0xF7, 0x05}));
			//Stocks the position of the ->
			int assignmentPosition = content.indexOf((char)0x0E);
			//Checks for "Step"
			int stepPosition = content.indexOf(new String(new char[]{0xF7, 0x06}));
			String variable = replaceNonAscii(content.substring(assignmentPosition+1, toPosition));
			System.out.println("Parsing a For instruction. Position of To is "+toPosition+
					", position of -> is "+assignmentPosition+", position of Step is "+stepPosition+
					", variable is: "+variable);
			
			//Check for empty for, which is replaced by Sleep()
			if (instructions.get(instructionNumber+1).equals(new String(new char[]{0xF7, 0x07}))) {
				System.out.println("Parsing empty for");
				String result = "Sleep(";
				if (stepPosition >= 0) {
					result += "Sleep(" + handleIntConversion(content.substring(toPosition+2, stepPosition)) + "/"+
							handleIntConversion(content.substring(stepPosition+2));
				} else {
					result += handleIntConversion(content.substring(toPosition+2));
				}
				instructionNumber++;
				return result + ");";
			}
			incrementTabs();
			String result = "for (";
			//variable = beginning;
			result += parse(content.substring(2, toPosition)) + ";";
			
			//TODO: parse the step as an integer to know if it is <0 or >0
			//also put the break condition in the for
			if (stepPosition >= 0) {
				//step < 0 && var >= limit || step > 0 && var <= limit; variable = variable + step) {
				String step = replaceNonAscii(content.substring(stepPosition+2));
				System.out.println("Step = " + step);
				String limit = replaceNonAscii(content.substring(toPosition+2, stepPosition));
				result += " (*B2C_calcExp((unsigned char*)\""+step+"<0\\x7F\"\"\\xB0\"\""+variable+"\\x12\"\""+limit+"\\x7F\"\"\\xB1\"\""+step+">0\\x7F\"\"\\xB0\"\""+variable+"\\x10\"\""+limit+"\"))[1]" + "; "
						+ parse(variable+(char)0x89+step+(char)0x0E+variable) + ") {";
						//+ "\n" + tabs + "if ((*B2C_calcExp((unsigned char*)\""+step+"<0\\x7F\"\"\\xB0\"\""+variable+"<"+limit+"\\x7F\"\"\\xB1\"\""+step+">0\\x7F\"\"\\xB0\"\""+variable+">"+limit+"\"))[1]) break;";
			} else {
				//variable <= limit; variable = variable + 1) {"
				result += " (*B2C_calcExp((unsigned char*)\"" + variable + "\\x10\"\"" + replaceNonAscii(content.substring(toPosition+2)) + "\"))[1]; "
						+ parse(variable + (char)0x89 + "1" + (char)0x0E + variable) + ") {";
			}
			
			return result;
		}
		
		//Next ; like IfEnd
		if (content.startsWith(new String(new char[]{0xF7,0x07}))) {
			if (content.length() > 2) {
				error("Instruction begins by Next but includes something else!");
			}
			decrementTabs();
			return "\n"+tabs+"}";
		}
		
		//While
		if (content.startsWith(new String(new char[]{0xF7,0x08}))) {
			incrementTabs();
			return "while ((*" + parse(content.substring(2), CONV_TO_BCD) + ")[1]) {";
		}
		
		//WhileEnd
		if (content.startsWith(new String(new char[]{0xF7,0x09}))) {
			if (content.length() > 2) {
				error("Instruction begins by WhileEnd but includes something else!");
			}
			decrementTabs();
			return "\n" + tabs + "}";
		}
		
		//Do
		if (content.startsWith(new String(new char[]{0xF7,0x0A}))) {
			if (content.length() > 2) {
				error("Instruction begins by Do but includes something else!");
			}
			incrementTabs();
			return "do {";
		}
		
		//LpWhile
		if (content.startsWith(new String(new char[]{0xF7,0x0B}))) {
			decrementTabs();
			return "\n" + tabs + "} while ((*" + parse(content.substring(2)) + ")[1]);";
		}
		
		//Return
		if (content.startsWith(new String(new char[]{0xF7,0x0C}))) {
			if (content.length() > 2) {
				error("Instruction begins by Return but includes something else!");
			}
			return "return;";
		}
		
		//Break
		if (content.startsWith(new String(new char[]{0xF7,0x0D}))) {
			if (content.length() > 2) {
				error("Instruction begins by Break but includes something else!");
			}
			return "break;";
		}
		
		//Stop
		if (content.startsWith(new String(new char[]{0xF7,0x0E}))) {
			if (content.length() > 2) {
				error("Instruction begins by Stop but includes something else!");
			}
			return "B2C_stop();";
		}
		
		//Locate
		if (content.startsWith(new String(new char[]{0xF7,0x10}))) {
			Integer[] args = parseArgs(content);
			return "locate(" + handleIntConversion(content.substring(2, args[0])) + ", "
					+ handleIntConversion(content.substring(args[0]+1, args[1]))
					+ "); Print((unsigned char*)"
					+ parseStr(content.substring(args[1]+1)) + "); ML_display_vram();";
		}
		
		//ClrText
		if (content.startsWith(new String(new char[]{0xF7,0x18}))) {
			if (content.length() > 2) {
				error("Instruction begins by ClrText but includes something else!");
			}
			return "ML_clear_vram();";
		}
		
		
		
		//End of starting functions. At this point the instruction is likely a mathematical operation, or a string,
		//or a variable assignment. Note that it can have functions inside, like the factorial or nCr function.
		
		if (content.startsWith("\"") || content.startsWith(new String(new char[]{0xF9, 0x3F}))) { //it is a standalone string
			return parseStr(content);
		}
		
		//Check for assignment
		
		matchResult = checkMatch(content, new String(new char[]{0x0E}));
		if (matchResult >= 0) {
			
			//Check for the Dim assignment case; in this case it's not an assignment but a method calling
			if (content.substring(matchResult+1).startsWith(new String(new char[]{0x7F, 0x46}))) {
				//The assignment is followed by a Dim. Now check if it's to a List or a Mat
				System.out.println("Parsing a Dim assignment.");
				
				if (content.substring(matchResult+3).startsWith(new String(new char[]{0x7F, 0x40}))) { //followed by Mat
					String result = "B2C_setDimMat('" + content.charAt(matchResult+5) + "', ";
					if (content.startsWith(new String(new char[]{0x7F, 0x51}))) {
						result += handleIntConversion(parse(content.substring(0, matchResult) + "[1]")) + ", ";
						result += handleIntConversion(parse(content.substring(0, matchResult) + "[2]")) + ");";
						return result;
					} else if (content.startsWith("{")) {
						Integer[] commaPos = parseArgs(content.substring(1, matchResult));
						if (commaPos.length != 1) {
							error("List must consist of 2 numbers!");
						}
						result += content.substring(1, commaPos[0]+1) + ", ";
						if (content.charAt(matchResult-1) != '}') {
							content = content.substring(0, matchResult) + '}' + content.substring(matchResult);
							matchResult++;
						}
						result += content.substring(commaPos[0]+2, matchResult-1) + ");";
						return result;
					} else {
						error("Unknown mat assignment");
					}
					
				} else if (content.substring(matchResult+3).startsWith(new String(new char[]{0x7F, 0x51}))) { //followed by List
					return "B2C_setDimList(" + handleIntConversion(content.substring(matchResult+5)) + ", "
							+ handleIntConversion(content.substring(0, matchResult)) + ");";
				} else {
					error("Dim instruction is not followed by List or Mat!");
				}
				
			//Check for '~' operator
			} else if (content.substring(matchResult+1).matches("[A-Z]~[A-Z]")) {
				String assignment = parse(content.substring(0, matchResult), CONV_TO_BCD);
				incrementTabs();
				String result = "for (i = '"+content.charAt(matchResult+1)+"'; i <= '"+content.charAt(matchResult+3)+"'; i++) {\n"+tabs+"B2C_setAlphaVar(i, "+assignment+");\n";
				decrementTabs();
				return result + tabs + "}";
			
			//Check for list assignment
			} else if (content.substring(matchResult+1).startsWith(new String(new char[]{0x7F, 0x51}))) {
				Integer[] check = parseBrackets(content.substring(matchResult+1));
				
				if (check.length > 0) {
					if (check.length == 2 && matchResult+1+check[1] == content.length()-1) {
						content = content.substring(0, content.length()-1);
					}
					String result = "B2C_setListRow(" + handleIntConversion(
							content.substring(matchResult+3, matchResult+1+check[0])) +
					", " + handleIntConversion(
							content.substring(matchResult+2+check[0], content.length())) +
					", " + parse(content.substring(0, matchResult), CONV_TO_BCD) + ");";
					return result;
				}
				
			//Check for Mat assignment
			} else if (content.substring(matchResult+1).startsWith(new String(new char[]{0x7F, 0x40}))) {
				//Account for possible unmatched bracket
				if (content.charAt(content.length()-1) != ']') {
					content += ']';
				}
				Integer[] check = parseArgs(content.substring(matchResult+5, content.length()-1));
				
				if (check.length != 1) {
					error("Mat instruction does not have one comma!");
				}
				String result = "B2C_setMat('" + content.charAt(matchResult+3) + "', " +
						handleIntConversion(content.substring(matchResult+5, matchResult+5+check[0])) + ", " +
						handleIntConversion(content.substring(matchResult+5+check[0]+1, content.length()-1)) + ", " +
						parse(content.substring(0, matchResult), CONV_TO_BCD) + ");";
				
				return result;
				
				/*if (check.length > 0) {
					if (check.length == 2 && matchResult+1+check[1] == content.length()-1) {
						content = content.substring(0, content.length()-1);
					}
					String result = "B2C_setMat(" + handleIntConversion(
							content.substring(matchResult+3, matchResult+1+check[0])) +
					", " + handleIntConversion(
							content.substring(matchResult+2+check[0], content.length())) +
					", " + parse(content.substring(0, matchResult), CONV_TO_BCD) + ");";
					return result;
				}*/
			
			//Check for variable assignment
			} else if (content.substring(matchResult+1).matches("[A-Z\\xCD\\xCE\\xC0]")) {
				String result = "B2C_setAlphaVar('";
				if (content.charAt(matchResult+1) >= 'A' && content.charAt(matchResult+1) <= 'Z') {
					result += content.charAt(matchResult+1);
				} else {
					result += "\\x" + Integer.toHexString(content.charAt(matchResult+1));
				}
				result += "', " + parse(content.substring(0, matchResult), CONV_TO_BCD) + ")";
				if (option == WHOLE_INSTRUCTION) {
					result += ";";
				}
				return result;
			} else {
				error("Unknown assignment!");
			}
			
		}
		
		//at this point it is a mathematical operation
		//stock the level of the parentheses
		Integer[] parenthesesPos = parseBrackets(content);

		//Mat
		if (content.startsWith(new String(new char[]{0x7F,0x40}))) {
			System.out.println("Parsing a matrix");
			//Before parsing, we must check if the entire instruction is a Mat instruction
			Integer[] check = parseBrackets(content);
			Integer[] arg = parseArgs(content.substring(check[0]+1, check[1]));
			if (check[1] == content.length()-1) {
				if (arg.length != 1) {
					error("matrix coordinates are fewer or more than two!");
				} else {
					return supportAns("mat[" + (content.charAt(2)-'A') + "].data[mat[" + (content.charAt(2)-'A') + "].width*(" 
							+ handleIntConversion(parse(content.substring(check[0]+1, check[0]+1+arg[0]))) + ")+("
							+ handleIntConversion(parse(content.substring(check[0]+2+arg[0],content.length()-1))) + ")]", option);
				}
				
			}
			
		}
		
		//Dim ; for now it cannot parse it automatically, need to wait for the getNextArg() function
		//TODO do getNextArg()
		if (content.startsWith(new String(new char[]{0x7F,0x46}))) {
			
			return "Dim " + parse(content.substring(2));
			
		}
		
		//List
		if (content.startsWith(new String(new char[]{0x7F,0x51}))) {
			System.out.println("Parsing a list");
			//Before parsing, we must check if the entire instruction is a List instruction
			Integer[] check = parseBrackets(content);
			
			if (check.length == 2 && check[1] == content.length()-1 || option == WHOLE_INSTRUCTION && check.length > 0) {
				if (check.length == 2 && check[1] == content.length()-1) {
					content = content.substring(0, content.length()-1);
				}
				return supportAns("list[" + handleIntConversion(parse(content.substring(2, check[0]))) + "].data[" 
						+ handleIntConversion(parse(content.substring(check[0]+1, content.length()))) + "]", option);
			}
		}

		//searches for an operator
		String[] operators = {
				new String(new char[]{0x7F, 0xB0}), //And
				new String(new char[]{0x7F, 0xB1}), //Or
				new String(new char[]{0x7F, 0xB4}), //Xor
				new String(new char[]{0x10}), //<=
				new String(new char[]{0x11}), //!=
				new String(new char[]{0x12}), //>=
				"=",
				"<",
				">",
				new String(new char[]{0xA8}), // ^
				new String(new char[]{0xA9}), // *
				new String(new char[]{0xB9}), // /
				new String(new char[]{0x89}), // +
				new String(new char[]{0x99}), // -
				new String(new char[]{0x7F, 0xB3}) //Not
		};
		
		for (int i = 0; i < operators.length; i++) {
			for (int j = 0; j < content.length(); j++) {
				if (content.startsWith(operators[i], j)) {
					//test if the operator is not within parentheses
					boolean isInParentheses = false;
					for (int k = 0; k < parenthesesPos.length; k+=2) {
						if (j >= parenthesesPos[k] && j <= parenthesesPos[k+1]) {
							isInParentheses = true;
						}
					}
					//If the operator is at the beginning of the string, it isn't a binary operator
					if (!isInParentheses && j != 0) {
						String str = "";
						
						switch(i) {
							case 0:
								str = "B2C_and(";
								break;
							case 1:
								str = "B2C_or(";
								break;
							case 2:
								str = "B2C_xor(";
								break;
							case 3:
								str = "B2C_lessOrEqualThan(";
								break;
							case 4:
								str = "B2C_notEqualTo(";
								break;
							case 5:
								str = "B2C_greaterOrEqualThan(";
								break;
							case 6:
								str = "B2C_equalTo(";
								break;
							case 7:
								str = "B2C_lessThan(";
								break;
							case 8:
								str = "B2C_greaterThan(";
								break;
							case 9: // ^
								str = "B2C_pow(";
								break;
							case 10: // *
								str = "B2C_mult(";
								break;
							case 11: // /
								str = "B2C_div(";
								break;
							case 12: // +
								str = "B2C_add(";
								break;
							case 13: // -
								str = "B2C_sub(";
								break;
						}

						str += "&buffer" + nbBuffers + ", ";
						nbBuffers++;
						if (i <= 2) {
							str += parse(content.substring(0, j), CONV_TO_BCD) + ", " + parse(content.substring(j+2), CONV_TO_BCD) + ")";
						} else if (i < 14){
							str += parse(content.substring(0, j), CONV_TO_BCD) + ", " + parse(content.substring(j+1), CONV_TO_BCD) + ")";
						} else {
							str += "B2C_not(" + parse(content.substring(2)) + ")";
						} if (option == WHOLE_INSTRUCTION) {
							str += ";";
						}
						return str;
					}
					
				}
				if (isMultibytePrefix(content.charAt(j))) {
					j++;
				}
			}
		}
		
		//this only occurs if the entire string is within parentheses, such as "(2+3)"
		if (parenthesesPos.length == 2 && parenthesesPos[0] == 0 && parenthesesPos[1] == content.length()-1) {
			return "(" + parse(content.substring(1, content.length()-1)) + ")";
		}
		
		//replace variables with their position in the var[] array; only do this if the string only contains the variable
		if (content.length() == 1 && !content.matches("^\\d")) {
			String result = "";
			if (content.charAt(0) >= 'A' && content.charAt(0) <= 'Z') {
				result += "var[" + (int)(content.charAt(0)-65) + "]";
			}
			if (content.charAt(0) == (char)0xC0) {
				result += "Ans";
			}
			if (content.charAt(0) == (char)0xCD) { //r
				result += "var[26]";
			}
			if (content.charAt(0) == (char)0xCE) { //theta
				result += "var[27]";
			}
			
			return supportAns(result, option);
		}
		
		//Test if it is a number (note that it can be something like 2X, implicit multiplication)
		if (content.matches("^[\\d\\x99\\.](.+)?")) {
			int testForImplicitMultiplication = -1;
			for (char i = '0'; i <= '9'; i++) {
				if (testForImplicitMultiplication < content.lastIndexOf(i)) {
					testForImplicitMultiplication = content.lastIndexOf(i);
				}
			}
			if (testForImplicitMultiplication != content.length()-1) {
				return supportAns("B2C_mult(" + parse(content.substring(0, testForImplicitMultiplication+1), CONV_TO_BCD) +
						", " + parse(content.substring(testForImplicitMultiplication+1, content.length())) + ")", option);
			}
			//At this point it is a number, add it to constants
			Constants.add(content);
			String result = "";
			if (option == WHOLE_INSTRUCTION || option == CONV_TO_BCD) {
				/*if (content.matches("\\d")) {
					result += consts[Integer.valueOf(content)];
				} else if (content.equals("10")) {
					result += consts[10];
				} else {
					result = "B2C_convToBCD(\"" + content + "\")";
				}*/
				result += "&"+Constants.getVarNotation(content);
			} else {
				result = content;
			}
			if (option == WHOLE_INSTRUCTION) {
				return supportAns(result, option);
			}
			return result;
		}
		
		//At this point it is a calculation, check for calculations functions
		String result = "";
		if (content.matches("\\d+")) {
			//Parse numbers as a global variable (for example, 36 is replaced by a const BCDvar _36 which value is calculated at the beginning).
			if (!Constants.consts.contains(Integer.parseInt(content))) {
				Constants.add(content);
			}
			result += "&" + Constants.getVarNotation(content);
			return result;
			//Check if it is a lone variable
		} else if (content.matches("[A-Z\\xCD\\xCE\\xC0]")) {
			result += handleAlphaVar(content, option);
			return result;
		} /*else {
			result += "B2C_calcExp((unsigned char*)" + parseStr("\"" + content + "\"") + ")";
			return result;
		}*/
		//return supportAns(result, option);
		
		//At this point in the code, the method must have already returned
		//if it has detected at least one instruction it understands
		
		error("function in the instruction above not recognized!");
		return "";
	}
	
	/* This function is called to parse hardcoded lists (written like {1,2,3}).
	 * At the moment the only functions calling this method are:
	 * - Assignment operation on Dim Mat ({1,2}->Dim Mat M)
	 * - Assignment operation on List ({1,2}->List 3)
	 * - Multi/Super drawstat (Graph(X,Y)=({1,2},{3,4});
	 */
	
	public static String parseList(String content) {
		String result = "B2C_newList(";
		
		//Check if the list is hardcoded or not
		if (content.startsWith(new String(new char[]{0x7F, 0x51}))) {
			return parse(content);
		}
		if (content.charAt(0) != '{') {
			error("Trying to parse hardcoded list but the list doesn't begin with a '{'!");
			return "";
		}
		
		
		if (content.charAt(content.length()-1) == '}') {
			content = content.substring(0, content.length()-1);
		}
		
		System.out.println("Parsing a hardcoded list: "+content);
		//remove the leading '{' for easier argument parsing
		content = content.substring(1);
		
		ArrayList<Integer> args = new ArrayList<Integer>();
		args.addAll(Arrays.asList(parseArgs(content.substring(0))));
		args.add(content.length());
		
		System.out.println(args.toString());
		result += args.size() + ", ";
		result += parse(content.substring(0, args.get(0)), CONV_TO_BCD) + ", ";
		for (int i = 0; i < args.size()-1; i++) {
			result += parse(content.substring(args.get(i)+1, args.get(i+1)), CONV_TO_BCD) + ", ";
		}
		
		//remove the last comma
		return result.substring(0, result.length()-2) + ")";
	}
	
	/* This method checks for the presence of the string match in the string content.
	 * This can't be done with traditional methods because it must checks if the match
	 * string is in the content string AND not in a string.
	 * 
	 * Returns -1 if the value doesn't exist, or the beginning of the first occurence of the match.
	 * 
	 * Examples:
	 * 
	 * checkMatch("Locate 1,1,\"=> First option\"", "=>") will return -1,
	 * because there is a '=>' but inside a string.
	 * 
	 * checkMatch("A>B => Locate 1,1,C", "=>") will return 4.
	 * 
	 */
	
	public static int checkMatch(String content, String match) {
				
		boolean positionIsString = false;
		for (int i = 0; i < content.length(); i++) {
			if (content.charAt(i) == (char)0x7F ||
					content.charAt(i) == (char)0xF7 ||
					content.charAt(i) == (char)0xE5 ||
					content.charAt(i) == (char)0xE6 ||
					content.charAt(i) == (char)0xE7) {
				i += 2;
				if (i >= content.length()) {
					break;
				}
			}
			if (content.substring(i).startsWith(match) && !positionIsString) {
				return i;
			}
			if (content.charAt(i) == '"') {
				positionIsString = !positionIsString;
			} else if (content.charAt(i) == '\\') {
				i++;
			}
			
		}
		
		
		return -1;
	}
	
	public static String parseProgName(String content) {
		//The use of replaceAll is possible because you can't have multi byte characters in program names
		String[] specialChars = {
				"\\{", "\\}",
				"\\[", "\\]",
				"\\.",
				"\"",
				" ",
				"\\xA9|\\*",
				"\\xB9|\\/",
				"\\x99|-",
				"\\x89|\\+",
				"\\xCD",
				"\\xCE",
				"'",
				"~",
				"\0"
		};
		String[] replacements = {
				"lcurlybracket",
				"rcurlybracket",
				"lbracket",
				"rbracket",
				"dot",
				"quote",
				"space",
				"mult",
				"div",
				"sub",
				"add",
				"radius",
				"theta",
				"apos",
				"tilde",
				""
		};
		for (int i = 0; i < specialChars.length; i++) {
			content = content.replaceAll(specialChars[i], replacements[i]);
		}
		content = content.replaceAll("[^ -~]", "_"); //replace non-ASCII characters
		return content;
	}
	
	/* This method parses a string. It is designed to parse things like:
	 * Str 1 + "test" + Str 2
	 * 
	 * The main parse() method only calls this method in case of an argument that is always a string,
	 * for the functions Locate, Text and standalone strings.
	 * 
	 */
	
	public static String parseStr(String content) {
		System.out.println("Parsing string: "+content);
		if (content.startsWith("\"")/* || content.startsWith(new String(new char[]{0xF9, 0x3F}))*/) {
			if (content.startsWith("\"") && content.charAt(content.length()-1) != '"') {
				content += '"';
			}
			content = replaceNonAscii(content);
			return content; //TODO parse Str function
		}
		return "B2C_convToStr(" + handleAlphaVar(content, NO_OPTION) + ")";
	}

	//Replace non-ASCII characters with \xXX, using string concatenation.
	public static String replaceNonAscii(String content) {
		for (int i = 0; i < content.length(); i++) {
			if (content.charAt(i) < ' ' || content.charAt(i) > '~') {
				String result = content.substring(0, i);
				result += "\\x";
				String str = Integer.toHexString(content.charAt(i));
				if (str.length() > 2) {
					error("Unicode character u" + str + "!");
				} else {
					result += str;
				}
				
				//if (i < content.length()-1) 
					result += "\"\"";
				result += content.substring(i+1);
				content = result;
				
			}
		}
		return content;
	}
	
	/* This method parses comma-separated arguments. It is used to parse any function
	 * with those kind of arguments. It is needed because the arguments themselves may have
	 * commas (example: Locate 1,Mat M[A,B],Str 1).
	 * 
	 * Note that it doesn't return the arguments themselves, but the position of the commas, that
	 * must be exploited by the parser.
	 * 
	 */
	public static Integer[] parseArgs(String content) {
		System.out.println("Parsing arguments: "+content);
		ArrayList<Integer> args = new ArrayList<Integer>();
		int argsBuffer = 0;
		boolean positionIsString = false;
		for (int i = 0; i < content.length(); i++) {
			
			if (content.charAt(i) == ',' && argsBuffer == 0 && !positionIsString) {
				args.add(i);
			}
			if ((content.charAt(i) == '(' || content.charAt(i) == '[' || content.charAt(i) == '{') && !positionIsString) {
				argsBuffer++;
			} else if ((content.charAt(i) == ')' || content.charAt(i) == ']' || content.charAt(i) == '}') && !positionIsString) {
				argsBuffer--;
			} else if (content.charAt(i) == '"') {
				positionIsString = !positionIsString;
			} else if (content.charAt(i) == '\\') {
				i++;
			}
			if (isMultibytePrefix(content.charAt(i))) {
				i++;
			}
			
		}
		//System.out.println("Result:"+args.toString());
		return args.toArray(new Integer[args.size()]);
	}
	
	/**
	 * This function returns the index of each first-level opening and closing brackets/parentheses.
	 * Example: the string "3*(4*(5+6))+(4*5)" will return {2, 10, 12, 16}.
	 * It accounts for unmatched brackets at the end, so "2->Mat M[1,3" will return the same as "2->Mat M[1,3]".
	 */
	public static Integer[] parseBrackets(String content) {
		ArrayList<Integer> bracketsPos = new ArrayList<Integer>();
		int bracketsLevel = 0;
		boolean currentPositionIsString = false;
		for (int i = 0; i < content.length(); i++) {
			if ((content.charAt(i) == '(' || content.charAt(i) == '[' || content.charAt(i) == '{') && !currentPositionIsString) {
				bracketsLevel++;
				if (bracketsLevel == 1) {
					bracketsPos.add(i);
				}
			} else if ((content.charAt(i) == ')' || content.charAt(i) == ']' || content.charAt(i) == '}') && !currentPositionIsString) {
				bracketsLevel--;
				if (bracketsLevel == 0) {
					bracketsPos.add(i);
				} else if (bracketsLevel < 0) {
					error("brackets level below 0!");
				}
			} else if (content.charAt(i) == '"') {
				currentPositionIsString = !currentPositionIsString;
			} else if (content.charAt(i) == '\\') {
				i++;
			} else if (isMultibytePrefix(content.charAt(i))) {
				i++;
			}
		}
		//This should take care of unmatched brackets at the end
		while (bracketsLevel > 0) {
			bracketsPos.add(content.length());
			bracketsLevel--;
		}
		
		return bracketsPos.toArray(new Integer[bracketsPos.size()]);
	}
	
	/* This method replaces some basic functions (listed below).
	 * Reason for this method is to avoid replacing functions in strings, which can't be done with replaceAll().
	 * The replacements are the following:
	 * 
	 * 0x99 (-) and 0x87 (-) by '-'
	 * 0x89 (+) by '+'
	 * 0xA8 (^) by '^'
	 * 0xA9 (*) by '*'
	 * 0xB9 (/) by '/'
	 * 
	 * It it not used at the moment.
	 */
	/*public static StringBuilder autoreplace(StringBuilder result) {
		boolean positionIsString = false;
		for (int i = 0; i < result.length(); i++) {
			
			if (result.charAt(i) == (char)0x99) {
				result.setCharAt(i, '-');
			}
			if (result.charAt(i) == (char)0x87 && !positionIsString) {
				result.setCharAt(i, '-');
			}
			if (result.charAt(i) == (char)0x89) {
				result.setCharAt(i, '+');
			}
			if (result.charAt(i) == (char)0xA8) {
				result.setCharAt(i, '^');
			}
			if (result.charAt(i) == (char)0xA9) {
				result.setCharAt(i, '*');
			}
			if (result.charAt(i) == (char)0xB9) {
				result.setCharAt(i, '/');
			}
			if (result.charAt(i) == '"') {
				positionIsString = !positionIsString;
			} else if (result.charAt(i) == '\\') {
				i++;
			}
			if (isMultibytePrefix(result.charAt(i))) {
				i++;
			}
			
		}
		return result;
		
		
	}*/
	
	public static boolean isMultibytePrefix(char prefix) {
		if (prefix == (char)0xF7 ||
				prefix == (char)0x7F ||
				prefix == (char)0xE5 ||
				prefix == (char)0xE6 ||
				prefix == (char)0xE7)
			return true;
		return false;
	}
	
	public static String supportAns(String content, int option) {
		if (option == WHOLE_INSTRUCTION) 
			return "B2C_setAlphaVar('\\xC0', " + content + ");";
		return content;
	}
	
	public static String handleIntConversion(String content) {
		//TODO: optimise some cases like "A+2" where you could convert A then add 2
		//instead of calculating "A+2" in BCD
		if (content.matches("\\d+")) {
			return content;
		}
		return "B2C_convToUInt(" + parse(content) + ")";
	}
	
	public static String handleAlphaVar(String content, int option) {
		if (content.matches("[A-Z\\xCD\\xCE\\xC0]")) {
			String result = "B2C_getAlphaVar('";
			if (content.charAt(0) >= 'A' && content.charAt(0) <= 'Z') {
				result += content;
			} else {
				result += "\\x" + Integer.toHexString(content.charAt(0));
			}
			result += "')";
			return supportAns(result, option);
		}
		return parse(content, option);
	}
	
	public static void incrementTabs() {
		tabs += "\t";
	}
	public static void decrementTabs() {
		tabs = tabs.substring(0, tabs.length()-1);
	}
	
	public static void error(String error) {
		System.out.println("\n===ERROR: "+error+"===\n");
		System.exit(0);
	}
}

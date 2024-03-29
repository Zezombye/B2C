package b2c;

import java.util.ArrayList;

public class G1MParser {
	
	
	public static String convert(String content) {
		if (content.isEmpty()) return "";
		
		String currentProgram = Parser.parseProgName(content.substring(0, 8)); //content.substring(60,68);
		System.out.println("Program name: " + currentProgram);
		Header.addPrototype("void prog_"+currentProgram+"();\n");
		//System.out.println("Instructions : " + content.substring(86));
		content = content.substring(26);
		int lastCarriageReturn = -1;
		Parser.instructions.clear();
		Parser.instructionNumber = 0;
		
		
		//Divides the instructions
		for (int i = 0; i < content.length(); i++) {
			if (content.charAt(i) == '\r') {
				Parser.instructions.add(content.substring(lastCarriageReturn+1, i));
				lastCarriageReturn = i;
			}
			if (Parser.isMultibytePrefix(content.charAt(i))) {
				i++;
			}
			
		}
		Parser.instructions.add(content.substring(lastCarriageReturn+1));
		StringBuilder result = new StringBuilder();
		
		/* Due to the functions returning BCDvar* and not BCDvar,
		 * there must be buffers in which the functions return.
		 * To avoid creating a buffer at each function call,
		 * as many buffers are created as functions calls in an instruction.
		 */
		int[] buffers = {0,0,0,0};
		do {
			for (int i = 0; i < buffers.length; i++) {
				Parser.buffers[0] = 0;
			}
			result.append(Parser.tabs + Parser.parse(Parser.instructions.get(Parser.instructionNumber), Parser.WHOLE_INSTRUCTION) + "\n");
			Parser.instructionNumber++;
			for (int i = 0; i < buffers.length; i++) {
				if (buffers[i] < Parser.buffers[i]) {
					buffers[i] = Parser.buffers[i];
				}
			}
		} while (Parser.instructionNumber < Parser.instructions.size());
		
		//Add buffers
		for (int h = 0; h < buffers.length; h++) {
			String bufferStr = "\t" + Parser.bufferTypes[h] + " ";
			for (int i = 0; i < buffers[h]; i++) {
				bufferStr += Parser.bufferVars[h] + i;
				if (i+1 < buffers[h]) bufferStr += ", ";
			}
			bufferStr += ";\n";
			if (buffers[h] > 0) result.insert(0, bufferStr);
		}
		result.insert(0, "void prog_"+currentProgram+"() {\n");
		//result = Parser.autoreplace(result);
		result.append("}");
		//System.out.println("nb buffers = " + nbBuffers);
		//System.out.println(Parser.nbBuffers);
		return result.toString();
		
	}
}

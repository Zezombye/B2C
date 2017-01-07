package b2c;

import java.util.ArrayList;

public class Constants {

	static ArrayList<String> consts = new ArrayList<String>();
	
	/**
	 * This method is to optimise the speed of B2C by pre-calculating constants.
	 * Only send to this method integer and double constants. For now, it won't calculate
	 * special things like 1e5, 1/3, etc.
	 */
	public static void add(String constant) {
		constant = constant.replaceAll("\\x99|\\x87", "-");
		//System.out.println(constant);
		//Interpret "." as "0.", example: -.5 = -0.5
		constant = constant.replaceAll("(?<!\\d)\\.", "0.");
		
		//Remove last ".", example: 85. = 85.0 = 85
		if (constant.charAt(constant.length()-1) == '.') {
			constant = constant.substring(0, constant.length()-1);
		}
		
		//Parse special operators
		//sqrt
		if (constant.startsWith(new String(new char[]{0x86}))) {
			String sqrt = String.valueOf(Math.sqrt(Double.valueOf(constant.substring(1))));
			if (sqrt.length() > 15) {
				sqrt = sqrt.substring(0, 15);
			}
			add(sqrt);
		}
		
		if (!isNumber(constant)) {
			Parser.error("Constant " + Parser.printNonAscii(constant) + " is not a number!");
		}
		if (consts.contains(constant)) {
			return;
		}
		consts.add(constant);
		//Calculate the bytes of the constant
		//Note: can't convert to double due to precision loss
		//To fix this, we remove the decimal point to convert to integer and keep the exponent
		
		int exponent = 100; //exponent is 100-indexed in casio's system
		
		if (constant.indexOf(".") >= 0) {
			//Remove trailing zeroes that have, in this case, no significance
			constant = constant.replaceAll("0+$", "");
			
			//The exponent is modified by the number of significant digits after the decimal part
			exponent -= constant.length() - (constant.indexOf(".")+1);
		}
		
		//Convert integer to scientific notation using StackOverflow magic
		//First number apparently means the number of significant digits (including exponent)
		//Second number is the number of digits after the decimal point (not including exponent)
		//System.out.println(constant);
		
		String sciNotation = String.format("%18.14e", Double.valueOf(constant.replaceAll("\\.", ""))).replace(",", ".");
		
		
		String mantissa = sciNotation.substring(0, sciNotation.indexOf("e")).replaceAll("\\.", "");
		//System.out.println(mantissa);
		exponent += Integer.valueOf(sciNotation.substring(sciNotation.indexOf('e')+1));
		
		//Can't use Integer.valueOf(mantissa) because it might be over 2^32
		if (mantissa.startsWith("-")) {
			exponent += 500;
			mantissa = mantissa.substring(1);
		}
		
		String bcdNotation = (exponent < 100 ? "0" : "") + String.valueOf(exponent) + mantissa;
		if (bcdNotation.contains(".") || bcdNotation.contains("-") || bcdNotation.length() != 18) {
			Parser.error("Error in BCD conversion of " + constant + " which gave " + bcdNotation);
		}
		//System.out.println("Result= "+bcdNotation);
		//Replace groups of 2 digits by "0x##, " and remove the last comma+space
		//System.out.println(bcdNotation);
		
		//Handle the special case of 0
		if (Double.valueOf(constant) == 0) {
			bcdNotation = "000000000000000000"; //18 0s
		}
		bcdNotation = bcdNotation.replaceAll("(.{2})", "0x$1, ").replaceAll(", $", "");
		//System.out.println(bcdNotation);
		Header.addGlobal("const BCDvar " + Constants.getVarNotation(constant) + " = {" + bcdNotation + "};\n");
	}
	
	public static String getVarNotation(String nb) {
		String result;
		//System.out.println(nb);
		result = nb.replaceAll("(?<!\\d)\\.", "0.");
		//System.out.println(result);
		result = result.replaceAll("\\.|\\-|\\x99|\\x87", "_");
		//System.out.println(result);
		//Remove last "."
		if (result.charAt(result.length()-1) == '.') {
			result = result.substring(0, result.length()-1);
		}
		return "_" + result + "_";
	}
	
	public static boolean isNumber(String nb) {
		return nb.matches("[\\d\\x99\\x87\\.\\-]+");
	}
	
}

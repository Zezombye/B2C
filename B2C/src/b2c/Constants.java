package b2c;

import java.util.ArrayList;

public class Constants {

	static ArrayList<Double> consts = new ArrayList<Double>();
	
	/**
	 * This method is to optimise the speed of B2C by pre-calculating constants.
	 * Only send to this method integer and double constants. For now, it won't calculate
	 * special things like 1e5, 1/3, etc.
	 */
	public static void add(String constant) {
		constant = constant.replaceAll("\\x99", "-");
		//System.out.println(constant);
		if (constant.startsWith(".")) {
			constant = "0" + constant;
		}
		if (consts.contains(Double.valueOf(constant))) {
			return;
		}
		consts.add(Double.valueOf(constant));
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
		bcdNotation = bcdNotation.replaceAll("(.{2})", "0x$1, ").replaceAll(", $", "");
		//System.out.println(bcdNotation);
		Header.addGlobal("const BCDvar " + Constants.getVarNotation(constant) + " = {" + bcdNotation + "};\n");
	}
	
	public static String getVarNotation(String nb) {
		return "_"+nb.replaceAll("\\.|\\-|\\x99", "_")+"_";
	}
	
}

package b2c;

import java.util.ArrayList;

public class Operators {
	
	public static int maxPrecedence = 0;
	//Not sure if the precedence of xor/or is equal or not
	//Pretty sure about all other precedences; feel free to test
	//All seem to be left-to-right, even equality operators
	public static Operator[][] operators = {{
		
			new Operator(new String(new char[]{0xA8}), "B2C_pow", true), 
			new Operator(new String(new char[]{0x86}), "B2C_nthRoot", true), 
	},{		
			new Operator(new String(new char[]{0x99}), "B2C_neg", true, 1),
			new Operator(new String(new char[]{0x87}), "B2C_neg", true, 1),
			//new Operator(new String(new char[]{0x89}), "B2C_unaryPlus", false, 1), //unary plus = nothing
			
	},{
			new Operator(new String(new char[]{0xA9}), "B2C_mult", true), 
			new Operator(new String(new char[]{0xB9}), "B2C_div", true), 
	},{		
			new Operator(new String(new char[]{0x89}), "B2C_add", true), 
			new Operator(new String(new char[]{0x99}), "B2C_sub", true), 
	},{		
			new Operator(new String(new char[]{0x86}), "B2C_sqrt", true, 1),
	},{
			new Operator(new String(new char[]{0x10}), "B2C_lessOrEqualThan", false), 
			new Operator(new String(new char[]{0x12}), "B2C_greaterOrEqualThan", false), 
			new Operator("<", "B2C_lessThan", false), 
			new Operator(">", "B2C_greaterThan", false),
			new Operator("=", "B2C_equalTo", false), 
			new Operator(new String(new char[]{0x11}), "B2C_notEqualTo", false),
	},{		
			new Operator(new String(new char[]{0x7F, 0xB0}), "B2C_and", false), 
	},{		
			new Operator(new String(new char[]{0x7F, 0xB1}), "B2C_or", false), 
			new Operator(new String(new char[]{0x7F, 0xB4}), "B2C_xor", false), 
	}};
	
	public static void initOperators() {
		maxPrecedence = operators.length-1;
	}
	
}

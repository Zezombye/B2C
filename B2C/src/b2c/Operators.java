package b2c;

import java.util.ArrayList;

public class Operators {
	
	public static int maxPrecedence = 6;
	//Not sure if the precedence of xor/or is equal or not
	//Pretty sure about all other precedences; feel free to test
	//All seem to be left-to-right
	public static Operator[][] operators = {{

			new Operator(1, new String(new char[]{0x7F, 0xB3}), "B2C_not", 0),
	},{
			new Operator(2, new String(new char[]{0xA8}), "B2C_pow", 1), 
			new Operator(2, new String(new char[]{0x86}), "B2C_sqrt", 1), 
	},{		
			new Operator(2, new String(new char[]{0xA9}), "B2C_mult", 2), 
			new Operator(2, new String(new char[]{0xB9}), "B2C_div", 2), 
	},{		
			new Operator(2, new String(new char[]{0x89}), "B2C_add", 3), 
			new Operator(2, new String(new char[]{0x99}), "B2C_sub", 3), 
	},{		
			new Operator(2, new String(new char[]{0x10}), "B2C_lessOrEqualThan", 4), 
			new Operator(2, new String(new char[]{0x12}), "B2C_greaterOrEqualThan", 4), 
			new Operator(2, "<", "B2C_lessThan", 4), 
			new Operator(2, ">", "B2C_greaterThan", 4), 
			new Operator(2, "=", "B2C_equalTo", 4), 
			new Operator(2, new String(new char[]{0x11}), "B2C_notEqualTo", 4), 
	},{		
			new Operator(2, new String(new char[]{0x7F, 0xB0}), "B2C_and", 5), 
	},{		
			new Operator(2, new String(new char[]{0x7F, 0xB1}), "B2C_or", 6), 
			new Operator(2, new String(new char[]{0x7F, 0xB4}), "B2C_xor", 6), 
	}};
	
}

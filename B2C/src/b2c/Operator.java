package b2c;

public class Operator {

	private String casioChar, asciiFunction;
	private int nbOperands;
	private boolean isMathematicalFunction;
	
	public boolean isMathematicalFunction() {
		return this.isMathematicalFunction;
	}

	public void setMathematicalFunction(boolean isMathematicalFunction) {
		this.isMathematicalFunction = isMathematicalFunction;
	}

	public int getNbOperands() {
		return nbOperands;
	}

	public void setNbOperands(int nbOperands) {
		this.nbOperands = nbOperands;
	}

	public Operator(String casioChar, String asciiFunction, boolean isMathematicalFunction) {
		this.casioChar = casioChar;
		this.asciiFunction = asciiFunction;
		this.isMathematicalFunction = isMathematicalFunction;
	}
	
	public Operator(String casioChar, String asciiFunction, boolean isMathematicalFunction, int nbOperands) {
		this.casioChar = casioChar;
		this.asciiFunction = asciiFunction;
		this.nbOperands = nbOperands;
		this.isMathematicalFunction = isMathematicalFunction;
	}
	
	public String getCasioChar() {
		return casioChar;
	}

	public void setCasioChar(String casioChar) {
		this.casioChar = casioChar;
	}

	public String getAsciiFunction() {
		return asciiFunction;
	}

	public void setAsciiFunction(String asciiFunction) {
		this.asciiFunction = asciiFunction;
	}
}

package b2c;

public class Operator {

	private String casioChar, asciiFunction;
	private int precedence, nbOperands;
	
	public Operator(int nbOperands, String casioChar, String asciiFunction, int precedence) {
		this.nbOperands = nbOperands;
		this.casioChar = casioChar;
		this.asciiFunction = asciiFunction;
		this.precedence = precedence;
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

	public int getPrecedence() {
		return precedence;
	}

	public void setPrecedence(int precedence) {
		this.precedence = precedence;
	}

	public int getNbOperands() {
		return nbOperands;
	}

	public void setNbOperands(int nbOperands) {
		this.nbOperands = nbOperands;
	}
}

/* ==========================================================================
 * GNU GENERAL PUBLIC LICENSE
 * Version 2, June 1991
 * 
 * Copyright (C) 1989, 1991 Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 * 
 * $LastChangeDate$
 * $Rev$
 * $LastChangedBy$
 * $URL$
 * $Id$
 * ========================================================================== */ 
package com.arykow.applications.ugabe.server;

import java.util.ArrayList;

public class RDParser {
	private char[] input;
	private int parsingPosition;
	private boolean[] isLeftAssociative;
	private boolean[] isBinary;
	private boolean[] isOperator;
	private int[] presedence;
	protected boolean parseError;

	public class flup {
		protected String s;
		protected int i;

		public flup(String str, int val) {
			this.s = str;
			this.i = val;
		}
	}

	ArrayList<flup> vars;

	public RDParser() {
		isLeftAssociative = new boolean[256];
		isBinary = new boolean[256];
		isOperator = new boolean[256];
		presedence = new int[256];
		isOperator['+'] = true;
		isOperator['-'] = true;
		isOperator['*'] = true;
		isOperator['/'] = true;
		isOperator['^'] = true;
		isOperator['&'] = true;
		isOperator['|'] = true;
		isOperator['~'] = true;
		isLeftAssociative['+'] = true;
		isLeftAssociative['-'] = true;
		isLeftAssociative['*'] = true;
		isLeftAssociative['/'] = true;
		isLeftAssociative['^'] = false;
		isLeftAssociative['&'] = true;
		isLeftAssociative['|'] = true;
		isLeftAssociative['~'] = false;
		isBinary['+'] = true;
		isBinary['-'] = true;
		isBinary['*'] = true;
		isBinary['/'] = true;
		isBinary['^'] = true;
		isBinary['&'] = true;
		isBinary['|'] = true;
		isBinary['~'] = false;
		presedence['|'] = 0;
		presedence['&'] = 1;
		presedence['+'] = 2;
		presedence['-'] = 2;
		presedence['~'] = 2;
		presedence['*'] = 3;
		presedence['/'] = 3;
		presedence['^'] = 4;
		vars = new ArrayList<flup>();
	}

	private int Next() {
		if (parsingPosition < input.length)
			return input[parsingPosition];
		else
			return -1;
	}

	private boolean Expect(int e) {
		if (Next() == e) {
			Consume();
			return true;
		} else {
			System.out.println("Error: Expected '" + ((char) e) + "'");
			parseError = true;
			return false;
		}
	}

	private void Consume() {
		if (parsingPosition < input.length)
			++parsingPosition;
	}

	public void addVariable(String str, int value) {
		vars.add(new flup(str, value));
	}

	public void removeVariables() {
		vars = new ArrayList<flup>();
	}

	private boolean inBase(int Base, char c) {
		switch (Base) {
		case 0:
			if ((c >= 'A') && (c <= 'Z'))
				return true;
			if ((c >= 'a') && (c <= 'z'))
				return true;
			return false;
		case 16:
			if ((c >= 'A') && (c <= 'F'))
				return true;
			if ((c >= 'a') && (c <= 'f'))
				return true;
			if ((c >= '0') && (c <= '9'))
				return true;
			return false;
		case 10:
		default:
			if ((c >= '0') && (c <= '9'))
				return true;
			return false;
		}
	}

	public int StrToInt(String in) {
		String s = in.trim();
		try {
			int i = s.indexOf("$");
			if (i > -1) {
				return Integer.parseInt(s.substring(i + 1), 16);
			} else {
				return Integer.parseInt(s, 10);
			}
		} catch (NumberFormatException ee) {
			System.out.println(ee.getMessage() + " is not a valid format for an integer.");
		}
		return -1;
	}

	private int symNumber(String ident, int Base) {
		int i = 0;
		String s = "";

		switch (Base) {
		case 16:
			++i;
			while ((i + parsingPosition < input.length) && inBase(16, input[parsingPosition + i])) {
				s += input[parsingPosition + i];
				++i;
			}
			parsingPosition += i;
			return StrToInt("$" + s);
		case 10:
		default:
			while ((i + parsingPosition < input.length) && inBase(10, input[parsingPosition + i])) {
				s += input[parsingPosition + i];
				++i;
			}
			parsingPosition += i;
			return StrToInt(s);
		}
	}

	private flup checkVariable() {
		int i = 0;
		flup f = null;
		String s = "";
		while ((i + parsingPosition < input.length) && inBase(0, input[parsingPosition + i])) {
			s += input[parsingPosition + i];
			++i;
		}
		for (int k = 0; k < vars.size(); ++k) {
			flup ff = (flup) vars.get(k);
			if (ff.s.equals(s))
				f = ff;
		}
		return f;
	}

	private int P(String ident) {
		flup f = checkVariable();

		if (isOperator[Next()] && (!isBinary[Next()])) {
			int op = Next();
			Consume();
			switch (op) {
			case '~':
				return -Expr(presedence[op], ident + "  ");
			default:
				System.out.println(ident + "Unknown unary operator '" + ((char) op) + "'");
				return -1;
			}
		} else if (Next() == '(') {
			Consume();

			int t = Expr(0, ident + "  ");

			if (Expect(')'))
				return t;
			else
				return -1;
		} else if ((Next() >= '0') && (Next() <= '9')) {
			int t = symNumber(ident + "  ", 10);
			return t;
		} else if (Next() == '$') {
			int t = symNumber(ident + "  ", 16);
			return t;
		} else if (f != null) {

			parsingPosition += f.s.length();
			return f.i;
		} else {
			System.out.println(ident + "Error: No case for P('" + ((char) Next()) + "')");
			parseError = true;
			return -1;
		}
	}

	private int Expr(int p, String ident) {

		int t1 = P(ident + "  ");

		while ((Next() != -1) && isBinary[Next()] && presedence[Next()] >= p) {
			int op = Next();

			Consume();
			int t2 = 0;
			if (isLeftAssociative[op])
				t2 = Expr(presedence[op] + 1, ident + "  ");
			else
				t2 = Expr(presedence[op], ident + "  ");

			switch (op) {
			case '+':

				t1 = t1 + t2;
				break;
			case '-':

				t1 = t1 - t2;
				break;
			case '*':

				t1 = t1 * t2;
				break;
			case '/':

				t1 = t1 / t2;
				break;
			case '^':

				t1 = (int) Math.round(Math.pow(t1, t2));
				break;
			case '&':

				t1 = t1 & t2;
				break;
			case '|':

				t1 = t1 | t2;
				break;
			default:
				System.out.println(ident + "Unknown binary operator '" + ((char) op) + "'");
				return -1;
			}
		}
		return t1;
	}

	public int Evaluate(String str) {
		if (str != null && str.length() > 0) {
			input = str.toCharArray();
			int i = 0;
			int j = 1;
			while ((j != str.length()) && (i != str.length())) {
				if (input[i] == ' ') {
					input[i] = input[j];
					input[j++] = ' ';
				} else {
					++i;
				}
			}

			parsingPosition = 0;
			parseError = false;
			int r = Expr(0, "");
			if (!parseError)
				return r;
			else
				return -1;
		} else
			return -1;
	}

	public static void main(String[] args) {
		RDParser parser = new RDParser();

		parser.addVariable("A", 1);
		parser.addVariable("B", 2);
		parser.addVariable("HL", 3);
		if (args.length > 0)
			System.out.println(parser.Evaluate(args[0]));
	}
}

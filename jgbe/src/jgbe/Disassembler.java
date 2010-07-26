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
package jgbe;

public class Disassembler {
	public static final int SIMPLE_DISASSEMBLY = 0;
	public static final int EXTENDED_DISASSEMBLY = 1;
	private int mode = 0;
	private CPU cpu;

	private static String[] opcode = { "NOP", "LD   BC, IMM16", "LD   [BC], A", "INC  BC", "INC  B", "DEC  B", "LD   B, IMM08", "RLCA", "LD   [IMM16], SP", "ADD  HL, BC", "LD   A, [BC]", "DEC  BC", "INC  C", "DEC  C", "LD   C, IMM08", "RRCA", "STOP", "LD   DE, IMM16", "LD   [DE], A", "INC  DE", "INC  D", "DEC  D", "LD   D, IMM08", "RLA", "JR   IMM08", "ADD  HL, DE", "LD   A, [DE]", "DEC  DE", "INC  E", "DEC  E", "LD   E, IMM08", "RRA", "JR   NZ, IMM08", "LD   HL, IMM16", "LDI  [HL], A", "INC  HL", "INC  H", "DEC  H", "LD   H, IMM08", "DAA", "JR   Z, IMM08", "ADD  HL, HL", "LDI  A, [HL]", "DEC  HL", "INC  L", "DEC  L", "LD   L, IMM08", "CPL", "JR   NC, IMM08", "LD   SP, IMM16", "LDD  [HL], A", "INC  SP", "INC  [HL]", "DEC  [HL]", "LD   [HL], IMM08", "SCF", "JR   C, IMM08", "ADD  HL, SP", "LDD  A, [HL]", "DEC  SP", "INC  A", "DEC  A", "LD   A, IMM08", "CCF", "LD   B, B", "LD   B, C", "LD   B, D", "LD   B, E", "LD   B, H", "LD   B, L", "LD   B, [HL]", "LD   B, A", "LD   C, B", "LD   C, C", "LD   C, D", "LD   C, E", "LD   C, H", "LD   C, L", "LD   C, [HL]", "LD   C, A", "LD   D, B", "LD   D, C", "LD   D, D", "LD   D, E", "LD   D, H", "LD   D, L", "LD   D, [HL]", "LD   D, A", "LD   E, B", "LD   E, C", "LD   E, D", "LD   E, E", "LD   E, H", "LD   E, L", "LD   E, [HL]", "LD   E, A", "LD   H, B", "LD   H, C", "LD   H, D", "LD   H, E", "LD   H, H", "LD   H, L", "LD   H, [HL]", "LD   H, A", "LD   L, B", "LD   L, C", "LD   L, D", "LD   L, E", "LD   L, H", "LD   L, L", "LD   L, [HL]", "LD   L, A", "LD   [HL], B", "LD   [HL], C", "LD   [HL], D", "LD   [HL], E", "LD   [HL], H", "LD   [HL], L", "HALT", "LD   [HL], A", "LD   A, B", "LD   A, C", "LD   A, D", "LD   A, E", "LD   A, H", "LD   A, L", "LD   A, [HL]", "LD   A, A", "ADD  A, B", "ADD  A, C", "ADD  A, D", "ADD  A, E", "ADD  A, H", "ADD  A, L", "ADD  A, [HL]", "ADD  A, A", "ADC  A, B", "ADC  A, C", "ADC  A, D", "ADC  A, E", "ADC  A, H", "ADC  A, L", "ADC  A, [HL]", "ADC  A, A", "SUB  A, B", "SUB  A, C", "SUB  A, D", "SUB  A, E", "SUB  A, H", "SUB  A, L", "SUB  A, [HL]", "SUB  A, A", "SBC  A, B", "SBC  A, C", "SBC  A, D", "SBC  A, E", "SBC  A, H", "SBC  A, L", "SBC  A, [HL]", "SBC  A, A", "AND  B", "AND  C", "AND  D", "AND  E", "AND  H", "AND  L", "AND  [HL]", "AND  A", "XOR  B", "XOR  C", "XOR  D", "XOR  E", "XOR  H", "XOR  L", "XOR  [HL]", "XOR  A", "OR   B", "OR   C", "OR   D", "OR   E", "OR   H", "OR   L", "OR   [HL]", "OR   A", "CP   B", "CP   C", "CP   D", "CP   E", "CP   H", "CP   L", "CP   [HL]", "CP   A", "RET  NZ", "POP  BC", "JP   NZ, IMM16", "JP   IMM16", "CALL NZ, IMM16", "PUSH BC", "ADD  A, IMM08", "RST  &0", "RET  Z", "RET", "JP   Z, IMM16", "**** CB ****", "CALL Z, IMM16", "CALL IMM16", "ADC  A, IMM08", "RST  &08", "RET  NC", "POP  DE", "JP   NC, IMM16", "**MISSING    INSTRUCTION**", "CALL NC, IMM16", "PUSH DE", "SUB  A, IMM08", "RST  &10", "RET  C", "RETI", "JP   C, IMM16", "**MISSING    INSTRUCTION**", "CALL C, IMM16", "**MISSING    INSTRUCTION**", "SBC  A, IMM08", "RST  &18", "LDH  [n], A", "POP  HL", "LDH  [C], A", "**MISSING    INSTRUCTION**", "**MISSING    INSTRUCTION**", "PUSH HL", "AND  IMM08", "RST  &20", "ADD  SP, dd", "JP   HL", "LD   [IMM16], A", "**MISSING    INSTRUCTION**", "**MISSING    INSTRUCTION**", "**MISSING    INSTRUCTION**", "XOR  IMM08", "RST  &28", "LDH  A, [n]", "POP  AF", "LDH  A, [C]", "DI", "**MISSING    INSTRUCTION**", "PUSH AF", "OR   IMM08", "RST  &30", "LD   HL, SP+dd", "LD   SP, HL", "LD   A, [IMM16]", "EI", "**MISSING    INSTRUCTION**", "**MISSING    INSTRUCTION**", "CP   IMM08", "RST  &38", "RLC  B", "RLC  C", "RLC  D", "RLC  E", "RLC  H", "RLC  L", "RLC  [HL]", "RLC  A", "RRC  B", "RRC  C", "RRC  D", "RRC  E", "RRC  H", "RRC  L", "RRC  [HL]", "RRC  A", "RL   B", "RL   C", "RL   D", "RL   E", "RL   H", "RL   L", "RL   [HL]", "RL   A", "RR   B", "RR   C", "RR   D", "RR   E", "RR   H", "RR   L", "RR   [HL]", "RR   A", "SLA  B", "SLA  C", "SLA  D", "SLA  E", "SLA  H", "SLA  L", "SLA  [HL]", "SLA  A", "SRA  B", "SRA  C", "SRA  D", "SRA  E", "SRA  H", "SRA  L", "SRA  [HL]", "SRA  A", "SWAP B", "SWAP C", "SWAP D", "SWAP E", "SWAP H", "SWAP L", "SWAP [HL]", "SWAP A", "SRL  B", "SRL  C", "SRL  D", "SRL  R", "SRL  H", "SRL  L", "SRL  [HL]", "SRL  A", "BIT  0, B", "BIT  0, C", "BIT  0, D", "BIT  0, E", "BIT  0, H", "BIT  0, L", "BIT  0, [HL]", "BIT  0, A", "BIT  1, B", "BIT  1, C", "BIT  1, D", "BIT  1, E", "BIT  1, H", "BIT  1, L", "BIT  1, [HL]", "BIT  1, A", "BIT  2, B", "BIT  2, C", "BIT  2, D", "BIT  2, E", "BIT  2, H", "BIT  2, L", "BIT  2, [HL]", "BIT  2, A", "BIT  3, B", "BIT  3, C", "BIT  3, D", "BIT  3, E", "BIT  3, H", "BIT  3, L", "BIT  3, [HL]", "BIT  3, A", "BIT  4, B", "BIT  4, C", "BIT  4, D", "BIT  4, E", "BIT  4, H", "BIT  4, L", "BIT  4, [HL]", "BIT  4, A", "BIT  5, B", "BIT  5, C", "BIT  5, D", "BIT  5, E", "BIT  5, H", "BIT  5, L", "BIT  5, [HL]", "BIT  5, A", "BIT  6, B", "BIT  6, C", "BIT  6, D", "BIT  6, E", "BIT  6, H", "BIT  6, L", "BIT  6, [HL]", "BIT  6, A", "BIT  7, B", "BIT  7, C", "BIT  7, D", "BIT  7, E", "BIT  7, H", "BIT  7, L", "BIT  7, [HL]", "BIT  7, A", "RES  0, B", "RES  0, C", "RES  0, D", "RES  0, E", "RES  0, H", "RES  0, L", "RES  0, [HL]", "RES  0, A", "RES  1, B", "RES  1, C", "RES  1, D", "RES  1, E", "RES  1, H", "RES  1, L", "RES  1, [HL]", "RES  1, A", "RES  2, B", "RES  2, C", "RES  2, D", "RES  2, E", "RES  2, H", "RES  2, L", "RES  2, [HL]", "RES  2, A", "RES  3, B", "RES  3, C", "RES  3, D", "RES  3, E", "RES  3, H", "RES  3, L", "RES  3, [HL]", "RES  3, A", "RES  4, B", "RES  4, C", "RES  4, D", "RES  4, E", "RES  4, H", "RES  4, L", "RES  4, [HL]", "RES  4, A", "RES  5, B", "RES  5, C", "RES  5, D", "RES  5, E", "RES  5, H", "RES  5, L", "RES  5, [HL]", "RES  5, A", "RES  6, B", "RES  6, C", "RES  6, D", "RES  6, E", "RES  6, H", "RES  6, L", "RES  6, [HL]", "RES  6, A", "RES  7, B", "RES  7, C", "RES  7, D", "RES  7, E", "RES  7, H", "RES  7, L", "RES  7, [HL]", "RES  7, A", "SET  0, B", "SET  0, C", "SET  0, D", "SET  0, E", "SET  0, H", "SET  0, L", "SET  0, [HL]", "SET  0, A", "SET  1, B", "SET  1, C", "SET  1, D", "SET  1, E", "SET  1, H", "SET  1, L", "SET  1, [HL]", "SET  1, A", "SET  2, B", "SET  2, C", "SET  2, D", "SET  2, E", "SET  2, H", "SET  2, L", "SET  2, [HL]", "SET  2, A", "SET  3, B", "SET  3, C", "SET  3, D", "SET  3, E", "SET  3, H", "SET  3, L", "SET  3, [HL]", "SET  3, A", "SET  4, B", "SET  4, C", "SET  4, D", "SET  4, E", "SET  4, H", "SET  4, L", "SET  4, [HL]", "SET  4, A", "SET  5, B", "SET  5, C", "SET  5, D", "SET  5, E", "SET  5, H", "SET  5, L", "SET  5, HL", "SET  5, A", "SET  6, B", "SET  6, C", "SET  6, D", "SET  6, E", "SET  6, H", "SET  6, L", "SET  6, [HL]", "SET  6, A", "SET  7, B", "SET  7, C", "SET  7, D", "SET  7, E", "SET  7, H", "SET  7, L", "SET  7, [HL]", "SET  7, A" };

	private static final char[] whitespace = { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' };

	public Disassembler(CPU cpu, int i) {
		this.cpu = cpu;
		this.mode = i;
	}

	private int regval(String reg) {
		if (reg.equalsIgnoreCase("A"))
			return cpu.A;
		if (reg.equalsIgnoreCase("B"))
			return cpu.B;
		if (reg.equalsIgnoreCase("C"))
			return cpu.C;
		if (reg.equalsIgnoreCase("D"))
			return cpu.D;
		if (reg.equalsIgnoreCase("E"))
			return cpu.E;
		if (reg.equalsIgnoreCase("H"))
			return cpu.H;
		if (reg.equalsIgnoreCase("L"))
			return cpu.L;
		if (reg.equalsIgnoreCase("AF"))
			return 0x10000 | ((cpu.A << 8) | cpu.F);
		if (reg.equalsIgnoreCase("BC"))
			return 0x10000 | ((cpu.B << 8) | cpu.C);
		if (reg.equalsIgnoreCase("DE"))
			return 0x10000 | ((cpu.D << 8) | cpu.E);
		if (reg.equalsIgnoreCase("HL"))
			return 0x10000 | ((cpu.H << 8) | cpu.L);
		if (reg.equalsIgnoreCase("SP"))
			return 0x10000 | (cpu.SP);
		if (reg.equalsIgnoreCase("PC"))
			return 0x10000 | (cpu.getPC());
		return -1;
	}

	public final int instructionLength(int PC) {
		try {
			int instr = cpu.read(PC);
			int i = -1;
			int bytecount = 1;
			String op = "";
			if (instr == 0xcb) {
				instr = cpu.read(PC + 1);
				op = opcode[instr + 0x100];
				bytecount = 2;
			} else {
				op = opcode[instr];
			}
			i = op.indexOf("IMM08");
			if (i > -1) {
				bytecount = 2;
			}
			i = op.indexOf("IMM16");
			if (i > -1) {
				bytecount = 3;
			}
			i = op.indexOf("[n]");
			if (i > -1) {
				bytecount = 2;
			}
			i = op.indexOf("dd");
			if (i > -1) {
				bytecount = 2;
			}
			return bytecount;
		} catch (Exception e) {
			return 1;
		}
	}

	public final String disassemble(int PC) {
		String result = null;
		switch (mode) {
		case 0:
			result = simple_disassemble(PC);
			break;
		case 1:
			result = extended_disassemble(PC);
			break;
		default:
			throw new Error("Assertion failed: " + "false");
		}
		return result;
	}

	private final String extended_disassemble(int PC) {
		try {
			int instr = cpu.read(PC);
			int immediate = -1;
			int i = -1;
			int j = -1;
			int bytecount = 1;
			String op = "";
			if (instr == 0xcb) {
				instr = cpu.read(PC + 1);
				op = opcode[instr + 0x100];
				bytecount = 2;
			} else {
				op = opcode[instr];
			}
			String s = op;
			i = op.indexOf("IMM08");
			if (i > -1) {
				immediate = cpu.read(PC + 1);
				if (op.indexOf("JR") > -1)
					immediate = (PC + 2) + (((immediate & 0x80) != 0) ? (-((immediate ^ 0xff) + 1)) : immediate);
				s = String.format(op.substring(0, i) + "$%02x" + op.substring(i + 5), immediate);
				bytecount = 2;
			}
			i = op.indexOf("IMM16");
			if (i > -1) {
				immediate = (cpu.read(PC + 2) << 8) | cpu.read(PC + 1);
				s = String.format(op.substring(0, i) + "$%04x" + op.substring(i + 5), immediate);
				bytecount = 3;
			}

			i = op.lastIndexOf(" ");
			if (i > -1) {
				j = op.lastIndexOf(",", i - 1);
				if (j > -1) {

					if (op.charAt(i + 1) == '[') {
						immediate = regval(op.substring(i + 2, op.indexOf("]", i + 2)));

						++i;
					} else {
						immediate = regval(op.substring(i + 1));
					}
					if (immediate > -1) {
						if (immediate > 0xffff) {
							s = String.format(s.substring(0, i + 1) + "$%04x" + s.substring(i + 3), immediate & 0xffff);
						} else {
							s = String.format(s.substring(0, i + 1) + "$%02x" + s.substring(i + 2), immediate);
						}
					}

					i = op.lastIndexOf(" ", j);
					if (op.charAt(i + 1) == '[') {
						immediate = regval(op.substring(i + 2, op.indexOf("]", i + 2)));
						++i;
					} else {
						immediate = -1;
					}
					if (immediate > -1) {
						if (immediate > 0xffff) {
							s = String.format(s.substring(0, i + 1) + "$%04x" + s.substring(i + 3), immediate & 0xffff);
						} else {
							s = String.format(s.substring(0, i + 1) + "$%02x" + s.substring(i + 2), immediate);
						}
					}
				} else {
					if (op.charAt(i + 1) == '[') {
						immediate = regval(op.substring(i + 2, op.indexOf("]", i + 2)));
						++i;
					} else {
						immediate = regval(op.substring(i + 1));
					}
					if (immediate > -1) {
						if (immediate > 0xffff) {
							s = String.format(s.substring(0, i + 1) + "$%04x" + s.substring(i + 3), immediate & 0xffff);
						} else {
							s = String.format(s.substring(0, i + 1) + "$%02x" + s.substring(i + 2), immediate);
						}
					}
				}
			}
			i = s.indexOf("[n]");
			if (i > -1) {
				immediate = cpu.read(PC + 1);
				bytecount = 2;
				if (op.indexOf("LDH") > -1)
					immediate |= 0xff00;
				s = String.format(s.substring(0, i + 1) + "$%04x" + s.substring(i + 2), immediate);

			}
			i = op.indexOf("dd");
			if (i > -1) {
				immediate = cpu.read(PC + 1);
				if (op.indexOf("(SP+dd)") > -1) {
					immediate ^= 0x80;
					immediate -= 0x80;
					immediate += cpu.SP;
				}
				bytecount = 2;
			}

			String prefix = String.format("$%04x ", PC);
			for (i = 0; i < bytecount; ++i) {
				prefix += String.format("$%02x ", cpu.read(PC + i));
			}
			for (i = 0; i < 3 - bytecount; ++i) {
				prefix += String.format("    ", cpu.read(PC + i));
			}
			return prefix + op + (new String(whitespace, 0, 18 - op.length())) + "// " + s;
		} catch (Exception e) {
			return "";
		}
	}

	private final String simple_disassemble(int PC) {
		try {
			int instr = cpu.read(PC);
			int immediate = -1;
			int bytecount = 1;
			int i;
			String op = "";
			if (instr == 0xcb) {
				instr = cpu.read(PC + 1);
				op = opcode[instr + 0x100];
				bytecount = 2;
			} else {
				op = opcode[instr];
			}
			String s = op;
			i = op.indexOf("IMM08");
			if (i > -1) {
				immediate = cpu.read(PC + 1);
				if (op.indexOf("JR") > -1)
					immediate = (PC + 2) + (((immediate & 0x80) != 0) ? (-((immediate ^ 0xff) + 1)) : immediate);
				s = String.format(op.substring(0, i) + "$%02x" + op.substring(i + 5), immediate);
				bytecount = 2;
			}
			i = op.indexOf("IMM16");
			if (i > -1) {
				immediate = (cpu.read(PC + 2) << 8) | cpu.read(PC + 1);
				s = String.format(op.substring(0, i) + "$%04x" + op.substring(i + 5), immediate);
				bytecount = 3;
			}
			i = s.indexOf("[n]");
			if (i > -1) {
				immediate = cpu.read(PC + 1);
				bytecount = 2;
				s = String.format(s.substring(0, i + 1) + "$%02x" + s.substring(i + 2), immediate);
			}
			i = op.indexOf("dd");
			if (i > -1) {
				immediate = cpu.read(PC + 1);
				bytecount = 2;
				s = String.format(s.substring(0, i) + "$%02x" + s.substring(i + 2), immediate);
			}

			String prefix = String.format("$%04x: ", PC);
			for (i = 0; i < bytecount; ++i) {
				prefix += String.format("$%02x ", cpu.read(PC + i));
			}
			for (; i < 3; ++i) {
				prefix += "    ";
			}
			return prefix + " " + s;
		} catch (Exception e) {
			return "";
		}
	}

	public static void main(String[] args) {
		CPU cpu = new CPU(new CPUServerImpl(), null);
		Disassembler disassembler = new Disassembler(cpu, SIMPLE_DISASSEMBLY);
		System.out.println(disassembler.disassemble(0));
		System.out.println(disassembler.disassemble(0));
		System.out.println(disassembler.disassemble(255));
		System.out.println(disassembler.disassemble(255));
	}
}

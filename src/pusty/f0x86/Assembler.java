package pusty.f0x86;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Main class for assembling code */
public class Assembler {

	/** Abstract Class for representing assembly statements */
	public abstract static class Node {

		/** The line this node represents */
		protected final String line;

		/** The line number this node is at */
		protected final int lineNr;

		/**
		 * Create an a Node with a given line and line number
		 * 
		 * @param line
		 *            the line this node represents
		 * @param lineNr
		 *            the line number this node is at
		 */
		public Node(String line, int lineNr) {
			this.line = line;
			this.lineNr = lineNr;
		}

		/**
		 * The data of this node in bytes
		 * 
		 * @return the content of this node
		 */
		public byte[] getData() {
			return new byte[] {};
		}

		/**
		 * Return the line this node represents
		 * 
		 * @return the line of this node
		 */
		public String getLine() {
			return line;
		}

		/**
		 * Call this when parsing this node failed
		 * 
		 * @param reason
		 *            the reason parsing failed
		 */
		public void throwError(String reason) {
			System.err.println("Error@Line " + lineNr + " (" + line + ") [" + reason + "]");
		}

		@Override
		public String toString() {
			return "Generic Node '" + line + "'";
		}

		/**
		 * Return the size of the data of this node
		 * 
		 * @return the size of the content of this node
		 */
		public int getSize() {
			return getData().length;
		}
	}

	/** A Node for representing "Labels" aka named offsets/addresses */
	public static class NodeLabel extends Node {

		/**
		 * Create an a NodeLabel with a given line and line number
		 * 
		 * @param line
		 *            the line this node represents
		 * @param lineNr
		 *            the line number this node is at
		 */
		public NodeLabel(String line, int lineNr) {
			super(line, lineNr);
		}

		/**
		 * Return the label this node represents
		 * 
		 * @return the label of this node
		 */
		public String getLabel() {
			return line;
		}

		/**
		 * Return whether this label is fixed (an address) or not fixed (an offset)
		 * 
		 * @return whether this node is fixed
		 */
		public boolean isFixed() {
			return false;
		}

		/**
		 * Return the address of this node if it's fixed (else return 0)
		 * 
		 * @return the address of this node if fixed
		 */
		public int getAddress() {
			return 0;
		}

		@Override
		public String toString() {
			return "Label Node '" + line + "'";
		}
	}

	/** A node for representing "addresses" aka fixed positions */
	public static class NodeAddress extends NodeLabel {
		/** The address this node represents */
		protected final int address;

		/**
		 * Create an a NodeAddress with a given line and line number
		 * 
		 * @param line
		 *            the line this node represents
		 * @param lineNr
		 *            the line number this node is at
		 */
		public NodeAddress(String line, int address) {
			super(line, -1);
			this.address = address;
		}

		@Override
		public int getAddress() {
			return address;
		}

		@Override
		public boolean isFixed() {
			return true;
		}

		@Override
		public String toString() {
			return "Address Node '" + line + "'";
		}
	}

	/** A Node for representing Instructions */
	public static class NodeInst extends Node {

		/** The assembled data of this instruction node */
		protected byte[] data;

		/** Whether this node has been assembled/processed or not */
		protected boolean processed;

		/**
		 * Create an a NodeInst with a given line and line number
		 * 
		 * @param line
		 *            the line this node represents
		 * @param lineNr
		 *            the line number this node is at
		 */
		public NodeInst(String line, int lineNr) {
			super(line, lineNr);
			data = new byte[] {};
			processed = false;
		}

		@Override
		public String toString() {
			return "Instruction Node '" + line + "'";
		}

		/**
		 * Return whether this instruction references absolute or relative addresses
		 * 
		 * @return if this instruction node is relative or absolute
		 */
		public boolean isRelative() { // TODO: optimize this
			String tmp = line.trim().toLowerCase();
			if (tmp.charAt(0) == 'j')
				return true;
			if (tmp.charAt(0) == 'c' && tmp.charAt(1) == 'a' && tmp.charAt(2) == 'l' && tmp.charAt(3) == 'l')
				return true;
			return false; // !(line.contains(",") || line.contains("["));
		}

		/**
		 * Assemble this node using a given F0x86 instance
		 * 
		 * @param fox
		 *            the F0x86 instance used for assembling
		 */
		public void assemble(F0x86 fox) {
			assemble(fox, line);
		}

		/**
		 * Assemble this node with a given F0x86 instance and a line (containing an
		 * instruction) to work with
		 * 
		 * @param fox
		 *            the F0x86 instance to use to assemble
		 * @param l
		 *            the line this instruction represents
		 */
		private void assemble(F0x86 fox, String l) {
			processed = true;
			Instruction result = fox.assembleInst(l);
			if (result == null) {
				throwError("Instruction not found");
				processed = false;
				return;
			}
			data = result.encode(l, false);
		}

		/**
		 * Return whether this instruction has been assembled/processed
		 * 
		 * @return if this instruction was processed
		 */
		public boolean isProcessed() {
			return processed;
		}

		@Override
		public byte[] getData() {
			return data;
		}

		@Override
		public int getSize() {
			if (isProcessed())
				return getData().length;
			return 4; // approximate average value if not assembled yet
		}

	}

	/** A Node for representing Macros */
	public static class NodeMacro extends Node {

		/** The Assembler instance this macro operates on */
		protected final Assembler assembler;

		/**
		 * Create MacroNode with a given assembler, line and line number
		 * 
		 * @param asm
		 *            the assembler this node operates on
		 * @param line
		 *            the line this node represents
		 * @param lineNr
		 *            the line number this node is at
		 */
		public NodeMacro(Assembler asm, String line, int lineNr) {
			super(line, lineNr);
			assembler = asm;
			parse();
		}

		/**
		 * Execute this macro
		 */
		public void parse() {
			throwError("Macro not found");
			// assembler.content.add(new NodeAddress("MessageBoxA", 0x1000));
		}

		@Override
		public String toString() {
			return "Macro Node '" + line + "'";
		}

	}

	/** The F0x86 instance this Assembler uses */
	protected final F0x86 fox;
	/** The Node ArrayList this Assembler has its content in */
	protected final ArrayList<Node> content;

	/**
	 * Create a new Assembler instance with a given F0x86 instance
	 * 
	 * @param fox
	 *            the F0x86 instance to work with
	 */
	public Assembler(F0x86 fox) {
		this.fox = fox;
		content = new ArrayList<Node>();
	}

	/**
	 * Adds file to content list in node format
	 * 
	 * @param file
	 *            the file to add into this
	 */
	public void parseFile(File file) {
		try {
			List<String> l = Files.readAllLines(file.toPath(), Charset.defaultCharset());
			String[] lines = l.toArray(new String[l.size()]);
			for (int i = 0; i < lines.length; i++) {
				int lineNr = i + 1;
				String line = lines[i].trim();
				line = line.split(";")[0].trim();
				if (line.length() == 0)
					continue;
				if (line.contains(":")) {
					content.add(new NodeLabel(line.split(":")[0].trim(), lineNr));
					if (line.split(":").length == 2) {
						String tmp = line.split(":")[1].trim();
						if (isDX(tmp))
							handleDX(tmp, lineNr);
						else
							content.add(new NodeInst(tmp, lineNr));
					} else if (line.split(":").length > 2)
						System.err.println("Error@Line" + i + " at more than one ':'");
					continue;
				}
				if (line.startsWith("#")) {
					content.add(new NodeMacro(this, line.substring(1), lineNr));
					continue;
				}
				if (isDX(line))
					handleDX(line, lineNr);
				else
					content.add(new NodeInst(line, lineNr));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean isDX(String line) {
		return (Character.toLowerCase(line.charAt(0)) == 'd' && (Character.toLowerCase(line.charAt(1)) == 'b'
				|| Character.toLowerCase(line.charAt(1)) == 'w' || Character.toLowerCase(line.charAt(1)) == 'd'
				|| Character.toLowerCase(line.charAt(1)) == 'q')) && line.contains(",");
	}

	private void handleDX(String line, int lineNr) {
		String[] parts = line.split(",");
		content.add(new NodeInst(parts[0], lineNr));
		for (int i = 1; i < parts.length; i++)
			content.add(new NodeInst(line.charAt(0) + "" + line.charAt(1) + ' ' + parts[i], lineNr));
	}

	public void processNodes(int codeBase) {

		Iterator<Node> iter = content.iterator();
		int[] nodePositions = new int[content.size()];
		boolean[] containsLabel = new boolean[content.size()];
		boolean[] containsMath = new boolean[content.size()];
		int offset = 0;
		// calculate approximate size and used labels
		for (int i = 0; i < nodePositions.length; i++) {
			Node n = iter.next();
			nodePositions[i] = offset;
			offset += n.getSize();
			if (n instanceof NodeLabel) {
				Iterator<Node> iter2 = content.iterator();
				for (int a = 0; a < nodePositions.length; a++) {
					Node b = iter2.next();
					if (a == i)
						continue;
					if (b.getLine().contains(((NodeLabel) n).getLabel())) {
						if (b.getLine().equals(((NodeLabel) n).getLabel())) {
							n.throwError("Label already defined");
							break;
						}
						containsLabel[a] = true;
					}
				}
			} else if (n instanceof NodeInst) {
				for (char c : n.getLine().toCharArray())
					if (c == '+' || c == '-' || c == '*' || c == '/' || c == '%') { // might add more math operations
																					// later but those are the common
																					// ones
						containsMath[i] = true;
						break;
					}
			}
		}

		// Process Nodes with guessed values and sizes
		iter = content.iterator();
		for (int i = 0; i < nodePositions.length; i++) {
			Node node = iter.next();
			if (node instanceof NodeInst) {
				NodeInst nodeI = (NodeInst) node;
				String instLine = nodeI.getLine();
				if (containsLabel[i])
					instLine = calculateLabels(nodeI, nodePositions, nodePositions[i], codeBase);
				if (containsMath[i])
					instLine = evaluateMath(instLine, nodeI);
				nodeI.assemble(fox, instLine);
			}
		}

		// Recalculate size as instructions are now processed
		// It's necessary to compute them before the precise processing, because offsets
		// of later
		// values are necessary when processing
		offset = 0;
		iter = content.iterator();
		for (int i = 0; i < nodePositions.length; i++) {
			Node n = iter.next();
			nodePositions[i] = offset;
			offset += n.getSize();
		}

		// Process Nodes again with precise values
		iter = content.iterator();
		for (int i = 0; i < nodePositions.length; i++) {
			Node node = iter.next();
			if (node instanceof NodeInst) {
				NodeInst nodeI = (NodeInst) node;
				int sizeBefore = nodeI.getSize();
				if (!nodeI.isProcessed())
					continue;
				String instLine = nodeI.getLine();
				if (containsLabel[i])
					instLine = calculateLabels(nodeI, nodePositions, nodePositions[i], codeBase);
				if (containsMath[i])
					instLine = evaluateMath(instLine, nodeI);
				nodeI.assemble(fox, instLine);
				if (nodeI.getSize() != sizeBefore)
					nodeI.throwError("Size changed");
			}
		}
	}

	public String calculateLabels(NodeInst node, int base) {
		int[] nodePositions = new int[content.size()];
		int nodePosition = 0;
		int offset = 0;
		for (int i = 0; i < nodePositions.length; i++) {
			nodePositions[i] = offset;
			if (node == content.get(i))
				nodePosition = offset;
			offset += content.get(i).getSize();
		}
		offset = 0;
		return calculateLabels(node, nodePositions, nodePosition, base);
	}

	/**
	 * Evaluate part of a string builder for a given operation in a given order
	 * 
	 * @param source
	 *            string to parse
	 * @param i
	 *            index containing the operation
	 * @param inc
	 *            inc == -1: parse reversed; inc == 1: parse forward
	 * @return array [first/last index, searched result] or null if not evaluated
	 * @throws Exception
	 *             if failed to evaluate
	 */
	private Integer[] partlyEvaluateOne(StringBuilder source, int i, int inc) throws Exception {
		int from = -1;
		int to = -1;

		// bin = 0x1, oct = 0x2, dec = 0x4, hex = 0x8, header_confirmed = 0x10
		final int isViableHex = 0x8;
		final int isViableDec = 0x4;
		final int isViableOct = 0x2;
		final int isViableBin = 0x1;
		final int isViableHeader = 0x10;

		int isViable = isViableBin | isViableOct | isViableDec | isViableHex;

		for (int a = i + inc; a >= 0 && a < source.length(); a += inc) {
			char a_c = source.charAt(a);
			int isViableCur = (Character.digit(a_c, 16) != -1 ? isViableHex : 0)
					| (Character.digit(a_c, 10) != -1 ? isViableDec : 0)
					| (Character.digit(a_c, 8) != -1 ? isViableOct : 0)
					| (Character.digit(a_c, 2) != -1 ? isViableBin : 0);
			boolean isReadable = ((isViable & isViableCur) & (~isViableHeader)) != 0;
			if (a == i + inc && (a_c == '-' || a_c == '+'))
				continue;
			if (isReadable == false && ((inc == -1 && a == i + inc) || (inc == +1))) { // last character of number
				if ((a_c == 'h' || a_c == 'H') && (isViable & isViableHex) != 0) {
					isViable = ((isViable & isViableHex) | isViableHeader);
					if (inc == -1)
						continue;
					else
						a++;
				} else if ((a_c == 'o' || a_c == 'O') && (isViable & isViableOct) != 0) {
					isViable = ((isViable & isViableOct) | isViableHeader);
					if (inc == -1)
						continue;
					else
						a++;
				} else if ((a_c == 'b' || a_c == 'B') && (isViable & isViableBin) != 0) {
					isViable = ((isViable & isViableBin) | isViableHeader);
					if (inc == -1)
						continue;
					else
						a++;
				}
			}
			if ((isViable & isViableHex) != 0 && a_c == 'x') {
				if (a + inc < source.length() && a + inc >= 0 && source.charAt(a - 1) == '0') {
					isViable = ((isViable & isViableHex) | isViableHeader);
					a += inc;
					if (inc == -1) {
						isReadable = false;
						continue;
					} else
						continue;
				}
			}
			if (isReadable && ((inc == -1 && a == 0) || (inc == 1 && a == source.length() - 1))) {
				a += inc;
				isReadable = false;
			}
			if (!isReadable) {
				if (a - (i + inc) != 0 && (((isViable & isViableDec) != 0) || isViable > isViableHeader)) {
					from = a - inc;
					to = i + inc;
				}
				break;
			}
			isViable &= (isViableCur | isViableHeader);
		}
		if (from != -1 && to != -1) {
			int t = from;
			from = Math.min(from, to);
			to = Math.max(to, t);
			// System.err.println("Evauluate.. "+source.substring(from, to+1)+" -> "+new
			// Integer((int) Instruction.parseNumber(source.substring(from, to+1))));
			return new Integer[] { Integer.valueOf(inc == -1 ? from : to),
					Integer.valueOf((int) Instruction.parseNumber(source.substring(from, to + 1))) };
		}
		return null;
	}

	/**
	 * Evaluate a StringBuilder for a given operation at a specific index
	 * 
	 * @param out
	 *            the StringBuilder to partly evaluate
	 * @param i
	 *            the index where the operation to process is at
	 * @return whether partly evaluating for this operation worked
	 * @throws Exception
	 *             something is malformed
	 */
	private boolean partlyEvaluate(StringBuilder out, int i) throws Exception {
		char c = out.charAt(i);
		Integer[] a_value = partlyEvaluateOne(out, i, -1);
		if (a_value == null)
			return false;
		Integer[] b_value = partlyEvaluateOne(out, i, +1);
		if (b_value == null)
			return false;
		int result = 0;
		// System.err.println(a_value[1]+" "+c+" "+b_value[1]);
		if (c == '+')
			result = a_value[1] + b_value[1];
		else if (c == '-')
			result = a_value[1] - b_value[1];
		else if (c == '*')
			result = a_value[1] * b_value[1];
		else if (c == '/')
			result = a_value[1] / b_value[1];
		else if (c == '%')
			result = a_value[1] % b_value[1];
		out.replace(a_value[0], b_value[0] + 1, Integer.toString(result));
		return true;
	}

	/**
	 * Evaluate a String containing math operations into the most simplified version
	 * 
	 * @param in
	 *            the String to evaluate and simplify
	 * @param node
	 *            the node this String belongs for, only used for Exception Messages
	 * @return the simplified String
	 */
	public String evaluateMath(String in, Node node) {
		// System.out.println(in);
		StringBuilder out = new StringBuilder(in);

		boolean replaced = false;
		try {
			do {
				replaced = false;
				for (int i = 0; i < out.length(); i++) {
					char c = out.charAt(i);
					if (c == '*' || c == '/' || c == '%') {
						if (partlyEvaluate(out, i)) {
							replaced = true;
							break;
						}
					}
				}
			} while (replaced);
			do {
				replaced = false;
				for (int i = 0; i < out.length(); i++) {
					char c = out.charAt(i);
					if (c == '+' || c == '-') {
						if (partlyEvaluate(out, i)) {
							replaced = true;
							break;
						}
					}
				}
			} while (replaced);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(in);
			node.throwError("Error processing");
		}
		return out.toString();
	}

	public String calculateLabels(NodeInst node, int[] nodePositions, int nodePosition, int base) {
		StringBuilder line = new StringBuilder(node.getLine());
		Iterator<Node> iter = content.iterator();
		for (int i = 0; i < nodePositions.length; i++) {
			Node n = iter.next();
			if (n instanceof NodeLabel) {
				String find = ((NodeLabel) n).getLabel();
				if (!node.getLine().contains(find))
					continue;
				int node_position = nodePosition + node.getSize(); // getNodePosition(node) + node.getSize();
				int label_position = nodePositions[i];// getNodePosition(n);
				int value = 0;
				String replace = null;
				if (node.isRelative()) {
					if (((NodeLabel) n).isFixed()) {
						value = ((NodeLabel) n).getAddress();
						replace = "dword [0x" + Integer.toHexString(value) + "]";
					} else {
						value = label_position - node_position;
						if (value < -32767)
							value &= 0xFFFFFFFF;
						else if (value < -127)
							value &= 0xFFFF;
						else if (value < 0)
							value &= 0xFF;
						replace = "0x" + Integer.toHexString(value);
					}
				} else {
					if (((NodeLabel) n).isFixed())
						value = ((NodeLabel) n).getAddress();
					else
						value = label_position + base;
					replace = "0x" + Integer.toHexString(value);
				}

				int p = -1;
				while ((p = line.indexOf(find, p + 1)) != -1) {
					char b = p > 0 ? line.charAt(p - 1) : 0;
					char a = line.length() > p + find.length() ? line.charAt(p + find.length()) : 0;
					if ((a == 0 || (!Character.isAlphabetic(a) && !Character.isDigit(a) && a != '_' && a != '$'
							&& a != '.'))
							&& (b == 0 || (!Character.isAlphabetic(b) && !Character.isDigit(b) && b != '_' && b != '$'
									&& b != '.'))) {
						line.replace(p, p + find.length(), replace);
					}
				}
				// line = line.replace(((NodeLabel)n).getLabel(), replace); // replace actually
				// replaces all occurences, just unlike replaceAll without regex..
			}
		}
		return line.toString();
	}

	/**
	 * Return the size of all nodes added
	 * 
	 * @return the size of this instance in assembled format
	 */
	public int getSize() {
		int size = 0;
		for (Node n : content)
			size += n.getSize();
		return size;
	}

	/**
	 * Assemble the content of this instance into a byte array
	 * 
	 * @return the assembled byte array
	 */
	public byte[] assemble() {
		byte[] data = new byte[getSize()];
		int currentWriting = 0;
		for (Node n : content) {
			System.arraycopy(n.getData(), 0, data, currentWriting, n.getData().length);
			currentWriting += n.getData().length;
		}
		return data;
	}

	/**
	 * Assemble the content of this instance into a hex string
	 * 
	 * @return the resulting hex string
	 */
	public String hexify() {
		byte[] data = assemble();
		StringBuilder output = new StringBuilder();
		for (byte d : data) {
			String h = Integer.toHexString(d & 0xFF);
			if (h.length() == 1)
				output.append("0");
			output.append(h);
		}
		return output.toString();
	}

	@Override
	public String toString() {
		return "Assembler with " + content.size() + " Nodes: " + hexify();
	}

}

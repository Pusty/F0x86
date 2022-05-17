package pusty.f0x86;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;

/** A class responsible for assembling and disassembling x86 assembly */
public class F0x86 {
	
	/** The instructions registered in this instance */
	private Instruction[] instructions;
	
	/**
	 * Create a new F0x86 instance
	 */
	public F0x86() {
		parseFile("base");
	}
	
	/**
	 * Parse a given internal file for instruction definitions.
	 * @param name the internal file to parse
	 */
	public void parseFile(String name) {
		ArrayList<Instruction> arrayList = new ArrayList<Instruction>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(F0x86.class.getResourceAsStream(name)));
		try {
			String line;
			while((line = reader.readLine()) != null) {
				if(line.startsWith("#") || line.trim().length()==0) continue;
				Instruction inst = new Instruction(line);
				arrayList.add(inst);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		try { reader.close(); }catch(Exception e){ e.printStackTrace(); }
		instructions = arrayList.toArray(new Instruction[arrayList.size()]);
	}
	
	
	/**
	 * Iterates over all possible encodings and return the Instruction with the smallest size
	 * NOTE: This is pretty slow and most likely not needed
	 * @param str the string to assemble
	 * @return the smallest instruction, null if not found
	 */
	public Instruction assembleInstFull(String str) {
		ArrayList<Instruction> list_inst = new ArrayList<Instruction>();
		ArrayList<Integer>     list_int  = new ArrayList<Integer>();
		for(Instruction inst:instructions) {
			try {
				byte[] data_encode = inst.encode(str, true); //fetch instructions in strict mode
				if(data_encode != null) {
					list_inst.add(inst);
					list_int.add(data_encode.length);
				}
			}catch(Exception e){}
		}
		if(list_inst.size() == 0) {
			try {
				for(Instruction inst:instructions) {
					byte[] data_encode = inst.encode(str, false); //fetch instructions without strict mode
					if(data_encode != null) {
						list_inst.add(inst);
						list_int.add(data_encode.length);
					}
				}
			}catch(Exception e){}
		}
		if(list_inst.size() == 0)
			return null;	
		int smallest = 0;
		int smallest_size = Integer.MAX_VALUE;
		for(int i=0;i<list_int.size();i++) {
			if(list_int.get(i) < smallest_size) {
				smallest = i;
				smallest_size = list_int.get(i);
			}
		}
		return list_inst.get(smallest);
	}
	
	
	/**
	 * Tries to find a matching instruction and stops after the first possible encoding
	 * @param str the string to assemble
	 * @return the resulting instruction, null if not possible
	 */
	public Instruction assembleInstLazy(String str) {
		for(Instruction inst:instructions) {
			try {
				byte[] data_encode = inst.encode(str, false); //fetch instructions in strict mode
				if(data_encode != null) {
					return inst;
				}
			}catch(Exception e){e.printStackTrace();System.err.println(inst);System.err.println(str);System.exit(1);}
		}
		return null;
	}
	
	
	/**
	 * Assemble a single instruction
	 * @param str the string to assemble
	 * @return the resulting instruction, null if not possible
	 */
	public Instruction assembleInst(String str) {
		//System.out.println(str);
		return assembleInstLazy(str);
	}
	
	
	/**
	 * Assemble a string into a byte array
	 * @param str the string to assemble
	 * @return the resulting assembled data as a byte array
	 */
	public byte[] assemble(String str) {
		return assembleInst(str).encode(str, false);
	}
	
	/**
	 * Assemble a string into a hex array
	 * @param str the string to assemble
	 * @return the resulting assembled data as a hex string
	 */
	public String assembleHexString(String str) {
		return assembleInst(str).encodeHexString(str);
	}
	
	/**
	 * Disassemble a given byte array
	 * @param data the byte array to disassemble
	 * @return the disassembled result
	 */
	public String disassemble(byte[] data) {
		if(data == null) return null;
		for(Instruction inst:instructions) {
			try {
			String disasm = inst.decodeInstruction(data);
			if(disasm != null) 
				return disasm;
			}catch(Exception e){}
		} return null;
	}
	
	/**
	 * Disassemble a given hex string
	 * @param str the hex string to disassemble
	 * @return the resulting assembly
	 */
	public String disassembleHexString(String str) {
		if(str.length()%2 != 0) {
			System.err.println("Can't work with odd length hex string");
			return null;
		}
		if(str.length() == 0) {
			System.err.println("Can't work with empty string");
			return null;
		}
		byte[] data = new byte[str.length()/2];
		for(int i=0;i<data.length;i++)
			data[i] = (byte) Integer.parseInt(str.substring(i*2, i*2+2), 16);
		return disassemble(data);
	}
	

	//test the file parsing functionality
	public static void testParsingFile() {
		F0x86 fox = new F0x86();
		Assembler assembler = new Assembler(fox);
		assembler.parseFile(new File("test.asm"));
		assembler.processNodes(0x401000);
		System.out.println(assembler.hexify());
	}
	
	//test the assemble methods
	public static void testAssembleLine() {
		F0x86 fox = new F0x86();
		System.out.println(fox.assembleHexString("xchg [123], eax"));
	}
	
	//test the disassemble methods
	public static void testDisassembleLine() {
		F0x86 fox = new F0x86();
		System.out.println(fox.disassembleHexString("c644484569"));
	}
	
	//method for testing the functionality of very thing
	public static void main(String[] args) {
		testParsingFile();
		testAssembleLine();
		testDisassembleLine();
	}
}

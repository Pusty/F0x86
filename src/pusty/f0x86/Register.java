package pusty.f0x86;

import java.util.ArrayList;
import java.util.List;

/**
 * A class for keeping track of all registers and providing information about them.
 * TODO: This probably should be an enumeration, check if that's still F0xCv2 compatible
 */
public class Register {
	
	private static final ArrayList<Register> registerList = new ArrayList<Register>();

	public static final int REG8  = 1;
	public static final int REG16 = 2;
	public static final int REG32 = 3;
	public static final int REG64 = 4;
	
	public static final Register AL  = new Register("AL",  REG8,  0);
	public static final Register AX  = new Register("AX",  REG16, 0);
	public static final Register EAX = new Register("EAX", REG32, 0);
	public static final Register RAX = new Register("RAX", REG64, 0);
	
	public static final Register CL  = new Register("CL",  REG8,  1);
	public static final Register CX  = new Register("CX",  REG16, 1);
	public static final Register ECX = new Register("ECX", REG32, 1);
	public static final Register RCX = new Register("RCX", REG64, 1);
	
	public static final Register DL  = new Register("DL",  REG8,  2);
	public static final Register DX  = new Register("DX",  REG16, 2);
	public static final Register EDX = new Register("EDX", REG32, 2);
	public static final Register RDX = new Register("RDX", REG64, 2);
	
	public static final Register BL  = new Register("BL",  REG8,  3);
	public static final Register BX  = new Register("BX",  REG16, 3);
	public static final Register EBX = new Register("EBX", REG32, 3);
	public static final Register RBX = new Register("RBX", REG64, 3);
	
	//Encoding 4 (100) within MOD R/M bits with a displacement => SIB MODE
	public static final Register AH  = new Register("AH",  REG8,  4);
	public static final Register SP  = new Register("SP",  REG16, 4);
	public static final Register ESP = new Register("ESP", REG32, 4);
	public static final Register RSP = new Register("RSP", REG64, 4);
	
	public static final Register CH  = new Register("CH",  REG8,  5);
	public static final Register BP  = new Register("BP",  REG16, 5);
	public static final Register EBP = new Register("EBP", REG32, 5);
	public static final Register RBP = new Register("RBP", REG64, 5);
	
	public static final Register DH  = new Register("DH",  REG8,  6);
	public static final Register SI  = new Register("SI",  REG16, 6);
	public static final Register ESI = new Register("ESI", REG32, 6);
	public static final Register RSI = new Register("RSI", REG64, 6);
	
	
	public static final Register BH  = new Register("BH",  REG8,  7);
	public static final Register DI  = new Register("DI",  REG16, 7);
	public static final Register EDI = new Register("EDI", REG32, 7);
	public static final Register RDI = new Register("RDI", REG64, 7);
	
	public static Register getRegister(int reg, int type) {
		for(Register r:getRegisters())
			if(r.getEncoded() == reg && r.getType() == type)
				return r;
		return null;
	}
	
	public static List<Register> getRegisters() {
		return registerList;
	}
	
	public static List<Register> getRegisters(int filter) {
		ArrayList<Register> lst = new ArrayList<Register>();
		for(Register r:getRegisters())
			if(r.getType() == filter)
				lst.add(r);
		return lst;
	}
	
	public static Register getRegister(String reg) {
		for(Register r:getRegisters())
			if(r.getName().equalsIgnoreCase(reg))
				return r;
		return null;
	}
	
	/** The name of this register */
	private final String name;
	/** The size/type of this register */
	private final int    type;
	/** The encoding bits for this register */
	private final int    encode;
	
	private Register(String name, int type, int encode) {
		this.name   = name.toLowerCase();
		this.type   = type;
		this.encode = encode;
		registerList.add(this);
	}
	public String getName() {
		return name;
	}
	public int getType() {
		return type;
	}
	public boolean is8bit() {
		return type == REG8;
	}
	public boolean is16bit() {
		return type == REG16;
	}
	public boolean is32bit() {
		return type == REG32;
	}
	public boolean is64bit() {
		return type == REG64;
	}
	public int getEncoded() {
		return encode;
	}
	
	/**
	 * Return the name for a size
	 * @param size the size to return a name for
	 * @return the name of the size
	 */
	public static String getNameOfType(int size) {
		if(size == REG8)
			return "byte";
		if(size == REG16)
			return "word";
		if(size == REG32)
			return "dword";
		if(size == REG64)
			return "qword";
		return "UNKNOWN";
	}
}

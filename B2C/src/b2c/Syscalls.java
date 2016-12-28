package b2c;

import java.io.File;
import java.util.ArrayList;

public class Syscalls {
	
	//Not much to explain here. Look at a generated file to get an idea of what this does
	
	static String syscallContent = "";
	static ArrayList<String> syscalls = new ArrayList<String>();
	static ArrayList<String> syscallIDs = new ArrayList<String>();
	
	public static void addSyscall(String syscall, String syscallID) {
		syscalls.add(syscall);
		syscallIDs.add(syscallID);
	}
	
	public static void createSyscallFile() {
		for (int i = 0; i < syscalls.size(); i++) {
			syscallContent += "\t.export\t_" + syscalls.get(i) + "\n";
		}
		for (int i = 0; i < syscalls.size(); i++) {
			syscallContent += 
					"\n_" + syscalls.get(i) + ":\n" +
					"\tmov.l\tsyscall_table, r2\n" +
					"\tmov.l\t_" + syscalls.get(i) + "_code, r0\n" +
					"\tjmp\t@r2\n" +
					"\tnop\n" + 
					"_" + syscalls.get(i) + "_code:\n" +
					"\t.data.l\tH'" + syscallIDs.get(i) + "\n";
		}
		syscallContent += 
				"\nsyscall_table:\n" +
				"\t.data.l\tH'80010070\n\n" +
				"\t.end";
		IO.writeToFile(new File(B2C.path + "/syscalls.src"), syscallContent, true); //TODO change to false when all syscalls are added
	}
	
}

package mdfs.cli;

import mdfs.client.api.FileQuery;
import mdfs.client.api.FileQueryImpl;
import mdfs.utils.io.protocol.MDFSProtocolMetaData;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class CLI {
	public CLI(){
		Scanner scanner = new Scanner(System.in);
		System.out.print("Enter Username: ");
		String user = scanner.nextLine();
		System.out.print("Enter Password: ");
		String pass = scanner.nextLine();
		
		FileQuery fq = new FileQueryImpl(user, pass);
		String command;
		while(true){
			System.out.print(user + "@MDFS:" + fq.pwd() + "$ " );
			command = scanner.nextLine();
			command = command.trim();
			
			
			
			if(command.startsWith("ls")){
				command = command.replaceFirst("ls", "").trim();
				MDFSProtocolMetaData[] files;
				timerStart();
				if(command.length()<1)
					files = fq.ls(null);
				else{
					files = fq.ls(command, null);
				}
				timerStop();
				


				for (MDFSProtocolMetaData file : files) {

                    System.out.print (file.getPermission() + "   ");
                    System.out.print(file.getOwner() + "  " + file.getGroup() + "\t");
                    System.out.print(file.getType() + "\t");
                    System.out.print(file.getSize() + "\t");
                    System.out.println(file.getPath());

				}
				timerPrint();
				
				
				
			}else if(command.startsWith("mkdir")){
 				command = command.replaceFirst("mkdir ", "");
				String[] dirs = command.split(" ");
				timerStart();
				for (String dir : dirs) {
					if(!fq.mkdir(dir)){
						System.out.println("mkdir: cannot create directory " + dir);
					}
				}
				timerStop();
				timerPrint();
				
			}else if(command.startsWith("cd")){
				command = command.replaceFirst("cd", "").trim();
				timerStart();
				fq.cd(command, null);
				timerStop();
				timerPrint();
			
			}else if(command.startsWith("touch")){
				command = command.replaceFirst("touch", "").trim();
				String[] files = command.split(" ");
				
				
		
				File f = null;
				try {
					f = File.createTempFile("mdfs.cli.tmptouch", ".tmp");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			
				timerStart();
				for (String file : files) {
					if(!fq.put(f, file, null))
						System.out.println("touch: cannot touch  " + file);
				}
				timerStop();
				timerPrint();	
				
				f.delete();
			}
			else if(command.startsWith("rm")){
				command = command.replaceFirst("rm", "").trim();
				String[] files = command.split(" ");
				
				timerStart();
				for (String file : files) {
					file = file.trim();
					if(!file.equals(""))
						fq.rm(file, "r");
				}
				timerStop();
				timerPrint();
			}else if(command.startsWith("pwd")){
                System.out.println(fq.pwd());
            }else if(command.startsWith("chmod")){
                System.out.println(fq.pwd());
                command = command.replaceFirst("chmod", "").trim();
                command = command.replace("     ", " ");
                command = command.replace("    ", " ");
                command = command.replace("   ", " ");
                command = command.replace("  ", " ");
                String[] parts = command.split(" ");
                fq.chmod(parts[1], Integer.parseInt(parts[0]), null);
            }else if(command.startsWith("chown")){
                System.out.println(fq.pwd());
                command = command.replaceFirst("chown", "").trim();
                command = command.replace("     ", " ");
                command = command.replace("    ", " ");
                command = command.replace("   ", " ");
                command = command.replace("  ", " ");
                String[] parts = command.split(" ");
                String[] mod = parts[0].split(":");
                fq.chown(parts[1], mod[0], mod[1], null);
            }
		}
		
		
	}
	
	long time;
	public void timerStart(){
		time = System.currentTimeMillis();
	}
	public void timerStop(){
		time = System.currentTimeMillis() - time;
	}
	public void timerPrint(){
		System.out.print("Time: " + ((double)(time))/(double)1000 );
		System.out.println("s = " + time + "ms");
	}
	
	public static void main(String[] args){
        System.out.println();
		new CLI();
	}
}

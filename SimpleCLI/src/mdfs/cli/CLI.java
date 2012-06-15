package mdfs.cli;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

import mdfs.client.api.FileQuery;
import mdfs.client.api.FileQueryImpl;

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
			
			
			
			
			if(command.startsWith("ls")){
				command = command.replaceFirst("ls", "").trim();
				JSONObject[] files;
				timerStart();
				if(command.length()<1)
					files = fq.ls(null);
				else{
					files = fq.ls(command, null);
				}
				timerStop();
				
				
				for (JSONObject file : files) {
					try {
						System.out.print (file.getInt("permission") + "   ");
						System.out.print(file.getString("owner") + "  " + file.getString("group") + "\t");	
						System.out.print(file.getString("type") + "\t");
						System.out.print(file.getLong("size") + "\t");
						System.out.println(file.getString("path"));
					} catch (JSONException e) {
						e.printStackTrace();
					}
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

		new CLI();
	}
}

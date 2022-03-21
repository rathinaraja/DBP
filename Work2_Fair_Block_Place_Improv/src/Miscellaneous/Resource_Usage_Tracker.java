package Miscellaneous;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException; 

public class Resource_Usage_Tracker{ 
	//public String FILENAME = "E:\\test\\filename.txt";
	public String FILENAME = "4.Resource_Tracker.txt";   
	FileWriter fw=null;
	BufferedWriter bw=null;
	
	synchronized public void file_open(){
		try{
			fw  =new FileWriter(FILENAME,true);				//the true will append the new data 
			bw = new BufferedWriter(fw);  
		}
		catch (IOException e) {
			System.err.println(e);
		}
	} 
	synchronized public void write_string(String str){
		file_open();
		try{ 
			bw.write(str);    
			bw.newLine();
		}
		catch (IOException e) {
			System.err.println(e);
		}
		file_close();
	}
	
	synchronized public void file_close(){ 
			try { 
				if (bw != null)
					bw.close(); 
				if (fw != null)
					fw.close(); 
			} catch (IOException ex) { 
				ex.printStackTrace(); 
			}  
	}
}

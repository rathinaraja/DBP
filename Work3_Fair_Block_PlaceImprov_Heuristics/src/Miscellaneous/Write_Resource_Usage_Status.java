package Miscellaneous;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException; 

public class Write_Resource_Usage_Status{ 
	//public String FILENAME = "E:\\test\\filename.txt";
	public String FILENAME = "3.Resource_Usage_Status.txt";   
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
	
	synchronized public void write_1D(int[] x,String name) throws IOException{ 
		file_open();
		try{
			bw.write("=====================================================================================================================================================");
			bw.newLine();
			bw.write(name);
			bw.newLine();
			bw.write("=====================================================================================================================================================");
			bw.newLine();
			for (int i = 0; i < x.length; i++) {  
					bw.write(x[i]+"\t"); 
					bw.newLine();
			  }
		}
		catch (IOException e) {
			System.err.println(e);
		}
		file_close();
	} 
	
	synchronized public void write_2D(int[][] x,String name) throws IOException{ 
		file_open();
		try{
			bw.write("=====================================================================================================================================================");
			bw.newLine();
			bw.write(name);
			bw.newLine();
			bw.write("=====================================================================================================================================================");
			bw.newLine();
			for (int i = 0; i < x.length; i++) { 
				for(int j=0;j<x[i].length;j++)
					bw.write(x[i][j]+"\t"); 
			    bw.newLine();
			  }
		}
		catch (IOException e) {
			System.err.println(e);
		}
		file_close();
	} 
	
	synchronized public void write_1D(double[] x,String name) throws IOException{ 
		file_open();
		try{
			bw.write("=====================================================================================================================================================");
			bw.newLine();
			bw.write(name);
			bw.newLine();
			bw.write("=====================================================================================================================================================");
			bw.newLine();
			for (int i = 0; i < x.length; i++) {  
				bw.write(x[i]+"\t"); 
			    bw.newLine();
			  }
		}
		catch (IOException e) {
			System.err.println(e);
		}
		file_close();
	} 
	
	synchronized public void write_2D(double[][] x,String name) throws IOException{ 
		file_open();
		try{
			bw.write("=====================================================================================================================================================");
			bw.newLine();
			bw.write(name);
			bw.newLine();
			bw.write("=====================================================================================================================================================");
			bw.newLine();
			for (int i = 0; i < x.length; i++) { 
				for(int j=0;j<x[i].length;j++)
					bw.write(x[i][j]+"\t"); 
			    bw.newLine();
			  }
		}
		catch (IOException e) {
			System.err.println(e);
		}
		file_close();
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

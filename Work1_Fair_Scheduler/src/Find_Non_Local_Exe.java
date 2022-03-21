import java.io.*; 
 
public class Find_Non_Local_Exe {

	public static void main(String[] args) throws NumberFormatException, IOException {
		// TODO Auto-generated method stub  
		File file = new File("test.txt"); 
		int count=0;
		  
		BufferedReader br = new BufferedReader(new FileReader(file)); 
		  
		String st; 
		while ((st = br.readLine()) != null){ 
			if(Double.parseDouble(st)==1){
				count++;
			}
		}  
    	System.out.println(count);  
	}
}

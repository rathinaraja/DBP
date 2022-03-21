package Miscellaneous;
import java.util.*;

public class Random_Gen {
	Random rand=new Random(13);
	
	// generating random numbers
	public int[] Gen_Ran_Nos(int start,int end,int size){
		ArrayList<Integer> numbers = new ArrayList<Integer>();  
		int arr[]=new int[size]; 
		while (numbers.size() < size) {
		    int random = rand.nextInt(end-start+1)+start;
		    numbers.add(random);
		}
		for (int i =0; i < numbers.size(); i++) 
	        arr[i] = numbers.get(i);   
		return arr;
	}
		
	// generating unique random numbers
	public int[] Gen_Uniq_Ran_Nos(int start,int end,int size){			
		ArrayList<Integer> numbers = new ArrayList<Integer>();  
		int arr[]=new int[size]; 
		while (numbers.size() < size) {
		    int random = rand.nextInt(end-start+1)+start;	// (end-start+1)+start == size else it runs in infinite loop
		    if (!numbers.contains(random)) {
		        numbers.add(random);
		    }
		}
		for (int i =0; i < numbers.size(); i++) 
            arr[i] = numbers.get(i);   
		return arr;
	}
	
	public double Gen_Map_Output(double start,double end){	
		int flag=1;
		double random=0;
		while(flag==1){
			random=Math.random()*end+start;
			if(random>=start && random <=end){
				flag=2; 
			} 
		} 
		//System.out.println("randam value "+random);
		return random;
	}
}

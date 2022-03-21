package Miscellaneous;
public class Display {
	// displaying 1D and 2D arrays
	synchronized public void display_1D(int[] x,String name){
		System.out.println("====================================================================================================================================================="); 
		System.out.println(name);
		System.out.println("====================================================================================================================================================="); 
		for(int i=0;i<x.length;i++){
			System.out.print(x[i]+"\t");
		}
		System.out.println("");
	}
	
	// displaying 2D arrays
	synchronized public void display_2D(int[][] x,String name){
		System.out.println("====================================================================================================================================================="); 
		System.out.println(name);
		System.out.println("====================================================================================================================================================="); 
		for(int i=0;i<x.length;i++){
			for(int j=0;j<x[0].length;j++)
				System.out.print(x[i][j]+"\t");
			System.out.println("");
		}
	}
	
	// displaying 2D arrays with double
	synchronized public void display_2D(double[][] x,String name){
		System.out.println("====================================================================================================================================================="); 
		System.out.println(name);
		System.out.println("====================================================================================================================================================="); 
		for(int i=0;i<x.length;i++){
			for(int j=0;j<x[0].length;j++)
				System.out.print(x[i][j]+"\t");
			System.out.println("");
		}
	} 
}

public class Temp { 
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		double[][] arr={{1.0,0.0,0.0,0.0,0.0,0.0,10.0,0.0,0.0,1.0,0.0,1.0,0.0,93.75},{0.0,0.0,1.0,0.0,1.0,0.0,93.75,1.0,0.0,0.0,0.0,0.0,0.0,10.0},{1.0,4,0.0,0.0,0.0,50,80.0,1.0,3,0.0,0.0,4,0.0,100.0},{1.0,3,0.0,0.0,4,0.0,100.0,1.0,4,0.0,0.0,0.0,50,80.0}};
		for(int i=0;i<arr.length;i++){
			for(int j=0;j<arr[0].length;j++){
				System.out.print("\t"+arr[i][j]);
			}
			System.out.println("");
		}

	}

}

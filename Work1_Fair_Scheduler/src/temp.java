

import java.text.DecimalFormat; 

public class temp{
	public static void main(String[] args) {
		// TODO Auto-generated method stub 
		DecimalFormat df2 = new DecimalFormat(".##");
		double d=10.45433434;
		double d1=Double.parseDouble(df2.format(d));
		System.out.println(df2.format(d));
		System.out.println(d+d1);
	}

}

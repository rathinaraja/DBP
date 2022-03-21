package RM;

import java.io.IOException;
import java.text.DecimalFormat;

import Miscellaneous.*; 

public class Forming_Combinations {
	public MRAManager mram;
	public int max_combi=50;
	public int max_map_possible[][];
	public double combinations[][]; 
	public DecimalFormat df = new DecimalFormat(".##");
	public Write_MR_Job_Status write=new Write_MR_Job_Status();
	
	public void get_MRAM(MRAManager mram){
		this.mram=mram;
		max_map_possible=new int[mram.wp.Num_Workloads][(mram.vm.num_flavors)+1];			// job/workload number,VM flav1, possible num of map tasks, etc.,
		combinations=new double[max_combi][(mram.wp.Num_Workloads+1)*mram.vm.num_flavors];	// J1,J2,J3,J4,J5,J6,resource usage --> for each flavour
	}
	
	// calculate possible number of map tasks for a workload in each VM flavor
	public void max_possible()throws IOException{ 
		for(int i=0;i<mram.wp.Num_Workloads;i++){
			max_map_possible[i][0]=i+1;
			for(int j=0;j<mram.vm.num_flavors;j++){
				boolean flag=true;
				double temp1,temp2,mul=1;
				while(flag){
					temp1=mul*mram.wp.MR_RR[i][0];
					temp2=mul*mram.wp.MR_RR[i][1];
					if(temp1<=mram.vm.VM_Flavs[j][0] && temp2<=mram.vm.VM_Flavs[j][1]){
						mul++;
						continue;
					}
					else{ 
						max_map_possible[i][j+1]=(int) mul-1;
						flag=false;
					}	
				}
			}
		}
		//mram.d.display_2D(max_map_possible,"possible map tasks of each workload in each VM flavor (Workload #, VM Flav 1, VM Flav 2, VM Flav 3, VM Flav 4, VM Flav 5, VM Flav 6)"); 
		write.write_2D(max_map_possible,"possible map tasks of each workload in each VM flavor (Job/Workload #, VM Flav 1, VM Flav 2, VM Flav 3, VM Flav 4, VM Flav 5, VM Flav 6)"); 
	}
	
	// find possible number of map tasks in each flavor (used as heuristics)
	public double[][] suitable_combinations()throws IOException{  
		double Mem_Ratio,vCPU_Ratio,Mem_Total,vCPU_Total;
		for(int i=0;i<mram.vm.num_flavors;i++){ 
			int count=0;
			while(count !=max_combi){ 
				Mem_Total=vCPU_Total=0; 
				int ran[]=new int[mram.wp.Num_Workloads];  
				for(int j=0;j<mram.wp.Num_Workloads;j++){ 
					ran[j]=(int)mram.r.Gen_Map_Output(0, max_map_possible[j][i+1]+1); 
					vCPU_Total+=ran[j]* mram.wp.MR_RR[j][0];
					Mem_Total+=ran[j]* mram.wp.MR_RR[j][1];
				} 
				vCPU_Ratio=(vCPU_Total/mram.vm.VM_Flavs[i][0])*100;
				Mem_Ratio=(Mem_Total/mram.vm.VM_Flavs[i][1])*100;  
				if(vCPU_Ratio>=70 && vCPU_Ratio<=100 && Mem_Ratio>=70 && Mem_Ratio<=100){
					for(int k=0;k<ran.length;k++)
						combinations[count][((mram.wp.Num_Workloads+1)*i)+k]=ran[k]; 
					combinations[count][mram.wp.Num_Workloads*(i+1)+i]=Double.parseDouble(df.format((vCPU_Ratio+Mem_Ratio)/2)); 
					count++; 
				}  
			} 
		} 
		mram.d.display_2D(combinations,"Possible combinations of map tasks of different jobs/workloads in different VM falvours (every row is possible combinatrion, every 6 columns are for one VM Flav (# of map tasks of each workload and total resource usage))");  
		 write.write_2D(combinations,"Possible combinations of map tasks of different jobs/workloads in different VM falvours (every row is possible combinatrion, every 6 columns are for one VM Flav (# of map tasks of each workload and total resource usage))");  
		return combinations;
	}
}

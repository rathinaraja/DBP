package RM;

import java.io.IOException;
import java.text.DecimalFormat;

import Miscellaneous.*;
import VMs.*;
import Workloads.*; 

public class MR_App_Master{
	public double Map_status[][],Reduce_status[][];  
	public double Map_Output[][];
	public Workload_Parameters wp; 
	public VM_Parameters vm; 
	Write_MR_Job_Status wjs=new Write_MR_Job_Status();
	 
	public int workload_num,map_count,reduce_count; 
	public boolean map_flag=true,reduce_flag=false;
	public Display d=new Display();
	public DecimalFormat df = new DecimalFormat(".##"); 
	public int non_local_exe=0;
	
	public MR_App_Master(Workload_Parameters wp,VM_Parameters vm,int workload_num)throws IOException{
		this.wp=wp;
		this.workload_num=workload_num; 
		this.vm=vm; 
		 
		//10 columns: map task #,Local(0)/Non_local(1), status(not_yet_started(0),running(1)/finished(2)), response time, start time, end time, latency, bin #, VM flavor #, PM #, Rack #	
		Map_status=new double[wp.No_Tasks_MR[workload_num][0]][11];  						// 1 for each workload		
		
		//10 columns: reduce task #, amount of data to process, status(not_yet_started(0),running(1)/finished(2)), response time, start time, end time, latency, bin #, VM flavor #, PM #, Rack #
		Reduce_status=new double[wp.No_Tasks_MR[workload_num][1]][11]; 						// 1 for each workload
		
		//initialize map output matrix: map task #, VM#, VM Flavour #, PM#, Rack #, Reduce1, Reduce2, etc (how much each map task produces to different reduce tasks)
		Map_Output=new double[wp.No_Tasks_MR[workload_num][0]][wp.No_Tasks_MR[workload_num][1]+5];
		
		//initialize with 0.0 for both map and reduce objects 
		for(int i=0;i<Map_status.length;i++)
			for(int j=0;j<Map_status[0].length;j++)
				Map_status[i][j]=0;		
		for(int i=0;i<Reduce_status.length;i++)
			for(int j=0;j<Reduce_status[0].length;j++)
				Reduce_status[i][j]=0; 
 		
		//w.write_2D(Map_Output, "map output matrix");
		//d.display_2D(Map_status, "Map Status of job/thread name  "+wp.workload_name[workload_num]+"  (10 columns: map task #,Local(0)/Non_local(1), status(not_yet_started(0),running(1)/finished(2)), response time, start time, end time, latency, bin #, VM flavor #, PM #, Rack #)");
		//w.write_2D(Map_status, "Map Status of job/thread name  "+wp.workload_name[workload_num]+"  (10 columns: map task #,Local(0)/Non_local(1), status(not_yet_started(0),running(1)/finished(2)), response time, start time, end time, latency, bin #, VM flavor #, PM #, Rack #)");
		//d.display_2D(Reduce_status, "Reduce Status of job/thread name  "+wp.workload_name[workload_num]+"  (10 columns: reduce task #, amount of data to process, status(not_yet_started(0),running(1)/finished(2)), response time, start time, end time, latency, bin #, VM flavor #, PM #, Rack #)");
		//w.write_2D(Reduce_status, "Reduce Status of job/thread name  "+wp.workload_name[workload_num]+"  (10 columns: reduce task #, amount of data to process, status(not_yet_started(0),running(1)/finished(2)), response time, start time, end time, latency, bin #, VM flavor #,PM #, Rack #)");
	} 	

	public void start_job_threads()throws IOException{ 
		System.out.println("\n\t"+wp.workload_name[workload_num]+"  job started");
		while(map_flag){
			map_count=0;  
			//condition checking for all map tasks completion 
			for(int i=0;i<Map_status.length;i++) 
				if(Map_status[i][2]==2)
					map_count++;   
			
			if(Map_status.length==map_count){
				System.out.println("\n\t\t"+"all map tasks of  "+wp.workload_name[workload_num] +" is completed"); 
				wjs.write_2D(Map_status, "Map Status of job/thread name: " + wp.workload_name[workload_num]+"  after done (10 columns: map task #,Local(0)/Non_local(1), status(not_yet_started(0),running(1)/finished(2)), response time, start time, end time, latency, bin #, VM flavor #, PM #, Rack #)");
				//wjs.write_2D(Map_Output, "Map output of " + wp.workload_name[workload_num]+ " map task #, VM#, VM Flavour #, PM#, Rack #, Reduce1, Reduce2, etc (how much each map task produces to different reduce tasks)"); 
				calculate_map_outputs();
				num_calc_exe();
				
				//System.out.println("=====================================================================================================================================================");
				wjs.write_string("=====================================================================================================================================================");
				//System.out.println("\nnumber of non-local execution for"+ wp.workload_name[workload_num]+" is "+non_local_exe); 
				wjs.write_string("number of non-local execution for"+ wp.workload_name[workload_num]+" is "+non_local_exe);
				//System.out.println("=====================================================================================================================================================");
				wjs.write_string("=====================================================================================================================================================");

				map_flag=false; 
				reduce_flag=true; 
			}   
		}
		while(reduce_flag){ 
			reduce_count=0;  
			//condition checking for all map tasks completion 
			for(int i=0;i<Reduce_status.length;i++) 
				if(Reduce_status[i][2]==2)
					reduce_count++;    
			if(Reduce_status.length==reduce_count || Reduce_status.length==0){ 
				System.out.println("\n\t\t"+"all reduce tasks of  "+wp.workload_name[workload_num] +" is completed"); 
				//wjs.write_2D(Reduce_status, "Reduce Status of job/thread name: " + wp.workload_name[workload_num]+" after done (10 columns: reduce task #, amount of data to process, status(not_yet_started(0),running(1)/finished(2)), response time, start time, end time, latency, bin #, VM flavor #, PM #, Rack #)");
				reduce_flag=false;
			} 
		}
		System.out.println("\n\t"+wp.workload_name[workload_num]+" job completed");
	}
	
	public void calculate_map_outputs(){
		for(int i=5;i<Map_Output[0].length;i++){ 
			double temp=0; 
			for(int j=0;j<Map_Output.length;j++){
				temp+=Map_Output[j][i];
			}
			Reduce_status[i-5][0]=(i-5)+1;										// reduce task number
			Reduce_status[i-5][1]=Double.parseDouble(df.format(temp));			// amount of data reduce task to process
		}
	}
	
	public void num_calc_exe(){
		for(int i=0;i<Map_status.length;i++){
			if(Map_status[i][1]==1){
				non_local_exe++;
			}
		}
	}
}
 

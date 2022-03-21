package Workloads;
import VMs.*;

import java.io.IOException;

import Miscellaneous.*;

public class Workload_Parameters {
	public int Num_Workloads=6;																			// number of workloads  
	public double MR_RR[][]={{1,2,1,1},{1,1,2,1},{1,1.5,2,2},		
			          {2,1.5,2,2.5},{1,2,0,0},{2,2,2,3}};												// resource requirements of map and reduce tasks: map(vcpu, mem) reduce(vcpu,mem)
	public String workload_name[]={"wordcount","wordmean","word std deviation","kmean","sort","join"}; 	// workloads name
	
	public int Dataset_size[]={128,64,256,192,77,153};													// amount of data (in GB) to be processed by each workload
	public int block_size=128;																			// HDFS block size in MB
	public int RF=3;																					// HDFS Replication Factor is 3
	public int Num_blocks[]=new int[Num_Workloads];
	public Block_Allocation blck[];																		// block allocation matrix
	
	public int No_Reduce_tasks[]={20,15,18,10,0,5};														// Number of reduce tasks	
	public int No_Tasks_MR[][]=new int[Num_Workloads][2]; 												// Number of map reduce tasks possible for each workload/job
	public double MR_Tasks_Latency[][]={{21,39},{18,33},{15,30},{21,60},{18,0},{32,64}};  					// Approximate Map and Reduce tasks latency in seconds 
	//public double MR_Tasks_Latency[][]={{7,13},{6,11},{5,10},{7,15},{6,0},{13,27}};  					// Approximate Map and Reduce tasks latency in seconds 
	
	public VM_Parameters vm;
	public Block_Allocation ba;
	public Write_To_File w;
	public Display d=new Display();

//==================================================================================================================================================
	public Workload_Parameters(VM_Parameters vm, Write_To_File w){
		this.vm=vm;	 
		this.w=w;
	}
	 
//=====================================================================================================================================================
	
	//display all the workload parameters
	public void worklod_config_display()throws IOException{
		//System.out.println("=====================================================================================================================================================");
		w.write_string("=====================================================================================================================================================");

		//System.out.println("WORKLOAD DETAILS ");
		w.write_string("WORKLOAD DETAILS ");
		
		//System.out.println("=====================================================================================================================================================");
		w.write_string("=====================================================================================================================================================");

		//System.out.println("\nNumber of workloads/jobs   : "+Num_Workloads);	
		w.write_string("Number of workloads/jobs   : "+Num_Workloads);
		
		w.write_string(" ");
		//System.out.println("\nDataset for each workload/job");
		w.write_string("Dataset for each workload/job");
	
		for(int i=0;i<Num_Workloads;i++){
			//System.out.println("\t\t"+workload_name[i]+": "+ Dataset_size[i]+" GB");
			w.write_string("\t\t"+workload_name[i]+": "+ Dataset_size[i]+" GB");
		}	
		
		w.write_string(" ");
		//System.out.println("\nHDFS block size is :"+block_size+" MB ");
		w.write_string("HDFS block size is :"+block_size+" MB ");
				
		w.write_string(" ");
		//System.out.println("\nNumber of map and reduce tasks for each workloads ");
		w.write_string("Number of map and reduce tasks for each workloads ");
		
		Calc_No_tasks();
		
		for(int i=0;i<Num_Workloads;i++){
				//System.out.println("\t\t"+workload_name[i]+ ": " + Num_blocks[i]+ " blocks, "+No_Tasks_MR[i][0]+" map tasks, and "+No_Tasks_MR[i][1]+" reduce tasks");	
				w.write_string("\t\t"+workload_name[i]+ ": " + Num_blocks[i]+ " blocks, "+No_Tasks_MR[i][0]+" map tasks, and "+No_Tasks_MR[i][1]+" reduce tasks");
		}
		
		w.write_string(" ");
		//System.out.println("\nResource requirements of map and reduce tasks");
		w.write_string("Resource requirements of map and reduce tasks");
		
		for(int i=0;i<Num_Workloads;i++){
			//System.out.println("\t\t"+"map tasks: "+ MR_RR[i][0]+" vCPU "+ MR_RR[i][1]+"  GB memory and reduce tasks:  "+ MR_RR[i][2]+" vCPU "+ MR_RR[i][3]+"  GB memory");
			w.write_string("\t\t"+"map tasks: "+ MR_RR[i][0]+" vCPU "+ MR_RR[i][1]+"  GB memory and reduce tasks:  "+ MR_RR[i][2]+" vCPU "+ MR_RR[i][3]+"  GB memory");
		}
		
		w.write_string(" ");
		//System.out.println("\nMap and reduce tasks latency of each workload ");
		w.write_string("Map and reduce tasks latency of each workload ");
		
		for(int i=0;i<Num_Workloads;i++){
				//System.out.println("\t\t"+workload_name[i]+ ": map task latency:" + MR_Tasks_Latency[i][0]+" and reduce task latency: "+MR_Tasks_Latency[i][1]);
				w.write_string("\t\t"+workload_name[i]+ ": map task latency:" + MR_Tasks_Latency[i][0]+" and reduce task latency: "+MR_Tasks_Latency[i][1]);
		} 
	    ba=new Block_Allocation(vm,w,this);
		ba.block_placement();
		
		w.write_string(" ");
		w.write_string("Block loading status of all workloads ");
		for(int i=0;i<Num_Workloads;i++){ 			
			if(ba.blck[i].Block_locations.length==No_Tasks_MR[i][0]){
				w.write_string("\t\tAll blocks of "+workload_name[i] + " job have been loaded");
			}
			else{
				w.write_string((No_Tasks_MR[i][0]-blck[i].Block_locations.length)+" blocks of "+workload_name[i] + " job have not been loaded due to insufficient storage");
			}
		}
		w.write_string(" ");
		//System.out.println("MapReduce Execution Assumptions");
		w.write_string("MapReduce Execution Assumptions");
		w.write_string("\t\tScheduler executes every 5 seconds");	
		w.write_string("\t\tOne map task is dedicated to one block");	
		w.write_string("\t\tBlock transfter time over network for non-local execution is 2 seconds");	
		w.write_string("\t\tReduce tasks are launched after all map tasks are done");	
		
		w.write_string(" ");
		for(int i=0;i<Num_Workloads;i++){ 
			//d.display_2D(ba.blck[i].Block_locations,wp.workload_name[i]+"\n block #, copy1(VM #, PM #, Rack #), copy2(VM #, PM #, Rack #), copy3(VM #, PM #, Rack #)"); 
			w.write_2D(ba.blck[i].Block_locations, workload_name[i]+" data blocks: block #, copy1(VM #, VM Flav #, PM #, Rack #), copy2(VM #, VM Flav #, PM #, Rack #), copy3(VM #, VM Flav #, PM #, Rack #)");
		}
	}	
	
	//Number of map and reduce tasks
		public void Calc_No_tasks(){																	// calculate number of map tasks
			for(int i=0;i<Num_Workloads;i++){
				No_Tasks_MR[i][0]=(int)(Dataset_size[i]*1024)/block_size; 								// converting GB to MB
				No_Tasks_MR[i][1]=No_Reduce_tasks[i]; 													// reduce tasks
				Num_blocks[i]=No_Tasks_MR[i][0]; 
			}
		}	
		
}

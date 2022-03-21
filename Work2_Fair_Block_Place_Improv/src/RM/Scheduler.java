package RM;

import java.io.IOException;
import java.util.*;

import MR.*; 
import Miscellaneous.*;  

public class Scheduler {  
	public Display d=new Display(); 
	public MRAManager mram; 
	public boolean job_status=true;
	public double shared_vCPU,shared_Mem;  
	public int commit=0; 
	public int commit_time=10000; 
	//public double temp_vCPU,temp_vMem;
	public Write_Resource_Usage_Status wrus=new Write_Resource_Usage_Status(); 
	public Resource_Usage_Tracker rus=new Resource_Usage_Tracker(); 
	public ArrayList<String> thread_names;  
	public int active_threads_count_temp;
	public Resource_Tracker rt;
	
	public void schedule_tasks(MRAManager mram)throws IOException{		
		this.mram=mram;	   
		String name;
		int workload_num=0;
		
		try{
	           Thread.sleep((long)5000);										// it should be here only to give time to create job threads
	    }
		catch(Exception err){
	            err.printStackTrace();
		} 
		
		rt=new Resource_Tracker(mram,this);
		
		while(job_status){ 
			thread_names = new ArrayList<String>();
			int active_threads_count_temp = Thread.activeCount();  
	      	Thread th[] = new Thread[active_threads_count_temp];								// returns the number of threads put into the array 
	      	Thread.enumerate(th); 
	      	for (int i = 0; i < active_threads_count_temp; i++) {
	      		for(int j=0;j<mram.wp.Num_Workloads;j++)
	      			try{
		      			if(th[i].getName()==mram.wp.workload_name[j]){
		      				if (!thread_names.contains(th[i])) {
		      	      			thread_names.add(th[i].getName());
		      			    } 
		      			}
		      		}
		      		catch(Exception e){
		      			System.out.println("Exception "+e+" happened.  But, jobs are still running");
		      		}
	      	} 
	      	//System.out.println("number of current active jobs "+thread_names.size());
			if(thread_names.size()==0){
				boolean end=true;
				while(end){
					int active_threads_count_temp1 = Thread.activeCount();  
					if (2==active_threads_count_temp1){
						end=false;
					}
				}
				System.out.println("  AM resource released");
				mram.release_AM_resources();
				job_status=false;
			}			
			else{ 
				find_fare_share(); 
				for(int i=0;i<thread_names.size();i++){  
					name=thread_names.get(i); 
					for(int j=0;j<mram.wp.workload_name.length;j++)						// finding running thread number 
						if(name==mram.wp.workload_name[j])
							workload_num=j;	 
					
					if(mram.mram_threads[workload_num].mam.map_flag){
						schedule_map_tasks(workload_num);
					}  
					if(mram.mram_threads[workload_num].mam.reduce_flag){  
						schedule_reduce_tasks(workload_num);
					}
				} 
			} 
		}  
	}  
		
	public void find_fare_share(){   
		shared_vCPU=shared_Mem=0;
		for(int i=0;i<mram.vm.VM_Matrix.length;i++){ 
			shared_vCPU+=mram.vm.VM_Matrix[i][1];
			shared_Mem+=mram.vm.VM_Matrix[i][2]; 
		} 
		//System.out.println("current total vCPU "+shared_vCPU);
		//System.out.println("current total vMemory "+shared_Mem);
		shared_vCPU=Math.floor(shared_vCPU/thread_names.size());
		shared_Mem=Math.floor(shared_Mem/thread_names.size());
		//System.out.println("current shared vcpu is "+shared_vCPU);
		//System.out.println("current shared memory is "+shared_Mem);
	}  
	
	public void schedule_map_tasks(int workload_num){  
		double temp_vCPU,temp_vMem;
		temp_vCPU=temp_vMem=0;
		for(int j=0;j<mram.wp.ba.blck[workload_num].Block_locations.length;j++){						// checking data locality
			if(mram.mram_threads[workload_num].mam.Map_status[j][2]==0){
				int temp2=0;
				two: for(int k=0;k<mram.wp.RF;k++){ 
					int temp1=(int)mram.wp.ba.blck[workload_num].Block_locations[j][k*4+1]-1;			// VM number (where 3 copies avaialble), as i use 1..n indexing i do -1 for array indexing
					if(temp_vCPU<=shared_vCPU && temp_vMem <= shared_Mem){								// data local execution
						if(mram.wp.MR_RR[workload_num][0]<=mram.vm.VM_Matrix[temp1][1] && mram.wp.MR_RR[workload_num][1]<=mram.vm.VM_Matrix[temp1][2]){
							temp_vCPU+=mram.wp.MR_RR[workload_num][0];
							temp_vMem+=mram.wp.MR_RR[workload_num][1];  
							mram.mram_threads[workload_num].mam.Map_status[j][1]=0;						// local execution (0) 
							mram.mram_threads[workload_num].mam.Map_status[j][2]=1;						// status(not_yet_started(0),running(1)/finished(2)) 
							mram.mram_threads[workload_num].mam.Map_status[j][3]=mram.t.getTime();		// task response time
							new Map_Task_Execution(mram,workload_num,j,temp1, "map task",0);			// launch the task and update the status  
							break two;
						} 
						else
							temp2++;   
					} 
				}
				if(temp2==mram.wp.RF){															// non-local execution (after 3 times tried) 
				//	for(;;){
						int z_temp=(mram.r.Gen_Ran_Nos(1, mram.vm.VM_Matrix.length, 1)[0])-1;
						if(mram.wp.MR_RR[workload_num][0]<=mram.vm.VM_Matrix[z_temp][1] && mram.wp.MR_RR[workload_num][1]<=mram.vm.VM_Matrix[z_temp][2]){
							temp_vCPU+=mram.wp.MR_RR[workload_num][0];
							temp_vMem+=mram.wp.MR_RR[workload_num][1];  
							mram.mram_threads[workload_num].mam.Map_status[j][1]=1;					// non-local execution (1) 
							mram.mram_threads[workload_num].mam.Map_status[j][2]=1;						// status(not_yet_started(0),running(1)/finished(2)) 
							mram.mram_threads[workload_num].mam.Map_status[j][3]=mram.t.getTime();	// task response time
							new Map_Task_Execution(mram,workload_num,j,z_temp, "map task",2000);	// launch the map task non-locally with 2 minutes delay and update the map task status  
							//break;
						}
					//}
				}
			}
		}  
	}
	
	public void schedule_reduce_tasks(int workload_num){ 
		double temp_vCPU,temp_vMem;
		temp_vCPU=temp_vMem=0;
		if(mram.wp.No_Tasks_MR[workload_num][1]!=0){	  
			for(int j=0;j<mram.wp.No_Tasks_MR[workload_num][1];j++){	
				if(mram.mram_threads[workload_num].mam.Reduce_status[j][2]==0 && temp_vCPU<=shared_vCPU && temp_vMem <= shared_Mem){ 
					//one: for(;;){ 
						int z_temp=(mram.r.Gen_Ran_Nos(1, mram.vm.VM_Matrix.length, 1)[0])-1;
						if(mram.wp.MR_RR[workload_num][2]<=mram.vm.VM_Matrix[z_temp][1] && mram.wp.MR_RR[workload_num][3]<=mram.vm.VM_Matrix[z_temp][2]){ 
							temp_vCPU+=mram.wp.MR_RR[workload_num][2];
							temp_vMem+=mram.wp.MR_RR[workload_num][3];   
							mram.mram_threads[workload_num].mam.Reduce_status[j][3]=mram.t.getTime();		// task response time (first two fields already filled in MR_App_Master class)
							mram.mram_threads[workload_num].mam.Reduce_status[j][2]=1;						// status(not_yet_started(0),running(1)/finished(2)) 
							new Reduce_Task_Execution(mram,workload_num,j,z_temp, "reduce task");			// launch the task and update the status  
							//break one;
						}
					//}
				}
			}
		} 
	}
}

 
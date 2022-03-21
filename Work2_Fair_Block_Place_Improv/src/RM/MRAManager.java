package RM;

import java.io.IOException;

import Miscellaneous.*;
import VMs.*;
import Workloads.*;

public class MRAManager {
	public Random_Gen r=new Random_Gen(); 
	public Display d=new Display();
	public VM_Parameters vm;
	public Workload_Parameters wp;	 
	public Scheduler fs=new Scheduler();
	public Write_To_File w; 
	public Time_Taken t=new Time_Taken();
	public Write_Resource_Usage_Status wrs=new Write_Resource_Usage_Status();
	public Write_MR_Job_Status wjs=new Write_MR_Job_Status();
	
	public MR_App_Master_Threads mram_threads[]=new MR_App_Master_Threads[6]; 
	public double AM_Res_Allocated[][];
	public int x;
	public int AM_conf[]={1,1,0};	
		
	public MRAManager(VM_Parameters vm,Workload_Parameters wp,Write_To_File w){
		this.vm=vm;
		this.wp=wp; 
		this.w=w;
		AM_Res_Allocated=new double[wp.Num_Workloads][6];					// vCPU, memory, MR_App_Master is running (1) or not running (0),  VM #, PM #, rack #
	}	
	
//=====================================================================================================================================================
	
	public void create_MRAMaster()throws IOException{
		int temp;
		for(int i=0;i<wp.Num_Workloads;i++){
			temp=1000; 
			mram_threads[i] =new MR_App_Master_Threads(wp,vm,i,w);
			//System.out.println("thread created in MRAM is "+mram_threads[i].thread_name);
			
			//assigning resources to application master
			while(temp!=0){		
				x=r.Gen_Ran_Nos(0, vm.VM_Matrix.length-1, 1)[0]; 
				if(vm.VM_Matrix[x][1]>AM_conf[0] && vm.VM_Matrix[x][2]>AM_conf[1]){ 
					for(int j=0;j<AM_conf.length-1;j++){
						vm.VM_Matrix[x][j+1]-=AM_conf[j];
						vm.VM_Matrix[x][j+AM_conf.length+1]+=AM_conf[j];  
						temp=0;  
					}
					for(int k=0;k<AM_conf.length-1;k++){
						AM_Res_Allocated[i][k]=AM_conf[k]; 			
						AM_Res_Allocated[i][k+AM_conf.length+1]=vm.VM_Matrix[x][k+(AM_conf.length*2)+2];			// which PM and Rack
					}
					AM_Res_Allocated[i][2]=1; 																	// MRAppMaster is up and running
					AM_Res_Allocated[i][3]=x+1; 
				}
			}
			//if(i==0)
				//break;
		}  
		
		//d.display_2D(AM_Res_Allocated, "MR App Master resource allocation  (vCPU, memory, MR_App_Master is running (1) or not running (0),  VM #, PM #, rack #) ");
		wrs.write_2D(AM_Res_Allocated, "MR App Master resource allocation  (vCPU, memory, MR_App_Master is running (1) or not running (0),  VM #, PM #, rack #) ");
		//d.display_2D(vm.VM_Matrix, "VM allocation matrix  after MRAM started   (VM #, resource allocated to each VM (vCPU, memory, storage), resource wasted in each VM (vCPU, memory, storage), which flavour, VM in which PM, number of blocks stored in a VM (each block is 0.125) )");   
		wrs.write_2D(vm.VM_Matrix, "VM allocation matrix  after MRAM started (VM #, resource allocated to each VM (vCPU, memory, storage), resource wasted in each VM (vCPU, memory, storage), which flavour, VM in which rack, VM in which PM, number of blocks stored in a VM (each block is 0.125) )");
						
		fs.schedule_tasks(this);   							// calling fair scheduler passing wp, vm, thread details of mram
	} 
	
	void release_AM_resources()throws IOException{
		for(int k=0;k<AM_Res_Allocated.length;k++){ 
			vm.VM_Matrix[(int)AM_Res_Allocated[k][3]-1][1]+=AM_Res_Allocated[k][0];
			vm.VM_Matrix[(int)AM_Res_Allocated[k][3]-1][2]+=AM_Res_Allocated[k][1]; 
			vm.VM_Matrix[(int)AM_Res_Allocated[k][3]-1][4]-=AM_Res_Allocated[k][0];
			vm.VM_Matrix[(int)AM_Res_Allocated[k][3]-1][5]-=AM_Res_Allocated[k][1]; 
			AM_Res_Allocated[k][2]=0; 
		}  
		wrs.write_2D(AM_Res_Allocated, "MR App Master resource allocation in the end (vCPU, memory, MR_App_Master is running (1) or not running (0),  VM #, PM #, rack #) "); 
	}
}

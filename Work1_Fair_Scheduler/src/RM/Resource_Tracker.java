package RM;

import java.io.IOException;

import Miscellaneous.Resource_Usage_Tracker; 

public class Resource_Tracker implements Runnable{
	public int commit=0; 
	public int commit_time=10000; 
	public MRAManager mram;  
	public Resource_Usage_Tracker rus=new Resource_Usage_Tracker();  
	Scheduler sf;
	
	public Thread t;
	
	public Resource_Tracker(MRAManager mram, Scheduler sf){
		this.mram=mram; 
		this.sf=sf;
		rus.write_string("Total resource allocated for Hadoop clsuter \t"+mram.vm.total_MR_vcpu+" vCPU \t"+ mram.vm.total_MR_mem + " GB memory");
		rus.write_string("Resources (vCPU, Memory) unused in every commit ("+commit_time+") milli seconds");
		//list = new ArrayList[mram.vm.VM_Matrix.length*mram.wp.Num_Workloads];  
		t=new Thread(this,"Resource tracker");
		t.start();
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		boolean flag=true;
		while(flag){
			try{
	           Thread.sleep((long)commit_time);										// it should be here only to give time to create job threads
			}
			catch(Exception err){
	            err.printStackTrace();
			}
			find_tot_resource();				
			try {
				sf.wrus.write_2D(mram.vm.VM_Matrix, "after "+ commit + " commit, VM matrix is (VM #, resource allocated to each VM (vCPU, memory, storage), resource used in each VM (vCPU, memory, storage), which flavour, VM in which PM, and VM in which rack, # blocks stored (each block is 0.125))");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int active_threads_count_temp = Thread.activeCount();  
			if(active_threads_count_temp==1)
				flag=false;
		}
	}
	
	public void find_tot_resource(){
		double tot_vcpu=0,tot_mem=0;
		for(int con=0;con<mram.vm.VM_Matrix.length;con++){
			tot_vcpu+=mram.vm.VM_Matrix[con][4];
			tot_mem+=mram.vm.VM_Matrix[con][5];
		}	
		/*System.out.println("\nmram.vm.total_MR_vcpu  "+mram.vm.total_MR_vcpu);
		System.out.println("\nmram.vm.total_MR_mem  "+mram.vm.total_MR_mem);
		System.out.println("\ntot_vcpu  "+tot_vcpu);
		System.out.println("\ntot_mem  "+tot_mem);*/
		rus.write_string(commit++ +"\t"+(mram.vm.total_MR_vcpu-tot_vcpu)+"\t"+ (mram.vm.total_MR_mem-tot_mem));
	}
}

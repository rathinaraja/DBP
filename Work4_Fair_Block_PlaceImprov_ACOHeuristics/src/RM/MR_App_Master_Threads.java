package RM;

import java.io.IOException;

import Miscellaneous.Time_Taken;
import Miscellaneous.Write_To_File;
import VMs.*;
import Workloads.*;

public class MR_App_Master_Threads implements Runnable{
	public Thread t;
	public int workload_num;
	public String thread_name;
	public Workload_Parameters wp; 
	public VM_Parameters vm;
	public MR_App_Master mam; 
	Write_To_File w; 
	int job_start_time,job_end_time;
	Time_Taken time=new Time_Taken();
	
	public MR_App_Master_Threads(Workload_Parameters wp,VM_Parameters vm,int workload_num,Write_To_File w){
		this.wp=wp;
		this.vm=vm;
		this.workload_num=workload_num;
		this.w=w;
		thread_name=wp.workload_name[workload_num];
		t=new Thread(this,thread_name);
		//System.out.println("\n thread name :"+thread_name); 
		t.start();
	}

	@Override
	public void run(){
		// TODO Auto-generated method stub
		try {
			mam=new MR_App_Master(wp,vm,workload_num); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			job_start_time=time.getTime();	
			mam.start_job_threads();
			job_end_time=time.getTime(); 
			//System.out.println(wp.workload_name[workload_num]+" Job submission time  "+job_start_time);
			//System.out.println(wp.workload_name[workload_num]+" Job completion time  "+job_end_time); 
			w.write_string("\n\n==========================================="+wp.workload_name[workload_num].toUpperCase()+"==============================================================");
			w.write_string("\t\t"+wp.workload_name[workload_num]+" Job submission time  "+job_start_time+" seconds");
			w.write_string("\t\t"+wp.workload_name[workload_num]+" Job completion time  "+job_end_time+" seconds");
			//System.out.println("\nLatency of "+ wp.workload_name[workload_num] +" job is "+Math.abs((job_end_time-job_start_time))+ " seconds");
			w.write_string("\t\tLatency of "+ wp.workload_name[workload_num] +" job is "+Math.abs((job_end_time-job_start_time))+ " seconds" ); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println( "test iniside MR_App_Master accessing a thread: " + mam.Map_status[0][0]);  
	} 
	
}

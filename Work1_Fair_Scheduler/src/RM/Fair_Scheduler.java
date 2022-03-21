package RM;
import java.io.File;
import java.io.IOException;

import Miscellaneous.*;
import VMs.*; 
import Workloads.*; 

public class Fair_Scheduler {
	public static void main(String args[])throws IOException{ 
		// CDC generation with Racks, servers, virtual machines
		int batch_start_time,batch_end_time;
		File file = new File("1.MR_Config_Status.txt"); 
		file.delete();
		File file2 = new File("2.MR_Job_Status.txt"); 
		file2.delete(); 
		File file3 = new File("3.Resource_Usage_Status.txt"); 
		file3.delete(); 
		File file4 = new File("4.Resource_Tracker.txt"); 
		file4.delete(); 
		
		Time_Taken t=new Time_Taken();
		Write_To_File w=new Write_To_File();   
		VM_Parameters vm=new VM_Parameters(w);	
		
		//Step 1: get VM details
		vm.basic_config_display();
		vm.VM_Flavors_display(); 
		vm.Placing_Hadoop_VMs();  
		vm.VM_PM_Rack_map();
		vm.VM_Flav_Mapping();
//=====================================================================================================================================================
		
		// Workload generation: number of workloads, its map and reduce tasks and its status, HDFS activity like block allocations
	    Workload_Parameters wp=new Workload_Parameters(vm,w);  
		//Step 2: workload details 
		wp.worklod_config_display();  
//=====================================================================================================================================================
	    
		// MapReduce Applications manager: creating one MRAppMaster for each workload and tasks status creation		
		batch_start_time=t.getTime();											// batch submission time
		System.out.println("\nBatch is submitted"); 
		MRAManager mam=new MRAManager(vm,wp,w);	
		mam.create_MRAMaster(); 
		batch_end_time=t.getTime();												// batch finishing time 
		System.out.println("\nBatch submission time  "+batch_start_time+" seconds");
		System.out.println("\nBatch completion time  "+batch_end_time+" seconds"); 
		w.write_string("\n\n====================================================================BATCH================================================================================");
		w.write_string("\nBatch submission time  "+batch_start_time);
		w.write_string("\nBatch completion time  "+batch_end_time);
		System.out.println("\nMakespan of this batch is "+Math.abs((batch_end_time-batch_start_time))+ " seconds");
		w.write_string("\nMakespan of this batch is "+Math.abs((batch_end_time-batch_start_time))+ " seconds"  ); 
		w.write_string("\n============================================================================================================================================================");
		 //=====================================================================================================================================================		
		//w.file_close();
	} 
}

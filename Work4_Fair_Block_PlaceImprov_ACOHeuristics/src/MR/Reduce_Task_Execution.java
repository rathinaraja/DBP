package MR;

import RM.*; 

public class Reduce_Task_Execution implements Runnable{
	public MRAManager mram;
	public int workload_num,vm_no;
	public int reduce_task_no;											 
	public String thread_name;
	public MR_App_Master temp;
	public Thread t; 
	
	public Reduce_Task_Execution(MRAManager mram, int workload_num,int reduce_task_no,int vm_no, String str){
		this.mram=mram;
		this.workload_num=workload_num;
		this.reduce_task_no=reduce_task_no;
		this.vm_no=vm_no; 
		
		thread_name=mram.wp.workload_name[workload_num]+" "+reduce_task_no+" "+str;
		t=new Thread(this,thread_name);
		//System.out.println("\n thread name :"+thread_name);
		temp=mram.mram_threads[workload_num].mam; 
		temp.Reduce_status[reduce_task_no][7]=vm_no+1;								// VM number
		temp.Reduce_status[reduce_task_no][8]=temp.vm.VM_Matrix[vm_no][7];			// VM number
		temp.Reduce_status[reduce_task_no][9]=temp.vm.VM_Matrix[vm_no][8];			// PM number
		temp.Reduce_status[reduce_task_no][10]=temp.vm.VM_Matrix[vm_no][9]; 		// rack number   
		
		mram.vm.VM_Matrix[vm_no][1]-=mram.wp.MR_RR[workload_num][2];			// reduce total resource of that vm 
		mram.vm.VM_Matrix[vm_no][2]-=mram.wp.MR_RR[workload_num][3];
		mram.vm.VM_Matrix[vm_no][4]+=mram.wp.MR_RR[workload_num][2];			// increase used resources in a VM
		mram.vm.VM_Matrix[vm_no][5]+=mram.wp.MR_RR[workload_num][3];
		
		t.start();
	}
	public void run(){
		// TODO Auto-generated method stub 
		//System.out.println("enters task execution");
		temp.Reduce_status[reduce_task_no][4]=mram.t.getTime();						// task start time 
		temp.Reduce_status[reduce_task_no][2]=1;									// status(not_yet_started(0),running(1)/finished(2)) 
		try {  			 
			Thread.sleep((long) (mram.wp.MR_Tasks_Latency[workload_num][1]*1000)+((long)(temp.Reduce_status[reduce_task_no][1])/30)*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		temp.Reduce_status[reduce_task_no][5]=mram.t.getTime();						// task end time
		temp.Reduce_status[reduce_task_no][6]=temp.Reduce_status[reduce_task_no][5]-temp.Reduce_status[reduce_task_no][4];  // task start time 
		temp.Reduce_status[reduce_task_no][2]=2;									// status(not_yet_started(0),running(1)/finished(2)) 
		//System.out.println("exits task execution");
		mram.vm.VM_Matrix[vm_no][1]+=mram.wp.MR_RR[workload_num][2];				// releasing resource of reduce task
		mram.vm.VM_Matrix[vm_no][2]+=mram.wp.MR_RR[workload_num][3];
		mram.vm.VM_Matrix[vm_no][4]-=mram.wp.MR_RR[workload_num][2];				// releasing resource of reduce task
		mram.vm.VM_Matrix[vm_no][5]-=mram.wp.MR_RR[workload_num][3]; 
	} 
}

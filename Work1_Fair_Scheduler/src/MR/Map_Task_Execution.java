package MR;

import java.text.DecimalFormat; 
import RM.*; 

public class Map_Task_Execution implements Runnable{
	public MRAManager mram;
	public int workload_num,vm_no;
	public int block_no;					// also map task number because for every one block one map task is launched
	public String thread_name;
	public MR_App_Master temp;
	public Thread t;
	public long non_local_exe_delay;
	public DecimalFormat df = new DecimalFormat(".##"); 
	
	public Map_Task_Execution(MRAManager mram, int workload_num,int block_no,int vm_no, String str, long delay){
		this.mram=mram;
		this.workload_num=workload_num;
		this.block_no=block_no;
		this.vm_no=vm_no;
		non_local_exe_delay=delay;
		
		thread_name=mram.wp.workload_name[workload_num]+" "+block_no+" "+str;
		t=new Thread(this,thread_name);
		//System.out.println("\n thread name :"+thread_name);
		temp=mram.mram_threads[workload_num].mam;
		temp.Map_status[block_no][0]=block_no+1;						// map task (number) on block	
		temp.Map_status[block_no][7]=vm_no+1;							// VM number 
		temp.Map_status[block_no][8]=temp.vm.VM_Matrix[vm_no][7];		// VM number
		temp.Map_status[block_no][9]=temp.vm.VM_Matrix[vm_no][8];		// PM number
		temp.Map_status[block_no][10]=temp.vm.VM_Matrix[vm_no][9]; 		// rack number  
		
		temp.Map_Output[block_no][0]=block_no+1;						// map task (number) on block	
		temp.Map_Output[block_no][1]=vm_no+1;							// VM number
		temp.Map_Output[block_no][2]=temp.vm.VM_Matrix[vm_no][8];		// VM number
		temp.Map_Output[block_no][3]=temp.vm.VM_Matrix[vm_no][8];		// PM number
		temp.Map_Output[block_no][4]=temp.vm.VM_Matrix[vm_no][9]; 		// rack number   
		
		mram.vm.VM_Matrix[vm_no][1]-=mram.wp.MR_RR[workload_num][0];			// reduce total resource of that vm 
		mram.vm.VM_Matrix[vm_no][2]-=mram.wp.MR_RR[workload_num][1];
		mram.vm.VM_Matrix[vm_no][4]+=mram.wp.MR_RR[workload_num][0];			// increase used resources in a VM
		mram.vm.VM_Matrix[vm_no][5]+=mram.wp.MR_RR[workload_num][1];
		
		t.start();
	}
	public void run(){
		// TODO Auto-generated method stub 
		//System.out.println("enters task execution");
		temp.Map_status[block_no][4]=mram.t.getTime();					// task start time 
		temp.Map_status[block_no][2]=1;									// status(not_yet_started(0),running(1)/finished(2)) 
		try { 
			//System.out.println("running time of "+mram.wp.workload_name[workload_num]+" is "+ mram.wp.MR_Tasks_Latency[workload_num][0]+ " seconds");
			if(mram.wp.No_Reduce_tasks[workload_num]!=0){				// produce map output for each reduce tasks in that particular workload
				double ran,temp_change=0; 
				double temp_size=mram.wp.block_size/mram.wp.No_Tasks_MR[workload_num][1]; 
				double start=temp_size/2;
				double end=temp_size+start;  
				
				for(int output=4;output<temp.Map_Output[0].length;output++){ 
					ran=Double.parseDouble(df.format(mram.r.Gen_Map_Output(start,end)));
					if(temp_change<=mram.wp.block_size+10){
						temp.Map_Output[block_no][output]=ran;								
						temp_change+=ran;
					}
					else
						temp.Map_Output[block_no][output]=end*Math.random();
				}
			}
			//System.out.println(mram.wp.workload_name[workload_num]+ "latency is " + mram.wp.MR_Tasks_Latency[workload_num][0]); 
			Thread.sleep((long) (mram.wp.MR_Tasks_Latency[workload_num][0]*1000)+non_local_exe_delay);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		temp.Map_status[block_no][5]=mram.t.getTime();					// task end time
		temp.Map_status[block_no][6]=temp.Map_status[block_no][5]-temp.Map_status[block_no][4];  // latency
		temp.Map_status[block_no][2]=2;									// status(not_yet_started(0),running(1)/finished(2)) 
		//System.out.println("exits task execution");
		mram.vm.VM_Matrix[vm_no][1]+=mram.wp.MR_RR[workload_num][0];	// releasing resource of map task
		mram.vm.VM_Matrix[vm_no][2]+=mram.wp.MR_RR[workload_num][1];
		mram.vm.VM_Matrix[vm_no][4]-=mram.wp.MR_RR[workload_num][0];	// releasing resource of map task
		mram.vm.VM_Matrix[vm_no][5]-=mram.wp.MR_RR[workload_num][1]; 
	} 
}

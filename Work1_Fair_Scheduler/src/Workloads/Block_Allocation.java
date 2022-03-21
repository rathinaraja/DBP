package Workloads;

import java.io.IOException;
import java.util.*;  
import Miscellaneous.*;
import VMs.*; 

public class Block_Allocation { 	
	public Block_Allocation blck[];						// 1 object for each workload
	public double Block_locations[][];  				// block #, copy1(VM #, PM #, Rack #), copy2(VM #, PM #, Rack #), copy3(VM #, PM #, Rack #)
	
	public Display d=new Display();
	public Random_Gen r=new Random_Gen();
	public VM_Parameters vm;
	public Workload_Parameters wp;
	public Write_To_File w;
	
	public Block_Allocation(){
		
	}
	
	public Block_Allocation(VM_Parameters vm,Write_To_File w,Workload_Parameters wp){
		this.vm=vm;
		this.w=w;
		this.wp=wp;
	}

//=====================================================================================================================================================
	
	//can do block load balancing among VMs following the rack awareness
	
	public void block_placement()throws IOException{
		int counter1,counter2,counter3; 
		blck=new Block_Allocation[wp.Num_Workloads];							// 1 object for each workload
		
		for(int i=0;i<wp.Num_Workloads;i++){ 
			counter1=r.Gen_Ran_Nos(1,vm.VMs, 1)[0];								// start placing block from a random VM, from there continuously
			counter2=r.Gen_Ran_Nos(1,vm.VMs, 1)[0];	
			counter3=r.Gen_Ran_Nos(1,vm.VMs, 1)[0];	
			blck[i]=new Block_Allocation();
			blck[i].Block_locations=new double[wp.Num_blocks[i]][wp.RF*4+1];		// block #, copy1(VM #, VM Flav #, PM #, Rack #), copy2(VM #, VM Flav #, PM #, Rack #), copy3(VM #, VM Flav #, PM #, Rack #)
			boolean flag=false;
			for(int j=0;j<wp.Num_blocks[i];j++){	
					// first copy of the block
					flag=true;
					while(flag){
						if(vm.VM_Matrix[counter1 % vm.VMs][3]>1){
							blck[i].Block_locations[j][0]=j+1; 
							blck[i].Block_locations[j][1]=(counter1 % vm.VMs)+1; 
							blck[i].Block_locations[j][2]=vm.VM_Matrix[counter1 % vm.VMs][7];
							blck[i].Block_locations[j][3]=vm.VM_Matrix[counter1 % vm.VMs][8]; 
							blck[i].Block_locations[j][4]=vm.VM_Matrix[counter1 % vm.VMs][9];
							vm.VM_Matrix[counter1 % vm.VMs][3]=vm.VM_Matrix[counter1 % vm.VMs][3]-0.125;				// 0.125 is a block
							vm.VM_Matrix[counter1 % vm.VMs][6]=vm.VM_Matrix[counter1 % vm.VMs][6]+0.125;				// update storage used in VMs
							vm.VM_Matrix[counter1 % vm.VMs][10]+=1;	 													// pointer to count number of blocks in a VM
							counter1++; 
							flag=false;
						}
						else
							counter1++;
					}
					
					flag=true;
					// second copy of the block
					while(flag){
						if(vm.VM_Matrix[counter2 % vm.VMs][3]>1){
							blck[i].Block_locations[j][5]=(counter2 % vm.VMs)+1;
							blck[i].Block_locations[j][6]=vm.VM_Matrix[counter2 % vm.VMs][7];
							blck[i].Block_locations[j][7]=vm.VM_Matrix[counter2 % vm.VMs][8];
							blck[i].Block_locations[j][8]=vm.VM_Matrix[counter2 % vm.VMs][9]; 
							vm.VM_Matrix[counter2 % vm.VMs][3]=vm.VM_Matrix[counter2 % vm.VMs][3]-0.125;				// 0.125 is a block
							vm.VM_Matrix[counter2 % vm.VMs][6]=vm.VM_Matrix[counter2 % vm.VMs][6]+0.125;				// update storage used in VMs
							vm.VM_Matrix[counter2 % vm.VMs][10]+=1;	 													// pointer to count number of blocks in a VM
							counter2++;
							flag=false;
						}
						else
							counter2++;
					}
					
					flag=true;
					// third copy of the block
					while(flag){
						if(vm.VM_Matrix[counter3 % vm.VMs][3]>1){
							blck[i].Block_locations[j][9]=(counter3 % vm.VMs)+1; 
							blck[i].Block_locations[j][10]=vm.VM_Matrix[counter3 % vm.VMs][7];
							blck[i].Block_locations[j][11]=vm.VM_Matrix[counter3 % vm.VMs][8];
							blck[i].Block_locations[j][12]=vm.VM_Matrix[counter3 % vm.VMs][9]; 
							vm.VM_Matrix[counter3 % vm.VMs][3]=vm.VM_Matrix[counter3 % vm.VMs][3]-0.125;				// 0.125 is a block
							vm.VM_Matrix[counter3 % vm.VMs][6]=vm.VM_Matrix[counter3 % vm.VMs][6]+0.125;				// update storage used in VMs
							vm.VM_Matrix[counter3 % vm.VMs][10]+=1;	 													// pointer to count number of blocks in a VM
							counter3++;
							flag=false;
						}
						else
							counter3++;
					} 
			}  
		}  
	}
}

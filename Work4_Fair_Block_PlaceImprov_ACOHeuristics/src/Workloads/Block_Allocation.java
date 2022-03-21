package Workloads;

import java.io.IOException;
import java.text.DecimalFormat; 
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
	public DecimalFormat df = new DecimalFormat(".###");
	
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
		int pointer[]=new int[vm.num_flavors];  
		
		blck=new Block_Allocation[wp.Num_Workloads];							// 1 object for each workload
		double vm_percent[]=new double[vm.num_flavors];
		double block_vm_percent[]=new double[vm.num_flavors];
		double sum=0;
		for(int z=0;z<vm.num_flavors;z++)
			sum+=vm.VM_Flavs[z][0]; 
		for(int z=0;z<vm.num_flavors;z++){
			vm_percent[z]=Double.parseDouble(df.format(vm.VM_Flavs[z][0]/sum)); 
		}
		
		w.write_string("=====================================================================================================================================================");
		//System.out.println("Block Allocation Scheme based on VM capacity");
		w.write_string("Block Allocation Scheme based on VM capacity");
		w.write_string("=====================================================================================================================================================");
	
		for(int i=0;i<wp.Num_Workloads;i++){  
			int tot_blocks=0;
			int block_no=0;
			blck[i]=new Block_Allocation();
			blck[i].Block_locations=new double[wp.Num_blocks[i]][wp.RF*4+1];		// block #, copy1(VM #, Flaver #, PM #, Rack #), copy2(VM #, Flaver #, PM #, Rack #), copy3(VM #, Flaver #, PM #, Rack #)
			int total_blocks=wp.Num_blocks[i]*3,temp=0; 
			for(int z=0;z<vm.num_flavors;z++){
				block_vm_percent[z]=Math.floor(Double.parseDouble(df.format(vm_percent[z]*total_blocks)));
				temp+=block_vm_percent[z]; 
			} 
			int diff=total_blocks-temp; 
			block_vm_percent[vm.num_flavors-1]+=diff;
			
			//System.out.println("Total number of "+wp.workload_name[i]+" job blocks (with RF 3) are "+wp.Num_blocks[i]+" The split up ratio among different flavors are :");
			w.write_string("Total number of "+wp.workload_name[i]+" job blocks (with RF 3) are "+wp.Num_blocks[i]*3+". The split up ratio among different flavors are :");
			for(int j=0;j<block_vm_percent.length;j++){
				//System.out.print("\t "+block_vm_percent[j]);
				w.write_string("\t\t"+ block_vm_percent[j]+" blocks are loaded in VM Flavour "+(j+1));
			} 
			w.write_string(" ");
			//System.out.println("");
			
			for(int j=block_vm_percent.length;j>0;j--){  
				//System.out.println("j "+j);
				two: for(int temp_count=0;temp_count<Math.ceil(block_vm_percent[j-1]/wp.RF);temp_count++){
					block_no++;
					if(tot_blocks==total_blocks) {
						break two;
					}
					else{
						int temp1=(vm.VM_Flav_Mapping_Data[j-1][pointer[j-1]++ % vm.each_flavor+1]-1);
						if(vm.VM_Matrix[temp1][3]>1){
							blck[i].Block_locations[block_no-1][0]=block_no; 
							blck[i].Block_locations[block_no-1][1]=temp1+1; 
							blck[i].Block_locations[block_no-1][2]=vm.VM_Matrix[(int)blck[i].Block_locations[block_no-1][1]-1][7];
							blck[i].Block_locations[block_no-1][3]=vm.VM_Matrix[(int)blck[i].Block_locations[block_no-1][1]-1][8];
							blck[i].Block_locations[block_no-1][4]=vm.VM_Matrix[(int)blck[i].Block_locations[block_no-1][1]-1][9];   
							vm.VM_Matrix[(int)temp1][3]=vm.VM_Matrix[(int)temp1][3]-0.125;											// 0.125 is a block
							vm.VM_Matrix[(int)temp1][6]=vm.VM_Matrix[(int)temp1][6]+0.125; 											// update storage used in VMs
							vm.VM_Matrix[(int)temp1][10]+=1;																		// pointer to count number of blocks in a VM								
							tot_blocks++; 
						}
					}
					if(tot_blocks==total_blocks) {
						break two;
					}
					else {
						int temp1=(vm.VM_Flav_Mapping_Data[j-1][pointer[j-1]++ % vm.each_flavor+1]-1); 
						if(vm.VM_Matrix[temp1][3]>1){ 
							blck[i].Block_locations[block_no-1][5]=temp1+1;
							blck[i].Block_locations[block_no-1][6]=vm.VM_Matrix[(int)blck[i].Block_locations[block_no-1][5]-1][7];
							blck[i].Block_locations[block_no-1][7]=vm.VM_Matrix[(int)blck[i].Block_locations[block_no-1][5]-1][8];
							blck[i].Block_locations[block_no-1][8]=vm.VM_Matrix[(int)blck[i].Block_locations[block_no-1][5]-1][9];
							vm.VM_Matrix[(int)temp1][3]=vm.VM_Matrix[(int)temp1][3]-0.125;											// 0.125 is a block
							vm.VM_Matrix[(int)temp1][6]=vm.VM_Matrix[(int)temp1][6]+0.125;  										// update storage used in VMs
							vm.VM_Matrix[(int)temp1][10]+=1;																		// pointer to count number of blocks in a VM
							tot_blocks++;  
						} 
					}
					if(tot_blocks==total_blocks) {
						break two;
					}
					else {
						int temp1=(vm.VM_Flav_Mapping_Data[j-1][pointer[j-1]++ % vm.each_flavor+1]-1); 
						if(vm.VM_Matrix[temp1][3]>1){  
							blck[i].Block_locations[block_no-1][9]=temp1+1;
							blck[i].Block_locations[block_no-1][10]=vm.VM_Matrix[(int)blck[i].Block_locations[block_no-1][9]-1][7];
							blck[i].Block_locations[block_no-1][11]=vm.VM_Matrix[(int)blck[i].Block_locations[block_no-1][9]-1][8];  
							blck[i].Block_locations[block_no-1][12]=vm.VM_Matrix[(int)blck[i].Block_locations[block_no-1][9]-1][9]; 
							vm.VM_Matrix[(int)temp1][3]=vm.VM_Matrix[(int)temp1][3]-0.125;											// 0.125 is a block
							vm.VM_Matrix[(int)temp1][6]=vm.VM_Matrix[(int)temp1][6]+0.125;  										// update storage used in VMs
							vm.VM_Matrix[(int)temp1][10]+=1;																		// pointer to count number of blocks in a VM
							tot_blocks++;
						}
					}
				} 
			}
		}
	}
}


/*System.out.println("blck[i].Block_locations[block_no][9] "+blck[i].Block_locations[block_no-1][9]);
System.out.println("blck[i].Block_locations[block_no][10] "+blck[i].Block_locations[block_no-1][10]);
System.out.println("blck[i].Block_locations[block_no][11] "+blck[i].Block_locations[block_no-1][11]);
System.out.println("blck[i].Block_locations[block_no][12] "+blck[i].Block_locations[block_no-1][12]);*/
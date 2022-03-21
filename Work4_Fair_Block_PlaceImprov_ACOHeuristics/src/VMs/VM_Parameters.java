package VMs;

import Miscellaneous.*;

import java.io.IOException;
import java.util.*;
// i have created VMs in such a way that it is not overloading into any PMs. if group of VMs allocated beyond a PM capacity, then you have to rebalance it
// load balancing is not implemented yet
// so for my work i assume that there is no overallocation of VMs onto PMs

public class VM_Parameters {
	public int Racks=5;															// number of racks in Cloud Data Center (CDC)
	public int PMs=15;															// number of Physical Machines (PM) in each rack
	public int PM_conf[]={12,128,2000};											// PM configuration. we consider all PMs are of same configuration and performance. (cores (one socket) with hyper-threaded (so 24 logical cores), memory, HDD)
	public int VMs=100;															// number of Hadoop Virtual Machine (VM) 
	public int PM_resource_reserved[][]=new int[PMs][Racks*PM_conf.length];		// Row: each PM Column: every 3 columns is for 1 rack, each column one rack: total resource, used resource, wasted resource
	public int PM_resource_allocated[][]=new int[PMs][Racks*PM_conf.length]; 	// Row: each PM Column: every 3 columns is for 1 rack, each column one rack: total resource, used resource, wasted resource
	public int PM_resource_unused[][]=new int[PMs][Racks*PM_conf.length];  		// Row: each PM Column: every 3 columns is for 1 rack, each column one rack: total resource, used resource, wasted resource
	public double VM_Matrix[][]=new double[VMs][11];							// VM #, resource allocated to each VM (vCPU, memory, storage), resource used in each VM (vCPU, memory, storage), which flavour, VM in which PM, and VM in which rack, # blocks stored (each block is 0.125)
	public TreeMap<String,String> VM_PM_Map;									// Rack #, PM #, list of VMs
	
	public static double temp[]=new double[3];
	public double PMBW=10;														// each PM is connected to its TOR with 10 Gbps ethernet
	public double ToRBW=100;													// intra cluster bandwidth using TOR is 100 Gbps
	public double IRackBW=500;													// inter rack bandwidth is 500 Gbps
	public double CSBW=1000;													// core switch bandwidth is 1 Tbps
	
	public int num_flavors=5;
	public double VM_Flavs[][]={{1,2,20},{2,4,40},								// General purpose T type of AWS instances with small, medium, large, xlarge, 2x large burstable on demand instances
						 {4,8,80},{8,16,160},									// in (vCPU, memory, HDD) order  https://aws.amazon.com/ec2/instance-types/
						 {12,24,250}};
	public String str[]={"small","medium","large",
						"x large","2x large"}; 
	public int each_flavor=VMs/num_flavors;										// to show VM/bin heterogeneity i take 20 VMs from each flavours
	public int VM_assignment[][]=new int[PMs][Racks*(num_flavors+1)]; 			// Row: each PM, every 6 column is for 1 rack, each Column in one rack: Flavour1 #, Flavour2 #, Flavour3 #, Flavour4 #, Flavour5 #, Total VMs in PM  
	public int VM_Flav_Mapping_Data[][]=new int[num_flavors][each_flavor+1];
	public double total_MR_vcpu=0,total_MR_mem=0;
	
	Random_Gen r=new Random_Gen();
	Display d=new Display();
	public Write_To_File w;
//===================================================================================================================================================== 
	
	public VM_Parameters(Write_To_File w){
		 this.w=w;
	}
	
	public VM_Parameters(){ 
	}
	
	// display basic configuration of CDC 
	public void basic_config_display()throws IOException{											
		//System.out.println("=====================================================================================================================================================");
		w.write_string("=====================================================================================================================================================");
		
		//System.out.println("CDC DETAILS");															
		w.write_string("CDC DETAILS");
		
		//System.out.println("=====================================================================================================================================================");
		w.write_string("=====================================================================================================================================================");
		
		//System.out.println(" Number of racks in a Cloud Data Center (CDC) : "+Racks);
		w.write_string("Number of racks in a Cloud Data Center (CDC) : "+Racks);
		
		//System.out.println("\n Number of Physical Machines (PMs) in each rack : "+PMs);
		w.write_string("Number of Physical Machines (PMs) in each rack : "+PMs);
		
		//System.out.println("\nEach machine's configuration is : "+PM_conf[0]+" cores "+PM_conf[1]+"  GB memory "+PM_conf[2]+" GB storage");	
		w.write_string("Each machine's configuration is : "+PM_conf[0]+" cores "+PM_conf[1]+"  GB memory "+PM_conf[2]+" GB storage");
		
		//System.out.println("\n Overall cluster CDC (cluster) capacity is : "+ PM_conf[0]*2*PMs*Racks+" logical cores "+PM_conf[1]*PMs*Racks+" GB memory "+PM_conf[2]*PMs*Racks+" GB storage");
		w.write_string("Overall cluster CDC (cluster) capacity is : "+ PM_conf[0]*2*PMs*Racks+" logical cores "+PM_conf[1]*PMs*Racks+" GB memory "+PM_conf[2]*PMs*Racks+" GB storage");
		
		//System.out.println("\n Number of Hadoop VMs in CDC : "+VMs);
		w.write_string("Number of Hadoop VMs in CDC : "+VMs);
				
		//System.out.println("\n Physical Machine bandwidth : "+ PMBW+" Gbps");
		w.write_string("Physical Machine bandwidth : "+ PMBW+" Gbps");
		
		//System.out.println("\n Top of the rack switch bandwidth : "+ ToRBW+" Gbps");
		w.write_string("Top of the rack switch bandwidth : "+ ToRBW+" Gbps");
		
		//System.out.println("\n Inter-rack bandwidth : "+ IRackBW+" Gbps");
		w.write_string("Inter-rack bandwidth : "+ IRackBW+" Gbps");
		
		//System.out.println("\n Core switch bandwidth : "+ CSBW+" Gbps"); 
		w.write_string("Core switch bandwidth : "+ CSBW+" Gbps");
			
		for(int i=0;i<Racks;i++)
			for(int j=0;j<PMs;j++){ 
				PM_resource_reserved[j][i*(PM_conf.length)]=PM_conf[0];
				PM_resource_reserved[j][i*(PM_conf.length)+1]=PM_conf[1];
				PM_resource_reserved[j][i*(PM_conf.length)+2]=PM_conf[2];
			} 
	}
	
	// display VM falvours
	public void VM_Flavors_display(){ 											
		//System.out.println("====================================================================================================================================================="); 
		w.write_string("=====================================================================================================================================================");
		
		//System.out.println("VM FLAVOUR DETAILS (falvour name, vCPU, Memory(GB), Storage(GB))");															
		w.write_string("VM FLAVOUR DETAILS (falvour name, vCPU (real number of cores assigned=vCPU/2), Memory(GB), Storage(GB))");
		
		//System.out.println("====================================================================================================================================================="); 
		w.write_string("=====================================================================================================================================================");
		
		for(int i=0;i<num_flavors;i++){ 
			//System.out.println(str[i]+":\t"+VM_Flavs[i][0]+" logical cores (vCPUs) "+VM_Flavs[i][1]+" GB memory "+VM_Flavs[i][2]+" GB storage");	
			w.write_string(str[i]+":\t"+VM_Flavs[i][0]+" logical cores (vCPUs) "+VM_Flavs[i][1]+" GB memory "+VM_Flavs[i][2]+" GB storage");
		}
	}
	
	//place hadoop VMs in the CDC
	public void Placing_Hadoop_VMs()throws IOException{							 
		int rack[]=new int[1];													// racks number for each VM flavour
		int phys_machine[]=new int[1];											// PM number for each VM flavour in that rack  
		boolean allocated=true; 
		int VM_counter=0;
		
		for(int i=0;i<num_flavors;i++){
			for(int j=0;j<VMs/num_flavors;j++){									// equal number of different VMs				
				while(allocated){
					rack=r.Gen_Ran_Nos(0,Racks-1,1);
					phys_machine=r.Gen_Ran_Nos(0,PMs-1,1);
					if(PM_resource_reserved[phys_machine[0]][(rack[0]*3)+0]>=(int) VM_Flavs[i][0] && PM_resource_reserved[phys_machine[0]][(rack[0]*3)+1]>=(int) VM_Flavs[i][1] && PM_resource_reserved[phys_machine[0]][(rack[0]*3)+2]>=(int) VM_Flavs[i][2]){
						VM_assignment[phys_machine[0]][(num_flavors+1)*rack[0]+i]+=1; 			// mark specific VM flavor in each rack and PM
						VM_assignment[phys_machine[0]][(num_flavors+1)*rack[0]+5]+=1; 			// add the number of VMs in a PM (every sixth column)
						
						PM_resource_allocated[phys_machine[0]][(rack[0]*3)+0]=(int) VM_Flavs[i][0];
						PM_resource_allocated[phys_machine[0]][(rack[0]*3)+1]=(int) VM_Flavs[i][1];
						PM_resource_allocated[phys_machine[0]][(rack[0]*3)+2]=(int) VM_Flavs[i][2];
						
						VM_Matrix[VM_counter][0]=VM_counter+1;
						VM_Matrix[VM_counter][1]=(int) VM_Flavs[i][0];
						VM_Matrix[VM_counter][2]=(int) VM_Flavs[i][1];
						VM_Matrix[VM_counter][3]=(int) VM_Flavs[i][2];
						VM_Matrix[VM_counter][7]=i+1;
						VM_Matrix[VM_counter][8]=phys_machine[0]+1;
						VM_Matrix[VM_counter][9]=rack[0]+1;
						VM_counter++;
						allocated=false;
					}
				}
				allocated=true;
			}
		}
		for(int i=0;i<Racks;i++)
			for(int j=0;j<PMs;j++){ 
				int w=i*(PM_conf.length);
				PM_resource_unused[j][w]=PM_resource_reserved[j][w]-PM_resource_allocated[j][w];
				PM_resource_unused[j][w+1]=PM_resource_reserved[j][w+1]-PM_resource_allocated[j][w+1];
				PM_resource_unused[j][w+2]=PM_resource_reserved[j][w+2]-PM_resource_allocated[j][w+2];
			}
		
		w.write_2D(VM_assignment,"VM assignment for the user in CDC  (Row: each PM Columns: every 6 columns is for 1 rack, each Column in one rack: Flavour1 #, Flavour2 #, Flavour3 #, Flavour4 #, Flavour5 #, Total VMs in PM )");
		w.write_2D(PM_resource_reserved,"Available resources in PM in CDC  (Row: each PM Column: every 3 columns is for 1 rack, each column one rack: total resource, used resource, wasted resource)");
		w.write_2D(PM_resource_allocated,"Resource allocated at present in each PMs  (Row: each PM Column: every 3 columns is for 1 rack, each column one rack: total resource, used resource, wasted resource)");
		w.write_2D(PM_resource_unused,"Resource unused at present in each PMs  (Row: each PM Column: every 3 columns is for 1 rack, each column one rack: total resource, used resource, wasted resource)");
		
		//d.display_2D(VM_Matrix,"VM allocation matrix  (VM #, resource allocated to each VM (vCPU, memory, storage), resource used in each VM (vCPU, memory, storage), which flavour, VM in which PM, and VM in which rack, # blocks stored (each block is 0.125))");
		//System.out.println("vm allocation matrix number of vms "+vm.VM_Matrix.length);
		w.write_2D(VM_Matrix,"VM allocation matrix  (VM #, resource allocated to each VM (vCPU, memory, storage), resource used in each VM (vCPU, memory, storage), which flavour, VM in which PM, and VM in which rack, # blocks stored (each block is 0.125))");
			
		for(int con=0;con<VM_Matrix.length;con++){
			total_MR_vcpu+=VM_Matrix[con][1];
			total_MR_mem+=VM_Matrix[con][2]; 
		}
		//System.out.println("Total resource allocated for Hadoop clsuter\t"+tot1_vcpu+" vCPU\t"+tot1_mem+ " GB memory"); 
		w.write_string("=====================================================================================================================================================");
		//System.out.println("Rack #, PM #,  list of VMs");
		w.write_string("Total resource allocated for Hadoop clsuter \t"+total_MR_vcpu+" vCPU\t"+total_MR_mem+ " GB memory");
		//System.out.println("====================================================================================================================================================="); 
		w.write_string("=====================================================================================================================================================");

	}
	
	 
	// load balancing algorithm can be implemented here*/ 
	public void load_balancing()throws IOException{
		//d.display_2D(VM_Matrix,"VM allocation matrix  (VM #, resource allocated to each VM (vCPU, memory, storage), resource used in each VM (vCPU, memory, storage), which flavour, VM in which PM, and VM in which rack, # blocks stored (each block is 0.125))");
		//System.out.println("vm allocation matrix number of vms "+vm.VM_Matrix.length);
		//w.write_2D(VM_Matrix,"VM allocation matrix  (VM #, resource allocated to each VM (vCPU, memory, storage), resource used in each VM (vCPU, memory, storage), which flavour, VM in which PM, and VM in which rack, # blocks stored (each block is 0.125))");
	}
	
	public void VM_PM_Rack_map()throws IOException{
		String key,value,str;
		VM_PM_Map=new TreeMap<String,String>();
		for(int i=0;i<VM_Matrix.length;i++){
			key=VM_Matrix[i][9]+"\t"+VM_Matrix[i][8]+"\t";
			value=i+1+"\t";
			if(VM_PM_Map.containsKey(key)){
				String temp=VM_PM_Map.get(key);
				VM_PM_Map.put(key, temp+value); 
			}
			else
				VM_PM_Map.put(key, value);
		} 
		// displaying the map
		//System.out.println("====================================================================================================================================================="); 
		w.write_string("=====================================================================================================================================================");
		//System.out.println("Rack #, PM #,  list of VMs");
		w.write_string("Rack #, PM #,  list of VMs");
		//System.out.println("====================================================================================================================================================="); 
		w.write_string("=====================================================================================================================================================");
		
		Set<Map.Entry<String, String>> set = VM_PM_Map.entrySet();
		for(Map.Entry<String, String> me : set) {
			//System.out.print(me.getKey() + ": ");
			str=me.getKey() + ": ";
			//System.out.println(me.getValue());
			str+=me.getValue();
			w.write_string(str);
		}
	}
	
	public void VM_Flav_Mapping()throws IOException{
		int counter1,counter2,counter3,counter4,counter5;
		counter1=counter2=counter3=counter4=counter5=1;
		
		for(int i=0;i<num_flavors;i++)
			VM_Flav_Mapping_Data[i][0]=i+1;
		
		for(int i=0;i<VM_Matrix.length;i++){
			if(VM_Matrix[i][7]==1){
				VM_Flav_Mapping_Data[(int)VM_Matrix[i][7]-1][counter1]=(int)VM_Matrix[i][0];
				counter1++;
			}
			else if(VM_Matrix[i][7]==2){
				VM_Flav_Mapping_Data[(int)VM_Matrix[i][7]-1][counter2]=(int)VM_Matrix[i][0];
				counter2++;
			}
			else if(VM_Matrix[i][7]==3){
				VM_Flav_Mapping_Data[(int)VM_Matrix[i][7]-1][counter3]=(int)VM_Matrix[i][0];
				counter3++;
			}
			else if(VM_Matrix[i][7]==4){
				VM_Flav_Mapping_Data[(int)VM_Matrix[i][7]-1][counter4]=(int)VM_Matrix[i][0];
				counter4++;
			}
			else {
				VM_Flav_Mapping_Data[(int)VM_Matrix[i][7]-1][counter5]=(int)VM_Matrix[i][0];
				counter5++;
			}
		}
		//d.display_2D(VM_Flav_Mapping_Data,"VM flavor mapping (VM Flav #, list of VM numbers that belong to the specific flavor)");
		w.write_2D(VM_Flav_Mapping_Data,"VM flavor mapping (VM Flav #, list of VM numbers that belong to the specific flavor)");
	}
}

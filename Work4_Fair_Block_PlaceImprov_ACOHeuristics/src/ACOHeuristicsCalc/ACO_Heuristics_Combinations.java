package ACOHeuristicsCalc;
 
import java.text.DecimalFormat;
import java.util.*;

/*
======================================================================== 
possible map tasks of each workload in each VM flavor 
========================================================================
(Job/Workload #, VM Flav 1, VM Flav 2, VM Flav 3, VM Flav 4, VM Flav 5)
	1				1			2			4			8		12	
	2				1			2			4			8		12	
	3				1			2			4			8		12	
	4				0			1			2			4		6	
	5				1			2			4			8		12	
	6				0			1			2			4		6	
======================================================================= 
in VM Flavour 1 what are the combination of workloads can be placed, in such a way that, resource utilization can be increased?
in VM Flavour 2 what are the combination of workloads can be placed, in such a way that, resource utilization can be increased?
in VM Flavour 3 what are the combination of workloads can be placed, in such a way that, resource utilization can be increased?
in VM Flavour 4 what are the combination of workloads can be placed, in such a way that, resource utilization can be increased?
in VM Flavour 5 what are the combination of workloads can be placed, in such a way that, resource utilization can be increased?

run the following ACO algorithm to find possible combination for each VM flavour one by one.
*/

public class ACO_Heuristics_Combinations{
	VMs.VM_Parameters vm=new VMs.VM_Parameters();
//	Workloads.Workload_Parameters wp=new Workloads.Workload_Parameters();
	public DecimalFormat df = new DecimalFormat(".##");
	Workloads.Workload_Parameters wp=new Workloads.Workload_Parameters();
	
	//step 1 : control parameters initialization
	int design_var=wp.Num_Workloads;   							// number of design/decision variable
	int max=0;
	int no_ants=10;												// number ants also called as population or candidate solution
	int no_iter=50;
	float row=0.5f;
	int sigma=1;
	double pherome[][]; 
	
	double soln_space[][];   
	//int poss_soln[]={1,1,1,0,1,0};						// for VM flavor 1
	//int poss_soln[]={2,2,2,1,2,1};						// for VM flavor 2
	//int poss_soln[]={4,4,4,2,4,2};						// for VM flavor 3
	//int poss_soln[]={8,8,8,4,8,4};						// for VM flavor 4
	int poss_soln[]={12,12,12,6,12,6}; 						// // for VM flavor 5
	//while changing here change respective VM flavor in objective function evaluation function
	
	double path[][];
	double ants[][]=new double[no_ants][design_var*2];
	int fit=(int)(Math.pow(no_ants,design_var));
	double fitness[][]=new double[fit][design_var+1];
	double prob[][];
	double result[][]=new double[1][design_var+1];
	double fbest;  
	double vCPU_Ratio,Mem_Ratio;
	// to find design variable which has maximum number of possible solution
	ACO_Heuristics_Combinations(){  	
		for(int i=0;i<design_var;i++){
			if(max>poss_soln[i]){
				continue;
			}
			else{
				max=poss_soln[i];
			}
		}
		//System.out.println(" max no is  "+max);
		pherome=new double[max*4][design_var];							// i have multiplied max and poss_soln index with 4 to increase search/solution space. search and change if you want to change the solution space
		prob=new double[max*4][design_var];
		soln_space=new double[max*4][design_var];
		path=new double[max*4][design_var*2];
	} 
	// display all initialization variables
	void init_display(){
		System.out.println("number of design variables 	 "+ design_var); 
		System.out.print("range for each design variable: ");
		for(int i=0;i<poss_soln.length;i++)
			System.out.print(poss_soln[i]+"\t");
		System.out.println("\nnumber of ants               	 "+no_ants); 
		System.out.println("pheromone decay factor       	 "+row);
		System.out.println("pheromone error factor       	 "+sigma);  		
	}
	
	
	public static void main(String args[]){
		ACO_Heuristics_Combinations a=new ACO_Heuristics_Combinations();
		System.out.println("\n\n initialization of control parameters  \n");
		a.init_display();
		a.phero_init();
		System.out.println("\n\n pheromone initializaed\n");
		a.phero_display(); 
		a.sol_space();
		System.out.println("\n\n search or solution space for your problem \n ");
		a.sol_space_display();
		
		for(int gen=0;gen<a.no_iter;gen++){
			//a.cal_prob();
			//System.out.println("\n\n probability calculation for pheromone to construct path\n");
			//a.prob_display(); 
			a.path_cons();
			//System.out.println("\n\n constructed path for design variables\n");
			//a.path_display();			
			a.ants_generation();
			//System.out.println("\n\n ant colony/population/candidate solution  generation \n ");
			//a.ants_display();
			a.path_map();
			//System.out.println("\n\n ant afer mapping \n");
			//a.ants_display();
			//System.out.println("\n\n forming combination \n");
			a.form_combi();
			//a.combi_display();
			//System.out.println("\n\n objective function evaluation \n");
			a.obj_eval();
			//a.combi_display();
			//System.out.println("\n\n finding the best path \n\n\n");
			a.find_best();
			//System.out.println("\n\n fbest \n\n\n");
			//a.global_best_display();
			a.local_update();
			//System.out.println("\n\n after local pheromone update \n\n\n");
			//a.phero_display();	
			a.global_update();
			//System.out.println("\n\n after global pheromone update \n\n\n");
			//a.phero_display();	
		}
		//a.phero_display();
		System.out.println("\n\nGlobal best for the design variables with fitness value\n");
		a.global_best_display();
	}
	
	
	// search/solution space generation. each variable can take input between 0-30
	void sol_space(){
		Random randomno = new Random();
		for(int i=0;i<design_var;i++){  
			for(int k=0;k<poss_soln[i]*4;k++){ 
				soln_space[k][i]=(int)(randomno.nextInt(poss_soln[i]+1));
			}
		} 
	}	
				
	//search space input display
	void sol_space_display(){ 
		for(int i=0;i<soln_space.length;i++){ 
			for(int k=0;k<design_var;k++){ 
				System.out.print(soln_space[i][k]+"\t"); 
			}
			System.out.println("\n"); 
		}				
	} 
		
	//step 2: pheromone initialization for each design variable  
	void phero_init(){
		int temp;
		for(int i=0;i<design_var;i++){
			temp=poss_soln[i]*4; 
			for(int k=0;k<temp;k++){
				pherome[k][i]=1; 
			}	
		}
	}
	
    // pheromone display
	void phero_display(){ 
	 for(int i=0;i<max*4;i++){
			for(int k=0;k<design_var;k++){
				System.out.print(pherome[i][k]+"\t"); 
			}	
			System.out.println("\n");
		}
	}
		
	// step 3: calcualtion of probability value
	void cal_prob(){
		int temp;
		for(int i=0;i<design_var;i++){
			double sum=0;
			double round;
			temp=poss_soln[i]*4;
			//System.out.println("the permissible range  is   "+temp); 
			for(int k=0;k<temp;k++){
				sum=sum+pherome[k][i]; 
			}	
			//System.out.println("the sume is   "+sum);
			for(int k=0;k<temp;k++){
				round=pherome[k][i]/sum;
				round=Math.round(round*100);
				round=round/100;
				prob[k][i]=round;
			}
		}
	}
	
    // probability calculation display
	void prob_display(){ 
	 for(int i=0;i<max*4;i++){
			for(int k=0;k<design_var;k++){
				System.out.print(prob[i][k]+"\t"); 
			}	
			System.out.println("\n");
		}
	}
	
	//step 4: calculation of permissible range/path construction
	void path_cons(){
		float temp; 	 
		int column=0;
		double round;		
		//calculating roullete wheel
		for(int i=0;i<design_var;i++){  				 
			temp=poss_soln[i]*4;
			//System.out.println("the permissible range  is   "+temp); 
			for(int counter=0;counter<temp;counter++){
				if(pherome[counter][i]!=0)
				if(counter==0){
					path[counter][column]=0;  
					path[counter][column+1]=prob[counter][i];
				}
				else{
					path[counter][column]=path[counter-1][column+1];
					round=path[counter][column]+prob[counter][i];
					round=Math.round(round*100);
					round=round/100;
					path[counter][column+1]=round;
				}
			}	
			column=column+2; 
		}
	}
	 
	// path display
	void path_display(){ 
		 for(int i=0;i<path.length;i++){
				for(int k=0;k<(design_var*2);k++){
					System.out.print(path[i][k]+"\t"); 
				}	
				System.out.println("\n");
			}
	}
	
	//step 5: random generation of ants, it is called population or candidate solution
	void ants_generation(){
		double round;	
		int count=0;
		for(int i=0;i<design_var;i++){
			for(int j=0;j<no_ants;j++){
				round=Math.random(); 
				round=Math.round(round*100);
				ants[j][count]=round/100;
			}
			count=count+2;
		}
	}
	
	// display ants
	void ants_display(){
		for(int i=0;i<no_ants;i++){
			for(int j=0;j<design_var*2;j++){
				System.out.print(ants[i][j]+"\t "); 
			}
			System.out.println("\n");
		}
	}
	
	//step 6: mapping of ant with path/path identification
	void path_map(){
		double temp;
		int col=0;
		int des;
		for(int i=0;i<design_var;i++){ 
			des=poss_soln[i]*4;
			for(int j=0;j<no_ants;j++){				 
				temp=ants[j][col];
				for(int k=0;k<des;k++) 
						if(temp>=path[k][col] & temp<=path[k][col+1]){
							ants[j][col+1]=soln_space[k][i];  
						}
			}
			col=col+2;
		}
	}
	
	//step 7: forming combination
	void form_combi(){ 
		int[] com=new int[design_var]; 
		int zz=design_var;
		for(int z=0;z<design_var;z++){
			com[z]=(int)(Math.pow(no_ants, zz=zz-1));  
			//System.out.println(com[z]);			
		}
		
		//System.out.println("fitness "+fit);
		for(int z=0;z<com.length;z++){
			 int tem=com[z];
			 int glo_counter=0;
			 for(int fi=0;fi<fit;fi++) {
					 fitness[glo_counter][z]=ants[(fi/tem)%no_ants][(z*2)+1]; 
					 glo_counter++; 
			 }
		}		
	}
	
	// display the combination
	void combi_display(){
		for(int i=0;i<fit;i++){
			for(int j=0;j<design_var+1;j++){
				System.out.print(fitness[i][j]+"\t");
			}
			System.out.println("\n");
		}
	}
	
	// step 8: objective fun evaluation for function a+2b+3a+4b=30
	void obj_eval(){ 
		double vCPU_Total, Mem_Total;
		
		for(int i=0;i<fit;i++){  
			vCPU_Total=Mem_Total=0; 			
			for(int j=0;j<wp.Num_Workloads;j++){   
				vCPU_Total+=fitness[i][j]* wp.MR_RR[j][0];
				Mem_Total+=fitness[i][j]* wp.MR_RR[j][1];
			} 
			/*System.out.println("vCPU_Total "+vCPU_Total);
			System.out.println("Mem_Total "+Mem_Total);*/
			vCPU_Ratio=(vCPU_Total/vm.VM_Flavs[4][0])*100;										// change this VM flavor for every new VM
			Mem_Ratio=(Mem_Total/vm.VM_Flavs[4][1])*100;  
			/*System.out.println("vCPU_Ratio "+vCPU_Ratio);
			System.out.println("Mem_Ratio "+Mem_Ratio);
			System.out.println("vm.VM_Flavs[0][0] "+vm.VM_Flavs[0][0]);
			System.out.println("vm.VM_Flavs[0][1] "+vm.VM_Flavs[0][1]);*/
			fitness[i][wp.Num_Workloads]=Double.parseDouble(df.format((vCPU_Ratio+Mem_Ratio)/2)); 
		}   
	}

	// step 9: selection of best path
	void find_best(){	 
		double previous_best=0;
		for(int i=0;i<fit;i++){
			if(fitness[i][wp.Num_Workloads]>=75 && fitness[i][wp.Num_Workloads]<=100){
				if(previous_best<=fitness[i][wp.Num_Workloads]){
					previous_best=fitness[i][wp.Num_Workloads];
					for(int j=0;j<1;j++)
						for(int k=0;k<wp.Num_Workloads+1;k++)
							result[j][k]=fitness[i][k];
				}
			}
		}
		fbest=previous_best; 
		System.out.println("fbest    "+fbest);
	}
	
	void global_best_display(){
		for(int j=0;j<design_var+1;j++){
			System.out.print(result[0][j]+"\t");
		}
	}
	
	// step 10: pheromone updating
	// local pheromone updating
	void local_update(){
		double temp,round;
		for(int i=0;i<design_var;i++){
			temp=poss_soln[i]*4; 
			for(int k=0;k<temp;k++){
				pherome[k][i]=((1-row)*pherome[k][i])+(row*0.35);
				round=Math.round(pherome[k][i]*100);
				pherome[k][i]=round/100;
			}
		}
	}
	
	// global pheromone updating
	void global_update(){
		double round;
		for(int i=0;i<design_var;i++){ 
			for(int k=0;k<poss_soln[i]*4;k++){
				if(result[0][i]==soln_space[k][i]){
					pherome[k][i]=((1-row)*pherome[k][i])+(sigma+(1/(result[0][design_var]+1))); 
					round=Math.round(pherome[k][i]*100);
					pherome[k][i]=round/100;
					break;
				}
			}
		}
	} 
}
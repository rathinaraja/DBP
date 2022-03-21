package ACOHeuristicsCalc;

import java.text.DecimalFormat;
import java.util.*; 

public class ACO_Heuristic_Final_Calc{
	VMs.VM_Parameters vm=new VMs.VM_Parameters();
	Workloads.Workload_Parameters wp=new Workloads.Workload_Parameters();
	public DecimalFormat df = new DecimalFormat(".####");
	
	// maximum number of map tasks possible in each VM flavor for different workloads: each row is a VM flavour, each column is maxi possible map tasks of a workload
	double max_tasks_possible[][]={{1,1,1,0,1,0},{2,2,2,1,2,1},{4,4,4,2,4,2},{8,8,8,4,8,4},{12,12,12,6,12,6}}; 
	
	// different possible combinations and its resource usage percentage: each row is a VM flavor, every 7 columns include six workloads map tasks and its resource usage percentage
	double[][] Heuristics={{1.0,0.0,0.0,0.0,0.0,0.0,100.0,0.0,0.0,0.0,0.0,1.0,0.0,100.0,0.0,0.0,1.0,0.0,0.0,0.0,87.5},
							{2.0,0.0,0.0,0.0,0.0,0.0,100.0,1.0,0.0,0.0,0.0,1.0,0.0,100.0,0.0,0.0,1.0,0.0,1.0,0.0,93.75,1.0,0.0,1.0,0.0,0.0,0.0,93.75,1.0,1.0,0.0,0.0,0.0,0.0,87.5,},
							{4.0,0.0,0.0,0.0,0.0,0.0,100.0,2.0,0.0,0.0,0.0,2.0,0.0,100.0,1.0,0.0,0.0,0.0,3.0,0.0,100.0,0.0,0.0,0.0,0.0,4.0,0.0,100.0,0.0,0.0,1.0,0.0,3.0,0.0,96.88,3.0,0.0,1.0,0.0,0.0,0.0,96.88,2.0,0.0,1.0,0.0,1.0,0.0,96.88,3.0,1.0,0.0,0.0,0.0,0.0,93.75,0.0,1.0,1.0,0.0,2.0,0.0,90.62},
							{8.0,0.0,0.0,0.0,0.0,0.0,100.0,0.0,0.0,0.0,0.0,8.0,0.0,100.0,5.0,0.0,0.0,0.0,0.0,2.0,100.0,0.0,0.0,0.0,0.0,0.0,6.0,100.0,0.0,0.0,1.0,0.0,7.0,0.0,98.44,0.0,2.0,5.0,0.0,2.0,0.0,98.44,5.0,1.0,0.0,0.0,2.0,0.0,96.88,4.0,0.0,3.0,0.0,1.0,0.0,95.31,1.0,1.0,2.0,0.0,4.0,0.0,93.75,5.0,1.0,2.0,0.0,0.0,0.0,93.75,2.0,1.0,3.0,0.0,2.0,0.0,92.19,1.0,2.0,2.0,0.0,3.0,0.0,90.62,3.0,2.0,1.0,0.0,2.0,0.0,92.19},
							{0.0,0.0,0.0,0.0,0.0,6.0,100.0,6.0,0.0,0.0,0.0,0.0,4.0,100.0,0.0,0.0,0.0,5.0,2.0,2.0,98.96,0.0,4.0,0.0,5.0,2.0,0.0,98.96,0.0,3.0,0.0,1.0,5.0,2.0,96.88,8.0,0.0,3.0,0.0,1.0,0.0,96.88,4.0,1.0,2.0,0.0,5.0,0.0,95.83,5.0,0.0,4.0,0.0,3.0,0.0,95.83,3.0,2.0,1.0,0.0,4.0,1.0,90.62,1.0,2.0,1.0,0.0,6.0,1.0,90.62,4.0,4.0,1.0,0.0,3.0,0.0,90.62,1.0,2.0,0.0,1.0,7.0,0.0,90.62,0.0,3.0,0.0,0.0,7.0,1.0,89.58,0.0,0.0,0.0,1.0,6.0,2.0,86.46}}; 
	
	// possible number of combinations for each flavor
	int no_heuristics[]={3,5,9,13,14}; 
	
	//step 1 : control parameters initialization
	int design_var=vm.num_flavors;   							// number of design/decision variable
	int max=0;
	int no_ants=5;												// number ants also called as population or candidate solution
	int no_iter=50;
	float row=0.5f;
	int sigma=1;
	double pherome[][]; 
	
	double soln_space[][]; 
	int poss_soln[]={3,5,9,13,14}; 								// possible fruitful combinations of different VM flavours
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
	ACO_Heuristic_Final_Calc(){  	
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
		System.out.println("number of design variables      "+ design_var);
		System.out.print("range for each design variable: ");
		for(int i=0;i<poss_soln.length;i++)
			System.out.print(poss_soln[i]+"\t");
		System.out.println("\nnumber of ants                  "+no_ants);
		System.out.println("number of iterations            "+no_iter);
		System.out.println("pheromone decay factor          "+row);
		System.out.println("pheromone error factor          "+sigma);  	 		
	}
	
	
	public static void main(String args[]){
		ACO_Heuristic_Final_Calc a=new ACO_Heuristic_Final_Calc();
		System.out.println("\n\n initialization of control parameters  \n");
		a.init_display();
		a.phero_init();
		//System.out.println("\n\n pheromone initializaed\n");
		//a.phero_display(); 
		a.sol_space();
		//System.out.println("\n\n search or solution space for your problem \n ");
		//a.sol_space_display();
		
		for(int gen=0;gen<a.no_iter;gen++){
			a.cal_prob();
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
				soln_space[k][i]=(int)(randomno.nextInt(poss_soln[i]));
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
				pherome[k][i]=100; 
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
			temp=poss_soln[i]*4;
			//System.out.println("the permissible range  is   "+temp); 
			for(int k=0;k<temp;k++){
				sum=sum+pherome[k][i]; 
			}	
			//System.out.println("the sume is   "+sum);
			for(int k=0;k<temp;k++){ 
				prob[k][i]=Double.parseDouble(df.format(pherome[k][i]/sum)); 
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
					path[counter][column+1]=Double.parseDouble(df.format(path[counter][column]+prob[counter][i])); 
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
		int count=0;
		for(int i=0;i<design_var;i++){
			for(int j=0;j<no_ants;j++){ 
				ants[j][count]=Double.parseDouble(df.format(Math.random())); 
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
		double temp[][]=new double[6][7];  
		double row_percentage[]=new double[6];
		
		for(int i=0;i<fit;i++){ 

				double VM_ratio=0,wokrload_ratio=0;
				//for VM flavour 1: get the details from heuristics
				for(int k=0;k<=wp.Num_Workloads;k++){ 
					temp[0][k]=Heuristics[0][(int)((fitness[i][0]*7)+k)]; 
				}
				//for VM flavour 2: get the details from heuristics
				for(int k=0;k<=wp.Num_Workloads;k++){
					temp[1][k]=Heuristics[1][(int)((fitness[i][1]*7)+k)];
				}
				//for VM flavour 3: get the details from heuristics
				for(int k=0;k<=wp.Num_Workloads;k++){
					temp[2][k]=Heuristics[2][(int)((fitness[i][2]*7)+k)];
				}
				//for VM flavour 4: get the details from heuristics
				for(int k=0;k<=wp.Num_Workloads;k++){
					temp[3][k]=Heuristics[3][(int)((fitness[i][3]*7)+k)];
				}
				//for VM flavour 5: get the details from heuristics
				for(int k=0;k<=wp.Num_Workloads;k++){
					temp[4][k]=Heuristics[4][(int)((fitness[i][4]*7)+k)];
				}
				
				//sum up number of map tasks in each flavor for a workload
				for(int k=0;k<6;k++){
					temp[5][k]=0;
					for(int l=0;l<5;l++){
						temp[5][k]+=temp[l][k];
					}   
					temp[5][k]=Double.parseDouble(df.format(temp[5][k]/6));	
				}
				
				//calculate % of different workloads of map tasks in the same VM flavour
				for(int k=0;k<5;k++){
					for(int l=0;l<6;l++){
						row_percentage[k]+= max_tasks_possible[k][l]==0? temp[k][l]/1 : temp[k][l]/max_tasks_possible[k][l];
					}
					row_percentage[k]=Double.parseDouble(df.format(row_percentage[k]/6));
					//System.out.print("\t"+row_percentage[k]);
				}
				
				//calculate % of blocks processed of same workload in differnt VM flavour at any point of time
				//System.out.println(""); 
				for(int k=0;k<5;k++)
					VM_ratio+=row_percentage[k];
				
				//calculate % of blocks processed of same VM Flavour for different workload
				//System.out.println(""); 
				for(int k=0;k<6;k++)
					wokrload_ratio+=temp[5][k];
				
				/*for(int k=0;k<temp.length;k++){
					for(int l=0;l<temp[0].length;l++)
						System.out.print("\t"+temp[k][l]);
					System.out.println(" ");	
				}  
				*/
				//System.out.println("VM_ratio "+VM_ratio);
				//System.out.println("wokrload_ratio "+wokrload_ratio);
				fitness[i][5]=Double.parseDouble(df.format((VM_ratio/5)*(wokrload_ratio/6)));
				//System.out.println("fitness[i][5] "+fitness[i][5]); 
		} 
	}

	// step 9: selection of best path
	void find_best(){	 
		double previous_best=0;
		for(int i=0;i<fit;i++){
			if(previous_best<fitness[i][5]){
				previous_best=fitness[i][5];
				for(int j=0;j<6;j++) 
					result[0][j]=fitness[i][j]; 
			}
		}
		fbest=previous_best; 
		System.out.println("fbest    "+fbest);
	}
	
	void global_best_display(){
		for(int j=0;j<design_var+1;j++){
			System.out.print(result[0][j]+"\t");
		}
		System.out.println("");
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

package cs4740p2;

import java.util.ArrayList;
import java.util.Map.Entry;


public class Viterbi {
	Preprocessor pr;
	
	public Viterbi(){
		pr = new Preprocessor();
		double BIvsOpercent = 0.05;
		double weaselSentencePercent = 0.5;
		pr.preprocess(BIvsOpercent,weaselSentencePercent, 0.8);
		System.out.println(pr);
	}
	
	// find the location for the max value in a double array
	public int indexforMaxValue(double[] d) {
		double dd = -1;
		int index = 0;
		for (int i = 0; i < d.length; i++) {
			if (d[i] >= dd) {
				dd = d[i];
				index = i;
			}
		}		
		return index;
	}


	public ArrayList<Character> viterbi(String s) {
		//System.out.println("allows" + Preprocessor.wordtype.get("allows"));
		//System.out.println(pr);		
		String[] str = s.split(" ");
		ArrayList<Character> tag = new ArrayList<Character>();

		//0=B,1=I,2=O
		char[] tagbyint = new char[]{'B', 'I', 'O'};
		double[][] score = new double[3][str.length];
		int[][] bptr = new int[3][str.length];
		double[] temp = new double[3];
		int[] tagindex = new int[str.length];
		
		  

		for(int t = 0; t < str.length; t++){			
			if(t == 0){				
				for(int i = 0; i < 3; i++){
					if(Preprocessor.wordtype.containsKey(str[t].toLowerCase())){
						//score[i][t] = pr.getProbWordGivenTag(str[t], tagbyint[i]) * pr.getBIOprob(i);
						score[i][t] = pr.getProbWordGivenTag(str[t], tagbyint[i])* pr.getTransitionProb(2, i);
					}else{
						score[i][t] = (double) 1 / 3 * pr.getBIOprob(i);
						
					}
					bptr[i][t] = 0;
//					System.out.println("str: " + str[t]);
//					System.out.println("i:" + i + "tag: " + tagbyint[i]);
//					System.out.println("i:" + i + "pr.getProbWordGivenTag:" + pr.getProbWordGivenTag(str[t], tagbyint[i]));
//					System.out.println("i:" + i + "pr.getBIOprob:" + pr.getBIOprob(i));
//					System.out.println("i:" + i + "score: " + score[i][t]);
				}               
			}
			else {
				//System.out.println( "str: " + str[t]);	
				for(int i = 0; i < 3; i++){
					//System.out.println("i:" + i);
					for(int j = 0; j < 3; j++){
						temp[j] = score[j][t - 1] * pr.getTransitionProb(j, i);	
							
//						System.out.println("j:" + j + "score[j][t-1]: " + score[j][t-1]);
//						System.out.println("pr.getTransitionProb(j, i)" + pr.getTransitionProb(j, i));
//						System.out.println("temp[j]" + temp[j]);
					}
					int maxindex = indexforMaxValue(temp);
					double max = temp[maxindex];
					if(Preprocessor.wordtype.containsKey(str[t].toLowerCase())){
						//System.out.println("in!!!");
						score[i][t] = max * pr.getProbWordGivenTag(str[t], tagbyint[i]);
					}else{
						score[i][t] = max * (double) 1 / 3;	
						
					}
					bptr[i][t] = maxindex;
//					System.out.println("maxindex: " + maxindex);
//					System.out.println("pr.getProbWordGivenTag(str[t], tagbyint[i])" + pr.getProbWordGivenTag(str[t], tagbyint[i]));
//					System.out.println("score: " + score[i][t]);
				}  
				
			}
		}
		for(int j = 0; j < 3; j++){
			temp[j] = score[j][str.length - 1];	
		}
		int maxindex = indexforMaxValue(temp);
		tagindex[str.length - 1] = maxindex;
		for(int i = str.length - 2; i >= 0; i--){
			tagindex[i] = bptr[tagindex[i + 1]][i + 1];
		}
		for(int i = 0; i < str.length; i++){
			tag.add(tagbyint[tagindex[i]]);
		}
		return tag;
	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Viterbi vf = new Viterbi();
		ArrayList<Character> a = vf.viterbi("This might allow experimenters to track the phosphorylation state of many phosphoproteins in the cell over time .");
		for(char c:a){
			System.out.println(c);
		}
	}

}


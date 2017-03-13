package cs4740p2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class Validation {

	private int sentenceCount;
	Viterbi vi = new Viterbi();

	public Validation(){

		sentenceCount = 0;
	}

	public void getValiSet(File file){
		StringBuilder result = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String s = null;			
			String words = "";

			while ((s = br.readLine()) != null) {
				//System.out.println("s:" + s);
				if (s.length() < 1)
					result.append(System.lineSeparator());
				if (s.length() < 2){
					if(s.length() < 2){
						sentenceCount++;
					}else{
						String[] str = s.split("\t");						
						words += str[0]+ " " ;

					}
					words = words.trim();
					//System.out.println("words" + words);
					if(!words.equals("")){
						ArrayList<Character> predtags = vi.viterbi(words);

						getnew(words, predtags, result);
//						for(char t : predtags){
//							System.out.println("pretag:" + t);
//						}
					}
					words = "";

				} else {
					if(s.length() > 1){
						String[] str = s.split("\t");						
						words += str[0]+ " " ;

					}
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		printToFile(result.toString(), "C:/Cornell/cs4740/project2/nlp_project2_uncertainty/preprocessed/validation_new.txt");

	}

	public void printToFile(String s, String outputpath){
		try( PrintWriter out = new PrintWriter(outputpath)){
			out.println(s);
		} catch (FileNotFoundException e) {
			System.out.println("output folder not found: " + outputpath);
			e.printStackTrace();
		}
	}

	public String getSpan(File f){
		String re = "";
		String lastTag = "";		
		int startIndex = 0;
		int endIndex = 0;
		int index = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String s = null;			

			while ((s = br.readLine()) != null) {
				if(s.length() > 1){
					String[] str = s.split("\t");

					if((str[1].equals("O") && (lastTag.equals("I") || lastTag.equals("B")))
							|| str[1].equals("B") && lastTag.equals("I")
							|| str[1].equals("B") && lastTag.equals("B")){
						endIndex = index - 1;
						re += startIndex + "-" + endIndex + " ";
					}
					if(str[1].equals("B")){
						startIndex = index;

					}
					lastTag = str[1];

					index++;
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return re;
	}
	
	public String getWeaselSentences(File f){
		StringBuilder result = new StringBuilder();
		int index = 0;
		boolean isWeasel = false;
		String sentence = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String s = null;			
			
			while ((s = br.readLine()) != null) {
				if (s.length() >= 1){
					String[] str = s.split("\t");
					if (str.length == 2) {
						if (str[1].equals("B")){
							isWeasel = true;
						}
						sentence += s;
					}
				} else if(isWeasel){
					isWeasel = false;
					sentence = "";
					result.append(" " + index++);
				} else {
					if (!sentence.equals(""))
						index++;
					sentence = "";
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}		
		System.out.println(result.toString());
		System.out.println("Highest sentence is is" + index);
		return result.toString();
	}


	public void getnew(String words, ArrayList<Character> predtags, StringBuilder result){
		String[] wordar = words.split(" ");
		//System.out.println("wordar.length" + wordar.length);
		//System.out.println("predtags.size()" + predtags.size());
		for(int i = 0; i < wordar.length; i++){
			String s = "";
			s += wordar[i] + "\t" + predtags.get(i);
			result.append(System.lineSeparator() + s);
		}
	}


	public double getP(String a, String b){
		int correct = 0;
		int predict = 0;
        HashMap<String, Double> amap = new HashMap<String, Double>();
        HashMap<String, Double> bmap = new HashMap<String, Double>();
        String[] stra = a.split(" ");
        for(String t : stra){
        	amap.put(t, 0d);
        }
        String[] strb = b.split(" ");
        for(String t : strb){
        	bmap.put(t, 0d);
        }
        for(String t : strb){
        	predict++;
        	if(amap.containsKey(t)) {
        		//System.out.println("same: " + t);
        		correct++;
        	}
        }
        double p = (double)correct / predict; 
		return p;
	}
	
	public double getR(String a, String b){
		int correct = 0;
		int testset = 0;
        HashMap<String, Double> amap = new HashMap<String, Double>();
        HashMap<String, Double> bmap = new HashMap<String, Double>();
        String[] stra = a.split(" ");
        for(String t : stra){
        	amap.put(t, 0d);
        }
        String[] strb = b.split(" ");
        for(String t : strb){
        	bmap.put(t, 0d);
        }
        for(String t : stra){
        	testset++;
        	if(bmap.containsKey(t)) correct++;
        }
        double r = (double)correct / testset; 
		return r;
	}
	
	public double getF(double p, double r){
		return 2 * p * r / (p + r);
	}



	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Validation va = new Validation();
		String valiset = Preprocessor.validation_file;        
		File valifile = new File(valiset);
		String a = va.getSpan(valifile);
		System.out.println("Part 1 test");
		System.out.println("Part 1 ground truth");
		System.out.println(a);
		
		//File valifile = new File("C:/Cornell/cs4740/project2/nlp_project2_uncertainty/preprocessed/testvali.txt");
		va.getValiSet(valifile);
		//System.out.println(va);
		String b = va.getSpan(new File("C:/Cornell/cs4740/project2/nlp_project2_uncertainty/preprocessed/validation_new.txt"));
		System.out.println("Part 1 test");
		System.out.println(b);
		double p = va.getP(a, b);
		System.out.println("p:" + p);
		double r = va.getR(a, b);
		System.out.println("r:" + r);
		double f = va.getF(p, r);
		System.out.println("f:" + f);
		System.out.println("Part 2 test");
		String part2 = va.getWeaselSentences(new File("C:/Cornell/cs4740/project2/nlp_project2_uncertainty/preprocessed/validation_new.txt"));

		System.out.println("Part 2 ground truth");
		String gtPart2 = va.getWeaselSentences(valifile);
		
		double p2 = va.getP(gtPart2,part2);
		System.out.println("p2:" + p2);
		double r2 = va.getR(gtPart2,part2);
		System.out.println("r2:" + r2);
		double f2 = va.getF(p2,r2);
		System.out.println("f2:" + f2);
		System.out.println();
	}

}


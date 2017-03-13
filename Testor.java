package cs4740p2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Testor {
	Viterbi vi;
	private int wordcount;
	private static final String test_public_file = "C:/Cornell/cs4740/project2/nlp_project2_uncertainty/test-public/"; 
	private static final String test_public = "C:/Cornell/cs4740/project2/nlp_project2_uncertainty/preprocessed/test_public.txt";
	private static final String test_public_label = "C:/Cornell/cs4740/project2/nlp_project2_uncertainty/preprocessed/test_public_label.txt";
	private static final String test_private_file = "C:/Cornell/cs4740/project2/nlp_project2_uncertainty/test-private/"; 
	private static final String test_private = "C:/Cornell/cs4740/project2/nlp_project2_uncertainty/preprocessed/test_private.txt";
	private static final String test_private_label = "C:/Cornell/cs4740/project2/nlp_project2_uncertainty/preprocessed/test_private_label.txt";
	private static final String test1 = "C:/Cornell/cs4740/project2/nlp_project2_uncertainty/preprocessed/test1.txt";
	//	private static final String test_public_file = "/Users/asm278/Documents/Sem9/5740/project2/test-public/"; 
	//	private static final String test_public = "/Users/asm278/Documents/Sem9/5740/project2/preprocessed/test_public.txt";
	//	private static final String test_public_label = "/Users/asm278/Documents/Sem9/5740/project2/preprocessed/test_public_label.txt";
	//	private static final String test_private_file = "/Users/asm278/Documents/Sem9/5740/project2/test-private/"; 
	//	private static final String test_private = "/Users/asm278/Documents/Sem9/5740/project2/preprocessed/test_private.txt";
	//	private static final String test_private_label = "/Users/asm278/Documents/Sem9/5740/project2/preprocessed/test_private_label.txt";
	//	private static final String test1 = "/Users/asm278/Documents/Sem9/5740/project2/preprocessed/test1.txt";	
	private static final String test2 = "C:/Cornell/cs4740/project2/nlp_project2_uncertainty/preprocessed/test2.txt";
	public Testor(){
		vi = new Viterbi();
		wordcount = 0;
	}

	public String connectFileWithPercentage(String f, double percentageTraining){
		String re = "";
		String FilePath[] = FilePathGet(f);
		for (int i = 0; i < (int) (FilePath.length * percentageTraining); i++) {
			//System.out.println("train set:" + FilePath[i]);
			if (FilePath[i].equals(".DS_Store")) continue; // sorry I have a mac
			File File = new File(f + FilePath[i]);
			re = re + " " + txt2String(File);
		}
		return re;
	}
	public String txt2String(File file) {
		StringBuilder result = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String s = null;
			while ((s = br.readLine()) != null) {
				result.append(System.lineSeparator() + s);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result.toString();
	}
	public String[] FilePathGet(String s) {
		File file = new File(s);
		String filepath[];
		filepath = file.list();
		return filepath;
	}

	public void printToFile(String s, String outputpath){
		try( PrintWriter out = new PrintWriter(outputpath)){
			out.println(s);
		} catch (FileNotFoundException e) {
			System.out.println("output folder not found: " + outputpath);
			e.printStackTrace();
		}
	}

	public String getPredict(File file){
		StringBuilder result = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String s = null;			
			String words = "";

			int countSentences = 0;
			while ((s = br.readLine()) != null) {
				//				System.out.println(s + "  " + s.equals("") + s.equals("\n") + s.trim().equals(""));
				if (s.trim().length() < 1){
					++countSentences;
					result.append(System.lineSeparator());
				}
				if (s.length() < 2){
					if(s.length() > 1){
						String[] str = s.split("\t");						
						words += str[0]+ " " ;

					}
					words = words.trim();
					if(!words.equals("")){
						ArrayList<Character> predtags = vi.viterbi(words);
						subtitute(words, predtags, result);
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
		return result.toString();
	}

	public void subtitute(String words, ArrayList<Character> predtags, StringBuilder result){
		String[] wordar = words.split(" ");
		//System.out.println("wordar.length" + wordar.length);
		//System.out.println("predtags.size()" + predtags.size());
		for(int i = 0; i < wordar.length; i++){
			String s = "";
			s += wordar[i] + "\t" + predtags.get(i);
			result.append(System.lineSeparator() + s);
		}
	}

	public void submitformatone(){
		String re = "";
		re += "Type,Spans" + "\n";
		re += "CUE-public,";
		re += getSpan(new File(test_public_label));
		re += "\n" + "CUE-private,";
		re += getSpan(new File(test_private_label));
		printToFile(re, test1);
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

	public void submitformattwo(){
		String re = "";
		re += "Type,Indices" + "\n";
		re += "SENTENCE-public,";
		re += getWeaselSentences(new File(test_public_label));
		re += "\n" + "SENTENCE-private,";
		re += getWeaselSentences(new File(test_private_label));
		printToFile(re, test2);
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
					if(! sentence.equals(""))
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


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Testor te = new Testor();
		String out = te.connectFileWithPercentage(test_public_file, 1);
		te.printToFile(out, test_public);
		File f = new File(test_public);
		String outlabel = te.getPredict(f);
		te.printToFile(outlabel, test_public_label);
		String out2 = te.connectFileWithPercentage(test_private_file, 1);
		te.printToFile(out2, test_private);
		File f2 = new File(test_private);
		String outlabel2 = te.getPredict(f2);
		te.printToFile(outlabel2, test_private_label);
		te.submitformatone();
		te.submitformattwo();


	}

}

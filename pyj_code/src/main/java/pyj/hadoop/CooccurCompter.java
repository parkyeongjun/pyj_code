package pyj.hadoop;
import java.util.regex.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer;
//DataHandler에서 출력해낸 parsed_xml이 하둡의 입력이며 밑에와같이 들어간다.
//////////////Mapper Input /////////////
/*		
year:2003 journal_no:1          The genome-wide transcriptome of the  P. falciparum  IDC was generated by measuring relative mRNA abundance levels in samples collected from a highly synchronized in vitro culture of parasites. The strain used was the well-characterized Honduran chloroquine-sensitive HB3 strain, which was used in the only two experimental crosses carried out thus far with  P. falciparum  ( Walliker et al. 1987 ;  Wellems et al. 1990 ). To obtain sufficient quantities of parasitized RBCs and to ensure the homogeneity of the samples, a large-scale culturing technique was developed using a 4.5 l bioreactor (see  Materials and Methods ). Samples were collected for a 48-h period beginning 1 h postinvasion (hpi). Culture synchronization was monitored every hour by Giemsa staining. We observed only the asexual form of the parasite in these stains. The culture was synchronous, with greater than 80% of the parasites invading fresh RBCs within 2 h prior to the harvesting of the first timepoint. Maintenance of synchrony throughout the IDC was demonstrated by sharp transitions between the ring-to-trophozoite and trophozoite-to-schizont stages at the 17- and 29-h timepoints, respectively ( Figure 1 A). 
year:2003 journal_no:1          The DNA microarray used in this study consists of 7,462 individual 70mer oligonucleotides representing 4,488 of the 5,409 ORFs manually annotated by the malaria genome sequencing consortium ( Bozdech et al. 2003 ). Of the 4,488 ORFs, 990 are represented by more than one oligonucleotide. Since our oligonucleotide design was based on partially assembled sequences periodically released by the sequencing consortium over the past several years, our set includes additional features representing 1,315 putative ORFs not part of the manually annotated collection. In this group, 394 oligonucleotides are no longer represented in the current assembled sequence. These latter ORFs likely fall into the gaps present in the published assembly available through the  Plasmodium  genome resource  PlasmoDB.org  ( Gardner et al. 2002 ;  Kissinger et al. 2002 ;  Bahl et al. 2003 ). 
year:2003 journal_no:1          To measure the relative abundance of mRNAs throughout the IDC, total RNA from each timepoint was compared to an arbitrary reference pool of total RNA from all timepoints in a standard two-color competitive hybridization ( Eisen and Brown 1999 ). The transcriptional profile of each ORF is represented by the mean-centered series of ratio measurements for the corresponding oligonucleotide(s) ( Figure 1 Bâ1E). Inspection of the entire dataset revealed a striking nonstochastic periodicity in the majority of expression profiles. The relative abundance of these mRNAs continuously varies throughout the IDC and is marked by a single maximum and a single minimum, as observed for the representative schizont-specific gene, erythrocyte-binding antigen 175 ( eba175 ), and the trophozoite-specific gene, dihydrofolate reductaseâthymidylate synthetase ( dhfr-ts ) ( Figure 1 B and 1C). However, there is diversity in both the absolute magnitude of relative expression and in the timing of maximal expression (phase). In addition, a minority of genes, such as adenylosuccinate lyase ( asl ) ( Figure 1 D), displayed a relatively constant expression profile. The accuracy of measurements from individual oligonucleotides was further verified by the ORFs that are represented by more than one oligonucleotide feature on the microarray. The calculated average pairwise Pearson correlation ( r ) is greater than 0.90 for 68% (0.75 for 86%) of the transcripts represented by multiple oligonucleotides with detectable expression during the IDC ( Table S1 ). Cases in which data from multiple oligonucleotides representing a single putative ORF disagree may represent incorrect annotation. The internal consistency of expression profile measurements for ORFs represented by more than one oligonucleotide sequence is graphically shown in  Figure 1 E for the hypothetical protein MAL6P1.147, the largest predicted ORF in the genome (31 kb), which is represented by 14 oligonucleotide elements spanning the entire length of the coding sequence. The average pairwise correlation ( r ) for these features is 0.98Â±0.02. 
year:2003 journal_no:1	         Periodicity in genome-wide.................
*/
//////////////Mapper Input /////////////         ----> 변수 <value>에 담겨온다.

public class CooccurCompter {
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: wordcount <in> <out>");
			System.exit(2);
		}
		Job job = new Job(conf, "word count");
		job.setJarByClass(CooccurCompter.class);
		job.setMapperClass(CoMapper.class);
		job.setCombinerClass(CoCombiner.class);
		job.setReducerClass(CoReducer.class);
		//job.setReducerClass(CoCombiner.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}

	/////// MAPPER ///////
	public static class CoMapper extends Mapper<Object, Text, Text, IntWritable> {

		private final static IntWritable one = new IntWritable(1);

		 MaxentTagger tagger = new MaxentTagger("edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");
	
		private Text word = new Text();//여기에 담아서 컴바이너로 전송.
		Pattern p = Pattern.compile("([a-zA-Z0-9].*[!,@,#,$,%,^,&,*,?,_,~])|([!,@,#,$,%,^,&,*,?,_,~].*[a-zA-Z0-9])");


		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			int W_S = 10; // 윈도우 사이즈
			List<Object> words = new ArrayList<Object>();// co_occur 알고리즘사용에 위한
			StringReader r = new StringReader(value.toString()); // doc 단위로 불러옴 valu가 인풋
			List<List<HasWord>> sentences = MaxentTagger.tokenizeText(r);// sentences에 line단위로 들어감.											// 리스트
			
			for (List<HasWord> sentence : sentences) {
				List<TaggedWord> tSentence = tagger.tagSentence(sentence); // 태그를 붙힘. 
				//Sentence.listToString(tSentence, false) = The/DT diluted/JJ extracts/NNS were/VBD incubated/VBN with/IN phosphorylated/VBN APCm3/NN beads/NNS 처럼 한라인이 들어갈것이다.
				String[] splited_Word = Sentence.listToString(tSentence, false).split(" "); //띄어쓰기 단위로 나눌것이다 0번째엔 The/DT 1번째엔 extracts/NNS 이런식으로
				for(int i = 0 ; i < splited_Word.length ; i++)
				{
					if (splited_Word[i].length() < 1 || splited_Word[i].length() > 20)// 단어길이여부
						continue;
					if (splited_Word[i].contains("year:") || splited_Word[i].contains("journal_no:"))// 체커여부
						continue;
					if (!(splited_Word[i].contains("/NNS") || splited_Word[i].contains("/NN")))// 명사여부
						continue;
					
					String nn_word=splited_Word[i].split("/")[0]; // The/DT로 되어있는것을 The만 뽑아낸다
					Pattern p = Pattern.compile("([a-zA-Z0-9].*[!,@,#,$,%,^,&,*,?,_,~,-,.])|([!,@,#,$,%,^,&,*,?,_,~,-,.].*[a-zA-Z0-9])");
					Matcher m = p.matcher(splited_Word[i]);
					if(m.find())//유효한문자만 골라냄
						continue;
					words.add(nn_word); // 골라낸 명사를 워드리스트에 단어별로 싹다넣는다.
					
					word.set("*\t" + nn_word); // 하나의 워드에 대한 빈도수 보낸다.
					
					context.write(word, one);// (* apple 1)

				}

			}

			// co_occur algorithm
			// 인터넷 알고리즘 참고 //
			for (int i = 0; i < words.size(); i++) {
				int sum = 0;
				for (int j = 0; j < W_S; j++) {
					if (i + j >= words.size()) {
						break;
					}
					if (words.get(i).toString().equals(words.get(i + j).toString()) == false) {

						word.set(words.get(i) + "\t" + words.get(i + j));
						context.write(word, one);//////////////////

						sum++;
					}
				}
				if (sum > 0) {
					// word.set(words.get(i)+"\t*\t"+sum);
					// word.set("*\t"+words.get(i)+"\t"+sum);
					// context.write(word, one);////////////////////
				}
			}
			//

		}
	}
	//

	// COMBINER
	public static class CoCombiner extends Reducer<Text, IntWritable, Text, IntWritable> {
		private IntWritable result = new IntWritable();

		public void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			result.set(sum);// 같은 키가 있으면 sum해서 리듀서로 보내는역할.
			context.write(key, result);
		}
	}

	// * a 12
	// * b 14
	// * c 15
	// * d 72
	// .
	// .
	// .
	// * z 45 //단어하나의 빈도수 다 들어온후.
	// a b 4
	// b c 5
	// c d 3
	// .
	// .
	// .
	// y z 5
	// reducer input.

	// REDUCER
	public static class CoReducer extends Reducer<Text, IntWritable, Text, DoubleWritable> {
		private DoubleWritable result = new DoubleWritable();
		HashMap<String, Integer> map = new HashMap<String, Integer>(); // 해쉬맵사용.
		int count = 0;

		public void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException, ArrayIndexOutOfBoundsException {
			int sum = 0;
			for (IntWritable val : values) {
				sum = sum + val.get();
			}
			String[] temp = (key.toString()).split("\t");// 입력이 apple dog 4
															// 이런식으로 들어오기때문에
			if (temp.length == 2) {

				String word1 = temp[0]; // apple
				String word2 = temp[1]; // dog
				int value = sum; // 4 이런식으로 넣는다 나눠서.

				if (word1.equals("*")) // 각토큰의 빈도수를 wordsum에 저장 wordval에 빈도수저장.
				{

					map.put(word2, value);
					count += value; // PMI공식에서 필요한 확률을 구하기위해 모든 수를 구한다.
				} else if (value < 5) // 수치가 너무 작으면 커트.
				{

				} else if (map.get(word1)!=null && map.get(word2) != null){
					///////////// 해쉬주소로 배열에서 원하는값 찾는 부분////////////

					int word1_val = map.get(word1);
					int word2_val = map.get(word2);
					double pmi = (value * count) / (word1_val * word2_val); // PMI공식
					pmi = Math.log(pmi) / Math.log(2); // 밑이2인 로그를만들기위해
					result.set(pmi);
					key = new Text(word1 + "\t" + word2);
					if (pmi > 5 && pmi < 100) // 너무작은 수치는 필터링
						context.write(key, result);
				}
			}
		}
	}
	//
}

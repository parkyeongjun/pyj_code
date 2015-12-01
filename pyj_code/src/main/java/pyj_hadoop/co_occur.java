package pyj_hadoop;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer;

public class co_occur {
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: wordcount <in> <out> <ngram>");
			System.exit(2);
		}
		Job job = new Job(conf, "word count");
		job.setJarByClass(co_occur.class);
		job.setMapperClass(TokenizerMapper.class);
		job.setCombinerClass(IntSumcom.class);
//		job.setReducerClass(IntSumcom.class);
		job.setReducerClass(IntSumReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
	//■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ MAPPER ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
	public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {

		private final static IntWritable one = new IntWritable(1);

		MaxentTagger tagger = new MaxentTagger("eng.tagger"); // 태거 로딩
		private Text word = new Text(); 

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			int W_S = 10; // 윈도우 사이즈
			
			List<Object> words = new ArrayList<Object>();//co_occur 알고리즘사용에 위한 리스트
			
			String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]"; // 유효성검사를 위한          
			
			PTBTokenizer<Word> ptb = PTBTokenizer.newPTBTokenizer(new StringReader((value.toString().toLowerCase())));
			
			List<Word> ptbwords = ptb.tokenize(); // 스탠포드 토크나이제이션 사용.
			
			for(int i  = 0 ; i < ptbwords.size() ; i++){
				
				String ptvword  = ptbwords.get(i).toString();
				String tagged = tagger.tagString(ptvword);// 스탠포드 포스 태그를붙힌다
				if ((ptvword.length() > 1 && ptvword.length() < 20) &&
						((tagged.contains("year:") || tagged.contains("journal_no:")) == false) && 
						(tagged.contains("_NNS") || tagged.contains("NN")) &&
						ptvword.contains(match)==false) //년도와 논문번호 여부 체커 & 명사여부 체크  (year:2011 나 with같은 명사가아닌것들을 제거.)
				{
					words.add(ptvword); // 골라낸 명사를 워드리스트에 단어별로 싹다넣는다.
					word.set("*\t" + ptvword); // 하나의 워드에 대한 빈도수 보낸다. 
					context.write(word, one);//(*	apple	1)
				}
			}
			
			
			//■■■■■■■■■■■■■■■■■■■■■■■■■co_occur algorithm ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
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
			//■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■

								
			
		}
	}
	//■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
	
	
	
	
	
	
	



	
	//■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■COMBINER ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
	public static class IntSumcom extends Reducer<Text, IntWritable, Text, IntWritable> {
		private IntWritable result = new IntWritable();

		public void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			result.set(sum);//같은 키가 있으면 sum해서 리듀서로 보내는역할.
			context.write(key, result);
		}
	}
	//■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
	
	
	
	
	
	
	
	
	
	
	//■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■REDUCER■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
	public static class IntSumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
		private IntWritable result = new IntWritable();
		HashMap<String , Integer> map = new HashMap<String , Integer>(); // 해쉬맵사용.
		int count = 0;

		public void reduce(Text keyin, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException, ArrayIndexOutOfBoundsException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			String[] temp = (keyin.toString()).split("\t");// 입력이          apple	dog	4   이런식으로 들어오기때문에
			if (temp.length == 2) {

				String word1 = temp[0]; //apple
				String word2 = temp[1]; //dog
				int value = sum; //4 이런식으로 넣는다 나눠서.

				if (word1.equals("*")) // 각토큰의 빈도수를 wordsum에 저장 wordval에 빈도수저장.
				{

					map.put(word2,value);
					count = count + value; // PMI공식에서 필요한 확률을 구하기위해 모든 수를 구한다.
				}
				else if(value < 5) // 수치가 너무 작으면 커트.
				{
					
				}
				else 
				{
					///////////// 해쉬주소로 배열에서 원하는값 찾는 부분////////////
									
					int word1_val = map.get(word1);
					int word2_val = map.get(word2);
					double pmi = (value * count) / (word1_val*word2_val); // PMI공식
					pmi = Math.log(pmi)/Math.log(2); // 밑이2인 로그를만들기위해
					
					keyin = new Text(word1 + "\t" + word2 + "\t" + pmi);
					if(pmi > 5 && pmi < 100) // 너무작은 수치는 필터링
						context.write(keyin, result);
				}
			}
		}
	}
	//■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
}

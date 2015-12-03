package pyj.hadoop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

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
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import java.io.*;
public class JasonToXmlGenerator {
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
		job.setReducerClass(CoReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}

	// MAPPER //
	public static class CoMapper extends Mapper<Object, Text, Text, IntWritable> {

		private final static IntWritable one = new IntWritable(1);



		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

			context.write(value, one);

		}
	}
 
	public static class CoReducer extends Reducer<Text, IntWritable, Text, DoubleWritable> {
		private DoubleWritable result = new DoubleWritable();
		HashMap<String, Integer> map = new HashMap<String, Integer>(); // 해쉬맵사용.
		int count = 0;
		List<String> labels = new ArrayList<>();
		Properties props = new Properties();
		
		
		public void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException, ArrayIndexOutOfBoundsException {

			props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, sentiment");
			StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
			String lines[] = key.toString().split("\n");
			
			for (int i = 0 ; i<lines.length ; i++) {
					String line = lines[i];
					String[] value = line.split("\t");
					String content = value[1];
					Annotation doc = pipeline.process(content);
					pipeline.annotate(doc);

					// pipeline.prettyPrint(doc, new PrintWriter(System.out));
					// StringBuffer sb;
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					pipeline.xmlPrint(doc, os);
					
					String[] con =os.toString().split("<root>"); // 루트태그 위에 아이디를 넣기위해
					String send = con[0]+"<ID>"+value[0]+"</ID>\n<root>"+con[1];
					send = send.replace("\n", "(n)").trim();
					context.write(new Text(send),null);
				}
			}

		}
}



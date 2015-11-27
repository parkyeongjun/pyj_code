package pyj_hadoop;

import java.io.IOException;
import java.util.StringTokenizer;

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

public class hadooptester {
	public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {

		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();
		String art_no_t = "";
		int ngram = 1;
		String year_t = "";
		String b = "";
		// int ngram = 1 ;////////////////////////////////select ngram
		String before[] = { "", "", "" };

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

			StringTokenizer itr = new StringTokenizer(value.toString().toLowerCase(), "\n\tㅁ ");
			while (itr.hasMoreTokens()) {
				String a = itr.nextToken();

				if (a.contains("yearck")) {
					year_t = a.substring(6);
				} else if (a.contains("artnck")) {
					art_no_t = a.substring(6);
				}

				//////////// ngram coding//////////

				if (ngram == 1) {
					b = a;
				} else if (ngram == 2) {
					b = before[0] + " " + a;
					before[0] = a;
				} else if (ngram == 3) {
					b = before[0] + " " + before[1] + " " + a;
					before[0] = before[1];
					before[1] = a;

				} else if (ngram == 4) {
					b = before[0] + " " + before[1] + " " + before[2] + " " + a;
					before[0] = before[1];
					before[1] = before[2];
					before[2] = a;

				}
				String test[] = b.split(" ");
				int n = test.length;
				//////////////////////////////////

				if (b.contains("http://") == false && b.contains("yearck") == false && a.contains("artnck") == false
						&& b.equals("") == false && n == ngram) {
					word.set(year_t + "\t" + b + "\t" + art_no_t);

					context.write(word, one);
				}

			}
		}
	}

	public static class IntSumcom extends Reducer<Text, IntWritable, Text, IntWritable> {
		private IntWritable result = new IntWritable();

		public void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			result.set(sum);
			context.write(key, result);
		}
	}

	public static class IntSumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
		private IntWritable result = new IntWritable();
		String[] before = { "", "", "" };
		String[] after = { "", "", "" };
		int start = 0;
		int yearcount = 0;
		int volcount = 1;

		public void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			after = key.toString().split("\t");
			int value = 0;
			after[2] = Integer.toString(value);
			for (IntWritable val : values) {
				value += val.get();
			} // value값을 받아온것일뿐.

			if ((after[0] + after[1]).equals(after[0] + before[1])) {
				yearcount = yearcount + value;
				volcount++;
			} else if (start != 0) {
				key = new Text(before[0] + "\t" + before[1] + "\t" + yearcount);
				result.set(volcount);
				context.write(key, result);
				yearcount = 0;
				volcount = 1;
				yearcount = yearcount + value;
			} else {
				yearcount = yearcount + value;
			}
			before = after;
			start++;

		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: wordcount <in> <out>");
			System.exit(2);
		}
		Job job = new Job(conf, "word count");
		job.setJarByClass(ngram.class);
		job.setNumReduceTasks(20); // 2 reducers
		job.setMapperClass(TokenizerMapper.class);
		job.setReducerClass(IntSumReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setCombinerClass(IntSumcom.class);
		job.setOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}

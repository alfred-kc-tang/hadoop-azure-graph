package alfred.graph;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.util.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import java.io.IOException;

public class Q4 {

  public static class NodeMapper extends Mapper<LongWritable, Text, Text, Text> {

    private Text node = new Text();
    private final static Text src = new Text("src");
    private final static Text tgt = new Text("tgt");

    @Override
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
      String[] src_tgt = value.toString().split("\t");
      for (int i = 0; i < 2; i++) {
        if (i == 0) {
          node.set(src_tgt[i]);
          context.write(node, src);
        } else if (i == 1) {
          node.set(src_tgt[i]);
          context.write(node, tgt);
        }
      }
    }
  }

  public static class DegDiffReducer extends Reducer<Text, Text, Text, Text> {

    private Text diff = new Text();

    @Override
    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
      int outDeg = 0;
      int inDeg = 0;
      for (Text val: values) {
        String type = val.toString();
        if (type.equals("src")) {
          outDeg++;
        } else if (type.equals("tgt")) {
          inDeg++;
        }
      }
      int degDiff = outDeg - inDeg;
      diff.set(Integer.toString(degDiff));
      context.write(key, diff);
    }
  }

  public static class DegDiffMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    private Text degDiff = new Text();
    private final static IntWritable one = new IntWritable(1);

    @Override
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
      String[] node_degDiff = value.toString().split("\t");
      degDiff.set(node_degDiff[1]);
      context.write(degDiff, one);
    }
  }

  public static class CountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

    private IntWritable count = new IntWritable();

    @Override
    public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      count.set(sum);
      context.write(key, count);
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "DegDiff");
    job.setJarByClass(Q4.class);
    job.setMapperClass(NodeMapper.class);
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(Text.class);
    //job.setCombinerClass(DegDiffReducer.class);
    job.setReducerClass(DegDiffReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1] + "_temp"));
    job.waitForCompletion(true);

    Job job2 = Job.getInstance(conf, "Count");
    job2.setJarByClass(Q4.class);
    job2.setMapperClass(DegDiffMapper.class);
    job2.setMapOutputKeyClass(Text.class);
    job2.setMapOutputValueClass(IntWritable.class);
    job2.setCombinerClass(CountReducer.class);
    job2.setReducerClass(CountReducer.class);
    job2.setOutputKeyClass(Text.class);
    job2.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job2, new Path(args[1] + "_temp"));
    FileOutputFormat.setOutputPath(job2, new Path(args[1]));
    System.exit(job2.waitForCompletion(true) ? 0 : 1);
  }
}

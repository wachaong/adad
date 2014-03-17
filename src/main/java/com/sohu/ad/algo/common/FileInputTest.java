
package com.sohu.ad.algo.common;
import java.io.IOException;
import java.util.List;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;


public class FileInputTest extends AbstractProcessor{
	public static class FIMapper extends Mapper<LongWritable, InstancesWritable, Text, Text> {
		public void setup(Context context) {
		}
		
		public void map(LongWritable key, InstancesWritable value, Context context)
				throws IOException, InterruptedException {
			List<SingleInstanceWritable> file_instances =value.getFile_instances();
			context.write(new Text("total"), new Text(String.valueOf(file_instances.size())));
			int i=0;
			for(SingleInstanceWritable sins:file_instances){
				context.write(new Text(String.valueOf(i++)), new Text(sins.toString()));
			}
		}
	}

	@Override
	protected void configJob(Job job) {
		// TODO Auto-generated method stub
		job.setMapperClass(FIMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setInputFormatClass(InstanceInputFormat.class);
		job.setOutputKeyClass(Text.class);  
        job.setOutputValueClass(Text.class);
	}
}


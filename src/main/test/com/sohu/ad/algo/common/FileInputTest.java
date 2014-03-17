package com.sohu.ad.algo.common;
import java.io.IOException;
import java.util.List;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import com.sohu.ad.algo.admm.io.InstancesWritable;
import com.sohu.ad.algo.admm.io.SingleInstanceWritable;

public class FileInputTest extends AbstractProcessor{
	public class FIMapper extends Mapper<LongWritable, InstancesWritable, Text, Text> {
		public void setup(Context context) {
		}
		
		public void map(LongWritable key, InstancesWritable value, Context context)
				throws IOException, InterruptedException {
			List<SingleInstanceWritable> file_instances =value.getFile_instances();
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
		job.setOutputKeyClass(Text.class);  
        job.setOutputValueClass(Text.class);
	}
}


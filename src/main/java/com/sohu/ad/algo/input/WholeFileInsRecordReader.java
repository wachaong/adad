package com.sohu.ad.algo.input;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.util.LineReader;

import com.sohu.ad.algo.admm.io.WholeFileRecordReader;

/**
 * Treats keys as offset in file and value as line.
 */
public class WholeFileInsRecordReader extends
		RecordReader<LongWritable, InstancesWritable> {
	private static final Log LOG = LogFactory
			.getLog(WholeFileRecordReader.class.getName());
	
	private CompressionCodecFactory compressionCodecs = null;
	private long start;
	private long pos;
	private long end;
	private LineReader in;
	private int maxLineLength;

	private LongWritable key = null;
	private InstancesWritable value = null;

	@Override
	public void initialize(InputSplit genericSplit, TaskAttemptContext context)
			throws IOException, InterruptedException {
		Configuration job = context.getConfiguration();
		this.maxLineLength = job.getInt("mapred.linerecordreader.maxlength",Integer.MAX_VALUE);
                
		FileSplit split = (FileSplit) genericSplit;
		start = split.getStart();
		end = start + split.getLength();
		final Path file = split.getPath();
		compressionCodecs = new CompressionCodecFactory(context.getConfiguration());
		final CompressionCodec codec = compressionCodecs.getCodec(file);

		// open the file and seek to the start of the split
		FileSystem fs = file.getFileSystem(context.getConfiguration());
		FSDataInputStream fileIn = fs.open(split.getPath());
		boolean skipFirstLine = false;
		if (codec != null) {
			in = new LineReader(codec.createInputStream(fileIn),
					context.getConfiguration());
			end = Long.MAX_VALUE;
		} else {
			if (start != 0) {
				skipFirstLine = true;
				--start;
				fileIn.seek(start);
			}
			in = new LineReader(fileIn, context.getConfiguration());
		}
		if (skipFirstLine) {
			// skip first line and re-establish "start".
			start += in.readLine(new Text(), 0,
					(int) Math.min((long) Integer.MAX_VALUE, end - start));
		}
		this.pos = start;
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		if (key == null) {
		      key = new LongWritable();
		    }
		    key.set(pos);
		if (value == null) {
		      value = new InstancesWritable();
		}
		
		Text lineValue = new Text();
		int newSize = 0;
		while (pos < end) {
			int lineSize = in.readLine(lineValue, maxLineLength, 
					Math.max((int) Math.min(Integer.MAX_VALUE, end - pos),maxLineLength));
			if (lineSize == 0) {
				return newSize > 0;
			}
			pos += lineSize;
			newSize += lineSize;

			//try{
				SingleInstanceWritable ins=new SingleInstanceWritable();
				String[] tokens=lineValue.toString().split("\t",2);
	
				double label=Double.parseDouble(tokens[0]);
				ins.setLabel(label);
				for(String kv:tokens[1].split("\t")){
					String[] fea_value=kv.split(":");
					if(fea_value[1].equals("1"))
						ins.addIdFea(Integer.valueOf(fea_value[0]));
					else{
						ins.addFloatFea(Integer.valueOf(fea_value[0]),Double.valueOf(fea_value[1]));
					}
				}
				value.addIns(ins);	
			//}
			//catch(Exception e){
			//	LOG.info("One instance exception " + e.toString());
			//}
		}
		
		return newSize > 0;
	}

	@Override
	public LongWritable getCurrentKey() throws IOException,
			InterruptedException {
		// TODO Auto-generated method stub
		return key;
	}

	@Override
	public InstancesWritable getCurrentValue() throws IOException,
			InterruptedException {
		// TODO Auto-generated method stub
		return value;
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		if (start == end) {
			return 0.0f;
		} else {
			return Math.min(1.0f, (pos - start) / (float) (end - start));
		}
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		if (in != null) {
			in.close();
		}
	}

}
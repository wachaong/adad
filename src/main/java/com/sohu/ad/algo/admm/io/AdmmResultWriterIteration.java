package com.sohu.ad.algo.admm.io;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;

import java.io.IOException;

public class AdmmResultWriterIteration extends AdmmResultWriter {

    @Override
    public void write(Job conf,
                      FileSystem hdfs,
                      Path hdfsFilePath,
                      Path finalOutputPath) throws IOException {
        Path finalOutputPathFull = new Path(finalOutputPath, hdfsFilePath.getName());
        FSDataInputStream in = hdfs.open(hdfsFilePath);

        getFSAndWriteFile(conf, in, finalOutputPathFull);

    }

}
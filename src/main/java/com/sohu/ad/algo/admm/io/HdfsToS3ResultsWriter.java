package com.sohu.ad.algo.admm.io;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;

import java.io.IOException;

public class HdfsToS3ResultsWriter {
    private Job conf;
    private Path hdfsDirectoryPath;
    private AdmmResultWriter admmResultWriter;
    private Path finalOutputPath;

    public HdfsToS3ResultsWriter(Job conf,
                                 Path hdfsDirectoryPath,
                                 AdmmResultWriter admmResultWriter,
                                 Path finalOutputPath) {
        this.conf = conf;
        this.hdfsDirectoryPath = hdfsDirectoryPath;
        this.admmResultWriter = admmResultWriter;
        this.finalOutputPath = finalOutputPath;
    }

    public void writeToS3() throws IOException {
        FileSystem hdfs = FileSystem.get(conf.getConfiguration());
        FileStatus[] hdfsFiles = hdfs.listStatus(hdfsDirectoryPath);
        Path[] hdfsFilePaths = FileUtil.stat2Paths(hdfsFiles);

        for (Path hdfsFilePath : hdfsFilePaths) {
            FileStatus fileStatus = hdfs.getFileStatus(hdfsFilePath);
            if (!fileStatus.isDir()) {
                admmResultWriter.write(conf, hdfs, hdfsFilePath, finalOutputPath);
            }
        }
    }
}
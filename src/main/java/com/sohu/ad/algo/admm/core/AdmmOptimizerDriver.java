package com.sohu.ad.algo.admm.core;

import com.google.common.base.Optional;
import com.sohu.ad.algo.admm.io.AdmmResultWriter;
import com.sohu.ad.algo.admm.io.AdmmResultWriterBetas;
import com.sohu.ad.algo.admm.io.AdmmResultWriterIteration;
import com.sohu.ad.algo.admm.io.HdfsToS3ResultsWriter;
import com.sohu.ad.algo.admm.io.SignalInputFormat;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

public class AdmmOptimizerDriver extends Configured implements Tool {

    private static final int DEFAULT_ADMM_ITERATIONS_MAX = 2;
    private static final float DEFAULT_REGULARIZATION_FACTOR = 0.000001f;
    private static final String S3_ITERATION_FOLDER_NAME = "iteration_";
    private static final String S3_FINAL_ITERATION_FOLDER_NAME = S3_ITERATION_FOLDER_NAME + "final";
    private static final String S3_STANDARD_ERROR_FOLDER_NAME = "standard-error";
    private static final String S3_BETAS_FOLDER_NAME = "betas";

    public static void main(String[] args) throws Exception {
        ToolRunner.run(new Configuration(), new AdmmOptimizerDriver(), args);
    }

    @Override
    public int run(String[] args) throws IOException, CmdLineException {
        AdmmOptimizerDriverArguments admmOptimizerDriverArguments = new AdmmOptimizerDriverArguments();
        parseArgs(args, admmOptimizerDriverArguments);

        String signalDataLocation = admmOptimizerDriverArguments.getSignalPath();
        String intermediateHdfsBaseString = "/tmp";
        URI finalOutputBaseUrl = admmOptimizerDriverArguments.getOutputPath();
        int iterationsMaximum = Optional.fromNullable(admmOptimizerDriverArguments.getIterationsMaximum()).or(
                DEFAULT_ADMM_ITERATIONS_MAX);
        float regularizationFactor = Optional.fromNullable(admmOptimizerDriverArguments.getRegularizationFactor()).or(
                DEFAULT_REGULARIZATION_FACTOR);
        boolean addIntercept = Optional.fromNullable(admmOptimizerDriverArguments.getAddIntercept()).or(false);
        boolean regularizeIntercept = Optional.fromNullable(admmOptimizerDriverArguments.getRegularizeIntercept()).or(false);
        String columnsToExclude = Optional.fromNullable(admmOptimizerDriverArguments.getColumnsToExclude()).or("");

        int iterationNumber = 0;
        boolean isFinalIteration = false;

        while (!isFinalIteration) {
            long preStatus = 0;
            Job job = new Job(getConf());
            job.setJarByClass(AdmmOptimizerDriver.class);
            
            Path previousHdfsResultsPath = new Path(intermediateHdfsBaseString + S3_ITERATION_FOLDER_NAME + (iterationNumber - 1));
            Path currentHdfsResultsPath = new Path(intermediateHdfsBaseString + S3_ITERATION_FOLDER_NAME + iterationNumber);

            long curStatus = doAdmmIteration(job,
                    previousHdfsResultsPath,
                    currentHdfsResultsPath,
                    signalDataLocation,
                    iterationNumber,
                    columnsToExclude,
                    addIntercept,
                    regularizeIntercept,
                    regularizationFactor);
            isFinalIteration = convergedOrMaxed(curStatus, preStatus, iterationNumber, iterationsMaximum);
            String s3IterationFolderName = getS3IterationFolderName(isFinalIteration, iterationNumber);
            printResultsToS3(job, currentHdfsResultsPath, finalOutputBaseUrl, new AdmmResultWriterIteration(), s3IterationFolderName);

            if (isFinalIteration) {
                printResultsToS3(job, currentHdfsResultsPath, finalOutputBaseUrl, new AdmmResultWriterBetas(),
                        S3_BETAS_FOLDER_NAME);
                Job stdErrJob = new Job(getConf());
                stdErrJob.setJarByClass(AdmmOptimizerDriver.class);
                Path standardErrorHdfsPath = new Path(intermediateHdfsBaseString + S3_STANDARD_ERROR_FOLDER_NAME);
                doStandardErrorCalculation(
                		stdErrJob,
                        currentHdfsResultsPath,
                        standardErrorHdfsPath,
                        signalDataLocation,
                        iterationNumber,
                        columnsToExclude,
                        addIntercept,
                        regularizeIntercept,
                        regularizationFactor);
                printResultsToS3(stdErrJob, standardErrorHdfsPath, finalOutputBaseUrl, new AdmmResultWriterIteration(),
                        S3_STANDARD_ERROR_FOLDER_NAME);
            }
            iterationNumber++;
        }

        return 0;
    }

    private void parseArgs(String[] args, AdmmOptimizerDriverArguments admmOptimizerDriverArguments) throws CmdLineException {
        ArrayList<String> argsList = new ArrayList<String>(Arrays.asList(args));

        for (int i = 0; i < args.length; i++) {
            if (i % 2 == 0 && !AdmmOptimizerDriverArguments.VALID_ARGUMENTS.contains(args[i])) {
                argsList.remove(args[i]);
                argsList.remove(args[i + 1]);
            }
        }

        new CmdLineParser(admmOptimizerDriverArguments).parseArgument(argsList.toArray(new String[argsList.size()]));
    }

    private String getS3IterationFolderName(boolean isFinalIteration, int iterationNumber) {
        return (isFinalIteration) ? S3_FINAL_ITERATION_FOLDER_NAME : S3_ITERATION_FOLDER_NAME + iterationNumber;
    }

    public void printResultsToS3(Job conf, Path hdfsDirectoryPath, URI finalOutputBaseUrl,
                                 AdmmResultWriter admmResultWriter, String finalOutputFolderName) throws IOException {
        Path finalOutputPath = new Path(finalOutputBaseUrl.resolve(finalOutputFolderName).toString());
        HdfsToS3ResultsWriter hdfsToS3ResultsWriter = new HdfsToS3ResultsWriter(conf, hdfsDirectoryPath,
                admmResultWriter, finalOutputPath);
        hdfsToS3ResultsWriter.writeToS3();
    }

    public void doStandardErrorCalculation(Job job,
                                           Path currentHdfsPath,
                                           Path standardErrorHdfsPath,
                                           String signalDataLocation,
                                           int iterationNumber,
                                           String columnsToExclude,
                                           boolean addIntercept,
                                           boolean regularizeIntercept,
                                           float regularizationFactor) throws IOException {
        Path signalDataInputLocation = new Path(signalDataLocation);

        // No addIntercept option as it would be added in the intermediate data by the Admm iterations.
        Configuration conf = job.getConfiguration();
        job.setJobName("ADMM Standard Errors");
        conf.set("mapred.child.java.opts", "-Xmx2g");
        conf.set("previous.intermediate.output.location", currentHdfsPath.toString());
        conf.set("columns.to.exclude", columnsToExclude);
        conf.setInt("iteration.number", iterationNumber);
        conf.setBoolean("add.intercept", addIntercept);
        conf.setBoolean("regularize.intercept", regularizeIntercept);
        conf.setFloat("regularization.factor", regularizationFactor);

        job.setMapperClass(AdmmStandardErrorsMapper.class);
        job.setReducerClass(AdmmStandardErrorsReducer.class);
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(Text.class);
        job.setInputFormatClass(SignalInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.setInputPaths(job, signalDataInputLocation);
        FileOutputFormat.setOutputPath(job, standardErrorHdfsPath);

        job.waitForCompletion(true);
    }

    public long doAdmmIteration(Job job,
                                Path previousHdfsPath,
                                Path currentHdfsPath,
                                String signalDataLocation,
                                int iterationNumber,
                                String columnsToExclude,
                                boolean addIntercept,
                                boolean regularizeIntercept,
                                float regularizationFactor) throws IOException {
        Path signalDataInputLocation = new Path(signalDataLocation);
        
        Configuration conf = job.getConfiguration();
        job.setJobName("ADMM Optimizer " + iterationNumber);
        conf.set("mapred.child.java.opts", "-Xmx2g");
        conf.set("previous.intermediate.output.location", previousHdfsPath.toString());
        conf.setInt("iteration.number", iterationNumber);
        conf.set("columns.to.exclude", columnsToExclude);
        conf.setBoolean("add.intercept", addIntercept);
        conf.setBoolean("regularize.intercept", regularizeIntercept);
        conf.setFloat("regularization.factor", regularizationFactor);

        job.setMapperClass(AdmmIterationMapper.class);
        job.setReducerClass(AdmmIterationReducer.class);
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(Text.class);
        job.setInputFormatClass(SignalInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.setInputPaths(job,signalDataInputLocation);
        FileSystem fs = FileSystem.get(conf);
        if (fs.exists(currentHdfsPath)) {
            fs.delete(currentHdfsPath, true);
        }
        FileOutputFormat.setOutputPath(job, currentHdfsPath);

        job.waitForCompletion(true);

        return job.getCounters().findCounter(AdmmIterationReducer.IterationCounter.ITERATION).getValue();
    }

    private boolean convergedOrMaxed(long curStatus, long preStatus, int iterationNumber, int iterationsMaximum) {
        return curStatus <= preStatus || iterationNumber >= iterationsMaximum;
    }
}

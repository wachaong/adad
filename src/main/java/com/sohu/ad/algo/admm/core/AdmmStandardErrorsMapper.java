package com.sohu.ad.algo.admm.core;

import org.apache.commons.math.linear.OpenMapRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.SparseRealMatrix;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;


import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdmmStandardErrorsMapper extends Mapper<LongWritable, Text, IntWritable, Text> {
    private static final IntWritable ZERO = new IntWritable(0);
    private static final Logger LOG = Logger.getLogger(AdmmIterationMapper.class.getName());
    private static final float DEFAULT_REGULARIZATION_FACTOR = 0.000001f;

    private int iteration;
    private FileSystem fs;
    private Map<String, String> splitToParameters;
    private Set<Integer> columnsToExclude;

    private boolean addIntercept;
    private String previousIntermediateOutputLocation;
    private Path previousIntermediateOutputLocationPath;

    public void setup(Context context) throws IOException,
	InterruptedException  {
    	Configuration conf = context.getConfiguration();
        iteration = Integer.parseInt(conf.get("iteration.number"));
        String columnsToExcludeString = conf.get("columns.to.exclude");
        columnsToExclude = AdmmIterationHelper.getColumnsToExclude(columnsToExcludeString);
        addIntercept = conf.getBoolean("add.intercept", false);
        previousIntermediateOutputLocation = conf.get("previous.intermediate.output.location");
        previousIntermediateOutputLocationPath = new Path(previousIntermediateOutputLocation);

        try {
            fs = FileSystem.get(conf);
        }
        catch (IOException e) {
            LOG.log(Level.FINE, e.toString());
        }

        splitToParameters = getSplitParameters();
    }

    protected Map<String, String> getSplitParameters() {
        return AdmmIterationHelper.readParametersFromHdfs(fs, previousIntermediateOutputLocationPath, iteration);
    }

    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        FileSplit split = (FileSplit) context.getInputSplit();
        String splitId = key.get() + "@" + split.getPath();
        splitId = AdmmIterationHelper.removeIpFromHdfsFileName(splitId);

        double[][] inputSplitData = AdmmIterationHelper.createMatrixFromDataString(value.toString(), columnsToExclude, addIntercept);
        AdmmMapperContext mapperContext = assembleMapperContextFromCache(inputSplitData, splitId);

        AdmmStandardErrorsReducerContext reducerContext = getReducerContext(mapperContext);
        context.write(ZERO, new Text(splitId + "::" + AdmmIterationHelper.admmStandardErrorReducerContextToJson(reducerContext)));
    }

    private AdmmStandardErrorsReducerContext getReducerContext(AdmmMapperContext mapperContext) {
        double[] zFinal = mapperContext.getZInitial();
        double[][] aMatrix = mapperContext.getA();
        int numRows = aMatrix.length;
        int numFeatures = aMatrix[0].length;
        double[][] xwxMatrix = new double[numFeatures][numFeatures];
        double[] rowMultipliers = getRowMultipliers(aMatrix, zFinal);

        SparseRealMatrix xtW = new OpenMapRealMatrix(numFeatures, numRows);
        SparseRealMatrix x = new OpenMapRealMatrix(numRows, numFeatures);
        for(int row = 0; row < numRows; row++) {
            for(int col = 0; col < numFeatures; col++) {
                if(aMatrix[row][col] != 0) {
                    x.setEntry(row, col, aMatrix[row][col]);
                    xtW.setEntry(col, row, aMatrix[row][col] * rowMultipliers[row]);
                }
            }
        }

        RealMatrix xtWX = xtW.multiply(x);

        for(int row = 0; row < numFeatures; row++) {
            for(int col = 0; col < numFeatures; col++) {
                xwxMatrix[row][col] = xtWX.getEntry(row, col);
            }
        }

        return new AdmmStandardErrorsReducerContext(xwxMatrix, mapperContext.getLambdaValue(), numRows);
    }

    private double[] getRowMultipliers(double[][] aMatrix, double[] zFinal) {
        double[] rowMultipliers = new double[aMatrix.length];
        for(int row = 0; row < rowMultipliers.length; row++) {
            double rowProbability = getPredictedProbability(aMatrix, zFinal, row);
            rowMultipliers[row] = rowProbability * (1 - rowProbability);
        }
        return rowMultipliers;
    }

    private double getPredictedProbability(double[][] aMatrix, double[] zFinal, int row) {
        double[] features = aMatrix[row];
        double dotProduct = 0;
        for(int i = 0; i < features.length; i++) {
            dotProduct += features[i] * zFinal[i];
        }
        return Math.exp(dotProduct) / (1 + Math.exp(dotProduct));
    }

    private AdmmMapperContext assembleMapperContextFromCache(double[][] inputSplitData, String splitId) throws IOException {
        if (splitToParameters.containsKey(splitId)) {
            AdmmMapperContext preContext = AdmmIterationHelper.jsonToAdmmMapperContext(splitToParameters.get(splitId));
            return new AdmmMapperContext(inputSplitData,
                    preContext.getUInitial(),
                    preContext.getXInitial(),
                    preContext.getZInitial(),
                    preContext.getRho(),
                    preContext.getLambdaValue(),
                    preContext.getPrimalObjectiveValue(),
                    preContext.getRNorm(),
                    preContext.getSNorm());
        }
        else {
            LOG.log(Level.FINE, "Key not found. Split ID: " + splitId + " Split Map: " + splitToParameters.toString());
            throw new IOException("Key not found.  Split ID: " + splitId + " Split Map: " + splitToParameters.toString());
        }
    }
}

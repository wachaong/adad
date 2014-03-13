package com.sohu.ad.algo.admm.core;

import org.apache.commons.math.linear.DecompositionSolver;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.MatrixUtils;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Pattern;

public class AdmmStandardErrorsReducer extends Reducer<IntWritable, Text, IntWritable, Text> {
    private static final Pattern KEY_VALUE_DELIMITER = Pattern.compile("::");
    private static final IntWritable ZERO = new IntWritable(0);
    private boolean regularizeIntercept;

    public void setup(Context context) throws IOException,
	InterruptedException  {
        regularizeIntercept = context.getConfiguration().getBoolean("regularize.intercept", false);
    }

    public void reduce(IntWritable key, Iterator<Text> values, Context context) throws IOException, 
	InterruptedException {
        AggregatedReducerContexts aggregatedReducerContexts = getAggregatedReducerContexts(values);
        double[] zStandardErrors = getStandardErrors(aggregatedReducerContexts);

        context.write(ZERO, new Text(AdmmIterationHelper.arrayToJson(zStandardErrors)));
    }

    private double[] getStandardErrors(AggregatedReducerContexts aggregatedReducerContexts) {
        // (X'WX + 2*lambda*I)^-1 * (X'WX) * (X'WX + 2*lambda*I)^-1
        RealMatrix xwxMatrix = MatrixUtils.createRealMatrix(aggregatedReducerContexts.getXwxMatrix());
        RealMatrix identityMatrix = MatrixUtils.createRealIdentityMatrix(xwxMatrix.getColumnDimension());
        RealMatrix identityMatrixScaled = identityMatrix.scalarMultiply(
                2 * aggregatedReducerContexts.getRegularizationFactor() * aggregatedReducerContexts.getTotalNumberOfRows());
        if (!regularizeIntercept) {
            identityMatrix.setEntry(0, 0, 0.0);
        }

        RealMatrix toInvert = xwxMatrix.add(identityMatrixScaled);
        DecompositionSolver solver = new LUDecompositionImpl(toInvert).getSolver();
        RealMatrix inverted = solver.getInverse();

        RealMatrix firstTimesSecond = inverted.multiply(xwxMatrix);
        RealMatrix varianceMatrix = firstTimesSecond.multiply(inverted);

        int numFeatures = varianceMatrix.getColumnDimension();
        double[] standardErrors = new double[numFeatures];
        for (int i = 0; i < numFeatures; i++) {
            double variance = varianceMatrix.getEntry(i, i);
            standardErrors[i] = Math.sqrt(variance);
        }

        return standardErrors;
    }

    private AggregatedReducerContexts getAggregatedReducerContexts(Iterator<Text> values) throws IOException {
        double regularizationFactor = -1;
        double[][] xwxMatrix = null;
        int totalTrainingExamples = 0;
        int numFeatures = -1;

        while (values.hasNext()) {
            Text mapperResult = values.next();
            String[] result = KEY_VALUE_DELIMITER.split(mapperResult.toString());
            AdmmStandardErrorsReducerContext context = AdmmIterationHelper.jsonToAdmmStandardErrorsReducerContext(result[1]);
            double[][] currentXwxMatrix = context.getXwxMatrix();
            if (regularizationFactor == -1) {
                regularizationFactor = context.getLambdaValue();
                numFeatures = currentXwxMatrix.length;
                xwxMatrix = new double[numFeatures][numFeatures];
            }
            for (int i = 0; i < numFeatures; i++) {
                for (int j = 0; j < numFeatures; j++) {
                    xwxMatrix[i][j] += currentXwxMatrix[i][j];
                }
            }
            totalTrainingExamples += context.getNumberOfRows();
        }
        return new AggregatedReducerContexts(xwxMatrix, regularizationFactor, totalTrainingExamples);
    }

    private class AggregatedReducerContexts {
        private double[][] xwxMatrix;
        private double regularizationFactor;
        private int totalNumberOfRows;

        AggregatedReducerContexts(double[][] xwxMatrix, double regularizationFactor, int totalNumberOfRows) {
            this.xwxMatrix = xwxMatrix;
            this.regularizationFactor = regularizationFactor;
            this.totalNumberOfRows = totalNumberOfRows;
        }

        private double[][] getXwxMatrix() {
            return xwxMatrix;
        }

        private double getRegularizationFactor() {
            return regularizationFactor;
        }

        private int getTotalNumberOfRows() {
            return totalNumberOfRows;
        }
    }

}

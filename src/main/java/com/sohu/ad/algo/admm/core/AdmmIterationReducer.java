package com.sohu.ad.algo.admm.core;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

public class AdmmIterationReducer extends Reducer<IntWritable, Text, IntWritable, Text> {

    private static final double THRESHOLD = 0.0001;
    private static final Logger LOG = Logger.getLogger(AdmmIterationReducer.class.getName());
    private static final IntWritable ZERO = new IntWritable(0);
    private Map<String, String> outputMap = new HashMap<String, String>();
    private int iteration;
    private int numberOfMappers;
    private boolean regularizeIntercept;

    @Override
    public void setup(Context context) throws IOException,
	InterruptedException {
    	Configuration conf = context.getConfiguration();
        iteration = Integer.parseInt(conf.get("iteration.number"));
        regularizeIntercept = conf.getBoolean("regularize.intercept", false);
        //numberOfMappers = conf.getNumMapTasks();
        numberOfMappers = Integer.valueOf(conf.get("mapred.map.tasks"));
    }

    public void reduce(IntWritable key, Iterator<Text> values, Context context) throws IOException, 
	InterruptedException {

        AdmmReducerContextGroup rcontext = new AdmmReducerContextGroup(values, numberOfMappers, LOG, iteration);
        setOutputMapperValues(rcontext);
        context.write(ZERO, new Text(AdmmIterationHelper.mapToJson(outputMap)));

        if (rcontext.getRNorm() > THRESHOLD || rcontext.getSNorm() > THRESHOLD) {
        	context.getCounter(IterationCounter.ITERATION).increment(1);
        }
    }

    private void setOutputMapperValues(AdmmReducerContextGroup context) throws IOException {
        double[] zUpdated = getZUpdated(context);
        double[][] xUpdated = context.getXUpdated();
        String[] splitIds = context.getSplitIds();

        for (int mapperNumber = 0; mapperNumber < context.getNumberOfMappers(); mapperNumber++) {
            double[] uUpdated = getUUpdated(context, mapperNumber, zUpdated);
            AdmmMapperContext admmMapperContext =
                    new AdmmMapperContext(null, null, uUpdated, xUpdated[mapperNumber], zUpdated,
                            context.getRho() * context.getRhoMultiplier(),
                            context.getLambda(), context.getPrimalObjectiveValue(),
                            context.getRNorm(), context.getSNorm());
            String currentSplitId = splitIds[mapperNumber];
            outputMap.put(currentSplitId, AdmmIterationHelper.admmMapperContextToJson(admmMapperContext));
            LOG.info("Iteration " + iteration + " Reducer Setting splitID " + currentSplitId);
        }
    }

    private double[] getZUpdated(AdmmReducerContextGroup context) {
        int numMappers = context.getNumberOfMappers();
        int numFeatures = context.getNumberOfFeatures();

        double[] xAverage = context.getXUpdatedAverage();
        double[] uAverage = context.getUInitialAverage();
        double[] zUpdated = new double[numFeatures];
        double zMultiplier = (numMappers * context.getRho()) / (2 * context.getLambda() + numMappers * context.getRho());

        for (int i = 0; i < numFeatures; i++) {
            if (i == 0 && !regularizeIntercept) {
                zUpdated[i] = xAverage[i] + uAverage[i];
            }
            else {
                zUpdated[i] = zMultiplier * (xAverage[i] + uAverage[i]);
            }
        }

        return zUpdated;
    }

    private double[] getUUpdated(AdmmReducerContextGroup context, int mapperNumber, double[] zUpdated) {
        int numFeatures = context.getNumberOfFeatures();
        double[] uInitial = context.getUInitial()[mapperNumber];
        double[] xUpdated = context.getXUpdated()[mapperNumber];
        double[] uUpdated = new double[numFeatures];
        double rhoMultiplier = context.getRhoMultiplier();

        for (int i = 0; i < numFeatures; i++) {
            uUpdated[i] = (1 / rhoMultiplier) * (uInitial[i] + xUpdated[i] - zUpdated[i]);
        }
        return uUpdated;
    }

    public static enum IterationCounter {
        ITERATION
    }
}

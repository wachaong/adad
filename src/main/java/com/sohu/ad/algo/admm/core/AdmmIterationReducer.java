package com.sohu.ad.algo.admm.core;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.sohu.ad.algo.math.SparseVector;

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
    	SparseVector zUpdated = getZUpdated(context);
    	SparseVector[] xUpdated = context.getXUpdated();
        String[] splitIds = context.getSplitIds();

        for (int mapperNumber = 0; mapperNumber < context.getNumberOfMappers(); mapperNumber++) {
        	SparseVector uUpdated = getUUpdated(context, mapperNumber, zUpdated);
            AdmmMapperContext admmMapperContext =
                    new AdmmMapperContext(uUpdated, xUpdated[mapperNumber], zUpdated,
                            context.getRho() * context.getRhoMultiplier(),
                            context.getLambda(), context.getPrimalObjectiveValue(),
                            context.getRNorm(), context.getSNorm());
            String currentSplitId = splitIds[mapperNumber];
            outputMap.put(currentSplitId, AdmmIterationHelper.admmMapperContextToJson(admmMapperContext));
            LOG.info("Iteration " + iteration + " Reducer Setting splitID " + currentSplitId);
        }
    }

    private SparseVector getZUpdated(AdmmReducerContextGroup context) {
        int numMappers = context.getNumberOfMappers();

        SparseVector xAverage = context.getXUpdatedAverage();
        SparseVector uAverage = context.getUInitialAverage();
        SparseVector zUpdated = new SparseVector();

        double zMultiplier = (numMappers * context.getRho()) / (2 * context.getLambda() + numMappers * context.getRho());
        zUpdated = xAverage.add(uAverage).scale(zMultiplier);
        if(!regularizeIntercept)
        {
        	zUpdated.setValue(0, xAverage.getValue(0) + uAverage.getValue(0));
        }
        return zUpdated;
    }

    private SparseVector getUUpdated(AdmmReducerContextGroup context, int mapperNumber, SparseVector zUpdated) {
 
        SparseVector uInitial = context.getUInitial()[mapperNumber];
        SparseVector xUpdated = context.getXUpdated()[mapperNumber];
        SparseVector uUpdated = new SparseVector();
        //?
        uUpdated = uInitial.add(xUpdated).minus(zUpdated).scale(1.0/context.getRhoMultiplier());        
        return uUpdated;
    }

    public static enum IterationCounter {
        ITERATION
    }
}

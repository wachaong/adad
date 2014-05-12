package com.sohu.ad.algo.admm.core;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import com.sohu.ad.algo.input.InstancesWritable;
import com.sohu.ad.algo.input.SingleInstanceWritable;
import com.sohu.ad.algo.math.LBFGS;
import com.sohu.ad.algo.models.LR;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


public class AdmmIterationMapper extends Mapper<LongWritable, InstancesWritable, IntWritable, Text> {
	
    private static final IntWritable ZERO = new IntWritable(0);
    private static final Logger LOG = Logger.getLogger(AdmmIterationMapper.class.getName());
    private static final float DEFAULT_REGULARIZATION_FACTOR = 0.000001f;
    private static final float DEFAULT_RHO = 0.1f;

    private int iteration;
    private FileSystem fs;
    private Map<String, String> splitToParameters;

    LBFGS lbfgsOptimizer = new LBFGS();
    private float regularizationFactor;
    private double rho;
    private String previousIntermediateOutputLocation;
    private Path previousIntermediateOutputLocationPath;

    @Override
    public void setup(Context context) throws IOException,
	InterruptedException {
    	Configuration conf = context.getConfiguration();
        iteration = Integer.parseInt(conf.get("iteration.number"));
        String columnsToExcludeString = conf.get("columns.to.exclude");
        //columnsToExclude = AdmmIterationHelper.getColumnsToExclude(columnsToExcludeString);
        //addIntercept = conf.getBoolean("add.intercept", false);
        rho = conf.getFloat("rho", DEFAULT_RHO);
        regularizationFactor = conf.getFloat("regularization.factor", DEFAULT_REGULARIZATION_FACTOR);
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

    @Override
    public void map(LongWritable key, InstancesWritable value, Context context) throws IOException, InterruptedException {
        FileSplit split = (FileSplit) context.getInputSplit();
        String splitId = key.get() + "@" + split.getPath();
        splitId = AdmmIterationHelper.removeIpFromHdfsFileName(splitId);

        List<SingleInstanceWritable> file_instances =value.getFile_instances();
        
        AdmmMapperContext mapperContext;
        if (iteration == 0) {
            mapperContext = new AdmmMapperContext(value, rho);
        }
        else {
            mapperContext = assembleMapperContextFromCache(value, splitId);
        }
        AdmmReducerContext reducerContext = localMapperOptimization(mapperContext);

        LOG.info("Iteration " + iteration + "Mapper outputting splitId " + splitId);
        context.write(ZERO, new Text(splitId + "::" + AdmmIterationHelper.admmReducerContextToJson(reducerContext)));
    }

    private AdmmReducerContext localMapperOptimization(AdmmMapperContext context) {
    	
    	LR lr_map = new LR(context.getXInitial(), 1.0);
    	lr_map.train(context.getDataset());
        
        double primalObjectiveValue = myFunction.evaluatePrimalObjective(optimizationContext.m_optimumX);
        return new AdmmReducerContext(context.getUInitial(),
                context.getXInitial(),
                optimizationContext.m_optimumX,
                context.getZInitial(),
                primalObjectiveValue,
                context.getRho(),
                regularizationFactor);
    }

    private AdmmMapperContext assembleMapperContextFromCache(InstancesWritable file_instances, String splitId) throws IOException {
    	if (splitToParameters.containsKey(splitId)) {
            AdmmMapperContext preContext = AdmmIterationHelper.jsonToAdmmMapperContext(splitToParameters.get(splitId));
            return new AdmmMapperContext(file_instances,
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
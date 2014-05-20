package com.sohu.ad.algo.admm.core;

import org.apache.hadoop.io.Text;

import com.sohu.ad.algo.math.SparseVector;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class AdmmReducerContextGroup {

    private static final Pattern KEY_VALUE_DELIMITER = Pattern.compile("::");
    private static final double SQUARE_ROOT_POWER = 0.5;
    private static final double RHO_INCREMENT_MULTIPLIER = 1.5;
    private static final double RHO_DECREMENT_MULTIPLIER = 1.5;
    private static final double RHO_UPDATE_THRESHOLD = 5;
    private final SparseVector[] uInitial;
    private final SparseVector[] xInitial;
    private final SparseVector[] xUpdated;
    private final SparseVector zInitial;
    private final String[] splitIds;
    private final double rho;
    private final double lambdaValue;
    private final int numberOfMappers;
    private final double rNorm;
    private final double sNorm;
    private final double primalObjectiveValue;

    public AdmmReducerContextGroup(Iterator<Text> mapperResults, int numberOfMappers, Logger logger, int iteration)
            throws IOException {
        this.numberOfMappers = numberOfMappers;
        uInitial = new SparseVector[numberOfMappers];
        xInitial = new SparseVector[numberOfMappers];
        xUpdated = new SparseVector[numberOfMappers];
        splitIds = new String[numberOfMappers];
        
        String[] result = getNextResult(mapperResults);
        String splitId = result[0];
        AdmmReducerContext context = AdmmIterationHelper.jsonToAdmmReducerContext(result[1]);
        rho = context.getRho();
        lambdaValue = context.getLambdaValue();
        zInitial = context.getZInitial();
        logger.info("Iteration " + iteration + " Reducer Getting splitId " + splitId);

        int contextNumber = 0;
        double primalObjectiveValueLoop = 0;

        while (result != null) {
            splitId = result[0];
            context = AdmmIterationHelper.jsonToAdmmReducerContext(result[1]);
            primalObjectiveValueLoop += context.getPrimalObjectiveValue();
            uInitial[contextNumber] = context.getUInitial();
            xInitial[contextNumber] = context.getXInitial();
            xUpdated[contextNumber] = context.getXUpdated();
            splitIds[contextNumber] = splitId;
            result = getNextResult(mapperResults);
            contextNumber++;
        }

        primalObjectiveValue = primalObjectiveValueLoop;
        rNorm = calculateRNorm();
        sNorm = calculateSNorm();
    }

    private String[] getNextResult(Iterator<Text> mapperResults) throws IOException {
        if (mapperResults.hasNext()) {
            Text mapperResult = mapperResults.next();
            return KEY_VALUE_DELIMITER.split(mapperResult.toString());
        }
        else {
            return null;
        }
    }

    private int getNumberOfMappers(Iterator<Text> mapperResults) {
        int numberOfMappers = 0;
        while (mapperResults.hasNext()) {
            mapperResults.next();
            numberOfMappers++;
        }
        return numberOfMappers;
    }

    private SparseVector getAverage(SparseVector[] toAverage) {
    	SparseVector average = new SparseVector();
        for (SparseVector sv : toAverage) {
        	average.add(sv);
        }
        return average.scale(1.0/toAverage.length);
    }

    private double calculateRNorm() {
        double result = 0.0;
        SparseVector xUpdatedAverage = getXUpdatedAverage();
        for (SparseVector thisXUpdated : xUpdated) {
        	result += thisXUpdated.minus(xUpdatedAverage).norm_2();
        }
        result = Math.pow(result, SQUARE_ROOT_POWER);
        return result;
    }

    private double calculateSNorm() {
    	SparseVector xPreviousAverage = getXInitialAverage();
    	SparseVector xUpdatedAverage = getXUpdatedAverage();
        double result = xUpdatedAverage.minus(xPreviousAverage).norm_2();
        result *= Math.pow(rho, 2);
        result *= numberOfMappers;
        result = Math.pow(result, SQUARE_ROOT_POWER);
        return result;
    }

    public double getRhoMultiplier() {
        double rNorm = getRNorm();
        double sNorm = getSNorm();

        if (rNorm > RHO_UPDATE_THRESHOLD * sNorm) {
            return RHO_INCREMENT_MULTIPLIER;
        }
        else if (sNorm > RHO_UPDATE_THRESHOLD * rNorm) {
            return 1.0 / RHO_DECREMENT_MULTIPLIER;
        }
        else {
            return 1.0;
        }
    }

    public int getNumberOfMappers() {
        return numberOfMappers;
    }

    public double getLambda() {
        return lambdaValue;
    }

    public double getRho() {
        return rho;
    }

    public SparseVector getUInitialAverage() {
        return getAverage(uInitial);
    }

    public SparseVector getXInitialAverage() {
        return getAverage(xInitial);
    }

    public SparseVector getXUpdatedAverage() {
        return getAverage(xUpdated);
    }

    public SparseVector[] getXUpdated() {
        return xUpdated;
    }

    public SparseVector[] getUInitial() {
        return uInitial;
    }

    public String[] getSplitIds() {
        return splitIds;
    }

    public double getRNorm() {
        return rNorm;
    }

    public double getSNorm() {
        return sNorm;
    }

    public double getPrimalObjectiveValue() {
        return primalObjectiveValue;
    }
}

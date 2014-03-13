package com.sohu.ad.algo.admm.core;

import org.apache.hadoop.io.Text;

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
    private final double[][] uInitial;
    private final double[][] xInitial;
    private final double[][] xUpdated;
    private final double[] zInitial;
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
        String[] result = getNextResult(mapperResults);
        AdmmReducerContext context = AdmmIterationHelper.jsonToAdmmReducerContext(result[1]);
        String splitId = result[0];
        logger.info("Iteration " + iteration + " Reducer Getting splitId " + splitId);

        rho = context.getRho();
        lambdaValue = context.getLambdaValue();
        zInitial = context.getZInitial();
        uInitial = new double[numberOfMappers][];
        xInitial = new double[numberOfMappers][];
        xUpdated = new double[numberOfMappers][];
        splitIds = new String[numberOfMappers];

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

    private double[] getAverage(double[][] toAverage) {
        double[] average = new double[toAverage[0].length];

        for (double[] aToAverage : toAverage) {
            for (int j = 0; j < aToAverage.length; j++) {
                average[j] += aToAverage[j];
            }
        }
        for (int j = 0; j < average.length; j++) {
            average[j] = average[j] / toAverage.length;
        }

        return average;
    }

    private double calculateRNorm() {
        double result = 0.0;
        double[] xUpdatedAverage = getXUpdatedAverage();
        for (double[] thisXUpdated : xUpdated) {
            for (int j = 0; j < xUpdatedAverage.length; j++) {
                result += Math.pow(thisXUpdated[j] - xUpdatedAverage[j], 2);
            }
        }
        result = Math.pow(result, SQUARE_ROOT_POWER);

        return result;
    }

    private double calculateSNorm() {
        double[] xPreviousAverage = getXInitialAverage();
        double[] xUpdatedAverage = getXUpdatedAverage();
        double result = 0.0;
        for (int i = 0; i < xPreviousAverage.length; i++) {
            result += Math.pow(xPreviousAverage[i] - xUpdatedAverage[i], 2);
        }
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

    public int getNumberOfFeatures() {
        return zInitial.length;
    }

    public double getLambda() {
        return lambdaValue;
    }

    public double getRho() {
        return rho;
    }

    public double[] getUInitialAverage() {
        return getAverage(uInitial);
    }

    public double[] getXInitialAverage() {
        return getAverage(xInitial);
    }

    public double[] getXUpdatedAverage() {
        return getAverage(xUpdated);
    }

    public double[][] getXUpdated() {
        return xUpdated;
    }

    public double[][] getUInitial() {
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

package com.sohu.ad.algo.admm.core;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.codehaus.jackson.annotate.JsonProperty;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class AdmmStandardErrorsReducerContext implements Writable {

    @JsonProperty("xwxMatrix")
    private double[][] xwxMatrix;
    @JsonProperty("lambdaValue")
    private double lambdaValue;
    @JsonProperty("numberOfRows")
    private int numberOfRows;

    public AdmmStandardErrorsReducerContext() {
    }

    public AdmmStandardErrorsReducerContext(double[][] xwxMatrix, double lambdaValue, int numberOfRows) {
        this.xwxMatrix = xwxMatrix;
        this.lambdaValue = lambdaValue;
        this.numberOfRows = numberOfRows;
    }

    public void setAdmmStandardErrorsReducerContext(AdmmStandardErrorsReducerContext context) {
        this.xwxMatrix = context.xwxMatrix;
        this.lambdaValue = context.lambdaValue;
        this.numberOfRows = context.numberOfRows;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        Text contextJson = new Text(AdmmIterationHelper.admmStandardErrorReducerContextToJson(this));
        contextJson.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        Text contextJson = new Text();
        contextJson.readFields(in);
        setAdmmStandardErrorsReducerContext(AdmmIterationHelper.jsonToAdmmStandardErrorsReducerContext(contextJson.toString()));
    }

    @JsonProperty("xwxMatrix")
    public double[][] getXwxMatrix() {
        return xwxMatrix;
    }

    @JsonProperty("lambdaValue")
    public double getLambdaValue() {
        return lambdaValue;
    }

    @JsonProperty("numberOfRows")
    public int getNumberOfRows() {
        return numberOfRows;
    }


}

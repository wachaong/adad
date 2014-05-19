package com.sohu.ad.algo.admm.core;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import com.sohu.ad.algo.admm.tools.MyPair;
import com.sohu.ad.algo.input.InstancesWritable;
import com.sohu.ad.algo.input.SingleInstanceWritable;
import com.sohu.ad.algo.math.*;


public class AdmmMapperContext implements Writable {

    private static final double LAMBDA_VALUE = 1e-6;
    
    @JsonProperty("uInitial")
    private SparseVector uInitial = null;

    @JsonProperty("xInitial")
    private SparseVector xInitial = null;

    @JsonProperty("zInitial")
    private SparseVector zInitial = null;

    @JsonProperty("rho")
    private double rho;

    @JsonProperty("lambdaValue")
    private double lambdaValue;

    @JsonProperty("primalObjectiveValue")
    private double primalObjectiveValue; 

    @JsonProperty("rNorm")
    private double rNorm;

    @JsonProperty("sNorm")
    private double sNorm;
    
    public AdmmMapperContext(double rho) {	
    	 uInitial = new SparseVector();
         xInitial = new SparseVector();
         zInitial = new SparseVector();

         rho = 1.0;
         lambdaValue = LAMBDA_VALUE;
         primalObjectiveValue = -1;
         rNorm = -1;
         sNorm = -1;
         this.rho = rho;
    }
    
    public AdmmMapperContext(SparseVector uInitial,
                             SparseVector xInitial,
                             SparseVector zInitial,
                             double rho,
                             double lambdaValue,
                             double primalObjectiveValue,
                             double rNorm,
                             double sNorm) {
    	    	
        this.uInitial = uInitial;
        this.xInitial = xInitial;
        this.zInitial = zInitial;
        this.rho = rho;
        this.lambdaValue = lambdaValue;
        this.primalObjectiveValue = primalObjectiveValue;
        this.rNorm = rNorm;
        this.sNorm = sNorm;
    }

    public AdmmMapperContext() {
    }

    public void setAdmmMapperContext(AdmmMapperContext context) {
        this.uInitial = context.uInitial;
        this.xInitial = context.xInitial;
        this.zInitial = context.zInitial;
        this.rho = context.rho;
        this.lambdaValue = context.lambdaValue;
        this.primalObjectiveValue = context.primalObjectiveValue;
        this.rNorm = context.rNorm;
        this.sNorm = context.sNorm;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        Text contextJson = new Text(AdmmIterationHelper.admmMapperContextToJson(this));
        contextJson.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        Text contextJson = new Text();
        contextJson.readFields(in);
        setAdmmMapperContext(AdmmIterationHelper.jsonToAdmmMapperContext(contextJson.toString()));
    }

    @JsonProperty("uInitial")
    public SparseVector getUInitial() {
        return uInitial;
    }

    @JsonProperty("xInitial")
    public SparseVector getXInitial() {
        return xInitial;
    }

    @JsonProperty("zInitial")
    public SparseVector getZInitial() {
        return zInitial;
    }

    @JsonProperty("rho")
    public double getRho() {
        return rho;
    }

    @JsonProperty("rho")
    public void setRho(double rho) {
        this.rho = rho;
    }

    @JsonProperty("lambdaValue")
    public double getLambdaValue() {
        return lambdaValue;
    }

    @JsonProperty("primalObjectiveValue")
    public double getPrimalObjectiveValue() {
        return primalObjectiveValue;
    }

    @JsonProperty("primalObjectiveValue")
    public void setPrimalObjectiveValue(double primalObjectiveValue) {
        this.primalObjectiveValue = primalObjectiveValue;
    }

    @JsonProperty("rNorm")
    public double getRNorm() {
        return rNorm;
    }

    @JsonProperty("rNorm")
    public void setRNorm(double rNorm) {
        this.rNorm = rNorm;
    }

    @JsonProperty("sNorm")
    public double getSNorm() {
        return sNorm;
    }

    @JsonProperty("sNorm")
    public void setSNorm(double sNorm) {
        this.sNorm = sNorm;
    }
}
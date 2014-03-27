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

   // @JsonProperty("a")
    //private double[][] a;
    //private SparseMatrix a = null;

    //@JsonProperty("b")
    //private double[] b;
    //private SparseVector b = null;
    
    @JsonProperty("dataset")
    private Dataset dataset = null;

    @JsonProperty("uInitial")
    //private double[] uInitial;
    private SparseVector uInitial = null;

    @JsonProperty("xInitial")
    //private double[] xInitial;
    private SparseVector xInitial = null;

    @JsonProperty("zInitial")
    //private double[] zInitial;
    private SparseVector zInitial = null;

    @JsonProperty("rho")
    private double rho;

    @JsonProperty("lambdaValue")
    private double lambdaValue;

    @JsonProperty("primalObjectiveValue")
    private double primalObjectiveValue;   //原始目标函数值

    @JsonProperty("rNorm")
    private double rNorm;

    @JsonProperty("sNorm")
    private double sNorm;
    

    /*
    public AdmmMapperContext(double[][] ab) {
        b = new double[ab.length];
        a = new double[ab.length][ab[0].length - 1];

        for (int row = 0; row < ab.length; row++) {
            b[row] = ab[row][ab[row].length - 1];
            for (int col = 0; col < ab[row].length - 1; col++) {
                a[row][col] = ab[row][col];
            }
        }

        uInitial = new double[a[0].length];
        xInitial = new double[a[0].length];
        zInitial = new double[a[0].length];

        rho = 1.0;
        lambdaValue = LAMBDA_VALUE;
        primalObjectiveValue = -1;
        rNorm = -1;
        sNorm = -1;
    }
    */
    
    public AdmmMapperContext(InstancesWritable instances) {
    	List<SingleInstanceWritable> file_instances =instances.getFile_instances();
    	dataset = new Dataset(file_instances.size());
    	int i = 0;
    	for(SingleInstanceWritable instance : file_instances){
    		dataset.getData().add(new Sample());
    		dataset.getData().get(i).setLabel(instance.getLabel());
    		for(int idx : instance.getId_fea_vec()) {
    			dataset.getData().get(i).setFeature(idx, 1.0);
    		}
    		for(MyPair<Integer, Double> pair : instance.getFloat_fea_vec()) {
    			dataset.getData().get(i).setFeature(pair.getFirst(), pair.getSecond());	
    		}
    		i+=1;
    	}
    	
    	 uInitial = new SparseVector();
         xInitial = new SparseVector();
         zInitial = new SparseVector();

         rho = 1.0;
         lambdaValue = LAMBDA_VALUE;
         primalObjectiveValue = -1;
         rNorm = -1;
         sNorm = -1;
    	
    }
    
    public AdmmMapperContext(InstancesWritable instances, double rho) {
        this(instances);
        this.rho = rho;
    }

    /*
    public AdmmMapperContext(double[][] ab, double[] uInitial, double[] xInitial, double[] zInitial, double rho, double lambdaValue,
                             double primalObjectiveValue, double rNorm, double sNorm) {
        b = new double[ab.length];
        a = new double[ab.length][ab[0].length - 1];

        for (int row = 0; row < ab.length; row++) {
            b[row] = ab[row][ab[row].length - 1];
            for (int col = 0; col < ab[row].length - 1; col++) {
                a[row][col] = ab[row][col];
            }
        }

        this.uInitial = uInitial;
        this.xInitial = xInitial;
        this.zInitial = zInitial;

        this.rho = rho;
        this.lambdaValue = lambdaValue;
        this.primalObjectiveValue = primalObjectiveValue;
        this.rNorm = rNorm;
        this.sNorm = sNorm;
    }
    */

    public AdmmMapperContext(InstancesWritable instances,
                             SparseVector uInitial,
                             SparseVector xInitial,
                             SparseVector zInitial,
                             double rho,
                             double lambdaValue,
                             double primalObjectiveValue,
                             double rNorm,
                             double sNorm) {
    	
    	List<SingleInstanceWritable> file_instances =instances.getFile_instances();
    	dataset = new Dataset(file_instances.size());
    	int i = 0;
    	for(SingleInstanceWritable instance : file_instances){
    		dataset.getData().add(new Sample());
    		dataset.getData().get(i).setLabel(instance.getLabel());
    		for(int idx : instance.getId_fea_vec()) {
    			dataset.getData().get(i).setFeature(idx, 1.0);
    		}
    		for(MyPair<Integer, Double> pair : instance.getFloat_fea_vec()) {
    			dataset.getData().get(i).setFeature(pair.getFirst(), pair.getSecond());	
    		}
    		i+=1;
    	}
    	
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
        this.dataset = context.dataset;
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

    /*
    @JsonProperty("a")
    public double[][] getA() {
        return a;
    }

    @JsonProperty("b")
    public double[] getB() {
        return b;
    }
    */
    @JsonProperty("dataset")
    public Dataset getDataset() {
        return dataset;
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
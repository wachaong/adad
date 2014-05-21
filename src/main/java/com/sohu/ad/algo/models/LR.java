package com.sohu.ad.algo.models;

import com.sohu.ad.algo.admm.tools.MyPair;
import com.sohu.ad.algo.input.InstancesWritable;
import com.sohu.ad.algo.input.SingleInstanceWritable;
import com.sohu.ad.algo.math.*;

import java.util.*;
import java.io.*;

import org.codehaus.jackson.annotate.JsonProperty;


public class LR implements Model  {
	
	private SparseVector w = new SparseVector();  //parameters
	private double lambda = 0.0; //regular coefficient
	private double rho;
    private SparseVector u = null;
    private SparseVector z = null;
	
	//Constructors
	public LR() {
	}
	
	public LR(SparseVector _w, SparseVector _u, SparseVector _z, double _rho, double _lambda) {
		rho = _rho;
		lambda = _lambda;
		w.assign(_w);
		u.assign(_u);
		z.assign(_z);
	}
	
	/*
	 * loss function for one sample(non-Javadoc)
	 * NLL : negative log-likelihood
	 * @see com.sohu.ad.algo.models.Model#loss(com.sohu.ad.algo.math.Sample)
	 */
	public double loss(SingleInstanceWritable s) {
		double weight_sum = 0.0;
		for(int idx : s.getId_fea_vec()) {
			weight_sum += w.getValue(idx);
		}
		for(MyPair<Integer, Double> pair : s.getFloat_fea_vec()) {
			double tmp = w.getValue(pair.getFirst()) * pair.getSecond();
			weight_sum += tmp;
		}
		return Math.log(1 + Math.exp(-s.getLabel() * weight_sum));
	}
	
	public double loss(InstancesWritable dataset) {
		int m = 0;
		double loss_sum = 0.0;
		for(SingleInstanceWritable instance : dataset.getFile_instances()){
			loss_sum += loss(instance);
			m++;
		}
		double dual_penalty_tmp =  w.minus(u).add(z).norm_2();
		double loss = loss_sum/m + dual_penalty_tmp*rho/2.0;
		return loss;
	}

	@Override
	public void loadModel(String path) throws FileNotFoundException, IOException {
			Scanner scanner = new Scanner(path);
			w.getData().clear();
			int key;
			double value;
			while(scanner.hasNext()) {
				key = scanner.nextInt();
				scanner.nextByte();
				value = scanner.nextDouble();
				w.setValue(key, value);
			}
	
			scanner.close();
	}

	@Override
	public void saveModel(String path) throws FileNotFoundException {
		PrintWriter printer = new PrintWriter(path);
		printer.println(w.toString());
		printer.close();
		
	}

	@Override
	/* 
	 * train the model with batch method(non-Javadoc)
	 * @see com.sohu.ad.algo.models.Model#train(com.sohu.ad.algo.math.Dataset)
	 * using LBFGS
	 */
	public double trainBatch(InstancesWritable dataset) {
		ObjectFunBatch f = new ObjectFunBatch(dataset);
		GradientFunBatch df = new GradientFunBatch(dataset);
		LBFGS lbfgs = new LBFGS();
		lbfgs.minimize(f, df, w);
		return f.eval(w);
	}
	
	public SparseVector getWeight() {
        return w;
    }
	

	@Override
	public double predict(SparseVector features) {
		return Util.sigmoid(features.dot(w));
	}
	
	public class ObjectFunBatch implements ObjectFunction<SparseVector> {
		private InstancesWritable dataset = null;
		
		public ObjectFunBatch(InstancesWritable data) {
			dataset = data;
		}
		
		@Override
		public double eval(SparseVector w) {
			double ans = 0.0;
			for(SingleInstanceWritable instance : dataset.getFile_instances()){
				ans += loss(instance);
			}
			//regular
			ans += LR.this.lambda * Math.pow(w.norm_2(), 2);
			return ans;	
		}
		
	}
	 
	public class GradientFunBatch implements GradientFunction<SparseVector> {
		private InstancesWritable dataset = null;
		public GradientFunBatch(InstancesWritable data) {
			dataset = data;
		}
		
		@Override
		public SparseVector gradient(SparseVector w) {
			SparseVector dw = new SparseVector();
			for(SingleInstanceWritable instance : dataset.getFile_instances()){
				double weight_sum = 0.0;
				for(int idx : instance.getId_fea_vec()) {
					weight_sum += w.getValue(idx);
				}
				for(MyPair<Integer, Double> pair : instance.getFloat_fea_vec()) {
					double tmp = w.getValue(pair.getFirst()) * pair.getSecond();
					weight_sum += tmp;
				}
				double pctr = Util.sigmoid(-instance.getLabel() * weight_sum);
				
				for(int i : instance.getId_fea_vec()) {
					double tmp = (1 - pctr) * instance.getLabel();
					dw.setValue(i, dw.getValue(i) + tmp);
				}
				for(MyPair<Integer, Double> pair : instance.getFloat_fea_vec()) {
					double tmp = (1 - pctr) * instance.getLabel() * pair.getSecond();
					dw.setValue(pair.getFirst(), dw.getValue(pair.getFirst()) + tmp);
				}
			}
			for(int i : w.getData().keySet()) {
				dw.setValue(i, dw.getValue(i) + 2 * LR.this.lambda * w.getValue(i));
			}
			return dw;
		}
	}
}

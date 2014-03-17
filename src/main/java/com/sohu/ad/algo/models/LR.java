package com.sohu.ad.algo.models;

import com.sohu.ad.algo.math.*;

import java.nio.file.Paths;
import java.util.*;
import java.io.*;


public class LR implements Model  {
	
	private SparseVector w = new SparseVector();  //parameters
	private double lambda = 0.0; //regular coefficient
	
	//Constructors
	public LR() {
	}
	
	public LR(SparseVector _w, double _lambda) {
		lambda = _lambda;
		w.assign(_w);
	}
	
	public LR(String w_str, double _lambda) {
		lambda = _lambda;
		Scanner scanner = new Scanner(w_str);
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
	
	/*
	 * loss function for one sample(non-Javadoc)
	 * @see com.sohu.ad.algo.models.Model#loss(com.sohu.ad.algo.math.Sample)
	 */
	@Override
	public double loss(Sample s) {
		double tmp = 0.0;
		for(int i : s.getFeatures().getData().keySet()) {
			tmp += s.getFeature(i) * w.getValue(i);
		}
		tmp *= s.getLabel();
		return Math.log(Util.sigmoid(tmp));
	}


	@Override
	public void loadModel(String path) throws FileNotFoundException, IOException {

			Scanner scanner = new Scanner(Paths.get(path));
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
	public void train(Dataset dataset) {
		ObjectFunBatch f = new ObjectFunBatch(dataset);
		GradientFunBatch df = new GradientFunBatch(dataset);
		LBFGS lbfgs = new LBFGS();
		lbfgs.minimize(f, df, w);
	}
	

	@Override
	public double predict(SparseVector features) {
		double tmp = features.dot(w);
		return Util.sigmoid(tmp); 
	}
	
	public class ObjectFunBatch implements ObjectFunction<SparseVector> {
		private Dataset dataset = null;
		
		public ObjectFunBatch(Dataset data) {
			dataset = data;
		}
		

		@Override
		public double eval(SparseVector w) {
			double ans = 0.0;
			for(Sample sample : dataset.getData()) {
				ans += LR.this.loss(sample);
			}
			//regular
			ans += LR.this.lambda * Math.pow(w.norm_2(), 2);
			return ans;	
		}
		
	}
	 
	public class GradientFunBatch implements GradientFunction<SparseVector> {
		private Dataset dataset;
		public GradientFunBatch(Dataset data) {
			dataset = data;
		}
		
		@Override
		public SparseVector eval(SparseVector w) {
			SparseVector dw = new SparseVector();
			for(Sample sample : dataset.getData()) {
				for(int i : w.getData().keySet()) {
					double tmp = 0.0;
					tmp = 1 - Util.sigmoid(-sample.getLabel() * w.getValue(i) * sample.getFeature(i));
					tmp *= -sample.getLabel() * sample.getFeature(i);
					dw.setValue(i, tmp);
				}
			}
			for(int i : w.getData().keySet()) {
				dw.setValue(i, dw.getValue(i) + 2 * LR.this.lambda * w.getValue(i));
			}
			
			return dw;
		}
	}

}

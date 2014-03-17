package com.sohu.ad.algo.math;

public class Sample {
	private double label = 0.0;
	private SparseVector features = new SparseVector();
	
	public Sample() {
	}
	
	public Sample(double _label, SparseVector f) {
		label = _label;
		for(int i : f.getData().keySet()) {
			features.setValue(i, f.getValue(i));
		}
	}
	
	public double getLabel() {
		return label;
	}
	
	public void setLabel(double l) {
		label = l;
	}
	
	public SparseVector getFeatures() {
		return features;
	}
	
	public double getFeature(int i) {
		return features.getValue(i);
	}
	
	public void setFeature(int i, double value) {
		features.setValue(i, value);
	}
}

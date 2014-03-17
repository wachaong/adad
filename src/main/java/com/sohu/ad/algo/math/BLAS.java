package com.sohu.ad.algo.math;

public class BLAS {
	
	// v1 + v2
	public static SparseVector add(SparseVector v1, SparseVector v2) {
		SparseVector v = new SparseVector(v1);
		for(int i : v2.getData().keySet()) {
			v.setValue(i, v.getValue(i) + v2.getValue(i));
		}
		return v;
	}
	
	// v1 - v2
	public static SparseVector minus(SparseVector v1, SparseVector v2) {
		SparseVector v = new SparseVector(v1);
		for(int i : v2.getData().keySet()) {
			v.setValue(i, v.getValue(i) - v2.getValue(i));
		}
		return v;
	}
	
	// v1 * v2
	public static double dot(SparseVector v1, SparseVector v2) {
		double ans = 0.0;
		for(int i : v1.getData().keySet()) {
			ans += v1.getValue(i) * v2.getValue(i);
		}
		return ans;
	}
	
	// v <- v * alpha
	public static void scale(SparseVector v, double alpha) {
		for(int i : v.getData().keySet()) {
			v.setValue(i, alpha * v.getValue(i));
		}
	}
	
	// y <- a * x + y
	public static void axpy(double a, SparseVector x, SparseVector y) {
		for(int i : x.getData().keySet()) {
			y.setValue(i, y.getValue(i) + a * x.getValue(i));
		}
	}
	
	//
}

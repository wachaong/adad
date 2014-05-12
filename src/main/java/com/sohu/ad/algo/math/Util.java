package com.sohu.ad.algo.math;

public class Util {
	
	public static final double TOL = 1e-10;

	public static double sigmoid(double x) {
		if(x > 40.0) {
			return 1;
		}
		else if(x < -40.0) {
			return -1;
		}
		else {
			return 1.0 / (1 + Math.exp(x));
		}
		
	}
	
	//check if a float number equals zero
	public static boolean equalsZero(double x) {
		return Math.abs(x) < TOL;
	}
	

}

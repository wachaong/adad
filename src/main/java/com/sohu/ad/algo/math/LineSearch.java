package com.sohu.ad.algo.math;

/* Wolfe Line Search Method */
public class LineSearch {
	private final double stepLength = 1;  //initial step size
	private double c1 = 0.1;
	private double c2 = 0.9;
	private final int MAX_ITER_NUM = 1000;
	
	LineSearch() {
	}
	
	LineSearch(double _c1, double _c2) {
		c1 = _c1;
		c2 = _c2;
	}
	
	public double getC1() {
		return c1;
	}
	
	public double getC2() {
		return c2;
	}
	
	public void setC1(double _c1) {
		c1 = _c1;
	}
	
	public void setC2(double _c2) {
		c2 = _c2;
	}
	
	
	
	public double search(ObjectFunction<SparseVector> f, GradientFunction<SparseVector> df,
					   final SparseVector x0, 
					   final SparseVector dfx0,
					   double fx,
					   final SparseVector d,
					   SparseVector xt,
					   SparseVector dfxt) {
		//normalize direction
		//SparseVector d = direction.scale(1.0 / direction.norm_2()); 
		double leftBound = 0.0;
		double rightBound = Double.MAX_VALUE;
		//SparseVector xt = new SparseVector();
		//SparseVector dxt = new SparseVector();
		double fxt;	
		double alpha = stepLength;
		double ddt, dd0 = BLAS.dot(d, dfx0);
				
		int iterNum = 0;
		while(iterNum < MAX_ITER_NUM) {
			++iterNum;
			xt.assignTmp(BLAS.add(x0, d.scale(alpha)));
			fxt = f.eval(xt);
			dfxt.assignTmp(df.eval(xt));
			ddt = BLAS.dot(d, dfxt);
			
			//check Armijo condition
			if(fxt > fx + c1 * alpha * dd0) {
				rightBound = alpha;
				alpha = (leftBound + rightBound) / 2;
			}
			
			//check Wolfe condition
			else if(ddt < c2 * dd0) {
				leftBound = alpha;
				alpha = (leftBound + rightBound) / 2;
			}
			
			else {
				return fxt;
			}
		}
		return -1;
	}
	
	public double search(ObjectFunction<Vector> f, GradientFunction<Vector> df,
			   final Vector x0, 
			   final Vector dfx0,
			   final double fx0,
			   final Vector d,
			   Vector xt,
			   Vector dfxt) {
		double leftBound = 0.0;
		double rightBound = Double.MAX_VALUE;
		//Vector xt = new Vector();
		//Vector dfxt = new Vector();
		double fxt;	
		double alpha = stepLength;
		double ddt, dd0 = d.dot(dfx0);
				
		int iterNum = 0;
		while(iterNum < MAX_ITER_NUM) {
			xt.assignTmp(x0.add(d.scale(alpha)));
			fxt = f.eval(xt);
			dfxt.assignTmp(df.eval(xt));
			ddt = d.dot(dfxt);
			
			//check Armijo condition
			if(fxt > fx0 + c1 * alpha * dd0) {
				rightBound = alpha;
				alpha = (leftBound + rightBound) / 2;
			}
			//check Wolfe condition
			else if(ddt < c2 * dd0) {
				leftBound = alpha;
				alpha = (leftBound + rightBound) / 2;
			}
			else {
				return fxt;
			}
		}
		return -1;
	}
	
}

package com.sohu.ad.algo.math;

public class BFGS {
	 private static final double TOL = 1e-14;
	 private static final int MAX_ITER_NUM = 200;
	 
	 public int minimize(ObjectFunction<SparseVector> f, GradientFunction<SparseVector> df,
			 SparseVector x0, SymmetricMatrix H0) {
		 double fx0 = f.eval(x0);
		 SparseVector dfx0 = df.eval(x0);
		 SparseVector xt = new SparseVector();
		 SparseVector dfxt = new SparseVector();
		 double fxt;
		 
		 SparseVector s = new SparseVector();
		 SparseVector y = new SparseVector();
		 SparseVector tmp = new SparseVector();
		 
		 SparseVector d = new SparseVector();
		 LineSearch lineSearch = new LineSearch();
		 
		 int iter_num = 0;
		 while(iter_num < MAX_ITER_NUM && dfx0.norm_2() > TOL) {
			 iter_num++;
			 //compute search direction
			 //d = - H0 * dfx0.
			 d = H0.multiply(y).scale(-1.0);
			 		 
			 /*
			 xt.assign(x0);
			 fxt = fx0;
			 dfxt.assign(dfx0);
			 */
			 
			 
			 fxt = lineSearch.search(f, df, x0, dfx0, fx0, d, xt, dfxt);
			 s = xt.minus(x0);
			 y = dfxt.minus(dfx0);
			 tmp = H0.multiply(y);
			 
			//update Hessian matrix
			 double rho = 1.0 / y.dot(s);
			 double tmp2 = tmp.dot(y) * rho * rho + rho;
			 for(int i = 0; i < H0.size1(); ++i) {
				 for(int j = 0; j <= i; ++j) {
					 double t = -rho * (s.getValue(i) * tmp.getValue(j) +
							 s.getValue(j) * tmp.getValue(i)) 
							 + tmp2 * s.getValue(i) * s.getValue(j);
					 H0.setValue(i, j, H0.getValue(i, j) + t);
				 }
			 }
			 
			 x0.swap(xt);
			 dfx0.swap(dfxt);
			 fx0 = fxt;
			 
		 }
		 
		 if(iter_num == MAX_ITER_NUM) 
			 return -1;
		 else 
			 return 0;
	 }
	 
	 public int minimize(ObjectFunction<Vector> f, GradientFunction<Vector> df,
			 Vector x0, SymmetricMatrix H0) {
		 double fx0 = f.eval(x0);
		 Vector dfx0 = df.eval(x0);
		 Vector xt = new Vector(x0.getData().length);
		 Vector dfxt = new Vector(x0.getData().length);
		 double fxt;
		 
		 Vector s = new Vector();
		 Vector y = new Vector();
		 Vector tmp = new Vector();
		 
		 Vector d = new Vector();
		 LineSearch lineSearch = new LineSearch();
		 
		 int iter_num = 0;
		 while(iter_num < MAX_ITER_NUM && dfx0.norm_2() > TOL) {
			 iter_num++;
			 //compute search direction
			 //d = - H0 * dfx0.
			 d = H0.multiply(dfx0).scale(-1);
			 
			 /*
			 xt.assign(x0);
			 fxt = fx0;
			 dfxt.assign(dfx0);
			 */
			 
			 
			 fxt = lineSearch.search(f, df, x0, dfx0, fx0, d, xt, dfxt);
			 s = xt.minus(x0);
			 y = dfxt.minus(dfx0);
			 tmp = H0.multiply(y);
			 
			//update Hessian matrix
			 double rho = 1.0 / y.dot(s);
			 double tmp2 = tmp.dot(y) * rho * rho + rho;
			 for(int i = 0; i < H0.size1(); ++i) {
				 for(int j = 0; j <= i; ++j) {
					 double t = -rho * (s.getValue(i) * tmp.getValue(j) +
							 s.getValue(j) * tmp.getValue(i)) 
							 + tmp2 * s.getValue(i) * s.getValue(j);
					 H0.setValue(i, j, H0.getValue(i, j) + t);
				 }
			 }
			 
			 x0.swap(xt);
			 dfx0.swap(dfxt);
			 fx0 = fxt;
			 
		 }
		 
		 if(iter_num == MAX_ITER_NUM) 
			 return -1;
		 else 
			 return 0;
	 }
}
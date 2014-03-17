package com.sohu.ad.algo.math;

/*
 * Limited-Memory BFGS Method
 */
import java.util.*;

public class LBFGS {
	private int M = 10;
	private LinkedList<SparseVector> s = new LinkedList<SparseVector>();
	private LinkedList<SparseVector> y = new LinkedList<SparseVector>();
	private LinkedList<Double> rho = new LinkedList<Double>();
	private final int MAX_ITER_NUM = 1000;
	private final double TOL = 1e-10;
	
	public int minimize(ObjectFunction<SparseVector> f,
						 GradientFunction<SparseVector> df,
						 SparseVector x0) {
		
		 double fx0 = f.eval(x0);
		 SparseVector dfx0 = df.eval(x0);
		 SparseVector xt = new SparseVector();
		 SparseVector dfxt = new SparseVector();
		 double fxt;
		 
		 //SparseVector s = new SparseVector();
		 //SparseVector y = new SparseVector();
		 SparseVector q = new SparseVector();
		 
		 SparseVector d = new SparseVector();
		 LineSearch lineSearch = new LineSearch();
		 
		 int iter_num = 0;
		 int m = 0;
		 while(iter_num < MAX_ITER_NUM && dfx0.norm_2() > TOL) {
			 iter_num++;
			 //compute search direction
			 //d = - H0 * dfx0.
			 q.assign(dfx0);
			 d.assignTmp(LBFGSLoop(q).scale(-1.0));
			 
			 
			 fxt = lineSearch.search(f, df, x0, dfx0, fx0, d, xt, dfxt);
			 if(m >= M) {
				 s.pop();
				 y.pop();
				 rho.pop();
			 }
			 ++m;
			 s.add(xt.minus(x0));
			 y.add(dfxt.minus(dfx0));
			 rho.add(1.0 / y.getLast().dot(s.getLast()));
			 
			 x0.swap(xt);
			 dfx0.swap(dfxt);
			 fx0 = fxt;
		 }
		 
		 if(iter_num == MAX_ITER_NUM) 
			 return -1;
		 else 
			 return 0;
	 }
	
	private SparseVector LBFGSLoop(SparseVector q) {
		if(s.isEmpty()) {
			return q;
		}
		else {
			Iterator<SparseVector> iter1 = s.descendingIterator();
			Iterator<SparseVector> iter2 = y.descendingIterator();
			Iterator<Double> iter3 = rho.descendingIterator();
			ListIterator<SparseVector> it1 = s.listIterator(0);
			ListIterator<SparseVector> it2 = y.listIterator(0);
			ListIterator<Double> it3 = rho.listIterator(0);
			
			LinkedList<Double> alpha = new LinkedList<Double>();
			
			while(iter1.hasNext()) {
				double tmp = iter3.next() *  q.dot(iter1.next());
				q.plusAssign(-tmp, iter2.next());
				alpha.addFirst(tmp);
			}
			
			
			double tmp = s.getLast().dot(y.getLast()) / y.getLast().dot(y.getLast());
			SparseVector r = q.scale(tmp);
			
			while(it1.hasNext()) {
				double beta = it3.next() * r.dot(it2.next());
				r.plusAssign(alpha.pollFirst() - beta, it1.next());	
			}
			return r;
		}
	}
}

package com.sohu.ad.algo.math;

//import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LBFGSTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMinimize() {
		LBFGS lbfgsOptimizer = new LBFGS();
		F f = new F();
		Df df = new Df();
		SparseVector x0 = new SparseVector();
		x0.setValue(0, 0);
		x0.setValue(1, 0);
		//Vector dfx0 = df.eval(x0);
		//double fx0 = 0;
		System.out.println(x0);
		//SymmetricMatrix H0 = new SymmetricMatrix(x);
		//for(int i = 0; i < H0.size1(); ++i) {
			//	H0.setValue(i, i, 2);

		//}
		lbfgsOptimizer.minimize(f, df, x0);
		System.out.println(x0);
	}
	
	public class F implements ObjectFunction<SparseVector> {
		public double eval(SparseVector x) {
			//return x.getValue(0) * x.getValue(0) +  x.getValue(1) * x.getValue(1)// + x.getValue(0) * x.getValue(1)
				//	- 10 * x.getValue(0) - 6 * x.getValue(1);
			return x.getValue(0) * x.getValue(0) +  x.getValue(1) * x.getValue(1) + x.getValue(0) * x.getValue(1)
					- 10 * x.getValue(0) - 6 * x.getValue(1);
		}
	}
	
	public class Df implements GradientFunction<SparseVector> {
		public SparseVector eval(SparseVector x) {
			SparseVector dfx = new SparseVector();
		//	dfx.setValue(0, 2 * x.getValue(0) - 10);
		//	dfx.setValue(1, 2 * x.getValue(1) - 6 );
			dfx.setValue(0, 2 * x.getValue(0) - 10 + x.getValue(1));
			dfx.setValue(1, 2 * x.getValue(1) - 6 + x.getValue(0));
			return dfx;
		}
	}


}

package com.sohu.ad.algo.math;

//import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BFGSTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMinimize() {
		
		BFGS bfgsOptimizer = new BFGS();
		F f = new F();
		Df df = new Df();
		Vector x0 = new Vector(2);
		x0.setValue(0, 0);
		x0.setValue(1, 0);
		//Vector dfx0 = df.eval(x0);
		//double fx0 = 0;
		System.out.println(x0);
		SymmetricMatrix H0 = new SymmetricMatrix(x0.getData().length);
		for(int i = 0; i < H0.size1(); ++i) {
				H0.setValue(i, i, 2);

		}
		bfgsOptimizer.minimize(f, df, x0, H0);
		System.out.println(x0);
	}
	
	public class F implements ObjectFunction<Vector> {
		public double eval(Vector x) {
			//return x.getValue(0) * x.getValue(0) +  x.getValue(1) * x.getValue(1)// + x.getValue(0) * x.getValue(1)
				//	- 10 * x.getValue(0) - 6 * x.getValue(1);
			return x.getValue(0) * x.getValue(0) +  x.getValue(1) * x.getValue(1) + x.getValue(0) * x.getValue(1)
					- 10 * x.getValue(0) - 6 * x.getValue(1);
		}
	}
	
	public class Df implements GradientFunction<Vector> {
		public Vector gradient(Vector x) {
			Vector dfx = new Vector(x.getData().length);
		//	dfx.setValue(0, 2 * x.getValue(0) - 10);
		//	dfx.setValue(1, 2 * x.getValue(1) - 6 );
			dfx.setValue(0, 2 * x.getValue(0) - 10 + x.getValue(1));
			dfx.setValue(1, 2 * x.getValue(1) - 6 + x.getValue(0));
			return dfx;
		}
	}


}

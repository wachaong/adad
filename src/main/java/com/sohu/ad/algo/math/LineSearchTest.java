package com.sohu.ad.algo.math;

//import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LineSearchTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		double[] a = {1,2,3};
		System.out.println(a);
		LineSearch ls = new LineSearch(0.01, 0.9);

		F f = new F();
		Df df = new Df();
		Vector x0 = new Vector(2);
		Vector xt = new Vector(2);
		Vector dfxt = new Vector(2);
		x0.setValue(0, 0.0);
		x0.setValue(1, 0.0);
		Vector dfx0 = df.gradient(x0);
		double fx0 = f.eval(x0);
		System.out.println(x0);
		int i = 0;
		while(dfx0.norm_2() > 1e-10 && i < 100) {
			++i;
			fx0 = ls.search(f, df, x0, dfx0, fx0, dfx0.scale(-1), xt, dfxt);
			x0.swap(xt);
			dfx0.swap(dfxt);
			System.out.println(i + ":" + fx0 + ":" +  "...." + x0);
		}
		
		
		
	}
	
	public class F implements ObjectFunction<Vector> {
		public double eval(Vector x) {
			return x.getValue(0) * x.getValue(0) +  x.getValue(1) * x.getValue(1)// + x.getValue(0) * x.getValue(1)
					- 10 * x.getValue(0) - 6 * x.getValue(1);
			//return x.getValue(0) * x.getValue(0) +  x.getValue(1) * x.getValue(1) + x.getValue(0) * x.getValue(1)
				//	- 10 * x.getValue(0) - 6 * x.getValue(1);
		}
	}
	
	public class Df implements GradientFunction<Vector> {
		public Vector gradient(Vector x) {
			Vector dfx = new Vector(x.getData().length);
			dfx.setValue(0, 2 * x.getValue(0) - 10);
			dfx.setValue(1, 2 * x.getValue(1) - 6 );
		//	dfx.setValue(0, 2 * x.getValue(0) - 10 + x.getValue(1));
		//	dfx.setValue(1, 2 * x.getValue(1) - 6 + x.getValue(0));
			return dfx;
		}
	}

}

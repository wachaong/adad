package com.sohu.ad.algo.math;

//import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BLASTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAdd() {
		System.out.println("Testing add");
		SparseVector v1 = new SparseVector();
		SparseVector v2 = new SparseVector();
		v1.setValue(1, 1.0);
		v1.setValue(2, 2.0);
		v2.setValue(1, 4.0);
		v2.setValue(100, 3.14);
		System.out.println("v1 = " + v1);
		System.out.println("v2 = " + v2);
		System.out.println("v1 + v2 = " + (BLAS.add(v1, v2)));
		System.out.println("-----------------------------");	
		
	}
	
	@Test
	public void testMinus() {
		System.out.println("Testing minus");
		SparseVector v1 = new SparseVector();
		SparseVector v2 = new SparseVector();
		v1.setValue(1, 1.0);
		v1.setValue(2, 2.0);
		v2.setValue(1, 4.0);
		v2.setValue(100, 3.14);
		System.out.println("v1 = " + v1);
		System.out.println("v2 = " + v2);
		System.out.println("v1 - v2 = " + (BLAS.minus(v1, v2)));
		System.out.println("-----------------------------");	
		
	}
	
	@Test
	public void testDot() {
		System.out.println("Testing dot");
		SparseVector v1 = new SparseVector();
		SparseVector v2 = new SparseVector();
		v1.setValue(1, 1.0);
		v1.setValue(2, 2.0);
		v2.setValue(1, 4.0);
		v2.setValue(100, 3.14);
		System.out.println("v1 = " + v1);
		System.out.println("v2 = " + v2);
		System.out.println("v1 * v2 = " + (BLAS.dot(v1, v2)));
		System.out.println("-----------------------------");		
	}
	
	@Test
	public void testScale() {
		System.out.println("Testing scale");
		SparseVector v1 = new SparseVector();
		SparseVector v2 = new SparseVector();
		v1.setValue(1, 1.0);
		v1.setValue(2, 2.0);
		v2.setValue(1, 4.0);
		v2.setValue(100, 3.14);
		System.out.println("v1 = " + v1);
		System.out.println("v2 = " + v2);
		BLAS.scale(v1, 2.0);
		BLAS.scale(v2, 0.5);
		System.out.println("v1 * 2 = " +  v1);
		System.out.println("v2 * 0.5 = " +  v2);
		System.out.println("-----------------------------");		
	}

}

package com.sohu.ad.algo.math;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SparseVectorTest {
	private SparseVector v1 = new SparseVector();
	private SparseVector v2 = new SparseVector();
	private SparseVector ans = null;
	private double ans2 = 0;

	@Before
	public void setUp() throws Exception {
		v1.setValue(1, 3.2);
		v1.setValue(2, 3.0);
		v2.setValue(1, 3.2);
		v2.setValue(3, 3.0);
		v2.setValue(100, 100.0);
		System.out.println("---------------------------------------------------");
		System.out.println("before operation:");
		System.out.println("v1 = " + v1);
		System.out.println("v2 = " + v2);
		System.out.println("ans = " + ans);
		System.out.println("ans2 = " + ans2);
	}

	@After
	public void tearDown() throws Exception {
		System.out.println("v1 = " + v1);
		System.out.println("v2 = " + v2);
		System.out.println("ans = " + ans);
		System.out.println("ans2 = " + ans2);
		System.out.println();
		System.out.println();
		System.out.println();
	}

	
	@Test
	public void testScale() {
		System.out.println("After operation : ans = v1.scale(3.0)");
		ans = v1.scale(3.0);
	}
	
	@Test
	public void testAdd() {
		System.out.println("After operation : ans = v1.add(v2)");
		ans = v1.add(v2);
	}
	
	@Test
	public void testMinus() {
		System.out.println("After operation : ans = v1.minus(v2)");
		ans = v1.minus(v2);
	}
	
	@Test
	public void testDot() {
		System.out.println("After operation : ans2 = v1.dot(v2)");
		ans2 = v1.dot(v2);
	}
	
	@Test
	public void testAssign() {
		System.out.println("After operation : v1.assign(v2)");
		v1.assign(v2);
	}
	
	@Test
	public void testAssignTmp() {
		System.out.println("After operation : v1.assignTmp(v2.add(v1))");
		v1.assignTmp(v2.add(v1));
	}
	
	@Test
	public void testPlusAssign() {
		System.out.println("After operation : v1.plusAssign(2, v2)");
		v1.plusAssign(2, v2);
	}
	
	@Test
	public void testSwap() {
		System.out.println("After operation : v1.swap(v2)");
		v1.swap(v2);
	}
}

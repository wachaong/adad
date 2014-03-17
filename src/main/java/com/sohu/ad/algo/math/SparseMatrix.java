package com.sohu.ad.algo.math;

import java.util.*;

public class SparseMatrix {
	private HashMap<Integer, SparseVector> data = new HashMap<Integer, SparseVector>();
	
	public SparseMatrix() {
	}
	
	public SparseMatrix(SparseMatrix m) {
		for(int i : data.keySet()) {
			data.put(i, new SparseVector(m.data.get(i)));
		}
	}
	
	public double getValue(int i, int j) {
		if(data.containsKey(i)) {
			return data.get(i).getValue(j);
		}
		else
			return 0.0;
	}
	
	public void setValue(int i, int j, double value) {
		if(!data.containsKey(i)) {
			data.put(i, new SparseVector());
		}
		data.get(i).setValue(j, value);
	}
	
	public SparseVector multiply(SparseVector v) {
		SparseVector ans = new SparseVector();
		for(int i : data.keySet()) {
			double tmp = data.get(i).dot(v);
			if(!Util.equalsZero(tmp)) {
				ans.setValue(i, tmp);
			}
		}
		return ans;
	}

}

package com.sohu.ad.algo.math;

public class SymmetricMatrix {
	private double[] _data = new double[0];
	private int _dimension = 0;
	
	public SymmetricMatrix() {
	}
	
	
	public SymmetricMatrix(int dim) {
		this._dimension = dim;
		this._data = new double[_dimension * (_dimension + 1) / 2];
	}
	
	public double[] getData() {
		return _data;
	}
	
	public double size1() {
		return _dimension;
	}
	
	public double size2() {
		return _dimension;
	}
	
	public int id(int i, int j) {
		return i * (i + 1) / 2 + j;
	}
	
	public double getValue(int i, int j) {
		if(i >= j) 
			return _data[id(i, j)];
		else
			return _data[id(j, i)];
	}
	
	public void setValue(int i, int j, double value) {
		if(i >= j)
			_data[id(i, j)] = value;
		else
			_data[id(j, i)] = value;
	}
	
	public Vector multiply(Vector v) {
		Vector ans = new Vector(_dimension);
		for(int i = 0; i < _dimension; ++i) {
			double tmp = 0;
			for(int j = 0; j < _dimension; ++j) {
				tmp += this.getValue(i, j) * v.getValue(j);
			}
			ans.setValue(i, tmp);
		}
		return ans;
	}
	
	public SparseVector multiply(SparseVector v) {
		SparseVector ans = new SparseVector();
		for(int i = 0; i < _dimension; ++i) {
			double tmp = 0;
			for(int j : v.getData().keySet()) {
				tmp += this.getValue(i, j) * v.getValue(j);
			}
			ans.setValue(i, tmp);
		}
		return ans;
	}

}

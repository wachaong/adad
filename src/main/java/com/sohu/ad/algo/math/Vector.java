package com.sohu.ad.algo.math;

public class Vector {
	private double[] _data = new double[0];
	
	public Vector() {
	}
	
	/*
	 * toDo : assert dim must be positive
	 */
	public Vector(int dim) {
		_data = new double[dim];
	}
	
	public Vector(Vector v) {
		_data = (double[]) v._data.clone();
	}
	
	public double[] getData() {
		return _data;
	}
	
	public double getValue(int i) {
		return _data[i];
	}
	
	public void setValue(int i, double value) {
		_data[i] = value;
	}
	
	/*
	 * operations
	 * add
	 */
	public Vector add(Vector v) {
		Vector ans = new Vector(_data.length);
		for(int i = 0; i < _data.length; ++i) {
			ans.getData()[i] = _data[i] + v.getValue(i);
		}
		return ans;
	}
	
	public Vector minus(Vector v) {
		Vector ans = new Vector(_data.length);
		for(int i = 0; i < _data.length; ++i) {
			ans.getData()[i] = _data[i] - v.getValue(i);
		}
		return ans;		
	}
	
	/*
	public Vector add(SparseVector v) {
		Vector ans = (Vector) this.clone();
		for(int i : v.keySet()) {
			
		}
	}
	*/
	
	/* 
	 * ±êÁ¿³Ë·¨
	 */
	public Vector scale(double a) {
		Vector ans = new Vector(_data.length);
		for(int i = 0; i < _data.length; ++i) {
			ans.getData()[i] = _data[i] * a;
		}
		return ans;
	}
	
	public double dot(Vector v) {
		double ans = 0.0;
		for(int i = 0; i < _data.length; ++i) {
			ans += _data[i] * v.getValue(i);
		}
		return ans;
	}
	
	public double norm_1() {
		double ans = 0.0;
		for(int i = 0; i < _data.length; ++i) {
			ans += Math.abs(_data[i]);
		}
		return ans;
	}
	
	public double norm_2() {
		double ans = 0.0;
		for(int i = 0; i < _data.length; ++i) {
			ans += _data[i] * _data[i];
		}
		return ans;
	}
	
	public void assign(Vector v) {
		System.arraycopy(v._data, 0, _data, 0, _data.length);
	}
	
	public void assignTmp(Vector v) {
		_data = v._data;
	}
	
	public void swap(Vector v) {
		double[] tmp = _data;
		_data = v._data;
		v._data = tmp;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(double e : _data) {
			sb.append(e + ",");
		}
		return sb.toString();
	}
}

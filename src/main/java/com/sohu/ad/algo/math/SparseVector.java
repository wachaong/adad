package com.sohu.ad.algo.math;

import java.util.*;

public class SparseVector {
	
	private HashMap<Integer, Double> _data = new HashMap<Integer, Double>();
	
	public SparseVector() {
	}
	
	public SparseVector(SparseVector v) {
		//_data = new HashMap<Integer, Double>();
		for(int i : v.getData().keySet()) {
			this.setValue(i, v.getValue(i));
		}
	}
	
	public HashMap<Integer, Double> getData() {
		return _data;
	}

	public double getValue(int i) {
		if(this.getData().containsKey(i)) {
			return this.getData().get(i);
		}
		else
			return 0.0;
	}
	
	public void setValue(int i, double value) {
		this.getData().put(i, value);
	}
	
	
	public SparseVector scale(double a) {
		SparseVector v = new SparseVector();
		for(int i : this.getData().keySet()) {
			v.getData().put(i, this.getValue(i) * a);
		}
		return v;
	}
	
	public SparseVector add(SparseVector v) {
		SparseVector ans = new SparseVector(this);
		for(int i : v._data.keySet()) {
			ans.setValue(i, ans.getValue(i) + v.getValue(i));
		}
		return ans;
	}
	
	public SparseVector minus(SparseVector v) {
		SparseVector ans = new SparseVector(this);
		for(int i : v._data.keySet()) {
			ans.setValue(i, ans.getValue(i) - v.getValue(i));
		}
		return ans;
	}
	
	public double dot(SparseVector v) {
		double ans = 0.0;
		if(_data.size() < v._data.size()) {
			for(int i : _data.keySet()) {
				ans += this.getValue(i) * v.getValue(i);
			}
		}
		else {
			for(int i : v._data.keySet()) {
				ans += this.getValue(i) * v.getValue(i);
			}
		}
		return ans;
	}
	
	public double norm_1() {
		double ans = 0.0;
		for(int i : this.getData().keySet()) {
			ans += Math.abs(this.getData().get(i));
		}
		return ans;
	}
	
	public double norm_2() {
		double ans = 0.0;
		double tmp;
		for(int i: this.getData().keySet()) {
			tmp = this.getData().get(i);
			ans += tmp * tmp;
		}
		return Math.sqrt(ans);
	}
	
	public int norm_inf_index() {
		int ans = -1;
		double max_abs_value = -1.0;
		double tmp;
		for(int i : this.getData().keySet()) {
			tmp = this.getData().get(i);
			if(Math.abs(tmp) > max_abs_value) {
				max_abs_value = tmp;
				ans = i;
			}
		}
		return ans;
	}
	
	public void assign(SparseVector v) {
		_data.clear();
		for(int i : v._data.keySet()) {
			_data.put(i, v.getValue(i));
		}
	}
	
	public void plusAssign(double a, SparseVector v) {
		for(int i : v._data.keySet()) {
			this.setValue(i, this.getValue(i) + a * v.getValue(i));
		}
	}
	
	public void assignTmp(SparseVector v) {
		_data = v._data;
		v._data = null;
	}
	
	public void swap(SparseVector v) {
		HashMap<Integer, Double> tmp = null;//new HashMap<Integer, Double>();
		tmp = _data;
		_data = v._data;
		v._data = tmp;
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		for(int i : _data.keySet()) {
			str.append(i + ":" + _data.get(i) + " ");
		}
		return str.toString();
	}
}

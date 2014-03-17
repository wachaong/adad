package com.sohu.ad.algo.math;

import java.util.*;

public class Dataset {
	private ArrayList<Sample> _data = null;
	
	public Dataset() {
		_data = new ArrayList<Sample>();
	}
	
	public Dataset(int size) {
		_data = new ArrayList<Sample>(size);
	}
	
	public ArrayList<Sample> getData() {
		return _data;
	}
	
	
	public void addSample(Sample sample) {
		_data.add(sample);
	}
	
	
}

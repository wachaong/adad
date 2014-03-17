package com.sohu.ad.algo.models;

import com.sohu.ad.algo.math.*;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface Model {
	//public void initModel(String path);
	public void loadModel(String path) throws FileNotFoundException, IOException;
	public void saveModel(String path) throws FileNotFoundException;
	public void train(Dataset dataset);
	public double loss(Sample sample);
	public double predict(SparseVector features);
}

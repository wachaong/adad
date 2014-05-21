package com.sohu.ad.algo.models;

import com.sohu.ad.algo.input.InstancesWritable;
import com.sohu.ad.algo.math.*;
import com.sohu.ad.algo.optimizer.LossObjective;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface Model {
	//public void initModel(String path);
	public void loadModel(String path) throws FileNotFoundException, IOException;
	public void saveModel(String path) throws FileNotFoundException;
	//return loss
	public double trainBatch(InstancesWritable dataset, LossObjective obj);
	//public void trainSGD(Dataset dataset);
	public double predict(SparseVector features);
}

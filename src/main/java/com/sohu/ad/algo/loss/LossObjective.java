package com.sohu.ad.algo.loss;

import com.sohu.ad.algo.admm.tools.MyPair;
import com.sohu.ad.algo.input.InstancesWritable;
import com.sohu.ad.algo.input.SingleInstanceWritable;

public interface LossObjective {
    
    void setCoefs(Object coefs);
    
    MyPair<Double, ?> calLossAndGradient(SingleInstanceWritable example);

    MyPair<Double, ?> calLossAndGradient(InstancesWritable dataSet);
}

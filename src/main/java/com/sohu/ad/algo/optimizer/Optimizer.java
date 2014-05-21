package com.sohu.ad.algo.optimizer;

import com.sohu.ad.algo.input.InstancesWritable;
import com.sohu.ad.algo.loss.LossObjective;

public interface Optimizer {
    /**
     * Called when all the parameters of the {@link Optimizer} are imported from the command line mode.
     * @param paraJsonObject
     * @throws JSONException
     */
    void setParameters(JSONObject paraJsonObject);

    void train(InstancesWritable dataSet, LossObjective obj);
    
    /**
     * Return all the parameters learned by the {@link Optimizer}.
     * @return
     */
    Object getCoefs();
    
    void setInitCoefs(Object initCoefs);
}

/* Copyright (C) 2005 Vladimir Roubtsov. All rights reserved.
 */
package com.sohu.ad.algo.admm.optimizer;

/**
 * @author Vlad Roubtsov
 */
public interface IFunctionGradient {
    void evaluate(double[] x, double[] out);

}
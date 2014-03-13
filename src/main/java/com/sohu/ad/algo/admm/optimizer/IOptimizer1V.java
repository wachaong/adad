/* Copyright (C) 2005 Vladimir Roubtsov. All rights reserved.
 */
package com.sohu.ad.algo.admm.optimizer;

/**
 * @author Vlad Roubtsov, 2006
 */
public interface IOptimizer1V {
    // public: ................................................................

    double optimize(IFunction1V f, double a, double b, double[] x);

}
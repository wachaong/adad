/* Copyright (C) 2005 Vladimir Roubtsov. All rights reserved.
 */
package com.sohu.ad.algo.admm.optimizer;

/**
 * @author Vlad Roubtsov
 */
public interface IFunction {
    double evaluate(double[] x);

}
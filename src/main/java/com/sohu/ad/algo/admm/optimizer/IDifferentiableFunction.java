/* Copyright (C) 2005 Vladimir Roubtsov. All rights reserved.
 */
package com.sohu.ad.algo.admm.optimizer;

/**
 * @author Vlad Roubtsov, (C) 2005
 */
public interface IDifferentiableFunction extends IFunction {
    IFunctionGradient gradient();

}
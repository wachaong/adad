package com.sohu.ad.algo.math;


//V是向量的类型
public interface GradientFunction<V> {
	public V eval(V x0);
}

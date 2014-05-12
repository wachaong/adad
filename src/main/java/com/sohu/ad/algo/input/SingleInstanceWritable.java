package com.sohu.ad.algo.input;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Vector;

import org.apache.hadoop.io.WritableComparable;

import com.sohu.ad.algo.admm.tools.MyPair;

public class SingleInstanceWritable implements WritableComparable{
	private Double label=null;
	private Vector<Integer> id_fea_vec=null;//id features
	private Vector<MyPair<Integer,Double>> float_fea_vec=null;//float features

	public SingleInstanceWritable(){
		label=new Double(0.0);
		id_fea_vec=new Vector<Integer>();
		float_fea_vec=new Vector<MyPair<Integer,Double>>();
	}
	public void addIdFea(int fea_id){
		id_fea_vec.add(fea_id);
	}
	public void addFloatFea(int fea_id,double feavalue){
		MyPair<Integer,Double> mp= new MyPair<Integer,Double>(fea_id,feavalue);
		float_fea_vec.add(mp);
	}
	public Double getLabel() {
		return label;
	}

	public void setLabel(Double label) {
		this.label = label;
	}

	public Vector<Integer> getId_fea_vec() {
		return id_fea_vec;
	}

	@Override
	public String toString() {
		String str=this.label+"\t";
		for(Integer i:this.getId_fea_vec())
			str+=i+"\t";
		for(MyPair<Integer,Double> mp:this.getFloat_fea_vec())
			str+=(mp.getFirst()+":"+mp.getSecond()+"\t");
		
		return str;
	}
	public void setId_fea_vec(Vector<Integer> id_fea_vec) {
		this.id_fea_vec = id_fea_vec;
	}

	public Vector<MyPair<Integer, Double>> getFloat_fea_vec() {
		return float_fea_vec;
	}

	public void setFloat_fea_vec(Vector<MyPair<Integer, Double>> float_fea_vec) {
		this.float_fea_vec = float_fea_vec;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		//write double label
		out.writeDouble(label);
		//write id_fea
		out.writeInt(id_fea_vec.size());
		out.writeInt(id_fea_vec.size());
		for(Integer id: id_fea_vec){
			out.writeInt(id);
		}
		
		//write float fea
		out.writeInt(float_fea_vec.size());
		for(MyPair<Integer,Double>  mp: float_fea_vec){
			out.writeInt(mp.getFirst());
			out.writeDouble(mp.getSecond());
		}
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		// TODO Auto-generated method stub
		id_fea_vec=new Vector<Integer>();
		id_fea_vec.clear();
		float_fea_vec=new Vector<MyPair<Integer,Double>>();
		float_fea_vec.clear();
		
		this.label=in.readDouble();//label
		int id_size=in.readInt();
		for(int i=0; i< id_size; i++){//id features
			id_fea_vec.add(in.readInt());
		}
		
		int float_size=in.readInt();
		for(int i=0; i< float_size; i++){
			float_fea_vec.add(new MyPair<Integer,Double>(in.readInt(),in.readDouble()));
		}
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}
	 
}

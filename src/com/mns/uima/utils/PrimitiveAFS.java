package com.mns.uima.utils;

import java.util.HashMap;

/** 
 * PrimitiveAFS holds a simplified representation of UIMA AnnotationFS
 * <p>
 * PrimitiveAFS is not a UIMA class. It is defined and used as a simple class that is 
 * easier to handle than AnnotationFS with the restriction that it can only hold 
 * features that are UIMA primitive types.
 *       
 * @author      Martin Saunders martin.saunders@uk.ibm.com
 * @version     2.0.0 $Revision: 168 $           
 */
public class PrimitiveAFS {
	private String typeStr;
	private String coveredText;
	private int begin, end;
	private HashMap<String, String> features;
	public int getBegin() {
		return begin;
	}
	public void setBegin(int begin) {
		this.begin = begin;
		if (features == null) {
			features = new HashMap<String,String>();
			features.put("begin", Integer.toString(begin));
		} else
			features.put("begin", Integer.toString(begin));
	}
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
		if (features == null) {
			features = new HashMap<String,String>();
			features.put("end", Integer.toString(end));
		} else
			features.put("end", Integer.toString(end));
	}
	public String getTypeStr() {
		return typeStr;
	}
	public void setTypeStr(String typeStr) {
		this.typeStr = typeStr;
	}
	public String getCoveredText() {
		return coveredText;
	}
	public void setCoveredText(String coveredText) {
		this.coveredText = coveredText;
	}
	public HashMap<String, String> getFeatures() {
		return features;
	}
	public void setFeatures(HashMap<String, String> features) {
		this.features = features;
	}
	public String getFeatureValue(String feature){
		String ret=null;
		if (features != null) {
			if (features.containsKey(feature))
				ret = features.get(feature);
		}
		return ret;
	}
}

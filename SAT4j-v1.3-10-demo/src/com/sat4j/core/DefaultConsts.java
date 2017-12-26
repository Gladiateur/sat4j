/*
 * @(#)DefaultConsts.java	1.0 17/05/1
 *
 * Copyright 2017 Gladiateur, Inc. All rights reserved.
 */


package com.sat4j.core;

/**
 * 定义全局常量的接口
 * 这个接口中定义的常量用于关联java和mysql中的类型。比如把mysql中的char,varchar,text转换成java.lang.String类型
 * 
 * 
 * @author Gladiateur
 * @version v1.2 17/5/5
 * */
public interface DefaultConsts {
	
	public final static String JAVA_STRING = "String";
	public final static String JAVA_BYTEARRAY = "byte[]";
	public final static String JAVA_LONG = "long";
	public final static String JAVA_FLOAT = "float";
	public final static String JAVA_DOUBLE = "double";
	public final static String JAVA_INTEGER = "Integer";
	public final static String JAVA_BOOLEAN = "Boolean";
	public final static String JAVA_DATE = "java.sql.Date";
	public final static String JAVA_TIME = "java.sql.Time";
	public final static String JAVA_TIMESTAMP = "java.sql.Timestamp";
	public final static String JAVA_BIGINTEGER = "java.math.BigInteger";
	public final static String JAVA_BIGDECIMAL = "java.math.BigDecimal";


}

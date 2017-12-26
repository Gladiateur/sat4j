/*
 * @(#)NotStartMySQLServiceException.java	1.0 17/05/20
 *
 * Copyright 2017 Gladiateur, Inc. All rights reserved.
 * 
 * This exception class is used to describe MySQL's service not open.
 */

package com.sat4j.exception;

/**
 * 当你想运行需要操作数据库的方法但却没有开启mysql的服务时会出现此异常。
 * <p>
 * 顾名思义，该异常<code>NotStartMySQLServiceException</code>意思是mysql服务未启动异常。出现此异常并不是说你的操作数据库的方法出现了问题，而是你
 * 连mysql服务都没有打开。
 * </p>
 * <p>
 * 该异常继承运行时异常<tt>RuntimeException</tt>,因为我希望出现该异常时使整个程序停止，不需要再往后进行。因为连服务都没启动就运行后面的程序是没任何
 * 意义的。
 * </p>
 * 
 * @author Gladiateur
 * @version v1.0 17/5/30
 * @since SAT4j-v1.0-test.jar
 * */
public class NotStartMySQLServiceException extends RuntimeException{
	/** use serialVersionUID from JDK 1.0.2 for interoperability */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 该类<code>NotStartMySQLServiceException</code>没有详细信息的构造函数。 
	 * */
	public NotStartMySQLServiceException(){
		super();
	}
	
	/**
	 * 该类<code>NotStartMySQLServiceException</code>包含异常详细信息的构造函数。 
	 * 
	 * @param msg 异常详细信息
	 * */
	public NotStartMySQLServiceException(String msg){
		super(msg);
	}
}

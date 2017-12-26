/*
 * @(#)NullConnectionException.java	1.0 17/05/25
 *
 * Copyright 2017 Gladiateur, Inc. All rights reserved.
 * 
 * This exception is used when the database is operating without 
 * 
 * access to the database connection.
 */

package com.sat4j.exception;

/**
 * 当你想运行需要操作数据库的方法但 却传入了空的连接对象时会出现此异常。
 * <p>
 * </p>
 * <p>
 * 该异常继承运行时异常<tt>RuntimeException</tt>,因为我希望出现该异常时使整个程序停止，不需要再往后进行。
 * 因为操作空连接对象是没任何意义的。
 * </p>
 * 
 * @author Gladiateur
 * @version v1.0 17/5/30
 * @since SAT4j-v1.0-test.jar
 * */
public class NullConnectionException extends RuntimeException{
	/** use serialVersionUID from JDK 1.0.2 for interoperability */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 该类<code>NullConnectionException</code>没有详细信息的构造函数。 
	 * */
	public NullConnectionException(){
		super();
	}
	
	/**
	 * 该类<code>NullConnectionException</code>包含异常详细信息的构造函数。 
	 * 
	 * @param msg 异常详细信息
	 * */
	public NullConnectionException(String msg){
		super(msg);
	}
}

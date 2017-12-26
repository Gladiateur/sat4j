/*
 * @(#)AbsFunction.java	1.0 17/05/23
 *
 * Copyright 2017 Gladiateur, Inc. All rights reserved.
 * 
 * This abstract class contains some ideas that come into being when conceived.
 */

package com.sat4j.core;

import java.sql.Connection;

/**
 * 该类<code>AbsFunction</code>是抽象的。在构思如何实现自动验证身份时需要以下所包含的方法。
 * <p>
 * 该类中定义了一些基本的行为。比如验证身份，将字符串转换为数组。
 * </p>
 * <p>
 * 在实现抽象类时，必须实现该类中的每一个抽象方法，而每个已实现的方法必须和抽象类中指定的方法一样，
 * 接收相同数目和类型的参数，具有同样的返回值。
 * </p>
 * 
 * @author Gladiateur
 * @version v1.0 17/5/23
 * @since SAT4j-v1.0-test.jar
 * */
public abstract class AbsFunction {
	
	/**
	 * 它会获取配置文件javabean.properties中CheckTable的键值，根据用户指定的表和列实现身份验证。
	 * <p>
	 * 关于java中的抽象方法不能被static修饰，请参见:http://www.cnblogs.com/zksh2016/p/5813857.html
	 * </p>
	 * */
	public abstract boolean checkStatus(Object paramId,Object paramPwd,Connection conn);
	
	/**
	 * 该方法用于将一串用逗号隔开的字符串转化成数组。
	 * <p>
	 * 实现自动验证身份也并不是说用户什么都不需要做，用户仍然需要配置你希望SAT4j帮你验证哪张表，哪两个列
	 * 的数据。这个方法为实现自动验证身份服务。
	 * </p>
	 * */
	public abstract String[] transformToArray(String string);
}

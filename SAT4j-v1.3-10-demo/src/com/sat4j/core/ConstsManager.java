/*
 * @(#)ConstsManager.java	1.0 17/04/16
 *
 * Copyright 2017 Gladiateur, Inc. All rights reserved.
 */

package com.sat4j.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 这个类用于获取配置文件信息，并对配置文件初始化。
 * <p>
 * 静态代码块用于加载配置文件。 利用输入流加载默认连接数据库常量的配置文件。并获取里面常量的值。
 * 可以称这个类是常量管理器。它实现了接口<interface>DefaultConsts</interface>。这个接口中定义了一些需要使用到的常量。
 * </p>
 * 
 * @author Gladiateur
 * @version v2.0 17/6/2
 * @since SAT4j-v1.0.3-test.jar
 * */
public final class ConstsManager implements DefaultConsts {
	
	/**	 常量数组 */
	public final static String[] javaTypes={JAVA_STRING,JAVA_BYTEARRAY,JAVA_LONG,JAVA_FLOAT,JAVA_DOUBLE,
				JAVA_INTEGER,JAVA_BOOLEAN,JAVA_DATE,JAVA_TIME,JAVA_TIMESTAMP,JAVA_BIGINTEGER,JAVA_BIGDECIMAL};
	
	/**	私有配置文件default-config.properties封装的Map集合	*/
	private static Map<String,String> configMap=loadFrameConfig();
	
	/**	私有配置文件default-relevance.properties封装的Map集合	*/
	private final static Map<String,String> releMap=loadRelevanceConfig(); 
	
	/**	私有配置文件default-connection.properties封装的Map集合	 */
	private static Map<String,String> connMap=loadConnectionConfig();
	
	/**	私有化配置文件javabean.properties封装的Map集合	*/
	private static Map<String,String> beanMap=loadBeanConfig();
	
	/**	向外暴露releMap对象	*/
	public static Map<String,String> getRelemap() {
		return releMap;
	}
	
	/**	向外部暴露connMap对象	*/
	public static Map<String,String> getConnMap() {
		return connMap;
	}
	
	/**	向外部暴露configMap对象	*/
	public static Map<String,String> getConfigMap() {
		return configMap;
	}
	
	/**	向外暴露beanMap对象	*/
	public static Map<String,String> getBeanMap() {
		return beanMap;
	}
	
	/**	使用JDBC驱动连接数据库，所以驱动类为com.mysql.jdbc.Driver	*/
	public final static String DRIVER = "com.mysql.jdbc.Driver";

	/**	URL:连接数据库的地址	*/
	public static String URL;

	/**	dbName:数据库名称	*/
	public static String dbName;

	/**	USERNAME:连接数据库的用户名	*/
	public static String USERNAME;

	/**	PASSWORD:连接数据库的密码	*/
	public static String PASSWORD;

	/**	path 生成实体类的包名	 */
	public static String path;
	
	/**	checkTableString ：键CheckTable的键值	*/
	public static String checkTableString;

	/**
	 * 该方法的功能是加载连接数据库的配置信息 将配置信息封装在Map集合中
	 * */
	private final static Map<String,String> loadConnectionConfig() {
		System.out.println("加载配置文件default-connetion.properties");
		InputStream in = ConstsManager.class
				.getResourceAsStream("/default-connection.properties");// 获取文件输入流
		Properties prop = new Properties();
		try {
			prop.load(in);// 加载配置信息
			dbName = prop.getProperty("dbName");
			USERNAME = prop.getProperty("username");
			PASSWORD = prop.getProperty("password");

		} catch (IOException e) {
			throw new RuntimeException("读取配置文件异常:" + e.getMessage());
		} catch (NullPointerException e) {
			throw new RuntimeException(
					"com.sat4j.core.ConstsManager:没有找到配置文件default-connection.propreties");
		}
		URL = "jdbc:mysql://localhost:3306/" + dbName+ "?useUnicode=true&characterEncoding=UTF-8";
		Map<String,String> map=new HashMap<String,String>();
		map.put("DRIVER", DRIVER);
		map.put("dbName", dbName);
		map.put("URL", URL);
		map.put("USERNAME", USERNAME);
		map.put("PASSWORD", PASSWORD);
		return map;
	}

	/**
	 * 该方法从配置文件javabean.properties中获取配置信息并封装成Map集合
	 * <p>
	 * 配置文件javabean.poperties的作用：用户在这个文件中配置数据库，和实体类输出路径 这个文件中有一个键：path
	 * 键的说明：<br>
	 * path:实体类路径 指定实体类文件相对于用户的工程所在的路径<br>
	 * </p>
	 * <p>
	 * 注意实体类路径的写法：\\xxx\\xxx\\... 例如: \\src\\com\\sat4j\\bean
	 * 该方法的功能是获取这个配置文件中的键值对信息并封装到Map集合中，返回这个集合。
	 * </p>
	 * <p>
	 * 在1.6.7.92版本后，这个方法增加了一个键：CheckTable,这个键用于配置有关自动验证身份的数据。
	 * 比如你可以这样配置：CheckTable=user,id,pwd,这个方法就会获取到这个键的键值并封装成Map集合。
	 * </p>
	 * */
	private final static Map<String, String> loadBeanConfig() {
		System.out
				.println("加载配置文件javabean.properties");
		InputStream in = ConstsManager.class
				.getResourceAsStream("/javabean.properties");// 获取文件输入流
		Properties prop = new Properties();
		try {
			prop.load(in);// 加载配置信息
			path = "\\src\\" + prop.getProperty("path");
			checkTableString=prop.getProperty("CheckTable");
			// System.out.println("测试，攻关2：path="+path);
		} catch (IOException e) {
			throw new RuntimeException("读取配置文件异常:" + e.getMessage());
		} catch (NullPointerException e) {
			throw new RuntimeException(
					"com.sat4j.core.ConstsManager:没有找到配置文件javabean.propreties");
		}
		Map<String,String> beanMap=new HashMap<String,String>();
		beanMap.put("path", path);
		beanMap.put("checkTableString", checkTableString);
		return beanMap;
	}

	/**
	 * 加载配置文件default-relevance.properties中的配置信息并封装成Map集合。
	 * <p>
	 * 这个配置文件配置了有关java和mysql中的类型关联信息。在自动创建实体类时需要把mysql中的部分类型转换成java中的类型。
	 * </p>
	 * */
	public final static Map<String, String> loadRelevanceConfig() {
		System.out.println("加载配置文件default-relevance.properties");
		String sqlStrings, sqlbyte, sqllong, sqlfloat, sqldouble, sqlinteger, sqlboolean, sqldate, sqltime, sqltimestamp, sqlbiginteger, sqlbigdecimal;
		InputStream in = ConstsManager.class
				.getResourceAsStream("/default-relevance.properties");// 获取文件输入流
		Properties prop = new Properties();
		try {
			prop.load(in);// 加载配置信息
			sqlStrings = prop.getProperty(JAVA_STRING);
			sqlbyte = prop.getProperty(JAVA_BYTEARRAY);
			sqllong = prop.getProperty(JAVA_LONG);
			sqlfloat = prop.getProperty(JAVA_FLOAT);
			sqldouble = prop.getProperty(JAVA_DOUBLE);
			sqlinteger = prop.getProperty(JAVA_INTEGER);
			sqlboolean = prop.getProperty(JAVA_BOOLEAN);
			sqldate = prop.getProperty(JAVA_DATE);
			sqltime = prop.getProperty(JAVA_TIME);
			sqltimestamp = prop.getProperty(JAVA_TIMESTAMP);
			sqlbiginteger = prop.getProperty(JAVA_BIGINTEGER);
			sqlbigdecimal = prop.getProperty(JAVA_BIGDECIMAL);
		} catch (IOException e) {
			throw new RuntimeException("读取配置文件异常:" + e.getMessage());
		} catch (NullPointerException e) {
			throw new RuntimeException("没有找到配置文件default-relevance.propreties");
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put(JAVA_STRING, sqlStrings);
		map.put(JAVA_BYTEARRAY, sqlbyte);
		map.put(JAVA_LONG, sqllong);
		map.put(JAVA_FLOAT, sqlfloat);
		map.put(JAVA_DOUBLE, sqldouble);
		map.put(JAVA_INTEGER, sqlinteger);
		map.put(JAVA_BOOLEAN, sqlboolean);
		map.put(JAVA_DATE, sqldate);
		map.put(JAVA_TIME, sqltime);
		map.put(JAVA_TIMESTAMP, sqltimestamp);
		map.put(JAVA_BIGINTEGER, sqlbiginteger);
		map.put(JAVA_BIGDECIMAL, sqlbigdecimal);
		return map;
	}
	
	/**
	 * 加载配置文件default-config.properties的配置信息，并将结果封装在Map集合中。
	 * */
	private final static Map<String,String> loadFrameConfig(){
		System.out.println("加载配置文件default-config.properties");
		InputStream in = ConstsManager.class.getResourceAsStream("/default-config.properties");
		Properties prop = new Properties();
		String value;
		try {
			prop.load(in);
			value=prop.getProperty("VERSION");
		} catch (IOException e) {
			throw new RuntimeException("读取配置文件异常:" + e.getMessage());
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("VERSION", value);	
		return map;
	}
	
	/**
	 * 该方法功能是将mysql中的类型转换成java中的类型。
	 * 开发版本：1705042320v1.0
	 * 参考源地址：http://www.cnblogs.com/jerrylz/p/5814460.html
	 * 
	 * 
	 * */
	public final static String sqltypeToJava(String sqltype){
		int k=0;
		for(k=0;k<12;k++){	//遍历对应的字符串值
			String values=releMap.get(javaTypes[k]);
			//System.out.println(values);
			String[] subStr=Core.substringToArray(values);
			for (String string : subStr) {
				//System.out.println(string);
				if(sqltype.equals(string)==true){
					System.out.println("正在类型转换：将mysql中的类型转换成java中的类型\t"+sqltype+"----->"+javaTypes[k]);
					return javaTypes[k];
				}
			}
		}
		return sqltype;

	}
	
	/**
	 * 根据上面的方法返回的java类型全名的类来判断是否需要导包，并把需要导包的字符串片段返回
	 * */
	public static String importContent(String key){
		char[] c=key.toCharArray();
		if(c[0]=='j'){
			return "import "+key+";";
		}
		return "";

	}
}

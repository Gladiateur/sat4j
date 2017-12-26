/*
 * @(#)FrameRoot.java	1.0 17/04/18
 *
 * Copyright 2017 Gladiateur, Inc. All rights reserved.
 */

package com.sat4j.user;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.sat4j.core.Core;
import com.sat4j.core.DefaultConsts;
import com.sat4j.core.TableManager;

/**
 * 这个类是用户实现功能的入口。 用户通过继承这个类来使用该jar包中的方法。
 * <p>
 * 这个类封装了访问数据库的方法。这个类中的方法基本都在调用类<class>Core</class>中的方法。
 * 用户也可以直接调用类<class>Core</class>中的方法，因为用户并不知道调用顺序，所以这个类进一步封装了底层的方法，
 * 以便用户的使用。这个类实现了接口<interface>TableManager,DefaultConsts</interface>
 * 它们分别扩展了增删改查操作和定义了常量。<br>
 * 用户想使用该jar包中的功能，就要继承该类，然后创建用户自己类的对象。即可调用该类中暴露给外部的全部方法。
 * 使用该jar包用户必须自己配置配置文件。注意poperties配置文件的编码格式必须是utf8,否则会出现乱码。
 * 在以前的版本用户需要手动创建配置文件：default-coneciton.properties和javabean.properties。
 * 然后需要自己记住这些配置文件中的键名。比如配置文件default-coneciton.properties有三个键：dbName,username,root。
 * 现在不需要用户手动创建，用户只需创建一个带有main方法的类，然后继承该类，运行这个空的main方法即可创建全部的配置文件。
 * 并且会初始化这些配置文件，会向配置文件写入的所有键，用户只需配置键值即可。<br>
 * 这个类有一个静态代码块，作用是该类一旦被加载在进内存(被实例化之前)，就会执行底层的方法<method>initConfigs</method>,该方法的作用是
 * 初始化所有的配置文件。静态代码块在类被加载后只执行一次。
 * </p>
 * <p>
 * 在版本v1.6.7.100之后新添了两个方法便于用户对数据库的操作。<br>
 * 实现注册功能的本质其实是向数据库中的某张表插入一条完整的记录。利用mysql中的insert语句可以向数据库中插入一条完整的记录。
 * 这个类中的方法insert便可以轻松的完成这项工作。在这之前，我们需要手动编写代码，需要手动拼接sql语句，比如：<br>
 * String sql="insert into "+表名+" values('"+列名列表+"')";<br>
 * 而现在不需要手动拼接sql语句，insert方法会自动完成。<br>
 * 同样地，实现身份验证的实质是判断数据库中是否有指定的一条记录。这个方法封装了身份验证的过程，用户只需在配置文件javabean.properties中
 * 配置CheckTable的键值，然后调用该方法即可完成验证。
 * </p>
 * 
 * @author Gladiateur
 * @version v1.2 17/6/16
 * @since SAT4j-v1.1-test.jar
 * */
public class FrameRoot implements TableManager,DefaultConsts{

//	/**
//	 * 该静态块用于初始化所有的配置文件
//	 * */
//	static {
//		Core.initConfigs();	
//	}
	
	public final static void init(){
		Core.initConfigs();
	}
	
	/**
	 * 用户的类的全名
	 * */
	public String className;

	/**
	 *  该方法的功能是返回调用者对象的类的全名。 为的是给底层提供反射要用到的参数：类的全名。
	 *  这个方法不允许被重写。
	 * 
	 * */
	public final String getThisClassName() {
		return this.getClass().getName();
	}

	/**
	 *  该方法的功能是获取一个数据库连接对象。
	 *  这个方法不允许被重写。
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * */
	public final Connection getConnection() throws SQLException,
			ClassNotFoundException {	
		return FrameRoot.build(this.getThisClassName());
	}

	/**
	 * 首先调用初始化方法得到一个Object对象，然后调用setFinalString方法设置建立数据库连接的常量。
	 * 设置常量之后再连接数据库，得到一个Connection对象 在这个方法中把底层的异常处理掉，把部分异常抛给调用者。
	 * 这个方法不允许被重写。
	 * 
	 * @throws SQLException,ClassNotFoundException
	 * */
	private final static Connection run(String className) throws SQLException,
			ClassNotFoundException {
		try {
			Core.init0();

		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}
		return Core.getConnection();

	}

	/**
	 * 它的功能是设置是否打印连接的信息。
	 * 这个方法不允许被重写。 
	 * 
	 * @param b 布尔类型，true为打印，false为不打印
	 * */
	private final static void changeVis(boolean b) {
		Core.vis = b;
	}

	/**
	 * 将这个类的方法<method>run</method>和<method>changeVis</method>整合在一起。
	 * 该方法的功能是与数据库建立连接并打印连接信息。
	 * 这个方法不允许被重写。
	 * 
	 * @param className 类的全名
	 * @throws SQLException
	 * */
	private final static Connection build(String className)
			throws SQLException, ClassNotFoundException {
		changeVis(true);
		return FrameRoot.run(className);
	}

	/**
	 * 上层的创建数据库的方法。这个方法不允许被重写，用户直接调用即可。 用户调用此对象时，只需用先前因为连接数据库而创建的对象即可。例如：
	 * <example> 建立连接：new T().getConnection(); 创建数据库：t.doCreate();//T t=new T();
	 * </example>
	 * 这个方法调用底层的方法创建数据库。部分异常在底层被处理，而这个方法只抛出ClassNotFoundException异常给用户处理。
	 * 
	 * @param dbName 数据库名称
	 * @throws ClassNotFoundException
	 * */
	public final void doCreate(String dbName) throws ClassNotFoundException {
		Core.createDatabase(dbName);
	}

	/**
	 * 这个方法用于删除一个数据库，当然，不允许删除mysql自带的数据库 这个方法调用底层，用户可以创建一个对象并调用此方法。这个方法给用户抛出异常。
	 * 当传入一个已存在的数据库名称时，这个方法会首先判断，传入的数据库是否是mysql自带的， 如果是则不允许用户删除，并给用户提示。
	 * 如果不是则删除，并提示删除成功。 如果用户传入了一个不存在的数据库则删除失败，并提示错误信息。
	 * 
	 * @param dbName 数据库名称
	 * @throws ClassNotFoundException
	 * */
	public final void doDelete(String dbName) throws ClassNotFoundException {
		Core.deleteDatabase(dbName);
	}

	/**
	 * 该方法调用底层用来完成输出某数据库中的所有表名。
	 * 遍历数据库中的所有表的表名返回一个表名的字符串数组。
	 * 
	 * @param dbName 数据库名称
	 * */
	public final String[] getTablesName(String dbName) {
		return Core.getTablesName(Core.getTables(dbName));
	}
	
	/**
	 * 该方法调用底层用来完成输出某数据库中的所有表名。
	 * 遍历数据库中的所有表的表名返回一个表名的字符串数组。
	 * 
	 * @param dbName 数据库名称
	 * @param tabName 表名
	 * */
	public final String[] getColsName(String dbName,String tabName) {
		return Core.getColsName(dbName, tabName);
	}
	
	/**
	 * 封装底层方法，得到类型数组。
	 * 根据数据库名和表名返回一个类型的数组。
	 * 
	 *	@param dbName 数据库名称
	 *	@param tabName 表名
	 * */
	public final String[] getTypes(String dbName,String tabName){
		return Core.getTypes(dbName, tabName);
	}
	
	/**
	 * 该方法输出列名和类型的集合。
	 * 根据数据库名和表名得到表的设计结构，然后根据表的结构获取表中的列名Field,类型Type并将它们以键值对的形式封装到Map集合中。
	 *
	 * @param dbName 数据库名称
	 * @param tabName 表名
	 * */
	public final Map<?, ?> designMap(String dbName,String tabName) {
		ResultSet rs = Core.getDesign(dbName,tabName);// 注意这里参数的写法：数据库名.表名
		return Core.getFieldAndTypeMap(rs);
	}

	/**
	 * 自动创建与数据库中所有表的实体类文件
	 * 用户继承这个类创建自己的类的对象然后调用方法，根据配置文件自动生成数据库中所有表的实体类文件。
	 * 
	 * */
	private final void autoBeans0() {
		try {
			this.getConnection(); 
		} catch (SQLException e) {
			throw new RuntimeException("连接数据库时出现了异常："+e.getMessage());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("连接数据库时出现了异常："+e.getMessage());
		}
		Core.autoBeans();
	}
	
	/**
	 * 按照用户的配置自动将某一数据库中的所有表对应生成该表的实体类文件。
	 * */
	public final static void autoBeans(){
		new FrameRoot().autoBeans0();
	}
	
	/**
	 * 自动向数据库中插入一条记录。
	 * <p>
	 * 该方法调用核心层的insert方法，实现自动向数据库中插入一条完整的记录。
	 * </p>
	 * 
	 * @param bean 实体类对象
	 * */
	public final static int insert(Object bean){
		Connection conn = null;
		try {
			conn=new FrameRoot().getConnection();
		} catch (SQLException e) {} 
		catch (ClassNotFoundException e) {}	
		return Core.insert(bean,conn);
	}
	
	/**
	 * 根据用户的配置自动完成身份验证。
	 * 
	 * @param paramId 需要验证的参数1，比如用户名
	 * @param paramPwd 需要验证的参数2，比如密码
	 * */
	public final static boolean check(Object paramId, Object paramPwd){
		return Core.check(paramId, paramPwd);
	}
	
	/**
	 * 
	 * 
	 * 
	 * */
	public final static List<Map<String, Object>> search(String tabName){
		List<Map<String, Object>> list=new ArrayList<Map<String, Object>>();
		try {
			list=Core.convertList(Core.selectResultSet(tabName));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * 
	 * 
	 * 
	 * */
	public final static List<Map<String, Object>> search(String tabName,String cols){
		List<Map<String, Object>> list=new ArrayList<Map<String, Object>>();
		try {
			list=Core.convertList(Core.selectResultSet(tabName,cols));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * 
	 * 
	 * 
	 * */
	public final static List<Map<String, Object>> searchByCondition(String tabName,String conditionStatement){
		List<Map<String, Object>> list=new ArrayList<Map<String, Object>>();
		try {
			list=Core.convertList(Core.selectResultSetByCondition(tabName, conditionStatement));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * 
	 * */
	public final static List<Object> searchTableColValue(String tabName,String col){
		List<Map<String, Object>> list=search(tabName);
		List<Object> valueList=new ArrayList<Object>();
		for (Object object : list) {
			//System.out.println(object);
			Map<?, ?> u=(Map<?,?>) object;
			Object uu=(String) u.get(col);
			valueList.add(uu);
		}
		return valueList;
	}
	
	/**
	 * 将一串由逗号隔开的字符串转化成字符串数组
	 * <p>
	 * 该方法实现了抽象类<tt>AbsFunction</tt>中的方法。这个方法辅助自动验证身份。
	 * </p>
	 * 
	 * @param string 由逗号隔开的字符串
	 * */
	public String[] transformToArray(String string) {
		return Core.substringToArray(string);
	}
	
	/**
	 * 通过表名和条件语句来删除表中的一条记录，返回0则删除失败，返回1则删除成功。
	 * 
	 * @param tabName 表名
	 * @param statement 条件语句
	 * */
	public final static int delete(String tabName,String statement){
		return Core.delete(tabName, statement);
	}
	
	/**
	 * 
	 * @param tabName 表名
	 * @param updateStatement 更新语句
	 * @param conditionStatement 条件语句
	 * */
	public final static int update(String tabName,String updateStatement,String conditionStatement){
		return Core.update(tabName, updateStatement, conditionStatement);
	}
	
	/**
	 * 获取当前系统时间
	 * 
	 * @param timeFormat 时间的输出格式
	 * */
	public final static String getSystemTime(String timeFormat){
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat(timeFormat);//可以方便地修改日期格式
		return dateFormat.format(now);
	}
	
	public final static java.sql.Date transformTime(String dateFormat){
		return Core.transformTime(dateFormat);
	}
	
	@Override
	public void insert(String sql) {
		Connection conn=null;
		Statement stat=null;
		try {
			conn=this.getConnection();
			stat=conn.createStatement();
			stat.execute(sql);
		} catch (SQLException e) {
		
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
	
			e.printStackTrace();
		}
	}
	
	@Override
	public int delete(String sql) {
		Connection conn=null;
		Statement stat=null;
		int state=0;
		try {
			conn=this.getConnection();
			stat=conn.createStatement();
			state=stat.executeUpdate(sql);
		} catch (SQLException e) {
		
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
	
			e.printStackTrace();
		}
		return state;
	}
	
	@Override
	public ResultSet select(String sql) {
		Connection conn=null;
		Statement stat=null;
		ResultSet rs = null;
		try {
			conn=this.getConnection();
			stat=conn.createStatement();
			rs=stat.executeQuery(sql);
		} catch (SQLException e) {
		
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
		
			e.printStackTrace();
		}
		return rs;
	}

	@Override
	public void update(String sql) {
		Connection conn=null;
		Statement stat=null;
		try {
			conn=this.getConnection();
			stat=conn.createStatement();
			stat.execute(sql);
		} catch (SQLException e) {
		
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
	
			e.printStackTrace();
		}
	}
}

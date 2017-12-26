/*
 * @(#)Core.java	1.0 17/04/16
 *
 * Copyright 2017 Gladiateur, Inc. All rights reserved.
 */

package com.sat4j.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import com.Logger;
import com.mysql.jdbc.MysqlDataTruncation;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import com.sat4j.exception.NotStartMySQLServiceException;
import com.sat4j.exception.NullConnectionException;
import com.sat4j.user.FrameRoot;

/**
 * 这个类开始的设计是让它作为核心类。控制服务整个SAT4j。 这个类的所有方法都是静态的，这个类私有了无参构造方法，不允许别的类对它创建对象。
 * <p>
 * <code>Core</code> 没有public的构造函数。 这个类<code>Core</code> 不需要创建对象,因为内部都是静态方法。
 * 这个类负责管理数据库的连接与释放。 这个类<code>Core</code>是一个最终类，
 * 不允许别的类继承该类,该类实现了核心的功能，所以不允许任何类继承。这个类中定义了 一些常量用于实现与数据库的交互。
 * </p>
 * <p>
 * 该类继承了一个抽象类<code>AbsFunction</code>,实现了里面全部的抽象方法，这两个方法服务于自动进行身份验证。
 * </p>
 * <p>
 * 常量：DRIVER-驱动名, URI-数据库地址, USERNAME-数据库用户名, PASSWORD-数据库密码
 * 这些常量的名称必须统一，如果常量名称不对，就会产生错误。
 * </p>
 * <p>
 * 例如在配置文件中这样定义这些常量： </br>
 * <pre>
 * dbName=mysql
 * username=root
 * password=123456
 * </pre>
 * </p>
 * 1.3以后的版本不用再实现接口来传递一个常量对象。而是使用配置文件default-connection.propeties来定义常量数据。
 * <p>
 * 该类被加载进内存时会初始化一些变量，它们的工作有的是为了初始化一些配置文件，有的是初始化与操作数据库有关的对象。
 * </p>
 * <p>
 * 这个类<code>Core</code>所包含的方法服务于SAT4j的核心部分，比如：定义了被保护的数据库的名称(mysql自带的数据库)，
 * 类名规范化，创建数据库，删除数据库，获取数据库连接，获取表的结构，获取列名和类型的集合，获取表中的属性集合，初始化配置文件，
 * 自动生成实体类文件，将一串由列表分隔符连接而成的字符串转换成字符串数组等方法。
 * </p>
 * <p>
 * 本来真正实现生成实体类文件的核心代码是写在类SAT4j里的，后来为了更好的封装核心的代码，就把它单独提了出来。
 * 真正实现生成实体类文件的是方法outputBeans()和autoBeans0()方法的组合使用。这两个方法是私有的，即外部不可见，
 * 但是通过方法autoBeans()来调用autoBeans0()这个方法，再有autoBeans0()去调用方法outputBeans()来实现向文件系统
 * 创建实体类文件。
 * </p>
 * <p>
 * 该类的新特性是新增了处理日志的功能。单独写了有关日志的jar包并把它和SAT4j合并在一起服务于SAT4j。
 * 日志类<tt>Logger</tt>封装了输出日志内容的方法，你可以将异常和有关发生异常的详细说明当做参数输入给print方法，
 * 当出现异常时它便会在默认路径下生成日志文件。	
 * </p>
 * <p>
 * 这一个段落的主角是新添的两个方法，这两个的方法的过程十分烧脑，但我还是花了两天时间搞定了。<br>
 * 在v1.6.7.99版本后通过反射实现了自动向数据库中添加数据的功能和根据配置数据自动验证身份的功能。在构思如何实现自动验证身份功能时
 * 产生了抽象类<tt>AbsFunction</tt>。其中声明了要实现自动身份验证所需要的方法。这两个方法在目前版本中仍然有不足之处：
 * 1.各种异常的处理;2.日志记录;3.配置文件的注释不详细<br>
 * 面对以上的问题在后面会解决。最重要的一点是优化代码过程。
 * </p>
 * <p>
 * sat4j-1.3优化了这个类，提取出公共的变量，提高该类的复用性和jar包的效率。
 * </p>
 * 
 * @author Gladiateur
 * @version v1.3 17/6/19
 * @see Logger
 * @see AbsFunction
 * @since sat4j-v1.3-test.jar
 */
public final class Core extends AbsFunction{
	
	/** 
	 * 不允许这个类实例化。
	 * */
	private Core() {}

	/**
	 * 初始化日志对象
	 * */
	private static Logger log=new Logger();
	
	/**
	 * 初始化connMap对象
	 * 配置文件default-connection.properties被封装成了Map对象。
	 * */
	public static Map<String,String> connMap =null;

	/**
	 * vis:是否打印反馈信息，true打印，false不打印 这个变量完成扩展功能，用户可以根据选择来设置它的值。
	 * 
	 * */
	public static boolean vis = false;

	/**
	 * conn:连接对象，初始化为空。
	 * */
	public static Connection conn = null;
	
	/**
	 * 初始化Statement对象。
	 * */
	private static Statement stat=null;
	
	/**初始化ResultSet对象
	 * 
	 * */
	private static ResultSet rs=null;
	
	/**
	 * 定义被保护的数据库的名称。
	 * */
	private final static String[] protectedDatabase = { "bin_db", "informaion_schema", "mysql",
			"performance_schma", "sys", "world"};
	
	/**
	 * 初始化配置文件信息。
	 * 用户可以创建一个类并且继承FrameRoot，然后再这个类中运行空的主函数即可自动创建并初始化用户需要的配置文件。
	 * 
	 * @see FrameRoot
	 * */
	public final static void initConfigs(){
		System.out.println("初始化配置文件...");
		WriteFileManager.initConnectionConfig();
		WriteFileManager.initBeansConfig();
		System.out.println("配置文件已初始化，请刷新工程！");
	}
	
	/**
	 * 该方法功能是将加载的连接数据库配置文件的数据封装在Map集合里并返回
	 * 为建立数据库的链接做准备。该方法调用常量管理器的方法<method>getConnMap</method>
	 * 通过它获取配置文件default-connection.properties的键值对数据。
	 * 
	 * */
	public final static Map<String,String> init0() {
		return connMap = ConstsManager.getConnMap();
	}

	/**
	 * 
	 * 这个方法建立数据库的连接并返回连接对象。
	 * 该方法通过jdbc驱动连接本地的mysql数据库。通过常量管理器<class>ConstsManager</class>获取到配置文件中配置数据库的连接信息。
	 * 该方法处理了可能发生的异常。 
	 * 
	 * @throws SQLException
	 *             抛出数据库异常
	 * @throws ClassNotFoundException
	 *             抛出找不到类的异常
	 * */
	public final static Connection getConnection() throws SQLException,ClassNotFoundException {
		if (conn == null) {
			try {
				Class.forName(connMap.get("DRIVER"));
				System.out.println("正在建立数据库的连接...");
				conn = DriverManager.getConnection(connMap.get("URL"), connMap
						.get("USERNAME"), connMap.get("PASSWORD"));
				System.out.println("\t得到连接对象conn：" + conn);
				//System.out.println("\t此时vis=" + vis);
				if (vis == true) {
					System.out.println("\t数据库 {" + connMap.get("URL") + "} 已连接");
				}
			} catch (MySQLSyntaxErrorException e) {
				log.print(e, "创建连接时出现的异常 ："+e.getMessage());
				throw new RuntimeException("创建连接时出现的异常 ："+e.getMessage());
			} catch (SQLException e) {
				if (e.getErrorCode() == 1045) {
					log.print(e, "配置文件中root值或password值错误！\n不存在数据库用户：["
									+ connMap.get("USERNAME") + "],或密码错误");
					throw new RuntimeException(
							"配置文件中root值或password值错误！\n不存在数据库用户：["
									+ connMap.get("USERNAME") + "],或密码错误");
				}
			} catch (NullPointerException e) {
				log.print(e, "创建连接时出现空指针异常：" + e.getMessage());
				throw new RuntimeException("创建连接时出现空指针异常：" + e.getMessage());
			} catch (RuntimeException e) {
				System.out
						.println("com.sat4j.core.Core.getConnection():没有找到配置文件default-connection.propreties");
			}
			return conn;
		}
		return conn;
	}

	/**
	 * 该方法用于创建一个数据库，这个方法处理了部分异常。
	 * 用户将会先与数据库创建连接，之后便可以创建数据库，而这个方法是底层的，只是提供给上层，用户调用上层的方法即可。
	 * 使用此方法时，需要传入准创建数据库的名称，该名称格式务必合法，否则将会报错。 如果创建两次相同的数据库同样会报错。
	 * 当创建成功时，会提示用户数据库XXX创建成功。失败时会提示错误代码。
	 * 
	 * @param dbName
	 *            数据库名称
	 * @throws SQLException
	 * */
	public final static void createDatabase(String dbName)
			throws ClassNotFoundException {
		// Connection conn = Core.getConnection()
		String sql = "create database " + dbName;
		// ResultSet rs = null;
		try {
			stat = conn.createStatement();
			stat.execute(sql);
			System.out.println("数据库：[" + dbName + "] 创建成功！");
		} catch (MySQLSyntaxErrorException e) {
			System.out.println("创建数据库时异常:" + e.getMessage());
		} catch (SQLException e) {
			if (("Can't create database '" + dbName + "'; database exists")
					.equals(e.getMessage())) {
				System.out.println("错误号：" + e.getErrorCode() + ",创建数据库失败，数据库["
						+ dbName + "] 已存在！");
			} else {
				e.printStackTrace();
				System.out.println("com.sat4j.core.Core.createDatabase(Stirng dbName):出现未知的错误！");
			}
		} catch (NullPointerException e) {
			System.out.println("com.sat4j.core.Core.createDatabase(Stirng dbName)：创建数据库时出现空指针异常：" + e.getMessage());
		}
	}

	/**
	 * 这个方法用于删除一个数据库，当然，不允许删除mysql自带的数据库 这个方法提供给上层，不需要让用户直接调用此方法。这个方法给上层抛出异常。
	 * 当传入一个已存在的数据库名称时，这个方法会首先判断，传入的数据库是否是mysql自带的， 如果是则不允许用户删除，并给用户提示。
	 * 如果不是则删除，并提示删除成功。 如果用户传入了一个不存在的数据库则删除失败，并提示错误信息。
	 * 
	 * @param dbName
	 *            数据库名称
	 * @throws ClassNotFoundException
	 * */
	public final static void deleteDatabase(String dbName)
			throws ClassNotFoundException {
		for (String string : protectedDatabase) {
			if (string.equals(dbName) == true) {
				System.out.println("受保护的数据库[" + dbName + "]不允许被删除！");
				return;
			}
		}
		String sql = "drop database " + dbName;
		Statement stat = null;
		try {
			stat = conn.createStatement();
			stat.execute(sql);
			System.out.println("数据库：[" + dbName + "] 删除成功！");
		} catch (MySQLSyntaxErrorException e) {
			System.out.println("你有一个语法错误：" + e.getMessage());
		} catch (SQLException e) {
			if (("Can't drop database '" + dbName + "'; database doesn't exist")
					.equals(e.getMessage())) {
				System.out.println("错误号：" + e.getErrorCode() + ",删除数据库失败，数据库["
						+ dbName + "] 不存在！");
			} else {
				e.printStackTrace();
			}
		} catch (NullPointerException e) {
			System.out.println("删除数据库时出现空指针异常：" + e.getMessage());
		}

	}

	/**
	 * 这个方法用于输出一个数据库中的表名集合
	 * 
	 * @param dbName 数据库名称
	 * */
	public final static ArrayList<String> getTables(String dbName){
		Statement stat = null;
		ResultSet rs = null;
		ArrayList<String> list = new ArrayList<String>();
		String tabName = null;
		String sql = null;
		try {
			stat = conn.createStatement();
			sql = "use " + dbName;
			stat.execute(sql);
			sql = "show tables;";
			rs = stat.executeQuery(sql);
			if (rs != null) {
				while (rs.next()) {
					tabName = rs.getString("Tables_in_" + dbName);
					list.add(tabName);
				}
			}
		} catch (MySQLSyntaxErrorException e) {
			if (e.getErrorCode() == 1064) {
				throw new RuntimeException("存在语法错误，请检查数据库名称"+e.getMessage());
			}
			if (("Unknown database '" + dbName + "'").equals(e.getMessage())) {
				log.print(e, "你输入了一个不存在的数据库！\t"+e.getMessage());
				throw new RuntimeException("数据库[" + dbName + "]不存在");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch(NullPointerException e){
			log.print(e, "获取表名集合时发生异常！可能是由于mysql服务未启动造成");
			throw new NotStartMySQLServiceException("获取表名集合时发生异常！可能是由于mysql服务未启动造成");
		} 
		return list;
	}

	/**
	 * 该方法功能是根据表名集合循环输出表名数组 
	 * */
	public final static String[] getTablesName(ArrayList<String> list) {
		String[] tabs = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			tabs[i] = (String) list.get(i);
		}
		return tabs;
	}
	
	/**
	 * 重载上面的方法
	 * */
	public final static String[] getTablesName(String dbName,ArrayList<String> list){
		String[] tabs = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			tabs[i] = dbName+"."+(String) list.get(i);
		}
		return tabs;	
	}
	
	// desc [dbName.tabName];输出设计视图的集合
	/**
	 * 该方法返回一个表的结构，封装在ResultSet集合中
	 * */
	public final static ResultSet getDesign(String dbName,String tabName) {
		System.out.println("正在获取该表的结构...");
		Statement stat = null;
		ResultSet rs = null;
		String sql = null;
		try {
			stat = conn.createStatement();
			if("order".equals(tabName)==true){
				tabName=dbName+"."+tabName;
			}
			sql = "desc " + tabName;
			rs = stat.executeQuery(sql);
		} catch (SQLException e) {
			System.out.println("检测语句："+sql);
			System.out.println("获取表结构异常：" + e.getMessage());
			log.print(e, "获取表结构异常：" + e.getMessage()+"\t位置：com.sat4j.core.Core.getDesign");
		} catch(NullPointerException e){
			throw new NullConnectionException("与数据库的连接是空的！！！您还未取得连接！！！");
		}
		return rs;
	}

	/**
	 * 根据getDesign方法返回的结果把<Field,Type>封装到一个Map集合
	 * */
	public final static Map<String, String> getFieldAndTypeMap(ResultSet rs) {
		//System.out.println("根据getDesign方法返回的结果把<Field,Type>封装到一个Map集合");
		Map<String, String> map = new HashMap<String, String>();
		if (rs != null) {
			try { 
				while (rs.next()) {
					map.put(rs.getString("Field"), rs.getString("Type"));
				}
			} catch (SQLException e) {
				System.out.println("将ResultSet集合中的Field,Type的值封装到Map集合视出现异常："
						+ e.getMessage());
			}
		}
		//System.out.println("Map map=" + map);
		return map;
	}

	/**
	 * 根据getFieldAndTypeMap方法返回的Map集合得到表结构中所有的列名，封装到ArrayList集合
	 * */
	public final static ArrayList<String> getFieldList(ResultSet rs) {
		ArrayList<String> list = new ArrayList<String>();
		if (rs != null) {
			try {
				while (rs.next()) {
					list.add(rs.getString("Field"));
				}
			} catch (SQLException e) {
				System.out.println("获取表中列名集合时出现异常：" + e.getMessage());
			}
		}
		return list;
	}
	
	/**
	 * 得到类型数组
	 * */
	public final static ArrayList<String> getTypeList(ResultSet rs){
		ArrayList<String> list = new ArrayList<String>();
		if (rs != null) {
			try {
				while (rs.next()) {
					list.add(rs.getString("Type"));
				}
			} catch (SQLException e) {
				System.out.println("获取表中类型集合时出现异常：" + e.getMessage());
			}
		}
		return list;
	}
	
	/**
	 * 根据list，遍历出所有的Field(列名)的值，返回列名数组
	 * 
	 * 注意数组的初始化
	 * 
	 * 
	 * */
	public final static String[] getColsName(ArrayList<String> list) {
		String[] cols = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			cols[i] = (String) list.get(i);
		}
		return cols; // 测试通过
	}
	
	/**
	 * 封装底层的方法，根据数据库名和表名返回表中列名数组。
	 * 
	 * ·@param dbName 数据库名称
	 *	@param tabName 表名
	 * */
	public final static String[] getColsName(String dbName,String tabName) {
		ResultSet rs = getDesign(dbName,tabName);// 注意这里参数的写法：数据库名.表名
		ArrayList<String> list = getFieldList(rs);
		String[] cols = getColsName(list);
		return cols;
	}
	
	/**
	 * 根据list,遍历所有的Type的值
	 * */
	public final static String[] getTypes(ArrayList<String> list){
		String[] types = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			types[i] = (String) list.get(i);
		}
		return types; // 测试通过
	}
	
	/**
	 * 封装底层方法，得到类型数组。
	 * 根据数据库名和表名返回一个类型的数组。
	 * 
	 *	@param dbName 数据库名称
	 *	@param tabName 表名
	 * */
	public final static String[] getTypes(String dbName,String tabName){
		ResultSet rs = getDesign(dbName,tabName);// 其实完整的表名应该是：数据库名.表名，在mysql中输入desc order会报错
		ArrayList<String> list = getTypeList(rs);
		String[] types = getColsName(list);
		return types;
	}
	

	/**
	 * 计算ResultSet集合的深度
	 * 
	 * 类似的，在集合Map中，它的对象有计算长度的方法size(),而集合ResultSet中没有这种方法
	 * 所以这个方法相当于是计算ResultSet集合的长度，其实应该是高度(深度)
	 * 
	 * 
	 * */
	public final static int deep(ResultSet rs) {
		int i = 0;
		if (rs != null) {
			try {
				while (rs.next()) {
					i++;
				}
			} catch (SQLException e) {
			}
		}
		return i;
	}
	
	
	/**
	 * 首字母大写
	 * 
	 * 因为数据库不区分大小写，而java严格区分大小写，所以不能直接根据遍历的表名数组来创建java文件
	 * 而是把遍历表名数组得到的表名经过首字母大写处理后再去创建实体类文件，这样符合java的命名规范。
	 * */
	public final static String captureName(String name) {
		// name = name.substring(0, 1).toUpperCase() + name.substring(1);
		// return name;
		if(isLowwer(name)==true){
			char[] cs = name.toCharArray();
			cs[0] -= 32;
			return String.valueOf(cs);
		}else{
			return name;
		}

	}
	
	/**
	 * 创建实体类文件的方法
	 * 
	 * 该方法用于自动创建用户指定的数据库里各表的实体类
	 * 创建成功后请刷新工程
	 *
	 * */
	public final static void createFile(String fileName,String filepath){	
		String javaFileName=Core.captureName(fileName);
		System.out.println("/*****************************create the new javabean file*********************************/\n正在创建实体类："+javaFileName+".java");
		File directory = new File("");// 设定为当前文件夹
		String path = null;
		try {
			path = directory.getCanonicalPath()
					+ filepath;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		File f = new File(path);
		if (!f.exists()) {
			f.mkdirs();
		}

		javaFileName = javaFileName+".java";
		File file = new File(f, javaFileName);
		if (!file.exists()) {
			try {
				file.createNewFile();
				System.out.println("javabean:"+javaFileName+"创建成功！");
			} catch (IOException e) {
				log.print(e, "创建实体类文件时出现空指针异常，位置:com.sat4j.core.Core.createFile");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * defaule-relevance.properties配置文件被封装成了Map<K,V>集合。
	 * 其中K就是键名,V就是键值,比如K=String;V=char,varchar,text。
	 * 这个方法就是把键值V根据列表分隔符转化成一个字符串型的数组。
	 * 这个方法参考百度。将一串由列表分隔符连接而成的字符串转换成字符串数组。
	 * 比如：String text="char,varchar,text";
	 * 将text作为参数调用该方法的结果为:String[] sqlTypeArray={"char","varchar","text"};
	 * 
	 * @param String mapValue 配置文件default-relevance.properties的每个键的键值
	 * @return String[] sqlTypeArray sql类型数组
	 * */
	public final static String[] substringToArray(String mapValue){
        StringTokenizer st = new StringTokenizer(mapValue,",");//把","作为分割标志，然后把分割好的字符赋予StringTokenizer对象。
        String[] sqlTypeArray = new String[st.countTokens()];//通过StringTokenizer 类的countTokens方法计算在生成异常之前可以调用此 tokenizer 的 nextToken 方法的次数。
        int i=0;
        while(st.hasMoreTokens()){//看看此 tokenizer 的字符串中是否还有更多的可用标记。
        	sqlTypeArray[i++] = st.nextToken();//返回此 string tokenizer 的下一个标记。
        }
        return sqlTypeArray;
    }
	
	/**
	 * 关于首字母，若已经是大写的，比如"P"，那么就不需要执行char[i]-32;
	 * 否则，结果首字母会会变为"0"，这样做不是预期效果。
	 * captureName方法应该先对首字母进行判断，判断是否是小写。
	 * 该方法功能是对一个字符串的首字母进行判断，判断其是否为小写，若是则返回true,若不是则返回false
	 * 在ASCII码中97-122号为26个小写英文字母，据此来判断单个字符是否为小写。
	 * 这里说明为什么需要这个方法：因为在以前的SAT4j版本中，没有这个方法时，出现了一个小错误,
	 * 当时数据库中的某张表的表名的首字母是"P",就是说它已经是大写的了,在执行了方法captureName后出现了不符合预期的结果。
	 * 因为不知道用户的数据库中的表名的首字母是否是大写，而实体类的类名是按照表名生成的并且java中的命名规范要求类名
	 * 首字母必须大写,所以要先判断表名的首字母是否是小写,若是,则执行首字母大写的方法captureName,否则不执行。
	 * 
	 * @param table 数据库中的表名
	 * */
	public final static boolean isLowwer(String table) {
		byte[] b = table.getBytes();
		if (b[0] >= 97 && b[0] <= 122) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 该方法的目的是实现自动生成实体类。
	 * 自动生成实体类的核心部分，把核心代码单独封装在这个类中。
	 * 首先根据实体类文件名和文件路径创建空的实体类文件，之后按照规则写入内容。
	 * 这段核心代码原本是写在类FrameRoot里的方法autoBeans()里的。这里做了核心代码的提取工作。
	 * 将核心代码单独封装在核心类中并且私有化,通过方法getOutputBeans将该功能暴露到外部,再在上层的类
	 * 中调用暴露出去的方法即可。
	 * 
	 * @param String[] tabNames 数据库表名数组
	 * @param String path 实体类文件的相对路径
	 * @param Stirng dbName 数据库名称
	 * @param String packName 实体类文件的目标包名
	 * */
	private final static void outputBeans(String[] tabNames,String path,String dbName,String packName){
		for (String fileName : tabNames) {
			// System.out.println(fileName);
			createFile(fileName, path);
			WriteFileManager.writer(dbName,path, fileName,packName);
		}
	}
	
	/**
	 * 生成实体类文件的核心方法，这个方法私有化。
	 * */
	private final static void autoBeans0(){
		String dbName, path;
		String[] tabNames;
		FrameRoot fr = new FrameRoot();
		Map<String, String> beanMap = ConstsManager.getBeanMap();// 获取javabean.properties配置文件的Map集合
		// System.out.println(map.get("dbName"));
		// System.out.println(map.get("path"));
		path = beanMap.get("path"); 
		Map<String,String> connMap=ConstsManager.getConnMap();
		dbName=connMap.get("dbName");	//以default-connection.properties中的值为准
		String packName=formatToPackage(path); 
		//System.out.println("1.6.2测试： dbName= "+dbName + "::: path= " + path);
		tabNames = fr.getTablesName(dbName);
		outputBeans(tabNames,path,dbName,packName);
		System.out.println("所有文件创建成功请刷新您的工程！");
		log.print("你已成功创建了数据库"+dbName+"中的实体类");
	}
	
	/**
	 * 路径格式转报名格式
	 * windows文件系统的路径格式为:com\a\b\c,在java中的路径格式为:com.a.b.c;所以需要将配置文件javabean.properties
	 * 中的文件系统的路径格式转换为java中的路径格式。
	 * */
	private final static String formatToPackage(String path) {
		String packName;
		path = path.substring(5);	///path的完全格式是\src\xxx\xxx...
		packName = path.replace('\\', '.');
		System.out.println("路径格式转包名格式："+path+"--->"+packName);
		return packName;
	}
	
	/**
	 * 调用私有化的生成实体类的核心方法。
	 * */
	public final static void autoBeans(){
		autoBeans0();
	}
	
	/**
	 * 从实体类对象获取表名
	 * */
	private final static String getTabName(Object bean){
		String string=bean.getClass().getName();
		int i= string.lastIndexOf('.');
		return string.substring(i+1).toLowerCase();
	}
	
	//insert 自动实现注册的方法  17/5/21-22
	/**
	 * 自动向数据库中插入一条记录。
	 * <p>
	 * 每个实体类是每张表的映射。该方法传入一个实体类对象便可通过该对象获取表名也可以通过这个对象反射出各个
	 * 属性。执行自动拼接的sql语句。
	 * </p>
	 * <p>
	 * 该方法的参数是一个实体类对象，诸如"User,Student..."之类的对象,在定义这个方法时还不确定参数具体是谁的实例，
	 * 利用多态，这些对象统一都是<tt>Object</tt>类的实例。所以定义参数的类型为Object。<br>
	 * 执行过程：首先根据传入的对象获取该对象的全类名，再根据全类名得到它的Class对象，然后通过反射得到实体类对象中的所有getter方法。
	 * 将这些方法存储在Map集合中。之后通过循环拼接出sql语句。最后执行这个sql语句。
	 * </p>
	 * <p>
	 * 该方法的过程比较复杂，核心的思想是高级for循环和反射的混合使用。这个方法目前还处于初级阶段，需要处理各种异常。
	 * 比较复杂的是拼接sql语句，其中有这么一个算式：<br>
	 * String sql="('"+getField1()+"','"+getField2()+"','"+...+"','"+getFieldn();+"')";<br>
	 * 对以上式子的分析：从getField1()到getFieldn()都是实体类中除了getClass()的其他所有get方法。注意它们的返回类型不一定相同。
	 * 但是无论它们的返回类型是什么都不影响它们可以直接拼接sql语句。
	 * </p>
	 * 
	 * @param bean 实体类对象
	 * @param conn 连接对象
	 * */
	public static int insert(Object bean,Connection conn){
		if(conn==null){
			throw new NullConnectionException("连接对象为空！");
		}
		if(bean==null){
			throw new NullPointerException("你正试图向表中插入一条记录，但这条记录是空的！SAT4j终止了你的操作，请检查对象：["+bean+"]是否为空。");
		}
		int n=0;
		//1.获取类的全名
		String beanName=bean.getClass().getName();	//com.bean.Exam
		//System.out.println(beanName);
		//2.通过反射，得到所有的属性名
		Class<?> c = null;
		try {
			c = Class.forName(beanName);
		} catch (ClassNotFoundException e1) {}
		//System.out.println(c);
		Method[] method=c.getMethods();
		//3.通过反射获取属性值
		int GETNUM=0;
		List<Method> list=new ArrayList<Method>();
		for (Method method2 : method) {
			if(("get".equals(method2.getName().substring(0, 3)))==true){
				//temp=method2.invoke(bean);		
				if("Class".equals(method2.getName().substring(3))==false){
					//System.out.println(method2.getName().substring(3));
					GETNUM++;
					list.add(method2);
				}	
			}
		} 
//		System.out.println("GETNUM= "+GETNUM);
//		for (int i = 0; i < list.size(); i++) {
//			System.out.println(i+" "+list.get(i).getName());
//		}
		
		//4.循环生成sql语句的成分	
		String sql="";
		//INSERT INTO tbl_name (col1,col2) VALUES(15,col1*2);
		try{
			for (int i = 0; i < GETNUM; i++) {
				if(i==0){
					sql+=("('"+list.get(i).invoke(bean)+"','");
				}
				if(i != GETNUM-1 && i != 0){
					sql+=(list.get(i).invoke(bean)+"','");
				}
				if(i == GETNUM-1){
					sql+=(list.get(i).invoke(bean)+"')");
				}
			}
		} catch(IllegalAccessException e){
			log.print(e);
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			log.print(e);
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			log.print(e);
			e.printStackTrace();
		}
		String colList="";
		for (int j = 0; j < list.size(); j++) {
			if(j != list.size()-1 ){
				colList+=(list.get(j).getName().substring(3)+",");
			}else{
				colList+=(list.get(j).getName().substring(3));
			}
		}
		colList="("+colList+") ";
		sql="insert into "+getTabName(bean)+ colList +" values"+sql+";";
		//System.out.println(colList+"\n"+sql);
		try {
			 stat=conn.createStatement();
			 n=stat.executeUpdate(sql);
			 System.out.println("受影响的行数："+n);
			 log.print("成功插入一条数据，受影响的行数："+n);
		} catch(MysqlDataTruncation e){
			log.print(e, "插入记录时字段超长！"+e.getMessage());
			throw new RuntimeException("插入记录时字段超长！"+e.getMessage());
		} catch(MySQLIntegrityConstraintViolationException e){
			if(e.getErrorCode()==1062){
				System.out.println("主键重复！"+e.getMessage());
			}
		} catch (SQLException e) {
			log.print(e, "自动插入数据时出现了未知的异常！");
			e.printStackTrace();
		} 
		return n;
	}
	
	/**
	 * 根据用户的配置自动完成身份验证。
	 * <p>
	 * 该方法实现了抽象类<tt>AbsFunction</tt>中的方法。
	 * </p>
	 * 
	 * @param paramId 需要验证的参数1，比如用户名
	 * @param paramPwd 需要验证的参数2，比如密码
	 * @param conn 连接对象
	 * */
	@Override
	public boolean checkStatus(Object paramId, Object paramPwd, Connection conn) {
		String tabName="",col1="",col2="";
		//1.获取配置文件的映射
		Map<String,String> beanMap=ConstsManager.getBeanMap();
		//2.获取CheckTable键值
		String checkTableString=beanMap.get("checkTableString");
		//3.字符串转为数组
		String[] checkTable=transformToArray(checkTableString);//这里应该需要判空
		//4.获取子字符串
		try{
			 tabName=checkTable[0];
			 col1=checkTable[1];
			 col2=checkTable[2];
		}catch(ArrayIndexOutOfBoundsException e){
			log.print(e);
			throw new RuntimeException("配置文件参数有错误！在配置文件javabean.properties中的CheckTable的键值应该是：表名，列名1，列名2");
		}
		
		
		//5.以下是拼接sql语句的核心代码
		//String sql="select * from "+tabName+"("+col1+","+col2+")"+" where "+col1+"='"+paramId+"' and "+col2+"='"+paramPwd+"'";
		String sql="select "+col1+" from "+tabName+" where "+col1+"='"+paramId+"' and "+col2+"='"+paramPwd+"';";
		System.out.println(sql);
		Object colObj = null;
		try {
			stat=conn.createStatement();
			rs=stat.executeQuery(sql);
		} catch(MySQLSyntaxErrorException e){
			if(e.getErrorCode()==1146){
				log.print(e, "CheckTable的键值配置有误：表["+tabName+"]不存在！");
				throw new RuntimeException("CheckTable的键值配置有误：表["+tabName+"]不存在！");
			}else if(e.getErrorCode()==1054){
				log.print(e, "CheckTable的键值配置有误:列["+col1+"]或["+col2+"]不存在！");
				throw new RuntimeException("CheckTable的键值配置有误:列["+col1+"]或["+col2+"]不存在！");
			}else{
				log.print(e, "自动验证身份是发生的未知异常！该异常信息已被日志记录！异常号："+e.getErrorCode()+" 异常信息："+e.getMessage());
				throw new RuntimeException("自动验证身份是发生的未知异常！该异常信息已被日志记录！"+e.getMessage());
			}		
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		if(rs!=null){
			try {
				while(rs.next()){
					colObj=rs.getObject(col1);
				}
				//System.out.println(colObj);
				if(colObj!=null){
					return true;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}	
		}else{
			return false;
		}
		return false;
	}
	
	/**
	 * 将一串由逗号隔开的字符串转化成字符串数组
	 * <p>
	 * 该方法实现了抽象类<tt>AbsFunction</tt>中的方法。这个方法辅助自动验证身份。
	 * </p>
	 * 
	 * @param string 由逗号隔开的字符串
	 * */
	@Override
	public String[] transformToArray(String string) {
		return substringToArray(string);
	}
	
	/**
	 * 根据用户的配置自动完成身份验证
	 * 
	 * @param paramId 需要验证的参数1，比如用户名
	 * @param paramPwd 需要验证的参数2，比如密码
	 * */
	public final static boolean check(Object paramId, Object paramPwd){
		//Connection conn = null;	//抽离公共变量 测试通过
		try {
			conn = new FrameRoot().getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return new Core().checkStatus(paramId, paramPwd, conn);
	}
	
	/**
	 * 通过表名将数据存入ResultSet中。
	 * 
	 * @param tabName 表名
	 * */
	public final static ResultSet selectResultSet(String tabName){
		//Connection conn = null;	//测试通过
		try {
			conn = new FrameRoot().getConnection();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		Statement stmt=null;
		ResultSet rs=null;
		String sql="select * from "+tabName;
		try {
			stmt=conn.createStatement();
			rs=stmt.executeQuery(sql);
			if(rs!=null){
				return rs;
			}else{
				throw new RuntimeException("表的结果为空！"+tabName);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		return rs;
	}
	
	/**
	 * 通过表名和列名字符串将数据存入ResultSet中。
	 * 
	 * @param tabName 表名
	 * @param cols 列名列表字符串，例如：name,sex,...
	 * */
	public final static ResultSet selectResultSet(String tabName,String cols){
		//Connection conn = null;	//测试通过
		try {
			conn = new FrameRoot().getConnection();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		Statement stmt=null;
		ResultSet rs=null;
		String sql="select "+cols+" from "+tabName;
		try {
			stmt=conn.createStatement();
			rs=stmt.executeQuery(sql);
			if(rs!=null){
				return rs;
			}else{
				throw new RuntimeException("表的结果为空！"+tabName);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}
	
	/**
	 * 
	 * */
	public final static ResultSet selectResultSetByCondition(String tabName,String conditionStatement){
		//Connection conn = null;	测试通过
		try {
			conn = new FrameRoot().getConnection();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		Statement stmt=null;
		ResultSet rs=null;
		String sql="select * from "+tabName+" where "+conditionStatement;
		try {
			stmt=conn.createStatement();
			rs=stmt.executeQuery(sql);
			if(rs!=null){
				return rs;
			}else{
				throw new RuntimeException("表的结果为空！"+tabName);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		return rs;
	}
	
	/**
	 * 该方法将ResultSet结果集转换为List集合。
	 * <p>
	 * 参考：http://jingyan.baidu.com/article/8065f87f80dd5c233124980f.html
	 * </p>
	 * 
	 * @param rs 表的结果集
	 * @param SQLException
	 * */
	public final static List<Map<String, Object>> convertList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        ResultSetMetaData md = rs.getMetaData();
        int columnCount = md.getColumnCount();
        while (rs.next()) {
            Map<String, Object> rowData = new HashMap<String, Object>();
            for (int i = 1; i <= columnCount; i++) {
                rowData.put(md.getColumnName(i), rs.getObject(i));
            }
            list.add(rowData);
        }
        return list;
	}
	
	/**
	 * 通过表名和条件语句来删除表中的一条记录，返回0则删除失败，返回1则删除成功。
	 * 
	 * @param tabName 表名
	 * @param statement 条件语句
	 * */
	public final static int delete(String tabName,String statement){
		//Connection conn = null;	//测试通过
		Statement stmt=null;
		int count=0;	//	0 means fail
		String sql="delete from "+tabName+" where "+statement;
		try {
			conn = new FrameRoot().getConnection();
			stmt=conn.createStatement();
			count=stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return count;
	}
	
	/**
	 * 通过表名，修改语句和条件语句来更新表中的一条记录，返回0则更新失败，返回1则更新成功。
	 * 
	 * @param tabName 表名
	 * @param updateStatement 更新语句
	 * @param conditionStatement 条件语句
	 * */
	public final static int update(String tabName,String updateStatement,String conditionStatement){
		//Connection conn = null;	测试通过
		//Statement stat=null;
		int count=0;	//	0 means fail
		String sql="update "+tabName+" set "+updateStatement+" where "+conditionStatement;
		try {
			conn = new FrameRoot().getConnection();
			stat=conn.createStatement();
			count=stat.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return count;
	}
	
	/**
	 *	获取当前时间，格式设置为yyyy-MM-dd,并转换成mysql中的类型。
	 *	参考：http://www.cnblogs.com/zhaotiancheng/p/6413067.html
	 *	<p>
	 *	获取当前系统时间，并转换成mysql数据库所支持的格式。
	 *	</p>	
	 *
	 *	@param dateFormat 设置日期格式
	 * */
	public final static java.sql.Date transformTime(String dateFormat){
	       SimpleDateFormat format = new SimpleDateFormat(dateFormat);
	       String time = format.format(new Date());
	        java.sql.Date timePara  = null;
	        try {
	            timePara = new java.sql.Date(format.parse(time).getTime());
	            System.out.println(timePara);
	        } catch (ParseException e) {
	            e.printStackTrace();
	        }
	        return timePara;
	    }
	
}

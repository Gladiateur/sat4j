/*
 * @(#)WriteFileManager.java	1.0 17/04/30
 *
 * Copyright 2017 Gladiateur, Inc. All rights reserved.
 */

package com.sat4j.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException; //import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Set;
import com.sat4j.core.ConstsManager;

/**
 * 这个类<code>WriteFileManager</code>负责向已经创建好的文件中写入内容
 * <p>
 * 该类含有初始化配置文件的方法。
 * </p>
 * <p>
 * 该类包含了实现自动生成实体类文件的若干方法。
 * </p>
 * 
 * @author Gladiateur
 * @version v1.7 17/6/2
 * */
public class WriteFileManager {

	/**
	 * 生成实体类中的内容
	 * <p>
	 * 把javabean中的代码分为两个段落，第一个段落是私有化属性的部分，比如：private String id;等等;
	 * 第二个段落是提供getter,setter方法部分，将这两个段落装在字符串数组中返回。
	 * </p>
	 * <p>
	 * 在v1.6.7.161版本之后生成实体类时同时生成无参数的构造方法和全参数的构造方法。
	 * </p>
	 * */
	private final static String[] beanContent(String dbName, String tabName) {
		System.out
				.println("正在生成实体类中的内容...");
		String[] cols = Core.getColsName(dbName, tabName);
		String[] types = Core.getTypes(dbName, tabName);
		String[] newTypes = new String[types.length];
		String[] textArray = new String[3];
		final String CONSTRUCTCODE=constructor(dbName, tabName);
		String page = "";
		String fun = "";
		String str1 = "\t/**封装各属性**/";
		String str2 = "\t/**提供getter,setter方法**/";
		String imp = "";
		// for (String string2 : types) {
		// int endFlag=string2.indexOf("(");
		// System.out.println("得到末尾值："+endFlag);
		// String type=string2.substring(0, endFlag);
		// System.out.println("得到类型："+type);
		//			
		// }
		for (int i = 0; i < types.length; i++) {
			// int endFlag=types[i].indexOf("(");
			newTypes[i] = types[i] + "(";
			// System.out.println("得到类型："+newTypes[i]);
		}
		for (int i = 0; i < newTypes.length; i++) {
			int endFlag = newTypes[i].indexOf("(");
			newTypes[i] = newTypes[i].substring(0, endFlag);

		}
		System.out.println("%%%%--------------正在自动生成代码--------------%%%%");
		for (String string : cols) {
			System.out.println("得到属性名：" + string);
		}
		for (String string2 : newTypes) {
			if ("varchar".equals(string2) == true) {
				string2 = "String";
			}
			System.out.println("得到类型：" + string2);
		}
		// ///////////////////////////////////////////////////////////////////////////
		for (int j = 0; j < newTypes.length; j++) {
			newTypes[j] = ConstsManager.sqltypeToJava(newTypes[j]);
			String text = "\tprivate " + newTypes[j] + " " + cols[j] + ";\n";
			page = page + text;
		}
		System.out.println(page);

		for (int i = 0; i < newTypes.length; i++) {
			newTypes[i] = ConstsManager.sqltypeToJava(newTypes[i]);
			String fun0 = "\n\tpublic " + newTypes[i] + " get"
					+ Core.captureName(cols[i]) + "(){\n \t\treturn " + cols[i] // 比如将getid变为getId
					+ ";\n\t}\n";
			String fun1 = "\tpublic void set" + Core.captureName(cols[i]) + "("
					+ newTypes[i] + " " + cols[i] + "){\n \t\tthis." + cols[i] // 比如将setid变为setId
					+ "=" + cols[i] + ";\n\t}";
			String fun2 = fun0 + fun1;
			fun = fun + fun2;
		}
		// ////////////////////////////////////////////////////////////////////////////
		
		textArray[0] = imp;
		textArray[1] = "\n" + str1 + "\n\n" + page + "\n" + str2 + "\n" + fun; // 这个数组有两个元素(包括两大块)，第一块是"引包"，第二块是实体类的内容。
		textArray[2] = CONSTRUCTCODE;
		return textArray;
	}

	/**
	 * 自动创建实体类
	 * 该方法实现了根据用户给的数据库名称，遍历这个数据库中的所有的表，并自动生成所有的实体类。
	 * 该方法的实现其实是调用了文件系统的输出流，将字符串写入到文件中保存在用户的硬盘上。
	 * */
	public final static void writer(String dbName, String filepath,
			String fileName,String packName) {
		System.out
				.println("写入内容中...");
		// System.out.println("测试，攻关2，WriteFileExample.filepath="+filepath);
		// System.out.println("测试表名："+fileName);
		String[] pageArray = beanContent(dbName, fileName);
		String imp = pageArray[0];
		String page = pageArray[1];
		String constrct=pageArray[2];
		String javaFileName = Core.captureName(fileName);
		String packContent = "";
		FileOutputStream fop = null;
		Map<String,String> configMap=ConstsManager.getConfigMap();
		// System.out.println("================================测试packName= "+packName);
		if ("".equals(packName)) {
			packContent = "\n//default package";
		} else {
			packContent = "package " + packName + ";";
		}
		File file;
		String content = packContent + "\n\n" + imp
				+ "\n\t/**通过"+configMap.get("VERSION")+"自动创建的实体类**/ \n\npublic class "
				+ javaFileName + "{ \n" + page + "\n\n" + constrct +"\n\n }";
		String path;
		try {

			file = new File("");
			path = file.getCanonicalPath() + filepath + "\\" + javaFileName
					+ ".java";

			file = new File(path);
			fop = new FileOutputStream(file);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			// get the content in bytes
			byte[] contentInBytes = content.getBytes();

			fop.write(contentInBytes);
			fop.flush();
			fop.close();

			System.out
					.println("\t写入成功！\n/*****************************finished*********************************/");

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fop != null) {
					fop.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 初始化连接配置文件
	 * */
	public final static void initConnectionConfig() {
		System.out
				.println("初始化连接配置文件");
		final String fileName = "default-connection.properties";
		FileOutputStream fop = null;
		File file;
		String content = "#由SAT4j自动创建,这个文件用于配置用户的数据库信息，配置好以下各键后SAT4j会在运行时自动创建数据库的连接\n\n#dbName:数据库名 \ndbName=\n\n#username:数据库的用户名\nusername=\n\n#password:数据库的密码\npassword=";
		String path;
		try {
			file = new File("");
			path = file.getCanonicalPath() + "\\src\\" + fileName;
			file = new File(path);
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			if (file.exists()) {
				// 判断内容是否为空，如若空则写入，不为空则不写
				if (file.length() == 0) {
					// OutputStreamWriter pw=null;
					// pw=new OutputStreamWriter(new
					// FileOutputStream(path),"UTF8");
					fop = new FileOutputStream(path);
					//System.out.println("检测出配置文件为空，需要初始化,length="+ file.length());
					// get the content in bytes
					byte[] contentInBytes = content.getBytes();
					fop.write(contentInBytes);
					fop.flush();
					fop.close();
					// pw.write(content);
					// pw.close();
					System.out.println("配置文件写入成功！");
				} else {
					//System.out.println("检测出配置文件不为空，不需初始化,length="+ file.length());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fop != null) {
					fop.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 初始化javabean配置文件
	 * */
	public final static void initBeansConfig() {
		System.out
				.println("初始化javabean配置文件");
		final String fileName = "javabean.properties";
		FileOutputStream fop = null;
		File file;
		String content = "#由SAT4j自动创建\n\n#path:实体类所在的包名,这里的书写格式比如：'com\\\\mycompany\\\\javabean',这个键的值可以为空\npath=\n\n#这个键用于声明哪一张表需要验证身份，该键键值格式为：表名，列名1，列名2\nCheckTable=";
		String path;
		try {
			file = new File("");
			path = file.getCanonicalPath() + "\\src\\" + fileName;
			file = new File(path);
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			if (file.exists()) {
				// 判断内容是否为空，如若空则写入，不为空则不写
				if (file.length() == 0) {
					// OutputStreamWriter pw=null;
					// pw=new OutputStreamWriter(new
					// FileOutputStream(path),"UTF8");
					fop = new FileOutputStream(path);
					//System.out.println("检测出配置文件为空，需要初始化,length="+ file.length());
					// get the content in bytes
					byte[] contentInBytes = content.getBytes();
					fop.write(contentInBytes);
					fop.flush();
					fop.close();
					// pw.write(content);
					// pw.close();
					System.out.println("配置文件写入成功！");
				} else {
					//System.out.println("检测出配置文件不为空，不需初始化,length="+ file.length());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fop != null) {
					fop.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 该方法用于生成无参数的构造方法和全参数的构造方法。
	 * <p>
	 * 首先获取表的结构，然后根据表的结构获取属性值和属性类型的Map集合，利用Map集合的entrySet方法将它存入
	 * Set集合,之后利用高级for循环遍历这个Set集合。在循环的过程中拼接构造方法的代码。
	 * </p>
	 * <p>
	 * 现来说明循环过程：此时getVaule方法获取的时数据库中原始的类型，例如:int(10),char(50)等等，我们不需要括号的部分，
	 * 所以必须处理掉括号部分，时char(50)变为char。没错，利用循环使每个最原始的类型变为中间数据，注意这里我强调这是中间
	 * 数据，处理后的数据为中间数据，例如：int,char,varchar,text等等。这里是不是比较眼熟，没错它们正是类<tt>ConstsManager</tt>
	 * 中方法sqltypeToJava的参数！
	 * </p>
	 * <p>
	 * 现来简单说明方法sqltypeToJava的作用，详细说明请另请参见类<tt>ConstsManager</tt>。
	 * 该方法目的是将中间数据，也就是mysql中的数据类型转换成java中的数据类型。
	 * </p>
	 * <p>
	 * 利用循环拼接出参数列表。
	 * 参数列表：type1 param1,type2 param2,...,typen paramn
	 * 但是循环的结果会在末尾多一个逗号出来，这个一会要处理掉。
	 * 利用循环拼接出构造方法的身体部分的代码。
	 * 身体部分的代码形式如下：<br>
	 * this.param1=param1;
	 * this.param2=param2;
	 * ...
	 * this.paramm=paramn;
	 * </p>
	 * <p>
	 * 现在来处理掉末尾的逗号：获取拼接后原始的参数列表的长度，获取原始 的参数列表的子字符串从头到最后字符的前一个
	 * 即可得到处理后的参数列表。<br>
	 * 最后将参数列表部分和身体部分整合成构造方法的代码。
	 * </p>
	 * 
	 * @param dbName 数据库名称
	 * @param tabName 表名
	 * @see ConstsManager
	 * */
	private final static String constructor(String dbName,String tabName){
		String paramList="";
		String code="";
		String constructText="";
		String text="\tpublic "+Core.captureName(tabName)+"(){\n\t\tsuper();\n\t}\n";
		final  String NOTE1= "\t/*	无参数的构造方法	*/\n";
		final  String NOTE2= "\t/*	包含全部参数的构造方法	*/\n";
		//Map<String,String> ftmap=new HashMap<String,String>();
		//desc [dbName.tabName];输出设计视图的集合
		//返回一个表的结构，封装在ResultSet集合中
		ResultSet rs=Core.getDesign(dbName, tabName);
		Map<String,String> ftmap=Core.getFieldAndTypeMap(rs);
		Set<Map.Entry<String, String>> entryKey=ftmap.entrySet();
		for (Map.Entry<String, String> me : entryKey) {
			String v=me.getValue()+'(';
			int end = v.indexOf('(');
			String sqltype=v.substring(0, end);
			paramList+=(ConstsManager.sqltypeToJava(sqltype)+" "+me.getKey()+",");
			code+=("\t\tthis."+me.getKey()+"="+me.getKey()+";\n");
		}
		paramList=paramList.substring(0, paramList.length()-1);
		constructText="\tpublic "+Core.captureName(tabName)+"("+paramList+"){\n\t\tsuper();\n"+code+"\t}";
		constructText=NOTE1+text+"\n"+NOTE2+constructText;
		//System.out.println(constructText);
		return constructText;
	}

}
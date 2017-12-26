/*
 * @(#)Sub.java	1.0 17/04/16
 *
 * Copyright 2017 Gladiateur, Inc. All rights reserved.
 */

package com.sat4j.test;

//import java.util.List;
//import com.sat4j.core.Core;
import com.sat4j.user.FrameRoot;


/**
 * 这是这个测试案例的实例 这个类告诉程序员如何使用SAT4j, 这个类继承类<class>FrameRoot</class>
 * 类<class>FrameRoot</class>封装了SAT4j连接数据库的方法 <method>getConnection
 * </method> 
 * 使用时用户创建自己类的对象然后调用即可，例如用户自己创建的类的类名是：MyTest.java,那么以下举例： <example> Connection
 * conn=new MyTest().getConnection(); </example>获取一个数据库连接的对象
 * 
 * @author Gladiateur
 * @version v1.0 17/5/31
 * 
 * */

/*
 * 实例，用户可模仿或复制 说明：配置文件default-connection.properties由用户创建 位置在src下
 * 
 * 该配置文件的常量为：dbName,username,passsword 常量名不能改，常量值由用户决定
 * 
 * dbName= username=root password=123456
 * 
 * javabean.properties配置文件配置信息如下： 
 * 	dbName=manage
 * 	path=com\\user
 * 
 * v1.4.0 后的版本都可以自动生成配置文件，只要继承了FrameRoot不需写任何方法就会自动创建配置文件
 */

/** v1.6.7.25 之后的版本调用autoBeans()方法不需再创建本类的对象，可以以静态的方式直接调用 */
public class Sub extends FrameRoot {
	
	//private static User u=new User();
	
	public static void main(String[] args) {	
		//init();
		//autoBeans();
//		u.setId(110);
//		u.setName("Afsdfs");
//		u.setSex("男asdfsadf");
//		u.setAddress("sdfsa");
//		u.setPhone("11522552266");
//		u.setPwd("111111");
//		User u=null;
//		insert(u);
	
//		System.out.println(	check(200, "666666"));
		
		//List<Map<String, Object>> list=search("user");
//		List<Object> list=searchTableColValue("user", "name");
//		for (Object object : list) {
//			System.out.println(object);
//		}
		
		//System.out.println(Core.delete("book", "id='666'"));
		//System.out.println(Core.update("book", "price='500'", "id='10'"));
//		List<Map<String, Object>> list=searchByCondition("book", "id='10'");
//		for (Map<String, Object> map : list) {
//			System.out.println(map.get("id")+"\t"+map.get("author")+"\t"+map.get("name")+"\t"+map.get("price"));
//		}
////		System.out.println(s.delete("delete from student where stuId='10002';"));
////		System.out.println(s.select("select * from student;"));
	}
}

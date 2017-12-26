/*
 * @(#)TableManager.java	1.0 17/04/30
 *
 * Copyright 2017 Gladiateur, Inc. All rights reserved.
 */

package com.sat4j.core;

/**
 * 该接口定义了管理表的若干方法
 * 
 * @author Gladiateur
 * @version 1.0 17/4/30
 * */
public interface TableManager {
	
	public void insert(String sql);
	
	public int delete(String sql);
	
	public void update(String sql);
	
	public Object select(String sql);
	
}

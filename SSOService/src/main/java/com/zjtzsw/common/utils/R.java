package com.zjtzsw.common.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;

/**
 * 返回数据
 * 
 * @author hhj
 * @date 2017年9月16日 上午9:36:37
 */
public class R extends HashMap<String, Object> {
	private static final long serialVersionUID = 1L;
	
	public R(boolean success) {
		put("code", 0);
		put("msg", "");
		put("success", success);
	}
	
	public static R error() {
		return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
	}
	
	public static R error(String msg) {
		return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
	}
	
	public static R error(int code, String msg) {
		R r = new R(false);
		r.put("code", code);
		r.put("msg", msg);
		return r;
	}

	public static R ok(String msg) {
		R r = new R(true);
		r.put("msg", msg);
		return r;
	}
	
	public static R ok(Map<String, Object> map) {
		R r = new R(true);
		r.putAll(map);
		return r;
	}
	
	public static R ok() {
		return new R(true);
	}
	
	public R setData(Object data){
		this.put("data", data);
		return this;
	}

	public R put(String key, Object value) {
		super.put(key, value);
		return this;
	}
}

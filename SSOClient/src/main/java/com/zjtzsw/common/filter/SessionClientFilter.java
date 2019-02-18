package com.zjtzsw.common.filter;

import io.jsonwebtoken.Claims;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;
import com.opslab.util.jwt.JKSUtil;
import com.opslab.util.jwt.JwtUtil;
import com.zjtzsw.common.constant.Constant;
import com.zjtzsw.common.utils.JedisUtil;
import com.zjtzsw.common.utils.SSOClientUtil;
import com.zjtzsw.common.xss.XssHttpServletRequestWrapper;
import com.zjtzsw.config.RedisService;
import com.zjtzsw.modules.sys.domain.UserInfo;


/**
 * 检查用户的cookie,并校验有效性，无效则跳转SSOServiceURL
 * @author wac
 * @date 2018年7月23日
 */
@PropertySource(value = {"classpath:sso.properties"},encoding="utf-8")
@ConfigurationProperties(prefix = "sso")
public class SessionClientFilter implements Filter {
	private static final String COOKIE_TOKEN_PREFIX = Constant.COOKIE_TOKEN_PREFIX;

	private static String url ;
	private static List<Pattern>  patterns;

	@Autowired
	Environment environment;

	@Autowired
	JedisUtil redisService;

	@Autowired
	RestTemplate restTemplate;
	
	
	@Override
	public void init(FilterConfig config) throws ServletException {
		System.out.println("SessionFilter Initing!");
		System.out.println("InitConfig's url : " + config.getInitParameter("ssourl"));
		

		//		String[] patternStr = config.getInitParameter("passUrl").split(";");
		//		
		//		for(String str : patternStr){
		//			patterns.add(Pattern.compile(str));
		//		}
		//		
	}
	
	public static boolean TokenCheck(String token){
		try {
			String privateKey = JKSUtil.getPrivateStrByJks("wac");
			Claims claims = JwtUtil.parseJWT(token, "secretkey");
			System.out.println("claims : " + claims);
			String id = claims.getId();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
			
		}//这里是加密解密的key。
	}
	
	public boolean redisCheck(String token){
		try {
			String privateKey = JKSUtil.getPrivateStrByJks("wac");
			Claims claims = JwtUtil.parseJWT(token, "secretkey");
			System.out.println("claims : " + claims);
			String id = claims.getId();
			
			//
			
			Object obj = redisService.get(token);
			
			System.out.println("redis_token_get : " + obj);
			
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
			
		}//这里是加密解密的key。
	}

    public static void main(String[] args) throws Exception {
        String sso_token_check_url = "http://www.sso.com:8081/sso/checkToken";
        String token ="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJTU08iLCJpc3MiOiJ3YWMiLCJuYW1lIjoid2FjIiwiZXhwIjoxNTUwMzM4NjQwfQ.37tzuEf4ieuRzGor0zVSPjBg0_ZC0imtepLsYOYZ5BU";

        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<>();
        requestEntity.add("token", token);
        requestEntity.add("logoutUrl", "http://www.aaa.com:8080/sso/logout");

        try {
            //
            String res = new RestTemplate().postForObject(sso_token_check_url, requestEntity, String.class);
            System.out.println("token校验结果：" + res);
            JSONObject result = JSONObject.parseObject(res);

            System.out.println("调用URL");
            if("00".equals(result.getString("code"))){
                // 校验token成功，注册成功
                System.out.println(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(false);

        }//这里是加密解密的key。
    }
	
	//获取到SSO去检验token的地址，并注册到SSO
	public boolean SSOTokenCheck(HttpServletRequest request,String token) throws IOException{
		String sso_token_check_url = SSOClientUtil.getSSOTokenCheckUrl(request, token);
		String client_logout_url = SSOClientUtil.getClientLogOutUrl();

        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<>();
        requestEntity.add("token", token);
        requestEntity.add("logoutUrl", client_logout_url);

        try {
            //
            String res = new RestTemplate().postForObject(sso_token_check_url, requestEntity, String.class);
            System.out.println("token校验结果：" + res);
			JSONObject result = JSONObject.parseObject(res);
			
			System.out.println("调用URL");
			if("01".equals(result.getString("code"))){
				// 校验token成功，注册成功
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
			
		}//这里是加密解密的key。
		return false;
	}
	
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @param chain
	 * @throws Exception 
	 */
	private void checkJWTToken(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws Exception {
		HttpSession hs = request.getSession();
		Enumeration<String> es = hs.getAttributeNames();
		while(es.hasMoreElements()){
			String key = es.nextElement();
			System.out.println("key : " + key + " , value : " + hs.getAttribute(key));
		}

		Boolean isLogin = (Boolean) hs.getAttribute("isLogin");
		if (isLogin != null && isLogin) {
			// 局部会话放行
			System.out.println("局部session有效，放行！");
			chain.doFilter(request, response);
			return;
		}
		System.out.println("局部session无效");
		String token = request.getParameter("token");
		System.out.println("Token : " +  token);

		if(token!=null&&!"".equals(token)){//连接是带token的
			
			//使用不同的校验方法来校验token
			if(TokenCheck(token)){
				//验证token里带的id和用户保存的loginId一致，token有效，增加session中isLogin=true
				hs.setAttribute("isLogin", true);
				//放行该次的请求
                chain.doFilter(request, response);
                return;
			}

		}
		//没有token,统一跳转登录中心
		SSOClientUtil.redirectToSSOURL(request, response);
		System.out.println("URL2 :   " + url + "?callbackurl=" + request.getRequestURL());

	}

	/**
	 *
	 * @param request
	 * @param response
	 * @param chain
	 * @throws Exception
	 */
	private void check(HttpServletRequest request, HttpServletResponse response, FilterChain chain)throws Exception {
		HttpSession hs = request.getSession();
		Enumeration<String> es = hs.getAttributeNames();
		while(es.hasMoreElements()){
			String key = es.nextElement();
			System.out.println("key : " + key + " , value : " + hs.getAttribute(key));
		}

		Boolean isLogin = (Boolean) hs.getAttribute("isLogin");
		if (isLogin != null && isLogin) {
			// 局部会话放行
			System.out.println("局部session有效，放行！");
			chain.doFilter(request, response);
			return;
		}
		System.out.println("局部session无效");

        String uri = request.getRequestURI();
        System.out.println("URI :" + uri);
        StringBuffer url = request.getRequestURL();
        System.out.println("url :" + url.toString());


		String token = request.getParameter("token");
		System.out.println("Token : " +  token);

		if(token!=null&&!"".equals(token)){//连接是带token的
			
			//使用不同的校验方法来校验token
			if(SSOTokenCheck(request,token)){
				// 校验成功，添加局部session标志
				//验证token里带的id和用户保存的loginId一致，token有效，增加session中isLogin=true
				hs.setAttribute("isLogin", true);
				hs.setAttribute("token", token);
				//放行该次的请求
                chain.doFilter(request, response);
                return;
			}

		}
		//没有token,统一跳转登录中心
		System.out.println("跳转SSO单点登录站点>>>>>>>>>>>>");
		SSOClientUtil.redirectToSSOURL(request, response);
	}
	
	/**
	 *  登录用户体系，用JSONP模式设置浏览器cookie
	 * @param request
	 * @param response
	 * @param chain
	 * @throws IOException
	 * @throws ServletException
	 */
	private void JSONPCheck(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpSession hs = request.getSession();
		System.out.println("Session_Id : " + hs.getId());
		Enumeration<String> es = hs.getAttributeNames();
		String[] names = hs.getValueNames();
		for(int i =0;i<names.length;i++){
			System.out.println(names[i]);
		}
		while(es.hasMoreElements()){
			String key = es.nextElement();
			System.out.println("key : " + key + " , value : " + hs.getAttribute(key));
		}
		System.out.println("RSession_Token : " + hs.getAttribute("token"));
		System.out.println("RSession_isLogin : " + hs.getAttribute("isLogin"));
		String isLogin = (String) hs.getAttribute("isLogin");
		if(isLogin!=null&&!"".equals(isLogin)){
			//局部会话放行
			System.out.println("部会话放行!");
			chain.doFilter(request, response);
			return;
		}
		
		//没有token,检查cookie里是否有有效token
		Cookie tc = getCookieByName(request,"token");
		String token = tc==null?"":tc.getValue();
		if(token!=null&&!"".equals(token)){//校验浏览器cookie里的token,有效则生成局部session
			System.out.println("cookie认证成功！");
			hs.setAttribute("token", token);
			hs.setAttribute("isLogin", "true");
			
			System.out.println("Session_Token : " + hs.getAttribute("token"));
			chain.doFilter(request, response);
			return;
		}
		
		System.out.println("局部session无效");
		System.out.println("请求URL : " + request.getRequestURL());
		request.setAttribute("callbackurl", request.getRequestURL());
//		request.getRequestDispatcher("/home/index").forward(request, response);
		response.sendRedirect(url + "?callbackurl=" + request.getRequestURL());	
	}

	private String getURLWithOutToken(HttpServletRequest request){
		String uri = request.getRequestURI();
		StringBuffer url = request.getRequestURL();
		url.append("?");
		Map<String, String[]> param = request.getParameterMap();
		
		param.remove("token");
		
		Set<String> keys= param.keySet();
		
		for(String key : keys){
			System.out.println(request.getParameter(key));
			
			url.append("&"+key+ "=" + request.getParameter(key));
		}
		
		System.out.println("拼好得URL : " + url.toString());
		
		
		return url.toString();
		
		
		
	}
	
	private UserInfo getCachedUser(String token){
		Object obj =  redisService.get(token);
		System.out.println(obj);
		if(obj==null) return null;
		return (UserInfo)obj;
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
//		check((HttpServletRequest)request,(HttpServletResponse)response,chain);
		try {
			System.out.println("SessionClientFilter working!");
			check((HttpServletRequest)request,(HttpServletResponse)response,chain);
			chain.doFilter(request, response);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String getUrl() {
		return url;
	}

	public static void setUrl(String url) {
		System.out.println("设置SSOFilter_URL:" + url);
		SessionClientFilter.url = url;
	}
	
	/**
	 * 根据名字获取cookie
	 * 
	 * @param request
	 * @param name
	 *            cookie名字
	 * @return
	 */
	public static Cookie getCookieByName(HttpServletRequest request, String name) {
	    Map<String, Cookie> cookieMap = ReadCookieMap(request);
	    if (cookieMap.containsKey(name)) {
	        Cookie cookie = (Cookie) cookieMap.get(name);
	        return cookie;
	    } else {
	        return null;
	    }
	}

	/**
	 * 将cookie封装到Map里面
	 * 
	 * @param request
	 * @return
	 */
	private static Map<String, Cookie> ReadCookieMap(HttpServletRequest request) {
	    Map<String, Cookie> cookieMap = new HashMap<String, Cookie>();
	    Cookie[] cookies = request.getCookies();
	    if (null != cookies) {
	        for (Cookie cookie : cookies) {
	            cookieMap.put(cookie.getName(), cookie);
	        }
	    }
	    return cookieMap;
	}

}
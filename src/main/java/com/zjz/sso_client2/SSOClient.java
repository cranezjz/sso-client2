package com.zjz.sso_client2;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.impl.TextCodec;

public class SSOClient {
	/**
	 * 测试开关，当开启测试模式时秘钥为默认值，不从redis取，也不缓存。
	 */
	public static boolean isTest=true;
	public static String testSecretKey="123456789abc";
	private static String clientSysId="";
	private static String domain="";
	
	private static Map<String,SecretKeyEntity> secretKeyMap = null;
	//客户端缓存秘钥的时间 （单位为秒），该值建议送 300（5分钟），
	private static long clientSecretExpire = 300;
	private SSOClient() {}
	
	/**
	 * 
	 * @param clusterAddress  
	 * @param clientSecretExpire 客户端缓存秘钥的时间 单位是秒
	 * @param serverSecretExpire 服务端保存秘钥的时间 单位是秒
	 * @param clientSysId
	 * @param domain
	 */
	public static void init(String clusterAddress,String clientSysId,String domain,int clientSecretExpire,int serverSecretExpire,boolean isShowLog) {
		JedisUtil.init(clusterAddress, serverSecretExpire);
		SSOClient.clientSecretExpire=((long)clientSecretExpire)*1000L;
		SSOClient.clientSysId = clientSysId;
		SSOClient.domain = domain;
		Log.show = isShowLog;
		if(isTest) {
			return;
		}
		secretKeyMap = new ConcurrentHashMap<String,SecretKeyEntity>();
		new Thread(new StorageManage(secretKeyMap)).start();
	}
	/**
	 * 
	 * @param clusterAddress 多个ip端口间“，”分割 192.168.75.130:6381,192.168.75.130:6382,192.168.75.130:6383,192.168.75.130:6384,192.168.75.130:6385,192.168.75.130:6386
	 * @param clientSysId
	 * @param domain
	 */
	public static void init(String clusterAddress,String clientSysId,String domain) {
		init(clusterAddress,clientSysId,domain,300,3600,true);
	}
	/**
	 * 
	 * @param username
	 * @param password
	 * @param remark
	 * @return
	 */
	public static String createTokenAndWriteCookie(String username,String remark,HttpServletResponse response) {
    	String secretKey = JedisUtil.getSecretKey(username);
    	if(secretKey==null) {
    		secretKey = String.valueOf(System.currentTimeMillis());
    		JedisUtil.saveSecretKey(username, secretKey);
	    }
    	if(!isTest) {
    		SecretKeyEntity ske = new SecretKeyEntity(secretKey,clientSecretExpire);
    		secretKeyMap.put(username, ske);
    	}
		String token = JwtHelper.createJWT(username,"1",remark,clientSysId,secretKey);
	    //保存token到cookie中
        Cookie cookie = new Cookie("g_token", token);
		cookie.setPath("/");
		cookie.setDomain(domain);
		response.addCookie(cookie);
		return token;
	}
	/**
	 * 验证token的有效性，每次一个客户端秘钥缓存周期跟新一次服务端秘钥的有效期。
	 * @param ssoLoginAddress
	 * @param token
	 * @return
	 */
	public static boolean verify(String token) {
		if(token==null || "".equals(token)) {
			return false;
		}
		String secretKey = getSecretKey(token);
		return JwtHelper.verify(token, secretKey);
	}
	/**
	 * 不签退服务端的秘钥，清理用户浏览器中的token
	 * @param token
	 * @return
	 */
	public static void logout(HttpServletRequest request,HttpServletResponse response) {
		String token = getToken(request);
		if(!verify(token)) {
			return;
		}
		if(!isTest) {
			TokenBody tokenbody = getTokenBody(token);
			secretKeyMap.remove(tokenbody.getUsername());
		}
		//保存token到cookie中
        Cookie cookie = new Cookie("g_token", "");
		cookie.setPath("/");
		cookie.setDomain(domain);
		response.addCookie(cookie);
	}
	/**
	 * 
	 * @param request
	 * @return
	 */
	public static String getToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();  
        if (null==cookies) {  
            return null;  
        } else {  
        	for(Cookie cookie : cookies){  
        		if("g_token".equals(cookie.getName())) {
        			return cookie.getValue();
        		}
            } 
        }
		return null;
	}

	/**
	 * 签退清除服务端的秘钥，将签退所有系统的登录状态
	 * @param token
	 * @param response
	 */
	public static void logoutAll(HttpServletRequest request,HttpServletResponse response) {
		logout(request,response);
		//清理服务器端的秘钥.
		JedisUtil.removeKey(getTokenBody(getToken(request)).getUsername());
	}
	
	private static String getSecretKey(String token) {
		if(isTest) {
			return testSecretKey;
		}
		String username=null;
		TokenBody tokenBody=getTokenBody(token);
		if(tokenBody==null) {
			return null;
		}
        //System.out.println(tokenBody);
	    username = tokenBody.getUsername();
	    Log.print("从token中解析出的username为:"+username);
	    SecretKeyEntity secretKeyEntity = secretKeyMap.get(username);
	    if(secretKeyEntity == null) {//如果本地缓存已被清理则从服务器取秘钥并保存到本地缓存
	    	Log.print("从本地缓存取秘钥失败");
	    	String secretKey = JedisUtil.getSecretKey(username);
	    	Log.print("从redis中取到的秘钥为"+secretKey);
	    	if(secretKey==null) {
	    		return secretKey;
	    	}
	    	secretKeyMap.put(username, new SecretKeyEntity(secretKey,clientSecretExpire));
	    	Log.print("秘钥放到本地缓存中");
	    	return secretKey;
	    }else {
	    	Log.print("从本地缓存取到的秘钥为:"+secretKeyEntity.toString());
	    	if(System.currentTimeMillis()>secretKeyEntity.getExpire()) {
	    		Log.print("从本地缓存秘钥失效");
	    		String secretKey = JedisUtil.getSecretKey(username);
	    		Log.print("从redis中取秘钥："+secretKey+"并保存到本地缓存");
	    		secretKeyMap.put(username, new SecretKeyEntity(secretKey,clientSecretExpire));
	    		return secretKey;
	    	}else {
	    		Log.print("从本地缓存秘钥有效,离失效时间还有："+(secretKeyEntity.getExpire()-System.currentTimeMillis())+"毫秒");
	    		return secretKeyEntity.getKey();
	    	}
	    }
	}
	/**
	 * 
	 * @param token
	 * @return
	 */
	public static TokenBody getTokenBody(String token) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			String bodyinfo = new String(TextCodec.BASE64URL.decode(token.split("\\.")[1]),"utf-8");
			return mapper.readValue(bodyinfo, TokenBody.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}

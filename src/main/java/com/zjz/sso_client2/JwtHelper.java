package com.zjz.sso_client2;

import java.security.Key;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;

public class JwtHelper {
	//有效期一年
	private static long TTLMillis = 365*24*3600;
	public static void init(long tokenExpire) {
		TTLMillis = tokenExpire;
	}
	public static Claims parseJWT(String jsonWebToken, String base64Security) {
		try {
			Claims claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(base64Security))
					.parseClaimsJws(jsonWebToken).getBody();
			return claims;
		}catch(Exception e){
			return null;
		}
	}
	public static boolean verify(String jsonWebToken, String base64Security) {
		Claims claims = parseJWT(jsonWebToken, base64Security);
		if(claims == null) {
			return false;
		}
		return true;
	}
	/**
	 *  iss: 该JWT的签发者，是否使用是可选的；
		sub: 该JWT所面向的用户，是否使用是可选的；
		aud: 接收该JWT的一方，是否使用是可选的；
		exp(expires): 什么时候过期，这里是一个Unix时间戳，是否使用是可选的；
		iat(issued at): 在什么时候签发的(UNIX时间)，是否使用是可选的；
		其他还有：
		nbf (Not Before)：如果当前时间在nbf里的时间之前，则Token不被接受；一般都会留一些余地，比如几分钟；，是否使用是可选的
	 * @param name
	 * @param userId
	 * @param remark
	 * @param audience
	 * @param issuer
	 * @param TTLMillis
	 * @param base64Security
	 * @return
	 */
	public static String createJWT(String name, String userId, String remark, String audience, String issuer,
			long TTLMillis, String base64Security) {
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);
		// 生成签名密钥
		byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(base64Security);
		Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
		// 添加构成JWT的参数
		JwtBuilder builder = Jwts.builder().setHeaderParam("typ", "JWT")
				.claim("username", name).claim("userid", userId).claim("remark", remark)
				.setIssuer(issuer).setAudience(audience)
				.signWith(signatureAlgorithm, signingKey);
		// 添加Token过期时间
		if (TTLMillis >= 0) {
			long expMillis = nowMillis + TTLMillis;
			Date exp = new Date(expMillis);
			builder.setExpiration(exp).setNotBefore(now);
		}
		// 生成JWT
		return builder.compact();
	}
/*	public static String createJWT(String name, String userId,String secretKey) {
		return createJWT(name, userId, "", "healthClient", "XHYJ",TTLMillis, secretKey);
	}*/
	public static String createJWT(String name, String userId,String remark,String sysId,String secretKey) {
		return createJWT(name, userId, remark, sysId, "XHYJ",TTLMillis, secretKey);
	}
	public static void main(String[] args) {
		JwtHelper.init(365*24*3600);
		String token = createJWT("zjz","0001","1","xhyjClient","xhyjServer",10*1000,"123123123");
		System.out.println(token);
		System.out.println(parseJWT(token,"123123123"));
		
		try {
			System.out.println(new String(TextCodec.BASE64URL.decode(token.split("\\.")[0]),"utf-8"));
			System.out.println(new String(TextCodec.BASE64URL.decode(token.split("\\.")[1]),"utf-8"));
			System.out.println(new String(TextCodec.BASE64URL.decode(token),"utf-8"));
			Thread.sleep(3*1000);
			System.out.println(parseJWT(token,"123123123"));
			
			String token2 = createJWT("zjz","0002","1","xhyjClient","xhyjServer",10*1000,"123123123");
			String token2Body=token2.split("\\.")[1];
			String tokenHead=token.split("\\.")[0];
			String tokenSign=token.split("\\.")[2];
			String newToken = tokenHead+"."+token2Body+"."+tokenSign;
			System.out.println("=================");
			System.out.println("token:"+token);
			System.out.println("toke2:"+newToken);
			System.out.println("=================");
			System.out.println("新token解析结果："+parseJWT(newToken,"123123123"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
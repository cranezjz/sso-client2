package com.zjz.sso_client2;

import java.util.HashSet;
import java.util.Set;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

public class JedisUtil {
	private static JedisCluster jedisCluster = null;
	private static int expire = 0;//单位是秒
	private static String address="192.168.75.130:6381,192.168.75.130:6382,192.168.75.130:6383,192.168.75.130:6384,192.168.75.130:6385,192.168.75.130:6386";
	/**
	 * 
	 * @param clusterAddress
	 * @param secretKeyExpire 单位是秒建议
	 */
	public static void init(String clusterAddress,int secretKeyExpire) {
		address = clusterAddress;
		expire = secretKeyExpire;
		String[] serverArray = address.split(",");
        Set<HostAndPort> nodes = new HashSet<HostAndPort>();
        for (String ipPort : serverArray) {
            String[] ipPortPair = ipPort.split(":");
            nodes.add(new HostAndPort(ipPortPair[0].trim(), Integer.valueOf(ipPortPair[1].trim())));
        }
        jedisCluster = new JedisCluster(nodes);
	}
	/**
	 * 
	 * @param username
	 * @param secretKey
	 */
	public static void saveSecretKey(String username,String secretKey) {
		jedisCluster.setex(username, expire, secretKey);
	}
	/**
	 * 
	 * @param username
	 * @return
	 */
	public static String getSecretKey(String username) {
		String secretKey = jedisCluster.get(username);
		if(secretKey != null) {
			jedisCluster.setex(username, expire, secretKey);
			Log.print("修改redis中【"+username+"】的有效期："+expire);
		}
		return secretKey;
	}
	public static void main(String[] args) {
		String clusterAddress="192.168.75.130:6381,192.168.75.130:6382,192.168.75.130:6383,192.168.75.130:6384,192.168.75.130:6385,192.168.75.130:6386";
		init(clusterAddress,2);
		saveSecretKey("username","123123");
		System.out.println(getSecretKey("username"));
        try {
			Thread.sleep(3*1000);
			System.out.println(getSecretKey("username"));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public static void removeKey(String username) {
		jedisCluster.del(username);
	}
}

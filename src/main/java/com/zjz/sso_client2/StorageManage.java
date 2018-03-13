package com.zjz.sso_client2;

import java.util.Iterator;
import java.util.Map;

/**
 * 自动清理共享内存
 * 每小时清理一次过期的秘钥
 * @author zhaojz
 */
class StorageManage implements Runnable{
	Map<String,SecretKeyEntity> secretKeyMap = null;
	public StorageManage(Map<String,SecretKeyEntity> secretKeyMap) {
		this.secretKeyMap = secretKeyMap;
	}
	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(60*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(secretKeyMap.size()>0) {
				long nowTime = System.currentTimeMillis();
				Iterator<String> it = secretKeyMap.keySet().iterator();
				while(it.hasNext()) {
					String key = it.next();
					SecretKeyEntity ske = secretKeyMap.get(key);
					if(ske.getExpire()<nowTime) {
						secretKeyMap.remove(key);
					}
				}
			}
		}
	}
}
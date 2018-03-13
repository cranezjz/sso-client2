package com.zjz.sso_client2;

public class SecretKeyEntity {
		private String key;
		private long expire;		//可以定时清理
/*		public SecretKeyEntity(String key){
			this.key = key;
			this.expire = System.currentTimeMillis()+ 1000*60*30;//默认秘钥保存30分钟。
		}*/
		/**
		 * 
		 * @param key
		 * @param expire 单位是毫秒
		 */
		public SecretKeyEntity(String key,long expire){
			this.key = key;
			this.expire = System.currentTimeMillis()+ expire;
		}
		
		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
		public long getExpire() {
			return expire;
		}
		public void setExpire(long expire) {
			this.expire = expire;
		}
		@Override
		public String toString() {
			return "SecretKeyEntity [key=" + key + ", expire=" + expire + "]";
		}
		
}

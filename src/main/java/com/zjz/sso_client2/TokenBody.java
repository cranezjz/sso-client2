package com.zjz.sso_client2;
/**
 * {"username":"zjz","userid":"0001","remark":"1","iss":"xhyjServer","aud":"xhyjClient","exp":1520492562,"nbf":1520492552}
 * @author zhaojz
 *
 */
public class TokenBody {
	private String username;
	private String userid;
	private String remark;
	private String iss;
	private String aud;
	private long exp;
	private long nbf;
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getIss() {
		return iss;
	}
	public void setIss(String iss) {
		this.iss = iss;
	}
	public String getAud() {
		return aud;
	}
	public void setAud(String aud) {
		this.aud = aud;
	}
	public long getExp() {
		return exp;
	}
	public void setExp(long exp) {
		this.exp = exp;
	}
	public long getNbf() {
		return nbf;
	}
	public void setNbf(long nbf) {
		this.nbf = nbf;
	}
	@Override
	public String toString() {
		return "TokenBody [username=" + username + ", userid=" + userid + ", remark=" + remark + ", iss=" + iss
				+ ", aud=" + aud + ", exp=" + exp + ", nbf=" + nbf + "]";
	}
	
	
}

package cn.aresoft.longshu.web.controller.cust;

import java.security.MessageDigest;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cn.aresoft.longshu.service.LsWeiXinService;
import cn.aresoft.longshu.util.HttpUtil;
import cn.aresoft.portal.controller.BaseController;

import com.puff.framework.annotation.Controller;
import com.puff.framework.annotation.Inject;
import com.puff.framework.utils.StringUtil;
import com.puff.jdbc.core.Record;
import com.puff.log.Log;
import com.puff.log.LogFactory;
import com.puff.web.mvc.PuffContext;
import com.puff.web.view.View;
import com.puff.web.view.ViewFactory;

/**
 * 
 *配置微信分享参数
 * 
 */
@Controller(value = "/wxconfig")
//@InterceptorChain(@Before(value = WeiXinUrlInterceptor.class, type = FilterType.EXCLUDE, method = {""}))
public class WxConfigJsController extends BaseController {
	@Inject
	private LsWeiXinService lsweixinservice;
	private static final Log log = LogFactory.get();
	/**
	 * 配置微信分享参数
	 * @return
	 */
	public View wxConfigJs() {
		log.info("WxConfigJsController wxconfig wxConfigJs start");
		String url=PuffContext.getParameter("url");
		log.info("WxConfigJsController wxconfig wxConfigJs geturl  url="+url);
		String nonce_str = create_nonce_str();
		String timestamp = create_timestamp();
		String appid = HttpUtil.getWeixinPropertyValue("mpappid");
	    //String secret = HttpUtil.getWeixinPropertyValue("secret");
	    //获取access_token值
	    Record r=lsweixinservice.gettoken();
	    //String access_token="";
	    String ticket="";
	    if(r!=null){
	    	ticket=r.getString("jsapiticket");
	    }
		String string1 = "jsapi_ticket=" + ticket + "&noncestr=" + nonce_str + "&timestamp="
				+ timestamp + "&url=" + url;
		log.info("string1111==============="+string1);
		String signature = "";
		try {
			MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			crypt.reset();
			crypt.update(string1.getBytes("UTF-8"));
			signature = byteToHex(crypt.digest());

		} catch (Exception e) {
			e.printStackTrace();
		}
		String unionid=geturluuid(url);;//获取url中的unionid
		String userid=geturluserid(url);
		//判断该用户是否是业务员，如果是业务员，重新获取userid拼接到url中，
		//如果不是业务人员，判断链接中是否含有业务人员userid，如果包含，则使用链接中的userid,如果不包含userid则默认为admin用户
		if(StringUtil.notEmpty(unionid)){
			userid=lsweixinservice.getuserid(unionid);
		}
		if(StringUtil.notEmpty(url)&&url.contains("?")){
			url=url.split("[?]")[0];
			url=url+"?userid="+userid;
		}
		log.info("WxConfigJsController wxconfig wxConfigJs 入参   url="+url+",unionid="+unionid+",userid="+userid);
		Map<String,String> result = new HashMap<String,String>();  
	    result.put("appid", appid);  
	    result.put("timestamp", timestamp);  
	    result.put("nonceStr", nonce_str);  
	    result.put("signature", signature);  
	    result.put("url", url); 
	    result.put("userid", userid);  
	    log.info("WxConfigJsController wxconfig wxConfigJs retrun param  "
	    		+ "appid="+appid+",timestamp="+timestamp+",nonceStr="+nonce_str+",signature="+signature+",url="+url+",userid="+userid);
		return ViewFactory.json(result);
	}
	private static String byteToHex(final byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		String result = formatter.toString();
		formatter.close();
		return result;
	}

	private static String create_nonce_str() {
		return UUID.randomUUID().toString();
	}

	private static String create_timestamp() {
		return Long.toString(System.currentTimeMillis() / 1000);
	}
	//通过链接获取userid,
	private static String geturluserid(String url) {
		String userid="";
		if(StringUtil.notEmpty(url)&&url.contains("?")){
			String[] arrSplit = url.split("[?]");
			if (arrSplit.length > 1) {
				if (arrSplit[1] != null) {
					String strAllParam = arrSplit[1];
					String[] paramSplit =strAllParam.split("[&]");;
					for (String s : paramSplit) {
						if(s.contains("userid")){
							String[] arrSplitEqual = null;
							arrSplitEqual = s.split("[=]");
							// 解析出键值
							if (arrSplitEqual.length > 1) {
								// 正确解析
								userid=arrSplitEqual[1];
							} else {
								if (arrSplitEqual[0] != "") {
									userid="admin";
								}
							}
						}
					}
				}
			}
		}
		return userid;
	}
	
	//通过链接获取unionid,
	private static String geturluuid(String url) {
		String unionid="";
		if(StringUtil.notEmpty(url)&&url.contains("?")){
			String[] arrSplit = url.split("[?]");
			if (arrSplit.length > 1) {
				if (arrSplit[1] != null) {
					String strAllParam = arrSplit[1];
					String[] paramSplit =strAllParam.split("[&]");;
					for (String s : paramSplit) {
						if(s.contains("unionid")){
							String[] arrSplitEqual = null;
							arrSplitEqual = s.split("[=]");
							// 解析出键值
							if (arrSplitEqual.length > 1) {
								// 正确解析
								unionid=arrSplitEqual[1];
								if(unionid.contains("#")){
									unionid=unionid.split("#")[0].toString();
								}
							} 
						}
					}
				}
			}
		}
		return unionid;
	}
}

package com.shtouyun.wechat.web;

import java.security.MessageDigest;
import java.util.Formatter;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.shtouyun.wechat.common.ResourcesConfigure;
import com.shtouyun.wechat.core.model.WechatSettingsDTO;
import com.shtouyun.wechat.service.WxConfigJsService;
import com.shtouyun.wechat.util.RequestHelper;

@Controller
public class WxConfigJsController {
	
	@Autowired
	private WxConfigJsService wxConfigJsService;

	@RequestMapping(value = "/wxconfig.js")
	@ResponseBody
	public String wxConfigJs(HttpServletRequest req, HttpServletResponse resp,
			@RequestParam(required = false) String appId, @RequestParam(required = false) Integer eseId,
			@RequestParam(required = false) String referer) {
		if (referer == null) {
			referer = req.getHeader("referer");
			if (referer == null)
				referer = ResourcesConfigure.getHostName() + "/wxconfig.js";
		}
		if (eseId == null && StringUtils.isBlank(appId)) {
			appId = ResourcesConfigure.touyunAppId;
		}
		// TODO
		// logger.info("come to request wxconfig.js with referer:{}", referer);
		// TODO 此处暂时都用透云的公众账号信息，不分企业
		WechatSettingsDTO wechatSetting = null;
		
		if(ResourcesConfigure.huiYuanAppid.equals(appId)) {
			wechatSetting = wxConfigJsService.getHuiYuanSetting(appId);
		} else {
			wechatSetting = RequestHelper.getWechatSettingsDTO(eseId, appId);
		}
		

		String nonce_str = create_nonce_str();
		String timestamp = create_timestamp();
		String string1 = "jsapi_ticket=" + wechatSetting.getJsTicket() + "&noncestr=" + nonce_str + "&timestamp="
				+ timestamp + "&url=" + referer;
		String signature = "";
		try {
			MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			crypt.reset();
			crypt.update(string1.getBytes("UTF-8"));
			signature = byteToHex(crypt.digest());

		} catch (Exception e) {
			e.printStackTrace();
		}
		StringBuilder sb = new StringBuilder();
		sb.append("wx.config({debug: false,appId:'").append(wechatSetting.getAppId()).append("',timestamp:")
				.append(timestamp).append(",nonceStr:'").append(nonce_str).append("',signature:'").append(signature)
				/* full api list */
				.append("',jsApiList:['checkJsApi','onMenuShareTimeline','onMenuShareAppMessage','onMenuShareQQ','onMenuShareWeibo','onMenuShareQZone',")
				.append("'hideMenuItems','showMenuItems','hideAllNonBaseMenuItem','showAllNonBaseMenuItem','translateVoice','startRecord','stopRecord','onVoiceRecordEnd',")
				.append("'playVoice','onVoicePlayEnd','pauseVoice','stopVoice','uploadVoice','downloadVoice','chooseImage','previewImage','uploadImage','downloadImage',")
				.append("'getNetworkType','openLocation','getLocation','hideOptionMenu','showOptionMenu','closeWindow','scanQRCode','chooseWXPay',")
				.append("'openProductSpecificView','addCard','chooseCard','openCard']})");

		// TODO
		return sb.toString();
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

}

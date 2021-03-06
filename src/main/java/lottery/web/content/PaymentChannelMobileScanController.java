package lottery.web.content;

import admin.domains.content.entity.AdminUser;
import admin.domains.jobs.AdminUserActionLogJob;
import admin.domains.jobs.AdminUserLogJob;
import admin.web.WUC;
import admin.web.WebJSONObject;
import admin.web.helper.AbstractActionController;
import javautils.http.HttpUtil;
import lottery.domains.content.biz.PaymentChannelQrCodeService;
import lottery.domains.content.biz.PaymentChannelService;
import lottery.domains.content.entity.PaymentChannelQrCode;
import lottery.domains.content.global.Global;
import lottery.domains.content.vo.payment.PaymentChannelVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
public class PaymentChannelMobileScanController extends AbstractActionController {

	@Autowired
	private AdminUserActionLogJob adminUserActionLogJob;

	@Autowired
	private AdminUserLogJob adminUserLogJob;
	
	@Autowired
	private PaymentChannelService paymentChannelService;
	@Autowired
	private PaymentChannelQrCodeService paymentChannelQrCodeService;
	
	@RequestMapping(value = WUC.LOTTERY_PAYMENT_CHANNEL_MOBILESCAN_LIST, method = { RequestMethod.POST })
	@ResponseBody
	public void LOTTERY_PAYMENT_CHANNEL_MOBILESCAN_LIST(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
		String actionKey = WUC.LOTTERY_PAYMENT_CHANNEL_MOBILESCAN_LIST;
		long t1 = System.currentTimeMillis();
		WebJSONObject json = new WebJSONObject(super.getAdminDataFactory());
		AdminUser uEntity = super.getCurrUser(session, request, response);
		if (uEntity != null) {
			if (super.hasAccess(uEntity, actionKey)) {
				List<PaymentChannelVO> list = paymentChannelService.listAllMobileScanVOs();
				json.set(0, "0-3");
				json.accumulate("data", list);
			} else {
				json.set(2, "2-4");
			}
		} else {
			json.set(2, "2-6");
		}
		long t2 = System.currentTimeMillis();
		if (uEntity != null) {
			adminUserActionLogJob.add(request, actionKey, uEntity, json, t2 - t1);
		}
		HttpUtil.write(response, json.toString(), HttpUtil.json);
	}
	
	@RequestMapping(value = WUC.LOTTERY_PAYMENT_CHANNEL_MOBILESCAN_GET, method = { RequestMethod.POST })
	@ResponseBody
	public void LOTTERY_PAYMENT_CHANNEL_MOBILESCAN_GET(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
		WebJSONObject json = new WebJSONObject(super.getAdminDataFactory());
		AdminUser uEntity = super.getCurrUser(session, request, response);
		if (uEntity != null) {
			int id = HttpUtil.getIntParameter(request, "id");
			PaymentChannelVO bean = paymentChannelService.getVOById(id);
			if (bean.getFixedQRAmount() == 1) {
				List<PaymentChannelQrCode> qrList=paymentChannelQrCodeService.getByChannelId(bean.getId());
				json.accumulate("qrList", qrList);
			}
			else {
				json.accumulate("qrList", new ArrayList<>());
			}
			json.accumulate("data", bean);
			json.set(0, "0-3");
		} else {
			json.set(2, "2-6");
		}
		HttpUtil.write(response, json.toString(), HttpUtil.json);
	}
	
	@RequestMapping(value = WUC.LOTTERY_PAYMENT_CHANNEL_MOBILESCAN_ADD, method = { RequestMethod.POST })
	@ResponseBody
	public void LOTTERY_PAYMENT_CHANNEL_MOBILESCAN_ADD(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
		String actionKey = WUC.LOTTERY_PAYMENT_CHANNEL_MOBILESCAN_ADD;
		long t1 = System.currentTimeMillis();
		WebJSONObject json = new WebJSONObject(super.getAdminDataFactory());
		AdminUser uEntity = super.getCurrUser(session, request, response);
		if (uEntity != null) {
			if (super.hasAccess(uEntity, actionKey)) {
				String name = HttpUtil.getStringParameterTrim(request, "name");
				String mobileName = HttpUtil.getStringParameterTrim(request, "mobileName");
				String frontName = HttpUtil.getStringParameterTrim(request, "frontName");
				String channelCode = HttpUtil.getStringParameterTrim(request, "channelCode");
				String merCode = HttpUtil.getStringParameterTrim(request, "merCode");
				double totalCredits = HttpUtil.getDoubleParameter(request, "totalCredits");
				double minTotalRecharge = HttpUtil.getDoubleParameter(request, "minTotalRecharge");
				double maxTotalRecharge = HttpUtil.getDoubleParameter(request, "maxTotalRecharge");
				double minUnitRecharge = HttpUtil.getDoubleParameter(request, "minUnitRecharge");
				double maxUnitRecharge = HttpUtil.getDoubleParameter(request, "maxUnitRecharge");
				String maxRegisterTime = HttpUtil.getStringParameterTrim(request, "maxRegisterTime");
				String qrCodeContent = HttpUtil.getStringParameterTrim(request, "qrCodeContent");
				int fixedQRAmount = HttpUtil.getIntParameter(request, "fixedQRAmount");
				int type = HttpUtil.getIntParameter(request, "type");
				int subType = HttpUtil.getIntParameter(request, "subType");
				double consumptionPercent = HttpUtil.getDoubleParameter(request, "consumptionPercent");
				String whiteUsernames = HttpUtil.getStringParameterTrim(request, "whiteUsernames");
				String startTime = HttpUtil.getStringParameterTrim(request, "startTime");
				String endTime = HttpUtil.getStringParameterTrim(request, "endTime");
				String fixedAmountQrs = HttpUtil.getStringParameterTrim(request, "fixedAmountQrs");

				if (StringUtils.isNotEmpty(maxRegisterTime)) {
					maxRegisterTime += " 23:59:59";
				}

				if (StringUtils.isEmpty(merCode)) {
					merCode = "123456";
				}

				boolean result = paymentChannelService.add(name, mobileName, frontName, channelCode, merCode, totalCredits, minTotalRecharge, maxTotalRecharge, minUnitRecharge, maxUnitRecharge, maxRegisterTime, qrCodeContent, fixedQRAmount, type, subType, consumptionPercent, whiteUsernames, startTime, endTime, fixedAmountQrs, Global.ADD_MONEY_TYPE_MANUAL);
				if(result) {
					adminUserLogJob.logAddPaymenChannel(uEntity, request, name);
					json.set(0, "0-6");
				} else {
					json.set(1, "1-6");
				}
			} else {
				json.set(2, "2-4");
			}
		} else {
			json.set(2, "2-6");
		}
		long t2 = System.currentTimeMillis();
		if (uEntity != null) {
			adminUserActionLogJob.add(request, actionKey, uEntity, json, t2 - t1);
		}
		HttpUtil.write(response, json.toString(), HttpUtil.json);
	}
	
	@RequestMapping(value = WUC.LOTTERY_PAYMENT_CHANNEL_MOBILESCAN_EDIT, method = { RequestMethod.POST })
	@ResponseBody
	public void LOTTERY_PAYMENT_CHANNEL_MOBILESCAN_EDIT(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
		String actionKey = WUC.LOTTERY_PAYMENT_CHANNEL_MOBILESCAN_EDIT;
		long t1 = System.currentTimeMillis();
		WebJSONObject json = new WebJSONObject(super.getAdminDataFactory());
		AdminUser uEntity = super.getCurrUser(session, request, response);
		if (uEntity != null) {
			if (super.hasAccess(uEntity, actionKey)) {
				int id = HttpUtil.getIntParameter(request, "id");
				String name = HttpUtil.getStringParameterTrim(request, "name");
				String mobileName = HttpUtil.getStringParameterTrim(request, "mobileName");
				String frontName = HttpUtil.getStringParameterTrim(request, "frontName");
				double totalCredits = HttpUtil.getDoubleParameter(request, "totalCredits");
				double minTotalRecharge = HttpUtil.getDoubleParameter(request, "minTotalRecharge");
				double maxTotalRecharge = HttpUtil.getDoubleParameter(request, "maxTotalRecharge");
				double minUnitRecharge = HttpUtil.getDoubleParameter(request, "minUnitRecharge");
				double maxUnitRecharge = HttpUtil.getDoubleParameter(request, "maxUnitRecharge");
				String maxRegisterTime = HttpUtil.getStringParameterTrim(request, "maxRegisterTime");
				String qrCodeContent = HttpUtil.getStringParameterTrim(request, "qrCodeContent");
				int fixedQRAmount = HttpUtil.getIntParameter(request, "fixedQRAmount");
				double consumptionPercent = HttpUtil.getDoubleParameter(request, "consumptionPercent");
				String whiteUsernames = HttpUtil.getStringParameterTrim(request, "whiteUsernames");
				String startTime = HttpUtil.getStringParameterTrim(request, "startTime");
				String endTime = HttpUtil.getStringParameterTrim(request, "endTime");
				String fixedAmountQrs = HttpUtil.getStringParameterTrim(request, "fixedAmountQrs");

				if (StringUtils.isNotEmpty(maxRegisterTime)) {
					maxRegisterTime += " 23:59:59";
				}

				boolean result = paymentChannelService.edit(id, name, mobileName, frontName, totalCredits, minTotalRecharge, maxTotalRecharge, minUnitRecharge, maxUnitRecharge, maxRegisterTime, qrCodeContent, fixedQRAmount, consumptionPercent, whiteUsernames, startTime, endTime, fixedAmountQrs);
				if(result) {
					adminUserLogJob.logEditPaymenChannel(uEntity, request, name);
					json.set(0, "0-6");
				} else {
					json.set(1, "1-6");
				}
			} else {
				json.set(2, "2-4");
			}
		} else {
			json.set(2, "2-6");
		}
		long t2 = System.currentTimeMillis();
		if (uEntity != null) {
			adminUserActionLogJob.add(request, actionKey, uEntity, json, t2 - t1);
		}
		HttpUtil.write(response, json.toString(), HttpUtil.json);
	}
	
	@RequestMapping(value = WUC.LOTTERY_PAYMENT_CHANNEL_MOBILESCAN_UPDATE_STATUS, method = { RequestMethod.POST })
	@ResponseBody
	public void LOTTERY_PAYMENT_CHANNEL_UPDATE_STATUS(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
		String actionKey = WUC.LOTTERY_PAYMENT_CHANNEL_MOBILESCAN_UPDATE_STATUS;
		long t1 = System.currentTimeMillis();
		WebJSONObject json = new WebJSONObject(super.getAdminDataFactory());
		AdminUser uEntity = super.getCurrUser(session, request, response);
		if (uEntity != null) {
			if (super.hasAccess(uEntity, actionKey)) {
				int id = HttpUtil.getIntParameter(request, "id");
				int status = HttpUtil.getIntParameter(request, "status");
				boolean result = paymentChannelService.updateStatus(id, status);
				if(result) {
					adminUserLogJob.logEditPaymenChannelStatus(uEntity, request, id, status);
					json.set(0, "0-5");
				} else {
					json.set(1, "1-5");
				}
			} else {
				json.set(2, "2-4");
			}
		} else {
			json.set(2, "2-6");
		}
		long t2 = System.currentTimeMillis();
		if (uEntity != null) {
			adminUserActionLogJob.add(request, actionKey, uEntity, json, t2 - t1);
		}
		HttpUtil.write(response, json.toString(), HttpUtil.json);
	}
	

	@RequestMapping(value = WUC.LOTTERY_PAYMENT_CHANNEL_MOBILESCAN_DELETE, method = { RequestMethod.POST })
	@ResponseBody
	public void LOTTERY_PAYMENT_CHANNEL_DELETE(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
		String actionKey = WUC.LOTTERY_PAYMENT_CHANNEL_MOBILESCAN_DELETE;
		long t1 = System.currentTimeMillis();
		WebJSONObject json = new WebJSONObject(super.getAdminDataFactory());
		AdminUser uEntity = super.getCurrUser(session, request, response);
		if (uEntity != null) {
			if (super.hasAccess(uEntity, actionKey)) {
				int id = HttpUtil.getIntParameter(request, "id");
				boolean result = paymentChannelService.delete(id);
				if(result) {
					adminUserLogJob.logDeletePaymenChannel(uEntity, request, id);
					json.set(0, "0-5");
				} else {
					json.set(1, "1-5");
				}
			} else {
				json.set(2, "2-4");
			}
		} else {
			json.set(2, "2-6");
		}
		long t2 = System.currentTimeMillis();
		if (uEntity != null) {
			adminUserActionLogJob.add(request, actionKey, uEntity, json, t2 - t1);
		}
		HttpUtil.write(response, json.toString(), HttpUtil.json);
	}
	
	@RequestMapping(value = WUC.LOTTERY_PAYMENT_CHANNEL_MOBILESCAN_QR_CODE_DELETE, method = { RequestMethod.POST })
	@ResponseBody
	public void LOTTERY_PAYMENT_CHANNEL_MOBILESCAN_QR_CODE_DELETE(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
		String actionKey = WUC.LOTTERY_PAYMENT_CHANNEL_MOBILESCAN_QR_CODE_DELETE;
		long t1 = System.currentTimeMillis();
		WebJSONObject json = new WebJSONObject(super.getAdminDataFactory());
		AdminUser uEntity = super.getCurrUser(session, request, response);
		if (uEntity != null) {
			if (super.hasAccess(uEntity, actionKey)) {
				int id = HttpUtil.getIntParameter(request, "id");
				boolean result = paymentChannelQrCodeService.delete(id);
				if(result) {
					json.set(0, "0-5");
				} else {
					json.set(1, "1-5");
				}
			} else {
				json.set(2, "2-4");
			}
		} else {
			json.set(2, "2-6");
		}
		long t2 = System.currentTimeMillis();
		if (uEntity != null) {
			adminUserActionLogJob.add(request, actionKey, uEntity, json, t2 - t1);
		}
		HttpUtil.write(response, json.toString(), HttpUtil.json);
	}
}
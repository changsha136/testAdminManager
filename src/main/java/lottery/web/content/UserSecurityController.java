package lottery.web.content;

import javautils.http.HttpUtil;
import javautils.jdbc.PageList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lottery.domains.content.biz.UserSecurityService;
import lottery.domains.content.dao.UserDao;
import lottery.domains.content.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import admin.domains.content.entity.AdminUser;
import admin.domains.jobs.AdminUserActionLogJob;
import admin.domains.jobs.AdminUserCriticalLogJob;
import admin.domains.jobs.AdminUserLogJob;
import admin.web.WUC;
import admin.web.WebJSONObject;
import admin.web.helper.AbstractActionController;

@Controller
public class UserSecurityController extends AbstractActionController {

	@Autowired
	private AdminUserActionLogJob adminUserActionLogJob;
	
	@Autowired
	private AdminUserLogJob adminUserLogJob;
	
	@Autowired
	private UserDao uDao;
	
	@Autowired
	private AdminUserCriticalLogJob adminUserCriticalLogJob;
	
	@Autowired
	private UserSecurityService uSecurityService;
	
	@RequestMapping(value = WUC.LOTTERY_USER_SECURITY_LIST, method = { RequestMethod.POST })
	@ResponseBody
	public void LOTTERY_USER_SECURITY_LIST(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
		String actionKey = WUC.LOTTERY_USER_SECURITY_LIST;
		long t1 = System.currentTimeMillis();
		WebJSONObject json = new WebJSONObject(super.getAdminDataFactory());
		AdminUser uEntity = super.getCurrUser(session, request, response);
		if (uEntity != null) {
			if (super.hasAccess(uEntity, actionKey)) {
				String username = request.getParameter("username");
				String key = request.getParameter("key");
				int start = HttpUtil.getIntParameter(request, "start");
				int limit = HttpUtil.getIntParameter(request, "limit");
				PageList pList = uSecurityService.search(username, key, start, limit);
				if(pList != null) {
					json.accumulate("totalCount", pList.getCount());
					json.accumulate("data", pList.getList());
				} else {
					json.accumulate("totalCount", 0);
					json.accumulate("data", "[]");
				}
				json.set(0, "0-3");
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
	
	@RequestMapping(value = WUC.LOTTERY_USER_SECURITY_RESET, method = { RequestMethod.POST })
	@ResponseBody
	public void LOTTERY_USER_SECURITY_RESET(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
		String actionKey = WUC.LOTTERY_USER_SECURITY_RESET;
		long t1 = System.currentTimeMillis();
		WebJSONObject json = new WebJSONObject(super.getAdminDataFactory());
		AdminUser uEntity = super.getCurrUser(session, request, response);
		if (uEntity != null) {
			if (super.hasAccess(uEntity, actionKey)) {
				String username = request.getParameter("username");
				User uBean = uDao.getByUsername(username);
				if(uBean != null) {
					boolean result = uSecurityService.reset(username);
					if(result) {
						adminUserLogJob.logResetSecurity(uEntity, request, username);
						adminUserCriticalLogJob.logResetSecurity(uEntity, request, username, actionKey);
						json.set(0, "0-5");
					} else {
						json.set(1, "1-5");
					}
				} else {
					json.set(2, "2-3");
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
package lottery.domains.content.jobs;

import javautils.date.DateUtil;
import javautils.date.Moment;
import javautils.math.MathUtil;
import lottery.domains.content.biz.UserBillService;
import lottery.domains.content.biz.UserGameReportService;
import lottery.domains.content.biz.UserGameWaterBillService;
import lottery.domains.content.biz.UserSysMessageService;
import lottery.domains.content.dao.UserDao;
import lottery.domains.content.entity.User;
import lottery.domains.content.entity.UserGameWaterBill;
import lottery.domains.content.global.Global;
import lottery.domains.content.vo.bill.UserGameReportVO;
import lottery.domains.pool.LotteryDataFactory;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 游戏返水任务，含PT和AG
 * Created by Nick on 2017/2/4.
 */
@Component
public class GameWaterJob {
    private static final Logger log = LoggerFactory.getLogger(GameWaterJob.class);

    @Autowired
    private UserGameReportService uGameReportService;

    @Autowired
    private UserGameWaterBillService uGameWaterBillService;

    @Autowired
    private UserBillService uBillService;

    @Autowired
    private UserSysMessageService uSysMessageService;

    @Autowired
    private UserDao uDao;

    @Autowired
    private LotteryDataFactory dataFactory;

    /**
     * 调度任务
     */
    @Scheduled(cron = "0 0 10 0/1 * *")
    // @PostConstruct
    public void schedule() {
        try {
            if (!dataFactory.getGameDividendConfig().isEnable()) {
                log.debug("老虎机真人体育分红没有开启，不发放游戏返水");
                return;
            }

            log.debug("游戏返水发放开始");

            // 获取昨天yyyy-MM-dd
            String yesterday = DateUtil.getYesterday();
            String today = DateUtil.getCurrentDate();

            // 开始日结
            settleUp(yesterday, today);

            log.debug("游戏返水发放完成");
        } catch (Exception e) {
            log.error("游戏返水发放出错", e);
        }
    }


    private void settleUp(String sTime, String eTime) {
        // 查找所有玩家投注数据
        List<UserGameReportVO> reports = uGameReportService.reportByUser(sTime, eTime);
        if (CollectionUtils.isEmpty(reports)) {
            return;
        }

        // 开始返水
        for (UserGameReportVO report : reports) {
            if (report.getBillingOrder() > 0) {
                waterReturn(report, sTime);
            }
        }
    }

    private void waterReturn(UserGameReportVO report, String sTime) {
        // 返给本人
        User user = uDao.getById(report.getUserId());
        if (user == null) {
            return;
        }
        //试玩不返水
        if(user.getUpid() == 0){
        	 return;
        }
        
        waterReturnToUser(report, user, user, Global.GAME_WATER_BILL_TYPE_USER, 0.003, sTime);

        // 返给上级
        User upperUser = uDao.getById(user.getUpid());
        if (upperUser != null && upperUser.getId() != 72) {
            waterReturnToUser(report, user, upperUser, Global.GAME_WATER_BILL_TYPE_PROXY, 0.001, sTime);

            // 返给上上级
            if (upperUser.getUpid() != Global.USER_TOP_ID) {
                User upperUpperUser = uDao.getById(upperUser.getUpid());
                if (upperUpperUser != null) {
                    waterReturnToUser(report, user, upperUpperUser, Global.GAME_WATER_BILL_TYPE_PROXY, 0.0005, sTime);
                }
            }
        }
    }

    private void waterReturnToUser(UserGameReportVO report, User fromUser, User toUser, int type, double scale, String sTime) {
        if (fromUser.getId() == Global.USER_TOP_ID) {
            // 总账号及关联账号不享受
            return;
        }

        UserGameWaterBill bill = new UserGameWaterBill();
        bill.setUserId(toUser.getId());
        bill.setIndicateDate(sTime);
        bill.setFromUser(fromUser.getId());
        bill.setSettleTime(new Moment().toSimpleTime());
        bill.setScale(scale);
        bill.setBillingOrder(report.getBillingOrder());

        double userAmount = MathUtil.multiply(report.getBillingOrder(), scale);
        bill.setUserAmount(userAmount);
        bill.setType(type);

        // 保存结果
        saveResult(bill, fromUser, toUser);
    }

    private void saveResult(UserGameWaterBill bill, User fromUser, User toUser) {
        if (bill.getUserAmount() <= 0) {
            return;
        }

        if (toUser.getAStatus() != 0 && toUser.getAStatus() != -1) {
            bill.setStatus(Global.GAME_WATER_BILL_STATUS_DENIED);
            bill.setRemark("用户永久冻结状态，不予发放");
        }
        else if (fromUser.getAStatus() != 0 && fromUser.getAStatus() != -1) {
            bill.setStatus(Global.GAME_WATER_BILL_STATUS_DENIED);
            bill.setRemark("触发用户永久冻结状态，不予发放");
        }
        else {
            bill.setStatus(Global.GAME_WATER_BILL_STATUS_ISSUED);
        }

        // 保存数据
        uGameWaterBillService.add(bill);

        // 新增账单并修改报表
        if (bill.getStatus() == Global.GAME_WATER_BILL_STATUS_ISSUED) {
            if (bill.getType() == Global.GAME_WATER_BILL_TYPE_USER) {
                uBillService.addGameWaterBill(toUser, Global.BILL_ACCOUNT_LOTTERY, bill.getType(), bill.getUserAmount(), "游戏返水");
            }
            else {
                uBillService.addGameWaterBill(toUser, Global.BILL_ACCOUNT_LOTTERY, bill.getType(), bill.getUserAmount(), "游戏代理返水");
            }

            // 加钱
            if (bill.getUserAmount() > 0) {
                uDao.updateLotteryMoney(bill.getUserId(), bill.getUserAmount());
                uSysMessageService.addGameWaterBill(bill.getUserId(), bill.getIndicateDate(), fromUser.getUsername(), toUser.getUsername());
            }
        }
    }
}

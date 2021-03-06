package lottery.domains.content.dao.impl;

import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javautils.jdbc.PageList;
import javautils.jdbc.hibernate.HibernateSuperDao;
import lottery.domains.content.dao.ActivityRechargeLoopBillDao;
import lottery.domains.content.entity.ActivityRechargeLoopBill;

@Repository
public class ActivityRechargeLoopBillDaoImpl implements ActivityRechargeLoopBillDao {
	
	private final String tab = ActivityRechargeLoopBill.class.getSimpleName();
	
	@Autowired
	private HibernateSuperDao<ActivityRechargeLoopBill> superDao;

	@Override
	public PageList find(List<Criterion> criterions, List<Order> orders, int start, int limit) {
		return superDao.findPageList(ActivityRechargeLoopBill.class, criterions, orders, start, limit);
	}

	@Override
	public double total(String sTime, String eTime) {
		String hql = "select sum(money) from " + tab + " where time >= ?0 and time < ?1";
		Object[] values = {sTime, eTime};
		Object result = superDao.unique(hql, values);
		return result != null ? ((Number) result).doubleValue() : 0;
	}

}

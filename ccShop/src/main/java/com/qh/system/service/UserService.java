package com.qh.system.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.qh.common.domain.Tree;
import com.qh.system.domain.DeptDO;
import com.qh.system.domain.UserDO;

@Service
public interface UserService {
	UserDO get(Integer id);

	List<UserDO> list(Map<String, Object> map);

	int count(Map<String, Object> map);

	int save(UserDO user);

	int update(UserDO user);

	int remove(Integer userId);

	int batchremove(Integer[] userIds);

	boolean exit(Map<String, Object> params);

	Set<String> listRoles(Integer userId);

	int resetPwd(UserDO user);

	int resetFundPwd(UserDO user);

	Tree<DeptDO> getTree();

	/**
	 * @Description 更新密码
	 * @param dataUserDo
	 */
	void updatePassword(UserDO dataUserDo);

	/**
	 * @Description 更新资金密码
	 * @param dataUserDo
	 */
	void updateFundPassword(UserDO dataUserDo);

}

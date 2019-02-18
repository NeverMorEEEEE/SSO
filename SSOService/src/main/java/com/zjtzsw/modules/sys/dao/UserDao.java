package com.zjtzsw.modules.sys.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zjtzsw.modules.sys.entity.UserEntity;

/**
 * 
 * @author wac
 * @version 创建时间：2019年2月13日 下午11:14:31
 */
@Repository
public interface UserDao extends JpaRepository<UserEntity, String> {
	
	@Query(value="select * from t_user where instr(userName,?1)",nativeQuery=true)
	public void jpaTest(@Param("name")String name);
	
	@Query(value="select * from t_user where userAccount = ?1",nativeQuery=true)
	public UserEntity findOneByAccount(@Param("account")String account);
}
package com.wsm.user.repo;


import com.wsm.user.pojo.Ouath2ClientRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface OauthClientRegisterRepository extends JpaRepository<Ouath2ClientRegister, Integer> {
    List<Ouath2ClientRegister> findByUserId(Integer userId);

    Ouath2ClientRegister findByAppName(String appName);

    @Query(value = "update oauth_ext_app_register set approve = 1 where app_name = ?1", nativeQuery = true)
    @Modifying
    @Transactional
    void updateRegisterClientByAppName(String appName);
}

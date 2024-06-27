package com.wsm.user.repo;

import com.wsm.user.pojo.Oauth2Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface OauthClientRepository extends JpaRepository<Oauth2Client, Integer> {

    Oauth2Client findByClientId(String clientId);

    // 对于手机+code 登录的用户，本身没有密码，code就是密码，所以这个密码需要随时更替
    // 用户登录30 天有效，30天以后需要重新登录，如果30以后使用phone+code的话，就需要更新
    // user表的password字段和oauth client 表的 client_secret 字段为当前code。
    @Query(value = "update oauth_client_details set client_secret = ?1 where client_id = ?2", nativeQuery = true)
    @Transactional
    @Modifying
    void updateSecretByClientId(String secret, String clientId);
}

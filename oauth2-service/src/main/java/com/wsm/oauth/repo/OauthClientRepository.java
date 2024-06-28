package com.wsm.oauth.repo;


import com.wsm.oauth.pojo.Oauth2Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface OauthClientRepository extends JpaRepository<Oauth2Client, Integer> {

    Oauth2Client findByClientId(String clientId);
}

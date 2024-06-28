package com.wsm.oauth.repo;

import com.wsm.oauth.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    User queryByUserName(String userName);
}

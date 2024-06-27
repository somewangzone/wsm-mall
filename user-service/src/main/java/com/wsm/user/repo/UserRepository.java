package com.wsm.user.repo;

import com.wsm.user.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {

    User findByUserName(String userName);

    User findByUserPhone(String phoneNumber);


}

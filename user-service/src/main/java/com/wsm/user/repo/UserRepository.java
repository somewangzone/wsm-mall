package com.wsm.user.repo;

import com.wsm.user.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    User findByUserName(String userName);

    User findByUserPhone(String phoneNumber);

    @Query(value = "update user set user_phone = ?1 where id = ?2", nativeQuery = true)
    @Transactional
    @Modifying
    void updatePhoneById(String phoneNumber, Integer userId);
}

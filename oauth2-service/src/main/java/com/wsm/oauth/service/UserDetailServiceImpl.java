package com.wsm.oauth.service;

import com.wsm.oauth.pojo.User;
import com.wsm.oauth.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// 第一步：重写 springSecurity 的 UserDetailsService,为了从我们的user表中提取当前用户的信息。
@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.queryByUserName(username);
        if (null != user) {
            return new org.springframework.security.core.userdetails.User(username,
                    user.getPasswd(),
                    AuthorityUtils.createAuthorityList(user.getPasswd()));
        } else {
            throw new UsernameNotFoundException("User not found!");
        }
    }
}

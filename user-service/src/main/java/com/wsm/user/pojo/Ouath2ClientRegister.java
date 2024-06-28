package com.wsm.user.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "oauth_ext_app_register")
public class Ouath2ClientRegister {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "app_name")
    private String appName;

    @Column(name = "app_main_page")
    private String appMainPage;

    @Column(name = "app_callback_url")
    private String appCallbackUrl;

    @Column(name = "app_logo")
    private Integer appLogo;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "client_secret")
    private String clientSecret;

    @Column(name = "approve")
    private int approve;

    @Column(name = "user_id")
    private Integer userId;
}

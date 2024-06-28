package com.wsm.oauth.filter;

import com.alibaba.fastjson.JSONObject;
import com.wsm.common.response.CommonResponse;
import com.wsm.common.response.ResponseCode;
import com.wsm.common.response.ResponseUtils;
import com.wsm.oauth.pojo.Oauth2Client;
import com.wsm.oauth.repo.OauthClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@WebFilter(filterName = "OauthFilter")
public class OauthFilter implements Filter {

    private String filterPath;

    @Autowired
    private OauthClientRepository oauthClientRepository;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterPath = "/oauth/authorize";
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String uri = request.getRequestURI();
        if (uri.equals(filterPath)) {
            Map<String, String[]> parameterMap = request.getParameterMap();
            if (!parameterMap.containsKey("redirect_url")) {
                CommonResponse commonResponse = ResponseUtils.failResponse(ResponseCode.BAD_REQUEST.getCode(),
                        null, "redirect url can not be blank");

                returnJson(servletResponse, JSONObject.toJSONString(commonResponse));
                return;
            }
            // 去DB查询我们当前的client id 的 redirect_url做对比
            Oauth2Client oauth2Client = oauthClientRepository.findByClientId(parameterMap.get("client_id")[0]);
            String redirectUrl = parameterMap.get("redirect_url")[0];
            if (!redirectUrl.equals(oauth2Client.getRedirectUrl())) {

                CommonResponse commonResponse = ResponseUtils.failResponse(ResponseCode.BAD_REQUEST.getCode(),
                        null, "redirect url is not match!");

                returnJson(servletResponse, JSONObject.toJSONString(commonResponse));
                return;
            }

            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private void returnJson(ServletResponse servletResponse, String responseJson) {
        servletResponse.setCharacterEncoding("UTF-8");
        servletResponse.setContentType("application/json; charset-utf-8");
        try (ServletOutputStream outputStream = servletResponse.getOutputStream()) {
            outputStream.write(responseJson.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UnsupportedOperationException("Unknown issues when write the OauthFilter response!");
        }
    }
}

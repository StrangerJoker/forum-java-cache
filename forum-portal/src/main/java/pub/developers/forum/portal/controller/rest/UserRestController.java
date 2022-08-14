package pub.developers.forum.portal.controller.rest;

import org.springframework.web.bind.annotation.*;
import pub.developers.forum.api.model.ResultModel;
import pub.developers.forum.api.request.user.UserEmailLoginRequest;
import pub.developers.forum.api.request.user.UserRegisterRequest;
import pub.developers.forum.api.request.user.UserUpdateInfoRequest;
import pub.developers.forum.api.request.user.UserUpdatePwdRequest;
import pub.developers.forum.api.service.UserApiService;
import pub.developers.forum.common.constant.Constant;
import pub.developers.forum.portal.support.WebUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Qiangqiang.Bian
 * @create 2020/10/29
 * @desc
 **/
@RestController
@RequestMapping("/user-rest")
public class UserRestController {

    @Resource
    private UserApiService userApiService;

    /**
     * 注册
     * @param request
     * @param servletRequest
     * @param response
     * @return
     */
    @PostMapping("/register")
    public ResultModel<String> register(@RequestBody UserRegisterRequest request,
                                        HttpServletRequest servletRequest, HttpServletResponse response) {
        request.setIp(WebUtil.requestIp(servletRequest));
        request.setUa(WebUtil.requestUa(servletRequest));
        ResultModel<String> resultModel = userApiService.register(request);

        WebUtil.cookieAddSid(response, resultModel.getData());

        return resultModel;
    }

    /**
     * 登录
     * @param request
     * @param servletRequest
     * @param response
     * @return
     */
    @PostMapping("/login")
    public ResultModel<String> login(@RequestBody UserEmailLoginRequest request, HttpServletRequest servletRequest, HttpServletResponse response) {
        request.setIp(WebUtil.requestIp(servletRequest));
        request.setUa(WebUtil.requestUa(servletRequest));
        ResultModel<String> resultModel =  userApiService.emailLogin(request);

        WebUtil.cookieAddSid(response, resultModel.getData());

        return resultModel;
    }

    /**
     * 更改消息
     * @param updateInfoRequest
     * @param request
     * @return
     */
    @PostMapping("/update-info")
    public ResultModel updateInfo(@RequestBody UserUpdateInfoRequest updateInfoRequest, HttpServletRequest request) {
        request.setAttribute(Constant.REQUEST_HEADER_TOKEN_KEY, WebUtil.cookieGetSid(request));

        return userApiService.updateInfo(updateInfoRequest);
    }

    /**
     * 更改密码
     * @param updatePwdRequest
     * @param request
     * @return
     */
    @PostMapping("/update-pwd")
    public ResultModel updatePwd(@RequestBody UserUpdatePwdRequest updatePwdRequest, HttpServletRequest request) {
        request.setAttribute(Constant.REQUEST_HEADER_TOKEN_KEY, WebUtil.cookieGetSid(request));

        return userApiService.updatePwd(updatePwdRequest);
    }

    /**
     * 关注
     * @param followed
     * @param request
     * @return
     */
    @PostMapping("/follow/{followed}")
    public ResultModel follow(@PathVariable("followed") Long followed, HttpServletRequest request) {
        request.setAttribute(Constant.REQUEST_HEADER_TOKEN_KEY, WebUtil.cookieGetSid(request));

        return userApiService.follow(followed);
    }

    /**
     * 取消关注
     * @param followed
     * @param request
     * @return
     */
    @PostMapping("/cancel-follow/{followed}")
    public ResultModel cancelFollow(@PathVariable("followed") Long followed, HttpServletRequest request) {
        request.setAttribute(Constant.REQUEST_HEADER_TOKEN_KEY, WebUtil.cookieGetSid(request));

        return userApiService.cancelFollow(followed);
    }
}

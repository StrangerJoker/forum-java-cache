package pub.developers.forum.portal.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import pub.developers.forum.api.model.ResultModel;
import pub.developers.forum.api.response.user.UserInfoResponse;
import pub.developers.forum.api.service.MessageApiService;
import pub.developers.forum.api.service.UserApiService;
import pub.developers.forum.common.constant.Constant;
import pub.developers.forum.common.support.GlobalViewConfig;
import pub.developers.forum.common.support.RequestContext;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Qiangqiang.Bian
 * @create 2020/10/29
 * @desc
 **/
@Slf4j
@Component
public class GlobalViewInterceptor extends HandlerInterceptorAdapter {

    @Resource
    private UserApiService userApiService;

    @Resource
    private MessageApiService messageApiService;

    @Resource
    private GlobalViewConfig globalViewConfig;

    /**
     * 目标方法执行之前
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 设置当前请求的 trace-id
        RequestContext.init();
        // 从 HttpServletRequest 中拿到sid（token）
        String sid = WebUtil.cookieGetSid(request);
        // 没有拿到 sid 就 执行
        if (ObjectUtils.isEmpty(sid)) {
            return true;
        }
        // 拿到 sid 就 在 webcontext中 设置 sid(token) 再 执行要执行的方法
        if (!ObjectUtils.isEmpty(sid)) {
            ResultModel<UserInfoResponse> resultModel = userApiService.info(sid);
            if (resultModel.getSuccess() && !ObjectUtils.isEmpty(resultModel.getData())) {
                WebContext.setCurrentSid(sid);
                WebContext.setCurrentUser(resultModel.getData());
            }
        }

        return true;
    }

    /**
     * 目标方法执行之后，视图返回之前
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        try {
            // 获取当前登录用户信息
            UserInfoResponse loginUserInfo = WebContext.getCurrentUser();
            // 重定向请求不需要添加
            // 向前端传递 用户的信息 登录信息 和  globalViewConfig
            if (!ObjectUtils.isEmpty(modelAndView) && !modelAndView.getViewName().startsWith(WebConst.REQUEST_REDIRECT_PREFIX)) {
                if (!ObjectUtils.isEmpty(loginUserInfo)) {
                    request.setAttribute(Constant.REQUEST_HEADER_TOKEN_KEY, WebUtil.cookieGetSid(request));

                    Map<String, Object> loginUser = new HashMap<>();
                    loginUser.put("id", loginUserInfo.getId());
                    loginUser.put("nickname", loginUserInfo.getNickname());
                    loginUser.put("avatar", loginUserInfo.getAvatar());
                    loginUser.put("role", loginUserInfo.getRole());
                    loginUser.put("unReadMsgNumber", countUnRead());

                    modelAndView.getModel().put("loginUser", loginUser);
                }
                modelAndView.getModel().put("isLogin", !ObjectUtils.isEmpty(loginUserInfo));
                modelAndView.getModel().put("globalConfig", globalViewConfig);
            }
        } finally {
            // 最后清除掉 WebContext  RequestContext 所有的内容
            WebContext.removeAll();
            RequestContext.removeAll();
        }
    }

    private Long countUnRead() {
        ResultModel<Long> countResult = messageApiService.countUnRead();
        if (countResult.getSuccess() && !ObjectUtils.isEmpty(countResult.getData())) {
            return countResult.getData();
        }
        return 0L;
    }

}

package pub.developers.forum.facade.aspect;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import pub.developers.forum.app.support.LoginUserContext;
import pub.developers.forum.common.constant.Constant;
import pub.developers.forum.common.enums.CacheBizTypeEn;
import pub.developers.forum.domain.entity.User;
import pub.developers.forum.domain.service.CacheService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Qiangqiang.Bian
 * @create 2020/10/31
 * @desc
 **/
@Slf4j
@Component
@Aspect
public class LoginUserAspect {

    @Resource
    private HttpServletRequest httpServletRequest;

    @Resource(name = "redisCacheServiceImpl")
    private CacheService cacheService;

    /* 环绕通知 = 前向通知 + 目标方法执行 + 环绕通知 ； ProceedingJoinPoint.proceed() 方法用于启动目标方法执行 */
    @Around("execution(* pub.developers.forum.api..*.*(..))")
    public Object invoke(ProceedingJoinPoint pjp) throws Throwable {
        // 从http请求中拿到token
        String token = httpServletRequest.getHeader(Constant.REQUEST_HEADER_TOKEN_KEY);
        if (ObjectUtils.isEmpty(token)) {
            Object tokenObj = httpServletRequest.getAttribute(Constant.REQUEST_HEADER_TOKEN_KEY);
            if (ObjectUtils.isEmpty(tokenObj)) {
                // 从http的属性值中拿不到token，用户未登录
                return pjp.proceed();
            }
            // 只要拿到了代表用户已经登陆过
            token = String.valueOf(tokenObj);
        }
        // 根据http请求 从缓存中拿到token
        String cacheString = cacheService.get(CacheBizTypeEn.USER_LOGIN_TOKEN, token);
        if (ObjectUtils.isEmpty(cacheString)) {
            // 拿不到 缓存过期了
            return pjp.proceed();
        }
        // 设置当前用户的token
        LoginUserContext.setToken(token);
        LoginUserContext.setUser(JSON.parseObject(cacheString, User.class));

        try {
            return pjp.proceed();
        } finally {
            LoginUserContext.removeAll();
        }
    }


}

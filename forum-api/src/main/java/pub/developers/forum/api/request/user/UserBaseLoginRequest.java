package pub.developers.forum.api.request.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Qiangqiang.Bian
 * @create 2020/12/4
 * @desc
 **/
@Data
public class UserBaseLoginRequest implements Serializable {

    // user-agent
    private String ua;
    // ip addr
    private String ip;

}

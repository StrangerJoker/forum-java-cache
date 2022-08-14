package pub.developers.forum.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Qiangqiang.Bian
 * @create 2020/10/20
 * @desc
 **/
@Getter
@AllArgsConstructor
public enum CacheBizTypeEn {
    USER_LOGIN_TOKEN("USER_LOGIN_TOKEN", "用户登录凭证 token"),
    TAG_USED("TAG_USED", "已使用标签"),


    // 自己添加的
    POST_APPROVAL_NUM("POST_APPROVAL_COUNT", "帖子的点赞量"),
    POST_COMMENT_NUM("POST_COMMENT_COUNT", "帖子的评论量"),
    POSTS_VIEW_NUM("POSTS_VIEW_NUM", "浏览量"),
    USER_FOLLOWED_NUM("USER_FOLLOWED_NUM", "用户关注数量"),
    USER_FOLLOWER_NUM("USER_FOLLOWER_NUM", "用户粉丝数量"),
    TAG_REFERENCE_NUM("TAG_REFERENCE_NUM", "标签引用数量"),
    ARTICLE_TYPE_REFERENCE_NUM("ARTICLE_TYPE_REFERENCE_NUM", "文章类型引用数量"),

    ;

    private String value;
    private String desc;
}

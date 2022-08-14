package pub.developers.forum.app.listener;

import org.springframework.stereotype.Component;
import pub.developers.forum.common.enums.CacheBizTypeEn;
import pub.developers.forum.common.support.EventBus;
import pub.developers.forum.domain.entity.BasePosts;
import pub.developers.forum.domain.repository.PostsRepository;
import pub.developers.forum.infrastructure.cache.RedisCacheServiceImpl;

import javax.annotation.Resource;

/**
 * @author Qiangqiang.Bian
 * @create 2020/11/10
 * @desc
 **/
@Component
public class PostsInfoListener extends EventBus.EventHandler<BasePosts> {

    @Resource
    private PostsRepository postsRepository;

    @Resource(name = "redisCacheServiceImpl")
    private RedisCacheServiceImpl redisCacheServiceImpl;

    @Override
    public EventBus.Topic topic() {
        return EventBus.Topic.POSTS_INFO;
    }

    @Override
    public void onMessage(BasePosts posts) {

        redisCacheServiceImpl.selfIncr(CacheBizTypeEn.POSTS_VIEW_NUM, posts.getId().toString());
        // postsRepository.increaseViews(posts.getId(), posts.getUpdateAt());
    }
}

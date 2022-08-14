package pub.developers.forum.app.listener;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.events.Event;
import pub.developers.forum.common.enums.CacheBizTypeEn;
import pub.developers.forum.common.support.EventBus;
import pub.developers.forum.domain.entity.Article;
import pub.developers.forum.domain.entity.Tag;
import pub.developers.forum.domain.repository.ArticleTypeRepository;
import pub.developers.forum.domain.repository.TagRepository;
import pub.developers.forum.domain.service.CacheService;
import pub.developers.forum.infrastructure.cache.RedisCacheServiceImpl;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Qiangqiang.Bian
 * @create 2020/11/4
 * @desc
 **/
@Component
public class ArticleCreateListener extends EventBus.EventHandler<Article> {

    @Resource
    private TagRepository tagRepository;

    @Resource
    private ArticleTypeRepository articleTypeRepository;

    @Resource(name = "redisCacheServiceImpl")
    private RedisCacheServiceImpl redisCacheServiceImpl;

    @Override
    public EventBus.Topic topic() {
        return EventBus.Topic.ARTICLE_CREATE;
    }

    @Override
    public void onMessage(Article article) {

        Set<Long> tagIds = article.getTags().stream().map(Tag::getId).collect(Collectors.toSet());

        redisCacheServiceImpl.batchSelfIncr(CacheBizTypeEn.TAG_REFERENCE_NUM, tagIds);
        redisCacheServiceImpl.selfIncr(CacheBizTypeEn.TAG_REFERENCE_NUM, article.getType().getId().toString());
//        tagRepository.increaseRefCount(tagIds);
//        articleTypeRepository.increaseRefCount(article.getType().getId());
    }
}

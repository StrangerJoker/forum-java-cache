package pub.developers.forum.infrastructure.cache;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pub.developers.forum.common.enums.CacheBizTypeEn;
import pub.developers.forum.common.support.SafesUtil;
import pub.developers.forum.domain.service.CacheService;
import pub.developers.forum.infrastructure.dal.dao.*;
import pub.developers.forum.infrastructure.dal.dataobject.ArticleTypeDO;
import pub.developers.forum.infrastructure.dal.dataobject.PostsDO;
import pub.developers.forum.infrastructure.dal.dataobject.TagDO;
import pub.developers.forum.infrastructure.dal.dataobject.TagPostsMappingDO;
import pub.developers.forum.infrastructure.dal.redis.RedisOperation;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.*;

/**
 * 直接缓存，不用定时任务
 *
 */
@Service("redisCacheServiceImpl")
public class RedisCacheServiceImpl implements CacheService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserDAO userDAO;

    @Resource
    private PostsDAO postsDAO;

    @Resource
    private TagDAO tagDAO;

    @Resource
    private ArticleTypeDAO articleTypeDAO;

    @Resource
    private TagPostsMappingDAO tagPostsMappingDAO;

    @Resource
    private RedisOperation redisOperation;

    /**
     * 批量处理
     * @param bizType
     * @param ids
     */
    public void batchSelfIncr(CacheBizTypeEn bizType, Set<Long> ids) {
        ids.forEach(id -> selfIncr(bizType, id.toString()));
    }

    /**
     * 自增 1
     * @param bizType
     * @param key
     */
    public void selfIncr(CacheBizTypeEn bizType, String key) {
        redisOperation.stringIncr(bizType, key);
    }

    public void batchSelfDecr(CacheBizTypeEn bizTypeEn, Set<Long> ids) {
        ids.forEach(id -> redisOperation.stringDecr(bizTypeEn, id.toString()));
    }

    /**
     * 自减 1
     * @param bizTypeEn
     * @param key
     */
    public void selfDecr(CacheBizTypeEn bizTypeEn, String key) {
        redisOperation.stringDecr(bizTypeEn, key);
    }

    @Override
    public boolean set(CacheBizTypeEn bizType, String key, String value) {
        redisOperation.stringOperaSet(bizType, key, value);
        return true;
    }

    public boolean set(CacheBizTypeEn bizTypeEn, String key, Long value) {
        redisOperation.stringOperaSet(bizTypeEn, key, value);
        return true;
    }

    @Override
    public boolean setAndExpire(CacheBizTypeEn bizType, String key, String value, Long seconds) {
        redisOperation.stringOperaSetAndExpire(bizType, key, value, seconds);
        return true;
    }

    public boolean setAndExpire(CacheBizTypeEn bizType, String key, Long value, Long seconds) {
        redisOperation.stringOperaSetAndExpire(bizType, key, value, seconds);
        return true;
    }


    @Override
    public String get(CacheBizTypeEn bizType, String key) {
        return redisOperation.stringOperaGet(bizType, key);
    }

    @Override
    public Boolean exists(CacheBizTypeEn bizType, String key) {
        return redisOperation.exits(bizType, key);
    }

    @Override
    public Boolean del(CacheBizTypeEn bizType, String key) {
        return redisOperation.stringOperaDelete(bizType, key);
    }

    /**
     * 关机的时候把redis的 用户信息更新到mysql
     */
    @PreDestroy
    public void preDestroy() {
        persistence();
    }

    /**
     * 启动的时候将 mysql 里的数据加载到 redis中
     */
    @PostConstruct
    public void postConstruct() {
        // 将 ArticleType 和 Tag 的 refCount 放入到redis
        loadDbToRedis();
    }

    /**
     * 从
     */
    private void calculateDbDataToRedis() {

    }

    /**
     * 去DB加载数据到redis
     */
    private void loadDbToRedis() {
        // 文章类型引用数量
        List<ArticleTypeDO> articleTypeDOList = articleTypeDAO.getAll();
        List<Long> attIds = new ArrayList<>();
        List<Long> attRefCnts = new ArrayList<>();

        SafesUtil.ofList(articleTypeDOList).forEach(type -> {
            attIds.add(type.getId());
            attRefCnts.add(type.getRefCount() != null ? type.getRefCount() : 0);
        });
        redisOperation.stringOperaBatchSet(CacheBizTypeEn.ARTICLE_TYPE_REFERENCE_NUM, attIds, attRefCnts);

        // tag 引用数量
        List<TagDO> tagDOList = tagDAO.getAll();
        List<Long> tagIds = new ArrayList<>();
        List<Long> tagRefCnts = new ArrayList<>();
        List<TagPostsMappingDO> tagPostsMappingDOList = tagPostsMappingDAO.getAll();
        Map<Long, Long> map = new HashMap<>();
        tagPostsMappingDOList.forEach(tagPostsMappingDO ->  {
            Long tagId = tagPostsMappingDO.getTagId();
            map.put(tagId, map.getOrDefault(tagId, 0L) + 1);
        });
        SafesUtil.ofList(tagDOList).forEach(tagDO -> {
            tagIds.add(tagDO.getId());
            tagRefCnts.add(map.getOrDefault(tagDO.getId(), 0L));
        });
        redisOperation.stringOperaBatchSet(CacheBizTypeEn.TAG_REFERENCE_NUM, tagIds, tagRefCnts);

        // posts的 访问数 点赞数 评论数
        List<PostsDO> postsDOList = postsDAO.getAll();
        List<Long> postsIds = new ArrayList<>();
        List<Long> views = new ArrayList<>();
        List<Long> approvals = new ArrayList<>();
        List<Long> comments = new ArrayList<>();
        postsDOList.forEach(postsDO -> {
            postsIds.add(postsDO.getId());
            views.add(postsDO.getViews());
            approvals.add(postsDO.getApprovals());
            comments.add(postsDO.getComments());
        });
        redisOperation.stringOperaBatchSet(CacheBizTypeEn.POSTS_VIEW_NUM, postsIds, views);
        redisOperation.stringOperaBatchSet(CacheBizTypeEn.POST_APPROVAL_NUM, postsIds, approvals);
        redisOperation.stringOperaBatchSet(CacheBizTypeEn.POST_COMMENT_NUM, postsIds, comments);
    }

    /**
     * 定时任务，将redis 和 mysql
     */
    @Scheduled(cron = "0/20 * * * * ? ")
    public void task() {
        persistence();
    }

    /**
     * 将redis的缓存记录到mysql中
     */
    private void persistence() {
        // 用户的 关注和粉丝数
        Set<String> userFollowedKeys = redisTemplate.keys(CacheBizTypeEn.USER_FOLLOWED_NUM.getValue()+ ":*");
        SafesUtil.ofSet(userFollowedKeys).forEach(key -> {
            String[] split = key.split(":");
            Long id = Long.parseLong(split[1]);
            Long followedNum = redisOperation.longOperaGet(CacheBizTypeEn.USER_FOLLOWED_NUM, id.toString());
            userDAO.updateFollowedNum(id, followedNum);
        });
        Set<String> followerKeys = redisTemplate.keys(CacheBizTypeEn.USER_FOLLOWER_NUM.getValue() + ":*");
        SafesUtil.ofSet(followerKeys).forEach(followerKey -> {
            String[] split = followerKey.split(":");
            Long id = Long.parseLong(split[1]);
            Long followedNum = redisOperation.longOperaGet(CacheBizTypeEn.USER_FOLLOWER_NUM, id.toString());
            userDAO.updateFollowerNum(id, followedNum);
        });
        // 帖子的点赞数
        Set<String> postsApprovalKeys = redisTemplate.keys(CacheBizTypeEn.POST_APPROVAL_NUM.getValue() + ":*");
        SafesUtil.ofSet(postsApprovalKeys).forEach(key -> {
            String[] split = key.split(":");
            Long id = Long.parseLong(split[1]);
            Long postsApprovalNum = redisOperation.longOperaGet(CacheBizTypeEn.POST_APPROVAL_NUM, id.toString());
            postsDAO.updateApprovalNum(id, postsApprovalNum);
        });
        // 帖子评论数
        Set<String> postsCommentKeys = redisTemplate.keys(CacheBizTypeEn.POST_COMMENT_NUM.getValue() + ":*");
        SafesUtil.ofSet(postsCommentKeys).forEach(key -> {
            String[] split = key.split(":");
            Long id = Long.parseLong(split[1]);
            Long commentNum = redisOperation.longOperaGet(CacheBizTypeEn.POST_COMMENT_NUM, id.toString());
            postsDAO.updateCommentNum(id, commentNum);
        });
        // 帖子浏览数
        Set<String> postsViewsKeys = redisTemplate.keys(CacheBizTypeEn.POSTS_VIEW_NUM.getValue() + ":*");
        SafesUtil.ofSet(postsViewsKeys).forEach(key -> {
            String[] split = key.split(":");
            Long id = Long.parseLong(split[1]);
            Long views = redisOperation.longOperaGet(CacheBizTypeEn.POSTS_VIEW_NUM, id.toString());
            postsDAO.updateViews(id, views);
        });

        // 标签的引用数
        Set<String> tagRefKeys = redisTemplate.keys(CacheBizTypeEn.TAG_REFERENCE_NUM.getValue() + "*:");
        SafesUtil.ofSet(tagRefKeys).forEach(key -> {
            String[] split = key.split(":");
            Long id = Long.parseLong(split[1]);
            Long refCount = redisOperation.longOperaGet(CacheBizTypeEn.TAG_REFERENCE_NUM, id.toString());
            tagDAO.updateRefCount(id, refCount);
        });

    }
}
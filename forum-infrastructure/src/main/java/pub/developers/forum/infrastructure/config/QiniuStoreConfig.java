package pub.developers.forum.infrastructure.config;

import com.qiniu.common.Zone;
import com.qiniu.storage.UploadManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QiniuStoreConfig {

    @Autowired
    private com.qiniu.storage.Configuration configuration;

    @Bean
    public UploadManager uploadManager() {
        return new UploadManager(configuration);
    }

    @Bean
    public com.qiniu.storage.Configuration configuration() {
        return new com.qiniu.storage.Configuration(Zone.zone2());
    }

}
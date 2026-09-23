package com.sagardevlab.distributed_webgenai.workspace_service.config;

import com.sagardevlab.distributed_webgenai.workspace_service.service.impl.ProjectTemplateServiceImpl;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

/**
 * Creates the MinIO buckets and uploads the starter template bundled under classpath:starter-template/.
 * The template is re-uploaded on every start so MinIO always matches the version shipped with this service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StorageInitializer implements ApplicationRunner {

    private static final String TEMPLATE_ROOT = "starter-template/";

    private final MinioClient minioClient;

    @Value("${minio.project-bucket}")
    private String projectBucket;

    @Value("${minio.template-bucket}")
    private String templateBucket;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ensureBucket(projectBucket);
        ensureBucket(templateBucket);
        syncTemplate();
    }

    private void ensureBucket(String bucket) throws Exception {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            log.info("Created MinIO bucket: {}", bucket);
        }
    }

    private void syncTemplate() throws Exception {
        Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources("classpath*:" + TEMPLATE_ROOT + "**/*");

        int uploaded = 0;
        for (Resource resource : resources) {
            if (!resource.isReadable()) continue; // directories

            String url = resource.getURL().toString();
            String relativePath = url.substring(url.lastIndexOf(TEMPLATE_ROOT) + TEMPLATE_ROOT.length());
            if (!relativePath.startsWith(ProjectTemplateServiceImpl.TEMPLATE_NAME + "/")) continue;
            byte[] bytes = resource.getContentAsByteArray();

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(templateBucket)
                    .object(relativePath)
                    .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                    .build());
            uploaded++;
        }
        log.info("Synced starter template into bucket '{}' ({} files)", templateBucket, uploaded);
    }
}

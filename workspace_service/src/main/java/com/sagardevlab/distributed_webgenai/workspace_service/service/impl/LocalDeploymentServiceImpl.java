package com.sagardevlab.distributed_webgenai.workspace_service.service.impl;

import com.sagardevlab.distributed_webgenai.workspace_service.dto.project.DeployResponse;
import com.sagardevlab.distributed_webgenai.workspace_service.entity.ProjectFile;
import com.sagardevlab.distributed_webgenai.workspace_service.repository.ProjectFileRepository;
import com.sagardevlab.distributed_webgenai.workspace_service.service.DeploymentService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Preview runner for local development: materialises the project's files into a local folder and runs
 * the Vite dev server there. Files saved later are written straight into that folder, so Vite hot-reloads them.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.preview.mode", havingValue = "local", matchIfMissing = true)
public class LocalDeploymentServiceImpl implements DeploymentService {

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    private final MinioClient minioClient;
    private final ProjectFileRepository projectFileRepository;

    @Value("${minio.project-bucket}")
    private String projectBucket;

    @Value("${app.preview.local-dir}")
    private String localDir;

    @Value("${app.preview.local-base-port:5200}")
    private int basePort;

    private final Map<Long, Process> runningPreviews = new ConcurrentHashMap<>();

    @Override
    public synchronized DeployResponse deploy(Long projectId) {
        int port = basePort + (int) (projectId % 700);
        String previewUrl = "http://localhost:" + port;

        Process existing = runningPreviews.get(projectId);
        if (existing != null && existing.isAlive()) {
            log.info("Preview for project {} already running at {}", projectId, previewUrl);
            return new DeployResponse(previewUrl);
        }

        Path projectDir = projectDir(projectId);
        try {
            syncAllFiles(projectId, projectDir);

            String command = "npm install --no-audit --no-fund && npm run dev -- --host 127.0.0.1 --port " + port + " --strictPort";
            ProcessBuilder builder = IS_WINDOWS
                    ? new ProcessBuilder("cmd.exe", "/c", command)
                    : new ProcessBuilder("sh", "-c", command);
            builder.directory(projectDir.toFile())
                    .redirectErrorStream(true)
                    .redirectOutput(projectDir.resolve("dev.log").toFile());

            runningPreviews.put(projectId, builder.start());
            log.info("Started local preview for project {} at {} (logs: {})", projectId, previewUrl, projectDir.resolve("dev.log"));
            return new DeployResponse(previewUrl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to start local preview for project " + projectId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void onFileSaved(Long projectId, String path, String content) {
        Path projectDir = projectDir(projectId);
        if (!Files.isDirectory(projectDir)) return; // preview was never started

        try {
            writeFile(projectDir, path, content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("Failed to sync {} into local preview of project {}", path, projectId, e);
        }
    }

    @PreDestroy
    public void stopAll() {
        runningPreviews.values().forEach(process -> {
            process.descendants().forEach(ProcessHandle::destroy);
            process.destroy();
        });
    }

    private void syncAllFiles(Long projectId, Path projectDir) throws Exception {
        List<ProjectFile> files = projectFileRepository.findByProjectId(projectId);
        for (ProjectFile file : files) {
            try (InputStream is = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(projectBucket)
                    .object(projectId + "/" + file.getPath())
                    .build())) {
                writeFile(projectDir, file.getPath(), is.readAllBytes());
            }
        }
    }

    private void writeFile(Path projectDir, String path, byte[] bytes) throws IOException {
        String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        Path target = projectDir.resolve(cleanPath).normalize();
        if (!target.startsWith(projectDir)) {
            throw new IOException("Refusing to write outside the project folder: " + path);
        }
        Files.createDirectories(target.getParent());
        Files.write(target, bytes);
    }

    private Path projectDir(Long projectId) {
        return Path.of(localDir, "project-" + projectId).toAbsolutePath().normalize();
    }
}

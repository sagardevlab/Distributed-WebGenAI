package com.sagardevlab.distributed_webgenai.workspace_service.service;


import com.sagardevlab.distributed_webgenai.common_lib.dto.FileTreeDto;
import com.sagardevlab.distributed_webgenai.workspace_service.dto.project.FileContentResponse;

public interface ProjectFileService {
    FileTreeDto getFileTree(Long projectId);

    String getFileContent(Long projectId, String path);

    void saveFile(Long projectId, String filePath, String fileContent);
}

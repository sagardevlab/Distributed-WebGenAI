package com.sagardevlab.distributed_webgenai.workspace_service.mapper;

import com.sagardevlab.distributed_webgenai.common_lib.dto.FileNode;
import com.sagardevlab.distributed_webgenai.workspace_service.entity.ProjectFile;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectFileMapper {

    List<FileNode> toListOfFileNode(List<ProjectFile> projectFileList);
}

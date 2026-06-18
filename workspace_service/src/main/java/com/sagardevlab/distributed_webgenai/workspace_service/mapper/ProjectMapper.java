package com.sagardevlab.distributed_webgenai.workspace_service.mapper;

import com.sagardevlab.distributed_webgenai.common_lib.enums.ProjectRole;
import com.sagardevlab.distributed_webgenai.workspace_service.dto.project.ProjectResponse;
import com.sagardevlab.distributed_webgenai.workspace_service.dto.project.ProjectSummaryResponse;
import com.sagardevlab.distributed_webgenai.workspace_service.entity.Project;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    ProjectResponse toProjectResponse(Project project);

    ProjectSummaryResponse toProjectSummaryResponse(Project project, ProjectRole role);

    List<ProjectSummaryResponse> toListOfProjectSummaryResponse(List<Project> projects);

}

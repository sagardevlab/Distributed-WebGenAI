package com.sagardevlab.distributed_webgenai.workspace_service.mapper;

import com.sagardevlab.distributed_webgenai.workspace_service.dto.member.MemberResponse;
import com.sagardevlab.distributed_webgenai.workspace_service.entity.ProjectMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMemberMapper {

    @Mapping(target = "userId", source = "id.userId")
    MemberResponse toProjectMemberResponseFromMember(ProjectMember projectMember);
}

package com.sagardevlab.distributed_webgenai.account_service.mapper;

import com.sagardevlab.distributed_webgenai.account_service.dto.auth.SignupRequest;
import com.sagardevlab.distributed_webgenai.account_service.dto.auth.UserProfileResponse;
import com.sagardevlab.distributed_webgenai.account_service.entity.User;
import com.sagardevlab.distributed_webgenai.common_lib.dto.UserDto;
import com.sagardevlab.distributed_webgenai.common_lib.security.JwtUserPrincipal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(SignupRequest signupRequest);

    @Mapping(source = "userId", target = "id")
    UserProfileResponse toUserProfileResponse(JwtUserPrincipal user);

    UserDto toUserDto(User user);

}

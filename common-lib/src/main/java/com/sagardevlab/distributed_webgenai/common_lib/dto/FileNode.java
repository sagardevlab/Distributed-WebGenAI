package com.sagardevlab.distributed_webgenai.common_lib.dto;


public record FileNode(
        String path
) {

    @Override
    public String toString() {
        return path;
    }
}
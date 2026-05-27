package com.playmotech.ghostcoach.systemvar.dto;

import com.playmotech.ghostcoach.systemvar.SystemVar;

public record SystemVarItem(String key, String label) {

    public static SystemVarItem from(SystemVar entity) {
        return new SystemVarItem(entity.getItemKey(), entity.getLabel());
    }
}

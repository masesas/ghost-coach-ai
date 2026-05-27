package com.playmotech.ghostcoach.user;

import com.playmotech.ghostcoach.systemvar.SystemVarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Whitelist of allowed {@code position} values per {@link Sport}, backed by
 * the {@code system_var} table (category={@link #CATEGORY}, scope=sport name).
 * Stored in DB as SCREAMING_SNAKE_CASE; frontend renders the display label.
 */
@Component
@RequiredArgsConstructor
public class UserPosition {

    public static final String CATEGORY = "POSITION";

    private final SystemVarService systemVarService;

    public boolean isValid(Sport sport, String position) {
        if (sport == null || position == null) return false;
        return systemVarService.isValidKey(CATEGORY, sport.name(), position);
    }
}

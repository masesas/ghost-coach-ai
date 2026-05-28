package com.playmotech.ghostcoach.systemvar;

import com.playmotech.ghostcoach.systemvar.dto.SystemVarItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SystemVarService")
class SystemVarServiceTest {

    @Mock
    SystemVarRepository repository;

    SystemVarService service;

    @BeforeEach
    void setUp() {
        service = new SystemVarService(repository);
    }

    private static SystemVar entity(String group, String key, String label, int sort) {
        return SystemVar.builder()
                .id((long) sort)
                .groupCode(group)
                .itemKey(key)
                .label(label)
                .sortOrder(sort)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("listByGroup maps and preserves repository order")
    void listByGroup_mapsAndPreservesOrder() {
        when(repository.findByGroupCodeAndActiveTrueOrderBySortOrderAscIdAsc("POSITION_FOOTBALL"))
                .thenReturn(List.of(
                        entity("POSITION_FOOTBALL", "GOALKEEPER", "Goalkeeper", 1),
                        entity("POSITION_FOOTBALL", "DEFENDER", "Defender", 2)
                ));

        List<SystemVarItem> result = service.listByGroup("POSITION_FOOTBALL");

        assertThat(result).containsExactly(
                new SystemVarItem("GOALKEEPER", "Goalkeeper"),
                new SystemVarItem("DEFENDER", "Defender")
        );
    }

    @Test
    @DisplayName("list composes group_code with scope")
    void list_withScope_composesGroupCode() {
        when(repository.findByGroupCodeAndActiveTrueOrderBySortOrderAscIdAsc("POSITION_CRICKET"))
                .thenReturn(List.of(entity("POSITION_CRICKET", "BATSMAN", "Batsman", 1)));

        List<SystemVarItem> result = service.list("POSITION", "CRICKET");

        assertThat(result).extracting(SystemVarItem::key).containsExactly("BATSMAN");
    }

    @Test
    @DisplayName("list without scope uses category as group_code")
    void list_withoutScope_usesCategoryOnly() {
        when(repository.findByGroupCodeAndActiveTrueOrderBySortOrderAscIdAsc("COUNTRY"))
                .thenReturn(List.of(entity("COUNTRY", "ID", "Indonesia", 1)));

        List<SystemVarItem> result = service.list("COUNTRY", null);

        assertThat(result).extracting(SystemVarItem::key).containsExactly("ID");
    }

    @Test
    @DisplayName("list returns empty when no rows match")
    void list_unknownGroup_returnsEmpty() {
        when(repository.findByGroupCodeAndActiveTrueOrderBySortOrderAscIdAsc("UNKNOWN_FOO"))
                .thenReturn(List.of());

        assertThat(service.list("UNKNOWN", "FOO")).isEmpty();
    }

    @Test
    @DisplayName("isValidKey returns true for known key")
    void isValidKey_known_true() {
        when(repository.findByGroupCodeAndActiveTrueOrderBySortOrderAscIdAsc("POSITION_FOOTBALL"))
                .thenReturn(List.of(entity("POSITION_FOOTBALL", "GOALKEEPER", "Goalkeeper", 1)));

        assertThat(service.isValidKey("POSITION", "FOOTBALL", "GOALKEEPER")).isTrue();
    }

    @Test
    @DisplayName("isValidKey returns false for unknown key")
    void isValidKey_unknown_false() {
        when(repository.findByGroupCodeAndActiveTrueOrderBySortOrderAscIdAsc("POSITION_FOOTBALL"))
                .thenReturn(List.of(entity("POSITION_FOOTBALL", "GOALKEEPER", "Goalkeeper", 1)));

        assertThat(service.isValidKey("POSITION", "FOOTBALL", "QUARTERBACK")).isFalse();
    }

    @Test
    @DisplayName("isValidKey returns false for null key")
    void isValidKey_nullKey_false() {
        assertThat(service.isValidKey("POSITION", "FOOTBALL", null)).isFalse();
    }

    @Test
    @DisplayName("composeGroupCode normalizes case")
    void composeGroupCode_lowercaseInputs_uppercased() {
        assertThat(SystemVarService.composeGroupCode("position", "football"))
                .isEqualTo("POSITION_FOOTBALL");
    }

    @Test
    @DisplayName("composeGroupCode throws on null category")
    void composeGroupCode_nullCategory_throws() {
        assertThatThrownBy(() -> SystemVarService.composeGroupCode(null, "FOOTBALL"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("composeGroupCode throws on blank category")
    void composeGroupCode_blankCategory_throws() {
        assertThatThrownBy(() -> SystemVarService.composeGroupCode("   ", "FOOTBALL"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

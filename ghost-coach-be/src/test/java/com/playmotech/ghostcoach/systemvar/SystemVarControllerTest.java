package com.playmotech.ghostcoach.systemvar;

import com.playmotech.ghostcoach.common.exception.GlobalExceptionHandler;
import com.playmotech.ghostcoach.support.TestConfig;
import com.playmotech.ghostcoach.systemvar.dto.SystemVarItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SystemVarControllerTest {

    @Mock
    SystemVarService systemVarService;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new SystemVarController(systemVarService))
                .setControllerAdvice(new GlobalExceptionHandler(TestConfig.defaultAppConfigProp()))
                .build();
    }

    @Test
    @DisplayName("GET POSITION?scope=FOOTBALL → 200 + items")
    void list_validCategoryAndScope_returnsItems() throws Exception {
        when(systemVarService.list("POSITION", "FOOTBALL"))
                .thenReturn(List.of(
                        new SystemVarItem("GOALKEEPER", "Goalkeeper"),
                        new SystemVarItem("DEFENDER", "Defender")
                ));

        mvc.perform(get("/api/v1/system-vars/POSITION").param("scope", "FOOTBALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].key").value("GOALKEEPER"))
                .andExpect(jsonPath("$.data[0].label").value("Goalkeeper"))
                .andExpect(jsonPath("$.data[1].key").value("DEFENDER"));
    }

    @Test
    @DisplayName("GET unknown category → 200 + empty array (no error)")
    void list_unknownGroup_returnsEmpty() throws Exception {
        when(systemVarService.list("POSITION", "UNKNOWN"))
                .thenReturn(List.of());

        mvc.perform(get("/api/v1/system-vars/POSITION").param("scope", "UNKNOWN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("GET without scope → 200, service called with null scope")
    void list_noScope_callsServiceWithNull() throws Exception {
        when(systemVarService.list("COUNTRY", null))
                .thenReturn(List.of(new SystemVarItem("ID", "Indonesia")));

        mvc.perform(get("/api/v1/system-vars/COUNTRY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].key").value("ID"));
    }

    @Test
    @DisplayName("GET lowercase category → 400 INVALID_PARAMETER")
    void list_lowercaseCategory_rejects() throws Exception {
        mvc.perform(get("/api/v1/system-vars/position").param("scope", "FOOTBALL"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_PARAMETER"));
    }

    @Test
    @DisplayName("GET scope with invalid chars → 400 INVALID_PARAMETER")
    void list_invalidScopeChars_rejects() throws Exception {
        mvc.perform(get("/api/v1/system-vars/POSITION").param("scope", "FOOT-BALL"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_PARAMETER"));
    }
}

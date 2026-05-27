package com.playmotech.ghostcoach.user;

import com.playmotech.ghostcoach.systemvar.SystemVarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class UserPositionTest {

    @Mock
    SystemVarService systemVarService;

    UserPosition userPosition;

    @BeforeEach
    void setUp() {
        userPosition = new UserPosition(systemVarService);

        // Mirror the seed migration whitelist for delegation tests.
        lenient().when(systemVarService.isValidKey(eq("POSITION"), eq("FOOTBALL"), eq("MIDFIELDER"))).thenReturn(true);
        lenient().when(systemVarService.isValidKey(eq("POSITION"), eq("FOOTBALL"), eq("GOALKEEPER"))).thenReturn(true);
        lenient().when(systemVarService.isValidKey(eq("POSITION"), eq("FOOTBALL"), eq("QUARTERBACK"))).thenReturn(false);
        lenient().when(systemVarService.isValidKey(eq("POSITION"), eq("BASKETBALL"), eq("POINT_GUARD"))).thenReturn(true);
        lenient().when(systemVarService.isValidKey(eq("POSITION"), eq("BASKETBALL"), eq("MIDFIELDER"))).thenReturn(false);
        lenient().when(systemVarService.isValidKey(eq("POSITION"), eq("CRICKET"), eq("BOWLER"))).thenReturn(true);
        lenient().when(systemVarService.isValidKey(eq("POSITION"), eq("BADMINTON"), eq("SINGLES"))).thenReturn(true);
        lenient().when(systemVarService.isValidKey(eq("POSITION"), eq("BADMINTON"), eq("DEFENDER"))).thenReturn(false);
        lenient().when(systemVarService.isValidKey(eq("POSITION"), eq("FOOTBALL"), isNull())).thenReturn(false);
    }

    @ParameterizedTest(name = "{0} + {1} → valid={2}")
    @CsvSource({
            "FOOTBALL, MIDFIELDER, true",
            "FOOTBALL, GOALKEEPER, true",
            "FOOTBALL, QUARTERBACK, false",
            "BASKETBALL, POINT_GUARD, true",
            "BASKETBALL, MIDFIELDER, false",
            "CRICKET, BOWLER, true",
            "BADMINTON, SINGLES, true",
            "BADMINTON, DEFENDER, false",
    })
    @DisplayName("isValid delegates to SystemVarService for whitelisted (sport, position) pairs")
    void isValidCases(Sport sport, String position, boolean expected) {
        assertThat(userPosition.isValid(sport, position)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "null inputs → false")
    @CsvSource({
            ", MIDFIELDER",
            "FOOTBALL, ",
    })
    @DisplayName("null sport or position → false (no service call)")
    void nullArgs(Sport sport, String position) {
        assertThat(userPosition.isValid(sport, position)).isFalse();
    }
}

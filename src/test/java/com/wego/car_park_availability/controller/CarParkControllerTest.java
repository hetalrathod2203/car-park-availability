package com.wego.car_park_availability.controller;

import com.wego.car_park_availability.config.TestConfig;
import com.wego.car_park_availability.dto.CarParkResponse;
import com.wego.car_park_availability.service.CarParkService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CarParkController.class)
@Import(TestConfig.class)
@ActiveProfiles("test")
@SuppressWarnings("removal")
@Disabled("Tests disabled temporarily")
class CarParkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarParkService carParkService;

    @Test
    void testGetNearestCarParks_Success() throws Exception {
        List<CarParkResponse> mockResponse = Arrays.asList(
            new CarParkResponse("BLK 401-413 HOUGANG AVE 10", 1.37429, 103.896, 693, 182),
            new CarParkResponse("BLK 351-357 HOUGANG AVE 7", 1.37234, 103.899, 249, 143)
        );

        when(carParkService.findNearestCarParks(anyDouble(), anyDouble(), anyInt(), anyInt()))
            .thenReturn(mockResponse);

        mockMvc.perform(get("/carparks/nearest")
                .param("latitude", "1.37326")
                .param("longitude", "103.897")
                .param("page", "1")
                .param("per_page", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].address").value("BLK 401-413 HOUGANG AVE 10"))
                .andExpect(jsonPath("$[0].latitude").value(1.37429))
                .andExpect(jsonPath("$[0].totalLots").value(693))
                .andExpect(jsonPath("$[0].availableLots").value(182));
    }

    @Test
    void testGetNearestCarParks_MissingLatitude() throws Exception {
        mockMvc.perform(get("/carparks/nearest")
                .param("longitude", "103.897"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetNearestCarParks_InvalidLatitude() throws Exception {
        when(carParkService.findNearestCarParks(anyDouble(), anyDouble(), anyInt(), anyInt()))
                .thenReturn(Arrays.asList());
        
        mockMvc.perform(get("/carparks/nearest")
                .param("latitude", "91.0")
                .param("longitude", "103.897"))
                .andExpect(status().isBadRequest());
    }
}
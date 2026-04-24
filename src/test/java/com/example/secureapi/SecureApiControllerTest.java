package com.example.secureapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SecureApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class SecureApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void statusEndpoint_returnsUpWithDevSecOpsPractices() throws Exception {
        mockMvc.perform(get("/api/v1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.message").value("DevSecOps best practices are active"))
                .andExpect(jsonPath("$.practices.sca").exists())
                .andExpect(jsonPath("$.practices.sast").exists())
                .andExpect(jsonPath("$.practices.containerSecurity").exists())
                .andExpect(jsonPath("$.practices.buildStrategy").exists());
    }
}

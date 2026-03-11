package com.jianqing.module.dev.controller;

import com.jianqing.common.exception.GlobalExceptionHandler;
import com.jianqing.module.dev.dto.GenColumnMeta;
import com.jianqing.module.dev.dto.GenPreviewFile;
import com.jianqing.module.dev.dto.GenPreviewRequest;
import com.jianqing.module.dev.dto.GenTableSummary;
import com.jianqing.module.dev.service.GeneratorMetadataService;
import com.jianqing.module.dev.service.GeneratorPreviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DevGeneratorControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private GeneratorMetadataService generatorMetadataService;

    @Mock
    private GeneratorPreviewService generatorPreviewService;

    @InjectMocks
    private DevGeneratorController devGeneratorController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(devGeneratorController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnGeneratorTables() throws Exception {
        GenTableSummary table = new GenTableSummary();
        table.setTableName("jq_demo_customer");
        table.setTableComment("客户表");
        when(generatorMetadataService.listTables()).thenReturn(List.of(table));

        mockMvc.perform(get("/api/dev/gen/tables"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].tableName").value("jq_demo_customer"))
                .andExpect(jsonPath("$.data[0].tableComment").value("客户表"));

        verify(generatorMetadataService).listTables();
    }

    @Test
    void shouldReturnGeneratorColumns() throws Exception {
        GenColumnMeta column = new GenColumnMeta();
        column.setColumnName("customer_name");
        column.setJavaType("String");
        column.setPrimaryKey(false);
        when(generatorMetadataService.listColumns("jq_demo_customer")).thenReturn(List.of(column));

        mockMvc.perform(get("/api/dev/gen/tables/jq_demo_customer/columns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].columnName").value("customer_name"))
                .andExpect(jsonPath("$.data[0].javaType").value("String"));

        verify(generatorMetadataService).listColumns("jq_demo_customer");
    }

    @Test
    void shouldReturnPreviewFiles() throws Exception {
        GenPreviewFile file = new GenPreviewFile("src/main/java/com/jianqing/module/demo/entity/Customer.java", "public class Customer {}\n");
        when(generatorPreviewService.preview(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(file));

        mockMvc.perform(post("/api/dev/gen/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildPreviewRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].filePath").value("src/main/java/com/jianqing/module/demo/entity/Customer.java"));

        verify(generatorPreviewService).preview(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldReturnZipDownload() throws Exception {
        when(generatorPreviewService.downloadZip(org.mockito.ArgumentMatchers.any())).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(post("/api/dev/gen/download")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildPreviewRequest())))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"customers-generator-preview.zip\""));

        verify(generatorPreviewService).downloadZip(org.mockito.ArgumentMatchers.any());
    }

    private GenPreviewRequest buildPreviewRequest() {
        GenPreviewRequest request = new GenPreviewRequest();
        request.setTableName("jq_demo_customer");
        request.setModuleName("demo");
        request.setBusinessName("customers");
        request.setClassName("Customer");
        request.setPermPrefix("system:customer");
        return request;
    }
}

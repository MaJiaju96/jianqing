package com.jianqing.module.dev.service.impl;

import com.jianqing.module.dev.dto.GenColumnMeta;
import com.jianqing.module.dev.dto.GenDeleteResult;
import com.jianqing.module.dev.dto.GenPreviewFile;
import com.jianqing.module.dev.dto.GenPreviewRequest;
import com.jianqing.module.dev.dto.GenWriteResult;
import com.jianqing.module.dev.mapper.GenWriteRecordMapper;
import com.jianqing.module.dev.service.GeneratorMetadataService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeneratorPreviewServiceImplTest {

    @Mock
    private GeneratorMetadataService generatorMetadataService;

    @Mock
    private GenWriteRecordMapper genWriteRecordMapper;

    @Test
    void shouldBuildPreviewFiles() {
        when(generatorMetadataService.listColumns("jq_demo_customer")).thenReturn(List.of(
                buildColumn("id", "主键", "bigint", "bigint(20)", "Long", false, true, true),
                buildColumn("customer_name", "客户名称", "varchar", "varchar(64)", "String", false, false, false),
                buildColumn("status", "状态", "tinyint", "tinyint(1)", "Integer", false, false, false),
                buildColumn("remark", "备注", "text", "text", "String", true, false, false),
                buildColumn("available_time", "可用时间", "time", "time", "LocalTime", true, false, false),
                buildColumn("published_at", "发布时间", "datetime", "datetime", "LocalDateTime", true, false, false),
                buildColumn("is_deleted", "删除标记", "tinyint", "tinyint(1)", "Integer", false, false, false)
        ));

        GeneratorPreviewServiceImpl service = new GeneratorPreviewServiceImpl(generatorMetadataService, genWriteRecordMapper);
        List<GenPreviewFile> files = service.preview(buildRequest());

        Assertions.assertEquals(12, files.size());
        Assertions.assertTrue(files.stream().anyMatch(file -> file.getFilePath().endsWith("Customer.java")));
        Assertions.assertTrue(files.stream().anyMatch(file -> file.getFilePath().endsWith("CustomerController.java")));
        Assertions.assertTrue(files.stream().anyMatch(file -> file.getFilePath().endsWith("demoCustomers.js")));
        Assertions.assertTrue(files.stream().anyMatch(file -> file.getFilePath().endsWith("CustomersView.vue")));
        Assertions.assertTrue(files.stream().anyMatch(file -> file.getContent().contains("@RequestMapping(\"/api/demo/customers\")")));
        Assertions.assertTrue(files.stream().anyMatch(file -> file.getContent().contains("export function fetchCustomers()")));
        Assertions.assertTrue(files.stream().anyMatch(file -> file.getContent().contains("新增Customer")));
        Assertions.assertTrue(files.stream().anyMatch(file -> file.getContent().contains("type=\"textarea\"")));
        Assertions.assertTrue(files.stream().anyMatch(file -> file.getContent().contains("type=\"datetime\"")));
        Assertions.assertTrue(files.stream().anyMatch(file -> file.getContent().contains("el-time-picker")));
        Assertions.assertTrue(files.stream().anyMatch(file -> file.getContent().contains("system:customer:list")));
    }

    @Test
    void shouldBuildZipArchive() {
        when(generatorMetadataService.listColumns("jq_demo_customer")).thenReturn(List.of(
                buildColumn("id", "主键", "bigint", "bigint(20)", "Long", false, true, true),
                buildColumn("customer_name", "客户名称", "varchar", "varchar(64)", "String", false, false, false)
        ));

        GeneratorPreviewServiceImpl service = new GeneratorPreviewServiceImpl(generatorMetadataService, genWriteRecordMapper);
        byte[] zip = service.downloadZip(buildRequest());

        Assertions.assertTrue(zip.length > 0);
        String zipText = new String(zip, StandardCharsets.ISO_8859_1);
        Assertions.assertTrue(zipText.contains("Customer.java"));
    }

    @Test
    void shouldRejectTableWithoutPrimaryKey() {
        when(generatorMetadataService.listColumns("jq_demo_customer")).thenReturn(List.of(
                buildColumn("customer_name", "客户名称", "varchar", "varchar(64)", "String", false, false, false)
        ));

        GeneratorPreviewServiceImpl service = new GeneratorPreviewServiceImpl(generatorMetadataService, genWriteRecordMapper);
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> service.preview(buildRequest()));

        Assertions.assertEquals("当前表缺少主键，暂不支持生成", exception.getMessage());
    }

    @Test
    void shouldTrimRequestFieldsBeforeValidation() {
        when(generatorMetadataService.listColumns("jq_demo_customer")).thenReturn(List.of(
                buildColumn("id", "主键", "bigint", "bigint(20)", "Long", false, true, true),
                buildColumn("customer_name", "客户名称", "varchar", "varchar(64)", "String", false, false, false)
        ));

        GeneratorPreviewServiceImpl service = new GeneratorPreviewServiceImpl(generatorMetadataService, genWriteRecordMapper);
        GenPreviewRequest request = buildRequest();
        request.setTableName("  jq_demo_customer  ");
        request.setModuleName("  demo  ");
        request.setBusinessName("  customers  ");
        request.setClassName("  Customer  ");
        request.setPermPrefix("  system:customer  ");

        List<GenPreviewFile> files = service.preview(request);
        Assertions.assertFalse(files.isEmpty());
    }

    @Test
    void shouldRejectWriteWhenConflictExistsAndOverwriteDisabled() throws IOException {
        when(generatorMetadataService.listColumns("jq_demo_customer")).thenReturn(List.of(
                buildColumn("id", "主键", "bigint", "bigint(20)", "Long", false, true, true),
                buildColumn("customer_name", "客户名称", "varchar", "varchar(64)", "String", false, false, false)
        ));
        GeneratorPreviewServiceImpl service = new GeneratorPreviewServiceImpl(generatorMetadataService, genWriteRecordMapper);
        Path root = Files.createTempDirectory("jq-gen-test-");
        try {
            Path conflictFile = root.resolve("src/main/java/com/jianqing/module/demo/entity/Customer.java");
            Files.createDirectories(conflictFile.getParent());
            Files.writeString(conflictFile, "existing");

            IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                    () -> service.generateToProject(buildRequest(), root.toString(), false));
            Assertions.assertTrue(exception.getMessage().contains("已存在文件"));
        } finally {
            deleteDirectory(root);
        }
    }

    @Test
    void shouldAllowWriteWhenConflictExistsAndOverwriteEnabled() throws IOException {
        when(generatorMetadataService.listColumns("jq_demo_customer")).thenReturn(List.of(
                buildColumn("id", "主键", "bigint", "bigint(20)", "Long", false, true, true),
                buildColumn("customer_name", "客户名称", "varchar", "varchar(64)", "String", false, false, false)
        ));
        GeneratorPreviewServiceImpl service = new GeneratorPreviewServiceImpl(generatorMetadataService, genWriteRecordMapper);
        Path root = Files.createTempDirectory("jq-gen-test-");
        try {
            Path conflictFile = root.resolve("src/main/java/com/jianqing/module/demo/entity/Customer.java");
            Files.createDirectories(conflictFile.getParent());
            Files.writeString(conflictFile, "existing");

            GenWriteResult result = service.generateToProject(buildRequest(), root.toString(), true);
            Assertions.assertFalse(result.getFiles().isEmpty());
            Assertions.assertFalse(result.getMarkerId().isBlank());
            Assertions.assertTrue(Files.readString(conflictFile).contains("public class Customer"));
        } finally {
            deleteDirectory(root);
        }
    }

    @Test
    void shouldDeleteGeneratedFilesByMarker() throws IOException {
        when(generatorMetadataService.listColumns("jq_demo_customer")).thenReturn(List.of(
                buildColumn("id", "主键", "bigint", "bigint(20)", "Long", false, true, true),
                buildColumn("customer_name", "客户名称", "varchar", "varchar(64)", "String", false, false, false)
        ));
        GeneratorPreviewServiceImpl service = new GeneratorPreviewServiceImpl(generatorMetadataService, genWriteRecordMapper);
        Path root = Files.createTempDirectory("jq-gen-test-");
        try {
            GenWriteResult writeResult = service.generateToProject(buildRequest(), root.toString(), true);
            GenDeleteResult deleteResult = service.deleteByMarker(writeResult.getMarkerId(), root.toString());
            Assertions.assertTrue(deleteResult.getDeletedCount() > 0);
            Assertions.assertEquals(0, deleteResult.getMissingCount());
        } finally {
            deleteDirectory(root);
        }
    }

    @Test
    void shouldListConflictFilesBeforeWrite() throws IOException {
        when(generatorMetadataService.listColumns("jq_demo_customer")).thenReturn(List.of(
                buildColumn("id", "主键", "bigint", "bigint(20)", "Long", false, true, true),
                buildColumn("customer_name", "客户名称", "varchar", "varchar(64)", "String", false, false, false)
        ));
        GeneratorPreviewServiceImpl service = new GeneratorPreviewServiceImpl(generatorMetadataService, genWriteRecordMapper);
        Path root = Files.createTempDirectory("jq-gen-test-");
        try {
            Path conflictFile = root.resolve("src/main/java/com/jianqing/module/demo/entity/Customer.java");
            Files.createDirectories(conflictFile.getParent());
            Files.writeString(conflictFile, "existing");

            List<String> conflicts = service.listConflictFiles(buildRequest(), root.toString());
            Assertions.assertEquals(1, conflicts.size());
            Assertions.assertEquals("src/main/java/com/jianqing/module/demo/entity/Customer.java", conflicts.get(0));
        } finally {
            deleteDirectory(root);
        }
    }

    private void deleteDirectory(Path root) throws IOException {
        try (Stream<Path> stream = Files.walk(root)) {
            stream.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }

    private GenPreviewRequest buildRequest() {
        GenPreviewRequest request = new GenPreviewRequest();
        request.setTableName("jq_demo_customer");
        request.setModuleName("demo");
        request.setBusinessName("customers");
        request.setClassName("Customer");
        request.setPermPrefix("system:customer");
        return request;
    }

    private GenColumnMeta buildColumn(String columnName, String comment, String dataType, String columnType,
                                      String javaType, boolean nullable, boolean primaryKey, boolean autoIncrement) {
        GenColumnMeta column = new GenColumnMeta();
        column.setColumnName(columnName);
        column.setColumnComment(comment);
        column.setDataType(dataType);
        column.setColumnType(columnType);
        column.setJavaType(javaType);
        column.setNullable(nullable);
        column.setPrimaryKey(primaryKey);
        column.setAutoIncrement(autoIncrement);
        return column;
    }
}

package com.jianqing.module.dev.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jianqing.module.dev.dto.GenDeleteResult;
import com.jianqing.module.dev.dto.GenColumnMeta;
import com.jianqing.module.dev.dto.GenPreviewFile;
import com.jianqing.module.dev.dto.GenPreviewRequest;
import com.jianqing.module.dev.dto.GenWriteRecordItem;
import com.jianqing.module.dev.dto.GenWriteResult;
import com.jianqing.module.dev.entity.GenWriteRecord;
import com.jianqing.framework.security.SecurityUtils;
import com.jianqing.module.dev.mapper.GenWriteRecordMapper;
import com.jianqing.module.dev.service.GeneratorMetadataService;
import com.jianqing.module.dev.service.GeneratorPreviewService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class GeneratorPreviewServiceImpl implements GeneratorPreviewService {

    private static final String MARKER_DIR = ".jianqing-generator/markers";
    private static final DateTimeFormatter MARKER_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final GeneratorMetadataService generatorMetadataService;
    private final GenWriteRecordMapper genWriteRecordMapper;

    public GeneratorPreviewServiceImpl(GeneratorMetadataService generatorMetadataService,
                                       GenWriteRecordMapper genWriteRecordMapper) {
        this.generatorMetadataService = generatorMetadataService;
        this.genWriteRecordMapper = genWriteRecordMapper;
    }

    @Override
    public List<GenPreviewFile> preview(GenPreviewRequest request) {
        NormalizedRequest normalized = validateRequest(request);
        List<GenColumnMeta> columns = generatorMetadataService.listColumns(normalized.tableName());
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("表字段不存在，无法生成代码");
        }
        GenColumnMeta primaryKey = requirePrimaryKey(columns);
        String basePackage = "com.jianqing.module." + normalized.moduleName();
        String entityPackage = basePackage + ".entity";
        String dtoPackage = basePackage + ".dto";
        String mapperPackage = basePackage + ".mapper";
        String servicePackage = basePackage + ".service";
        String serviceImplPackage = servicePackage + ".impl";
        String controllerPackage = basePackage + ".controller";
        String className = normalized.className();
        String businessName = normalized.businessName();
        String entityVar = lowerFirst(className);
        String saveRequestName = className + "SaveRequest";
        String summaryName = className + "Summary";
        String mapperName = className + "Mapper";
        String serviceName = className + "Service";
        String serviceImplName = className + "ServiceImpl";
        String controllerName = className + "Controller";
        String pkJavaType = primaryKey.getJavaType();
        String rootPath = pathOf("src/main/java", entityPackage);
        List<GenPreviewFile> files = new ArrayList<>();
        files.add(new GenPreviewFile(rootPath + "/" + className + ".java",
                buildEntity(entityPackage, normalized.tableName(), className, columns, primaryKey)));
        files.add(new GenPreviewFile(pathOf("src/main/java", dtoPackage) + "/" + saveRequestName + ".java",
                buildSaveRequest(dtoPackage, saveRequestName, columns, primaryKey)));
        files.add(new GenPreviewFile(pathOf("src/main/java", dtoPackage) + "/" + summaryName + ".java",
                buildSummary(dtoPackage, summaryName, columns, primaryKey)));
        files.add(new GenPreviewFile(pathOf("src/main/java", mapperPackage) + "/" + mapperName + ".java",
                buildMapper(mapperPackage, mapperName, entityPackage, className)));
        files.add(new GenPreviewFile(pathOf("src/main/java", mapperPackage) + "/" + mapperName + ".xml",
                buildMapperXml(mapperPackage, mapperName)));
        files.add(new GenPreviewFile(pathOf("src/main/java", servicePackage) + "/" + serviceName + ".java",
                buildService(servicePackage, serviceName, entityPackage, className, dtoPackage,
                        saveRequestName, summaryName, pkJavaType, businessName)));
        files.add(new GenPreviewFile(pathOf("src/main/java", serviceImplPackage) + "/" + serviceImplName + ".java",
                buildServiceImpl(serviceImplPackage, serviceImplName, servicePackage, serviceName, mapperPackage,
                        mapperName, entityPackage, className, dtoPackage, saveRequestName, summaryName,
                        businessName, entityVar, columns, primaryKey)));
        files.add(new GenPreviewFile(pathOf("src/main/java", controllerPackage) + "/" + controllerName + ".java",
                buildController(controllerPackage, controllerName, servicePackage, serviceName, dtoPackage,
                        saveRequestName, summaryName, normalized.moduleName(), businessName, className, pkJavaType)));
        files.add(new GenPreviewFile("jianqing-admin-web/src/api/" + buildFrontendApiFileName(normalized.moduleName(), businessName),
                buildFrontendApi(normalized.moduleName(), businessName, className)));
        files.add(new GenPreviewFile("jianqing-admin-web/src/views/" + normalized.moduleName() + "/"
                + upperFirst(toCamelCase(businessName)) + "View.vue",
                buildFrontendView(normalized, className, businessName, columns, primaryKey)));
        files.add(new GenPreviewFile("jianqing-admin-web/src/router/snippets/" + businessName + ".js",
                buildFrontendRouteSnippet(normalized, businessName)));
        files.add(new GenPreviewFile("sql/patch/codegen/" + businessName + "_menu.sql",
                buildMenuSql(normalized, businessName)));
        files.sort(Comparator.comparing(GenPreviewFile::getFilePath));
        return files;
    }

    @Override
    public byte[] downloadZip(GenPreviewRequest request) {
        List<GenPreviewFile> files = preview(request);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream, StandardCharsets.UTF_8)) {
            for (GenPreviewFile file : files) {
                zipOutputStream.putNextEntry(new ZipEntry(file.getFilePath()));
                zipOutputStream.write(file.getContent().getBytes(StandardCharsets.UTF_8));
                zipOutputStream.closeEntry();
            }
            zipOutputStream.finish();
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("生成代码压缩包失败", exception);
        }
    }

    @Override
    public List<String> listConflictFiles(GenPreviewRequest request, String projectRoot) {
        List<GenPreviewFile> files = preview(request);
        return findConflictFiles(resolveProjectRoot(projectRoot), files);
    }

    @Override
    public GenWriteResult generateToProject(GenPreviewRequest request, String projectRoot, boolean overwrite) {
        List<GenPreviewFile> files = preview(request);
        projectRoot = resolveProjectRoot(projectRoot);
        List<String> conflictFiles = findConflictFiles(projectRoot, files);
        if (!overwrite && !conflictFiles.isEmpty()) {
            throw new IllegalArgumentException(buildConflictMessage(conflictFiles));
        }
        for (GenPreviewFile file : files) {
            File targetFile = resolveTargetFile(projectRoot, file.getFilePath());
            File parentDir = targetFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    throw new IllegalStateException("无法创建目录: " + parentDir.getAbsolutePath());
                }
            }
            try {
                Files.write(targetFile.toPath(), file.getContent().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new IllegalStateException("写入文件失败: " + targetFile.getAbsolutePath(), e);
            }
        }
        String markerId = buildMarkerId();
        writeMarkerFile(projectRoot, markerId, files);
        NormalizedRequest normalized = validateRequest(request);
        saveWriteRecord(markerId, normalized);
        GenWriteResult result = new GenWriteResult();
        result.setMarkerId(markerId);
        result.setFiles(files);
        return result;
    }

    @Override
    public List<GenWriteRecordItem> listWriteRecords(int limit, String tableName, LocalDateTime startAt, LocalDateTime endAt) {
        if (genWriteRecordMapper == null) {
            return List.of();
        }
        int safeLimit = Math.min(Math.max(1, limit), 100);
        LambdaQueryWrapper<GenWriteRecord> queryWrapper = new LambdaQueryWrapper<GenWriteRecord>()
                .like(tableName != null && !tableName.isBlank(), GenWriteRecord::getTableName, tableName == null ? "" : tableName.trim())
                .ge(startAt != null, GenWriteRecord::getCreatedAt, startAt)
                .le(endAt != null, GenWriteRecord::getCreatedAt, endAt)
                .orderByDesc(GenWriteRecord::getId)
                .last("limit " + safeLimit);
        List<GenWriteRecord> records = genWriteRecordMapper.selectList(queryWrapper);
        return records.stream().map(this::toWriteRecordItem).toList();
    }

    @Override
    public GenDeleteResult deleteByMarker(String markerId, String projectRoot) {
        requirePattern(normalizeIdentifier(markerId), "^[a-zA-Z0-9_-]+$", "标记不合法");
        projectRoot = resolveProjectRoot(projectRoot);
        Path markerPath = markerFilePath(projectRoot, markerId);
        if (!Files.exists(markerPath)) {
            throw new IllegalArgumentException("标记不存在或已删除: " + markerId);
        }
        List<String> filePaths;
        try {
            filePaths = Files.readAllLines(markerPath, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("读取标记文件失败: " + markerPath, exception);
        }
        GenDeleteResult result = new GenDeleteResult();
        result.setMarkerId(markerId);
        List<String> deletedFiles = new ArrayList<>();
        List<String> missingFiles = new ArrayList<>();
        for (String filePath : filePaths) {
            String normalizedPath = normalizeIdentifier(filePath);
            if (normalizedPath.isEmpty()) {
                continue;
            }
            Path targetPath = resolveTargetFile(projectRoot, normalizedPath).toPath();
            try {
                if (Files.deleteIfExists(targetPath)) {
                    deletedFiles.add(normalizedPath);
                } else {
                    missingFiles.add(normalizedPath);
                }
            } catch (IOException exception) {
                throw new IllegalStateException("删除文件失败: " + normalizedPath, exception);
            }
        }
        try {
            Files.deleteIfExists(markerPath);
        } catch (IOException exception) {
            throw new IllegalStateException("删除标记文件失败: " + markerPath, exception);
        }
        result.setDeletedFiles(deletedFiles);
        result.setDeletedCount(deletedFiles.size());
        result.setMissingFiles(missingFiles);
        result.setMissingCount(missingFiles.size());
        if (genWriteRecordMapper != null) {
            genWriteRecordMapper.delete(new LambdaQueryWrapper<GenWriteRecord>().eq(GenWriteRecord::getMarkerId, markerId));
        }
        return result;
    }

    private void saveWriteRecord(String markerId, NormalizedRequest request) {
        if (genWriteRecordMapper == null) {
            return;
        }
        GenWriteRecord record = new GenWriteRecord();
        record.setMarkerId(markerId);
        record.setTableName(request.tableName());
        record.setModuleName(request.moduleName());
        record.setBusinessName(request.businessName());
        record.setClassName(request.className());
        record.setPermPrefix(request.permPrefix());
        record.setUsername(SecurityUtils.currentUsername());
        record.setCreatedAt(LocalDateTime.now());
        genWriteRecordMapper.insert(record);
    }

    private GenWriteRecordItem toWriteRecordItem(GenWriteRecord record) {
        GenWriteRecordItem item = new GenWriteRecordItem();
        item.setMarkerId(record.getMarkerId());
        item.setTableName(record.getTableName());
        item.setModuleName(record.getModuleName());
        item.setBusinessName(record.getBusinessName());
        item.setClassName(record.getClassName());
        item.setPermPrefix(record.getPermPrefix());
        item.setUsername(record.getUsername());
        item.setCreatedAt(record.getCreatedAt());
        return item;
    }

    private List<String> findConflictFiles(String projectRoot, List<GenPreviewFile> files) {
        List<String> conflicts = new ArrayList<>();
        for (GenPreviewFile file : files) {
            File targetFile = resolveTargetFile(projectRoot, file.getFilePath());
            if (targetFile.exists()) {
                conflicts.add(file.getFilePath());
            }
        }
        return conflicts;
    }

    private String resolveProjectRoot(String projectRoot) {
        if (projectRoot == null || projectRoot.isBlank()) {
            return System.getProperty("user.dir");
        }
        return projectRoot;
    }

    private String buildMarkerId() {
        String timePart = LocalDateTime.now().format(MARKER_TIME_FORMATTER);
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        return "gen-" + timePart + "-" + randomPart;
    }

    private void writeMarkerFile(String projectRoot, String markerId, List<GenPreviewFile> files) {
        Path markerPath = markerFilePath(projectRoot, markerId);
        Path markerDir = markerPath.getParent();
        if (markerDir != null && !Files.exists(markerDir)) {
            try {
                Files.createDirectories(markerDir);
            } catch (IOException exception) {
                throw new IllegalStateException("创建标记目录失败: " + markerDir, exception);
            }
        }
        List<String> lines = new ArrayList<>();
        for (GenPreviewFile file : files) {
            lines.add(file.getFilePath());
        }
        try {
            Files.write(markerPath, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("写入标记文件失败: " + markerPath, exception);
        }
    }

    private Path markerFilePath(String projectRoot, String markerId) {
        return Paths.get(projectRoot, MARKER_DIR, markerId + ".txt");
    }

    private File resolveTargetFile(String projectRoot, String relativePath) {
        Path baseRoot = resolveBaseRoot(projectRoot, relativePath);
        Path targetPath = baseRoot.resolve(relativePath).normalize();
        if (!targetPath.startsWith(baseRoot)) {
            throw new IllegalArgumentException("标记文件存在非法路径: " + relativePath);
        }
        return targetPath.toFile();
    }

    private Path resolveBaseRoot(String projectRoot, String relativePath) {
        Path rootPath = Paths.get(projectRoot).toAbsolutePath().normalize();
        Path workspaceRoot = detectWorkspaceRoot(rootPath);
        Path backendRoot = workspaceRoot.resolve("jianqing-backend").normalize();
        if (relativePath.startsWith("jianqing-admin-web/") || relativePath.startsWith("sql/")) {
            return workspaceRoot;
        }
        if (relativePath.startsWith("src/") && Files.exists(backendRoot)) {
            return backendRoot;
        }
        return rootPath;
    }

    private Path detectWorkspaceRoot(Path rootPath) {
        if (hasWorkspaceChildren(rootPath)) {
            return rootPath;
        }
        Path parent = rootPath.getParent();
        if (parent != null && hasWorkspaceChildren(parent)) {
            return parent;
        }
        return rootPath;
    }

    private boolean hasWorkspaceChildren(Path path) {
        return Files.exists(path.resolve("jianqing-backend"))
                && Files.exists(path.resolve("jianqing-admin-web"));
    }

    private String buildConflictMessage(List<String> conflictFiles) {
        int previewSize = Math.min(conflictFiles.size(), 5);
        String previewList = String.join("、", conflictFiles.subList(0, previewSize));
        if (conflictFiles.size() > previewSize) {
            return "检测到 " + conflictFiles.size() + " 个已存在文件，写入已中止。示例：" + previewList
                    + "。如需覆盖请传 overwrite=true";
        }
        return "检测到已存在文件，写入已中止：" + previewList + "。如需覆盖请传 overwrite=true";
    }

    private NormalizedRequest validateRequest(GenPreviewRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("生成参数不能为空");
        }
        String tableName = normalizeIdentifier(request.getTableName());
        String moduleName = normalizeIdentifier(request.getModuleName());
        String businessName = normalizeBusinessName(request.getBusinessName());
        String className = normalizeClassName(request.getClassName());
        String permPrefix = normalizePermPrefix(request.getPermPrefix());

        requirePattern(tableName, "^[a-zA-Z0-9_]+$", "表名不合法");
        requirePattern(moduleName, "^[a-z][a-z0-9_]*$", "模块名不合法");
        requirePattern(businessName, "^[a-z][a-z0-9_-]*$", "业务名不合法");
        requirePattern(className, "^[A-Z][A-Za-z0-9]*$", "实体名不合法");
        requirePattern(permPrefix, "^[a-z][a-z0-9:-]*$", "权限前缀不合法");
        return new NormalizedRequest(tableName, moduleName, businessName, className, permPrefix);
    }

    private void requirePattern(String value, String pattern, String message) {
        if (value == null || !value.matches(pattern)) {
            throw new IllegalArgumentException(message);
        }
    }

    private GenColumnMeta requirePrimaryKey(List<GenColumnMeta> columns) {
        return columns.stream()
                .filter(column -> Boolean.TRUE.equals(column.getPrimaryKey()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("当前表缺少主键，暂不支持生成"));
    }

    private String buildEntity(String packageName, String tableName, String className,
                               List<GenColumnMeta> columns, GenColumnMeta primaryKey) {
        Set<String> imports = new LinkedHashSet<>();
        imports.add("com.baomidou.mybatisplus.annotation.TableName");
        imports.add("com.baomidou.mybatisplus.annotation.TableId");
        if (Boolean.TRUE.equals(primaryKey.getAutoIncrement())) {
            imports.add("com.baomidou.mybatisplus.annotation.IdType");
        }
        if (columns.stream().anyMatch(this::needsTableField)) {
            imports.add("com.baomidou.mybatisplus.annotation.TableField");
        }
        collectJavaTypeImports(imports, columns);

        StringBuilder builder = new StringBuilder();
        appendPackageAndImports(builder, packageName, imports);
        builder.append("@TableName(\"").append(tableName).append("\")\n");
        builder.append("public class ").append(className).append(" {\n\n");
        for (GenColumnMeta column : columns) {
            appendEntityField(builder, column, primaryKey);
        }
        for (GenColumnMeta column : columns) {
            String fieldName = toCamelCase(column.getColumnName());
            String javaType = column.getJavaType();
            String methodSuffix = upperFirst(fieldName);
            builder.append("    public ").append(javaType).append(" get").append(methodSuffix).append("() {\n")
                    .append("        return ").append(fieldName).append(";\n")
                    .append("    }\n\n")
                    .append("    public void set").append(methodSuffix).append("(")
                    .append(javaType).append(" ").append(fieldName).append(") {\n")
                    .append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n")
                    .append("    }\n\n");
        }
        builder.append("}\n");
        return builder.toString();
    }

    private String buildSaveRequest(String packageName, String saveRequestName,
                                    List<GenColumnMeta> columns, GenColumnMeta primaryKey) {
        List<GenColumnMeta> requestColumns = filterEditableColumns(columns, primaryKey);
        Set<String> imports = new LinkedHashSet<>();
        boolean needNotBlank = false;
        boolean needNotNull = false;
        boolean needSize = false;
        boolean needPattern = false;
        boolean needMinMax = false;
        collectJavaTypeImports(imports, requestColumns);
        for (GenColumnMeta column : requestColumns) {
            if (!Boolean.TRUE.equals(column.getNullable())) {
                if (Objects.equals(column.getJavaType(), String.class.getSimpleName())) {
                    needNotBlank = true;
                } else {
                    needNotNull = true;
                }
            }
            if (Objects.equals(column.getJavaType(), String.class.getSimpleName())) {
                String columnType = column.getColumnType() != null ? column.getColumnType().toLowerCase() : "";
                if (columnType.contains("varchar") || columnType.contains("char")) {
                    String lengthMatch = columnType.replaceAll(".*\\((\\d+)\\).*", "$1");
                    if (!lengthMatch.equals(columnType) && Integer.parseInt(lengthMatch) > 255) {
                        needSize = true;
                    }
                }
                String comment = column.getColumnComment() != null ? column.getColumnComment().toLowerCase() : "";
                if (comment.contains("邮箱") || comment.contains("email")) {
                    needPattern = true;
                } else if (comment.contains("手机") || comment.contains("电话")) {
                    needPattern = true;
                }
            }
            if (isNumericType(column.getJavaType())) {
                needMinMax = true;
            }
        }
        if (needNotBlank) {
            imports.add("jakarta.validation.constraints.NotBlank");
        }
        if (needNotNull) {
            imports.add("jakarta.validation.constraints.NotNull");
        }
        if (needSize) {
            imports.add("jakarta.validation.constraints.Size");
        }
        if (needPattern) {
            imports.add("jakarta.validation.constraints.Pattern");
        }
        if (needMinMax) {
            imports.add("jakarta.validation.constraints.Min");
            imports.add("jakarta.validation.constraints.Max");
        }

        StringBuilder builder = new StringBuilder();
        appendPackageAndImports(builder, packageName, imports);
        builder.append("public class ").append(saveRequestName).append(" {\n\n");
        for (GenColumnMeta column : requestColumns) {
            appendValidation(builder, column);
            builder.append("    private ").append(column.getJavaType()).append(" ")
                    .append(toCamelCase(column.getColumnName())).append(";\n");
        }
        builder.append("\n");
        for (GenColumnMeta column : requestColumns) {
            appendGetterSetter(builder, column.getJavaType(), toCamelCase(column.getColumnName()));
        }
        builder.append("}\n");
        return builder.toString();
    }

    private String buildSummary(String packageName, String summaryName,
                                List<GenColumnMeta> columns, GenColumnMeta primaryKey) {
        List<GenColumnMeta> summaryColumns = filterSummaryColumns(columns, primaryKey);
        Set<String> imports = new LinkedHashSet<>();
        collectJavaTypeImports(imports, summaryColumns);
        StringBuilder builder = new StringBuilder();
        appendPackageAndImports(builder, packageName, imports);
        builder.append("public record ").append(summaryName).append("(");
        for (int index = 0; index < summaryColumns.size(); index++) {
            GenColumnMeta column = summaryColumns.get(index);
            if (index > 0) {
                builder.append(", ");
            }
            builder.append(column.getJavaType()).append(" ").append(toCamelCase(column.getColumnName()));
        }
        builder.append(") {\n}\n");
        return builder.toString();
    }

    private String buildMapper(String packageName, String mapperName, String entityPackage, String className) {
        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(packageName).append(";\n\n")
                .append("import com.baomidou.mybatisplus.core.mapper.BaseMapper;\n")
                .append("import ").append(entityPackage).append('.').append(className).append(";\n")
                .append("import org.apache.ibatis.annotations.Mapper;\n\n")
                .append("@Mapper\n")
                .append("public interface ").append(mapperName).append(" extends BaseMapper<")
                .append(className).append("> {\n")
                .append("}\n");
        return builder.toString();
    }

    private String buildMapperXml(String packageName, String mapperName) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
                + "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n"
                + "<mapper namespace=\"" + packageName + "." + mapperName + "\">\n"
                + "</mapper>\n";
    }

    private String buildService(String packageName, String serviceName, String entityPackage, String className,
                                String dtoPackage, String saveRequestName, String summaryName,
                                String pkJavaType, String businessName) {
        String listMethod = "list" + upperFirst(toCamelCase(businessName));
        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(packageName).append(";\n\n")
                .append("import com.baomidou.mybatisplus.extension.plugins.pagination.Page;\n")
                .append("import com.baomidou.mybatisplus.extension.service.IService;\n")
                .append("import ").append(dtoPackage).append('.').append(saveRequestName).append(";\n")
                .append("import ").append(dtoPackage).append('.').append(summaryName).append(";\n")
                .append("import ").append(entityPackage).append('.').append(className).append(";\n\n")
                .append("import java.util.List;\n\n")
                .append("public interface ").append(serviceName).append(" extends IService<").append(className).append("> {\n\n")
                .append("    List<").append(summaryName).append("> ").append(listMethod).append("();\n\n")
                .append("    Page<").append(summaryName).append("> listPage(int pageNo, int pageSize);\n\n")
                .append("    long count();\n\n")
                .append("    long countEnabled();\n\n")
                .append("    ").append(summaryName).append(" create").append(className).append("(")
                .append(saveRequestName).append(" request);\n\n")
                .append("    ").append(summaryName).append(" update").append(className).append("(")
                .append(pkJavaType).append(" id, ").append(saveRequestName).append(" request);\n\n")
                .append("    void delete").append(className).append("(").append(pkJavaType).append(" id);\n")
                .append("}\n");
        return builder.toString();
    }

    private String buildServiceImpl(String packageName, String serviceImplName, String servicePackage,
                                    String serviceName, String mapperPackage, String mapperName,
                                    String entityPackage, String className, String dtoPackage,
                                    String saveRequestName, String summaryName, String businessName,
                                    String entityVar, List<GenColumnMeta> columns, GenColumnMeta primaryKey) {
        Set<String> imports = new LinkedHashSet<>();
        imports.add("com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper");
        imports.add("com.baomidou.mybatisplus.extension.plugins.pagination.Page");
        imports.add("com.baomidou.mybatisplus.extension.service.impl.ServiceImpl");
        imports.add(dtoPackage + "." + saveRequestName);
        imports.add(dtoPackage + "." + summaryName);
        imports.add(entityPackage + "." + className);
        imports.add(mapperPackage + "." + mapperName);
        imports.add(servicePackage + "." + serviceName);
        imports.add("org.springframework.stereotype.Service");
        imports.add("org.springframework.transaction.annotation.Transactional");
        imports.add("java.util.List");

        String packageHeader = "package " + packageName + ";\n\n";
        StringBuilder builder = new StringBuilder(packageHeader);
        for (String importName : imports) {
            builder.append("import ").append(importName).append(";\n");
        }
        builder.append("\n@Service\n")
                .append("public class ").append(serviceImplName).append(" extends ServiceImpl<")
                .append(mapperName).append(", ").append(className).append("> implements ")
                .append(serviceName).append(" {\n\n");

        String listMethod = "list" + upperFirst(toCamelCase(businessName));
        String pkType = primaryKey.getJavaType();
        String pkField = toCamelCase(primaryKey.getColumnName());
        String getterPk = getterMethodName(pkField, pkType);

        builder.append("    @Override\n")
                .append("    public List<").append(summaryName).append("> ").append(listMethod).append("() {\n")
                .append("        return baseMapper.selectList(new LambdaQueryWrapper<").append(className).append(">()")
                .append(buildListWrapper(columns, className))
                .append("                .orderByAsc(").append(className).append("::").append(getterPk).append("))\n")
                .append("                .stream().map(this::toSummary).toList();\n")
                .append("    }\n\n");

        builder.append("    @Override\n")
                .append("    public Page<").append(summaryName).append("> listPage(int pageNo, int pageSize) {\n")
                .append("        Page<").append(className).append("> page = new Page<>(pageNo, pageSize);\n")
                .append("        return baseMapper.selectPage(page, new LambdaQueryWrapper<").append(className).append(">()")
                .append(buildListWrapper(columns, className))
                .append("                .orderByDesc(").append(className).append("::getCreatedAt))\n")
                .append("                .convert(this::toSummary);\n")
                .append("    }\n\n");

        builder.append("    @Override\n")
                .append("    public long count() {\n")
                .append("        return count(new LambdaQueryWrapper<").append(className).append(">()")
                .append(buildListWrapper(columns, className)).append(");\n")
                .append("    }\n\n");

        if (hasStatusColumn(columns)) {
            builder.append("    @Override\n")
                    .append("    public long countEnabled() {\n")
                    .append("        return count(new LambdaQueryWrapper<").append(className).append(">()\n")
                    .append("                .eq(").append(className).append("::getStatus, 1)")
                    .append(buildListWrapper(columns, className)).append(");\n")
                    .append("    }\n\n");
        }

        builder.append("    @Override\n")
                .append("    @Transactional(rollbackFor = Exception.class)\n")
                .append("    public ").append(summaryName).append(" create").append(className).append("(")
                .append(saveRequestName).append(" request) {\n")
                .append("        ").append(className).append(' ').append(entityVar).append(" = new ")
                .append(className).append("();\n")
                .append(buildFillEntity(columns, primaryKey, entityVar, "request", true))
                .append("        baseMapper.insert(").append(entityVar).append(");\n")
                .append("        return toSummary(").append(entityVar).append(");\n")
                .append("    }\n\n");

        builder.append("    @Override\n")
                .append("    @Transactional(rollbackFor = Exception.class)\n")
                .append("    public ").append(summaryName).append(" update").append(className).append("(")
                .append(pkType).append(" id, ").append(saveRequestName).append(" request) {\n")
                .append("        ").append(className).append(' ').append(entityVar).append(" = getOrThrow(id);\n")
                .append(buildFillEntity(columns, primaryKey, entityVar, "request", false))
                .append("        baseMapper.updateById(").append(entityVar).append(");\n")
                .append("        return toSummary(").append(entityVar).append(");\n")
                .append("    }\n\n");

        builder.append("    @Override\n")
                .append("    @Transactional(rollbackFor = Exception.class)\n")
                .append("    public void delete").append(className).append("(").append(pkType).append(" id) {\n")
                .append("        ").append(className).append(' ').append(entityVar).append(" = getOrThrow(id);\n")
                .append(buildDeleteLogic(columns, entityVar, primaryKey))
                .append("    }\n\n");

        builder.append("    private ").append(summaryName).append(" toSummary(").append(className).append(' ').append(entityVar)
                .append(") {\n")
                .append("        return new ").append(summaryName).append('(');
        List<GenColumnMeta> summaryColumns = filterSummaryColumns(columns, primaryKey);
        for (int index = 0; index < summaryColumns.size(); index++) {
            if (index > 0) {
                builder.append(", ");
            }
            String fieldName = toCamelCase(summaryColumns.get(index).getColumnName());
            builder.append(entityVar).append('.').append(getterMethodName(fieldName, summaryColumns.get(index).getJavaType())).append("()");
        }
        builder.append(");\n")
                .append("    }\n\n")
                .append("    private ").append(className).append(" getOrThrow(").append(pkType).append(" id) {\n")
                .append("        ").append(className).append(' ').append(entityVar)
                .append(" = baseMapper.selectOne(new LambdaQueryWrapper<").append(className).append(">()\n")
                .append("                .eq(").append(className).append("::").append(getterPk).append(", id)")
                .append(buildGetOrThrowWrapper(columns, className))
                .append("                .last(\"limit 1\"));\n")
                .append("        if (").append(entityVar).append(" == null) {\n")
                .append("            throw new IllegalArgumentException(\"").append(className).append("不存在\");\n")
                .append("        }\n")
                .append("        return ").append(entityVar).append(";\n")
                .append("    }\n")
                .append("}\n");
        return builder.toString();
    }

    private String buildController(String packageName, String controllerName, String servicePackage, String serviceName,
                                   String dtoPackage, String saveRequestName, String summaryName,
                                   String moduleName, String businessName, String className, String pkJavaType) {
        String listMethod = "list" + upperFirst(toCamelCase(businessName));
        String requestPath = normalizeBusinessName(moduleName) + "/" + normalizeBusinessName(businessName);
        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(packageName).append(";\n\n")
                .append("import com.jianqing.common.api.ApiResponse;\n")
                .append("import com.baomidou.mybatisplus.extension.plugins.pagination.Page;\n")
                .append("import ").append(dtoPackage).append('.').append(saveRequestName).append(";\n")
                .append("import ").append(dtoPackage).append('.').append(summaryName).append(";\n")
                .append("import ").append(servicePackage).append('.').append(serviceName).append(";\n")
                .append("import jakarta.validation.Valid;\n")
                .append("import org.springframework.web.bind.annotation.GetMapping;\n")
                .append("import org.springframework.web.bind.annotation.PathVariable;\n")
                .append("import org.springframework.web.bind.annotation.PostMapping;\n")
                .append("import org.springframework.web.bind.annotation.RequestBody;\n")
                .append("import org.springframework.web.bind.annotation.RequestMapping;\n")
                .append("import org.springframework.web.bind.annotation.RequestParam;\n")
                .append("import org.springframework.web.bind.annotation.RestController;\n\n")
                .append("import java.util.List;\n\n")
                .append("@RestController\n")
                .append("@RequestMapping(\"/api/").append(requestPath).append("\")\n")
                .append("public class ").append(controllerName).append(" {\n\n")
                .append("    private final ").append(serviceName).append(' ').append(lowerFirst(serviceName)).append(";\n\n")
                .append("    public ").append(controllerName).append("(").append(serviceName).append(' ')
                .append(lowerFirst(serviceName)).append(") {\n")
                .append("        this.").append(lowerFirst(serviceName)).append(" = ").append(lowerFirst(serviceName)).append(";\n")
                .append("    }\n\n")
                .append("    @GetMapping\n")
                .append("    public ApiResponse<List<").append(summaryName).append(">> list() {\n")
                .append("        return ApiResponse.success(").append(lowerFirst(serviceName)).append('.').append(listMethod).append("());\n")
                .append("    }\n\n")
                .append("    @GetMapping(\"/page\")\n")
                .append("    public ApiResponse<Page<").append(summaryName).append(">> page(\n")
                .append("            @RequestParam(defaultValue = \"1\") int pageNo,\n")
                .append("            @RequestParam(defaultValue = \"10\") int pageSize) {\n")
                .append("        return ApiResponse.success(").append(lowerFirst(serviceName))
                .append(".listPage(pageNo, pageSize));\n")
                .append("    }\n\n")
                .append("    @GetMapping(\"/count\")\n")
                .append("    public ApiResponse<Long> count() {\n")
                .append("        return ApiResponse.success(").append(lowerFirst(serviceName)).append(".count());\n")
                .append("    }\n\n")
                .append("    @GetMapping(\"/count/enabled\")\n")
                .append("    public ApiResponse<Long> countEnabled() {\n")
                .append("        return ApiResponse.success(").append(lowerFirst(serviceName)).append(".countEnabled());\n")
                .append("    }\n\n")
                .append("    @PostMapping\n")
                .append("    public ApiResponse<").append(summaryName).append("> create(@Valid @RequestBody ")
                .append(saveRequestName).append(" request) {\n")
                .append("        return ApiResponse.success(").append(lowerFirst(serviceName)).append(".create")
                .append(className).append("(request));\n")
                .append("    }\n\n")
                .append("    @PostMapping(\"/{id}/update\")\n")
                .append("    public ApiResponse<").append(summaryName).append("> update(@PathVariable ")
                .append(pkJavaType).append(" id, @Valid @RequestBody ").append(saveRequestName).append(" request) {\n")
                .append("        return ApiResponse.success(").append(lowerFirst(serviceName)).append(".update")
                .append(className).append("(id, request));\n")
                .append("    }\n\n")
                .append("    @PostMapping(\"/{id}/delete\")\n")
                .append("    public ApiResponse<Void> delete(@PathVariable ").append(pkJavaType).append(" id) {\n")
                .append("        ").append(lowerFirst(serviceName)).append(".delete").append(className).append("(id);\n")
                .append("        return ApiResponse.success(null);\n")
                .append("    }\n")
                .append("}\n");
        return builder.toString();
    }

    private String buildMenuSql(NormalizedRequest request, String businessName) {
        String menuName = upperFirst(toCamelCase(businessName));
        return "INSERT INTO jq_sys_menu\n"
                + "(parent_id, menu_type, menu_name, route_path, component, perms, icon, sort_no, visible, status, is_deleted, created_by, updated_by)\n"
                + "VALUES\n"
                + "(1, 2, '" + menuName + "管理', '" + businessName + "', '" + request.moduleName() + "/"
                + businessName + "/index', '" + request.permPrefix() + ":list', 'Grid', 99, 1, 1, 0, 1, 1),\n"
                + "(LAST_INSERT_ID(), 3, '查询', '', '', '" + request.permPrefix() + ":query', '', 1, 1, 1, 0, 1, 1),\n"
                + "(LAST_INSERT_ID(), 3, '新增', '', '', '" + request.permPrefix() + ":add', '', 2, 1, 1, 0, 1, 1),\n"
                + "(LAST_INSERT_ID(), 3, '修改', '', '', '" + request.permPrefix() + ":edit', '', 3, 1, 1, 0, 1, 1),\n"
                + "(LAST_INSERT_ID(), 3, '删除', '', '', '" + request.permPrefix() + ":remove', '', 4, 1, 1, 0, 1, 1);\n";
    }

    private String buildFrontendApi(String moduleName, String businessName, String className) {
        String apiName = upperFirst(toCamelCase(businessName));
        String singularName = className;
        String lowerSingularName = lowerFirst(className);
        String resourcePath = "/" + moduleName + "/" + businessName;
        StringBuilder builder = new StringBuilder();
        builder.append("import { http } from './http';\n\n")
                .append("export function fetch").append(apiName).append("() {\n")
                .append("  return http.get('").append(resourcePath).append("');\n")
                .append("}\n\n")
                .append("export function create").append(singularName).append("(payload) {\n")
                .append("  return http.post('").append(resourcePath).append("', payload);\n")
                .append("}\n\n")
                .append("export function update").append(singularName).append("(id, payload) {\n")
                .append("  return http.post(`").append(resourcePath).append("/${id}/update`, payload);\n")
                .append("}\n\n")
                .append("export function delete").append(singularName).append("(id) {\n")
                .append("  return http.post(`").append(resourcePath).append("/${id}/delete`);\n")
                .append("}\n");
        return builder.toString().replace("lowerSingularName", lowerSingularName);
    }

    private String buildFrontendView(NormalizedRequest request, String className, String businessName,
                                     List<GenColumnMeta> columns, GenColumnMeta primaryKey) {
        List<GenColumnMeta> summaryColumns = filterSummaryColumns(columns, primaryKey);
        List<GenColumnMeta> editableColumns = filterEditableColumns(columns, primaryKey);
        List<GenColumnMeta> searchableColumns = summaryColumns.stream()
                .filter(column -> Objects.equals(column.getJavaType(), String.class.getSimpleName()))
                .limit(2)
                .toList();
        String apiFileName = buildFrontendApiFileName(request.moduleName(), businessName).replace(".js", "");
        String apiImportName = upperFirst(toCamelCase(businessName));
        String viewName = upperFirst(toCamelCase(businessName)) + "View";
        StringBuilder builder = new StringBuilder();
        builder.append("<template>\n")
                .append("  <el-card class=\"jq-glass-card jq-list-page\" shadow=\"never\">\n")
                .append("    <template #header>\n")
                .append("      <ListPageHeader :refresh-loading=\"pageLoading\" @search=\"handleSearch\" @reset=\"handleReset\" @refresh=\"handleRefresh\">\n")
                .append("        <template #filters>\n");
        if (!searchableColumns.isEmpty()) {
            builder.append("          <el-input v-model=\"keywordInput\" clearable placeholder=\"搜索")
                    .append(buildSearchPlaceholder(searchableColumns))
                    .append("\" class=\"jq-toolbar-field\" @keyup.enter=\"handleSearch\" />\n");
        }
        builder.append("        </template>\n")
                .append("        <template #actions>\n")
                .append("          <el-button v-if=\"canAdd\" type=\"primary\" :icon=\"Plus\" @click=\"openCreate\">新增")
                .append(className).append("</el-button>\n")
                .append("        </template>\n")
                .append("      </ListPageHeader>\n")
                .append("    </template>\n")
                .append("    <div class=\"jq-table-panel\">\n")
                .append("      <el-table :data=\"pagedRows\" stripe :empty-text=\"tableEmptyText\" height=\"100%\">\n");
        for (GenColumnMeta column : summaryColumns) {
            appendFrontendTableColumn(builder, column);
        }
        builder.append("        <el-table-column label=\"操作\" width=\"180\" fixed=\"right\">\n")
                .append("          <template #default=\"scope\">\n")
                .append("            <el-button v-if=\"canEdit\" type=\"primary\" link :disabled=\"submitLoading || deleteLoadingId === scope.row.")
                .append(toCamelCase(primaryKey.getColumnName())).append("\" @click=\"openEdit(scope.row)\">编辑</el-button>\n")
                .append("            <el-button v-if=\"canDelete\" type=\"danger\" link :loading=\"deleteLoadingId === scope.row.")
                .append(toCamelCase(primaryKey.getColumnName())).append("\" :disabled=\"submitLoading\" @click=\"handleDelete(scope.row)\">删除</el-button>\n")
                .append("          </template>\n")
                .append("        </el-table-column>\n")
                .append("      </el-table>\n")
                .append("    </div>\n")
                .append("    <div class=\"jq-pagination-panel\">\n")
                .append("      <el-pagination class=\"table-pagination\" background layout=\"total, sizes, prev, pager, next\" :total=\"total\" :page-sizes=\"pageSizes\" v-model:current-page=\"pageNo\" v-model:page-size=\"pageSize\" />\n")
                .append("    </div>\n")
                .append("  </el-card>\n\n")
                .append("  <el-dialog v-model=\"dialogVisible\" :title=\"isEdit ? '编辑").append(className).append("' : '新增").append(className).append("'\" width=\"560px\" append-to-body>\n")
                .append("    <el-form label-width=\"100px\">\n");
        for (GenColumnMeta column : editableColumns) {
            appendFrontendFormItem(builder, column);
        }
        builder.append("    </el-form>\n")
                .append("    <template #footer>\n")
                .append("      <el-button :disabled=\"submitLoading\" @click=\"dialogVisible = false\">取消</el-button>\n")
                .append("      <el-button type=\"primary\" :loading=\"submitLoading\" @click=\"handleSubmit\">保存</el-button>\n")
                .append("    </template>\n")
                .append("  </el-dialog>\n")
                .append("</template>\n\n")
                .append("<script setup>\n")
                .append("import { computed, ref } from 'vue';\n")
                .append("import { Plus } from '@element-plus/icons-vue';\n")
                .append("import ListPageHeader from '../../components/ListPageHeader.vue';\n")
                .append("import { DEFAULT_LIST_PAGE_SIZE, PAGE_SIZE_OPTIONS")
                .append(hasStatusColumn(columns) ? ", STATUS_DISABLED, STATUS_ENABLED" : "")
                .append(" } from '../../constants/app';\n")
                .append("import { create").append(className).append(", delete").append(className).append(", fetch").append(apiImportName)
                .append(", update").append(className).append(" } from '../../api/").append(apiFileName).append("';\n")
                .append("import { useTableFeedback } from '../../composables/useAsyncState';\n")
                .append("import { useEntityDeleteAction } from '../../composables/useEntityDeleteAction';\n")
                .append("import { useEntityDialogForm } from '../../composables/useEntityDialogForm';\n")
                .append("import { useEntitySubmitAction } from '../../composables/useEntitySubmitAction';\n")
                .append("import { usePermissionGroup } from '../../composables/usePermissions';\n")
                .append("import { usePageInitializer } from '../../composables/usePageInitializer';\n")
                .append("import { useSystemListPage } from '../../composables/useSystemListPage';\n");
        if (hasStatusColumn(columns)) {
            builder.append("import StatusTag from '../../components/StatusTag.vue';\n");
        }
        builder.append("\nconst rows = ref([]);\n\n")
                .append("const { canAdd, canEdit, canDelete } = usePermissionGroup({\n")
                .append("  canAdd: '").append(request.permPrefix()).append(":add',\n")
                .append("  canEdit: '").append(request.permPrefix()).append(":edit',\n")
                .append("  canDelete: '").append(request.permPrefix()).append(":remove'\n")
                .append("});\n\n")
                .append("const { keywordInput, pageNo, pageSize, pageSizes, pagedRows, total, handleSearch, handleReset, handleRefresh } = useSystemListPage({\n")
                .append("  defaultPageSize: DEFAULT_LIST_PAGE_SIZE,\n")
                .append("  pageSizes: PAGE_SIZE_OPTIONS,\n")
                .append("  loadData,\n")
                .append("  filterRows: ({ keyword }) => {\n")
                .append("    const query = keyword.trim().toLowerCase();\n")
                .append("    if (!query) {\n")
                .append("      return rows.value;\n")
                .append("    }\n")
                .append(buildFrontendFilterFunction(searchableColumns))
                .append("  }\n")
                .append("});\n\n")
                .append("const tableFeedback = useTableFeedback();\n")
                .append("const pageLoading = tableFeedback.loading;\n")
                .append("const tableEmptyText = tableFeedback.emptyText;\n\n")
                .append("const { dialogVisible, isEdit, editingId, form, openCreate, openEdit, closeDialog } = useEntityDialogForm({\n")
                .append("  createForm: () => ({")
                .append(buildFrontendCreateForm(editableColumns))
                .append("}),\n")
                .append("  mapForm: (row) => ({")
                .append(buildFrontendMapForm(editableColumns))
                .append("})\n")
                .append("});\n\n")
                .append("const { deleteLoadingId, handleDelete } = useEntityDeleteAction({\n")
                .append("  entityLabel: '").append(className).append("',\n")
                .append("  getRowLabel: (row) => String(row.").append(toCamelCase(primaryKey.getColumnName())).append("),\n")
                .append("  deleteEntity: delete").append(className).append(",\n")
                .append("  reloadData: loadData,\n")
                .append("  successText: '删除").append(className).append("'\n")
                .append("});\n\n")
                .append("const { submitLoading, handleSubmit: runSubmit } = useEntitySubmitAction({\n")
                .append("  closeDialog,\n")
                .append("  reloadData: loadData,\n")
                .append("  getSuccessText: () => (isEdit.value ? '更新").append(className).append("' : '新增").append(className).append("')\n")
                .append("});\n\n")
                .append("async function loadData() {\n")
                .append("  await tableFeedback.run(async () => {\n")
                .append("    rows.value = await fetch").append(apiImportName).append("();\n")
                .append("  });\n")
                .append("}\n\n")
                .append("async function handleSubmit() {\n")
                .append("  await runSubmit(async () => {\n")
                .append("    if (isEdit.value) {\n")
                .append("      await update").append(className).append("(editingId.value, form.value);\n")
                .append("    } else {\n")
                .append("      await create").append(className).append("(form.value);\n")
                .append("    }\n")
                .append("  });\n")
                .append("}\n\n")
                .append("usePageInitializer(async () => {\n")
                .append("  await loadData();\n")
                .append("  handleSearch();\n")
                .append("});\n")
                .append("</script>\n\n")
                .append("<style scoped>\n")
                .append(".table-pagination {\n  margin-top: 0;\n  justify-content: flex-end;\n}\n")
                .append("</style>\n");
        return builder.toString();
    }

    private String buildFrontendRouteSnippet(NormalizedRequest request, String businessName) {
        String viewName = upperFirst(toCamelCase(businessName)) + "View";
        return "// Add this route into src/router/index.js\n"
                + "const " + viewName + " = () => import('../views/" + request.moduleName() + "/" + viewName + ".vue');\n\n"
                + "// children.push({ path: '/" + request.moduleName() + "/" + businessName + "', component: "
                + viewName + ", meta: { perm: '" + request.permPrefix() + ":list' } });\n";
    }

    private String buildFrontendApiFileName(String moduleName, String businessName) {
        return toCamelCase(moduleName + "_" + businessName) + ".js";
    }

    private void appendFrontendTableColumn(StringBuilder builder, GenColumnMeta column) {
        String fieldName = toCamelCase(column.getColumnName());
        String label = column.getColumnComment() == null || column.getColumnComment().isBlank()
                ? fieldName : column.getColumnComment();
        if (Objects.equals(column.getColumnName(), "status")) {
            builder.append("        <el-table-column label=\"").append(label).append("\" width=\"100\">\n")
                    .append("          <template #default=\"scope\">\n")
                    .append("            <StatusTag :status=\"scope.row.status\" :enabled-value=\"STATUS_ENABLED\" enabled-text=\"启用\" disabled-text=\"禁用\" />\n")
                    .append("          </template>\n")
                    .append("        </el-table-column>\n");
            return;
        }
        builder.append("        <el-table-column prop=\"").append(fieldName).append("\" label=\"")
                .append(label).append("\" min-width=\"140\" />\n");
    }

    private void appendFrontendFormItem(StringBuilder builder, GenColumnMeta column) {
        String fieldName = toCamelCase(column.getColumnName());
        String label = column.getColumnComment() == null || column.getColumnComment().isBlank()
                ? fieldName : column.getColumnComment();
        builder.append("      <el-form-item label=\"").append(label).append("\">\n");
        if (Objects.equals(column.getColumnName(), "status")) {
            builder.append("        <el-select v-model=\"form.").append(fieldName).append("\" style=\"width: 100%;\">\n")
                    .append("          <el-option :value=\"STATUS_ENABLED\" label=\"启用\" />\n")
                    .append("          <el-option :value=\"STATUS_DISABLED\" label=\"禁用\" />\n")
                    .append("        </el-select>\n");
        } else if (isDateTimeType(column.getJavaType())) {
            builder.append("        <el-date-picker v-model=\"form.").append(fieldName)
                    .append("\" type=\"datetime\" value-format=\"YYYY-MM-DD HH:mm:ss\" style=\"width: 100%;\" />\n");
        } else if (isDateType(column.getJavaType())) {
            builder.append("        <el-date-picker v-model=\"form.").append(fieldName)
                    .append("\" type=\"date\" value-format=\"YYYY-MM-DD\" style=\"width: 100%;\" />\n");
        } else if (isTimeType(column.getJavaType())) {
            builder.append("        <el-time-picker v-model=\"form.").append(fieldName)
                    .append("\" value-format=\"HH:mm:ss\" style=\"width: 100%;\" />\n");
        } else if (isBooleanType(column.getJavaType())) {
            builder.append("        <el-switch v-model=\"form.").append(fieldName).append("\" />\n");
        } else if (isLongTextColumn(column)) {
            builder.append("        <el-input v-model=\"form.").append(fieldName)
                    .append("\" type=\"textarea\" :rows=\"4\" />\n");
        } else if (isNumericType(column.getJavaType())) {
            builder.append("        <el-input-number v-model=\"form.").append(fieldName)
                    .append("\" style=\"width: 100%;\" :controls=\"false\" />\n");
        } else {
            builder.append("        <el-input v-model=\"form.").append(fieldName).append("\" />\n");
        }
        builder.append("      </el-form-item>\n");
    }

    private String buildSearchPlaceholder(List<GenColumnMeta> searchableColumns) {
        return searchableColumns.stream()
                .map(column -> column.getColumnComment() == null || column.getColumnComment().isBlank()
                        ? toCamelCase(column.getColumnName()) : column.getColumnComment())
                .reduce((left, right) -> left + "/" + right)
                .orElse("关键字");
    }

    private String buildFrontendFilterFunction(List<GenColumnMeta> searchableColumns) {
        if (searchableColumns.isEmpty()) {
            return "    return rows.value;\n";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("    return rows.value.filter((item) => ");
        for (int index = 0; index < searchableColumns.size(); index++) {
            String fieldName = toCamelCase(searchableColumns.get(index).getColumnName());
            if (index > 0) {
                builder.append(" || ");
            }
            builder.append("String(item.").append(fieldName).append(" || '').toLowerCase().includes(query)");
        }
        builder.append(");\n");
        return builder.toString();
    }

    private String buildFrontendCreateForm(List<GenColumnMeta> editableColumns) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < editableColumns.size(); index++) {
            GenColumnMeta column = editableColumns.get(index);
            if (index > 0) {
                builder.append(", ");
            }
            builder.append(toCamelCase(column.getColumnName())).append(": ").append(defaultFrontendValue(column));
        }
        return builder.toString();
    }

    private String buildFrontendMapForm(List<GenColumnMeta> editableColumns) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < editableColumns.size(); index++) {
            GenColumnMeta column = editableColumns.get(index);
            if (index > 0) {
                builder.append(", ");
            }
            String fieldName = toCamelCase(column.getColumnName());
            builder.append(fieldName).append(": row.").append(fieldName);
        }
        return builder.toString();
    }

    private String defaultFrontendValue(GenColumnMeta column) {
        if (Objects.equals(column.getColumnName(), "status")) {
            return "STATUS_ENABLED";
        }
        if (isBooleanType(column.getJavaType())) {
            return "false";
        }
        if (isDateType(column.getJavaType()) || isDateTimeType(column.getJavaType()) || isTimeType(column.getJavaType())) {
            return "''";
        }
        if (isNumericType(column.getJavaType())) {
            return "undefined";
        }
        return "''";
    }

    private boolean isNumericType(String javaType) {
        return Objects.equals(javaType, Integer.class.getSimpleName())
                || Objects.equals(javaType, Long.class.getSimpleName())
                || Objects.equals(javaType, Double.class.getSimpleName())
                || Objects.equals(javaType, Float.class.getSimpleName())
                || Objects.equals(javaType, BigDecimal.class.getSimpleName());
    }

    private boolean isDateType(String javaType) {
        return Objects.equals(javaType, LocalDate.class.getSimpleName());
    }

    private boolean isDateTimeType(String javaType) {
        return Objects.equals(javaType, LocalDateTime.class.getSimpleName());
    }

    private boolean isTimeType(String javaType) {
        return Objects.equals(javaType, LocalTime.class.getSimpleName());
    }

    private boolean isBooleanType(String javaType) {
        return Objects.equals(javaType, Boolean.class.getSimpleName());
    }

    private boolean isLongTextColumn(GenColumnMeta column) {
        String columnType = column.getColumnType() == null ? "" : column.getColumnType().toLowerCase();
        String comment = column.getColumnComment() == null ? "" : column.getColumnComment();
        return columnType.contains("text")
                || columnType.contains("json")
                || comment.contains("备注")
                || comment.contains("描述")
                || comment.contains("内容");
    }

    private boolean hasStatusColumn(List<GenColumnMeta> columns) {
        return hasColumn(columns, "status");
    }

    private String pathOf(String root, String packageName) {
        return root + "/" + packageName.replace('.', '/');
    }

    private String normalizeClassName(String className) {
        return normalizeIdentifier(className);
    }

    private String normalizeBusinessName(String businessName) {
        return normalizeIdentifier(businessName).toLowerCase(Locale.ROOT);
    }

    private String normalizePermPrefix(String permPrefix) {
        return normalizeIdentifier(permPrefix).toLowerCase(Locale.ROOT);
    }

    private String normalizeIdentifier(String value) {
        return value == null ? "" : value.trim();
    }

    private void appendPackageAndImports(StringBuilder builder, String packageName, Set<String> imports) {
        builder.append("package ").append(packageName).append(";\n\n");
        for (String importName : imports) {
            builder.append("import ").append(importName).append(";\n");
        }
        if (!imports.isEmpty()) {
            builder.append("\n");
        }
    }

    private void collectJavaTypeImports(Set<String> imports, List<GenColumnMeta> columns) {
        for (GenColumnMeta column : columns) {
            String javaType = column.getJavaType();
            if (Objects.equals(javaType, BigDecimal.class.getSimpleName())) {
                imports.add(BigDecimal.class.getName());
            }
            if (Objects.equals(javaType, LocalDate.class.getSimpleName())) {
                imports.add(LocalDate.class.getName());
            }
            if (Objects.equals(javaType, LocalTime.class.getSimpleName())) {
                imports.add(LocalTime.class.getName());
            }
            if (Objects.equals(javaType, LocalDateTime.class.getSimpleName())) {
                imports.add(LocalDateTime.class.getName());
            }
        }
    }

    private void appendEntityField(StringBuilder builder, GenColumnMeta column, GenColumnMeta primaryKey) {
        if (Objects.equals(column.getColumnName(), primaryKey.getColumnName())) {
            builder.append(Boolean.TRUE.equals(column.getAutoIncrement())
                    ? "    @TableId(type = IdType.AUTO)\n"
                    : "    @TableId\n");
        } else if (needsTableField(column)) {
            builder.append("    @TableField(\"").append(column.getColumnName()).append("\")\n");
        }
        builder.append("    private ").append(column.getJavaType()).append(' ')
                .append(toCamelCase(column.getColumnName())).append(";\n");
    }

    private boolean needsTableField(GenColumnMeta column) {
        return !toSnakeCase(toCamelCase(column.getColumnName())).equals(column.getColumnName());
    }

    private void appendValidation(StringBuilder builder, GenColumnMeta column) {
        if (!Boolean.TRUE.equals(column.getNullable())) {
            String message = buildRequiredMessage(column);
            if (Objects.equals(column.getJavaType(), String.class.getSimpleName())) {
                builder.append("    @NotBlank(message = \"").append(message).append("\")\n");
            } else {
                builder.append("    @NotNull(message = \"").append(message).append("\")\n");
            }
        }
        if (Objects.equals(column.getJavaType(), String.class.getSimpleName())) {
            appendStringValidation(builder, column);
        }
        if (isNumericType(column.getJavaType())) {
            appendNumericValidation(builder, column);
        }
    }

    private void appendStringValidation(StringBuilder builder, GenColumnMeta column) {
        String columnType = column.getColumnType() != null ? column.getColumnType().toLowerCase() : "";
        String comment = column.getColumnComment() != null ? column.getColumnComment() : "";
        String lowerComment = comment.toLowerCase();

        if (columnType.contains("varchar") || columnType.contains("char")) {
            String lengthMatch = columnType.replaceAll(".*\\((\\d+)\\).*", "$1");
            if (!lengthMatch.equals(columnType)) {
                try {
                    int length = Integer.parseInt(lengthMatch);
                    if (length > 255) {
                        builder.append("    @Size(max = ").append(length).append(", message = \"长度不能超过").append(length).append("\")\n");
                    }
                } catch (NumberFormatException e) {
                }
            }
        }

        if (lowerComment.contains("邮箱") || lowerComment.contains("email")) {
            builder.append("    @Pattern(regexp = \"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,}$\", message = \"邮箱格式不正确\")\n");
        } else if (lowerComment.contains("手机") || lowerComment.contains("电话")) {
            builder.append("    @Pattern(regexp = \"^1[3-9]\\\\d{9}$\", message = \"手机号格式不正确\")\n");
        } else if (lowerComment.contains("身份证")) {
            builder.append("    @Pattern(regexp = \"^\\\\d{15}$|^\\\\d{17}[\\\\dXx]$\", message = \"身份证号格式不正确\")\n");
        }
    }

    private void appendNumericValidation(StringBuilder builder, GenColumnMeta column) {
        String columnType = column.getColumnType() != null ? column.getColumnType().toLowerCase() : "";
        if (columnType.contains("int")) {
            builder.append("    @Min(value = -2147483648, message = \"值超出范围\")\n");
            builder.append("    @Max(value = 2147483647, message = \"值超出范围\")\n");
        } else if (columnType.contains("bigint")) {
            builder.append("    @Min(value = -9223372036854775808L, message = \"值超出范围\")\n");
            builder.append("    @Max(value = 9223372036854775807L, message = \"值超出范围\")\n");
        }
    }

    private String buildRequiredMessage(GenColumnMeta column) {
        String label = column.getColumnComment();
        if (label == null || label.isBlank()) {
            label = column.getColumnName();
        }
        return label + "不能为空";
    }

    private void appendGetterSetter(StringBuilder builder, String javaType, String fieldName) {
        String methodSuffix = upperFirst(fieldName);
        builder.append("    public ").append(javaType).append(" get").append(methodSuffix).append("() {\n")
                .append("        return ").append(fieldName).append(";\n")
                .append("    }\n\n")
                .append("    public void set").append(methodSuffix).append("(")
                .append(javaType).append(" ").append(fieldName).append(") {\n")
                .append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n")
                .append("    }\n\n");
    }

    private List<GenColumnMeta> filterEditableColumns(List<GenColumnMeta> columns, GenColumnMeta primaryKey) {
        return columns.stream()
                .filter(column -> !Objects.equals(column.getColumnName(), primaryKey.getColumnName()))
                .filter(column -> !isAuditColumn(column.getColumnName()))
                .toList();
    }

    private List<GenColumnMeta> filterSummaryColumns(List<GenColumnMeta> columns, GenColumnMeta primaryKey) {
        return columns.stream()
                .filter(column -> !Objects.equals(column.getColumnName(), "updated_by"))
                .filter(column -> !Objects.equals(column.getColumnName(), "created_by"))
                .filter(column -> !Objects.equals(column.getColumnName(), "is_deleted"))
                .filter(column -> !Objects.equals(column.getColumnName(), "password_hash"))
                .toList();
    }

    private boolean isAuditColumn(String columnName) {
        return Objects.equals(columnName, "created_by")
                || Objects.equals(columnName, "updated_by")
                || Objects.equals(columnName, "created_at")
                || Objects.equals(columnName, "updated_at")
                || Objects.equals(columnName, "is_deleted");
    }

    private String buildListWrapper(List<GenColumnMeta> columns, String className) {
        if (hasColumn(columns, "is_deleted")) {
            return "\n                .eq(" + className + "::getIsDeleted, 0)\n";
        }
        return "\n";
    }

    private String buildGetOrThrowWrapper(List<GenColumnMeta> columns, String className) {
        if (hasColumn(columns, "is_deleted")) {
            return "\n                .eq(" + className + "::getIsDeleted, 0)\n";
        }
        return "\n";
    }

    private String buildFillEntity(List<GenColumnMeta> columns, GenColumnMeta primaryKey,
                                   String entityVar, String requestVar, boolean onCreate) {
        StringBuilder builder = new StringBuilder();
        for (GenColumnMeta column : columns) {
            String columnName = column.getColumnName();
            if (Objects.equals(columnName, primaryKey.getColumnName()) || isAuditColumn(columnName)) {
                continue;
            }
            String fieldName = toCamelCase(columnName);
            builder.append("        ").append(entityVar).append(".set").append(upperFirst(fieldName)).append("(")
                    .append(requestVar).append('.').append(getterMethodName(fieldName, column.getJavaType())).append("());\n");
        }
        if (onCreate && hasColumn(columns, "is_deleted")) {
            builder.append("        ").append(entityVar).append(".setIsDeleted(0);\n");
        }
        return builder.toString();
    }

    private String buildDeleteLogic(List<GenColumnMeta> columns, String entityVar, GenColumnMeta primaryKey) {
        if (hasColumn(columns, "is_deleted")) {
            return "        " + entityVar + ".setIsDeleted(1);\n        baseMapper.updateById(" + entityVar + ");\n";
        }
        String pkField = toCamelCase(primaryKey.getColumnName());
        return "        baseMapper.deleteById(" + entityVar + "."
                + getterMethodName(pkField, primaryKey.getJavaType()) + "());\n";
    }

    private boolean hasColumn(List<GenColumnMeta> columns, String columnName) {
        return columns.stream().anyMatch(column -> Objects.equals(column.getColumnName(), columnName));
    }

    private String toCamelCase(String value) {
        StringBuilder builder = new StringBuilder();
        boolean upperNext = false;
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (current == '_' || current == '-') {
                upperNext = true;
                continue;
            }
            if (builder.length() == 0) {
                builder.append(Character.toLowerCase(current));
                upperNext = false;
                continue;
            }
            builder.append(upperNext ? Character.toUpperCase(current) : current);
            upperNext = false;
        }
        return builder.toString();
    }

    private String toSnakeCase(String value) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (Character.isUpperCase(current)) {
                if (index > 0) {
                    builder.append('_');
                }
                builder.append(Character.toLowerCase(current));
            } else {
                builder.append(current);
            }
        }
        return builder.toString();
    }

    private String upperFirst(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private String lowerFirst(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }

    private String getterMethodName(String fieldName, String javaType) {
        if (Objects.equals(javaType, Boolean.class.getSimpleName()) && fieldName.startsWith("is") && fieldName.length() > 2) {
            return upperFirst(fieldName);
        }
        return "get" + upperFirst(fieldName);
    }

    private record NormalizedRequest(String tableName, String moduleName, String businessName,
                                     String className, String permPrefix) {
    }
}

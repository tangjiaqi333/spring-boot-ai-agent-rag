package com.itheima.aiagent01.controller;


import com.itheima.aiagent01.common.Result;
import com.itheima.aiagent01.dto.KnowledgeAddRequest;
import com.itheima.aiagent01.dto.KnowledgeDocumentResponse;
import com.itheima.aiagent01.rag.KnowledgeBaseService;
import jakarta.validation.Valid;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {
    private final KnowledgeBaseService knowledgeBaseService;
    public KnowledgeController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }
    @PostMapping
    public Result<String> addknowledge(@Valid @RequestBody KnowledgeAddRequest request) {
        knowledgeBaseService.addDocument(request.getContent());
        return Result.success("添加成功");
    }

    @GetMapping
    public Result<List<KnowledgeDocumentResponse>> listknowledge() {
        List<KnowledgeDocumentResponse> documents = knowledgeBaseService.listDocuments();
        return Result.success(documents);
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteknowledge(@PathVariable Long id) {
        boolean deleted = knowledgeBaseService.deleteDocument(id);
        if (deleted) {
            return Result.success("知识删除成功");
        }

        return Result.error(404,"删除失败");
    }

    @PutMapping("/{id}")
    public Result<String> updateknowledge(@PathVariable Long id, @Valid @RequestBody KnowledgeAddRequest request) {
        boolean updated = knowledgeBaseService.updateDocument(id, request.getContent());
        if (updated) {
            return Result.success("知识更新成功");
        }

        return Result.error(404,"更新失败");
    }
    @PostMapping("/upload")
    public Result<String> uploadknowledge(@RequestParam("file") MultipartFile file) {
        knowledgeBaseService.uploadDocument(file);
        return Result.success("上传成功");
    }
}

package com.abel.example.controller;


import com.abel.example.model.response.ResponseMessage;
import com.abel.example.service.pdf.PdfService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/pdf")
@Tag(name = "pdf管理", description = "解析pdf文件")
public class PdfController {

    @Autowired
    private PdfService pdfService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, value = "/upload")
    public ResponseMessage uploadPdf(@RequestParam("file") MultipartFile file) {
        try {
            String text = pdfService.parsePdf(file);
            return ResponseMessage.success(text);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseMessage.error("解析失败: " + e.getMessage());
        }
    }
}

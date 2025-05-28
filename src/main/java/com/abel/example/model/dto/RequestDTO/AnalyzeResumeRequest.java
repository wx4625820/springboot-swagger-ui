package com.abel.example.model.dto.RequestDTO;

import jakarta.validation.constraints.NotBlank;

public class AnalyzeResumeRequest {
    @NotBlank(message = "简历内容不能为空")
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

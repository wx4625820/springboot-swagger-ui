package com.abel.example.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VideoMetaData {
    private double duration;
    private String filename;
    private double fps;
    @JsonProperty("frame_count")
    private int frameCount;
    private String resolution;
}

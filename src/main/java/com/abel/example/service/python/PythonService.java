package com.abel.example.service.python;

import com.abel.example.model.entity.VideoMetaDataWrapper;

public interface PythonService {
    VideoMetaDataWrapper analyzeVideo(String videoUrl);
}

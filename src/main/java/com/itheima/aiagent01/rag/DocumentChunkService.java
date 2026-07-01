package com.itheima.aiagent01.rag;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
//切片
@Service
public class DocumentChunkService {
    public List<String> splitText(String text, int chunkSize, int overlapSize) {
        List<String> chunks = new ArrayList<>();

        if (!StringUtils.hasText(text)){
            return chunks;
        }

        if (chunkSize <= 0){
            throw new RuntimeException("分块大小必须大于 0");
        }

        if (overlapSize < 0 || overlapSize >= chunkSize){
            throw new RuntimeException("重叠大小必须大于等于 0 且小于分块大小");
        }

        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            String chunk = text.substring(start, end).trim();
            if (StringUtils.hasText(chunk)){
                chunks.add(chunk);
            }
            if (end == text.length()){
                break;
            }

            start = end - overlapSize;
        }
        return chunks;
    }
}

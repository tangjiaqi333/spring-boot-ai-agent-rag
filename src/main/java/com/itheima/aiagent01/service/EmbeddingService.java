package com.itheima.aiagent01.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmbeddingService {

    private static final int VECTOR_SIZE = 128;

    public double[] embed(String text) {
        double[] vector = new double[VECTOR_SIZE];

        if (!StringUtils.hasText(text)) {
            return vector;
        }

        List<String> tokens = tokenize(text);

        for (String token : tokens) {
            int index = Math.abs(token.hashCode()) % VECTOR_SIZE;
            vector[index] += 1.0;
        }

        normalize(vector);

        return vector;
    }

    public double cosineSimilarity(double[] a, double[] b) {
        if (a == null || b == null || a.length != b.length) {
            return 0;
        }

        double dot = 0;
        double normA = 0;
        double normB = 0;

        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0 || normB == 0) {
            return 0;
        }

        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();

        String lower = text.toLowerCase()
                .replace("？", " ")
                .replace("?", " ")
                .replace("，", " ")
                .replace(",", " ")
                .replace("。", " ")
                .replace(".", " ")
                .replace("\n", " ");

        String[] parts = lower.split("\\s+");

        for (String part : parts) {
            if (StringUtils.hasText(part)) {
                tokens.add(part);
            }

            // 中文没有天然空格，所以额外按单个中文字符切一下
            for (int i = 0; i < part.length(); i++) {
                char c = part.charAt(i);

                if (isChinese(c)) {
                    tokens.add(String.valueOf(c));
                }
            }
        }

        return tokens;
    }

    private boolean isChinese(char c) {
        return c >= '\u4e00' && c <= '\u9fa5';
    }

    private void normalize(double[] vector) {
        double sum = 0;

        for (double v : vector) {
            sum += v * v;
        }

        if (sum == 0) {
            return;
        }

        double norm = Math.sqrt(sum);

        for (int i = 0; i < vector.length; i++) {
            vector[i] = vector[i] / norm;
        }
    }
}
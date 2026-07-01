package com.itheima.aiagent01.tool;

import org.springframework.stereotype.Component;

@Component
public class CalculatorTool implements AgentTool {

    public String calculate(String expression) {
        if (expression == null || expression.isBlank()) {
            return "表达式不能为空";
        }

        expression = expression.trim()
                .replace(" ", "")
                .replace("乘以", "*")
                .replace("乘", "*")
                .replace("x", "*")
                .replace("X", "*")
                .replace("除以", "/")
                .replace("除", "/")
                .replace("加", "+")
                .replace("减", "-");

        try {
            if (expression.contains("*")) {
                String[] parts = expression.split("\\*");

                if (parts.length != 2) {
                    return "暂时只支持两个数字的乘法";
                }

                double a = Double.parseDouble(parts[0]);
                double b = Double.parseDouble(parts[1]);

                return formatResult(a * b);
            }

            if (expression.contains("/")) {
                String[] parts = expression.split("/");

                if (parts.length != 2) {
                    return "暂时只支持两个数字的除法";
                }

                double a = Double.parseDouble(parts[0]);
                double b = Double.parseDouble(parts[1]);

                if (b == 0) {
                    return "除数不能为 0";
                }

                return formatResult(a / b);
            }

            if (expression.contains("+")) {
                String[] parts = expression.split("\\+");

                if (parts.length != 2) {
                    return "暂时只支持两个数字的加法";
                }

                double a = Double.parseDouble(parts[0]);
                double b = Double.parseDouble(parts[1]);

                return formatResult(a + b);
            }

            if (expression.contains("-")) {
                String[] parts = expression.split("-");

                if (parts.length != 2) {
                    return "暂时只支持两个数字的减法";
                }

                double a = Double.parseDouble(parts[0]);
                double b = Double.parseDouble(parts[1]);

                return formatResult(a - b);
            }

            return "暂时只支持两个数字的加减乘除";

        } catch (Exception e) {
            return "计算失败，请输入类似 123*456、10+20、100/4 的表达式";
        }
    }

    private String formatResult(double result) {
        if (result == (long) result) {
            return String.valueOf((long) result);
        }

        return String.valueOf(result);
    }

    @Override
    public String getName() {
        return "calculator";
    }

    @Override
    public String getDescription() {
        return "执行数学计算。input 传入数学表达式，例如：12345*678。";
    }

    @Override
    public String execute(String input) {
        return calculate(input);
    }
}
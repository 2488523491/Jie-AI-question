package com.JieAI.AIquestion.model.dto.question;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionContentDTO {


    /**
     * 题目标题
     */
    private String title;

    /**
     * 题目选项表
     */
    private List<Option> options;

    /**
     * 题目选项
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Option {

        /**
         * 如果是测评类 result用来保存答案属性
         */
        private String result;

        /**
         * 如果是得分类，用score来表示本题分数
         */
        private int score;

        private String value;

        private String key;

    }
}


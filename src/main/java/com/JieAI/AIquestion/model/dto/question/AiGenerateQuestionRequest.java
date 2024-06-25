package com.JieAI.AIquestion.model.dto.question;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI 生成题目请求
 *
 
 */
@Data
public class AiGenerateQuestionRequest implements Serializable {

    /**
     * 应用id
     */
    private Long appId;
    /**
     * 题目数
     */
    int questionNumber = 10;
    /**
     * 选项数
     */
    int optionNumber = 2;

    private static final long serialVersionUID = 1L;


}
package com.JieAI.AIquestion.scoring;

import com.JieAI.AIquestion.model.entity.App;
import com.JieAI.AIquestion.model.entity.UserAnswer;

import java.util.List;

/**
 * 评分策略，接收选项列表和应用，然后进行评分
 */
public interface ScoringStrategy {

    UserAnswer doScore(List<String> choices, App app) throws Exception;
}

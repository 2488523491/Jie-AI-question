package com.JieAI.AIquestion.scoring;

import com.JieAI.AIquestion.common.ErrorCode;
import com.JieAI.AIquestion.exception.BusinessException;
import com.JieAI.AIquestion.model.entity.App;
import com.JieAI.AIquestion.model.entity.UserAnswer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 评分策略执行器
 */
@Service
public class ScoringStrategyExecutor {

    @Resource
    private List<ScoringStrategy> scoringStrategyList;


    /**
     * 评分
     * @param choiceList
     * @param app
     * @return
     * @throws Exception
     */
    public UserAnswer doScore(List<String> choiceList, App app) throws Exception {
        Integer appType = app.getAppType();
        Integer scoringStrategy = app.getScoringStrategy();
        if (appType == null || scoringStrategy == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用配置有误，未找到匹配策略");
        }
        for (ScoringStrategy strategy : scoringStrategyList) {
            if (strategy.getClass().isAnnotationPresent(ScoringStrategyConfig.class)) {
                ScoringStrategyConfig config = strategy.getClass().getAnnotation(ScoringStrategyConfig.class);
                if (config.appType() == appType && config.scoringStrategy() == scoringStrategy) {
                     return strategy.doScore(choiceList, app);
                }
            }
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用配置有误，未找到匹配策略");
    }
}

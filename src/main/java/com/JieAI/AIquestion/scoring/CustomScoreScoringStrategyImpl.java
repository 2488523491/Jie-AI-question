package com.JieAI.AIquestion.scoring;

import cn.hutool.json.JSONUtil;
import com.JieAI.AIquestion.model.dto.question.QuestionContentDTO;
import com.JieAI.AIquestion.model.entity.App;
import com.JieAI.AIquestion.model.entity.Question;
import com.JieAI.AIquestion.model.entity.ScoringResult;
import com.JieAI.AIquestion.model.entity.UserAnswer;
import com.JieAI.AIquestion.model.vo.QuestionVO;
import com.JieAI.AIquestion.service.QuestionService;
import com.JieAI.AIquestion.service.ScoringResultService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 自定义打分类应用评分策略
 */

@ScoringStrategyConfig(appType= 0, scoringStrategy = 0)
public class CustomScoreScoringStrategyImpl implements ScoringStrategy {

    @Resource
    QuestionService questionService;
    @Resource
    private ScoringResultService scoringResultService;
    @Override
    public UserAnswer doScore(List<String> choices, App app) throws Exception {
        //1.根据id查询到题目和题目结果信息
        Question question = questionService.getOne(
                Wrappers.lambdaQuery(Question.class)
                        .eq(Question::getAppId, app.getId())
        );
        List<ScoringResult> scoringResultList = scoringResultService.list(
                Wrappers.lambdaQuery(ScoringResult.class)
                        .eq(ScoringResult::getAppId, app.getId())
                        .orderByDesc(ScoringResult::getResultScoreRange)
        );

        //2.根据用户选择的答案，统计总得分
        int totalScore = 0; 
        QuestionVO questionVO = QuestionVO.objToVo(question);
        List<QuestionContentDTO> questionContents = questionVO.getQuestionContent();
        for (QuestionContentDTO questionContent : questionContents) {
            for (String answer : choices) {
                for (QuestionContentDTO.Option option : questionContent.getOptions()) {
                    if (option.getKey().equals(answer)) {
                        //获取option中的score属性
                        int score = option.getScore();
                        //初始化为0
                        Optional.ofNullable(score).orElse(0);
                        //将对应的值加1
                        totalScore += score;
                    }
                }
            }
        }
        //3.遍历得分结果
        ScoringResult maxscoringResult = scoringResultList.get(0);

        for(ScoringResult result : scoringResultList){
            if(totalScore >= maxscoringResult.getResultScoreRange()){
                maxscoringResult = result;
                break;
            }
        }


        //4.构造返回值，填充答案对象的属性
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setAppId(app.getId());
        userAnswer.setAppType(app.getAppType());
        userAnswer.setScoringStrategy(app.getScoringStrategy());
        userAnswer.setChoices(JSONUtil.toJsonStr(choices));
        userAnswer.setResultName(maxscoringResult.getResultName());
        userAnswer.setResultDesc(maxscoringResult.getResultDesc());
        userAnswer.setResultPicture(maxscoringResult.getResultPicture());
        userAnswer.setResultScore(totalScore);
        return userAnswer;
    }
}

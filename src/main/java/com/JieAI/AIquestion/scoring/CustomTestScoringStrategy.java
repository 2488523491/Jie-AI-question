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

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义测评类应用  例如 mbti
 */
@ScoringStrategyConfig(appType= 1, scoringStrategy = 0)
public class CustomTestScoringStrategy implements ScoringStrategy {
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
        );
        //2.统计用户每个选项选择对应的个数
        Map<String, Integer> optionCount = new HashMap<>();

        QuestionVO questionVO = QuestionVO.objToVo(question);
        List<QuestionContentDTO> questionContents = questionVO.getQuestionContent();
        System.out.println("2."+questionContents);
        for (QuestionContentDTO questionContent : questionContents) {
            for (String answer : choices) {
                for (QuestionContentDTO.Option option : questionContent.getOptions()) {
                    if (option.getKey().equals(answer)) {
                        //获取option中的Result属性
                        String result = option.getResult();
                        //如果result属性不在optionCount中，初始化为0
                        if (!optionCount.containsKey(result)) {
                            optionCount.put(result, 1);
                        }
                        //如果result属性在optionCount中，则将对应的值加1
                        optionCount.put(result, optionCount.get(result) + 1);
                    }
                }
            }
        }
        //3.遍历每种评分结果，计算哪个结果的得分更高
        //初始化最高分和最高分对应的评分结果
        int maxScore = 0;
        ScoringResult maxscoringResult = scoringResultList.get(0);
        //遍历评分结果列表

        for (ScoringResult result : scoringResultList) {
            //计算每个评分结果的得分
            List<String> resultProps = JSONUtil.toList(result.getResultProp(), String.class);
            int score = resultProps.stream()
                    .mapToInt(prop -> optionCount.getOrDefault(prop, 0))
                    .sum();
            //如果分数高于最高分数，更新最高分数和最高分数对应的评分结果
            if (score > maxScore) {
                maxScore = score;
                maxscoringResult = result;
            }
        }

        //4.构造返回值，填充答案对象的属性
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setAppId(app.getId());
        userAnswer.setAppType(app.getAppType());
        userAnswer.setScoringStrategy(app.getScoringStrategy());
        userAnswer.setChoices(JSONUtil.toJsonStr(choices));
        userAnswer.setResultId(maxscoringResult.getId());
        userAnswer.setResultName(maxscoringResult.getResultName());
        userAnswer.setResultDesc(maxscoringResult.getResultDesc());
        userAnswer.setResultPicture(maxscoringResult.getResultPicture());

        return userAnswer;
    }
}

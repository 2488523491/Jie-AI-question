package com.JieAI.AIquestion.service.impl;

import com.JieAI.AIquestion.mapper.QuestionMapper;
import com.JieAI.AIquestion.model.entity.Question;
import com.JieAI.AIquestion.service.QuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
* @author 14316
* @description 针对表【question(题目)】的数据库操作Service实现
* @createDate 2024-06-17 19:35:47
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService {

}





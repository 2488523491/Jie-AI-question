package com.JieAI.AIquestion.model.dto.userAnswer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 编辑用户答案请求
 *
 
 */
@Data
public class UserAnswerEditRequest implements Serializable {
    /**
     *
     */
    private Long id;

    /**
     * 应用 id
     */
    private Long appId;


    /**
     * 用户答案（JSON 数组）
     */
    private List<String> choices;

}
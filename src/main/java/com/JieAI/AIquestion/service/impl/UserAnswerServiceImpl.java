package com.JieAI.AIquestion.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.JieAI.AIquestion.common.ErrorCode;
import com.JieAI.AIquestion.constant.CommonConstant;
import com.JieAI.AIquestion.exception.ThrowUtils;
import com.JieAI.AIquestion.mapper.UserAnswerMapper;
import com.JieAI.AIquestion.model.dto.userAnswer.UserAnswerQueryRequest;
import com.JieAI.AIquestion.model.entity.App;
import com.JieAI.AIquestion.model.entity.User;
import com.JieAI.AIquestion.model.entity.UserAnswer;
import com.JieAI.AIquestion.model.vo.UserAnswerVO;
import com.JieAI.AIquestion.model.vo.UserVO;
import com.JieAI.AIquestion.service.AppService;
import com.JieAI.AIquestion.service.UserAnswerService;
import com.JieAI.AIquestion.service.UserService;
import com.JieAI.AIquestion.utils.SqlUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户答案服务实现
 */
@Service
@Slf4j
public class UserAnswerServiceImpl extends ServiceImpl<UserAnswerMapper, UserAnswer> implements UserAnswerService {
    @Resource
    private UserAnswerMapper userAnswerMapper;
    @Resource
    private UserService userService;

    @Resource
    private AppService appService;

    /**
     * 校验数据
     *
     * @param userAnswer
     * @param add        对创建的数据进行校验
     */
    @Override
    public void validUserAnswer(UserAnswer userAnswer, boolean add) {
        System.out.println(userAnswer);

        ThrowUtils.throwIf(userAnswer == null, ErrorCode.PARAMS_ERROR);
        //  从对象中取值
        Long appId = userAnswer.getAppId();
        System.out.println(appId);
        Long id = userAnswer.getId();
        // 创建数据时，参数不能为空
        if (add) {
            //  补充校验规则
            ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "非法应用Id");
            ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "非法Id");
        }
        if (appId != null) {
            App app = appService.getById(appId);
            ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR, "应用不存在");
        }
    }

    /**
     * 获取查询条件
     *
     * @param userAnswerQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<UserAnswer> getQueryWrapper(UserAnswerQueryRequest userAnswerQueryRequest) {
        QueryWrapper<UserAnswer> queryWrapper = new QueryWrapper<>();
        if (userAnswerQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = userAnswerQueryRequest.getId();
        Long appId = userAnswerQueryRequest.getAppId();
        Integer appType = userAnswerQueryRequest.getAppType();
        Integer scoringStrategy = userAnswerQueryRequest.getScoringStrategy();
        String choices = userAnswerQueryRequest.getChoices();
        Long resultId = userAnswerQueryRequest.getResultId();
        String resultName = userAnswerQueryRequest.getResultName();
        String resultDesc = userAnswerQueryRequest.getResultDesc();
        String searchText = userAnswerQueryRequest.getSearchText();
        String sortField = userAnswerQueryRequest.getSortField();
        String sortOrder = userAnswerQueryRequest.getSortOrder();
        Long userId = userAnswerQueryRequest.getUserId();

        //  补充需要的查询条件
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("resultName", searchText).or().like("resultDesc", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(choices), "choices", choices);
        queryWrapper.like(StringUtils.isNotBlank(resultName), "resultName", resultName);
        queryWrapper.like(StringUtils.isNotBlank(resultDesc), "resultDesc", resultDesc);
        // JSON 数组查询

        // 精确查询
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.ne(ObjectUtils.isNotEmpty(resultId), "resultId", resultId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appId), "appId", appId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appType), "appType", appType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(scoringStrategy), "scoringStrategy", scoringStrategy);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    /**
     * 获取用户答案封装
     *
     * @param userAnswer
     * @param request
     * @return
     */
    @Override
    public UserAnswerVO getUserAnswerVO(UserAnswer userAnswer, HttpServletRequest request) {
        // 对象转封装类
        UserAnswerVO userAnswerVO = UserAnswerVO.objToVo(userAnswer);

        // 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = userAnswer.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        userAnswerVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        long userAnswerId = userAnswer.getId();
        User loginUser = userService.getLoginUserPermitNull(request);
        // endregion

        return userAnswerVO;
    }

    /**
     * 分页获取用户答案封装
     *
     * @param userAnswerPage
     * @param request
     * @return
     */
    @Override
    public Page<UserAnswerVO> getUserAnswerVOPage(Page<UserAnswer> userAnswerPage, HttpServletRequest request) {
        List<UserAnswer> userAnswerList = userAnswerPage.getRecords();
        Page<UserAnswerVO> userAnswerVOPage = new Page<>(userAnswerPage.getCurrent(), userAnswerPage.getSize(), userAnswerPage.getTotal());
        if (CollUtil.isEmpty(userAnswerList)) {
            return userAnswerVOPage;
        }
        // 对象列表 => 封装对象列表
        List<UserAnswerVO> userAnswerVOList = userAnswerList.stream().map(UserAnswerVO::objToVo).collect(Collectors.toList());

        // 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = userAnswerList.stream().map(UserAnswer::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        // 填充信息
        userAnswerVOList.forEach(userAnswerVO -> {
            Long userId = userAnswerVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            userAnswerVO.setUser(userService.getUserVO(user));
        });
        // endregion
        userAnswerVOPage.setRecords(userAnswerVOList);
        return userAnswerVOPage;
    }

}

package com.example.BackendApi.Service;

import com.example.BackendApi.Dto.Vo.UserJobLinkVo;

import java.util.List;

public interface IUserJobLinkService {

    UserJobLinkVo createUserJobLink(UserJobLinkVo userJobLinkVo);

    List<UserJobLinkVo> getAllUserJobLinks();

    UserJobLinkVo getUserJobLinkById(String id);

    void deleteUserJobLink(String id);

    List<UserJobLinkVo> getUserJobLinksByUserId(String userId);

    List<UserJobLinkVo> getUserJobLinksByJobPostingId(String jobPostingId);
}

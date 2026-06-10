package com.example.BackendArchitectureLab.Service.impl;

import com.example.BackendArchitectureLab.DataAccess.IJobPostingDataAccess;
import com.example.BackendArchitectureLab.DataAccess.IUserDataAccess;
import com.example.BackendArchitectureLab.DataAccess.IUserJobLinkDataAccess;
import com.example.BackendArchitectureLab.Dto.Vo.UserJobLinkVo;
import com.example.BackendArchitectureLab.Entity.JobPosting;
import com.example.BackendArchitectureLab.Entity.User;
import com.example.BackendArchitectureLab.Entity.UserJobLink;
import com.example.BackendArchitectureLab.Mapper.UserJobLinkMapper;
import com.example.BackendArchitectureLab.Service.IUserJobLinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserJobLinkService implements IUserJobLinkService {

    private final IUserJobLinkDataAccess userJobLinkDataAccess;
    private final IUserDataAccess userDataAccess;
    private final IJobPostingDataAccess jobPostingDataAccess;
    private final UserJobLinkMapper userJobLinkMapper;

    @Override
    @Transactional
    @CacheEvict(value = "userJobLinks", allEntries = true)
    public UserJobLinkVo createUserJobLink(UserJobLinkVo userJobLinkVo) {
        UserJobLink link = new UserJobLink();

        if (userJobLinkVo.getUserId() != null) {
            User user = userDataAccess.findById(UUID.fromString(userJobLinkVo.getUserId()))
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            link.setUser(user);
        }

        if (userJobLinkVo.getJobPostingId() != null) {
            JobPosting jobPosting = jobPostingDataAccess.findById(UUID.fromString(userJobLinkVo.getJobPostingId()))
                    .orElseThrow(() -> new IllegalArgumentException("Job posting not found"));
            link.setJobPosting(jobPosting);
        }

        link.setUserNotes(userJobLinkVo.getUserNotes());
        link = userJobLinkDataAccess.save(link);
        return userJobLinkMapper.toVo(link);
    }

    @Override
    @Cacheable(value = "userJobLinks", sync = true)
    public List<UserJobLinkVo> getAllUserJobLinks() {
        return userJobLinkDataAccess.findAll().stream()
                .map(userJobLinkMapper::toVo)
                .toList();
    }

    @Override
    @Cacheable(value = "userJobLinks", key = "#id", sync = true)
    public UserJobLinkVo getUserJobLinkById(String id) {
        UUID uuid = mapUuid(id);
        if (uuid == null) {
            throw new IllegalArgumentException("ID must not be null");
        }
        UserJobLink link = userJobLinkDataAccess.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("User job link not found"));
        return userJobLinkMapper.toVo(link);
    }

    @Override
    @Transactional
    @CacheEvict(value = "userJobLinks", allEntries = true)
    public void deleteUserJobLink(String id) {
        UUID uuid = mapUuid(id);
        if (uuid == null) {
            throw new IllegalArgumentException("ID must not be null");
        }
        if (!userJobLinkDataAccess.existsById(uuid)) {
            throw new IllegalArgumentException("User job link not found");
        }
        userJobLinkDataAccess.deleteById(uuid);
    }

    @Override
    @Cacheable(value = "userJobLinks", key = "'byuser:' + #userId", sync = true)
    public List<UserJobLinkVo> getUserJobLinksByUserId(String userId) {
        UUID uuid = mapUuid(userId);
        if (uuid == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }
        return userJobLinkDataAccess.findByUserId(uuid).stream()
                .map(userJobLinkMapper::toVo)
                .toList();
    }

    @Override
    @Cacheable(value = "userJobLinks", key = "'byjob:' + #jobPostingId", sync = true)
    public List<UserJobLinkVo> getUserJobLinksByJobPostingId(String jobPostingId) {
        UUID uuid = mapUuid(jobPostingId);
        if (uuid == null) {
            throw new IllegalArgumentException("Job posting ID must not be null");
        }
        return userJobLinkDataAccess.findByJobPostingId(uuid).stream()
                .map(userJobLinkMapper::toVo)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "userJobLinks", allEntries = true)
    public UserJobLinkVo addJobToCurrentUser(String currentUserId, String jobPostingId) {
        UUID userUuid = mapUuid(currentUserId);
        UUID jobUuid = mapUuid(jobPostingId);
        if (userUuid == null || jobUuid == null) {
            throw new IllegalArgumentException("User ID and Job Posting ID must not be null");
        }
        if (userJobLinkDataAccess.existsByUserIdAndJobPostingId(userUuid, jobUuid)) {
            throw new IllegalArgumentException("Job already bound to current user");
        }
        User user = userDataAccess.findById(userUuid)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        JobPosting jobPosting = jobPostingDataAccess.findById(jobUuid)
                .orElseThrow(() -> new IllegalArgumentException("Job posting not found"));
        UserJobLink link = new UserJobLink();
        link.setUser(user);
        link.setJobPosting(jobPosting);
        link = userJobLinkDataAccess.save(link);
        return userJobLinkMapper.toVo(link);
    }

    @Override
    @Transactional
    @CacheEvict(value = "userJobLinks", allEntries = true)
    public void removeJobFromCurrentUser(String currentUserId, String jobPostingId) {
        UUID userUuid = mapUuid(currentUserId);
        UUID jobUuid = mapUuid(jobPostingId);
        if (userUuid == null || jobUuid == null) {
            throw new IllegalArgumentException("User ID and Job Posting ID must not be null");
        }
        if (!userJobLinkDataAccess.existsByUserIdAndJobPostingId(userUuid, jobUuid)) {
            throw new IllegalArgumentException("Job binding not found");
        }
        userJobLinkDataAccess.deleteByUserIdAndJobPostingId(userUuid, jobUuid);
    }

    @Override
    @Cacheable(value = "userJobLinks", key = "'currentuser:' + #currentUserId", sync = true)
    public List<UserJobLinkVo> getCurrentUserJobLinks(String currentUserId) {
        return getUserJobLinksByUserId(currentUserId);
    }

    private UUID mapUuid(String id) {
        return id == null || id.isBlank() ? null : UUID.fromString(id);
    }
}

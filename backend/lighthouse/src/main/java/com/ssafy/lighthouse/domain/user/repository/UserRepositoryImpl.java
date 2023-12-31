package com.ssafy.lighthouse.domain.user.repository;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ssafy.lighthouse.domain.common.dto.BadgeResponse;
import com.ssafy.lighthouse.domain.common.dto.TagDto;
import com.ssafy.lighthouse.domain.common.entity.Badge;
import com.ssafy.lighthouse.domain.study.dto.SimpleStudyDto;
import com.ssafy.lighthouse.domain.study.entity.ParticipationHistory;
import com.ssafy.lighthouse.domain.study.entity.Study;
import com.ssafy.lighthouse.domain.study.repository.BookmarkRepository;
import com.ssafy.lighthouse.domain.study.repository.ParticipationHistoryRepository;
import com.ssafy.lighthouse.domain.user.dto.ProfileResponse;
import com.ssafy.lighthouse.domain.user.dto.SimpleProfileResponse;
import com.ssafy.lighthouse.domain.user.dto.SimpleUserResponse;
import com.ssafy.lighthouse.domain.user.entity.QFollow;
import com.ssafy.lighthouse.global.util.ROLE;
import com.ssafy.lighthouse.global.util.STATUS;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

import static com.querydsl.jpa.JPAExpressions.select;
import static com.ssafy.lighthouse.domain.common.entity.QTag.tag;
import static com.ssafy.lighthouse.domain.study.entity.QBookmark.bookmark;
import static com.ssafy.lighthouse.domain.study.entity.QParticipationHistory.participationHistory;
import static com.ssafy.lighthouse.domain.study.entity.QStudy.study;
import static com.ssafy.lighthouse.domain.study.entity.QStudyLike.studyLike;
import static com.ssafy.lighthouse.domain.user.entity.QFollow.follow;
import static com.ssafy.lighthouse.domain.user.entity.QUser.user;
import static com.ssafy.lighthouse.domain.user.entity.QUserEval.userEval;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;
    private final ParticipationHistoryRepository participationHistoryRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserTagRepository userTagRepository;
    private final UserBadgeRepository userBadgeRepository;

    @Override
    public ProfileResponse findProfileByUserId(Long userId, Long loginId) {
        // all
        Set<Long> allStudyIdSet = participationHistoryRepository.findStudyIdAllByUserId(userId);
        // participated StudyIdSet
        Set<Long> participatedStudyIdSet = participationHistoryRepository.findStudyIdAllByUserIdandStatus(userId, STATUS.PREPARING);

        List<Study> studyList = jpaQueryFactory.select(study).from(study).where(study.id.in(allStudyIdSet), study.isValid.eq(1)).fetch();
        List<SimpleStudyDto> participatedStudies = new ArrayList<>();
        List<SimpleStudyDto> recruitingStudies = new ArrayList<>();
        List<SimpleStudyDto> progressStudies = new ArrayList<>();
        List<SimpleStudyDto> terminatedStudies = new ArrayList<>();

        studyList.forEach((study) -> {
            SimpleStudyDto simpleStudyDto = new SimpleStudyDto(study);
            simpleStudyDto.setLeaderProfile(findSimpleProfileByUserId(study.getLeaderId()));

            // status에 따른 스터디 분류
            switch(study.getStatus()) {
                // 생성중 스터디
                case STATUS.PREPARING:
                    if(userId.equals(loginId)) {
                        participatedStudies.add(simpleStudyDto);
                    }
                    break;

                // 모집중 스터디
                case STATUS.RECRUITING:
                    // 신청한 스터디
                    if(participatedStudyIdSet.contains(study.getId())) {
                        if(userId.equals(loginId)) {
                            participatedStudies.add(simpleStudyDto);
                        }
                    }

                    // 진행 예정 스터디
                    else {
                        recruitingStudies.add(simpleStudyDto);
                    }
                    break;

                // 진행중 스터디
                case STATUS.PROGRESS:
                    progressStudies.add(simpleStudyDto);
                    break;

                // 끝난 스터디
                case STATUS.TERMINATED: case STATUS.SHARE:
                    terminatedStudies.add(simpleStudyDto);
                    break;
            }
        });

        // 북마크한 스터디
        Set<Long> bookmarkSet = bookmarkRepository.findAllByUserId(userId);
        List<SimpleStudyDto> bookmarkStudies = jpaQueryFactory.select(study)
                .from(study)
                .where(study.id.in(bookmarkSet),
                        study.isValid.eq(1))
                .fetch()
                .stream()
                .map(SimpleStudyDto::new)
                .collect(Collectors.toList());

        QFollow followee = new QFollow("followee");
        ProfileResponse result = jpaQueryFactory.select(Projections.fields(ProfileResponse.class,
                        user.id,
                        user.isValid,
                        user.nickname,
                        user.profileImgUrl,
                        user.description,
                        ExpressionUtils.as(select(userEval.score.avg()).from(userEval).where(userEval.userId.eq(userId), userEval.isValid.eq(1)), "score"),
                        ExpressionUtils.as(select(follow.followeeId.count()).from(follow).where(follow.followerId.eq(userId), follow.isValid.eq(1)), "following"),
                        ExpressionUtils.as(select(followee.followerId.count()).from(followee).where(followee.followeeId.eq(userId), followee.isValid.eq(1)), "follower")))
                .from(user)
                .where(user.id.eq(userId), user.isValid.eq(1))
                .fetchOne();

        // badgeList
        List<BadgeResponse> badgeResponses = getBadgeResponsesByUserId(userId);

        // tag
        Set<Long> tagSet = userTagRepository.findTagIdAllByUserId(userId);
        List<TagDto> tags = jpaQueryFactory.select(Projections.constructor(TagDto.class, tag)).from(tag).where(tag.id.in(tagSet), tag.isValid.eq(1)).fetch();

        // userInfo
//        SimpleUserResponse userInfo = findUserInfo(loginId);

        // participatedUserProfiles
        Map<Long, List<SimpleProfileResponse>> participatedUserProfiles = new HashMap<>();
        if(userId.equals(loginId)) {
            // 내가 리더인 스터디
            List<Long> leaderStudies = recruitingStudies.stream()
                    .filter(study -> study.getLeaderProfile().getId().equals(loginId))
                    .map(SimpleStudyDto::getId)
                    .collect(Collectors.toList());
            
            // 내가 리더인 스터디에 참가 신청한 유저 아이디
            List<ParticipationHistory> participationHistories = jpaQueryFactory.selectFrom(participationHistory)
                    .where(participationHistory.studyId.in(leaderStudies),
                            participationHistory.isValid.eq(1),
                            participationHistory.status.eq(STATUS.PREPARING),
                            participationHistory.userRole.eq(ROLE.TEAMMATE))
                    .fetch();

            // map에 담기
            participationHistories.stream()
                    .collect(Collectors.groupingBy(ParticipationHistory::getStudyId))
                    .forEach((studyId, val) -> {
                        List<Long> userIds = val.stream().map(ParticipationHistory::getUserId).collect(Collectors.toList());
                        participatedUserProfiles.put(studyId, findSimpleProfileByUserIds(userIds));
                    });
        }

        return ProfileResponse.builder()
                .id(result.getId())
                .isValid(result.getIsValid())
                .nickname(result.getNickname())
                .profileImgUrl(result.getProfileImgUrl())
                .description(result.getDescription())
                .tags(tags)
                .badges(badgeResponses)
                .participatedStudies(participatedStudies)
                .recruitingStudies(recruitingStudies)
                .progressStudies(progressStudies)
                .terminatedStudies(terminatedStudies)
                .bookmarkStudies(bookmarkStudies)
                .participatedUserProfiles(participatedUserProfiles)
                .score(result.getScore())
                .following(result.getFollowing())
                .follower(result.getFollower())
                .build();
    }

    @Override
    public SimpleProfileResponse findSimpleProfileByUserId(Long userId) {
        SimpleProfileResponse result = jpaQueryFactory.select(Projections.fields(SimpleProfileResponse.class,
                        user.id,
                        user.isValid,
                        user.nickname,
                        user.profileImgUrl,
                        user.description,
                        ExpressionUtils.as(select(userEval.score.avg()).from(userEval).where(userEval.userId.eq(userId), userEval.isValid.eq(1)), "score")))
                .from(user)
                .where(user.id.eq(userId), user.isValid.eq(1))
                .fetchOne();

        Set<Long> tagSet = userTagRepository.findTagIdAllByUserId(userId);
        List<TagDto> tags = jpaQueryFactory.select(Projections.constructor(TagDto.class, tag)).from(tag).where(tag.id.in(tagSet), tag.isValid.eq(1)).fetch();

        // badgeList
        List<BadgeResponse> badgeResponses = getBadgeResponsesByUserId(userId);

        return SimpleProfileResponse.builder()
                .id(result.getId())
                .isValid(result.getIsValid())
                .nickname(result.getNickname())
                .profileImgUrl(result.getProfileImgUrl())
                .description(result.getDescription())
                .tags(tags)
                .badges(badgeResponses)
                .score(result.getScore())
                .build();
    }

    @Override
    public List<SimpleProfileResponse> findSimpleProfileByUserIds(List<Long> userIds) {
        List<SimpleProfileResponse> result = jpaQueryFactory.select(Projections.fields(SimpleProfileResponse.class,
                        user.id,
                        user.isValid,
                        user.nickname,
                        user.profileImgUrl,
                        user.description,
                        ExpressionUtils.as(select(userEval.score.avg()).from(userEval).where(userEval.userId.eq(user.id), userEval.isValid.eq(1)), "score")))
                .from(user)
                .where(user.id.in(userIds), user.isValid.eq(1))
                .fetch();

        return result.stream().map((simpleProfileResponse) -> SimpleProfileResponse.builder()
                .id(simpleProfileResponse.getId())
                .isValid(simpleProfileResponse.getIsValid())
                .nickname(simpleProfileResponse.getNickname())
                .profileImgUrl(simpleProfileResponse.getProfileImgUrl())
                .description(simpleProfileResponse.getDescription())
                .tags(jpaQueryFactory
                        .select(Projections.constructor(TagDto.class, tag))
                        .from(tag)
                        .where(tag.id.in(userTagRepository.findTagIdAllByUserId(simpleProfileResponse.getId())), tag.isValid.eq(1))
                        .fetch())
                .score(simpleProfileResponse.getScore())
                .build())
                .collect(Collectors.toList());
    }

    @Override
    public SimpleUserResponse findUserInfo(Long userId) {
        return SimpleUserResponse.builder()
                .id(userId)
                .progressStudies(jpaQueryFactory.select(participationHistory.studyId)
                        .from(participationHistory)
                        .where(participationHistory.userId.eq(userId),
                                participationHistory.isValid.eq(1))
                        .fetch())
                .bookmarks(jpaQueryFactory.select(bookmark.studyId)
                        .from(bookmark)
                        .where(bookmark.userId.eq(userId),
                                bookmark.isValid.eq(1))
                        .fetch())
                .likes(jpaQueryFactory.select(studyLike.studyId)
                        .from(studyLike)
                        .where(studyLike.userId.eq(userId),
                                studyLike.isValid.eq(1))
                        .fetch())
                .follows(jpaQueryFactory.select(follow.followeeId)
                        .from(follow)
                        .where(follow.followerId.eq(userId),
                                follow.isValid.eq(1))
                        .fetch())
                .build();
    }

    // badgeList
    private List<BadgeResponse> getBadgeResponsesByUserId(Long userId) {
        return userBadgeRepository.findBadgeIdAllByUserId(userId).stream()
                        .filter(userBadge -> userBadge.getBadge().isValid())
                        .map(userBadge -> {
                            Badge badge = userBadge.getBadge();
                            return BadgeResponse.builder()
                                    .name(badge.getName())
                                    .imgUrl(badge.getImgUrl())
                                    .description(badge.getDescription())
                                    .id(badge.getId())
                                    .build();
                        })
                        .collect(Collectors.toList());
    }
}

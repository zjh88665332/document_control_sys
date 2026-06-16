package com.docmanage.service;

import com.docmanage.common.BusinessException;
import com.docmanage.dto.*;
import com.docmanage.entity.Friend;
import com.docmanage.entity.User;
import com.docmanage.repository.FriendRepository;
import com.docmanage.repository.UserRepository;
import com.docmanage.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendService {

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_ACCEPTED = 1;
    private static final int STATUS_REJECTED = 2;

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final WebSocketNotificationService webSocketNotificationService;

    @Transactional(readOnly = true)
    public List<FriendSearchVO> searchFriends(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw new BusinessException("搜索关键词不能为空");
        }

        User user = findCurrentUser();
        return userRepository.searchByKeyword(user.getId(), keyword).stream()
                .map(FriendSearchVO::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void sendApply(FriendApplyRequest request) {
        User user = findCurrentUser();
        Long targetId = request.getFriendId();

        if (targetId.equals(user.getId())) {
            throw new BusinessException("不能添加自己为好友");
        }

        userRepository.findById(targetId)
                .filter(u -> "user".equals(u.getRole()) && u.getStatus() == 1)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        if (friendRepository.existsFriendship(user.getId(), targetId, STATUS_ACCEPTED)) {
            throw new BusinessException("你们已经是好友了");
        }
        if (friendRepository.existsPendingApply(user.getId(), targetId)) {
            throw new BusinessException("已有待处理的好友申请");
        }

        Friend friend = new Friend();
        friend.setUserId(user.getId());
        friend.setFriendId(targetId);
        friend.setApplyMessage(request.getApplyMessage());
        friend.setStatus(STATUS_PENDING);
        friend.setIsDel(0);

        friendRepository.save(friend);
        webSocketNotificationService.pushToUser(targetId, WsNotificationMessage.builder()
                .type("friend")
                .title("新的好友申请")
                .content(user.getRealName() + " 请求添加您为好友")
                .targetId(targetId)
                .build());
    }

    @Transactional(readOnly = true)
    public PageResult<ReceivedApplyVO> listReceivedApplies(int pageNum, int pageSize) {
        User user = findCurrentUser();
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<Friend> page = friendRepository.findByFriendIdOrderByApplyTimeDesc(user.getId(), pageable);

        Map<Long, User> applicantMap = loadUsers(page.getContent().stream()
                .map(Friend::getUserId)
                .collect(Collectors.toList()));

        List<ReceivedApplyVO> list = page.getContent().stream()
                .map(f -> ReceivedApplyVO.from(f, applicantMap.get(f.getUserId())))
                .collect(Collectors.toList());

        return PageResult.<ReceivedApplyVO>builder()
                .total(page.getTotalElements())
                .list(list)
                .build();
    }

    @Transactional
    public void handleApply(Long id, HandleFriendApplyRequest request) {
        if (request.getStatus() != STATUS_ACCEPTED && request.getStatus() != STATUS_REJECTED) {
            throw new BusinessException("状态值无效");
        }

        User user = findCurrentUser();
        Friend friend = friendRepository.findByIdAndFriendIdAndStatusAndIsDel(id, user.getId(), STATUS_PENDING, 0)
                .orElseThrow(() -> new BusinessException(404, "申请记录不存在"));

        friend.setStatus(request.getStatus());
        friend.setReplyMessage(request.getReplyMessage());
        friend.setHandleTime(LocalDateTime.now());
        friendRepository.save(friend);
    }

    @Transactional(readOnly = true)
    public PageResult<SentApplyVO> listSentApplies(int pageNum, int pageSize) {
        User user = findCurrentUser();
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<Friend> page = friendRepository.findByUserIdOrderByApplyTimeDesc(user.getId(), pageable);

        Map<Long, User> targetMap = loadUsers(page.getContent().stream()
                .map(Friend::getFriendId)
                .collect(Collectors.toList()));

        List<SentApplyVO> list = page.getContent().stream()
                .map(f -> SentApplyVO.from(f, targetMap.get(f.getFriendId())))
                .collect(Collectors.toList());

        return PageResult.<SentApplyVO>builder()
                .total(page.getTotalElements())
                .list(list)
                .build();
    }

    @Transactional(readOnly = true)
    public PageResult<FriendListItemVO> listFriends(String name, String phone, int pageNum, int pageSize) {
        User user = findCurrentUser();
        List<Friend> friendships = friendRepository.findAcceptedFriends(user.getId());

        List<FriendListItemVO> all = new ArrayList<>();
        for (Friend friendship : friendships) {
            Long friendUserId = friendship.getUserId().equals(user.getId())
                    ? friendship.getFriendId()
                    : friendship.getUserId();
            User friendUser = userRepository.findById(friendUserId).orElse(null);
            if (friendUser == null) {
                continue;
            }
            if (StringUtils.hasText(name) && (friendUser.getRealName() == null
                    || !friendUser.getRealName().contains(name))) {
                continue;
            }
            if (StringUtils.hasText(phone) && (friendUser.getPhone() == null
                    || !friendUser.getPhone().contains(phone))) {
                continue;
            }
            all.add(FriendListItemVO.from(friendship, friendUser));
        }

        int fromIndex = Math.min((pageNum - 1) * pageSize, all.size());
        int toIndex = Math.min(fromIndex + pageSize, all.size());
        List<FriendListItemVO> pageList = all.subList(fromIndex, toIndex);

        return PageResult.<FriendListItemVO>builder()
                .total(all.size())
                .list(pageList)
                .build();
    }

    @Transactional
    public void deleteFriend(Long friendId) {
        User user = findCurrentUser();

        if (!friendRepository.existsFriendship(user.getId(), friendId, STATUS_ACCEPTED)) {
            throw new BusinessException(404, "好友不存在");
        }

        friendRepository.findAcceptedFriends(user.getId()).stream()
                .filter(f -> {
                    Long otherId = f.getUserId().equals(user.getId()) ? f.getFriendId() : f.getUserId();
                    return otherId.equals(friendId);
                })
                .forEach(this::markDeleted);
    }

    public boolean isFriend(Long userId, Long friendId) {
        return friendRepository.existsFriendship(userId, friendId, STATUS_ACCEPTED);
    }

    private void markDeleted(Friend friend) {
        friend.setIsDel(1);
        friendRepository.save(friend);
    }

    private Map<Long, User> loadUsers(List<Long> userIds) {
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private User findCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "未授权"));
    }
}

package com.docmanage.service;

import com.docmanage.dto.OperationLogVO;
import com.docmanage.dto.PageResult;
import com.docmanage.entity.OperationLog;
import com.docmanage.repository.OperationLogRepository;
import com.docmanage.security.SecurityUtils;
import com.docmanage.service.support.AdminAuthSupport;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogRepository operationLogRepository;
    private final AdminAuthSupport adminAuth;

    @Transactional
    public void log(String module, String action, Long targetId, String targetName, String detail) {
        OperationLog log = new OperationLog();
        log.setUserId(SecurityUtils.getCurrentUserIdOrNull());
        log.setUsername(SecurityUtils.getCurrentUsernameOrNull());
        log.setModule(module);
        log.setAction(action);
        log.setTargetId(targetId);
        log.setTargetName(targetName);
        log.setDetail(detail);
        log.setIp(resolveClientIp());
        operationLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public PageResult<OperationLogVO> listLogs(String module, String username, int pageNum, int pageSize) {
        adminAuth.requireAdmin();
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<OperationLog> page = operationLogRepository.findByFilters(
                StringUtils.hasText(module) ? module : null,
                StringUtils.hasText(username) ? username : null,
                pageable
        );
        return PageResult.from(page, OperationLogVO::from);
    }

    private String resolveClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return null;
            }
            HttpServletRequest request = attrs.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (!StringUtils.hasText(ip)) {
                ip = request.getRemoteAddr();
            } else {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        } catch (Exception e) {
            return null;
        }
    }
}

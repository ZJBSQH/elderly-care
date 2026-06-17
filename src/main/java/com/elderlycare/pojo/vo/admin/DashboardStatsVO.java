package com.elderlycare.pojo.vo.admin;

import java.math.BigDecimal;

/**
 * 仪表盘统计数据 VO
 */
public record DashboardStatsVO(
    /**
     * 总用户数
     */
    Long totalUsers,

    /**
     * 老人数量
     */
    Long elderCount,

    /**
     * 家属数量
     */
    Long familyCount,

    /**
     * 管理员数量
     */
    Long adminCount,

    /**
     * 今日健康记录数
     */
    Integer todayHealthRecords,

    /**
     * 正在服药的数量
     */
    Integer activeMedicines,

    /**
     * 今日健康警告数
     */
    Integer todayWarnings,

    /**
     * 健康正常率（百分比）
     */
    BigDecimal normalRate,

    /**
     * 资讯总数
     */
    Long totalNews,

    /**
     * 已发布资讯数
     */
    Long publishedNews,

    /**
     * 公告总数
     */
    Integer totalAnnouncements
) {}

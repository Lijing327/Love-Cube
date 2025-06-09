package com.lovecube.backend.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author Admin
 * 用于返回用户信息和相似度分数。
 */
@Data
public class UserSimilarityDTO
{
    private String Username;//用户名
    private Integer Gender;//性别
    private int age;//年龄
    private Date BirthDate;//出生日期
    private String Location;// 用户所在地
    private String occupation;//工作

    private double similarityScore;//相似性得分
}

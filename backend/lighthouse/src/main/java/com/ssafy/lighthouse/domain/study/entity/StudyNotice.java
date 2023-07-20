package com.ssafy.lighthouse.domain.study.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyNotice {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String createdAt;
	private int isValid;
	private int studyId;
	private String content;

	@Builder
	public StudyNotice(int studyId, String content) {
		this.studyId = studyId;
		this.content = content;
	}
}

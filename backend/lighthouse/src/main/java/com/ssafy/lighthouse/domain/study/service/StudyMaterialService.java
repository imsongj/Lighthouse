package com.ssafy.lighthouse.domain.study.service;

import java.util.List;

import com.ssafy.lighthouse.domain.study.dto.StudyMaterialDto;
import com.ssafy.lighthouse.domain.study.entity.StudyMaterial;

public interface StudyMaterialService {
	List<StudyMaterial> findAllByStudyId(Long studyId);
	Long createMaterial(StudyMaterialDto.Req dto);
	Long updateMaterial(Long id, StudyMaterialDto.Req dto);
	Long removeMaterial(Long id);
	StudyMaterial findById(Long id);
}
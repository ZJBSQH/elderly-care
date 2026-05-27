package com.elderlycare.user.service;

import com.elderlycare.common.core.result.Result;
import com.elderlycare.user.dto.ElderInfoDTO;
import com.elderlycare.user.dto.FamilyBindConfirmRequest;
import com.elderlycare.user.dto.FamilyBindRequest;

import java.util.List;
import java.util.Map;

public interface UserService {

    Result<String> generateElderQRCode();

    Result<ElderInfoDTO> parseQRCode(String qrCodeToken);

    Result<Void> bindFamily(FamilyBindRequest request);

    Result<Void> bindElderByQRCode(FamilyBindConfirmRequest request);

    Result<Void> unbindFamily(Integer elderId);

    Result<List<Map<String, Object>>> getBoundElders();

    Result<List<Map<String, Object>>> getBoundFamilyMembers(Integer elderId);

}

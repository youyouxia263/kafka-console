package com.wubing.kafka.agent.service;

import com.wubing.kafka.agent.domain.entity.AclInfo;
import com.wubing.kafka.agent.domain.request.AclConfigReq;
import com.wubing.kafka.agent.domain.response.AclsResponse;
import com.wubing.kafka.agent.utils.ResultData;

public interface AclOpService {
    ResultData<String> createAcl(AclConfigReq aclConfigReq);
    ResultData<AclsResponse> listAclLimits(int pageIndex, int pageSize);
    ResultData<String> deleteAclLimits(AclInfo aclInfo);
}

package com.wubing.kafka.agent.service;

import com.wubing.kafka.agent.domain.request.AclUserConfigReq;
import com.wubing.kafka.agent.domain.response.AclUsersResponse;

import com.wubing.kafka.agent.utils.ResultData;



public interface AclUserService {
    ResultData<String> upsertAclUser(AclUserConfigReq aclUserConfigReq);
    ResultData<AclUsersResponse> getAclUsers(int pageIndex, int pageSize);
    ResultData<String> deleteAclUser(String aclUserName);
}

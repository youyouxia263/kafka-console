package com.wubing.kafka.agent.service.impl;

import com.wubing.kafka.agent.config.KafkaClientPool;
import com.wubing.kafka.agent.constants.KafkaOpConstants;
import com.wubing.kafka.agent.constants.ResponeMsg;
import com.wubing.kafka.agent.domain.request.AclUserConfigReq;
import com.wubing.kafka.agent.domain.response.AclUsersResponse;
import com.wubing.kafka.agent.service.AclUserService;
import com.wubing.kafka.agent.utils.ResultData;
import com.wubing.kafka.agent.utils.ReturnCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.security.scram.internals.ScramFormatter;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

@Slf4j
@Service
public class AclUserServiceImpl implements AclUserService {

    @Override
    public ResultData<String> upsertAclUser(AclUserConfigReq aclUserConfigReq) {
        if (aclUserConfigReq.getAclUser().equals(KafkaClientPool.kafkaServerConfig.getSuperUser()) ) {
            log.warn("Super User is not support update!");
            return ResultData.fail(ReturnCode.RC307.getCode(), ReturnCode.RC307.getMessage() );
        }
        try {
            AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
            AlterUserScramCredentialsResult alterUserScramCredentialsResult = adminClient.alterUserScramCredentials(
                    Arrays.asList(
                            // scram-256
                            new UserScramCredentialUpsertion(
                                    aclUserConfigReq.getAclUser(),
                                    new ScramCredentialInfo(ScramMechanism.SCRAM_SHA_256, KafkaOpConstants.SCRAM_ITERATIONS),
                                    useBase64Decode(aclUserConfigReq.getAclUserPassword()).getBytes(StandardCharsets.UTF_8),
                                    ScramFormatter.secureRandomBytes(new SecureRandom())),

                            // scram-512
                            new UserScramCredentialUpsertion(
                                    aclUserConfigReq.getAclUser(),
                                    new ScramCredentialInfo(ScramMechanism.SCRAM_SHA_512, KafkaOpConstants.SCRAM_ITERATIONS),
                                    useBase64Decode(aclUserConfigReq.getAclUserPassword()).getBytes(StandardCharsets.UTF_8),
                                    ScramFormatter.secureRandomBytes(new SecureRandom()))
                    ),
                    new AlterUserScramCredentialsOptions().timeoutMs(KafkaOpConstants.ADMIN_CLIENT_TIMEOUT_MS)
            );
            alterUserScramCredentialsResult.all().get();
        }catch (Exception e) {
            log.error("exception: ", e);
            return ResultData.fail(ReturnCode.RC301.getCode(),  e.getCause() == null ?e.getMessage(): e.getCause().getMessage());
        }
        return ResultData.success(ResponeMsg.UPSERT_ACL_USER_SUCCESS);
    }

    @Override
    public ResultData<AclUsersResponse> getAclUsers(int pageIndex, int pageSize) {
        AclUsersResponse aclUsersResponse = new AclUsersResponse();
        try {
            AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
            DescribeUserScramCredentialsResult describeUserScramCredentialsResult = adminClient.describeUserScramCredentials();

            Map<String, UserScramCredentialsDescription> descriptionMap = describeUserScramCredentialsResult.all().get();
            List<AclUsersResponse.AclUserInfo> kafkaUserList = new ArrayList<>();
            for (Map.Entry<String, UserScramCredentialsDescription> entry: descriptionMap.entrySet()) {
                if (!entry.getKey().equals(KafkaClientPool.kafkaServerConfig.getSuperUser()) ) {
                    kafkaUserList.add(new AclUsersResponse.AclUserInfo(entry.getKey(), null, "SCRAM", new Properties()));
                }
            }
            aclUsersResponse.setTotalCount(kafkaUserList.size());
            if (kafkaUserList.size() <= pageSize)
            {
                aclUsersResponse.setItem(kafkaUserList);
            }else {
                aclUsersResponse.setItem(kafkaUserList.subList((pageIndex - 1) * pageSize, Math.min((pageIndex * pageSize), kafkaUserList.size())));
            }
        }catch (Exception e) {
            log.error("exception: ", e);
            return ResultData.fail(ReturnCode.RC301.getCode(),  e.getCause() == null ?e.getMessage(): e.getCause().getMessage());
        }
        return ResultData.success(aclUsersResponse);
    }


    @Override
    public ResultData<String> deleteAclUser(String aclUserName) {
        if (aclUserName.equals(KafkaClientPool.kafkaServerConfig.getSuperUser()) ) {
            log.warn("Super User is not support delete!");
            return ResultData.fail(ReturnCode.RC307.getCode(), ReturnCode.RC307.getMessage() );
        }
        try {
            AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
            AlterUserScramCredentialsResult alterUserScramCredentialsResult = adminClient.alterUserScramCredentials(
                    Arrays.asList(
                            // scram-256
                            new UserScramCredentialDeletion(
                                    aclUserName,
                                    ScramMechanism.SCRAM_SHA_256),
                            // scram-512
                            new UserScramCredentialDeletion(
                                    aclUserName,
                                    ScramMechanism.SCRAM_SHA_512)
                    ),
                    new AlterUserScramCredentialsOptions().timeoutMs(KafkaOpConstants.ADMIN_CLIENT_TIMEOUT_MS)
            );
            alterUserScramCredentialsResult.all().get();
        }catch (Exception e) {
            log.error("exception: ", e);
            return ResultData.fail(ReturnCode.RC302.getCode(),  e.getCause() == null ?e.getMessage(): e.getCause().getMessage());
        }
        return ResultData.success(ResponeMsg.DELETE_ACL_USER_SUCCESS);
    }

    private String useBase64Decode(String password) {
        return new String(Base64.getDecoder().decode(password), StandardCharsets.UTF_8);
    }
}

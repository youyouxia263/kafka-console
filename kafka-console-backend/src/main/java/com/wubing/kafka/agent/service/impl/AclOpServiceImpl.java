package com.wubing.kafka.agent.service.impl;

import com.wubing.kafka.agent.config.KafkaClientPool;
import com.wubing.kafka.agent.constants.KafkaOpConstants;
import com.wubing.kafka.agent.constants.ResponeMsg;
import com.wubing.kafka.agent.domain.entity.AclInfo;
import com.wubing.kafka.agent.domain.request.AclConfigReq;
import com.wubing.kafka.agent.domain.response.AclsResponse;
import com.wubing.kafka.agent.service.AclOpService;
import com.wubing.kafka.agent.utils.KafkaOpUtil;
import com.wubing.kafka.agent.utils.ResultData;
import com.wubing.kafka.agent.utils.ReturnCode;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AccessControlEntryFilter;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourcePatternFilter;
import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@Slf4j
@Service
public class AclOpServiceImpl implements AclOpService {

    @Override
    public ResultData<String> createAcl(AclConfigReq aclConfigReq) {
        List<AclInfo> aclInfoList = new ArrayList<>();
        switch (aclConfigReq.getUseType()) {
            case KafkaOpConstants.ACL_PRODUCER_TYPE:
                aclConfigReq.getPrincipals().parallelStream().forEach(principal -> aclConfigReq.getTopicNames().parallelStream()
                        .forEach(topicName -> aclInfoList.add(new AclInfo(principal, aclConfigReq.getAclClientHost(), aclConfigReq.getAclOperation(),
                                aclConfigReq.getAclPermissionType(), KafkaOpConstants.ACL_RESOURCE_TYPE_TOPIC, topicName, aclConfigReq.getTopicPatternType()))));
                break;
            case KafkaOpConstants.ACL_CONSUMER_TYPE:
                aclConfigReq.getPrincipals().parallelStream().forEach(principal -> {
                    aclConfigReq.getTopicNames().parallelStream()
                            .forEach(topicName -> aclInfoList.add(new AclInfo(principal, aclConfigReq.getAclClientHost(), aclConfigReq.getAclOperation(),
                                    aclConfigReq.getAclPermissionType(), KafkaOpConstants.ACL_RESOURCE_TYPE_TOPIC, topicName, aclConfigReq.getTopicPatternType())));
                    aclConfigReq.getConsumerGroupNames().parallelStream().forEach(consumerGroupName -> aclInfoList.add(new AclInfo(principal, aclConfigReq.getAclClientHost(), aclConfigReq.getAclOperation(),
                            aclConfigReq.getAclPermissionType(), KafkaOpConstants.ACL_RESOURCE_TYPE_GROUP, consumerGroupName, aclConfigReq.getGroupPatternType())));
                });
                break;
            case KafkaOpConstants.ACL_USER_DEFINE_TYPE:
                aclConfigReq.getPrincipals().parallelStream().forEach(principal ->{
                    // 判断是否为 cluster资源类型，cluster资源类型，资源名固定为kafka-cluster
                    if (aclConfigReq.getResourceType() == KafkaOpConstants.ACL_RESOURCE_TYPE_CLUSTER) {
                        aclInfoList.add(new AclInfo(principal,  aclConfigReq.getAclClientHost(), aclConfigReq.getAclOperation(),
                                aclConfigReq.getAclPermissionType(), aclConfigReq.getResourceType(), KafkaOpConstants.ACL_RESOURCE_CLUSTER_NAME, aclConfigReq.getResourcePatternType()));
                    }else {
                        if (aclConfigReq.getResourceNames().isEmpty()) {
                            aclConfigReq.setResourceNames(Lists.newArrayList("*"));
                        }
                        aclConfigReq.getResourceNames().parallelStream()
                                .forEach(resourceName -> aclInfoList.add(new AclInfo(principal,  aclConfigReq.getAclClientHost(), aclConfigReq.getAclOperation(),
                                        aclConfigReq.getAclPermissionType(), aclConfigReq.getResourceType(), resourceName, aclConfigReq.getResourcePatternType()))
                                );
                    }
                });
                break;
            default:
                return ResultData.fail(ReturnCode.RC306.getCode(), ReturnCode.RC306.getMessage());
        }
        try {
            handleAclConfig(aclInfoList);
        }catch (Exception e) {
            log.error("exception: ", e);
            return ResultData.fail(ReturnCode.RC303.getCode(),  e.getCause() == null ?e.getMessage(): e.getCause().getMessage());
        }
        return ResultData.success(ResponeMsg.UPSERT_ACL_LIMITS_SUCCESS);
    }

    private void handleAclConfig(List<AclInfo> aclInfoList) throws ExecutionException, InterruptedException {
        AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
        CreateAclsResult createAclsResult = adminClient.createAcls(
                aclInfoList.stream().map(aclInfo ->
                        new AclBinding(
                                new ResourcePattern(
                                        KafkaOpUtil.getKafkaResourceType(aclInfo.getResourceType()),
                                        aclInfo.getResourceName(),
                                        KafkaOpUtil.getKafkaResourcePatternType(aclInfo.getResourcePatternType())
                                ),
                                new AccessControlEntry(
                                        new KafkaPrincipal(KafkaPrincipal.USER_TYPE, aclInfo.getPrincipal()).toString(),
                                        aclInfo.getAclClientHost(),
                                        KafkaOpUtil.getKafkaAclOperation(aclInfo.getAclOperation()),
                                        KafkaOpUtil.getKafkaAclPermissionType(aclInfo.getAclPermissionType())
                                )
                        )
                ).collect(Collectors.toList()),
                new CreateAclsOptions().timeoutMs(KafkaOpConstants.ADMIN_CLIENT_TIMEOUT_MS)
        );
        createAclsResult.all().get();
    }

    @Override
    public ResultData<AclsResponse> listAclLimits(int pageIndex, int pageSize) {
        AclsResponse aclsResponse = new AclsResponse();
        try {
            AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
            DescribeAclsResult describeAclsResult = adminClient.describeAcls(AclBindingFilter.ANY);
            Collection<AclBinding> aclBindings = describeAclsResult.values().get();
            List<AclInfo> aclConfigList = new ArrayList<>();
            aclBindings.forEach(aclBinding -> {
                ResourcePattern resourcePattern = aclBinding.pattern();
                AccessControlEntry accessControlEntry = aclBinding.entry();
                aclConfigList.add(new AclInfo(accessControlEntry.principal(), accessControlEntry.host(),
                                accessControlEntry.operation().code(), accessControlEntry.permissionType().code(),
                                resourcePattern.resourceType().code(), resourcePattern.name(), resourcePattern.patternType().code() )
                        );

            });
            aclsResponse.setTotalCount(aclConfigList.size());
            if (aclConfigList.size() <= pageSize)
            {
                aclsResponse.setItem(aclConfigList);
            }else {
                aclsResponse.setItem(aclConfigList.subList((pageIndex - 1) * pageSize, Math.min((pageIndex * pageSize), aclConfigList.size())));
            }
        }catch (Exception e) {
            log.error("exception: ", e);
            return ResultData.fail(ReturnCode.RC304.getCode(),  e.getCause() == null ?e.getMessage(): e.getCause().getMessage());
        }
        return ResultData.success(aclsResponse);
    }

    @Override
    public ResultData<String> deleteAclLimits(AclInfo aclInfo) {
        try {
            AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
            // 构造需要删除的权限
            AclBindingFilter aclBindingFilter = new AclBindingFilter(
                    new ResourcePatternFilter(KafkaOpUtil.getKafkaResourceType(aclInfo.getResourceType()) , aclInfo.getResourceName(),
                            KafkaOpUtil.getKafkaResourcePatternType(aclInfo.getResourcePatternType()) ),
                    new AccessControlEntryFilter(
                            new KafkaPrincipal(KafkaPrincipal.USER_TYPE, aclInfo.getPrincipal()).toString(),
                            aclInfo.getAclClientHost(),
                            KafkaOpUtil.getKafkaAclOperation(aclInfo.getAclOperation()),
                            KafkaOpUtil.getKafkaAclPermissionType(aclInfo.getAclPermissionType())
                    )
            );

            DeleteAclsResult deleteAclsResult = adminClient.deleteAcls(Collections.singletonList(aclBindingFilter), new DeleteAclsOptions().timeoutMs(KafkaOpConstants.ADMIN_CLIENT_TIMEOUT_MS));
            deleteAclsResult.all().get();
        }catch (Exception e) {
            log.error("exception: ", e);
            return ResultData.fail(ReturnCode.RC305.getCode(),  e.getCause() == null ?e.getMessage(): e.getCause().getMessage());
        }
        return ResultData.success(ResponeMsg.DELETE_ACL_LIMITS_SUCCESS);
    }
}

package com.wubing.kafka.agent.utils;

public enum ReturnCode {
    /**操作成功**/
    RC200(200,"success"),


    /**topic操作失败**/
    RC001(101000001, "create topic failed"),
    RC002(101000002,"delete topic failed"),
    RC003(101000003,"topic already exists"),
    RC004(101000004,"get topic list failed"),
    RC005(101000005,"get topic detail failed"),
    RC006(101000006,"get topic property failed"),
    RC007(101000007,"topic property param is empty"),
    RC008(101000008,"update topic property failed"),
    RC009(101000009,"this is unknown topic"),
    RC010(101000010,"get consumerInfo for topic failed"),
    RC011(101000011,"update partition count of topic failed"),
    RC012(101000012,"update replicas failed"),
    RC013(101000013,"send message failed!"),
    RC014(101000014, "delete topic failed because there are online producers or consumers occupying this topic"),
    RC015(101000015,"import all topics failed"),
    RC016(101000016,"export all topics failed"),
    /**consumerGroup操作失败**/
    RC101(101000101,"consumer group delete failed"),
    RC102(101000102,"get consumer group list failed"),
    RC103(101000103,"cannot delete this consumer group since it is in state (Dead/Stable)"),

    /**broker操作处理**/
    RC201(101000201,"List kafka brokers failed "),
    RC202(101000202,"Get kafka broker config failed "),
    RC203(101000203,"Update kafka broker config failed "),
    RC204(101000204,"List all producers failed "),
    RC205(101000205,"List all consumers failed "),

    /**ACL操作处理**/
    RC301(101000301,"Upsert Acl user failed"),
    RC302(101000302,"Delete Acl user failed"),
    RC303(101000303,"Create Acl limits failed"),
    RC304(101000304,"Get Acl limits failed"),
    RC305(101000305,"Delete Acl limits failed"),
    RC306(101000306,"Current acl use type is not supported"),
    RC307(101000307,"Super acl user is not support update or delete!"),
    /**异常处理**/
    RC401(101000401,"Internal network exception"),
    RC402(101000402,"MethodArgumentNotValid"),
    RC403(101000403,"Kafka Client "),
    RC404(101000404,"Kafka Client ExecutionException "),
    RC405(101000405,"Kafka agent InterruptedException "),
    RC406(101000406,"Kafka agent IllegalArgumentException "),

    /**offset操作处理**/
    RC501(101000501,"Reset offset failed "),
    RC502(101000502,"The consumer group is in state (Dead/Stable) , resetting the offset is forbidden"),

    /**服务异常**/
    RC500(500,"The system is abnormal. Please try again later"),

    REQUEST_EXCEPTION(1000, "");

    /**自定义状态码**/
    private final int code;
    /**自定义描述**/
    private final String message;

    ReturnCode(int code, String message){
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

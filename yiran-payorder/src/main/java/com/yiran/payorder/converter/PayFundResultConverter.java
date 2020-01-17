package com.yiran.payorder.converter;

import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netfinworks.common.domain.Extension;
import com.netfinworks.common.lang.StringUtil;
import com.yiran.paychannel.enums.BizType;
import com.yiran.paychannel.enums.ExtensionKey;
import com.yiran.paychannel.enums.InstOrderStatus;
import com.yiran.paychannel.enums.YesNo;
import com.yiran.paychannel.utils.CommonConverter;
import com.yiran.payorder.domain.ChannelPayOrder;
import com.yiran.payorder.domain.PayInstOrder;
import com.yiran.payorder.domain.PayInstOrderResult;
import com.yiran.payorder.enums.ChannelRefundStatus;
import com.yiran.payorder.enums.InstOrderProcessStatus;
import com.yiran.payorder.enums.InstOrderResultStatus;
import com.yiran.payorder.enums.OrderRiskStatus;
import com.yiran.payorder.enums.PayFundResultCode;
import com.yiran.payorder.enums.PayOrderStatus;
import com.yiran.payorder.response.PayFundResult;
import com.yiran.payorder.utils.BizUtil;

/**
 * 机构订单结果-->CMF处理结果,  转换类
 * @author pandaa
 *
 */
public class PayFundResultConverter {
	private static final Logger logger = LoggerFactory.getLogger(PayFundResultConverter.class);
	
	public static PayFundResult convert(ChannelPayOrder channelPayOrder, PayInstOrder instOrder, PayInstOrderResult from) {
		PayFundResult to = new PayFundResult();
        if (BizType.FUNDIN.equals(from.getOrderType())) {
            populateFundinResult(from, to, instOrder, channelPayOrder);
        }
        boolean needBuildFinalResult = true;
        if (BizType.REFUND.equals(from.getOrderType())) {
            populateReFundResult(from, to, instOrder); //由PE来判断是否0005；CMF需要返回0001 + channelPayNo； 或者0004 + channelPayNo
            buildFinalResult(from, to, instOrder, channelPayOrder); // 需要返回PE：渠道编号，channelPayNo
            needBuildFinalResult = false;
        }

        if (BizType.FUNDOUT.equals(from.getOrderType())) {
            populateFundoutResult(from, to, instOrder, channelPayOrder);
        }

        //明确结果的，须把渠道编号、金额等信息返回给PE
        if (needBuildFinalResult && InstOrderProcessStatus.SUCCESS == from.getProcessStatus()) {
            buildFinalResult(from, to, instOrder, channelPayOrder);
        }
        to.setFormHtml(from.getFromHtml());
        return to;
    }

    /**
     * 出款结果.
     *
     * @param from
     * @param to
     */
    private static void populateFundoutResult(PayInstOrderResult from, PayFundResult to,
                                              PayInstOrder instOrder, ChannelPayOrder channelPayOrder) {

        if (InstOrderProcessStatus.SUBMIT_CMF_FAIL.equals(from.getProcessStatus())) {
            //如果保存cmf订单失败,明确失败
            to.setSuccess(false);
            to.setResultCode(PayFundResultCode.FAILED);
        } else if (InstOrderResultStatus.FAILURE == from.getStatus()
                   && InstOrderProcessStatus.SUCCESS == from.getProcessStatus()) {
            //如果机构订单结果是：失败,则明确失败
            to.setSuccess(false);
            to.setResultCode(PayFundResultCode.FAILED);
        } else if (InstOrderProcessStatus.SUBMIT_INST_SUCCESS == from.getProcessStatus()) {
            //提交机构成功，并且已明确出款渠道，须返回渠道编码及channelPayNo给PE
            to.setSuccess(true);
            to.setResultCode(PayFundResultCode.SUBMIT_INST);
            buildFinalResult(from, to, instOrder, channelPayOrder);
        } else if (InstOrderProcessStatus.SUCCESS == from.getProcessStatus()) {
            //提交机构成功，并且已明确出款渠道，须返回渠道编码及channelPayNo给PE
            to.setSuccess(true);
            if (InstOrderResultStatus.SUCCESSFUL.equals(from.getStatus())
                && PayOrderStatus.SUCCESSFUL.equals(channelPayOrder.getStatus())) { //防止拆分,保证结果一致
                to.setResultCode(PayFundResultCode.SUCCESS);
            } else if (InstOrderResultStatus.FAILURE.equals(from.getStatus())
                       && PayOrderStatus.FAILURE.equals(channelPayOrder.getStatus())) { //防止拆分,保证结果一致
                to.setResultCode(PayFundResultCode.FAILED);
            } else {
                //其它情况都为请求成功
                to.setSuccess(true);
                to.setResultCode(PayFundResultCode.REQUEST_SUCCESS);
            }
            buildFinalResult(from, to, instOrder, channelPayOrder);
        } else {
            //其它情况都为请求成功
            to.setSuccess(true);
            to.setResultCode(PayFundResultCode.REQUEST_SUCCESS);
        }
        to.setResultMessage(from.getMemo());
    }

    /**
     * 入款结果.
     *
     * @param from
     * @param to
     */
    private static void populateFundinResult(PayInstOrderResult from, PayFundResult to,
                                             PayInstOrder instOrder, ChannelPayOrder cmfOrder) {
        if (InstOrderProcessStatus.SUCCESS.equals(from.getProcessStatus())) { //处理成功
            to.setSuccess(true);

            //明确成功  or 明确失败, 风险订单 则是处理中,其它默认处理中
            PayFundResultCode resultCode;
            if (InstOrderResultStatus.SUCCESSFUL.equals(from.getStatus())
                && PayOrderStatus.SUCCESSFUL.equals(cmfOrder.getStatus())) {
                resultCode = PayFundResultCode.SUCCESS;
            } else if (InstOrderResultStatus.RISK.equals(from.getStatus())) {
                resultCode = PayFundResultCode.REQUEST_SUCCESS;
            } else if (InstOrderResultStatus.FAILURE.equals(from.getStatus())
                       && PayOrderStatus.FAILURE.equals(cmfOrder.getStatus())) {
                resultCode = PayFundResultCode.FAILED;
            } else {
                resultCode = PayFundResultCode.REQUEST_SUCCESS;
            }
            String paymentNo = from.getExtension().get(ExtensionKey.PAYMENT_SEQ_NO.key);
            if (!StringUtil.isEmpty(paymentNo)) {
                //无磁无密判断风险订单逻辑不同
                if (PayOrderStatus.IN_PROCESS.equals(cmfOrder.getStatus())) {
                    resultCode = PayFundResultCode.REQUEST_SUCCESS;
                }
            }
            to.setResultCode(resultCode);
        } else if (InstOrderProcessStatus.AWAITING.equals(from.getProcessStatus())) { //提交成功，待返回(异步)
            to.setSuccess(true);
            to.setResultCode(PayFundResultCode.REQUEST_SUCCESS);
            if (instOrder != null) {
                to.setInstOrderNo(instOrder.getInstOrderNo());
            }
        } else if (InstOrderProcessStatus.FAILURE.equals(from.getProcessStatus())) {
            to.setSuccess(false);

            //明确失败  or 正在处理该订单(包括CMF的补单任务)
            PayFundResultCode resultCode = (InstOrderResultStatus.FAILURE.equals(from.getStatus())) ? PayFundResultCode.FAILED
                : PayFundResultCode.IN_PROCESS;
            to.setResultCode(resultCode);
        } else if (InstOrderProcessStatus.SUBMIT_CMF_FAIL.equals(from.getProcessStatus())) {
            to.setSuccess(false);
            //如果保存cmf订单失败,明确失败
           PayFundResultCode resultCode = PayFundResultCode.FAILED;
            to.setResultCode(resultCode);
        } else {
            to.setSuccess(false);
            to.setResultCode(PayFundResultCode.IN_PROCESS); //返回给PE的是处理中状态，不能是未知异常(未知异常213会导致PE后续的状态跃迁异常)。
        }

        to.setResultMessage(from.getMemo());
        to.setExtension(CommonConverter.convertExtensionWithoutConvertKey(from.getExtension()));
        //add by 2016.8.16 增加返回统一结果码
        to.getExtension().add("instResultCode",from.getInstResultCode());

        //B2C验签Form返回给PE.
        String signForm = from.getExtension().get(ExtensionKey.PAGE_URL_FOR_SIGN.key);
        if (!StringUtils.isEmpty(signForm)) {
            to.getExtension().add(ExtensionKey.PAGE_URL_FOR_SIGN.key, signForm);
        }

        //风控特殊逻辑处理
        if (instOrder != null && OrderRiskStatus.IN_PROCESS.equals(instOrder.getRiskStatus())) {
            to.getExtension().add(ExtensionKey.IS_BANK_RISK.key, YesNo.YES.getCode());
        }
    }

    /**
     * 对于有明确结果的交易，须回传PE渠道编号、金额等信息.
     *
     * @param from
     * @param to
     */
    private static void buildFinalResult(PayInstOrderResult from, PayFundResult to,
                                         PayInstOrder instOrder, ChannelPayOrder channelPayOrder) {
        //渠道生成的订单号(发送给银行的)
        String channelPayNo = ChannelPayNoConverter.getReturnInstOrderNo(instOrder, from);
        if (StringUtil.isNotEmpty(channelPayNo)) {
            to.setChannelPayNo(channelPayNo);
        } else {
            to.setChannelPayNo(channelPayOrder.getPaySeqNo().toString());
        }

        if (instOrder != null) {
            to.setInstOrderNo(instOrder.getInstOrderNo());
        }
        if (to.getResultCode() != null && StringUtil.isEmpty(to.getResultMessage())) {
            to.setResultMessage(to.getResultCode().getMessage());
        }

        //金额
        if (null != from.getRealAmount()) {
            to.setAmount(from.getRealAmount().getAmount());
            to.setCurrencyCode(from.getRealAmount().getCurrency().getCurrencyCode());
        }

        //支付时间
        to.setInstPayTime(Calendar.getInstance().getTime());

        // 获取渠道编码
        String fundChannelCode = populateFundChannelCode(from, to, instOrder, channelPayOrder);
        if (!StringUtils.isEmpty(fundChannelCode)) {
            to.setFundsChannel(fundChannelCode);
        }

        if (from != null) {
            if (to.getExtension() == null) {
                to.setExtension(new Extension());
            }
            to.getExtension().add(ExtensionKey.UNITY_RESULT_CODE.key, from.getInstResultCode());
            String message = from.getExtension().get(ExtensionKey.UNITY_RESULT_MESSAGE.key);
            to.getExtension().add(ExtensionKey.UNITY_RESULT_MESSAGE.key,
                StringUtil.isEmpty(message) ? "" : message);
        }
    }

    /**
     * 获取渠道编码
     *
     * @param from
     * @param to
     */
    private static String populateFundChannelCode(PayInstOrderResult from, PayFundResult to,
                                                  PayInstOrder dbInstOrder, ChannelPayOrder channelPayOrder) {
        String fundChannelCode = null;

        if (dbInstOrder != null) {
            //cmfOrder里存的是PE请求的渠道编码.
            fundChannelCode = dbInstOrder.getFundChannelCode();
        }
        //确定性入款直接返回PE传入渠道编码
        if (null != channelPayOrder && BizUtil.isDeterminedFundin(channelPayOrder.getPaymentCode())) {
            fundChannelCode = channelPayOrder.getFundChannelCode();
        }

        return fundChannelCode;
    }

    /**
     * 充退结果.
     *
     * @param from
     * @param result
     */
    private static void populateReFundResult(PayInstOrderResult from, PayFundResult result,
                                             PayInstOrder instOrder) {
        if (logger.isDebugEnabled()) {
            logger.debug("返回PE订单" + from.getInstOrderId() + "结果" + from);
        }

        result.setSuccess(true);

        /**
         * 1.PE提交充退请求给CMF时，如果是手工充退，提交资金后台并返回code0004，如果是自动的(直连)并且是同步，直接获得结果，返回0000，否则返回0001(处理中)
         * 2. CMF得到异步的结果后通过MQ返回PE0000(退款成功)或者0003(退款失败)
         * 3. 在未提交机构时发生的异常返回0003直接失败，提交机构失败，处理结果不明的返回处理中
         */
        PayFundResultCode resultCode = getReFundRerturnCode(from, instOrder);
        result.setResultCode(resultCode);
        result.setResultMessage(resultCode == null ? from.getMemo() : resultCode.getMessage() + ","
                                                                      + from.getMemo());
        result.setExtension(CommonConverter.convertExtension(from.getExtension()));
    }

    private static PayFundResultCode getReFundRerturnCode(PayInstOrderResult from, PayInstOrder instOrder) {

        String code = from.getInstResultCode();

        if (InstOrderProcessStatus.SUBMIT_CMF_FAIL.equals(from.getProcessStatus())) {
            //如果保存cmf订单失败,明确失败
            return PayFundResultCode.FAILED;
        }
        //R_C:使用统一编码
        if (from.isReturnCodeRefacted()) {
            if (from.getStatus() == null) {
                return PayFundResultCode.IN_PROCESS;
            }
            switch (from.getStatus()) {
                case SUCCESSFUL:
                    if (InstOrderStatus.SUCCESSFUL.equals(instOrder.getStatus())) { //针对风控特殊逻辑处理,结果表成功,不一定成功,目前用不到
                        return PayFundResultCode.SUCCESS;
                    } else if (InstOrderStatus.FAILURE.equals(instOrder.getStatus())) {
                        return PayFundResultCode.FAILED;
                    }
                case FAILURE:
                    return PayFundResultCode.FAILED;
                case AWAITING:
                case QUESTIONABLE:
                case RISK:
                    return PayFundResultCode.REQUEST_SUCCESS;
                    //TODO:设置手工退款
                default:
                    return PayFundResultCode.IN_PROCESS;
            }
        } else {
            ChannelRefundStatus refundStatus = ChannelRefundStatus.getByCode(code);
            if (ChannelRefundStatus.RefundAccept.equals(refundStatus)
                || ChannelRefundStatus.RefundBankAccept.equals(refundStatus)
                || ChannelRefundStatus.RefundCheckPass.equals(refundStatus)
                || ChannelRefundStatus.RefundSubmitException.equals(refundStatus)
                || ChannelRefundStatus.SystemError.equals(refundStatus)) {
                return PayFundResultCode.REQUEST_SUCCESS;//RefundResultCode.RefundSubmited;0

            } else if (ChannelRefundStatus.RefundFailed.equals(refundStatus)
                       || InstOrderResultStatus.FAILURE.equals(from.getStatus())) {
                return PayFundResultCode.FAILED;

            } else if (ChannelRefundStatus.RefundSuccess.equals(refundStatus)
                       || (InstOrderResultStatus.SUCCESSFUL.equals(from.getStatus()) && InstOrderProcessStatus.SUCCESS
                           .equals(from.getProcessStatus()))) {
                if (InstOrderStatus.SUCCESSFUL.equals(instOrder.getStatus())) { //针对风控特殊逻辑处理,结果表成功,不一定成功,目前用不到
                    return PayFundResultCode.SUCCESS;
                } else if (InstOrderStatus.FAILURE.equals(instOrder.getStatus())) {
                    return PayFundResultCode.FAILED;
                }
            } else if (ChannelRefundStatus.RefundManualAccept.equals(refundStatus)
                       || ChannelRefundStatus.RefundManualCheckPass.equals(refundStatus)) {
                return PayFundResultCode.REFUND_MANUAL;
            }
            if (null == refundStatus && InstOrderResultStatus.FAILURE == from.getStatus()) { //明确失败（比如无可用路由），机构订单结果：失败
                return PayFundResultCode.FAILED;
            } else {
                return PayFundResultCode.IN_PROCESS; //不能返回未知异常给PE（会导致PE的后续状态跃迁异常），告诉PE在处理中.
            }
        }
    }

    /**
     * 依据cmf订单组装返回结果
     * 注: 只有当instOrder 和instOrderResult 不存在时方能调用
     * @param cmfOrder
     * @return
     */
    public static PayFundResult convert(ChannelPayOrder channelPayOrder) {
    	PayFundResult cmfFundResult = new PayFundResult();
        switch (channelPayOrder.getStatus()) {
            case SUCCESSFUL:
                cmfFundResult.setResultCode(PayFundResultCode.SUCCESS);
                cmfFundResult.setSuccess(true);
                break;
            case FAILURE:
            case CANCEL:
                cmfFundResult.setResultCode(PayFundResultCode.FAILED);
                cmfFundResult.setSuccess(false);
                break;
            default:
                cmfFundResult.setResultCode(PayFundResultCode.REQUEST_SUCCESS);
                cmfFundResult.setSuccess(false);
                break;
        }

        cmfFundResult.setAmount(channelPayOrder.getAmount().getAmount());
        cmfFundResult.setCurrencyCode(channelPayOrder.getAmount().getCurrency().getCurrencyCode());
        cmfFundResult.setChannelPayNo(channelPayOrder.getPaySeqNo().toString());
        return cmfFundResult;
    }
}

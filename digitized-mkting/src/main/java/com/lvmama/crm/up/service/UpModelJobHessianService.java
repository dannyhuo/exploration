package crm.up.service;

import com.lvmama.crm.enumerate.UpModelJobEnum.EXE_EVENT;
import com.lvmama.crm.enumerate.UpModelJobEnum.EXE_STATUS;

/**
 * Created by huoqiang on 18/7/2017.
 */
public interface UpModelJobHessianService {
    /**
     * 动态MR任务事件回调函数
     *
     * @param modelId
     *            模型ID
     * @param resultId
     *            结果ID，running状态时传null
     * @param intoStatus
     *            进入状态(running, normal)
     * @param exeEvent
     *            <ul>
     *            执行事件
     *            <li>//enqueue((short) 1),
     *            <li>//submitted((short) 2),
     *            <li>running((short) 3)开始执行事件，
     *            <li>runSucessful((short) 4)执行成功事件
     *            <li>runFailed((short) 5)执行失败事件
     *            <li>//manualStop((short) 6)手动停止事件
     *            <li>emergencyStop((short) 7)异常自动停止
     *            <ul>
     * @param errMsg
     *            异常信息，无异常为空
     * @return 事件处理结果：
     *         <ul>
     *         <li><intoStatus>为running状态时，成功返回resultId，不成功返回－1 <li>
     *         <intoStatus>为非running状态时，成功返回0，不成功返回－1
     *         </ul>
     */
    public Long doEvent(Long modelId, Long resultId, EXE_STATUS intoStatus, EXE_EVENT exeEvent, String errMsg);
}

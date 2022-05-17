import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowStatusManagerUtil;

WorkflowStatusManagerUtil.updateStatus(
	WorkflowConstants.getLabelStatus("approved"), workflowContext);
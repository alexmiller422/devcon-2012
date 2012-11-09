package org.alfresco.devcon2012.behaviours;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.alfresco.devcon2012.model.DevconModel;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnRemoveAspectPolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.UrlUtil;
import org.springframework.web.util.UriUtils;

import com.ibm.icu.text.MessageFormat;

public class AssetBehaviours implements NodeServicePolicies.OnAddAspectPolicy,  NodeServicePolicies.OnRemoveAspectPolicy, NodeServicePolicies.OnDeleteNodePolicy {

	private static final String ASSET_ADDED_HTML = 
	    "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">" +
	    "<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
		    "<body>" +
		        "You can view it <a href=\"{0}\">here</a>." +
		    "</body>" +
		"</html>";
			
	private static final String ASSET_REMOVED_HTML = 
		    "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">" +
		    "<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
			    "<body>" +
			        "The asset, &quot;{0}&quot; has been removed and is no longer available to purchase." +
			    "</body>" +
			"</html>";

	private ActionService actionService;
	private NodeService nodeService;
	private PolicyComponent policyComponent;
	private ServiceRegistry serviceRegistry;
	private SiteService siteService;
	
	public void setActionService(ActionService actionService)
	{
		this.actionService = actionService;
	}
	
	public void setNodeService(NodeService nodeService)
	{
		this.nodeService = nodeService;
	}
	
	public void setPolicyComponent(PolicyComponent policyComponent)
	{
		this.policyComponent = policyComponent;
	}
	
	public void setServiceRegistry(ServiceRegistry serviceRegistry)
	{
		this.serviceRegistry = serviceRegistry;
	}
	
	public void setSiteService(SiteService siteService)
	{
		this.siteService = siteService;
	}
	
	public void init() 
	{
		policyComponent.bindClassBehaviour(OnAddAspectPolicy.QNAME, DevconModel.ASPECT_ASSET, 
				                           new JavaBehaviour(this, "onAddAspect", Behaviour.NotificationFrequency.EVERY_EVENT));
		policyComponent.bindClassBehaviour(OnDeleteNodePolicy.QNAME, DevconModel.ASPECT_ASSET,
				                           new JavaBehaviour(this, "onDeleteNode", Behaviour.NotificationFrequency.EVERY_EVENT));
		policyComponent.bindClassBehaviour(OnRemoveAspectPolicy.QNAME, DevconModel.ASPECT_ASSET,
                						   new JavaBehaviour(this, "onRemoveAspect", Behaviour.NotificationFrequency.EVERY_EVENT));
	}
	
	public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived) 
	{
		assetRemoved(childAssocRef.getChildRef());
	}

	public void onRemoveAspect(NodeRef nodeRef, QName aspectTypeQName) 
	{
		assetRemoved(nodeRef);
	}

	public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) 
	{
        assetAdded(nodeRef);
	}

	private void assetAdded(NodeRef nodeRef)
	{
		try {
			SiteInfo siteInfo = siteService.getSite(nodeRef);
			
			String siteGroup = siteService.getSiteGroup(siteInfo.getShortName());
			String nodeName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
			
			String shareBaseUrl = UrlUtil.getShareUrl(serviceRegistry.getSysAdminParams());
			String nodeUrl = shareBaseUrl + "/page/site/" + siteInfo.getShortName() + "/document-details?nodeRef=" + UriUtils.encodeQueryParam(nodeRef.toString(), "UTF-8"); 
			
			String subject = MessageFormat.format("Asset, \"{0}\", added", new Object[]{nodeName});
			String html = MessageFormat.format(ASSET_ADDED_HTML, new Object[] {nodeUrl});
	
			sendMail(nodeRef, siteGroup, subject, html);
		}
		catch (UnsupportedEncodingException ex) 
		{
			throw new AlfrescoRuntimeException("Unexpected UnsupportedEncodingException", ex);
		}
	}
	private void assetRemoved(NodeRef nodeRef)
	{
		SiteInfo siteInfo = siteService.getSite(nodeRef);
		
		String siteGroup = siteService.getSiteGroup(siteInfo.getShortName());
		String nodeName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
		
		String subject = MessageFormat.format("Asset, \"{0}\", removed", new Object[]{nodeName});
		String html = MessageFormat.format(ASSET_REMOVED_HTML, new Object[] {nodeName});

		sendMail(nodeRef, siteGroup, subject, html);
	}

	private void sendMail(NodeRef nodeRef, String siteGroup, String subject, String html) {
		Action mailAction = actionService.createAction(MailActionExecuter.NAME);
		mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, subject);
		mailAction.setParameterValue(MailActionExecuter.PARAM_FROM, "noreply@alfresco.com");
		mailAction.setParameterValue(MailActionExecuter.PARAM_TO_MANY, siteGroup);
		mailAction.setParameterValue(MailActionExecuter.PARAM_HTML, html);
		actionService.executeAction(mailAction, nodeRef, true, true);
	}
}

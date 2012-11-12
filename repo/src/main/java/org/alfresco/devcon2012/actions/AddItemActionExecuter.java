package org.alfresco.devcon2012.actions;

import java.util.List;

import org.alfresco.devcon2012.model.DevconModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

public class AddItemActionExecuter extends ActionExecuterAbstractBase {

	private static final String NAME = "create-purchase-order-items";
	private static final String PARAM_ITEM = "item";
	
	private NodeService nodeService;
	
	public void setNodeService(NodeService nodeService)
	{
		this.nodeService = nodeService;
	}
	
	@Override
	protected void executeImpl(Action action, NodeRef actionedUponNodeRef) 
	{
		NodeRef item = (NodeRef)action.getParameterValue(PARAM_ITEM);
		nodeService.createAssociation(actionedUponNodeRef, item, DevconModel.ASSOC_PURCHASE_ORDER_ITEMS);
	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
	{
		 paramList.add(new ParameterDefinitionImpl(PARAM_ITEM, DataTypeDefinition.NODE_REF, true, getParamDisplayLabel(PARAM_ITEM), false, null));
	}

}

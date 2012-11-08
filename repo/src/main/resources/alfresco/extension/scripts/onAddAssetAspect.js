logger.log("onAddAspect policy code for devcon:asset");

var assetNode = behaviour.args[0];

var assetTitle = assetNode.properties["cm:name"];

// Get the issue creator's email address.
var assetCreatorId = assetNode.properties["cm:creator"];
var assetCreatorPerson = people.getPerson(assetCreatorId);
var assetCreatorEmailAddress = assetCreatorPerson.properties["cm:email"];
logger.log("asset creator email address = " + assetCreatorEmailAddress);

//create a 'mail' action
var mail = actions.create("mail");
mail.parameters.to = assetCreatorEmailAddress;
mail.parameters.subject = "Asset '" + assetTitle + "' has been added";
mail.parameters.from = "noreply@alfresco.com";

// But we'll keep it simple
mail.parameters.text = "Asset was created";

// execute action against the issueNode
mail.executeAsynchronously(assetNode);
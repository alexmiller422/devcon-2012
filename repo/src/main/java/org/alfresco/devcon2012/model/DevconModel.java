package org.alfresco.devcon2012.model;

import org.alfresco.service.namespace.QName;

/**
 * Model constants.
 * 
 * @author amiller
 *
 */
public interface DevconModel {
	
	static final String DEVCON_MODEL_1_0_URI = "http://www.alfresco.org/model/devcon/1.0";

    final static QName ASPECT_ASSET = QName.createQName(DEVCON_MODEL_1_0_URI, "asset");

}

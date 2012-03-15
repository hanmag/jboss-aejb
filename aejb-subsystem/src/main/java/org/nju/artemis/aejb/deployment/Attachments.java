package org.nju.artemis.aejb.deployment;

import java.util.Map;

import org.jboss.as.server.deployment.AttachmentKey;
import org.nju.artemis.aejb.component.AcContainer;

/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public class Attachments {

	/**
     * The name that uniquely identifies the deployment to the management layer across the domain.
     */
    public static final AttachmentKey<Map<String,AcContainer>> AEJB_INFO = AttachmentKey.create(Map.class);
}

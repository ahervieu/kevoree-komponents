package org.kevoree.library.javase.http.api.page;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Start;
import org.kevoree.log.Log;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 23/01/12
 * Time: 08:16
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentType
@Library(name = "web")
public abstract class AbstractParentHTTPHandler extends AbstractHTTPHandler {

    @Override
    @Start
    public void start() throws Exception {
        if (!urlPattern.endsWith(".*")) {
            urlPattern = urlPattern + ".*";
            Log.debug("Parent abstract page start with pattern = {}", urlPattern);
        }
        super.start();
    }
}
